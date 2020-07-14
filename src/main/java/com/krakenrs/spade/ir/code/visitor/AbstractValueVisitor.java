package com.krakenrs.spade.ir.code.visitor;

public abstract class AbstractValueVisitor<T> extends AbstractCodeVisitor {
	public abstract T get();
}
