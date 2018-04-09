package plzero;
import java.util.Arrays;
import java.util.Vector;

/**
* BString contains utility methods to help byte[] arrays act as strings.
*/
public class BString {
	public static String FRONT="/";
	public static String DOT=".";

	//compare to String.equals
	public static boolean equals(byte[] b1,byte[] b2) {
		if (b1==null || b2==null) return false;
		else return Arrays.equals(b1,b2);
	}

	//utility function based on String.indexOf()
	//look for byte b in the given byte array
	public static int indexOf(byte[] ba,byte b) {
		for (int i=0;i<ba.length;i++) {
			if (ba[i]==b) return i;
		}
		return -1;
	}

	//utility function based on String.substring()
	//return a substring of byte array, starting at beg, ending at end
	public static byte[] substring(byte[] ba,int beg,int end) {
		byte[] bb=new byte[end-beg];
		System.arraycopy(ba,beg,bb,0,end-beg);
		return bb;
	}

	//use instead of System.out.print
	public static void print(byte[] ba) {
		System.out.write(ba,0,ba.length);
	}

	//use instead of System.out.println
	public static void println(byte[] ba) {
		print(ba);
		System.out.write('\r');
		System.out.write('\n');
	}

	public static boolean startsWith(byte[] ba, byte[] starter) {
		if (ba==null || starter==null) {return false;}
		if (starter.length>ba.length) {return false;}
		for (int i=0;i<starter.length;i++) {
			if (ba[i]!=starter[i]) {return false;}
		}
		return true;
	}

	//-----------------------------------------
	//other non-string methods

	//given a java classname with slashes, change them to dots
	public static String doticize(byte[] cname) {
		String c=new String(cname);
		return c.replaceAll(FRONT,DOT);
	}

	//this is meant for matching a java method
	//we expect the format to look something like (IILjava/lang/String;)V
	public static Class[] getMethodParameterTypes(byte[] methodType) throws ClassNotFoundException {
		String methodTypeString=new String(methodType);
		Vector v=new Vector();	//temporary list
		//start with 1 because 0 is (
		int st=1;
		Class k=nextToken(methodTypeString,st);

		while (k!=null) {
			//System.out.println("adding token type "+k.toString());
			v.add(k);
			if (k.isPrimitive()) {
				st++;
			} else {
				st=st+k.getName().length()+2;
			}
			k=nextToken(methodTypeString,st);
		}

		Class[] params=new Class[v.size()];
		for (int i=0;i<params.length;i++) {
			params[i]=(Class)v.elementAt(i);
		}
		return params;
	}

	//we only handle String,int,float,boolean
	public static Class nextToken(String paramType, int start) throws ClassNotFoundException {
		//System.out.println("looking for token starting with "+start);
		if (start+1>paramType.length()) return null;
		char ch=paramType.charAt(start);
		if (ch==')') return null;	//no more

		if (ch=='I') {
			return Integer.TYPE;
		} else if (ch=='F') {
			return Float.TYPE;
		} else if (ch=='Z') {
			return Boolean.TYPE;
		} else if (ch=='L') {
			int end=paramType.indexOf((int)';',start);
			String className=paramType.substring(start+1,end);
			className=className.replaceAll(FRONT,DOT);
			Class c=Class.forName(className);
			return c;
		} else {
			System.out.println("DEBUG: BString.nextToken unhandled class type "+ch);
			return null;
		}
	}

	public static void main(String[] args) throws ClassNotFoundException {
		System.out.println("testing getMethodParameterTypes().  Input should look like: (Ljava/lang/String;I)V");
		Class[] p=getMethodParameterTypes(args[0].getBytes())	;
		for (int i=0;i<p.length;i++) {
			Class k=p[i];
			System.out.println("param "+i+" "+k.getName());
		}
	}
}