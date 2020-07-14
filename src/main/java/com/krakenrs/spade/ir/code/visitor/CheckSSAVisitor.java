package com.krakenrs.spade.ir.code.visitor;

import com.krakenrs.spade.ir.code.expr.value.LoadLocalExpr;
import com.krakenrs.spade.ir.code.expr.value.ValueExpr;
import com.krakenrs.spade.ir.code.stmt.AssignCatchStmt;
import com.krakenrs.spade.ir.code.stmt.AssignLocalStmt;
import com.krakenrs.spade.ir.code.stmt.AssignParamStmt;
import com.krakenrs.spade.ir.code.stmt.AssignPhiStmt;
import com.krakenrs.spade.ir.value.Local;

public class CheckSSAVisitor extends AbstractValueVisitor<Boolean> {
	private boolean isSSA;

	public CheckSSAVisitor() {
		isSSA = true;
	}

	private void mergeLocal(Local local) {
		if (!local.isVersioned()) {
			isSSA = false;
		}
	}

	@Override
	public void visitAssignLocalStmt(AssignLocalStmt s) {
		super.visitAssignLocalStmt(s);
		mergeLocal(s.var());
	}

	@Override
	public void visitAssignParamStmt(AssignParamStmt s) {
		super.visitAssignParamStmt(s);
		mergeLocal(s.var());
	}

	@Override
	public void visitAssignCatchStmt(AssignCatchStmt s) {
		super.visitAssignCatchStmt(s);
		mergeLocal(s.var());
	}

	@Override
	public void visitValueExpr(ValueExpr<?> e) {
		super.visitValueExpr(e);
		if (e instanceof LoadLocalExpr) {
			mergeLocal((Local) e.value());
		}
	}

	@Override
	public void visitAssignPhiStmt(AssignPhiStmt s) {
		super.visitAssignPhiStmt(s);
		s.arguments().values().forEach(this::mergeLocal);
		mergeLocal(s.var());
	}

	@Override
	public Boolean get() {
		return isSSA;
	}
}
