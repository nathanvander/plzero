package plzero.jvms;
import plzero.BString;
import java.util.Arrays;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.Type;

//this is a bare-bones version of a method.
public class MethodInfo {
	public static final short ACC_STATIC = 0x0008;
	public final ClassInfo class_info;
	public final short access_flags;
	public final byte[] name;
	public final byte[] descriptor;
	public final byte[] return_type;
	public final byte param_count;
	public final byte[] code;

	public MethodInfo(ClassInfo c,Method m) {
		class_info=c;
		access_flags=(short)m.getAccessFlags();
		int name_index=m.getNameIndex();
		name=c.pool.get((short)name_index).getUtf8();
		int dx=m.getSignatureIndex();
		descriptor=c.pool.get((short)dx).getUtf8();

		return_type=getReturnType(descriptor);

		Code codeAttr=m.getCode();
		if (codeAttr!=null) {
			code=codeAttr.getCode();
		} else {
			code=null;
		}

		//method parameters
		//we don't care what the types are but we need the number
		param_count=getParamCount(m);
	}

	public static byte getParamCount(Method m) {
		Type[] ta=m.getArgumentTypes();
		if (ta==null) {return 0;}
		else {return (byte)ta.length;}
	}

	//public static byte getParamCount(Method m) {
	//	byte p=(byte)0;
	//	Attribute[] aa=m.getAttributes();
	//	if (aa==null) {
	//		return p;
	//	} else {
	//		for (short s=0;s<aa.length;s++) {
	//			Attribute a=aa[s];
	//			//System.out.println("DEBUG: MethodInfo "+a.getName());
	//			if (a.getName().equals("MethodParameters")) {
	//				MethodParameters mp=(MethodParameters)a;
	//				MethodParameter[] mpa=mp.getParameters();
	//				//a MethodParameter has no type
	//				return (byte)mpa.length;
	//			}
	//		}
	//		System.out.println("DEBUG: MethodInfo: getParamCount attribute MethodParameters not found");
	//		return p;
	//	}
	//}

	public boolean isStatic() {
		return (0 != (access_flags & ACC_STATIC));
	}

	public boolean nameEquals(byte[] b) {
		if (b==null) return false;
		else return Arrays.equals(name,b);
	}

	//the return type is at the end of the descriptor
	public static byte[] getReturnType(byte[] descriptor) {
		int ch=BString.indexOf(descriptor,(byte)')');
		return BString.substring(descriptor,ch+1,descriptor.length);
	}

}