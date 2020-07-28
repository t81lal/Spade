package com.krakenrs.spade.ir.type.infer;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.objectweb.asm.ClassReader;

import com.google.inject.Inject;
import com.krakenrs.spade.app.asm.ASMTypeManager;
import com.krakenrs.spade.app.asm.KlassScene;
import com.krakenrs.spade.ir.type.ArrayType;
import com.krakenrs.spade.ir.type.ClassType;
import com.krakenrs.spade.ir.type.ObjectType;
import com.krakenrs.spade.ir.type.PrimitiveType;
import com.krakenrs.spade.ir.type.ResolvedClassType;
import com.krakenrs.spade.ir.type.TypeHierarchy;
import com.krakenrs.spade.ir.type.TypeManager;
import com.krakenrs.spade.ir.type.TypeTree;
import com.krakenrs.spade.ir.type.ValueType;

import lombok.ToString;

public class BytecodeTypeHierarchy implements TypeHierarchy {

    private final TypeManager typeManager;
    private final TypeTree typeTree;

    @Inject
    public BytecodeTypeHierarchy(TypeManager typeManager, TypeTree typeTree) {
        this.typeManager = typeManager;
        this.typeTree = typeTree;
    }

    private Collection<TreeNode> buildTree(ClassType root) {
        if (!(root instanceof ResolvedClassType)) {
            return Collections.emptyList();
        }
        LinkedList<TreeNode> leafs = new LinkedList<>();
        leafs.add(new TreeNode(null, root));

        final ClassType objCT = typeManager.asClassType(Object.class);

        LinkedList<TreeNode> r = new LinkedList<>();

        while (!leafs.isEmpty()) {
            TreeNode node = leafs.remove();
            ClassType t = node.type;
            if (objCT.equals(t)) {
                r.add(node);
            } else {
                for (ClassType ifCT : t.getSuperInterfaceTypes()) {
                    leafs.add(new TreeNode(node, ifCT));
                }

                if (t instanceof ResolvedClassType && (!t.isInterface() || t.getSuperInterfaceTypes().isEmpty())) {
                    leafs.add(new TreeNode(node, t.getSuperClassType()));
                }
            }
        }

        return r;
    }

    private static Collection<PrimitiveType> PRIMITIVE_INT_TYPES = Set.of(PrimitiveType.BOOLEAN, PrimitiveType.BYTE,
            PrimitiveType.CHAR, PrimitiveType.SHORT, PrimitiveType.INT);

    public static boolean isIntLike(ValueType t) {
        if (t instanceof PrimitiveType) {
            return PRIMITIVE_INT_TYPES.contains(t);
        } else {
            return t instanceof SizedIntType;
        }
    }

    private static Collection<PrimitiveType> REAL_PRIMITIVE__TYPES = Set.of(PrimitiveType.BOOLEAN, PrimitiveType.BYTE,
            PrimitiveType.CHAR, PrimitiveType.SHORT, PrimitiveType.INT, PrimitiveType.FLOAT, PrimitiveType.DOUBLE,
            PrimitiveType.LONG);

    public static boolean isPrimLike(ValueType t) {
        // Not null or void
        return REAL_PRIMITIVE__TYPES.contains(t) || t instanceof SizedIntType;
    }

    private Collection<ValueType> lcas_(ValueType t1, ValueType t2) {
        if (t1.equals(t2)) {
            return Collections.singleton(t1);
        }

        if (t1 instanceof BottomType) {
            return Collections.singleton(t2);
        } else if (t2 instanceof BottomType) {
            return Collections.singleton(t1);
        }

        boolean isT1Int = isIntLike(t1), isT2Int = isIntLike(t2);

        if (isT1Int && isT2Int) {
            return Collections.singleton(PrimitiveType.INT);
        } else if (isT1Int && PrimitiveType.FLOAT.equals(t2)) {
            return Collections.singleton(PrimitiveType.FLOAT);
        } else if (isT2Int && PrimitiveType.FLOAT.equals(t1)) {
            return Collections.singleton(PrimitiveType.FLOAT);
        }

        if (isPrimLike(t1) || isPrimLike(t2)) {
            return Collections.emptySet();
        }

        if (PrimitiveType.NULL.equals(t1)) {
            return Collections.singleton(t2);
        } else if (PrimitiveType.NULL.equals(t2)) {
            return Collections.singleton(t1);
        }
        
        boolean isT1Array = t1 instanceof ArrayType, isT2Array = t2 instanceof ArrayType;

        if (isT1Array && isT2Array) {
            ValueType eT1 = ((ArrayType) t1).elementType(), eT2 = ((ArrayType) t2).elementType();
            Collection<ValueType> elementLCAs;

            if(isPrimLike(eT1) || isPrimLike(eT2)) {
                // Primitive array are not covariant
                elementLCAs = Collections.emptyList();
            } else {
                elementLCAs = lcas_(eT1, eT2);
            }
            
            Set<ValueType> results = new HashSet<>();
            if(elementLCAs.isEmpty()) {
                results.add(typeManager.asObjectType(Object.class));
                results.add(typeManager.asObjectType(Serializable.class));
                results.add(typeManager.asObjectType(Cloneable.class));
            } else {
                for(ValueType t : elementLCAs) {
                    results.add(typeManager.asArrayType(t, 1));
                }
            }
            
            return results;
        } else if(isT1Array || isT2Array) {
            ValueType nonArrayType;
            if(isT1Array) {
                nonArrayType = t2;
            } else {
                nonArrayType = t1;
            }

            Set<ValueType> results = new HashSet<>();
            if(!nonArrayType.equals(typeManager.asObjectType(Object.class))) {
                ObjectType serializableT = typeManager.asObjectType(Serializable.class);
                if(doIsAncestorOf(serializableT, nonArrayType)) {
                    results.add(serializableT);
                }
                
                ObjectType cloneableT = typeManager.asObjectType(Cloneable.class);
                if(doIsAncestorOf(cloneableT, nonArrayType)) {
                    results.add(cloneableT);
                }
            }
            
            if(results.isEmpty()) {
                results.add(typeManager.asObjectType(Object.class));
            }
            
            return results;
        } else {
            if(!(t1 instanceof ObjectType && t2 instanceof ObjectType)) {
                throw new UnsupportedOperationException(t1.getClass() + " vs " + t2.getClass());
            }
            
            Collection<TreeNode> tree1 = buildTree(((ObjectType) t1).getClassType());
            Collection<TreeNode> tree2 = buildTree(((ObjectType) t2).getClassType());
            Set<ValueType> results = new HashSet<>();
            
            for(TreeNode n1 : tree1) {
                for(TreeNode n2 : tree2) {
                    ObjectType possibleLeast = lcaTree(n1, n2).asValueType();
                    boolean isRealLeast = true;
                    
                    Iterator<ValueType> resultIter = results.iterator();
                    while(resultIter.hasNext()) {
                        ValueType curResult = resultIter.next();
                        
                        if(doIsAncestorOf(possibleLeast, curResult)) {
                            isRealLeast = false;
                            break;
                        }
                        
                        if(doIsAncestorOf(curResult, possibleLeast)) {
                            resultIter.remove();
                        }
                    }
                    
                    if(isRealLeast) {
                        results.add(possibleLeast);
                    }
                }
            }
            
            if(results.isEmpty()) {
                throw new IllegalStateException();
            }
            
            return results;
        }
    }

    private ClassType lcaTree(TreeNode n1, TreeNode n2) {
        ClassType t = null;
        while (n1 != null && n2 != null && n1.type.equals(n2.type)) {
            t = n1.type;
            n1 = n1.next;
            n2 = n2.next;
        }
        return t;
    }

    @Override
    public Collection<ValueType> getLeastCommonAncestors(ValueType t1, ValueType t2) {
        return lcas_(t1, t2);
    }

    public boolean doIsAncestorOf(ValueType possibleAncestor, ValueType node) {
        if(possibleAncestor.equals(node)) {
            return true;
        } else if(node instanceof BottomType) {
            return true;
        } else if(possibleAncestor instanceof BottomType) {
            return false;
        } else if(isIntLike(possibleAncestor) && isIntLike(node)) {
            return true;
        } else if(isPrimLike(possibleAncestor) || isPrimLike(node)) {
            return false;
        } else if(node.equals(PrimitiveType.NULL)) {
            return true;
        } else {
            return typeTree.canStoreType(node, possibleAncestor);
        }
    }

    @Override
    public boolean isAncestorOf(ValueType possibleAncestor, ValueType node) {
        return doIsAncestorOf(possibleAncestor, node);
    }

    @ToString
    static class TreeNode {
        final TreeNode next;
        final ClassType type;

        public TreeNode(TreeNode next, ClassType type) {
            this.next = next;
            this.type = type;
        }
    }
    
    public static void main(String[] args) throws IOException {
        KlassScene scene = new KlassScene();
        scene.addJarSource(new File("ext/lib/rt1.8.0_221.jar"), ClassReader.SKIP_CODE);

        ASMTypeManager tm = new ASMTypeManager(scene);
        TypeTree tree = new TypeTree(tm);
        scene.getKlasses().forEach(x -> tree.addClass(tm.asClassType(x.name)));
        
        BytecodeTypeHierarchy bch = new BytecodeTypeHierarchy(tm, tree);
        
        ArrayType at = tm.asArrayType(PrimitiveType.INT, 2);
        ValueType t2 = tm.asArrayType(tm.asObjectType(Object.class), 2);
        
        Object[][] xs = null;
        int[][] is = null;
        
        
        System.out.println(bch.getLeastCommonAncestors(at, t2));
    }
}
