package com.krakenrs.spade.ir.type.infer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.krakenrs.spade.ir.type.ArrayType;
import com.krakenrs.spade.ir.type.PrimitiveType;
import com.krakenrs.spade.ir.type.ResolvingMockTypeManager;
import com.krakenrs.spade.ir.type.TypeManager;
import com.krakenrs.spade.ir.type.TypeTree;
import com.krakenrs.spade.ir.type.UnresolvedClassType;
import com.krakenrs.spade.ir.type.ValueType;
import com.krakenrs.spade.ir.type.infer.TestTypes.A;
import com.krakenrs.spade.ir.type.infer.TestTypes.B;
import com.krakenrs.spade.ir.type.infer.TestTypes.I;
import com.krakenrs.spade.ir.type.infer.TestTypes.J;
import com.krakenrs.spade.ir.type.infer.TestTypes.X;
import com.krakenrs.spade.ir.type.infer.TestTypes.Y;
import com.krakenrs.spade.ir.type.infer.TestTypes.Z;

public class BytecodeTypeHierarchyTest {

    private static TypeManager typeManager;
    private static BytecodeTypeHierarchy hierarchy;

    @BeforeAll
    static void setupClass() {
        typeManager = new ResolvingMockTypeManager();

        TypeTree typeTree = new TypeTree(typeManager);
        typeTree.addClass(typeManager.asClassType(Object.class));
        typeTree.addClass(typeManager.asClassType(Serializable.class));
        typeTree.addClass(typeManager.asClassType(Cloneable.class));

        typeTree.addClass(typeManager.asClassType(I.class));
        typeTree.addClass(typeManager.asClassType(J.class));
        typeTree.addClass(typeManager.asClassType(A.class));
        typeTree.addClass(typeManager.asClassType(B.class));
        typeTree.addClass(typeManager.asClassType(X.class));
        typeTree.addClass(typeManager.asClassType(Y.class));
        typeTree.addClass(typeManager.asClassType(Z.class));

        hierarchy = new BytecodeTypeHierarchy(typeManager, typeTree);
    }

    @AfterAll
    static void tearDownClass() {
        typeManager = null;
    }

    static Stream<Arguments> intLikeArgs() {
        var intLikeTypes = getIntLikeTypes().stream().map(x -> Arguments.of(x, true));
        var nonIntLikeTypes = Stream.of(Arguments.of(PrimitiveType.FLOAT, false),
                Arguments.of(PrimitiveType.DOUBLE, false), Arguments.of(PrimitiveType.LONG, false),

                Arguments.of(PrimitiveType.NULL, false), Arguments.of(PrimitiveType.VOID, false),
                Arguments.of(new UnresolvedClassType("MyClass").asValueType(), false),
                Arguments.of(new ArrayType(PrimitiveType.INT), false));
        return Stream.concat(intLikeTypes, nonIntLikeTypes);
    }

    static Stream<Arguments> primLikeArgs() {
        return Stream.of(Arguments.of(PrimitiveType.BOOLEAN, true), Arguments.of(PrimitiveType.BYTE, true),
                Arguments.of(PrimitiveType.CHAR, true), Arguments.of(PrimitiveType.SHORT, true),
                Arguments.of(PrimitiveType.INT, true), Arguments.of(PrimitiveType.FLOAT, true),
                Arguments.of(PrimitiveType.DOUBLE, true), Arguments.of(PrimitiveType.LONG, true),
                Arguments.of(SizedIntType.BOOL, true), Arguments.of(SizedIntType.BYTE, true),
                Arguments.of(SizedIntType.SHORT, true),

                Arguments.of(PrimitiveType.NULL, false), Arguments.of(PrimitiveType.VOID, false),
                Arguments.of(new UnresolvedClassType("MyClass").asValueType(), false),
                Arguments.of(new ArrayType(PrimitiveType.INT), false));
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("intLikeArgs")
    void testIsIntLike(ValueType type, boolean expectedIsIntLike) {
        assertEquals(expectedIsIntLike, BytecodeTypeHierarchy.isIntLike(type));
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("primLikeArgs")
    void testIsPrimLike(ValueType type, boolean expectedIsPrimLike) {
        assertEquals(expectedIsPrimLike, BytecodeTypeHierarchy.isPrimLike(type));
    }

    static Set<ValueType> getIntLikeTypes() {
        return Set.of(PrimitiveType.BYTE, PrimitiveType.BOOLEAN, PrimitiveType.CHAR, PrimitiveType.SHORT,
                PrimitiveType.INT, SizedIntType.BOOL, SizedIntType.BYTE, SizedIntType.SHORT);
    }

    static Set<ValueType> getOCSArrayTypes(int dims) {
        return getOCSTypes().stream().map(z -> typeManager.asArrayType(z, dims)).collect(Collectors.toSet());
    }

    static Set<ValueType> getOCSTypes() {
        return Set.of(typeManager.asClassType(Object.class).asValueType(),
                typeManager.asClassType(Serializable.class).asValueType(),
                typeManager.asClassType(Cloneable.class).asValueType());
    }

    @Test
    void testIntLikeVsIntLikeLCA() {
        // IntLike vs IntLike => Int
        for (var x : getIntLikeTypes()) {
            for (var y : getIntLikeTypes()) {
                ValueType ex;
                if (x == y) {
                    ex = x;
                } else {
                    ex = PrimitiveType.INT;
                }
                assertEquals(Set.of(ex), hierarchy.getLeastCommonAncestors(x, y));
                assertEquals(Set.of(ex), hierarchy.getLeastCommonAncestors(y, x));
            }
        }
    }

    @Test
    void teetIntLikeVsFloatLCA() {
        // IntLike vs Float => Float
        for (var x : getIntLikeTypes()) {
            assertEquals(Set.of(PrimitiveType.FLOAT), hierarchy.getLeastCommonAncestors(x, PrimitiveType.FLOAT));
            assertEquals(Set.of(PrimitiveType.FLOAT), hierarchy.getLeastCommonAncestors(PrimitiveType.FLOAT, x));
        }
    }

    @Test
    void testDifferentPrimLikeLCA() {
        // Any other Prim vs Any other different Prim => nothing (as equal types always return the same type)
        var otherPrimOrObj = Set.of(PrimitiveType.LONG, PrimitiveType.DOUBLE,
                typeManager.asClassType(Object.class).asValueType());

        var intLikeAndFloat = new ArrayList<>(getIntLikeTypes());
        intLikeAndFloat.add(PrimitiveType.FLOAT);

        // Other prims vs ints/float
        for (var x : intLikeAndFloat) {
            for (var y : otherPrimOrObj) {
                assertEquals(Collections.emptySet(), hierarchy.getLeastCommonAncestors(x, y));
                assertEquals(Collections.emptySet(), hierarchy.getLeastCommonAncestors(y, x));
            }
        }

        // Other prims vs other other prims
        for (var x : otherPrimOrObj) {
            for (var y : otherPrimOrObj) {
                Set<ValueType> ex;
                if (x == y) {
                    ex = Set.of(x);
                } else {
                    ex = Collections.emptySet();
                }
                assertEquals(ex, hierarchy.getLeastCommonAncestors(x, y));
                assertEquals(ex, hierarchy.getLeastCommonAncestors(y, x));
            }
        }
    }

    @Test
    void testNullTypeLCA() {
        // Null vs non int/bottom => other
        assertEquals(Collections.emptySet(), hierarchy.getLeastCommonAncestors(PrimitiveType.INT, PrimitiveType.NULL));
        assertEquals(Collections.emptySet(), hierarchy.getLeastCommonAncestors(PrimitiveType.NULL, PrimitiveType.INT));

        assertEquals(Set.of(PrimitiveType.NULL),
                hierarchy.getLeastCommonAncestors(BottomType.INST, PrimitiveType.NULL));
        assertEquals(Set.of(PrimitiveType.NULL),
                hierarchy.getLeastCommonAncestors(PrimitiveType.NULL, BottomType.INST));

        var objType = typeManager.asClassType(Object.class).asValueType();
        assertEquals(Set.of(objType), hierarchy.getLeastCommonAncestors(objType, PrimitiveType.NULL));
        assertEquals(Set.of(objType), hierarchy.getLeastCommonAncestors(PrimitiveType.NULL, objType));
    }

    @Test
    void testPrimitiveArrayLCA() {
        var primArrays = getIntLikeTypes().stream()
                .flatMap(x -> Stream.of(new ArrayType(x), new ArrayType(new ArrayType(x))))
                .collect(Collectors.toList());

        for (var x : primArrays) {
            for (var y : primArrays) {
                Set<ValueType> ex;
                if (x == y) {
                    // Same one, just the same type
                    ex = Set.of(x);
                } else if (x.dimensions() == y.dimensions() && x.dimensions() > 1) {
                    // e.g. [[Z vs [[B gives Object[], Serializable[], Cloneable[]
                    // but [Z vs [B gives Object, Serializable, Cloneable (thats why > 1)
                    ex = getOCSArrayTypes(x.dimensions() - 1);
                } else {
                    // Different, prim arrays aren't covariant (but can be casted to OCS arrays) so ocs types
                    ex = getOCSTypes();
                }

                assertEquals(ex, hierarchy.getLeastCommonAncestors(x, y));
                assertEquals(ex, hierarchy.getLeastCommonAncestors(y, x));
            }
        }
    }

    @Test
    void testCovariantArrayLCA() {
        var intAA = typeManager.asArrayType(PrimitiveType.INT, 2);
        var objAA = typeManager.asArrayType(typeManager.asObjectType(Object.class), 2);
        assertEquals(getOCSArrayTypes(1), hierarchy.getLeastCommonAncestors(intAA, objAA));
        assertEquals(getOCSArrayTypes(1), hierarchy.getLeastCommonAncestors(objAA, intAA));

        var obj = typeManager.asObjectType(Object.class);
        assertEquals(Set.of(obj), hierarchy.getLeastCommonAncestors(obj, intAA));
        assertEquals(Set.of(obj), hierarchy.getLeastCommonAncestors(intAA, obj));

        var ser = typeManager.asObjectType(Serializable.class);
        assertEquals(Set.of(ser), hierarchy.getLeastCommonAncestors(intAA, ser));

        var cloneable = typeManager.asObjectType(Cloneable.class);
        assertEquals(Set.of(cloneable), hierarchy.getLeastCommonAncestors(intAA, cloneable));
    }

    @Test
    void testClassTypeLCA() {
        assertEquals(Set.of(typeManager.asObjectType(I.class)), hierarchy
                .getLeastCommonAncestors(typeManager.asObjectType(I.class), typeManager.asObjectType(I.class)));
        assertEquals(Set.of(typeManager.asObjectType(Object.class)), hierarchy
                .getLeastCommonAncestors(typeManager.asObjectType(I.class), typeManager.asObjectType(J.class)));

        assertEquals(Set.of(typeManager.asObjectType(I.class)), hierarchy
                .getLeastCommonAncestors(typeManager.asObjectType(A.class), typeManager.asObjectType(I.class)));
        assertEquals(Set.of(typeManager.asObjectType(J.class)), hierarchy
                .getLeastCommonAncestors(typeManager.asObjectType(B.class), typeManager.asObjectType(J.class)));

        assertEquals(Set.of(typeManager.asObjectType(Object.class)), hierarchy
                .getLeastCommonAncestors(typeManager.asObjectType(X.class), typeManager.asObjectType(I.class)));
        assertEquals(Set.of(typeManager.asObjectType(J.class)), hierarchy
                .getLeastCommonAncestors(typeManager.asObjectType(X.class), typeManager.asObjectType(J.class)));

        assertEquals(Set.of(typeManager.asObjectType(X.class)), hierarchy
                .getLeastCommonAncestors(typeManager.asObjectType(Y.class), typeManager.asObjectType(X.class)));
        assertEquals(Set.of(typeManager.asObjectType(I.class)), hierarchy
                .getLeastCommonAncestors(typeManager.asObjectType(Y.class), typeManager.asObjectType(I.class)));
        assertEquals(Set.of(typeManager.asObjectType(J.class)), hierarchy
                .getLeastCommonAncestors(typeManager.asObjectType(Y.class), typeManager.asObjectType(J.class)));

        assertEquals(Set.of(typeManager.asObjectType(X.class)), hierarchy
                .getLeastCommonAncestors(typeManager.asObjectType(Z.class), typeManager.asObjectType(X.class)));
        assertEquals(Set.of(typeManager.asObjectType(Y.class)), hierarchy
                .getLeastCommonAncestors(typeManager.asObjectType(Z.class), typeManager.asObjectType(Y.class)));
        assertEquals(Set.of(typeManager.asObjectType(Z.class)), hierarchy
                .getLeastCommonAncestors(typeManager.asObjectType(Z.class), typeManager.asObjectType(Z.class)));

        assertEquals(Set.of(typeManager.asObjectType(I.class), typeManager.asObjectType(J.class)), hierarchy
                .getLeastCommonAncestors(typeManager.asObjectType(Z.class), typeManager.asObjectType(A.class)));
        assertEquals(Set.of(typeManager.asObjectType(B.class)), hierarchy
                .getLeastCommonAncestors(typeManager.asObjectType(Z.class), typeManager.asObjectType(B.class)));
    }

    @Test
    void testLcas() {
        // Bottom vs other => other
        assertEquals(Set.of(PrimitiveType.INT), hierarchy.getLeastCommonAncestors(PrimitiveType.INT, BottomType.INST));
        assertEquals(Set.of(PrimitiveType.INT), hierarchy.getLeastCommonAncestors(BottomType.INST, PrimitiveType.INT));
    }
}
