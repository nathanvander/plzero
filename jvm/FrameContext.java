package plzero.jvm;
import plzero.Variable;
import plzero.jvms.MethodInfo;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
* This is generic so you can either set up a
* PlZero methodInfo invocation or a Java method invocation.
*/
public class FrameContext {
	public final boolean isStatic;
	//this has the name, class and type. It is not needed at this point but we include it for debugging
	public final Variable vmethod;
	public final MethodInfo zmethod;
	public final Method jmethod;
	//the object on which the method is called.  Will be null if static
	public final Variable vobj;
	public final Variable[] params;

	//use this for plzero methods
	public FrameContext(Variable vmethod,MethodInfo method,Variable vobj,Variable[] params) {
		isStatic=method.isStatic();
		this.vmethod=vmethod;
		zmethod=method;
		this.vobj=vobj;
		this.params=params;
		jmethod=null;
	}

	//use this for java methods
	public FrameContext(Variable vmethod,Method meth,Variable vobj,Variable[] params) {
	 	if (Modifier.isStatic(meth.getModifiers())) {
			isStatic=true;
	 	} else {
			isStatic=false;
		}
		this.vmethod=vmethod;
		jmethod=meth;
		this.vobj=vobj;
		this.params=params;
		zmethod=null;
	}
}