package com.krakenrs.spade.ir.gen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.krakenrs.spade.commons.collections.tuple.Tuple3.ImmutableTuple3;
import com.krakenrs.spade.ir.code.expr.InvokeExpr;
import com.krakenrs.spade.ir.code.stmt.JumpCondStmt;
import com.krakenrs.spade.ir.gen.LocalStack.TypedLocal;
import com.krakenrs.spade.ir.type.PrimitiveType;
import com.krakenrs.spade.ir.type.TypeManager;
import com.krakenrs.spade.ir.type.ValueType;

public class TypeHelper implements org.objectweb.asm.Opcodes {

    private static TypedLocal remove_block(LocalStack initialStack, LocalStack currentStack, int remaining) {
        TypedLocal e = currentStack.pop();
        int width = e.getB().getSize();

        if (remaining - width < 0) {
            /* the end of the block is in the middle of one of the elements of the stack */
            int e_idx = initialStack.size() - currentStack.size() - 1;
            throw new IllegalStateException(
                    String.format("Element +%d from the top: %s is too large, expected width of %d, got %d: %s", e_idx,
                            e, remaining, width, initialStack.toString()));
        } else {
            return e;
        }
    }

    public static List<ImmutableTuple3<Integer, Integer, ValueType>> dupx(LocalStack stack, int block_width,
            int offset) {
        /* copies in the form (dst, src) */
        List<ImmutableTuple3<Integer, Integer, ValueType>> copies = new ArrayList<>();
        LocalStack newStack = stack.copy();

        /*
         * we don't know how many items are in the block, meaning we don't know the
         * maximum index we will use when allocating the vars. therefore we count from 0
         * instead of counting backwards from the maximum index and translate this index
         * when we generate the real copy.
         */
        Map<Integer, ValueType> varTypes = new HashMap<>();
        int inverse_spill_index = 0;
        int block_remaining = block_width;
        /* the number of actual elements in the block */
        int block_items = 0;
        /* get the block we want to copy */
        while (block_remaining > 0) {
            TypedLocal e = remove_block(stack, newStack, block_remaining);
            block_remaining -= e.getB().getSize();
            copies.add(new ImmutableTuple3<>(inverse_spill_index, e.getA().index(), e.getB()));
            varTypes.put(inverse_spill_index, e.getB());
            inverse_spill_index++;
            block_items++;
        }

        /* -1 as the first var starts at 0 */
        final int max_index = stack.size() + block_items - 1;

        int offset_remaining = offset;
        while (offset_remaining > 0) {
            /*
             * these elements have to be reassigned because they come before the offset
             * point
             */
            TypedLocal e = remove_block(stack, newStack, offset_remaining);
            offset_remaining -= e.getB().getSize();
            copies.add(new ImmutableTuple3<>(inverse_spill_index++, e.getA().index(), e.getB()));
        }

        int temp_index = max_index;
        while (block_items-- > 0) {
            copies.add(new ImmutableTuple3<>(inverse_spill_index++, temp_index, varTypes.get(max_index - temp_index)));
            temp_index--;
        }

        List<ImmutableTuple3<Integer, Integer, ValueType>> realCopies = new ArrayList<>();
        for (ImmutableTuple3<Integer, Integer, ValueType> t : copies) {
            realCopies.add(new ImmutableTuple3<>(max_index - t.getA(), t.getB(), t.getC()));
        }
        return realCopies;
    }

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
            return types.asClassType(Object.class).asValueType();
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
            throw new UnsupportedOperationException(asmType.toString() + " :" + sort);
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
