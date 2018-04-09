package plzero.jvm;
import plzero.Variable;

/**
* ExecutionStatus is the return value from execute
*/
public class ExecutionStatus {
	public final boolean isCompleted;
	//set this if the status is completed, can be null
	public final Variable returnValue;
	//if invoking another method, use this
	public final FrameContext ctx;

	//only set 1 of returnValue,invoked
	public ExecutionStatus(boolean completed,Variable rv,FrameContext fc) {
		isCompleted=completed;
		returnValue=rv;
		ctx=fc;
	}
}