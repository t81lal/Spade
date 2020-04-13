package com.krakenrs.spade.ir.type;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MethodTypeTest {
    static Stream<Arguments> strings() {
        return Stream.of(
                Arguments.of("(II)V",
                        new MethodType(List.of(PrimitiveType.INT, PrimitiveType.INT), PrimitiveType.VOID)),
                Arguments.of("(II)I",
                        new MethodType(List.of(PrimitiveType.INT, PrimitiveType.INT), PrimitiveType.INT)),
                Arguments.of("([Ljava/lang/String;)V", new MethodType(
                        List.of(new ArrayType(new UnresolvedClassType("java/lang/String").asValueType())),
                        PrimitiveType.VOID))
        );
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("strings")
    void testToString(String expected, MethodType methodType) {
        assertEquals(expected, methodType.toString());
    }
}
