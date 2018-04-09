package plzero.jvms;
import plzero.Variable;
import plzero.BString;
import java.io.IOException;
import java.util.Hashtable;
import org.apache.bcel.classfile.*;

public class ClassInfo {
	public final Pool pool;
	public final byte[] this_class;
	public final byte[] super_class;
	public final byte[][] interfaces;
	public final FieldInfo[] static_fields;
	public final FieldInfo[] fields;
	public final MethodInfo[] methods;

	//classname here has / in itn
	public static ClassInfo read(String classNameString) throws IOException {
		String classFileName=classNameString+".class";
		ClassParser cp=new ClassParser(classFileName);
		JavaClass jc=cp.parse();
		return new ClassInfo(jc);
	}

	//-----------------------------
	//classinfoloader.  I moved the code in here because it is so short
	static Hashtable<byte[],ClassInfo> classes;

	static {
		classes=new Hashtable<byte[],ClassInfo>(128);
	}

	//the class name here does not end in a class, and it has slashes instead of dots
	public static ClassInfo getClass(byte[] className) throws IOException {
		ClassInfo c=classes.get(className);
		if (c==null) {
			c=ClassInfo.read(new String(className));
			classes.put(className,c);
		}
		return c;
	}

	public ClassInfo(JavaClass jc) {
		pool=new Pool(jc.getConstantPool());
		int cnx=jc.getClassNameIndex();
		this_class=pool.get((short)cnx).getUtf8();
		int scx=jc.getSuperclassNameIndex();
		if (scx>0) {
			super_class=pool.get((short)scx).getUtf8();
		} else {
			super_class=null;
		}

		//get interfaces
		int[] ifxs=jc.getInterfaceIndices();
		//interfaces=new Variable[ifxs.length];
		interfaces=new byte[ifxs.length][];
		for (short s=0;s<ifxs.length;s++) {
			interfaces[s]=pool.get((short)ifxs[s]).getUtf8();
		}

		//get fields.  Do this in 2 phases. First count number of static fields and nonstatic fields
		Field[] fa=jc.getFields();
		int sf=0;
		int nsf=0;
		for (short s=0;s<fa.length;s++) {
			if (fa[s].isStatic()) {
				sf++;
			} else {
				nsf++;
			}
		}

		static_fields=new FieldInfo[sf];
		fields=new FieldInfo[nsf];
		sf=0;
		nsf=0;
		for (short s=0;s<fa.length;s++) {
			if (fa[s].isStatic()) {
				static_fields[sf]=new FieldInfo(this,fa[s]);
				sf++;
			} else {
				fields[nsf]=new FieldInfo(this,fa[s]);
				nsf++;
			}
		}

		//get methods
		Method[] ma=jc.getMethods();
		methods=new MethodInfo[ma.length];
		for (short s=0;s<ma.length;s++) {
			methods[s]=new MethodInfo(this,ma[s]);
		}
	}

	//probably null
	public ClassInfo getSuperClass() throws IOException {
		return getClass(super_class);
	}

	//change to match both on name and type
	//and look in superclass too
	public MethodInfo getMethod(byte[] methodName,byte[] methodType) {
		for (short s=0;s<methods.length;s++) {
			MethodInfo m=methods[s];
			//if (m.name.toString().equals(methodName)) {
			if (BString.equals(methodName,m.name) && BString.equals(methodType,m.descriptor)) {
				return m;
			}
		}
		try {
		ClassInfo superClass=getSuperClass();
		if (superClass!=null) {
			MethodInfo supermethod=superClass.getMethod(methodName,methodType);
			if (supermethod!=null) {
				return supermethod;
			}
		}
		} catch (Exception x) {
			System.out.println(x.getMessage());
		}
		System.out.println("no method "+(new String(methodName))+"with type "+(new String(methodType))+" found in "+this_class.toString());
		return null;
	}

	public FieldInfo getStaticField(byte[] fieldName) {
		for (short s=0;s<static_fields.length;s++) {
			FieldInfo fi=static_fields[s];
			//if (m.name.toString().equals(methodName)) {
			if (BString.equals(fieldName,fi.name)) {
				return fi;
			}
		}
		try {
		ClassInfo superClass=getSuperClass();
		if (superClass!=null) {
			FieldInfo superStaticField=superClass.getStaticField(fieldName);
			if (superStaticField!=null) {
				return superStaticField;
			}
		}
		} catch (Exception x) {
				System.out.println(x.getMessage());
		}
		System.out.println("no static field "+(new String(fieldName))+" found in "+this_class.toString());
		return null;
	}

	public FieldInfo getField(byte[] fieldName) {
		for (short s=0;s<fields.length;s++) {
			FieldInfo fi=fields[s];
			//if (m.name.toString().equals(methodName)) {
			if (BString.equals(fieldName,fi.name)) {
				return fi;
			}
		}
		try {
		ClassInfo superClass=getSuperClass();
		if (superClass!=null) {
			FieldInfo superField=superClass.getField(fieldName);
			if (superField!=null) {
				return superField;
			}
		}
		} catch (Exception x) {
				System.out.println(x.getMessage());
		}
		System.out.println("no field "+(new String(fieldName))+" found in "+this_class.toString());
		return null;
	}
}