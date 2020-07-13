package com.krakenrs.spade.ir.code.factory;

import java.util.List;
import java.util.Map;

import com.google.inject.assistedinject.Assisted;
import com.krakenrs.spade.ir.code.CodeBlock;
import com.krakenrs.spade.ir.code.Expr;
import com.krakenrs.spade.ir.code.expr.AllocArrayExpr;
import com.krakenrs.spade.ir.code.expr.AllocObjectExpr;
import com.krakenrs.spade.ir.code.expr.ArithmeticExpr;
import com.krakenrs.spade.ir.code.expr.ArrayLengthExpr;
import com.krakenrs.spade.ir.code.expr.CastExpr;
import com.krakenrs.spade.ir.code.expr.CompareExpr;
import com.krakenrs.spade.ir.code.expr.InstanceOfExpr;
import com.krakenrs.spade.ir.code.expr.InvokeExpr;
import com.krakenrs.spade.ir.code.expr.InvokeExpr.Mode;
import com.krakenrs.spade.ir.code.expr.LoadArrayExpr;
import com.krakenrs.spade.ir.code.expr.LoadFieldExpr;
import com.krakenrs.spade.ir.code.expr.NegateExpr;
import com.krakenrs.spade.ir.code.expr.NewObjectExpr;
import com.krakenrs.spade.ir.code.expr.value.LoadLocalExpr;
import com.krakenrs.spade.ir.code.expr.value.ValueExpr;
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
import com.krakenrs.spade.ir.type.ClassType;
import com.krakenrs.spade.ir.type.MethodType;
import com.krakenrs.spade.ir.type.Type;
import com.krakenrs.spade.ir.type.ValueType;
import com.krakenrs.spade.ir.value.Local;

public interface CodeFactory {
    LoadLocalExpr createLoadLocalExpr(ValueType type, Local value);

    AllocArrayExpr createAllocArrayExpr(ValueType type, List<ValueExpr<?>> bounds);

    AllocObjectExpr createObjectExpr(ClassType type);

    ArithmeticExpr createArithmeticExpr(ValueType resultType, ArithmeticExpr.Operation operation,
            @Assisted("left") ValueExpr<?> lhs, @Assisted("right") ValueExpr<?> rhs);

    ArrayLengthExpr createArrayLengthExpr(LoadLocalExpr var);

    CastExpr createCastExpr(ValueType type, LoadLocalExpr var);

    CompareExpr createCompareExpr(@Assisted("left") LoadLocalExpr lhs, @Assisted("right") LoadLocalExpr rhs,
            CompareExpr.Operation operation);

    InstanceOfExpr createInstanceOfExpr(LoadLocalExpr var, Type checkType);

    InvokeExpr.InvokeVirtualExpr createInvokeVirtualExpr(ClassType owner, String name, MethodType methodType, Mode mode,
            LoadLocalExpr accessor, List<ValueExpr<?>> arguments);

    InvokeExpr.InvokeStaticExpr createInvokeStaticExpr(ClassType owner, String name, MethodType methodType,
            List<ValueExpr<?>> arguments);

    LoadArrayExpr createLoadArrayExpr(ValueType componentType, LoadLocalExpr array, ValueExpr<?> index);

    LoadFieldExpr.LoadStaticFieldExpr createLoadStaticFieldExpr(ClassType owner, String name, ValueType fieldType);

    LoadFieldExpr.LoadVirtualFieldExpr createLoadVirtualFieldExpr(ClassType owner, String name, ValueType fieldType,
            LoadLocalExpr accessor);

    NegateExpr createNegateExpr(LoadLocalExpr var);

    NewObjectExpr createNewObjectExpr(ClassType owner, MethodType methodType, List<ValueExpr<?>> arguments);

    AssignArrayStmt createAssignArrayStmt(LoadLocalExpr array, @Assisted("index") ValueExpr<?> index,
            @Assisted("value") ValueExpr<?> value);

    AssignCatchStmt creaetAssignCatchStmt(Local var, ClassType type);

    AssignFieldStmt.AssignStaticFieldStmt createAssignStaticFieldStmt(ClassType owner, String name, ValueType fieldType,
            ValueExpr<?> value);

    AssignFieldStmt.AssignVirtualFieldStmt createAssignVirtualFieldStmt(ClassType owner, String name,
            ValueType fieldType, ValueExpr<?> value, LoadLocalExpr accessor);

    AssignLocalStmt createAssignLocalStmt(Local var, Expr value);

    AssignParamStmt createAssignParamStmt(Local var);

    AssignPhiStmt createAssignPhiStmt(Local var, Map<CodeBlock, Local> arguments);

    ConsumeStmt createConsumeStmt(Expr expr);

    JumpCondStmt createJumpCondStmt(@Assisted("left") ValueExpr<?> lhs, @Assisted("right") ValueExpr<?> rhs,
            JumpCondStmt.Mode mode, CodeBlock target);

    JumpSwitchStmt createJumpSwitchStmt(ValueExpr<?> expr, Map<Integer, CodeBlock> cases, CodeBlock defaultCase);

    JumpUncondStmt createJumpUncondStmt(CodeBlock target);

    MonitorStmt createMonitorStmt(LoadLocalExpr var, MonitorStmt.Mode mode);

    ReturnStmt createReturnStmt(LoadLocalExpr var);

    ThrowStmt createThrowStmt(LoadLocalExpr var);
}
