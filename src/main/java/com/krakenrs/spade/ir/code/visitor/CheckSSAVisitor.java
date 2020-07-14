package com.krakenrs.spade.ir.code.visitor;

import com.krakenrs.spade.ir.code.expr.value.LoadLocalExpr;
import com.krakenrs.spade.ir.code.expr.value.ValueExpr;
import com.krakenrs.spade.ir.value.Local;

public class CheckSSAVisitor extends AbstractValueVisitor<Boolean> {
	private boolean isSSA;

	public CheckSSAVisitor() {
		isSSA = true;
	}

	@Override
	public void visitValueExpr(ValueExpr<?> e) {
		if (e instanceof LoadLocalExpr) {
			Local local = (Local) e.value();
			if (!local.isVersioned()) {
				isSSA = false;
			}
		}
	}

	@Override
	public Boolean get() {
		return isSSA;
	}
}
