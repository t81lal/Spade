package com.krakenrs.spade.ir.gen.ssa;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.krakenrs.spade.ir.code.CodePrinter;
import com.krakenrs.spade.ir.code.CodeUnit;
import com.krakenrs.spade.ir.code.Expr;
import com.krakenrs.spade.ir.code.stmt.AssignPhiStmt;
import com.krakenrs.spade.ir.value.Constant;
import com.krakenrs.spade.ir.value.Local;

import lombok.AllArgsConstructor;
import lombok.ToString;

@ToString
public class LatestValue {

    public Val realVal;
    public Val suggestedValue;
    public LocalLV source;

    public List<Predicate<CodeUnit>> constraints = new ArrayList<>();

    public LatestValue(Val realVal, Val suggestedValue, LocalLV source) {
        this.realVal = realVal;
        this.suggestedValue = suggestedValue;
        this.source = source;
    }

    public void addConstraints(LatestValue other) {
        constraints.addAll(other.constraints);
    }

    public void collectConstraints(Expr e) {
        ExprConstraints.collectConstraints(this, e);
    }

    public static class Val {
    }

    @AllArgsConstructor
    public static class LocalLV extends Val {
        Local local;

        @Override
        public String toString() {
            return "Local[" + String.valueOf(local) + "]";
        }
    }

    @AllArgsConstructor
    public static class ConstLV extends Val {
        Constant<?> cst;

        @Override
        public String toString() {
            return "Const[" + String.valueOf(cst) + "]";
        }
    }

    @AllArgsConstructor
    public static class VarLV extends Val {
        Expr expr;

        @Override
        public String toString() {
            return "Expr[" + CodePrinter.toString(expr) + "]";
        }
    }

    public static class CatchLV extends Val {
        @Override
        public String toString() {
            return "Catch[]";
        }
    }

    @AllArgsConstructor
    public static class PhiLV extends Val {
        AssignPhiStmt decl;

        @Override
        public String toString() {
            return CodePrinter.toString(decl);
        }
    }
}
