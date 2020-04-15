package com.krakenrs.spade.ir.code.stmt;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import com.krakenrs.spade.ir.code.CodeBlock;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.Stmt;
import com.krakenrs.spade.ir.code.expr.value.ValueExpr;

public class JumpSwitchStmt extends Stmt {

    private final ValueExpr<?> expr;
    private final Map<Integer, CodeBlock> cases;
    private final CodeBlock defaultCase;

    public JumpSwitchStmt(ValueExpr<?> expr, Map<Integer, CodeBlock> cases, CodeBlock defaultCase) {
        super(Opcodes.JUMP_SWITCH);
        this.expr = expr;
        this.cases = Collections.unmodifiableMap(cases);
        this.defaultCase = defaultCase;
    }

    public ValueExpr<?> expr() {
        return expr;
    }

    public Map<Integer, CodeBlock> cases() {
        return cases;
    }

    public CodeBlock defaultCase() {
        return defaultCase;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), expr, cases, defaultCase);
    }
}
