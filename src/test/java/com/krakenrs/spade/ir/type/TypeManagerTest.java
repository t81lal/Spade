package com.krakenrs.spade.ir.type;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TypeManagerTest {
    final Supplier<MockTypeManager> typeManagerSupplier = MockTypeManager::new;

    private static Stream<Arguments> methodTypes() {
        return Stream.of(Arguments.of(new MethodType(List.of(), PrimitiveType.VOID), "()V"),
                Arguments.of(new MethodType(List.of(), PrimitiveType.INT), "()I"), Arguments
                        .of(new MethodType(List.of(PrimitiveType.INT, PrimitiveType.INT), PrimitiveType.INT), "(II)I"));
    }

    @ParameterizedTest(name = "[{index}] {1}") @MethodSource("methodTypes") void testAsMethodType(MethodType expected,
            String descriptor) {
        final var tm = typeManagerSupplier.get();
        final MethodType actual = tm.asMethodType(descriptor);
        assertEquals(expected, actual);
    }

    static class MockTypeManager extends TypeManager {
        private final Map<String, ClassType> classes = new HashMap<>();

        public MockTypeManager() {
            addClass("java/lang/Object", new ResolvedClassType("java/lang/Object", null, Set.of()));
        }

        public void addClass(String className, ClassType classType) {
            classes.put(className, classType);
        }

        @Override protected ClassType findClassType(String name) {
            return classes.getOrDefault(name, new UnresolvedClassType(name));
        }
    }
}
