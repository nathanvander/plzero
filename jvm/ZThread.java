package plzero.jvm;
import plzero.jvms.*;
import plzero.*;
import java.lang.reflect.Method;

/**
* A ZThread is not an operating system thread or Java Thread.  Instead it is just a way of controlling the application.
*
* For now keep this very simple.  invoke creates and calls a new frame.  If that frame needs to invoke another
* it sends the request to ZThread.  The caller is placed on the framestack.  When the new frame returns, it sends the result
* back to the calling frame.
*
*/
public class ZThread {
	Frame[] frameStack;
	//frameStackMarker always points to the next,currently empty, slot for a frame
	short frameStackMarker;

	public ZThread() {
		frameStack=new Frame[100];
	}

	private void pushFrame(Frame f) {
		frameStack[frameStackMarker]=f;
		frameStackMarker++;
	}

	private Frame popFrame() {
		if (frameStackMarker==0) {return null;}
		else {
			frameStackMarker--;
			return frameStack[frameStackMarker];
		}
	}

	public Variable invoke(MethodInfo method,Variable oref,Variable[] params) {
		Frame f=new Frame(method,oref,params);
		ExecutionStatus status=null;
		Variable state=null;
		boolean completed=false;

		while (!completed) {
			status=f.execute(state);
			if (status.isCompleted) {
				return status.returnValue;
			} else {
				//we need more information before we can return
				FrameContext fctx=status.ctx;
				if (fctx.zmethod!=null) {
					//it's a PlZero method
					pushFrame(f);
					//check if this is a
					//do this recursively
					//System.out.print("DEBUG: ZThread.invoke ");
					if (fctx.params==null) {System.out.println("fctx.params==null");}
					state=invoke(fctx.zmethod,fctx.vobj,fctx.params);
					f=popFrame();
				} else {
					//call in to java
					Object res=javaInvoke(fctx);
					if (res instanceof String) {
						//convert it to a pz string
						state = new Variable((String)res);
					} else if (res instanceof Integer) {
						Integer ri=(Integer)res;
						state = new Variable(ri.intValue());
					} else {
						//store it natively
						state=Heap.addJavaObject(res);
					}
				}
			}
		}
		System.out.println("warning: ZThread.invoke() should have returned before this");
		return null;
	}

	//pass this off to the host jvm
	//at this point, we can only handle 1 argument
	public static Object javaInvoke(FrameContext ctx) {
		if (ctx==null) {throw new IllegalArgumentException("ctx is null");}
		if (!ctx.isStatic && ctx.vobj==null) {throw new IllegalArgumentException("ctx.vobj is null");}
		//System.out.println("DEBUG: ZThread.javaInvoke method.name="+ctx.jmethod.getName());
		Class[] types=ctx.jmethod.getParameterTypes();
		//Class type0=types[0];
		//System.out.println("DEBUg: ZThread.javaInvoke method.type="+type0.getName());
		try {
			//get the base object
			Object base=null;
			if (!ctx.isStatic) {
				byte type=ctx.vobj.tag();
				if (type!=Variable.JAVA_OBJECT) {
					System.out.println("vobj type is not a Java Object");
					return null;
				} else {
					base=Heap.get(ctx.vobj.getInt());
				}
			}

			//now get the parameters
			//we only handle int,String and Object
			//convert params to jparams
			Variable[] params=ctx.params;
			Object[] jparams=new Object[params.length];
			for (int i=0;i<params.length;i++) {
				Variable vp=params[i];
				if (vp.tag()==Variable.INT) {
					jparams[i]=Integer.valueOf(vp.getInt());
				} else if (vp.tag()==Variable.FLOAT) {
					jparams[i]=Float.valueOf(vp.getFloat());
				} else if (vp.tag()==Variable.UTF8) {
					jparams[i]=vp.toString();
				} else if (vp.tag()==Variable.JAVA_OBJECT) {
					Object o=Heap.get(vp.getInt());
					jparams[i]=o;
				} else {
					throw new IllegalStateException("unable to set params because type is "+vp.tag());
				}
			}

			//System.out.println("DEBUG: ZThread.javaInvoke() base.type="+base.getClass().getName());
			//System.out.println("DEBUG: ZThread.javaInvoke() jparam1.type="+jparam1.getClass().getName());
			//finally we can run it
			//we only handle 1 param
			return ctx.jmethod.invoke(base,jparams);
		} catch (Exception x) {
			x.printStackTrace();
		}
		return null;
	}

}