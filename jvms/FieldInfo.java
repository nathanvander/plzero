package plzero.jvms;
import java.util.Arrays;
import plzero.Variable;
import org.apache.bcel.classfile.*;

//this is a bare-bones version of a field.
public class FieldInfo {
	public static final short ACC_STATIC = 0x0008;
	public final ClassInfo class_info;
	public final short access_flags;
	public final byte[] name;
	public final byte[] descriptor;
	//this is usually null
	public final Variable init_value;

	public FieldInfo(ClassInfo c,Field f) {
		class_info=c;
		access_flags=(short)f.getAccessFlags();
		int nx=f.getNameIndex();
		name=c.pool.get((short)nx).getUtf8();
		int dx=f.getSignatureIndex();
		descriptor=c.pool.get((short)dx).getUtf8();
		//----------
		//init value
		ConstantValue cv=f.getConstantValue();
		if (cv==null) {
			init_value=null;
		} else {
			int cvx=cv.getConstantValueIndex();
		 	init_value=c.pool.get((short)cvx);
		}
	}

	public boolean isStatic() {
		return (0 != (access_flags & ACC_STATIC));
	}

	//does this name equal the proferred name
	public boolean nameEquals(byte[] b) {
		if (b==null) return false;
		else return Arrays.equals(name,b);
	}

}