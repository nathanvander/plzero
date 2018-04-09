package plzero.jvm;
import plzero.Variable;
import plzero.jvms.*;
import java.util.Hashtable;
import java.io.IOException;

/**
* A Static Object holds static fields.  There is one per ClassInfo.
*
* A Static object also has a static constructor "<clinit>", but that's more than I want to think about right now.
*/
public class StaticObj {
	public final ClassInfo class_info;
	public final StaticObj parent;
	private Variable[] values;

	static Hashtable<byte[],StaticObj> static_objects;
	static {
		static_objects=new Hashtable<byte[],StaticObj>(128);
	}

	//get a staticObj, given the classname
	public static StaticObj getStatic(byte[] className) throws IOException {
		StaticObj so=static_objects.get(className);
		if (so==null) {
			ClassInfo c=ClassInfo.getClass(className);
			so=new StaticObj(c);
			static_objects.put(className,so);
		}
		return so;
	}

	private StaticObj(ClassInfo cinfo) throws IOException {
		class_info=cinfo;

		//see if there is a parent
		if (cinfo.super_class!=null) {
			parent=getStatic(cinfo.super_class);
		} else {
			parent=null;
		}

		if (cinfo.static_fields!=null && cinfo.static_fields.length>0) {
			values=new Variable[cinfo.static_fields.length];
		}

		//init values
		for (short s=0;s<values.length;s++) {
			FieldInfo fi=cinfo.static_fields[s];
			if (fi.init_value!=null) {
				values[s]=fi.init_value;
			}
		}
	}

	public short getNumFields() {return (short)values.length;}

	public String name(short fnum) {return class_info.static_fields[fnum].name.toString();}
	public String descriptor(short fnum) {return class_info.static_fields[fnum].descriptor.toString();}
	//given the fieldname get the fieldnumber
	public short fieldNumber(byte[] fname) {
		for (short s=0;s<values.length;s++) {
			//if (class_info.static_fields[s].name.toString().equals(fname)) {
			if (class_info.fields[s].nameEquals(fname)) {
				return s;
			}
		}
		return (short)-1;	//not found
	}

	//we should check the type
	public void set(byte[] fname,Variable v) {
		short fn=fieldNumber(fname);
		if (fn>-1) {
			values[fn]=v;
		} else {
			if (parent!=null) {
				parent.set(fname,v);
			} else {
				System.out.println("static field "+fname+" not found");
			}
		}
	}

	public Variable get(byte[] fname) {
		short fn=fieldNumber(fname);
		if (fn>-1) {
			return values[fn];
		} else {
			if (parent!=null) {
				return parent.get(fname);
			} else {
				System.out.println("static field "+fname+" not found");
				return null;
			}
		}
	}
}