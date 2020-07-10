package com.krakenrs.spade.ir.gen.asm;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.jupiter.api.Test;

import com.krakenrs.spade.commons.collections.tuple.Tuple2.ImmutableTuple2;
import com.krakenrs.spade.commons.collections.tuple.Tuple3.ImmutableTuple3;
import com.krakenrs.spade.ir.gen.asm.LocalStack.TypedLocal;
import com.krakenrs.spade.ir.type.PrimitiveType;
import com.krakenrs.spade.ir.type.ValueType;
import com.krakenrs.spade.ir.value.Local;

public class CopyHelperTest {

    private List<ImmutableTuple2<String, String>> generateAssigns(
            List<ImmutableTuple3<Integer, Integer, ValueType>> in) {
        List<ImmutableTuple2<String, String>> out = new ArrayList<>();
        for (ImmutableTuple3<Integer, Integer, ValueType> t : in) {
            out.add(new ImmutableTuple2<>("var" + t.getA(), "var" + t.getB()));
        }
        return out;
    }

    private void test(String fileName, List<ImmutableTuple2<String, String>> generatedAssigns) throws Exception {
        List<String> lines = Files.readAllLines(
                new File(CopyHelperTest.class.getResource("specs/" + fileName + ".txt").toURI()).toPath());
        List<ImmutableTuple2<String, String>> assigns = new ArrayList<>();
        Map<String, Integer> values = new HashMap<>();
        Map<String, Integer> expects = new HashMap<>();
        parseAssigns(lines, values, expects, assigns);
        // Map<String, Integer> initialValues = new HashMap<>(values);

        if (generatedAssigns != null) {
            assigns = generatedAssigns;
        }

        simulateAssigns(values, assigns);

        if (!checkValues(values, expects)) {
            // System.out.println(fileName + "::Fail");
            // System.out.println("Initial:");
            // printValues(initialValues);
            // System.out.println("Final:");
            // printValues(values);
            // System.out.println("Expect:");
            // printValues(expects);
            // System.out.println("Code:");
            // printCode(assigns);
            fail(fileName);
        }
    }

    /*private void printCode(List<ImmutableTuple2<String, String>> assigns) {
        for (ImmutableTuple2<String, String> t : assigns) {
            System.out.println(t.getA() + " = " + t.getB());
        }
    }*/

    /*private void printValues(Map<String, Integer> values) {
        for (Entry<String, Integer> e : values.entrySet()) {
            System.out.println(e.getKey() + " = " + e.getValue());
        }
    }*/

    private boolean checkValues(Map<String, Integer> values, Map<String, Integer> expects) {
        for (Entry<String, Integer> e : expects.entrySet()) {
            if (values.containsKey(e.getKey())) {
                if (!e.getValue().equals(values.get(e.getKey()))) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    private void parseAssigns(List<String> lines, Map<String, Integer> values, Map<String, Integer> expects,
            List<ImmutableTuple2<String, String>> assigns) {
        for (String line : lines) {
            boolean ex;
            if (ex = line.startsWith("expect")) {
                line = line.substring("expect".length());
            }
            String[] vars = line.split("=");
            if (vars.length != 2)
                continue;
            String src = vars[1].trim();
            String dst = vars[0].trim();
            try {
                int value = Integer.parseInt(src);
                if (ex) {
                    expects.put(dst, value);
                } else {
                    values.put(dst, value);
                }
            } catch (NumberFormatException e) {
                if (ex)
                    throw new UnsupportedOperationException();
                assigns.add(new ImmutableTuple2<>(dst, src));
            }
        }
    }

    private void simulateAssigns(Map<String, Integer> values, List<ImmutableTuple2<String, String>> assigns) {
        for (ImmutableTuple2<String, String> t : assigns) {
            String dst = t.getA();
            String src = t.getB();
            values.put(dst, values.get(src));
        }
    }

    private LocalStack createStack(PrimitiveType... types) {
        LocalStack stack = new LocalStack();
        for (int i = 0; i < types.length; i++) {
            stack.push(new TypedLocal(new Local(i, true), types[i]));
        }
        return stack;
    }

    private void test(String fileName, int block_width, int offset, PrimitiveType... reverseStack) throws Exception {
        test(fileName, generateAssigns(CopyHelper.dupx(createStack(reverseStack), block_width, offset)));
    }

    @Test
    void test_dup_x1() throws Exception {
        // dup_x1 [int, int] => [int, int, int]
        test("dup_x1", 1, 1, PrimitiveType.INT, PrimitiveType.INT);
    }

    @Test
    void test_dup_x2_form1() throws Exception {
        // dup1_x2 form 1: [int, int, int] => [int, int, int, int]
        test("dup_x2_form1", 1, 2, PrimitiveType.INT, PrimitiveType.INT, PrimitiveType.INT);
    }

    @Test
    void test_dup_x2_form2() throws Exception {
        // dup1_x2 form2: [int, long] => [int, long, int]
        test("dup_x2_form2", 1, 2, PrimitiveType.LONG, PrimitiveType.INT);
    }

    @Test
    void test_dup2_x1_form1() throws Exception {
        // dup2_x1 form 1: [int, int, int] => [int, int, int, int, int]
        test("dup2_x1_form1", 2, 1, PrimitiveType.INT, PrimitiveType.INT, PrimitiveType.INT);
    }

    @Test
    void test_dup2_x1_form2() throws Exception {
        // dup2_x1 form 2: [long, int] => [long, int, long]
        test("dup2_x1_form2", 2, 1, PrimitiveType.INT, PrimitiveType.LONG);
    }

    @Test
    void test_dup2_x2_form1() throws Exception {
        // dup2_x2 form 1: [int, int, int, int] => [int, int, int, int, int, int]
        test("dup2_x2_form1", 2, 2, PrimitiveType.INT, PrimitiveType.INT, PrimitiveType.INT, PrimitiveType.INT);
    }

    @Test
    void test_dup2_x2_form2() throws Exception {
        // dup2_x2 form2: [long, int, int] => [long, int, int, long]
        test("dup2_x2_form2", 2, 2, PrimitiveType.INT, PrimitiveType.INT, PrimitiveType.LONG);
    }

    @Test
    void test_dup2_x2_form3() throws Exception {
        // dup2_x2 form3: [int, int, long] => [int, int, long, int, int]
        test("dup2_x2_form3", 2, 2, PrimitiveType.LONG, PrimitiveType.INT, PrimitiveType.INT);
    }

    @Test
    void test_dup2_x2_form4() throws Exception {
        // dup2_x2 form4: [long, long] => [long, long, long]
        test("dup2_x2_form4", 2, 2, PrimitiveType.LONG, PrimitiveType.LONG);
    }
}
