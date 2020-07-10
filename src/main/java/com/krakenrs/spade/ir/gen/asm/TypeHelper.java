package com.krakenrs.spade.ir.gen.asm;

import com.krakenrs.spade.ir.code.expr.InvokeExpr;
import com.krakenrs.spade.ir.code.stmt.JumpCondStmt;
import com.krakenrs.spade.ir.type.PrimitiveType;
import com.krakenrs.spade.ir.type.TypeManager;
import com.krakenrs.spade.ir.type.ValueType;

public class TypeHelper implements org.objectweb.asm.Opcodes {

    public static ValueType getArithmeticType(int opcode) {
        switch (opcode) {
            case IADD:
            case ISUB:
            case IMUL:
            case IDIV:
            case IREM:
            case INEG:
            case ISHL:
            case ISHR:
            case IUSHR:
            case IAND:
            case IOR:
            case IXOR:
                return PrimitiveType.INT;
            case LADD:
            case LSUB:
            case LMUL:
            case LDIV:
            case LREM:
            case LNEG:
            case LSHL:
            case LSHR:
            case LUSHR:
            case LAND:
            case LOR:
            case LXOR:
                return PrimitiveType.LONG;
            case FADD:
            case FSUB:
            case FMUL:
            case FDIV:
            case FREM:
            case FNEG:
                return PrimitiveType.FLOAT;
            case DADD:
            case DSUB:
            case DMUL:
            case DDIV:
            case DREM:
            case DNEG:
                return PrimitiveType.DOUBLE;
            default:
                throw new IllegalArgumentException(String.valueOf(opcode));
        }
    }

    public static ValueType getStoreType(TypeManager types, int opcode) {
        switch (opcode) {
            case ISTORE:
                return PrimitiveType.INT;
            case LSTORE:
                return PrimitiveType.LONG;
            case FSTORE:
                return PrimitiveType.FLOAT;
            case DSTORE:
                return PrimitiveType.DOUBLE;
            case ASTORE:
                return types.asClassType("java/lang/Object").asValueType();
            default:
                throw new IllegalArgumentException(String.valueOf(opcode));
        }
    }

    public static ValueType getLoadType(TypeManager types, int opcode) {
        switch (opcode) {
            case ILOAD:
                return PrimitiveType.INT;
            case LLOAD:
                return PrimitiveType.LONG;
            case FLOAD:
                return PrimitiveType.FLOAT;
            case DLOAD:
                return PrimitiveType.DOUBLE;
            case ALOAD:
                return types.asClassType("java/lang/Object").asValueType();
            default:
                throw new IllegalArgumentException(String.valueOf(opcode));
        }
    }

    public static PrimitiveType getCastType(int opcode) {
        switch (opcode) {
            case I2B:
                return PrimitiveType.BYTE;
            case I2C:
                return PrimitiveType.CHAR;
            case I2S:
                return PrimitiveType.SHORT;
            case L2I:
            case F2I:
            case D2I:
                return PrimitiveType.INT;
            case I2F:
            case L2F:
            case D2F:
                return PrimitiveType.FLOAT;
            case I2D:
            case L2D:
            case F2D:
                return PrimitiveType.DOUBLE;
            case I2L:
            case F2L:
            case D2L:
                return PrimitiveType.LONG;
            default:
                throw new IllegalArgumentException(String.valueOf(opcode));
        }
    }

    public static ValueType getValueType(TypeManager types, Object cst) {
        if (cst == null) {
            throw new NullPointerException();
        } else if (cst instanceof String) {
            return types.asClassType(String.class).asValueType();
        } else if (cst instanceof Character) {
            return PrimitiveType.CHAR;
        } else if (cst instanceof Byte) {
            return PrimitiveType.BYTE;
        } else if (cst instanceof Short) {
            return PrimitiveType.SHORT;
        } else if (cst instanceof Integer) {
            return PrimitiveType.INT;
        } else if (cst instanceof Float) {
            return PrimitiveType.FLOAT;
        } else if (cst instanceof Long) {
            return PrimitiveType.LONG;
        } else if (cst instanceof Double) {
            return PrimitiveType.DOUBLE;
        } else if (cst instanceof org.objectweb.asm.Type) {
            org.objectweb.asm.Type asmType = (org.objectweb.asm.Type) cst;
            int sort = asmType.getSort();
            throw new UnsupportedOperationException(cst + " :: " + asmType.toString() + " | " + sort);
        } else {
            throw new UnsupportedOperationException(cst.getClass().getName());
        }
    }

    public static PrimitiveType getPrimitiveType(int tcode) {
        switch (tcode) {
            case T_BOOLEAN:
                return PrimitiveType.BOOLEAN;
            case T_BYTE:
                return PrimitiveType.BYTE;
            case T_SHORT:
                return PrimitiveType.SHORT;
            case T_CHAR:
                return PrimitiveType.CHAR;
            case T_INT:
                return PrimitiveType.INT;
            case T_LONG:
                return PrimitiveType.LONG;
            case T_FLOAT:
                return PrimitiveType.FLOAT;
            case T_DOUBLE:
                return PrimitiveType.DOUBLE;
            default:
                throw new IllegalArgumentException(String.valueOf(tcode));
        }
    }

    public static ValueType getArrayLoadType(TypeManager types, int opcode) {
        switch (opcode) {
            case IALOAD:
                return PrimitiveType.INT;
            case LALOAD:
                return PrimitiveType.LONG;
            case FALOAD:
                return PrimitiveType.FLOAT;
            case DALOAD:
                return PrimitiveType.DOUBLE;
            case AALOAD:
                return types.asClassType("java/lang/Object").asValueType();
            case BALOAD:
                return PrimitiveType.BYTE;
            case CALOAD:
                return PrimitiveType.CHAR;
            case SALOAD:
                return PrimitiveType.SHORT;
            default:
                throw new IllegalArgumentException(String.valueOf(opcode));
        }
    }

    public static ValueType getArrayStoreType(TypeManager types, int opcode) {
        switch (opcode) {
            case IASTORE:
                return PrimitiveType.INT;
            case LASTORE:
                return PrimitiveType.LONG;
            case FASTORE:
                return PrimitiveType.FLOAT;
            case DASTORE:
                return PrimitiveType.DOUBLE;
            case AASTORE:
                return types.asClassType("java/lang/Object").asValueType();
            case BASTORE:
                return PrimitiveType.BYTE;
            case CASTORE:
                return PrimitiveType.CHAR;
            case SASTORE:
                return PrimitiveType.SHORT;
            default:
                throw new IllegalArgumentException(String.valueOf(opcode));
        }
    }

    public static JumpCondStmt.Mode getConditionalCompareMode(int opcode) {
        switch (opcode) {
            case IF_ACMPEQ:
            case IF_ICMPEQ:
            case IFEQ:
                return JumpCondStmt.Mode.EQ;
            case IF_ACMPNE:
            case IF_ICMPNE:
            case IFNE:
                return JumpCondStmt.Mode.NE;
            case IF_ICMPGT:
            case IFGT:
                return JumpCondStmt.Mode.GT;
            case IF_ICMPGE:
            case IFGE:
                return JumpCondStmt.Mode.GE;
            case IF_ICMPLT:
            case IFLT:
                return JumpCondStmt.Mode.LT;
            case IF_ICMPLE:
            case IFLE:
                return JumpCondStmt.Mode.LE;
            default:
                throw new IllegalArgumentException(String.valueOf(opcode));
        }
    }

    public static InvokeExpr.Mode getInvokeExprMode(int opcode) {
        switch (opcode) {
            case INVOKEVIRTUAL:
                return InvokeExpr.Mode.VIRTUAL;
            case INVOKESTATIC:
                return InvokeExpr.Mode.STATIC;
            case INVOKEDYNAMIC:
                return InvokeExpr.Mode.DYNAMIC;
            case INVOKESPECIAL:
                return InvokeExpr.Mode.SPECIAL;
            case INVOKEINTERFACE:
                return InvokeExpr.Mode.INTERFACE;
            default:
                throw new IllegalArgumentException(String.valueOf(opcode));
        }
    }
}
