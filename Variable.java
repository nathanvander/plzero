package plzero;
import java.nio.ByteBuffer;
import plzero.jvm.Array;
import plzero.jvm.Obj;
import plzero.jvm.Heap;

/**
* This is a variable object which can hold anything.  There is a certain simplistic elegance in handling everything as raw
* bytes.  There is overhead in converting it back and forth but the alternative is to waste space.
*
* The first byte is the type.
*/
public class Variable {
	//the tag is one of these
	public static final byte UTF8=1;	//also used for Strings and Classnames
	public static final byte INT=3;
	public static final byte FLOAT=4;
	public static final byte LONG=5;	//Long is handled only so the pool will load.
	public static final byte DOUBLE=6;	//Double is for reference only. Pool doubles are converted to floats.
	public static final byte FIELD=9;
	public static final byte METHOD=10;
	public static final byte IFACE_METHOD=11;
	public static final byte NAME_TYPE=12;	//from CONSTANT_NameAndType
	//missing MethodHandle, MethodType and InvokeDynamic
	public static final byte OBJ=(byte)'@';
	public static final byte ARRAY=(byte)'%';
	public static final byte JAVA_OBJECT=(byte)'J';
	//----------------------------------------------------
	public static final Variable NIL=new Variable(OBJ,0);
	public static final Variable ICONST_M1=new Variable(-1);
	public static final Variable ICONST_0=new Variable(0);
	public static final Variable ICONST_1=new Variable(1);
	public static final Variable ICONST_2=new Variable(2);
	public static final Variable ICONST_3=new Variable(3);
	public static final Variable ICONST_4=new Variable(4);
	public static final Variable ICONST_5=new Variable(5);
	public static final Variable FCONST_0=new Variable(0.0F);
	public static final Variable FCONST_1=new Variable(1.0F);
	public static final Variable FCONST_2=new Variable(2.0F);

	//------------------------------
	public final ByteBuffer bb;

	//----------------------------
	//utf8, used also for String and classname
	public Variable(byte[] u) {
		bb=ByteBuffer.allocate(u.length+1);
		bb.put(UTF8);	//define the type
		bb.put(u);
	}

	public Variable(String s) {
		this(s.getBytes());
	}

	public byte tag() {return bb.get(0);}

	public byte[] getUtf8() {
		byte[] ba=new byte[bb.capacity()-1];
		bb.position(1);
		bb.get(ba);
		return ba;
	}

	public String toString() {
		return new String(getUtf8());
	}
	//------------------------------------------------
	public Variable(int i) {
		bb=ByteBuffer.allocate(5);
		bb.put(INT);
		bb.putInt(i);
	}

	public int getInt() {
		bb.position(1);
		return bb.getInt();
	}
	//----------------------------------------------
	//I don't want to handle longs but am forced to
	public Variable(long lo) {
		bb=ByteBuffer.allocate(9);
		bb.put(LONG);
		bb.putLong(lo);
	}
	public long getLong() {
		bb.position(1);
		return bb.getLong();
	}
	//-----------------------------------------------
	public Variable(float f) {
		bb=ByteBuffer.allocate(5);
		bb.put(FLOAT);
		bb.putFloat(f);
	}

	public float getFloat() {
		bb.position(1);
		return bb.getFloat();
	}
	//--------------------------------------------
	//for use by FIELD,METHOD,IFACE_METHOD and NAME_TYPE
	public Variable(byte tag,byte[] cname,byte[] name,byte[] type) {
		//this is complicated, so check the input
		if (tag<9 || tag>12) {throw new IllegalArgumentException("invalid tag");}
		//cname may be null for type 12, but name and type may not be null
		if (name==null || type==null) {throw new IllegalArgumentException("name and type may not be null");}
		//---------------
		//calculate length
		int len=3;
		if (cname!=null) len+=cname.length;
		len+=name.length;
		len+=type.length;
		bb=ByteBuffer.allocate(len);
		//-----------------
		//calculate x2 and x3
		//we have 4 distinct ranges, call them x0..x3
		//x0 always starts at 0 and is 1 in length
		//x1 always starts at 3 and has 0 or cname.length in length and ends at x2-1
		//x2 starts at x1+cname.length and has name.length in length and ends at x3-1
		//x3 starts at x2+name.length and has type.length in length and ends at bb.capacity
		//so we only need to define x2 and x3
		int x2=3;
		if (cname!=null) {x2=cname.length+3;}
		int x3=x2+name.length;
		//--------------------
		//now save the data
		bb.put(tag);
		bb.put((byte)x2);
		bb.put((byte)x3);
		if (cname!=null) {bb.put(cname);}
		bb.put(name);
		bb.put(type);
	}

	//only valid for tags 9..12
	public byte x2() {return bb.get(1);}
	public byte x3() {return bb.get(2);}

	//may be null
	public byte[] getClassName() {
		if (x2()==(byte)3) {return null;}
		else {
			byte[] bcn=new byte[x2()-3];
			bb.position(3);
			bb.get(bcn);
			return bcn;
		}
	}

	public String getClassNameString() {return new String(getClassName());}

	public byte[] getName() {
		byte[] bn=new byte[x3()-x2()];
		bb.position(x2());
		bb.get(bn);
		return bn;
	}

	public String getNameString() {
		return new String(getName());
	}

	public byte[] getType() {
		byte[] bt=new byte[bb.capacity()-x3()];
		bb.position(x3());
		bb.get(bt);
		return bt;
	}

	//----------------------------------
	//use for refs
	public Variable(byte t,int i) {
		bb=ByteBuffer.allocate(5);
		bb.put(t);
		bb.putInt(i);
	}

	//------------------------------------------
	//don't add a carriage return;
	public void dump(StringBuilder sb) {
		switch(tag()) {
			case UTF8: sb.append(toString()); break;
			case INT: sb.append("I:"+getInt()); break;
			case FLOAT: sb.append("F:"+getFloat()); break;
			case LONG: sb.append("L:"+getLong()); break;
			case FIELD:
			case METHOD:
			case IFACE_METHOD:
			case NAME_TYPE:
				String className=null;
				byte[] cn=getClassName();
				if (cn!=null) {className=new String(cn);}
				String name=new String(getName());
				String type=new String(getType());
				sb.append("("+tag()+")(class)"+className+": (name)"+name+" (type)"+type);
				break;
			default: sb.append("unknown: "+String.valueOf(tag())); break;
		}
	}

	public String dumpValue() {
		switch(tag()) {
			case UTF8: return toString();
			case INT: return "I:"+String.valueOf(getInt());
			case FLOAT: return "F:"+String.valueOf(getFloat());
			case FIELD:
			case METHOD:
			case IFACE_METHOD:
			case NAME_TYPE:
				String className=null;
				byte[] cn=getClassName();
				if (cn!=null) {className=new String(cn);}
				String name=new String(getName());
				String type=new String(getType());
				return "("+tag()+")(class)"+className+": (name)"+name+" (type)"+type;
			case ARRAY:
				Array arr=(Array)Heap.get(getInt());
				return "(Array) of "+new String(arr.type)+" size: "+arr.size;
			case OBJ:
				if (getInt()==0) {
					return "NIL";
				} else {
					Obj o=(Obj)Heap.get(getInt());
					return "(Obj) of "+new String(o.class_info.this_class)+" size: "+o.getNumFields();
				}
			case JAVA_OBJECT:
				Object job=Heap.get(getInt());
				return "(Object) of "+job.getClass().getName();
			default: return "unknown: "+String.valueOf(tag());
		}
	}

	//=======================
	//public static void main(String[] args) {
		//Variable v1=new Variable(args[0]);
		//System.out.println(v1);
		//float f=Float.parseFloat(args[0]);
		//Variable v=new Variable(f);
		//System.out.println(v.getFloat());
		//I don't know if these are valid,just testing
		//String cn="java/lang/String";
		//String name="hash";
		//String type="I";
		//Variable v=new Variable(FIELD,cn.getBytes(),name.getBytes(),type.getBytes());
		//byte[] cn2=v.getClassName();
		//byte[] n=v.getName();
		//byte[] ty=v.getType();
		//System.out.println(new String(cn2)+","+new String(n)+","+new String(ty));
	//}

}