package com.krakenrs.spade.ir.code.stmt;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.krakenrs.spade.ir.code.CodeBlock;
import com.krakenrs.spade.ir.code.CodeUnit;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.Stmt;
import com.krakenrs.spade.ir.code.expr.value.ValueExpr;
import com.krakenrs.spade.ir.code.visitor.CodeVisitor;

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

    @Override
    public void accept(CodeVisitor vis) {
        super.accept(vis);
        vis.visitJumpSwitchStmt(this);
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
    public boolean equivalent(CodeUnit u) {
        if (super.equivalent(u)) {
            JumpSwitchStmt jss = (JumpSwitchStmt) u;
            if (equivalent(expr, jss.expr) && Objects.equals(defaultCase, jss.defaultCase)) {
                Set<Integer> allKeys = new HashSet<>();
                allKeys.addAll(cases.keySet());
                allKeys.addAll(jss.cases.keySet());

                for (int key : allKeys) {
                    if (!cases.containsKey(key) || !jss.cases.containsKey(key)) {
                        return false;
                    }
                    if (!Objects.equals(cases.get(key), jss.cases.get(key))) {
                        return false;
                    }
                }

                return true;
            }
        }
        return false;
    }
}
