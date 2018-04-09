package plzero.jvm;
import plzero.Variable;
import plzero.jvms.*;
import java.util.Hashtable;
import java.io.IOException;

/**
* An Obj is just a wrapper around a Variable array, one entry per field.
*
* This just creates a raw Object.  For the constructor, see Heap.
*
*/
public class Obj {
	public final ClassInfo class_info;
	public final Obj parent;
	private Variable[] values;

	public Obj(ClassInfo cinfo,Obj parent) {
		class_info=cinfo;
		this.parent=parent;
		if (cinfo.fields!=null && cinfo.fields.length>0) {
			values=new Variable[cinfo.fields.length];
		}

		//init values
		for (short s=0;s<values.length;s++) {
			FieldInfo fi=cinfo.fields[s];
			if (fi.init_value!=null) {
				values[s]=fi.init_value;
			}
		}
	}

	public short getNumFields() {return (short)values.length;}

	public String name(short fnum) {return class_info.fields[fnum].name.toString();}
	public String descriptor(short fnum) {return class_info.fields[fnum].descriptor.toString();}
	public short fieldNumber(byte[] fname) {
		for (short s=0;s<values.length;s++) {
			//if (class_info.fields[s].name.toString().equals(fname)) {
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
				System.out.println("field "+fname+" not found");
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
				System.out.println("field "+fname+" not found");
				return null;
			}
		}
	}

}