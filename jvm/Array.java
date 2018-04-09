package plzero.jvm;
import plzero.Variable;

/**
* A Plzero Array can hold values of any type.  The type field is for debugging only.
*/
public class Array {
	public final short size;
	public final byte[] type;
	private Variable[] values;

	public Array(byte[] type,short size) {
		this.size=size;
		this.type=type;
		if (size<1) {
			throw new IllegalArgumentException();
		}
		values=new Variable[size];
	}

	public Variable get(short index) {
		if (index<0 || index > size) {throw new IndexOutOfBoundsException(String.valueOf(index));}
		return values[index];
	}

	public void set(short index,Variable v) {
		if (index<0 || index > size) {throw new IndexOutOfBoundsException(String.valueOf(index));}
		values[index]=v;
	}

	public int length() {return values.length;}
}