package com.krakenrs.spade.ir.code.factory;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;

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

public abstract class AbstractCodeFactoryTest {

    protected CodeFactory factory;
    
    public abstract CodeFactory getImpl();
    
    @BeforeAll
    public void setup() {
        factory = getImpl();
    }
    
//    LoadLocalExpr testCreateLoadLocalExpr(ValueType type, Local value) {
//        
//    }
//
//    AllocArrayExpr testCreateAllocArrayExpr(ValueType type, List<ValueExpr<?>> bounds);
//
//    AllocObjectExpr testCreateObjectExpr(ClassType type);
//
//    ArithmeticExpr testCreateArithmeticExpr(ValueType resultType, ArithmeticExpr.Operation operation,
//            @Assisted("left") ValueExpr<?> lhs, @Assisted("right") ValueExpr<?> rhs);
//
//    ArrayLengthExpr testCreateArrayLengthExpr(LoadLocalExpr var);
//
//    CastExpr testCreateCastExpr(ValueType type, LoadLocalExpr var);
//
//    CompareExpr testCreateCompareExpr(@Assisted("left") LoadLocalExpr lhs, @Assisted("right") LoadLocalExpr rhs,
//            CompareExpr.Operation operation);
//
//    InstanceOfExpr testCreateInstanceOfExpr(LoadLocalExpr var, Type checkType);
//
//    InvokeExpr.InvokeVirtualExpr testCreateInvokeVirtualExpr(ClassType owner, String name, MethodType methodType, Mode mode,
//            LoadLocalExpr accessor, List<ValueExpr<?>> arguments);
//
//    InvokeExpr.InvokeStaticExpr testCreateInvokeStaticExpr(ClassType owner, String name, MethodType methodType,
//            List<ValueExpr<?>> arguments);
//
//    LoadArrayExpr testCreateLoadArrayExpr(ValueType componentType, LoadLocalExpr array, ValueExpr<?> index);
//
//    LoadFieldExpr.LoadStaticFieldExpr testCreateLoadStaticFieldExpr(ClassType owner, String name, ValueType fieldType);
//
//    LoadFieldExpr.LoadVirtualFieldExpr testCreateLoadVirtualFieldExpr(ClassType owner, String name, ValueType fieldType,
//            LoadLocalExpr accessor);
//
//    NegateExpr testCreateNegateExpr(LoadLocalExpr var);
//
//    NewObjectExpr testCreateNewObjectExpr(ClassType owner, MethodType methodType, List<ValueExpr<?>> arguments);
//
//    AssignArrayStmt testCreateAssignArrayStmt(LoadLocalExpr array, @Assisted("index") ValueExpr<?> index,
//            @Assisted("value") ValueExpr<?> value);
//
//    AssignCatchStmt creaetAssignCatchStmt(Local var, ClassType type);
//
//    AssignFieldStmt.AssignStaticFieldStmt testCreateAssignStaticFieldStmt(ClassType owner, String name, ValueType fieldType,
//            ValueExpr<?> value);
//
//    AssignFieldStmt.AssignVirtualFieldStmt testCreateAssignVirtualFieldStmt(ClassType owner, String name,
//            ValueType fieldType, ValueExpr<?> value, LoadLocalExpr accessor);
//
//    AssignLocalStmt testCreateAssignLocalStmt(Local var, Expr value);
//
//    AssignParamStmt testCreateAssignParamStmt(Local var);
//
//    AssignPhiStmt testCreateAssignPhiStmt(Local var, Map<CodeBlock, Local> arguments);
//
//    ConsumeStmt testCreateConsumeStmt(Expr expr);
//
//    JumpCondStmt testCreateJumpCondStmt(@Assisted("left") ValueExpr<?> lhs, @Assisted("right") ValueExpr<?> rhs,
//            JumpCondStmt.Mode mode, CodeBlock target);
//
//    JumpSwitchStmt testCreateJumpSwitchStmt(ValueExpr<?> expr, Map<Integer, CodeBlock> cases, CodeBlock defaultCase);
//
//    JumpUncondStmt testCreateJumpUncondStmt(CodeBlock target);
//
//    MonitorStmt testCreateMonitorStmt(LoadLocalExpr var, MonitorStmt.Mode mode);
//
//    ReturnStmt testCreateReturnStmt(LoadLocalExpr var);
//
//    ThrowStmt testCreateThrowStmt(LoadLocalExpr var);
}
