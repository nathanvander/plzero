package plzero.jvm;
import plzero.*;
import plzero.jvms.*;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
* A Frame is to a Method what an Object is to a Class. It is the "live" embodiment of a method, of which
* MethodInfo is the blueprint.
*
* This creates a Chip which does most of the work.  This makes it easier to test.
* The only bytecodes that are implemented here and not in JavaChip or ZThread are:
*	IF,GOTO, and RETURN/IRETURN
*
*/
public class Frame implements OpCode {
	static int count;
	int id;		//unique identifier
	MethodInfo method;
	Pool pool;
	Chip chip;
	byte[] code;
	int pc;				//the program counter. It always points to the next byte to be called
	boolean running;
	boolean returned;
	/**
	* create a new frame.  If static, the oref will be null.
	* Note that oref is used only to be passed in.  It sits at local0 if needed.
	*/
	public Frame(MethodInfo m,Variable oref,Variable[] params) {
		id=count++;
		System.out.println("creating new Frame #"+id+", running method: "+(new String(m.name)));
		method=m;
		pool=method.class_info.pool;
		chip=new Chip(pool);
		code=method.code;
		if (m.isStatic()) {
			//System.out.println("DEBUG: Frame() passing static params");
			chip.passStaticParams(params);
		} else {
			//System.out.println("DEBUG: Frame() passing params");
			chip.passParams(oref,params);
		}
		running=false;
		if (chip.local0!=null) {
			System.out.println("in Frame#"+id+", local0="+chip.local0.dumpValue());
		}
	}

	//you usually don't want this. It only goes up to 127 and can return a negative number
	public byte NEXT_SIGNED() {return code[pc++];}
	//this will return a number from 0..255
	public short NEXT() {short s=(short)(code[pc] & 0xFF);  pc++; return s;}
	//this will return a number from 0..32767 and negative numbers as well
	public short NEXT2() {
		byte byte1=NEXT_SIGNED();
		short s=NEXT();
		return (short)((byte1 << 8 ) | s);
	}

	/**
	* When calling this for the first time, don't pass anything in as stackItem.
	* When resuming the frame execution the second time, pass in the item.
	*/
	public ExecutionStatus execute(Variable stackItem) {
		if (returned) {
			System.out.println("frame #"+id+" has already returned");
		}
		if (stackItem!=null) chip.PUSH(stackItem);
		running=true;
		short op=0;
		short s=0;
		int my_line=0;
		boolean b=false;
		ExecutionStatus status=null;

		while (running) {
			op=NEXT();

			switch (op) {
				//just do these in order, there are a lot
				case ACONST_NULL: 	chip.ACONST_NULL(); break;
				case ICONST_M1: 	chip.ICONST_M1(); break;
				case ICONST_0:		chip.ICONST_0(); break;
				case ICONST_1:		chip.ICONST_1(); break;
				case ICONST_2:		chip.ICONST_2(); break;
				case ICONST_3:		chip.ICONST_3(); break;
				case ICONST_4:		chip.ICONST_4(); break;
				case ICONST_5:		chip.ICONST_5(); break;
				case FCONST_0:		chip.FCONST_0(); break;
				case FCONST_1:		chip.FCONST_0(); break;
				case FCONST_2:		chip.FCONST_0(); break;
				case BIPUSH: byte by=NEXT_SIGNED(); chip.BIPUSH(by); break;
				case SIPUSH: s=NEXT2(); chip.SIPUSH(s); break;
				case LDC: 	s=NEXT(); chip.LDC(s); break;
				case LDC_W: s=NEXT2(); chip.LDC(s); break;
				case ILOAD: s=NEXT(); chip.ILOAD(s); break;
				case ILOAD_0:		//fall through
				case ALOAD_0: chip.ILOAD_0(); break;
				case ILOAD_1:		//fall through
				case ALOAD_1: chip.ILOAD_1(); break;
				case ILOAD_2:		//fall through
				case ALOAD_2: chip.ILOAD_2(); break;
				case ILOAD_3:		//fall through
				case ALOAD_3: chip.ILOAD_3(); break;
				case IALOAD:	//take item from array and load onto stack
				case SALOAD:
				case AALOAD: chip.AALOAD(); break;
				case ISTORE: s=NEXT(); chip.ISTORE(s); break;
				case ISTORE_0:
				case ASTORE_0:	chip.ISTORE_0(); break;
				case ISTORE_1:
				case ASTORE_1:	chip.ISTORE_1(); break;
				case ISTORE_2:
				case ASTORE_2:	chip.ISTORE_2(); break;
				case ISTORE_3:
				case ASTORE_3:	chip.ISTORE_3(); break;
				case IASTORE:	//take item from stack and store in array
				case SASTORE:
				case AASTORE: chip.AASTORE(); break;
				case DUP: chip.DUP(); break;
				case IADD: chip.IADD(); break;
				case FADD: chip.FADD(); break;
				case ISUB: chip.ISUB(); break;
				case FSUB: chip.FSUB(); break;
				case IMUL: chip.IMUL(); break;
				case FMUL: chip.FMUL(); break;
				case IDIV: chip.IDIV(); break;
				case FDIV: chip.FDIV(); break;
				case IREM: chip.IREM(); break;
				case FREM: chip.FREM(); break;
				case INEG: chip.INEG(); break;
				case FNEG: chip.FNEG(); break;
				case IAND: chip.IAND(); break;
				case IOR: chip.IOR(); break;
				case IXOR: chip.IXOR(); break;
				case IINC: chip.IINC(); break;
				case I2F: chip.I2F(); break;
				case F2I: chip.F2I(); break;
				case IFNE:
					my_line=pc-1;
					s=NEXT2();
					//System.out.println("DEBUG: Frame#"+id+" IFNE s="+s);
					b=chip.NE();	//not equal to zero
					//if (b) pc=pc+s;
					if (b) pc=my_line+s;
					//System.out.println("DEBUG: Frame#"+id+" jumping to line "+pc);
					break;
				case IF_ICMPNE:
					my_line=pc-1;
					s=NEXT2();
					//System.out.println("DEBUG: Frame#"+id+" IFNE s="+s);
					b=chip.ICMPNE();	//not equal to zero
					//if (b) pc=pc+s;
					if (b) pc=my_line+s;
					//System.out.println("DEBUG: Frame#"+id+" jumping to line "+pc);
					break;
				case IF_ICMPGE:
					my_line=pc-1;
					s=NEXT2();
					b=chip.ICMPGE();
					if (b) pc=my_line+s;
					//if (b) pc=pc+s;	//jump to given byte
					break;
				case GOTO:		//absolute jump
					my_line=pc-1;
					s=NEXT2();
					//pc=pc+s;
					pc=my_line+s;
					break;
				case IRETURN:	//we are supposed to check the return type but I'm lazy
				case FRETURN:
				case ARETURN:
					Variable retval=chip.POP();
					status=new ExecutionStatus(true,retval,null);
					running=false;
					returned=true;
					return status;
				case RETURN:
					status=new ExecutionStatus(true,null,null);
					running=false;
					returned=true;
					return status;
				case GETSTATIC: s=NEXT2(); chip.GETSTATIC(s); break;
				case PUTSTATIC: s=NEXT2(); chip.PUTSTATIC(s); break;
				case GETFIELD: s=NEXT2(); chip.GETFIELD(s); break;
				case PUTFIELD: s=NEXT2(); chip.PUTFIELD(s); break;
				case INVOKEVIRTUAL:
					s=NEXT2();
					//do a shortcut for PRINT
					if (PRINT(s)) {
						break;
					} else {
						FrameContext fc=prepareInvoke(false,s);
						status=new ExecutionStatus(false,null,fc);
						running=false;
						return status;
					}
				case INVOKESPECIAL:
					//I think this is just like invokevirtual
					//except we can't call Object.<init>
					s=NEXT2();
					if (OBJECT_INIT(s)) {
						break;
					} else {
						FrameContext fc=prepareInvoke(false,s);
						status=new ExecutionStatus(false,null,fc);
						running=false;
						return status;
					}
				case INVOKESTATIC:
					s=NEXT2();
					FrameContext fc2=prepareInvoke(true,s);
					status=new ExecutionStatus(false,null,fc2);
					running=false;
					return status;
				case NEW_: s=NEXT2(); chip.NEWOBJECT(s); break;
				case NEWARRAY: byte ty=NEXT_SIGNED(); chip.NEWARRAY(ty); break;
				case ANEWARRAY: s=NEXT2(); chip.ANEWARRAY(s); break;
				case ARRAYLENGTH: chip.ARRAYLENGTH(); break;
				default:
					throw new IllegalStateException("unimplemented opcode "+op);
			}
		}
		throw new IllegalStateException("frame #"+id+" has completed without returning");
	}

	// The run-time constant pool item at that index must be a symbolic reference to a method
	//prepare the FrameContext
	public FrameContext prepareInvoke(boolean isStatic,short x) {
		Variable vmethod=pool.get(x);
		//check to see if it is a java method
		if (BString.startsWith(vmethod.getClassName(),Chip.JAVA)) {
			return prepareJavaInvoke(isStatic,vmethod);
		} else {
			return preparePlZeroInvoke(isStatic,vmethod);
		}
	}

	public FrameContext prepareJavaInvoke(boolean isStatic,Variable vmethod) {
		try {
			//replace slashes with dots
			String className=BString.doticize(vmethod.getClassName());
			Class c=Class.forName(className);
			String methodName=new String(vmethod.getName());
			String methodType=new String(vmethod.getType());
			//System.out.println("DEBUG: Frame.prepareJavaInvoke() methodName="+methodName+" methodType="+methodType);
			//we only handle 1 parameter
			Class[] ptypes=BString.getMethodParameterTypes(vmethod.getType());
			Method theMethod=null;
			theMethod=c.getMethod(methodName,ptypes);
			//Method[] ma=c.getMethods();
			//for (int i=0;i<ma.length;i++) {
			//	Method m=ma[i];
			//	if (m.getName().equals(methodName)) {
			//		theMethod=m;
			//		break;
			//	}
			//}

			if (theMethod==null) {
				throw new IllegalStateException("no method named "+methodName+" found in class "+className);
			}
			//check static
	 		if (isStatic!=Modifier.isStatic(theMethod.getModifiers())) {
				throw new IllegalStateException("unclear whether method is static");
			}
			//get the number of parameters
			int numParams=theMethod.getParameterCount();

			//prepare the arguments to the invocation
			//pop these in reverse order
			Variable[] params=new Variable[numParams];
			for (int i=0;i<params.length;i++) {
				int n=params.length-i-1;
				params[n]=chip.POP();
			}
			Variable oref=null;
			if (!isStatic) {
				oref=chip.POP();
			}
			FrameContext fc=new FrameContext(vmethod,theMethod,oref,params);
			return fc;
		} catch (Exception x) {
			x.printStackTrace();
			return null;
		}
	}

	public FrameContext preparePlZeroInvoke(boolean isStatic,Variable vmethod) {
		try {
			ClassInfo cinfo=ClassInfo.getClass(vmethod.getClassName());
			MethodInfo method=cinfo.getMethod(vmethod.getName(),vmethod.getType());
			if (isStatic!=method.isStatic()) {
				throw new IllegalStateException("unclear whether method is static");
			}
			//prepare the arguments to the invocation
			Variable[] params=new Variable[method.param_count];
			//System.out.println("DEBUG: Frame.preparePlZeroInvoke method.param_count="+method.param_count);
			for (int i=0;i<params.length;i++) {
				int n=params.length-i-1;
				params[n]=chip.POP();
			}
			Variable oref=null;
			if (!isStatic) {
				oref=chip.POP();
			}
			//---------------
			if (params==null) {
				System.out.println("DEBUG: Frame preparePlZeroInvoke() params==null");
			}
			FrameContext fc=new FrameContext(vmethod,method,oref,params);
			return fc;
		} catch (Exception x) {
			x.printStackTrace();
			return null;
		}
	}

	//shortcut to System.out.println
	//this only handles Strings.  It could be modified to handle ints or other types
	//if this doesn't trigger, then don't change any state
	//if it does trigger, then the end state must be the same as if this were invoked
	public boolean PRINT(short x) {
		//check the oref.  See if this is a System.out object of class java/io/PrintStream
		Variable oref=chip.PEEK2();
		if (oref.tag()==Variable.JAVA_OBJECT) {
			Object job=Heap.get(oref.getInt());
			if (job instanceof java.io.PrintStream) {
				//now check the method params
				Variable vmethod=pool.get(x);
				if (	BString.equals(vmethod.getClassName(),"java/io/PrintStream".getBytes()) &&
						BString.equals(vmethod.getName(),"println".getBytes()) &&
				 		BString.equals(vmethod.getType(),"(Ljava/lang/String;)V".getBytes())	)
				{
					//do it
					System.out.println("DEBUG: doing a PRINT call inline");
					//grab the String
					Variable vstring=chip.POP();
					//grab the oref.  We already looked at it above
					Variable system_out=chip.POP();
					System.out.println(vstring.toString());
					return true;
				} else {
					//abort
					return false;
				}
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	//pretend to handle Object.<init>
	//actually we don't do anything
	public boolean OBJECT_INIT(short x) {
		Variable vmethod=pool.get(x);
		if ( BString.equals( vmethod.getClassName(),"java/lang/Object".getBytes() ) &&
			BString.equals( vmethod.getName(),"<init>".getBytes() ) )
		{
			System.out.println("DEBUG: Object.<init> called, ignoring");
			return true;
		} else {
			return false;
		}
	}

}