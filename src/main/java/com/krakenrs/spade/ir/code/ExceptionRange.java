package com.krakenrs.spade.ir.code;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.krakenrs.spade.ir.type.ClassType;

public class ExceptionRange {

    private Set<CodeBlock> range;
    private CodeBlock handler;
    private Set<ClassType> catchTypes;

    public ExceptionRange() {
        range = new HashSet<>();
        catchTypes = new HashSet<>();
    }

    public void addProtectedBlock(CodeBlock block) {
        range.add(block);
    }

    public void removeProtectedBlock(CodeBlock block) {
        range.remove(block);
    }

    public Set<CodeBlock> range() {
        return Collections.unmodifiableSet(range);
    }

    public void setHandler(CodeBlock handler) {
        this.handler = handler;
    }

    public CodeBlock handler() {
        return handler;
    }

    public void addCatchType(ClassType type) {
        catchTypes.add(type);
    }

    public void removeCatchType(ClassType type) {
        catchTypes.remove(type);
    }
    
    public Set<ClassType> catchTypes() {
        return Collections.unmodifiableSet(catchTypes);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ExceptionRange) {
            ExceptionRange other = (ExceptionRange) o;
            return Objects.equals(range, other.range) && Objects.equals(handler, other.handler)
                    && Objects.equals(catchTypes, other.catchTypes);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(range, handler, catchTypes);
    }
}
