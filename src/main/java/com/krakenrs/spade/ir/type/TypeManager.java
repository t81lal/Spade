package com.krakenrs.spade.ir.type;

import java.util.*;

public abstract class TypeManager {
    private final Map<String, ClassType> classTypes;
    private final Map<String, ValueType> valueTypes;
    private final Map<String, MethodType> methodTypes;

    public TypeManager() {
        classTypes = new HashMap<>();
        valueTypes = new HashMap<>();
        methodTypes = new HashMap<>();
    }

    protected abstract ClassType findClassType(String name);

    public ClassType asClassType(String name) {
        if (name == null) {
            throw new NullPointerException();
        } else {
            name = name.replace(".", "/");
            if (classTypes.containsKey(name)) {
                return classTypes.get(name);
            } else {
                ClassType ct = findClassType(name);
                ObjectType vt = ct.asValueType();
                classTypes.put(name, ct);
                valueTypes.put(vt.toString(), vt);
                return ct;
            }
        }
    }
    
    public ObjectType asObjectType(Class<?> clazz) {
        return asClassType(clazz).asValueType();
    }

    public ClassType asClassType(Class<?> clazz) {
        if (clazz == null) {
            throw new NullPointerException();
        } else {
            return asClassType(clazz.getName());
        }
    }

    public ArrayType asArrayType(Class<?> clazz, int dims) {
        if (clazz == null) {
            throw new NullPointerException();
        } else if (dims <= 0) {
            throw new IllegalArgumentException("dims should be greater than 0");
        }
        return asArrayType(asClassType(clazz).asValueType(), dims);
    }

    private ArrayType _asArrayType(ValueType baseElementType, int dims) {
        // dims > 0
        if (dims == 1) {
            String key = '[' + baseElementType.toString();
            if (valueTypes.containsKey(key)) {
                return (ArrayType) valueTypes.get(key);
            } else {
                ArrayType at = new ArrayType(baseElementType);
                valueTypes.put(key, at);
                return at;
            }
        } else {
            StringBuilder sb = new StringBuilder();
            int i = dims;
            while (i-- > 0) {
                sb.append('[');
            }
            sb.append(baseElementType);
            String key = sb.toString();

            if (valueTypes.containsKey(key)) {
                return (ArrayType) valueTypes.get(key);
            } else {
                ArrayType at = new ArrayType(_asArrayType(baseElementType, dims - 1));
                valueTypes.put(key, at);
                return at;
            }
        }
    }

    public ArrayType asArrayType(ValueType baseElementType, int dims) {
        if (baseElementType == null) {
            throw new NullPointerException();
        } else if (baseElementType instanceof ArrayType) {
            throw new IllegalArgumentException("Cannot rebind ArrayType");
        } else if (dims <= 0) {
            throw new IllegalArgumentException("Illegal number of dimensions: " + dims);
        } else {
            return _asArrayType(baseElementType, dims);
        }
    }

    public ValueType asValueType(String s) {
        if (s == null) {
            throw new NullPointerException();
        } else if (s.isEmpty()) {
            throw new IllegalArgumentException("Empty string");
        } else {
            if (valueTypes.containsKey(s)) {
                return valueTypes.get(s);
            } else {
                char[] cs = s.toCharArray();
                int len = cs.length, i = 0;
                while (i < len && cs[i] == '[')
                    i++;
                if (i >= len) {
                    throw new TypeParsingException("No value type:", cs, i, len - 1, true);
                }

                if (cs[i] == 'L') {
                    if (cs[len - 1] == ';') {
                        return _asValueType(cs, 0, len);
                    } else {
                        throw new TypeParsingException("Expecting ';', got ", cs, 0, i, true);
                    }
                } else if (i == len - 1) {
                    ValueType vt = _asValueType(cs, 0, len);
                    if (!(vt instanceof PrimitiveType) && !(vt instanceof ArrayType
                            && ((ArrayType) vt).componentType() instanceof PrimitiveType)) {
                        throw new TypeParsingException("Expecting primitive, got " + vt, cs, 0, i, false);
                    } else {
                        return vt;
                    }
                } else {
                    throw new TypeParsingException("No ref or primitive type at ", cs, 0, i, true);
                }
            }
        }
    }

    // mega private, don't let users parse their own value types
    private ValueType _asValueType(char[] cs, int offset, int csLength) {
        if (offset >= cs.length) {
            throw new TypeParsingException("Out of range");
        }

        int dims = 0, i = offset;
        while (cs[i] == '[' && i++ <= (csLength + offset - 1)) {
            dims++;
        }
        final int len = csLength - (i - offset);

        ValueType vt = null;
        if (len == 0) {
            throw new TypeParsingException("No value type:", cs, i, csLength - 1, true);
        } else if (len == 1) {
            String d = new String(cs, i, len);
            if (PrimitiveType.DESCRIPTORS.containsKey(d)) {
                vt = PrimitiveType.DESCRIPTORS.get(d);
            }
        } else if (len > 2) {
            if (cs[i] == 'L' && cs[i + len - 1] == ';') {
                vt = asClassType(new String(cs, i + 1, len - 2)).asValueType();
            }
        }

        if (vt != null) {
            if (dims != 0) {
                return asArrayType(vt, dims);
            } else {
                return vt;
            }
        } else {
            throw new TypeParsingException("No type found ", cs, offset, csLength - 1, false);
        }
    }

    public MethodType asMethodType(String descriptor) {
        if (methodTypes.containsKey(descriptor)) {
            return methodTypes.get(descriptor);
        } else {
            MethodType mt = parseMethodType(descriptor.toCharArray(), 0, descriptor.length() - 1);
            methodTypes.put(descriptor, mt);
            return mt;
        }
    }

    private MethodType parseMethodType(char[] cs, int offset, int end) {
        if (offset >= cs.length) {
            throw new TypeParsingException("Out of range");
        }

        if (cs[offset] == '(') {
            List<ValueType> paramTypes = new ArrayList<>();
            int i = offset + 1;
            for (; i <= end && cs[i] != ')'; i++) {
                char c = cs[i];
                int endPos = i;
                boolean isObj = c == 'L';
                /* determine the end pos for the current value type */
                if (c == '[') {
                    /* find the start of the element type */
                    for (; endPos <= end && (cs[endPos] == '[' && cs[endPos] != ')'); endPos++)
                        ;
                    /* went too far */
                    if (endPos == end || cs[endPos] == ')') {
                        throw new TypeParsingException("No element for array at", cs, i, end, true);
                    }
                    isObj = cs[endPos] == 'L';
                }
                if (isObj) {
                    /* find the end of the object type */
                    for (; endPos <= end && (cs[endPos] != ';' && cs[endPos] != ')'); endPos++)
                        ;
                    /* went too far */
                    if (endPos == end || cs[endPos] == ')') {
                        throw new TypeParsingException("Unterminated object type at ", cs, i, end, true);
                    }
                }
                /* endPos-i+1 is 1 in the case of a primitive */
                paramTypes.add(_asValueType(cs, i, endPos - i + 1));
                i = endPos;
            }
            if (i > end) {
                throw new TypeParsingException("No return type", cs, offset, end, false);
            } else if (cs[i] != ')') {
                throw new TypeParsingException("Expecting ')', got", cs, offset, i, true);
            }
            ValueType rt;
            if (end - i == 1 && cs[end] == 'V') {
                rt = PrimitiveType.VOID;
            } else {
                rt = _asValueType(cs, i + 1, end - i);
            }
            return new MethodType(paramTypes, rt);
        } else {
            throw new TypeParsingException("Expecting '(', got " + cs[offset]);
        }
    }

    public ClassType lca(Set<ClassType> types) {
        // System.out.printf("lca(%s)\n", types);
        if (types.isEmpty()) {
            return null;
        } else {
            int s = types.size();
            if (s == 1) {
                return types.iterator().next();
            } else {
                Iterator<ClassType> it = types.iterator();
                ClassType t3 = lca(it.next(), it.next());
                if (s == 2) {
                    return t3;
                } else {
                    while (it.hasNext()) {
                        t3 = lca(t3, it.next());
                    }
                    return t3;
                }
            }
        }
    }

    public ClassType lca(ClassType t1, ClassType t2) {
        // System.out.printf("lca(%s, %s)\n", t1, t2);
        if (t1 == null || t2 == null) {
            throw new NullPointerException();
        }
        if (t1.equals(t2)) {
            return t1;
        } else if (t1 instanceof UnresolvedClassType || t2 instanceof UnresolvedClassType) {
            return asClassType(Object.class);
        } else {
            ClassType objectCT = asClassType(Object.class);
            if (t1.getSuperClassType() == null || t2.getSuperClassType() == null) {
                return objectCT;
            } else if (t1.getSuperClassType().equals(t2)) {
                return t2;
            } else if (t2.getSuperClassType().equals(t1)) {
                return t1;
            } else {
                Set<ClassType> if1 = new HashSet<>(t1.getSuperInterfaceTypes()),
                        if2 = new HashSet<>(t2.getSuperInterfaceTypes());
                if (if1.contains(t2)) {
                    return t2;
                } else if (if2.contains(t1)) {
                    return t1;
                } else {
                    // intersection
                    if1.retainAll(if2);
                    ClassType t3 = lca(if1);
                    if (t3 != null) {
                        return t3;
                    } else {
                        return lca(t1.getSuperClassType(), t2.getSuperClassType());
                    }
                }
            }
        }
    }
}
