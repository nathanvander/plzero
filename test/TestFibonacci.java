package plzero.test;
import plzero.jvm.*;
import java.io.IOException;

/**
* In the Fibonacci sequence, each number is defined as the sum of the two previous fib. So the sequence looks like this:
*	fib(0) => 0
*	fib(1) => 1
*	fib(2) => 1
*	fib(3) => 2
*	fib(4) => 3
* 	fib(5) => 5
*	fib(6) => 8
*/

public class TestFibonacci {
	//the Fibonacci tests recursion
	public static class Fibonacci {

		public static int fib(int n) {
			if (n==0) return 0;
			else if (n<3) return 1;
			else if (n<5) return n-1;
			else {
				return fib(n-1) + fib(n-2);
			}
		}

		public static void main(String[] args) {
			int i=Integer.parseInt(args[0]);
			int f=fib(i);
			System.out.println(f);
		}
	}

	//now run it
	public static void main(String[] args) throws IOException {
		String className="plzero/test/TestFibonacci$Fibonacci";
		String[] margs=new String[]{className,args[0]};
		Main.main(margs);
	}
}