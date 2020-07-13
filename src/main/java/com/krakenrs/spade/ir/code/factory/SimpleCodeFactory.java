package com.krakenrs.spade.ir.code.factory;

import java.util.List;
import java.util.Map;

import com.krakenrs.spade.ir.code.CodeBlock;
import com.krakenrs.spade.ir.code.Expr;
import com.krakenrs.spade.ir.code.expr.AllocArrayExpr;
import com.krakenrs.spade.ir.code.expr.AllocObjectExpr;
import com.krakenrs.spade.ir.code.expr.ArithmeticExpr;
import com.krakenrs.spade.ir.code.expr.ArrayLengthExpr;
import com.krakenrs.spade.ir.code.expr.CastExpr;
import com.krakenrs.spade.ir.code.expr.CompareExpr;
import com.krakenrs.spade.ir.code.expr.InstanceOfExpr;
import com.krakenrs.spade.ir.code.expr.InvokeExpr.InvokeStaticExpr;
import com.krakenrs.spade.ir.code.expr.InvokeExpr.InvokeVirtualExpr;
import com.krakenrs.spade.ir.code.expr.InvokeExpr.Mode;
import com.krakenrs.spade.ir.code.expr.LoadArrayExpr;
import com.krakenrs.spade.ir.code.expr.LoadFieldExpr.LoadStaticFieldExpr;
import com.krakenrs.spade.ir.code.expr.LoadFieldExpr.LoadVirtualFieldExpr;
import com.krakenrs.spade.ir.code.expr.NegateExpr;
import com.krakenrs.spade.ir.code.expr.NewObjectExpr;
import com.krakenrs.spade.ir.code.expr.value.LoadLocalExpr;
import com.krakenrs.spade.ir.code.expr.value.ValueExpr;
import com.krakenrs.spade.ir.code.stmt.AssignArrayStmt;
import com.krakenrs.spade.ir.code.stmt.AssignCatchStmt;
import com.krakenrs.spade.ir.code.stmt.AssignFieldStmt.AssignStaticFieldStmt;
import com.krakenrs.spade.ir.code.stmt.AssignFieldStmt.AssignVirtualFieldStmt;
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
import com.krakenrs.spade.ir.type.ClassType;
import com.krakenrs.spade.ir.type.MethodType;
import com.krakenrs.spade.ir.type.Type;
import com.krakenrs.spade.ir.type.ValueType;
import com.krakenrs.spade.ir.value.Local;

public class SimpleCodeFactory implements CodeFactory {
    @Override
    public LoadLocalExpr createLoadLocalExpr(ValueType type, Local value) {
        return new LoadLocalExpr(type, value);
    }

    @Override
    public AllocArrayExpr createAllocArrayExpr(ValueType type, List<ValueExpr<?>> bounds) {
        return new AllocArrayExpr(type, bounds);
    }

    @Override
    public AllocObjectExpr createObjectExpr(ClassType type) {
        return new AllocObjectExpr(type);
    }

    @Override
    public ArithmeticExpr createArithmeticExpr(ValueType resultType, ArithmeticExpr.Operation operation,
            ValueExpr<?> lhs, ValueExpr<?> rhs) {
        return new ArithmeticExpr(resultType, operation, lhs, rhs);
    }

    @Override
    public ArrayLengthExpr createArrayLengthExpr(LoadLocalExpr var) {
        return new ArrayLengthExpr(var);
    }

    @Override
    public CastExpr createCastExpr(ValueType type, LoadLocalExpr var) {
        return new CastExpr(type, var);
    }

    @Override
    public CompareExpr createCompareExpr(LoadLocalExpr lhs, LoadLocalExpr rhs, CompareExpr.Operation operation) {
        return new CompareExpr(lhs, rhs, operation);
    }

    @Override
    public InstanceOfExpr createInstanceOfExpr(LoadLocalExpr var, Type checkType) {
        return new InstanceOfExpr(var, checkType);
    }

    @Override
    public InvokeVirtualExpr createInvokeVirtualExpr(ClassType owner, String name, MethodType methodType, Mode mode,
            LoadLocalExpr accessor, List<ValueExpr<?>> arguments) {
        return new InvokeVirtualExpr(owner, name, methodType, mode, accessor, arguments);
    }

    @Override
    public InvokeStaticExpr createInvokeStaticExpr(ClassType owner, String name, MethodType methodType,
            List<ValueExpr<?>> arguments) {
        return new InvokeStaticExpr(owner, name, methodType, arguments);
    }

    @Override
    public LoadArrayExpr createLoadArrayExpr(ValueType componentType, LoadLocalExpr array, ValueExpr<?> index) {
        return new LoadArrayExpr(componentType, array, index);
    }

    @Override
    public LoadStaticFieldExpr createLoadStaticFieldExpr(ClassType owner, String name, ValueType fieldType) {
        return new LoadStaticFieldExpr(owner, name, fieldType);
    }

    @Override
    public LoadVirtualFieldExpr createLoadVirtualFieldExpr(ClassType owner, String name, ValueType fieldType,
            LoadLocalExpr accessor) {
        return new LoadVirtualFieldExpr(owner, name, fieldType, accessor);
    }

    @Override
    public NegateExpr createNegateExpr(LoadLocalExpr var) {
        return new NegateExpr(var);
    }

    @Override
    public NewObjectExpr createNewObjectExpr(ClassType owner, MethodType methodType, List<ValueExpr<?>> arguments) {
        return new NewObjectExpr(owner, methodType, arguments);
    }

    @Override
    public AssignArrayStmt createAssignArrayStmt(LoadLocalExpr array, ValueExpr<?> index, ValueExpr<?> value) {
        return new AssignArrayStmt(array, index, value);
    }

    @Override
    public AssignCatchStmt creaetAssignCatchStmt(Local var, ClassType type) {
        return new AssignCatchStmt(var, type);
    }

    @Override
    public AssignStaticFieldStmt createAssignStaticFieldStmt(ClassType owner, String name, ValueType fieldType,
            ValueExpr<?> value) {
        return new AssignStaticFieldStmt(owner, name, fieldType, value);
    }

    @Override
    public AssignVirtualFieldStmt createAssignVirtualFieldStmt(ClassType owner, String name, ValueType fieldType,
            ValueExpr<?> value, LoadLocalExpr accessor) {
        return new AssignVirtualFieldStmt(owner, name, fieldType, value, accessor);
    }

    @Override
    public AssignLocalStmt createAssignLocalStmt(Local var, Expr value) {
        return new AssignLocalStmt(var, value);
    }

    @Override
    public AssignParamStmt createAssignParamStmt(Local var) {
        return new AssignParamStmt(var);
    }

    @Override
    public AssignPhiStmt createAssignPhiStmt(Local var, Map<CodeBlock, Local> arguments) {
        return new AssignPhiStmt(var, arguments);
    }

    @Override
    public ConsumeStmt createConsumeStmt(Expr expr) {
        return new ConsumeStmt(expr);
    }

    @Override
    public JumpCondStmt createJumpCondStmt(ValueExpr<?> lhs, ValueExpr<?> rhs, JumpCondStmt.Mode mode,
            CodeBlock target) {
        return new JumpCondStmt(lhs, rhs, mode, target);
    }

    @Override
    public JumpSwitchStmt createJumpSwitchStmt(ValueExpr<?> expr, Map<Integer, CodeBlock> cases,
            CodeBlock defaultCase) {
        return new JumpSwitchStmt(expr, cases, defaultCase);
    }

    @Override
    public JumpUncondStmt createJumpUncondStmt(CodeBlock target) {
        return new JumpUncondStmt(target);
    }

    @Override
    public MonitorStmt createMonitorStmt(LoadLocalExpr var, MonitorStmt.Mode mode) {
        return new MonitorStmt(var, mode);
    }

    @Override
    public ReturnStmt createReturnStmt(LoadLocalExpr var) {
        return new ReturnStmt(var);
    }

    @Override
    public ThrowStmt createThrowStmt(LoadLocalExpr var) {
        return new ThrowStmt(var);
    }
}
