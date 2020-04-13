package com.krakenrs.spade.ir.type;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class ArrayTypeTest {
    static Stream<Arguments> strings() {
        return Stream.of(
                Arguments.of("[I", new ArrayType(PrimitiveType.INT)),
                Arguments.of("[[I", new ArrayType(new ArrayType(PrimitiveType.INT))),
                Arguments.of("[Ljava/lang/Object;",
                        new ArrayType(new UnresolvedClassType("java/lang/Object").asValueType()))
        );
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("strings")
    void testToString(String expected, ArrayType arrayType) {
        assertEquals(expected, arrayType.toString());
    }
}
