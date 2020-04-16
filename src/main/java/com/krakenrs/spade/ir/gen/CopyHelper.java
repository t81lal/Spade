package com.krakenrs.spade.ir.gen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.krakenrs.spade.commons.collections.tuple.Tuple3.ImmutableTuple3;
import com.krakenrs.spade.ir.gen.LocalStack.TypedLocal;
import com.krakenrs.spade.ir.type.ValueType;

public class CopyHelper {

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
}
