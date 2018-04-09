package plzero.jvms;
import plzero.Variable;
import org.apache.bcel.classfile.*;

public class Pool {
	short constant_pool_count;
	Variable[] constant_pool;

	public Pool(ConstantPool cp) {
		constant_pool_count=(short)cp.getLength();
		constant_pool=new Variable[constant_pool_count];

		//we do this in 3 passes
		pass1(cp);
		pass2(cp);
		pass3(cp);
	}

	//pass 1 has all the primitives
	void pass1(ConstantPool cp) {
		for (short s=0;s<constant_pool_count;s++) {
			Constant c=cp.getConstant(s);
			if (c==null) continue;
			//no need for else
			byte tag = c.getTag();
			switch (tag) {
				case CONSTANT_Utf8:
					ConstantUtf8 cu=(ConstantUtf8)c;
					//this is an odd line
					byte[] bytes=cu.getBytes().getBytes();
					//this uses type STRING
					constant_pool[s]=new Variable(bytes);
					break;
				case CONSTANT_Integer:
					ConstantInteger ci=(ConstantInteger)c;
					constant_pool[s]=new Variable(ci.getBytes());
					break;
				case CONSTANT_Float:
					ConstantFloat cf=(ConstantFloat)c;
					float f=cf.getBytes();
					constant_pool[s]=new Variable(f);
					break;
				case CONSTANT_Long:
					//we are forced to handle longs.  try to make them fit in an int
					ConstantLong clo=(ConstantLong)c;
					long lo=clo.getBytes();
					int smaller;
					if ( (lo<(long)Integer.MAX_VALUE) && (lo>(long)Integer.MIN_VALUE)) {
						//cast it to an int
						smaller=(int)lo;
						constant_pool[s]=new Variable(smaller);
					} else {
						//our little secret, we actually handle longs if we are forced to
						constant_pool[s]=new Variable(lo);
					}
					break;
				case CONSTANT_Double:
					ConstantDouble cd=(ConstantDouble)c;
					double dd=cd.getBytes();
					//degrade it to a float
					constant_pool[s]=new Variable((float)dd);
					break;
				default:
					break;
			}	//end switch
		} //end for
	}

	//pass2 does String,classname,name and type
	void pass2(ConstantPool cp) {
		short ux=0;
		for (short s=0;s<constant_pool_count;s++) {
			Constant c=cp.getConstant(s);
			if (c==null) continue;
			//no need for else
			byte tag = c.getTag();
			switch (tag) {
				case CONSTANT_Class:
					ConstantClass cc=(ConstantClass)c;
					ux=(short)cc.getNameIndex();
					Variable u=constant_pool[ux];
					//just copy it over, we treat it the same as a string
					constant_pool[s]=u;
					break;
				case CONSTANT_String:
					ConstantString cs=(ConstantString)c;
					ux=(short)cs.getStringIndex();
					Variable u2=constant_pool[ux];
					//just use the same string
					constant_pool[s]=u2;
					break;
				case CONSTANT_NameAndType:
					ConstantNameAndType cnat=(ConstantNameAndType)c;
					Variable name=constant_pool[cnat.getNameIndex()];
					Variable type=constant_pool[cnat.getSignatureIndex()];
					Variable vnat=new Variable(Variable.NAME_TYPE,null,name.getUtf8(),type.getUtf8());
					constant_pool[s]=vnat;
					break;
				default:
					break;
			}
		}
	}

	//pass3 does refs
	void pass3(ConstantPool cp) {
		short ux=0;
		for (short s=0;s<constant_pool_count;s++) {
			Constant c=cp.getConstant(s);
			if (c==null) continue;
			//no need for else
			byte tag = c.getTag();
			switch (tag) {
				case CONSTANT_Fieldref:
				case CONSTANT_Methodref:
				case CONSTANT_InterfaceMethodref:
					ConstantCP cccp=(ConstantCP)c;
					Variable cname=constant_pool[cccp.getClassIndex()];
					Variable vnat=constant_pool[cccp.getNameAndTypeIndex()];
					Variable vref=new Variable(tag, cname.getUtf8(),vnat.getName(),vnat.getType());
					constant_pool[s]=vref;
					break;
				default:
			}
		}
	}

	public Variable get(short s) {
		return constant_pool[s];
	}

	public void dump(StringBuilder sb) {
		sb.append("Pool["+constant_pool_count+"]:\r\n");
		for (short s=0;s<constant_pool_count;s++) {
			Variable v=constant_pool[s];
			if (v!=null) {
				sb.append("#"+s+" ");
				v.dump(sb);
				sb.append("\r\n");
			}
		}
	}

	//==============================
	//from the JVM spec, Table 4.4-A. Constant pool tags
	//a CONSTANT_Utf8 Utf8 is a byte array of characters
	public static final byte CONSTANT_Utf8 = (byte)1;
	public static final byte CONSTANT_Integer = (byte)3;
	public static final byte CONSTANT_Float = (byte)4;
	public static final byte CONSTANT_Long = (byte)5;
	public static final byte CONSTANT_Double = (byte)6;
	public static final byte CONSTANT_Class = (byte)7;
	public static final byte CONSTANT_String = (byte)8;
	public static final byte CONSTANT_Fieldref = (byte)9;
	public static final byte CONSTANT_Methodref = (byte)10;
	public static final byte CONSTANT_InterfaceMethodref = (byte)11;
	public static final byte CONSTANT_NameAndType = (byte)12;
	//these last 3 are just for reference, we don't use them
	public static final byte CONSTANT_MethodHandle = (byte)15;
	public static final byte CONSTANT_MethodType = (byte)16;
	public static final byte CONSTANT_InvokeDynamic = (byte)18;
}