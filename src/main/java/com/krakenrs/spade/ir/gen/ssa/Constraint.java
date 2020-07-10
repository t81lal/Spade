package com.krakenrs.spade.ir.gen.ssa;

import java.util.function.Predicate;

import com.krakenrs.spade.ir.code.CodeUnit;
import com.krakenrs.spade.ir.code.expr.LoadFieldExpr;
import com.krakenrs.spade.ir.code.stmt.AssignFieldStmt;

/**
 * Represents a constraint that is applied to a CodeUnit. This allows
 * constraints to be gathered so that a solution can be constrained to ensure
 * the validity of the operation being performed.
 * <p>
 * The meaning of the {@link #test(CodeUnit)} method is 'fails', i.e. if calling
 * {@link #test(CodeUnit)} with a given {@link CodeUnit} returns true then the
 * constraint is <b>NOT</b> satisfied.
 * 
 * @author Bibl
 *
 */
public interface Constraint extends Predicate<CodeUnit> {

    static Constraint makeFieldConstraint(LoadFieldExpr e) {
        return u -> {
            if (u instanceof AssignFieldStmt) {
                AssignFieldStmt assign = (AssignFieldStmt) u;
                return assign.name().equals(e.name()) && assign.fieldType().equals(e.getType());
            } else {
                return ExprConstraints.isInvoke(u.opcode());
            }
        };
    }

    static Constraint makeInvokeConstraint() {
        return u -> {
            int opcode = u.opcode();
            return ExprConstraints.isInvoke(opcode) || ExprConstraints.isHeapStore(opcode);
        };
    }

    static Constraint makeArrayConstraint() {
        // TODO: This is more complicated and needs works
        return u -> {
            return true;
        };
    }
}
