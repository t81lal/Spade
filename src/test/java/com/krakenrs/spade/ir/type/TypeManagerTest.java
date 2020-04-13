package com.krakenrs.spade.ir.type;

import org.junit.jupiter.api.Test;
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
import static org.junit.jupiter.api.Assertions.assertThrows;

class TypeManagerTest {
    final Supplier<MockTypeManager> typeManagerSupplier = MockTypeManager::new;

    private static Stream<Arguments> methodTypes() {
        return Stream.of(
                Arguments.of(new MethodType(List.of(), PrimitiveType.VOID), "()V"),
                Arguments.of(new MethodType(List.of(), PrimitiveType.INT), "()I"),
                Arguments.of(new MethodType(List.of(PrimitiveType.INT, PrimitiveType.INT),
                        PrimitiveType.INT), "(II)I"));
    }

    private static Stream<Arguments> methodTypesInvalid() {
        return Stream.of(
                Arguments.of(""),
                Arguments.of("()"),
                Arguments.of("(II)"),
                Arguments.of(")V"),
                Arguments.of("();V"));
    }

    private static Stream<Arguments> ancestors() {
        return Stream.of(
                Arguments.of("java.lang.Object", "java.lang.Object", "java.lang.Object"),
                Arguments.of("java.lang.Comparable", "java.io.Serializable", "java.lang.Object"),
                Arguments.of("java.lang.Integer", "java.lang.Number", "java.lang.Number"),
                Arguments.of("java.lang.Number", "java.lang.Integer", "java.lang.Number"),
                Arguments.of("java.lang.Integer", "java.lang.Long", "java.lang.Comparable"),
                Arguments.of("java.lang.Long", "java.lang.Integer", "java.lang.Comparable"),
                Arguments.of("foo.Unresolved", "java.lang.Integer", "java.lang.Object"),
                Arguments.of("java.lang.Integer", "foo.Unresolved", "java.lang.Object"),
                Arguments.of("foo.CoolInteger", "java.lang.Long", "java.lang.Number"),
                Arguments.of("foo.CoolInteger", "java.lang.Integer", "java.lang.Integer"));
    }

    @ParameterizedTest(name = "[{index}] {1}")
    @MethodSource("methodTypes")
    void testAsMethodType(MethodType expected, String descriptor) {
        final var tm = typeManagerSupplier.get();
        final MethodType actual = tm.asMethodType(descriptor);
        assertEquals(expected, actual);
    }

    @ParameterizedTest(name = "[{index}] \"{0}\"")
    @MethodSource("methodTypesInvalid")
    void testAsMethodTypeInvalid(String descriptor) {
        final var tm = typeManagerSupplier.get();
        assertThrows(TypeParsingException.class, () -> tm.asMethodType(descriptor));
    }

    @ParameterizedTest(name = "[{index}] {0}, {1}")
    @MethodSource("ancestors")
    void testLca(String lhsDesc, String rhsDesc, String expectedDesc) {
        final var typeManager = typeManagerSupplier.get();

        // TODO: Use org.junit.jupiter.params.converter.ArgumentConverter.
        final var lhs = typeManager.asClassType(lhsDesc);
        final var rhs = typeManager.asClassType(rhsDesc);
        final var expected = typeManager.asClassType(expectedDesc);

        final var actual = typeManager.lca(lhs, rhs);
        assertEquals(expected, actual);
    }

    @Test
    void testLcaInvalid() {
        final var typeManager = typeManagerSupplier.get();
        assertThrows(NullPointerException.class, () -> typeManager.lca(null, null));
    }

    static class MockTypeManager extends TypeManager {
        private final Map<String, ClassType> classes = new HashMap<>();

        public MockTypeManager() {
            addClass("java/lang/Object", new ResolvedClassType("java/lang/Object", null, Set.of()));
            addClass("java/io/Serializable", new ResolvedClassType("java/io/Serializable", null, Set.of()));
            addClass("java/lang/Comparable", new ResolvedClassType("java/lang/Comparable", null, Set.of()));
            addClass("java/lang/Number", new ResolvedClassType("java/lang/Number", findClassType("java/lang/Object"),
                    Set.of(findClassType("java/io/Serializable"))));
            addClass("java/lang/Integer", new ResolvedClassType("java/lang/Integer", findClassType("java/lang/Number"),
                    Set.of(findClassType("java/lang/Comparable"))));
            addClass("java/lang/Long", new ResolvedClassType("java/lang/Long", findClassType("java/lang/Number"),
                    Set.of(findClassType("java/lang/Comparable"))));

            addClass("foo/CoolInteger",
                    new ResolvedClassType("foo/CoolInteger", findClassType("java/lang/Integer"), Set.of()));
        }

        public void addClass(String className, ClassType classType) {
            classes.put(className, classType);
        }

        @Override
        protected ClassType findClassType(String name) {
            return classes.getOrDefault(name, new UnresolvedClassType(name));
        }
    }
}
