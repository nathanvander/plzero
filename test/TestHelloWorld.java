package plzero.test;
import plzero.jvm.*;
import java.io.IOException;

//this now does inline println
public class TestHelloWorld {
	//create our class
	public static class HelloWorld {
		public static void main(String[] args) {
			System.out.println("Hello World!");
		}
	}

	//now run it in our virtual machine
	public static void main(String[] args) throws IOException {
		String className="plzero/test/TestHelloWorld$HelloWorld";
		String[] margs=new String[]{className};
		Main.main(margs);
	}
}