package plzero.jvm;
import plzero.*;
import plzero.jvms.*;
import java.io.IOException;
import java.lang.reflect.Field;

/**
* I am having trouble making all the pieces fit together smoothly, so here is another way of looking at it.  This is a
* primitive state machine, inside our virtual computer. You can test each function separately.
*
* This only has logical functions, not control flow instructions.
*
* This can now handle 5 local variables.  If we handle any more then will need to put them into an array and restructure
* opcodes.
*/
public class Chip {
	public static final byte[] JAVA=new byte[]{(byte)'j',(byte)'a',(byte)'v',(byte)'a'};
	Pool pool;
	Variable local0;	//at some point I may want to put these into an array
	Variable local1;
	Variable local2;
	Variable local3;
	Variable local4;
	short stackMarker;
	Variable[] stack;

	public Chip(Pool p) {
		pool=p;
		//we don't need a big stack
		stack=new Variable[10];
	}

	//----------------------------
	//stack operations
	public Variable POP() {
		--stackMarker;						//go back to the last slot used
		Variable tmp=stack[stackMarker];			//get the item
		stack[stackMarker]=Variable.NIL;		//erase it internally so it is gone
		return tmp;
	}

	public void PUSH(Variable v) {stack[stackMarker++]=v;}

	public Variable PEEK() {return stack[stackMarker-1];}

	//return the 2nd item on stack without changing the stack
	public Variable PEEK2() {
		if (stackMarker>1) {return stack[stackMarker-2];}
		else {return null;}
	}
	//--------------------------

	/**
	* Use this when passing non-static parameters.  object may not be null, the other three may be.
	*/
	public void passParams(Variable oref,Variable[] params) {
		if (oref==null) {throw new IllegalArgumentException("oref may not be null");}
		System.out.println("[Chip Debug: object type="+oref.tag()+"]");
		local0=oref;
		if (params==null) return;
		if (params.length>=1) {
			local1=params[0];
			//System.out.println("[Chip Debug: local1 holds a type of "+param0.tag()+"]");
		}
		if (params.length>=2) {
			local2=params[1];
			//System.out.println("[JChip Debug: local2 holds a type of "+param1.tag()+"]");
		}
		if (params.length>=3) {
			local3=params[2];
			//System.out.println("[Chip Debug: local3 holds a type of "+param2.tag()+"]");
		}
		if (params.length>=4) {
			throw new IllegalStateException("more than 3 parameters");
		}
	}

	/**
	* Use this when passing static parameters.
	* There is no object we are operating on.  Instead, just pass up to three parameters, which may be null.
	* In the typical case of static void main(String[] args), put the args on the heap, and then pass
	* the Item reference as param0
	*/
	public void passStaticParams(Variable[] params) {
		if (params==null) {
			//System.out.println("passStaticParams() params==null");
			return;
		} else {
			//System.out.println("passStaticParams() params.length="+params.length);
		}
		if (params.length>=1) {local0=params[0];}
		if (params.length>=2) {local1=params[1];}
		if (params.length>=3) {local2=params[2];}
		if (params.length>=4) {
			throw new IllegalStateException("more than 3 parameters");
		}
		//if (local0!=null) {
		//	System.out.println("local0="+local0.dumpValue());
		//}
		//if (local1!=null) {
		//	System.out.println("local1="+local1.dumpValue());
		//}
	}

	//---------------------------------
	public void ACONST_NULL() { PUSH(Variable.NIL);}			//java byte code #1
	public void ICONST_M1() { PUSH(Variable.ICONST_M1);}		//#2
	public void ICONST_0() { PUSH(Variable.ICONST_0);}		//#3
	public void ICONST_1() { PUSH(Variable.ICONST_1);}
	public void ICONST_2() { PUSH(Variable.ICONST_2);}
	public void ICONST_3() { PUSH(Variable.ICONST_3);}
	public void ICONST_4() { PUSH(Variable.ICONST_4);}
	public void ICONST_5() { PUSH(Variable.ICONST_5);}		//#8
	public void FCONST_0() { PUSH(Variable.FCONST_0);}
	public void FCONST_1() { PUSH(Variable.FCONST_1);}
	public void FCONST_2() { PUSH(Variable.FCONST_2);}

	public void BIPUSH(byte b) { PUSH(new Variable((int)b));}
	//push the given number on to the stack
	//The immediate unsigned byte1 and byte2 values are assembled into an intermediate short, where the value of
	//the short is (byte1 << 8) | byte2. The intermediate value is then sign-extended to an int value.
	//That value is pushed onto the operand stack.
	//also use for BIPUSH
	public void SIPUSH(short s) { PUSH(new Variable((int)s));}		//#17

	//load the given constant on to the stack
	//for now we expect it to be an integer or float
	//also handles LDC_W
	public void LDC(short x) {		//#18
		Variable e=pool.get(x);
		if (e==null) {throw new IllegalStateException("no constant found at "+x);}
		PUSH(e);
	}

	public void ILOAD(short index) {
		if (index!=(short)4) {
			throw new IllegalStateException("Chip: ILOAD index="+index+" but we can't handle that");
		} else {
			PUSH(local4);
		}
	}

	public void ILOAD_0() {PUSH(local0);}			//#26 use also for ALOAD_0
	public void ILOAD_1() {PUSH(local1);}
	public void ILOAD_2() {PUSH(local2);}
	public void ILOAD_3() {PUSH(local3);}

	/**
	* Takes arrayref and index from the stack, and puts value back on.
	* Java bytecode #50
	* Also handles SALOAD and IALOAD
	* At this point, we only handle String arrays, short arrays and int arrays, which are all stored as Arrays.
	* This doesn't work with byte arrays, which it probably should
	*/
	public void AALOAD() {
		//take index off first
		Variable index=POP();
		Variable aref=POP();
		if (aref==null || aref.tag()!=Variable.ARRAY) {throw new IllegalArgumentException("invalid aref");}
		Array arr=(Array)Heap.get(aref.getInt());
		Variable item=arr.get((short)index.getInt());
		PUSH(item);
	}	//end AALOAD

	public void ISTORE(short index) {
		if (index!=(short)4) {
			throw new IllegalStateException("Chip: ISTORE index="+index+" but we can't handle that");
		} else {
			local4=POP();
		}
	}

	public void ISTORE_0() {local0=POP();}		//also ASTORE_0
	public void ISTORE_1() {local1=POP();}		//also ASTORE_1
	public void ISTORE_2() {local2=POP();}		//also ASTORE_2
	public void ISTORE_3() {local3=POP();}		//also ASTORE_3

	/**
	* Store value in given array
	* Takes 3 values off the stack: arrayref, index, value.
	* Also handles SASTORE and IASTORE
	*/
	public void AASTORE() {	//#83
		Variable item=POP();
		Variable index=POP();
		Variable aref=POP();
		if (aref==null || aref.tag()!=Variable.ARRAY) {throw new IllegalArgumentException("invalid aref");}
		Array arr=(Array)Heap.get((short)aref.getInt());
		arr.set((short)index.getInt(),item);
	}

	public void DUP() {PUSH(PEEK());}

	//take 2 items off the stack, add them and put the result back on the stack
	public void IADD() {
		Variable i2=POP();
		Variable i1=POP();
		int res = i1.getInt() + i2.getInt();		//add
		Variable vres=new Variable(res);
		PUSH(vres);
	}

	public void FADD() {
		Variable f2=POP();
		Variable f1=POP();
		float fres = f1.getFloat() + f2.getFloat();		//add
		Variable vfres=new Variable(fres);
		PUSH(vfres);
	}

	public void ISUB() {
		Variable i2=POP();
		Variable i1=POP();
		int res = i1.getInt() - i2.getInt();		//subtract
		Variable vres=new Variable(res);
		PUSH(vres);
	}

	public void FSUB() {
		Variable f2=POP();
		Variable f1=POP();
		float fres = f1.getFloat() - f2.getFloat();
		Variable vfres=new Variable(fres);
		PUSH(vfres);
	}


	public void IMUL() {
		Variable i2=POP();
		Variable i1=POP();
		int res = i1.getInt() * i2.getInt();
		Variable vres=new Variable(res);
		PUSH(vres);
	}

	public void FMUL() {
		Variable f2=POP();
		Variable f1=POP();
		float fres = f1.getFloat() * f2.getFloat();
		Variable vfres=new Variable(fres);
		PUSH(vfres);
	}

	public void IDIV() {
		Variable i2=POP();
		Variable i1=POP();
		int res = i1.getInt() / i2.getInt();
		Variable vres=new Variable(res);
		PUSH(vres);
	}

	public void FDIV() {
		Variable f2=POP();
		Variable f1=POP();
		float fres = f1.getFloat() / f2.getFloat();
		Variable vfres=new Variable(fres);
		PUSH(vfres);
	}

	public void IREM() {
		Variable i2=POP();
		Variable i1=POP();
		int res = i1.getInt() % i2.getInt();
		Variable vres=new Variable(res);
		PUSH(vres);
	}

	public void FREM() {
		Variable f2=POP();
		Variable f1=POP();
		float fres = f1.getFloat() % f2.getFloat();
		Variable vfres=new Variable(fres);
		PUSH(vfres);
	}

	public void INEG() {
		Variable i1=POP();
		int res = 0 - i1.getInt();
		Variable vres=new Variable(res);
		PUSH(vres);
	}

	public void FNEG() {
		Variable f1=POP();
		float fres = 0.0F - f1.getFloat();
		Variable vfres=new Variable(fres);
		PUSH(vfres);
	}

	public void IAND() {
		Variable i2=POP();
		Variable i1=POP();
		int res = i1.getInt() & i2.getInt();
		Variable vres=new Variable(res);
		PUSH(vres);
	}

	public void IOR() {
		Variable i2=POP();
		Variable i1=POP();
		int res = i1.getInt() | i2.getInt();
		Variable vres=new Variable(res);
		PUSH(vres);
	}

	public void IXOR() {
		Variable i2=POP();
		Variable i1=POP();
		int res = i1.getInt() ^ i2.getInt();
		Variable vres=new Variable(res);
		PUSH(vres);
	}

	public void IINC() {
		Variable i1=POP();
		int n=i1.getInt();
		n++;
		Variable vres=new Variable(n);
		PUSH(vres);
	}

	public void I2F() {
		Variable vi=POP();
		if (vi.tag()!=Variable.INT) {throw new IllegalStateException("variable is not an int, it is type "+vi.tag());}
		int i=vi.getInt();
		float f=(float)i;
		Variable vf=new Variable(f);
		PUSH(vf);
	}

	public void F2I() {
		Variable vf=POP();
		if (vf.tag()!=Variable.FLOAT) {throw new IllegalStateException("variable is not a float, it is type "+vf.tag());}
		float f=vf.getFloat();
		int i=(int)f;
		Variable vi=new Variable(i);
		PUSH(vi);
	}

	//byte code #154
	//ifne succeeds if and only if value != 0
	public boolean NE() {
		Variable i=POP();
		return (i.getInt()!=0);
	}

	//if_icmpne succeeds if and only if value1 != value2
	public boolean ICMPNE() {
		Variable i2=POP();
		Variable i1=POP();
		return (i1.getInt() != i2.getInt());
	}

	//pop the top two ints off the stack and compare them
	//return value1 >= value2
	//this seems to be the most common one.  Implement others are we encounter them.
	public boolean ICMPGE() {
		Variable i2=POP();
		Variable i1=POP();
		return (i1.getInt() >= i2.getInt());
	}

	// The run-time constant pool item at that index must be a symbolic reference to a field (§5.1),
	//which gives the name and descriptor of the field as well as a symbolic reference to the class
	//or interface in which the field is to be found
	public void GETSTATIC(short x) {
		Variable e=pool.get(x);
		if (e.tag()!=Variable.FIELD) {throw new IllegalStateException("Constant at "+x+" is not a field");}
		byte[] className=e.getClassName();
		byte[] fieldName=e.getName();
		Variable vo=null;
		if (BString.startsWith(className,JAVA)) {
			Field jf=getJavaField(className,fieldName);
			try {
				Object ob=jf.get(null);
				if (ob!=null) {
					//turn the java object into a Variable
					vo=Heap.addJavaObject(ob);
				}
			} catch (Exception ex2) {
				System.out.println(ex2.getMessage());
			}
		} else {
			StaticObj so=null;
			try {
				so=StaticObj.getStatic(className);
				vo=so.get(fieldName);
			} catch (Exception ex) {
				System.out.println(ex.getMessage());
			}
		}
		PUSH(vo);
	}


	//works with both static and nonstatic
	public static Field getJavaField(byte[] cname,byte[] fname) {
		//System.out.println("DEBUG: cname="+(new String(cname))+"; fname="+(new String(fname)));
		try {
			String jclassName=BString.doticize(cname);
			//System.out.println("DEBUG: jclassName="+jclassName);
			Class c=Class.forName(jclassName);
			java.lang.reflect.Field field=c.getDeclaredField(new String(fname));
			field.setAccessible(true);
			if (field==null) {
				System.out.println("unable for find field "+(new String(fname))+" in class "+jclassName);
			} else {
				//find the type
				Class ctype=field.getType();
				//System.out.println("type of field is "+ctype.toString());
				return field;
			}
		} catch (Exception x) {
			x.getStackTrace();
		}
		return null;
	}

	//need to do this for java as well
	public void PUTSTATIC(short x) {
		Variable v=POP();
		Variable e=pool.get(x);
		if (e.tag()!=Variable.FIELD) {throw new IllegalStateException("Constant at "+x+" is not a field");}
		byte[] className=e.getClassName();
		byte[] fieldName=e.getName();
		if (BString.startsWith(className,JAVA)) {
			Field jf=getJavaField(className,fieldName);
			//this is a little tricky. we can't just put a Variable in a java field
			//see if it is a string
			if (v.tag()==Variable.UTF8) {
				String str=v.toString();
				try {
					jf.set(null,str);
				} catch (Exception x2) {
					System.out.println(x2.getMessage());
				}
			} else {
				System.out.println("the variable is of type "+v.tag()+" so I don't know how to put it in field "+new String(fieldName));
			}
		} else {
			try {
				StaticObj so=StaticObj.getStatic(className);
				so.set(fieldName,v);
			} catch (Exception ex) {
				System.out.println(ex.getMessage());
			}
		}
	}

	// The run-time constant pool item at that index must be a symbolic reference to a field (§5.1),
	//which gives the name and descriptor of the field
	public void GETFIELD(short x) {
		Variable oref=POP();
		if (oref==null) {throw new IllegalArgumentException("invalid oref");}
		System.out.println("DEBUG: Chip.GETFIELD oref = "+oref.dumpValue());
		Variable e=pool.get(x);
		if (e.tag()!=Variable.FIELD) {throw new IllegalStateException("Constant at "+x+" is not a field");}
		byte[] className=e.getClassName();
		byte[] fieldName=e.getName();

		Variable item=null;
		if (BString.startsWith(className,JAVA)) {
			//we expect the oref to be of type Object
			if (oref.tag()!=Variable.JAVA_OBJECT) {
				throw new IllegalStateException("oref is not a Java Object");
			}
			Object o=Heap.get(oref.getInt());

			Field jf=getJavaField(className,fieldName);
			try {
				Object ob=jf.get(o);
				if (ob!=null) {
					item=Heap.addJavaObject(ob);
				}
			} catch (Exception x2) {
				System.out.println(x2.getMessage());
			}
		} else {
			if (oref.tag()!=Variable.OBJ) {
				throw new IllegalStateException("oref is not a Plzero Obj");
			}
			Obj ob=(Obj)Heap.get(oref.getInt());
			if (ob!=null) {
				item=ob.get(fieldName);
			}
		}
		PUSH(item);
	}

	//here is the description of PUTFIELD
	//The value and objectref are popped from the operand stack. The objectref must be of type reference.
	//The value undergoes value set conversion (§2.8.3), resulting in value', and the referenced field in objectref is set to value'.
	public void PUTFIELD(short x) {
		//get the input
		Variable item=POP();
		Variable oref=POP();
		if (oref==null) {throw new IllegalArgumentException("invalid oref");}
		Variable e=pool.get(x);
		if (e.tag()!=Variable.FIELD) {throw new IllegalStateException("Constant at "+x+" is not a field");}
		byte[] className=e.getClassName();
		byte[] fieldName=e.getName();

		//now put it
		if (BString.startsWith(className,JAVA)) {
			//we expect the oref to be of type Object
			if (oref.tag()!=Variable.JAVA_OBJECT) {
				throw new IllegalStateException("oref is not a Java Object");
			}
			Object baseObject=Heap.get(oref.getInt());

			Field jf=getJavaField(className,fieldName);
			//here is the tricky part.  What exactly can we put in the field?  We can look up the type
			//but I'm kind of lazy.  Assume it is a string
			if (item.tag()==Variable.UTF8) {
				String str=item.toString();
				try {
					jf.set(baseObject,str);
				} catch (Exception x2) {
					System.out.println(x2.getMessage());
				}
			} else {
				System.out.println("the variable is of type "+item.tag()+" so I don't know how to put it in field "+new String(fieldName));
			}
		} else {
			if (oref.tag()!=Variable.OBJ) {
				throw new IllegalStateException("oref is not a Plzero Obj");
			}
			Obj ob=(Obj)Heap.get(oref.getInt());
			if (ob!=null) {
				ob.set(fieldName,item);
			}
		}
	}

	public void NEWOBJECT(short x) {
		//this has the classname
		try {
			Variable e=pool.get(x);
			byte[] className=e.getUtf8();
			Variable oref=null;
			if (BString.startsWith(className,JAVA)) {
				Object o = newJavaObject(className);
				oref=Heap.addJavaObject(o);
			} else {
				ClassInfo ci=ClassInfo.getClass(e.getUtf8());
				oref=Heap.createNewObject(ci);
			}
			PUSH(oref);
		} catch (Exception xx) {
			System.out.println(xx.getMessage());
		}
	}

	//create a new Java object using reflection
	public static Object newJavaObject(byte[] cname) {
		try {
			String jclassName=BString.doticize(cname);
			Class c=Class.forName(jclassName);
			return c.newInstance();
		} catch (Exception x) {
			throw new RuntimeException(x.getMessage());
		}
	}

	//The count must be of type int. It is popped off the operand stack. The count represents the number of elements
	//in the array to be created.
	//The atype is a code that indicates the type of array to create. It must take one of the following values:
	public void NEWARRAY(byte type) {
		Variable v=POP();
		if (v.tag()!=Variable.INT) {throw new IllegalStateException("the variable on the stack must be of type int, instead if is of type "+v.tag());}
		int count=v.getInt();
		byte[] ty=getArrayType(type);
		Variable arr=Heap.createNewArray(ty,(short)count);
		PUSH(arr);
	}

	//Array type codes
	//T_BOOLEAN 4	= Z
	//T_CHAR 	5	= C
	//T_FLOAT 	6	= F
	//T_DOUBLE 	7	= D
	//T_BYTE 	8	= B
	//T_SHORT 	9	= S
	//T_INT 	10	= I
	//T_LONG 	11	= J
	public static byte[] getArrayType(byte t) {
		switch (t) {
			case (byte)4: return new byte[]{(byte)'Z'};
			case (byte)5: return new byte[]{(byte)'C'};
			case (byte)6: return new byte[]{(byte)'F'};
			case (byte)7: return new byte[]{(byte)'D'};
			case (byte)8: return new byte[]{(byte)'B'};
			case (byte)9: return new byte[]{(byte)'S'};
			case (byte)10: return new byte[]{(byte)'I'};
			case (byte)11: return new byte[]{(byte)'J'};
			default:
				throw new IllegalStateException("trying to create primitive array of type "+t);
		}
	}

 	//The run-time constant pool item at that index must be a symbolic reference to a class, array, or interface type.
	public void ANEWARRAY(short x) {
		Variable v=POP();
		if (v.tag()!=Variable.INT) {throw new IllegalStateException("the variable on the stack must be of type int, instead if is of type "+v.tag());}
		int count=v.getInt();
		Variable vtype=pool.get(x);
		byte[] className=vtype.getClassName();
		Variable arr=Heap.createNewArray(className,(short)count);
		PUSH(arr);
	}

	public void ARRAYLENGTH() {
		Variable aref=POP();
		Array arr=(Array)Heap.get(aref.getInt());
		Variable vlen=new Variable(arr.length());
		PUSH(vlen);
	}
}