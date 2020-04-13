package com.krakenrs.spade.ir.type;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class TypeManagerTest {
    final Supplier<MockTypeManager> typeManagers = MockTypeManager::new;
    final Supplier<MockTypeManager> resolvingTypeManagers = ResolvingMockTypeManager::new;

    static Stream<Arguments> methodTypes() {
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
    void testParseValueTypePrimitive() throws TypeParsingException {
        var types = typeManagers.get();
        assertEquals(PrimitiveType.BYTE, types.asValueType("B"));
        assertEquals(PrimitiveType.CHAR, types.asValueType("C"));
        assertEquals(PrimitiveType.DOUBLE, types.asValueType("D"));
        assertEquals(PrimitiveType.FLOAT, types.asValueType("F"));
        assertEquals(PrimitiveType.INT, types.asValueType("I"));
        assertEquals(PrimitiveType.LONG, types.asValueType("J"));
        assertEquals(PrimitiveType.SHORT, types.asValueType("S"));
        assertEquals(PrimitiveType.BOOLEAN, types.asValueType("Z"));
    }

    @Test
    void testParseValueTypeArray() throws TypeParsingException {
        var types = typeManagers.get();
        assertEquals(new ArrayType(PrimitiveType.INT), types.asArrayType(PrimitiveType.INT, 1));
        assertEquals(new ArrayType(new ArrayType(PrimitiveType.INT)), types.asArrayType(PrimitiveType.INT, 2));
    }

    @Test
    void testParseValueTypeObject() throws TypeParsingException {
        var types = typeManagers.get();
        // TODO:
        assertEquals(new ObjectType(new UnresolvedClassType("java/lang/String")),
                types.asValueType("Ljava/lang/String;"));
        assertEquals(new ObjectType(new UnresolvedClassType("ash/ir/type/Type")),
                types.asValueType("Lash/ir/type/Type;"));
    }

    @Test
    void testParseMethod() throws TypeParsingException {
        var types = typeManagers.get();
        assertEquals(new MethodType(List.of(), PrimitiveType.VOID), types.asMethodType("()V"));
        assertEquals(new MethodType(List.of(), PrimitiveType.INT), types.asMethodType("()I"));
        assertEquals(new MethodType(List.of(PrimitiveType.INT), PrimitiveType.INT), types.asMethodType("(I)I"));
        assertEquals(new MethodType(List.of(PrimitiveType.INT, PrimitiveType.INT), PrimitiveType.INT),
                types.asMethodType("(II)I"));
        assertEquals(new MethodType(List.of(), new ObjectType(new UnresolvedClassType("java/lang/String"))),
                types.asMethodType("()Ljava/lang/String;"));
        assertEquals(new MethodType(List.of(new ObjectType(new UnresolvedClassType("java/lang/String"))),
                PrimitiveType.VOID), types.asMethodType("(Ljava/lang/String;)V"));
        assertEquals(
                new MethodType(List.of(new ObjectType(new UnresolvedClassType("java/lang/String")), PrimitiveType.INT),
                        PrimitiveType.VOID),
                types.asMethodType("(Ljava/lang/String;I)V"));
        assertEquals(
                new MethodType(List.of(PrimitiveType.INT, new ObjectType(new UnresolvedClassType("java/lang/String"))),
                        PrimitiveType.VOID),
                types.asMethodType("(ILjava/lang/String;)V"));
        assertEquals(
                new MethodType(List.of(new ObjectType(new UnresolvedClassType("java/lang/String")),
                        new ObjectType(new UnresolvedClassType("java/lang/String"))), PrimitiveType.VOID),
                types.asMethodType("(Ljava/lang/String;Ljava/lang/String;)V"));
        assertEquals(new MethodType(List.of(new ArrayType(new ArrayType(PrimitiveType.INT))), PrimitiveType.INT),
                types.asMethodType("([[I)I"));
        assertEquals(new MethodType(List.of(new ArrayType(PrimitiveType.INT)), PrimitiveType.INT),
                types.asMethodType("([I)I"));
        assertEquals(
                new MethodType(List.of(new ArrayType(PrimitiveType.INT), PrimitiveType.BOOLEAN), PrimitiveType.INT),
                types.asMethodType("([IZ)I"));
        assertEquals(
                new MethodType(List.of(PrimitiveType.INT, PrimitiveType.INT, PrimitiveType.INT,
                        new ArrayType(new ArrayType(new ArrayType(PrimitiveType.INT)))), PrimitiveType.VOID),
                types.asMethodType("(III[[[I)V"));
    }

    @Test
    void testAsClassTypeNPE() {
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

    @Test
    void testLCANPE() {
        var types = typeManagers.get();
        assertThrows(NullPointerException.class, () -> types.lca(null, null));
    }

    @Test
    void testLCA() {
        var types = resolvingTypeManagers.get();

        // Equal
        ClassType stringClassType = types.asClassType(String.class);
        ClassType objectClassType = types.asClassType(Object.class);
        assertEquals(stringClassType, types.lca(stringClassType, stringClassType));
        assertEquals(objectClassType, types.lca(objectClassType, objectClassType));
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

        protected ClassType findClassType0(String name) {
            return new UnresolvedClassType(name);
        }

        @Override
        public ClassType findClassType(String name) {
            // Can't use computeIfAbsent as classes might change while current lookup is being performed
            if (classes.containsKey(name)) {
                return classes.get(name);
            } else {
                ClassType ct = findClassType0(name);
                classes.put(name, Objects.requireNonNull(ct));
                return ct;
            }
        }
    }

    static class ResolvingMockTypeManager extends MockTypeManager {
        @Override
        protected ClassType findClassType0(String name) {
            Objects.requireNonNull(name);

            String klassName = name.replace(".", "/");
            String clazzName = name.replace("/", ".");
            try {
                Class<?> clazz = Class.forName(clazzName);
                Class<?> superClazz = clazz.getSuperclass();
                ClassType superClassType = null;
                if (superClazz != null) {
                    // Object and interfaces
                    superClassType = asClassType(superClazz);
                }
                Set<ClassType> superInterfacesTypes = Arrays.asList(clazz.getInterfaces()).stream()
                        .map(this::asClassType).collect(Collectors.toSet());
                return new ResolvedClassType(klassName, superClassType, superInterfacesTypes);
            } catch (ClassNotFoundException e) {
                return new UnresolvedClassType(klassName);
            }
        }
    }
}
