package plzero.jvm;
import plzero.Variable;
import plzero.jvms.*;
import java.io.IOException;

//use this to in turn call static void main on the target class
public class Main {
	public static final byte[] MAIN="main".getBytes();
	public static final byte[] MAIN_TYPE="([Ljava/lang/String;)V".getBytes();

	public static void main(String[] args) throws IOException {
		String className=args[0];
		ClassInfo ci=ClassInfo.getClass(className.getBytes());
		MethodInfo method=ci.getMethod(MAIN,MAIN_TYPE);

		//pass in the params.  Note that for the main method, it has a single parameter which is a String array
		Variable[] params=null;
		if (args.length>1) {
			params=new Variable[1];
			//pass these in as Strings
			Array ar=new Array("java.lang.String".getBytes(),(short)(args.length-1));
			for (short i=1;i<args.length;i++) {
				Variable vstring=new Variable(args[i]);
				ar.set((short)(i-1),vstring);
			}
			Variable varr=Heap.addArray(ar);
			params[0]=varr;
		}

		ZThread zt=new ZThread();
		zt.invoke(method,null,params);
	}

	//public static Variable parseVariable(String s) {
	//	try {
	//		if (isFloat(s)) {
	//			float f=Float.parseFloat(s);
	//			return new Variable(f);
	//		} else if (isInt(s)) {
	//			int i=Integer.parseInt(s);
	//			return new Variable(i);
	//		} else {
	//			return new Variable(s);
	//		}
	//	} catch (Exception x) {
	//		//shouldn't throw any exceptions because we have already trapped them
	//		x.printStackTrace();
	//	}
	//	return null;
	//}

	//public static boolean isFloat(String s) {
	//	int dot=s.indexOf((int)'.');
	//	if (dot<0) return false;
	//	try {
	//		Float.parseFloat(s);
	//		return true;
	//	} catch (Exception x) {
	//		return false;
	//	}
	//}

	//public static boolean isInt(String s) {
	//	try {
	//		Integer.parseInt(s);
	//		return true;
	//	} catch (Exception x) {
	//		return false;
	//	}
	//}

}