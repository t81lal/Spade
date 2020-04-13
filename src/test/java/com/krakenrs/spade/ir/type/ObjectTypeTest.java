package com.krakenrs.spade.ir.type;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class ObjectTypeTest {
    static Stream<Arguments> strings() {
        return Stream.of(
                Arguments.of("Ljava/lang/Object;", new ObjectType(new UnresolvedClassType("java/lang/Object"))),
                Arguments.of("Lfoo/Unresolved;",
                        new ObjectType(new UnresolvedClassType("foo/Unresolved")))
        );
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("strings")
    void testToString(String expected, ObjectType objectType) {
        assertEquals(expected, objectType.toString());
    }
}
