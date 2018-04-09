package plzero.jvm;

/**
* This has a minimal set of opcodes. We only handle those below, not the complete set.
*
* We handle longs only where absolutely required, so the long version of the opcodes below aren't supported.
*
* There are 3 types of opcodes: 1) those that deal with logic (most of them), 2) those that deal
*	with control flow, like Goto, IF and return, 2) and those that call other frames, like InvokeVirtual
*/
public interface OpCode {
	public static final short ACONST_NULL 	= 1;
	public static final short ICONST_M1 	= 2;
	public static final short ICONST_0 		= 3;	//literal 0
	public static final short ICONST_1 		= 4;
	public static final short ICONST_2 		= 5;
	public static final short ICONST_3     	= 6;
	public static final short ICONST_4     	= 7;
	public static final short ICONST_5     	= 8;
	public static final short FCONST_0 		= 11;
	public static final short FCONST_1 		= 12;
	public static final short FCONST_2 		= 13;
	public static final short BIPUSH     	= 16;
	public static final short SIPUSH     	= 17;
	public static final short LDC     		= 18;
	public static final short LDC_W     	= 19;
	public static final short ILOAD			= 21;
	public static final short ILOAD_0      	= 26;
	public static final short ILOAD_1      	= 27;
	public static final short ILOAD_2      	= 28;
	public static final short ILOAD_3      	= 29;
	public static final short FLOAD_0      	= 34;
	public static final short FLOAD_1      	= 35;
	public static final short FLOAD_2      	= 36;
	public static final short FLOAD_3      	= 37;
	public static final short ALOAD_0		= 42;
	public static final short ALOAD_1		= 43;
	public static final short ALOAD_2		= 44;
	public static final short ALOAD_3		= 45;
	public static final short IALOAD 		= 46;
	public static final short AALOAD 		= 50;
	public static final short SALOAD 		= 53;
	public static final short ISTORE		= 54;
	public static final short ISTORE_0 		= 59;
	public static final short ISTORE_1 		= 60;
	public static final short ISTORE_2 		= 61;
	public static final short ISTORE_3 		= 62;
	public static final short FSTORE_0 		= 67;
	public static final short FSTORE_1 		= 68;
	public static final short FSTORE_2 		= 69;
	public static final short FSTORE_3 		= 70;
	public static final short ASTORE_0 		= 75;
	public static final short ASTORE_1 		= 76;
	public static final short ASTORE_2 		= 77;
	public static final short ASTORE_3 		= 78;
	public static final short IASTORE 		= 79;
	public static final short AASTORE 		= 83;
	public static final short SASTORE 		= 86;
	public static final short DUP 			= 89;
	public static final short IADD 			= 96;
	public static final short FADD 			= 98;
	public static final short ISUB			= 100;
	public static final short FSUB			= 102;
	public static final short IMUL			= 104;
	public static final short FMUL			= 106;
	public static final short IDIV			= 108;
	public static final short FDIV			= 110;
	public static final short IREM			= 112;
	public static final short FREM			= 114;
	public static final short INEG			= 116;
	public static final short FNEG			= 118;
	//we don't implement long or double ops
	//public static final short LADD 			= 97;
	//public static final short LSUB			= 101;
	//public static final short LMUL			= 105;
	//public static final short LDIV			= 109;
	//public static final short LREM			= 113;
	//public static final short LNEG			= 117;
	//public static final short LAND			= 127;
	//public static final short LOR			= 129;
	//public static final short LXOR			= 131;
	public static final short IAND			= 126;
	public static final short IOR			= 128;
	public static final short IXOR			= 130;
	public static final short IINC			= 132;
	public static final short I2F 			= 134;
	public static final short F2I 			= 139;
	public static final short IFNE 			= 154;
	public static final short IF_ICMPNE 	= 160;	// (0xa0)
	public static final short IF_ICMPGE       = 162;	//follow by line no
	//there are lots of other IF instructions we will implement as needed
	public static final short GOTO         = 167;
	public static final short IRETURN         = 172;
	public static final short FRETURN         = 174;
	public static final short ARETURN         = 176;
	public static final short RETURN		= 177;
	public static final short GETSTATIC		= 178;	//followed by static field num
	public static final short PUTSTATIC 	= 179;
	public static final short GETFIELD 		= 180;
	public static final short PUTFIELD 		= 181;
	public static final short INVOKEVIRTUAL	= 182;	//followed by methodref
	public static final short INVOKESPECIAL	= 183;	//followed by <init> method
	public static final short INVOKESTATIC	= 184;  //follow by methodref
	public static final short NEW_     		= 187;	//followed by class
	public static final short NEWARRAY     	= 188;	//create new array of primitive type
	public static final short ANEWARRAY     = 189;	//create new array of class, array or interface type
	public static final short ARRAYLENGTH = 190;
}