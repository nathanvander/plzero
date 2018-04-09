package plzero.jvm;
import plzero.jvms.ClassInfo;
import plzero.Variable;
import java.util.Vector;

/**
* The Heap stores objects and arrays. And java objects.
*
*/
public class Heap {
	private static Vector mem=new Vector(128);

	static {
		mem.add(null);	//set entry 0 to null
	}

	//the new command
	public static synchronized Variable createNewObject(ClassInfo cinfo) {
		Obj ob=new Obj(cinfo,null);
		mem.add(ob);
		int index=mem.lastIndexOf(ob);
		return new Variable(Variable.OBJ,index);
	}

	//arrays
	//create a new array of the given size
	public static synchronized Variable createNewArray(byte[] type,short s) {
		Array arr=new Array(type,s);
		mem.add(arr);
		int index=mem.lastIndexOf(arr);
		return new Variable(Variable.ARRAY,index);
	}

	public static synchronized Variable addArray(Array arr) {
		if (arr==null) return Variable.NIL;
		mem.add(arr);
		int index=mem.lastIndexOf(arr);
		return new Variable(Variable.ARRAY,index);
	}

	//this doesn't create the object
	//handles java arrays as well
	public static synchronized Variable addJavaObject(Object job) {
		if (job==null) {
			return Variable.NIL;
		} else if (job instanceof String) {
			String str=(String)job;
			return new Variable(str);
		} else {
			mem.add(job);
			int index=mem.lastIndexOf(job);
			return new Variable(Variable.JAVA_OBJECT,index);
		}
	}

	public static Object get(int ix) {
		return mem.elementAt(ix);
	}
}