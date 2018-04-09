package plzero;
import java.util.Arrays;

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

	//despite the fancy name, this can only decipher 1 param type.
	//this is meant for matching a java method
	public static Class[] getMethodParameterTypes(byte[] methodType) {
		String methodTypeString=new String(methodType);
		if (methodTypeString.startsWith("(Ljava/lang/String;")) {
			return new Class[]{String.class};
		} else if (methodTypeString.startsWith("(I")) {
			return new Class[]{Integer.TYPE};
		} else {
			System.out.println("unable to decipher methodType "+methodTypeString);
			return null;
		}
	}
}