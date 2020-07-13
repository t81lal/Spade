package com.krakenrs.spade.ir.code.visitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.krakenrs.spade.commons.collections.tuple.Tuple2;
import com.krakenrs.spade.commons.collections.tuple.Tuple3;
import com.krakenrs.spade.ir.code.CodeUnit;
import com.krakenrs.spade.ir.code.Expr;
import com.krakenrs.spade.ir.code.Stmt;
import com.krakenrs.spade.ir.code.expr.AllocArrayExpr;
import com.krakenrs.spade.ir.code.expr.AllocObjectExpr;
import com.krakenrs.spade.ir.code.expr.ArithmeticExpr;
import com.krakenrs.spade.ir.code.expr.ArrayLengthExpr;
import com.krakenrs.spade.ir.code.expr.CastExpr;
import com.krakenrs.spade.ir.code.expr.CompareExpr;
import com.krakenrs.spade.ir.code.expr.InstanceOfExpr;
import com.krakenrs.spade.ir.code.expr.InvokeExpr;
import com.krakenrs.spade.ir.code.expr.InvokeExpr.InvokeStaticExpr;
import com.krakenrs.spade.ir.code.expr.InvokeExpr.InvokeVirtualExpr;
import com.krakenrs.spade.ir.code.expr.LoadArrayExpr;
import com.krakenrs.spade.ir.code.expr.LoadFieldExpr;
import com.krakenrs.spade.ir.code.expr.LoadFieldExpr.LoadVirtualFieldExpr;
import com.krakenrs.spade.ir.code.expr.NegateExpr;
import com.krakenrs.spade.ir.code.expr.NewObjectExpr;
import com.krakenrs.spade.ir.code.expr.value.LoadConstExpr;
import com.krakenrs.spade.ir.code.expr.value.LoadLocalExpr;
import com.krakenrs.spade.ir.code.stmt.AssignArrayStmt;
import com.krakenrs.spade.ir.code.stmt.AssignCatchStmt;
import com.krakenrs.spade.ir.code.stmt.AssignFieldStmt;
import com.krakenrs.spade.ir.code.stmt.AssignLocalStmt;
import com.krakenrs.spade.ir.code.stmt.AssignParamStmt;
import com.krakenrs.spade.ir.code.stmt.AssignPhiStmt;
import com.krakenrs.spade.ir.code.stmt.ConsumeStmt;
import com.krakenrs.spade.ir.code.stmt.JumpCondStmt;
import com.krakenrs.spade.ir.code.stmt.JumpSwitchStmt;
import com.krakenrs.spade.ir.code.stmt.JumpUncondStmt;
import com.krakenrs.spade.ir.code.stmt.MonitorStmt;
import com.krakenrs.spade.ir.code.stmt.ReturnStmt;
import com.krakenrs.spade.ir.code.stmt.ThrowStmt;

public class AbstractCodeReducer implements CodeReducer {

    /**
     * Reduces the given {@link Expr}, returning an {@link Optional} that is empty when nothing changed or an optional
     * containing the reduced {@link Expr}.
     * <p>
     * See {@link #reduce2(Expr, Expr)} and {@link #reduce3(Expr, Expr, Expr)}
     * 
     * @param <R> The exact type of the input Expr
     * @param r The input expr
     * @return An {@link Optional} of the reduced expr
     */
    <R extends Expr> Optional<R> reduce1(R r) {
        @SuppressWarnings("unchecked")
        R rR = reduceExprTo(r, (Class<R>) r.getClass());
        Objects.requireNonNull(rR);
        if (rR == r) {
            return Optional.empty();
        } else {
            return Optional.of(rR);
        }
    }

    /**
     * Reduces the given {@link Expr}s, returning an {@link Optional} that is empty if none of the given exprs were
     * changed, otherwise returns a {@link Tuple2} that
     * contains parts of the expr that were reduced and copies of everything else.
     * <p>
     * E.g. Given two exprs, a and b, if a is reduced but b is not, we need to make a new copy of b as b is already
     * bound to a parent, so we return (reducedA, b.deepCopy)
     * <p>
     * See {@link #reduce1(Expr)} and {@link #reduce3(Expr, Expr, Expr)}
     * 
     * @param <R> The type of the first input expr
     * @param <S> The type of the second input expr
     * @param r The first input expr
     * @param s The second input expr
     * @return An {@link Optional} of the reduced/copied exprs
     */
    @SuppressWarnings("unchecked")
    <R extends Expr, S extends Expr> Optional<Tuple2<R, S>> reduce2(R r, S s) {
        R rR = reduceExprTo(r, (Class<R>) r.getClass());
        S rS = reduceExprTo(s, (Class<S>) s.getClass());
        Objects.requireNonNull(rR);
        Objects.requireNonNull(rS);

        boolean rChange = rR != r;
        boolean sChange = rS != s;

        if (rChange || sChange) {
            R newR = rChange ? rR : (R) r.deepCopy();
            S newS = sChange ? rS : (S) s.deepCopy();
            return Optional.of(new Tuple2.ImmutableTuple2<>(newR, newS));
        } else {
            return Optional.empty();
        }
    }

    /**
     * 3 pararmeter extension of {@link #reduce1(Expr)} and {@link #reduce2(Expr, Expr)}
     * 
     * @param <R>
     * @param <S>
     * @param <T>
     * @param r
     * @param s
     * @param t
     * @return
     */
    @SuppressWarnings("unchecked")
    <R extends Expr, S extends Expr, T extends Expr> Optional<Tuple3<R, S, T>> reduce3(R r, S s, T t) {
        R rR = reduceExprTo(r, (Class<R>) r.getClass());
        S rS = reduceExprTo(s, (Class<S>) s.getClass());
        T rT = reduceExprTo(t, (Class<T>) t.getClass());
        Objects.requireNonNull(rR);
        Objects.requireNonNull(rS);
        Objects.requireNonNull(rT);

        boolean rChange = rR != r;
        boolean sChange = rS != s;
        boolean tChange = rT != t;

        if (rChange || sChange) {
            R newR = rChange ? rR : (R) r.deepCopy();
            S newS = sChange ? rS : (S) s.deepCopy();
            T newT = tChange ? rT : (T) t.deepCopy();
            return Optional.of(new Tuple3.ImmutableTuple3<>(newR, newS, newT));
        } else {
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    <T extends Expr> Optional<List<T>> reduceList(List<T> ts) {
        if (ts.size() == 0) {
            /* can't reduce if there are no elements to reduce */
            return Optional.empty();
        }

        Class<T> tClass = (Class<T>) ts.get(0).getClass();

        List<T> newTs = new ArrayList<>();
        boolean anyChange = false;

        /* we can't iterate using indices and call list.set(index, val) to set the reduced exprs
         * as we can't allocate an ArrayList that way */
        for (T t : ts) {
            T rT = reduceExprTo(t, tClass);
            Objects.requireNonNull(rT);

            boolean tChange = rT != t;
            if (tChange) {
                newTs.add(rT);
            } else {
            	/* intermediary value meaning that the element at this index should be deepCopy'd later */
            	newTs.add(null);
            }

            anyChange |= tChange;
        }

        if (!anyChange) {
            return Optional.empty();
        }

        for (int i = 0; i < ts.size(); i++) {
            if (newTs.get(i) == null) {
                newTs.set(i, (T) ts.get(i).deepCopy());
            }
        }

        return Optional.of(newTs);
    }

    @Override
    public Stmt reduceAssignArrayStmt(AssignArrayStmt s) {
        return reduce3(s.array(), s.index(), s.value())
                .map(parts -> new AssignArrayStmt(parts.getA(), parts.getB(), parts.getC())).orElse(s);
    }

    @Override
    public Stmt reduceAssignCatchStmt(AssignCatchStmt s) {
        return s;
    }

    @Override
    public Stmt reduceAssignFieldStmt(AssignFieldStmt s) {
        if (!s.isStatic()) {
            AssignFieldStmt.AssignVirtualFieldStmt vs = (AssignFieldStmt.AssignVirtualFieldStmt) s;
            return reduce2(vs.accessor(), s.value()).map(parts -> new AssignFieldStmt.AssignVirtualFieldStmt(s.owner(),
                    s.name(), s.fieldType(), parts.getB(), parts.getA())).orElse(vs);
        } else {
            AssignFieldStmt.AssignStaticFieldStmt ss = (AssignFieldStmt.AssignStaticFieldStmt) s;
            return reduce1(s.value()).map(
                    accessor -> new AssignFieldStmt.AssignStaticFieldStmt(s.owner(), s.name(), s.fieldType(), accessor))
                    .orElse(ss);
        }
    }

    @Override
    public Stmt reduceAssignLocalStmt(AssignLocalStmt s) {
        return reduce1(s.value()).map(e -> new AssignLocalStmt(s.var(), e)).orElse(s);
    }

    @Override
    public Stmt reduceAssignParamStmt(AssignParamStmt s) {
        return s;
    }

    @Override
    public Stmt reduceAssignPhiStmt(AssignPhiStmt s) {
        return s;
    }

    @Override
    public Stmt reduceConsumeStmt(ConsumeStmt s) {
        return reduce1(s.expr()).map(ConsumeStmt::new).orElse(s);
    }

    @Override
    public Stmt reduceJumpCondStmt(JumpCondStmt s) {
        return reduce2(s.lhs(), s.rhs())
                .map(parts -> new JumpCondStmt(parts.getA(), parts.getB(), s.mode(), s.target())).orElse(s);
    }

    @Override
    public Stmt reduceJumpSwitchStmt(JumpSwitchStmt s) {
        return reduce1(s.expr()).map(e -> new JumpSwitchStmt(e, s.cases(), s.defaultCase())).orElse(s);
    }

    @Override
    public Stmt reduceJumpUncondStmt(JumpUncondStmt s) {
        return s;
    }

    @Override
    public Stmt reduceMonitorStmt(MonitorStmt s) {
        return reduce1(s.var()).map(v -> new MonitorStmt(v, s.mode())).orElse(s);
    }

    @Override
    public Stmt reduceReturnStmt(ReturnStmt s) {
        if (s.var() == null) {
            // void return
            return s;
        } else {
            return reduce1(s.var()).map(ReturnStmt::new).orElse(s);
        }
    }

    @Override
    public Stmt reduceThrowStmt(ThrowStmt s) {
        return reduce1(s.var()).map(ThrowStmt::new).orElse(s);
    }

    @Override
    public Expr reduceLoadLocalExpr(LoadLocalExpr e) {
        return e;
    }

    @Override
    public Expr reduceLoadConstExpr(LoadConstExpr<?> e) {
        return e;
    }

    @Override
    public Expr reduceAllocArrayExpr(AllocArrayExpr e) {
        return reduceList(e.getBounds()).map(bs -> new AllocArrayExpr(e.getType(), bs)).orElse(e);
    }

    @Override
    public Expr reduceAllocObjectExpr(AllocObjectExpr e) {
        return e;
    }

    @Override
    public Expr reduceArithmeticExpr(ArithmeticExpr e) {
        return reduce2(e.getLhs(), e.getRhs())
                .map(parts -> new ArithmeticExpr(e.getType(), e.getOperation(), parts.getA(), parts.getB())).orElse(e);
    }

    @Override
    public Expr reduceNegateExpr(NegateExpr e) {
        return reduce1(e.var()).map(NegateExpr::new).orElse(e);
    }

    @Override
    public Expr reduceArrayLengthExpr(ArrayLengthExpr e) {
        return reduce1(e.var()).map(ArrayLengthExpr::new).orElse(e);
    }

    @Override
    public Expr reduceCastExpr(CastExpr e) {
        return reduce1(e.var()).map(var -> new CastExpr(e.getType(), var)).orElse(e);
    }

    @Override
    public Expr reduceCompareExpr(CompareExpr e) {
        return reduce2(e.lhs(), e.rhs()).map(parts -> new CompareExpr(parts.getA(), parts.getB(), e.op())).orElse(e);
    }

    @Override
    public Expr reduceInstanceOfExpr(InstanceOfExpr e) {
        return reduce1(e.var()).map(var -> new InstanceOfExpr(var, e.checkType())).orElse(e);
    }

    @Override
    public Expr reduceInvokeExpr(InvokeExpr e) {
        if (e instanceof InvokeExpr.InvokeVirtualExpr) {
            InvokeExpr.InvokeVirtualExpr ve = (InvokeVirtualExpr) e;

            var optAcc = reduce1(ve.accessor());
            var optArgs = reduceList(ve.arguments());

            if (optAcc.isEmpty() && optArgs.isEmpty()) {
                return ve;
            } else {
                optAcc = optAcc.or(() -> Optional.of(ve.accessor().deepCopy()));
                optArgs = optArgs.or(() -> Optional.of(CodeUnit.deepCopy(ve.arguments())));

                return new InvokeExpr.InvokeVirtualExpr(e.owner(), e.name(), e.methodType(), e.mode(), optAcc.get(),
                        optArgs.get());
            }
        } else if (e instanceof InvokeExpr.InvokeStaticExpr) {
            InvokeExpr.InvokeStaticExpr se = (InvokeStaticExpr) e;
            return reduceList(se.arguments())
                    .map(args -> new InvokeExpr.InvokeStaticExpr(e.owner(), e.name(), e.methodType(), args)).orElse(se);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public Expr reduceLoadArrayExpr(LoadArrayExpr e) {
        return reduce2(e.array(), e.index()).map(parts -> new LoadArrayExpr(e.getType(), parts.getA(), parts.getB()))
                .orElse(e);
    }

    @Override
    public Expr reduceLoadFieldExpr(LoadFieldExpr e) {
        if (e.isStatic()) {
            return e;
        } else {
            LoadFieldExpr.LoadVirtualFieldExpr ve = (LoadVirtualFieldExpr) e;
            return reduce1(ve.accessor())
                    .map(acc -> new LoadFieldExpr.LoadVirtualFieldExpr(e.owner(), e.name(), e.getType(), acc))
                    .orElse(ve);
        }
    }

    @Override
    public Expr reduceNewObjectExpr(NewObjectExpr e) {
        return reduceList(e.arguments()).map(args -> new NewObjectExpr(e.owner(), e.methodType(), args)).orElse(e);
    }
}
