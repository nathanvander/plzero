package plzero.test;
import plzero.jvm.*;
import java.io.IOException;

//challenge.  Accept a number from the command-line and convert it to hex
//using the method in Integer, and print it out

public class TestHex {
	//create our class
	public static class Hex {
		public static String convert(int i) {
			//invoke java to convert it
			return Integer.toString(i,16);
		}

		public static void main(String[] args) {
			int i=Integer.parseInt(args[0]);
			String s=convert(i);
			System.out.println(s);
			//should print out: e
		}
	}

	//now run it in our virtual machine
	public static void main(String[] args) throws IOException {
		String className="plzero/test/TestHex$Hex";
		String stri=args[0];
		String[] margs=new String[]{className,stri};
		Main.main(margs);
	}
}