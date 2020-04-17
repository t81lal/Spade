package com.krakenrs.spade.ir.type;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TypeManagerTest {
    final Supplier<MockTypeManager> typeManagers = () -> {
        MockTypeManager manager = new MockTypeManager();
        manager.addClass("java/lang/Object", new ResolvedClassType("java/lang/Object", null, Set.of()));
        manager.addClass("java/io/Serializable", new ResolvedClassType("java/io/Serializable", null, Set.of()));
        manager.addClass("java/lang/Comparable", new ResolvedClassType("java/lang/Comparable", null, Set.of()));
        manager.addClass("java/lang/Number", new ResolvedClassType("java/lang/Number", manager.findClassType("java/lang/Object"),
                Set.of(manager.findClassType("java/io/Serializable"))));
        manager.addClass("java/lang/Integer", new ResolvedClassType("java/lang/Integer", manager.findClassType("java/lang/Number"),
                Set.of(manager.findClassType("java/lang/Comparable"))));
        manager.addClass("java/lang/Long", new ResolvedClassType("java/lang/Long", manager.findClassType("java/lang/Number"),
                Set.of(manager.findClassType("java/lang/Comparable"))));
        manager.addClass("foo/CoolInteger",
                new ResolvedClassType("foo/CoolInteger", manager.findClassType("java/lang/Integer"), Set.of()));
        return manager;
    };

    final Supplier<MockTypeManager> resolvingTypeManagers = ResolvingMockTypeManager::new;

    static Stream<Arguments> methodTypes() {
        return Stream.of(
                Arguments.of(new MethodType(List.of(), PrimitiveType.VOID), "()V"),
                Arguments.of(new MethodType(List.of(), PrimitiveType.INT), "()I"),
                Arguments.of(new MethodType(List.of(PrimitiveType.INT, PrimitiveType.INT),
                        PrimitiveType.INT), "(II)I"),
                Arguments.of(new MethodType(
                        List.of(PrimitiveType.INT, new ObjectType(new UnresolvedClassType("foo/Unresolved"))),
                        PrimitiveType.INT), "(ILfoo/Unresolved;)I")
        );
    }

    static Stream<Arguments> valueTypes() {
        return Stream.of(
                Arguments.of(PrimitiveType.BYTE, "B"),
                Arguments.of(PrimitiveType.CHAR, "C"),
                Arguments.of(PrimitiveType.DOUBLE, "D"),
                Arguments.of(PrimitiveType.FLOAT, "F"),
                Arguments.of(PrimitiveType.INT, "I"),
                Arguments.of(PrimitiveType.LONG, "J"),
                Arguments.of(PrimitiveType.SHORT, "S"),
                Arguments.of(PrimitiveType.BOOLEAN, "Z"),
                Arguments.of(new ObjectType(new UnresolvedClassType("foo/Unresolved")), "Lfoo/Unresolved;"),
                Arguments.of(new ArrayType(PrimitiveType.INT), "[I"),
                Arguments.of(new ArrayType(new ArrayType(PrimitiveType.INT)), "[[I")
        );
    }

    static Stream<Arguments> methodTypesInvalid() {
        return Stream.of(
                Arguments.of(""),
                Arguments.of("()"),
                Arguments.of("(II)"),
                Arguments.of(")V"),
                Arguments.of("();V"));
    }

    static Stream<Arguments> ancestors() {
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
        final var tm = typeManagers.get();
        final MethodType actual = tm.asMethodType(descriptor);
        assertEquals(expected, actual);
    }

    @ParameterizedTest(name = "[{index}] \"{0}\"")
    @MethodSource("methodTypesInvalid")
    void testAsMethodTypeInvalid(String descriptor) {
        final var tm = typeManagers.get();
        assertThrows(TypeParsingException.class, () -> tm.asMethodType(descriptor));
    }

    @ParameterizedTest(name = "[{index}] {1}")
    @MethodSource("valueTypes")
    void testAsValueType(ValueType expected, String descriptor) {
        final var tm = typeManagers.get();
        final ValueType actual = tm.asValueType(descriptor);
        assertEquals(expected, actual);
    }

    @ParameterizedTest(name = "[{index}] {0}, {1}")
    @MethodSource("ancestors")
    void testLca(String lhsDesc, String rhsDesc, String expectedDesc) {
        final var typeManager = typeManagers.get();

        // TODO: Use org.junit.jupiter.params.converter.ArgumentConverter.
        final var lhs = typeManager.asClassType(lhsDesc);
        final var rhs = typeManager.asClassType(rhsDesc);
        final var expected = typeManager.asClassType(expectedDesc);

        final var actual = typeManager.lca(lhs, rhs);
        assertEquals(expected, actual);
    }

    @Test
    void testLcaInvalid() {
        final var typeManager = typeManagers.get();
        assertThrows(NullPointerException.class, () -> typeManager.lca(null, null));
    }

    @Test
    void testAsClassTypeInvalid() {
        var types = typeManagers.get();
        assertThrows(NullPointerException.class, () -> types.asClassType((String) null));
        assertThrows(NullPointerException.class, () -> types.asClassType((Class<?>) null));
    }

    @Test
    void testAsArrayTypeArguments() {
        var types = typeManagers.get();
        assertThrows(NullPointerException.class, () -> types.asArrayType((Class<?>) null, 1));
        assertThrows(IllegalArgumentException.class, () -> types.asArrayType(Object.class, 0));
        assertThrows(IllegalArgumentException.class, () -> types.asArrayType(Object.class, -1));

        var objectArrayType = types.asArrayType(Object.class, 1);
        assertThrows(NullPointerException.class, () -> types.asArrayType((ValueType) null, 1));
        assertThrows(IllegalArgumentException.class, () -> types.asArrayType(objectArrayType, 1));
        assertThrows(IllegalArgumentException.class, () -> types.asArrayType(objectArrayType.elementType(), 0));
    }

    @Test
    void testAsValueTypeArguments() {
        var types = typeManagers.get();
        assertThrows(NullPointerException.class, () -> types.asValueType(null));
        assertThrows(IllegalArgumentException.class, () -> types.asValueType(""));

        //No trailing ;
        assertThrows(TypeParsingException.class, () -> types.asValueType("Ljava/lang/Object"));
        // No type
        assertThrows(TypeParsingException.class, () -> types.asValueType("L;"));
        assertThrows(TypeParsingException.class, () -> types.asValueType("["));
        // Invalid primitive type
        assertThrows(TypeParsingException.class, () -> types.asValueType("[K"));
        assertThrows(TypeParsingException.class, () -> types.asValueType("K"));
    }
}
