package com.krakenrs.spade.ir.code;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.krakenrs.spade.ir.code.expr.AllocArrayExpr;
import com.krakenrs.spade.ir.code.expr.AllocObjectExpr;
import com.krakenrs.spade.ir.code.expr.ArithmeticExpr;
import com.krakenrs.spade.ir.code.expr.ArrayLengthExpr;
import com.krakenrs.spade.ir.code.expr.CastExpr;
import com.krakenrs.spade.ir.code.expr.CompareExpr;
import com.krakenrs.spade.ir.code.expr.InstanceOfExpr;
import com.krakenrs.spade.ir.code.expr.InvokeExpr;
import com.krakenrs.spade.ir.code.expr.InvokeExpr.InvokeVirtualExpr;
import com.krakenrs.spade.ir.code.expr.LoadArrayExpr;
import com.krakenrs.spade.ir.code.expr.LoadFieldExpr;
import com.krakenrs.spade.ir.code.expr.LoadFieldExpr.LoadVirtualFieldExpr;
import com.krakenrs.spade.ir.code.expr.NegateExpr;
import com.krakenrs.spade.ir.code.expr.value.ValueExpr;
import com.krakenrs.spade.ir.code.stmt.AssignArrayStmt;
import com.krakenrs.spade.ir.code.stmt.AssignCatchStmt;
import com.krakenrs.spade.ir.code.stmt.AssignFieldStmt;
import com.krakenrs.spade.ir.code.stmt.AssignFieldStmt.AssignVirtualFieldStmt;
import com.krakenrs.spade.ir.code.stmt.AssignLocalStmt;
import com.krakenrs.spade.ir.code.stmt.AssignParamStmt;
import com.krakenrs.spade.ir.code.stmt.ConsumeStmt;
import com.krakenrs.spade.ir.code.stmt.JumpCondStmt;
import com.krakenrs.spade.ir.code.stmt.JumpSwitchStmt;
import com.krakenrs.spade.ir.code.stmt.JumpUncondStmt;
import com.krakenrs.spade.ir.code.stmt.MonitorStmt;
import com.krakenrs.spade.ir.code.stmt.ReturnStmt;
import com.krakenrs.spade.ir.code.stmt.ThrowStmt;
import com.krakenrs.spade.ir.type.ArrayType;
import com.krakenrs.spade.ir.type.ObjectType;

public class CodePrinter implements Opcodes {

    public static String toString(ControlFlowGraph cfg) {
        Emitter e = new Emitter();
        e.emitCfg(cfg);
        return e.toString();
    }

    static class Emitter {
        final String tab = "  ";

        int indent = 0;
        String prefix = "";

        StringBuilder buffer = new StringBuilder();

        Emitter indent() {
            indent++;
            prefix += tab;
            return this;
        }

        Emitter unindent() {
            if (indent > 0) {
                indent--;
                prefix = "";
                for (int i = 0; i < indent; i++) {
                    prefix += tab;
                }
            }
            return this;
        }
        
        Emitter emitIndent() {
            buffer.append(prefix);
            return this;
        }

        Emitter nl() {
            buffer.append('\n');
            emitIndent();
            return this;
        }

        Emitter emit(Object o) {
            buffer.append(o.toString());
            return this;
        }

        void emitCfg(ControlFlowGraph cfg) {
            List<CodeBlock> blocks = new ArrayList<>(cfg.getVertices());
            blocks.sort((a, b) -> a.getOrderHint() - b.getOrderHint());

            Iterator<CodeBlock> it = blocks.iterator();
            while (it.hasNext()) {
                emitBlock(cfg, it.next());
                if (it.hasNext()) {
                    nl();
                }
            }
        }

        void emitBlock(ControlFlowGraph cfg, CodeBlock block) {
            List<Stmt> stmts = block.stmts();
            emit("==Block(id=L").emit(block.id()).emit(", order=").emit(block.getOrderHint()).emit(", size=")
                    .emit(stmts.size()).emit(")");
            indent();
            for (int i = 0; i < stmts.size(); i++) {
                nl();
                emit(i).emit(". ").emitStmt(stmts.get(i));
            }
            // Empty newline but without the indentation
            unindent().nl().indent();
            for (FlowEdge e : cfg.getEdges(block)) {
                nl();
                emitEdge(e);
            }
            unindent();
            nl();
        }

        Emitter emitTarget(CodeBlock block) {
            return emit("L").emit(block.id());
        }

        Emitter emitExpr(Expr e) {
            switch (e.opcode()) {
                case LOAD_CONST:
                case LOAD_LOCAL: {
                    return emit(((ValueExpr<?>) e).value());
                }
                case ALLOCARR: {
                    AllocArrayExpr aae = (AllocArrayExpr) e;
                    emit("new ").emit(((ArrayType) aae.type()).elementType());
                    for (ValueExpr<?> bound : aae.bounds()) {
                        emit('[').emitExpr(bound).emit(']');
                    }
                    return this;
                }
                case ALLOCOBJ: {
                    AllocObjectExpr aoe = (AllocObjectExpr) e;
                    return emit("new ").emit(((ObjectType) aoe.type()).getClassType());
                }
                case ARITHMETIC: {
                    ArithmeticExpr ae = (ArithmeticExpr) e;
                    return emitExpr(ae.lhs()).emit(" ").emit(ae.op()).emit(" ").emitExpr(ae.rhs());
                }
                case ARRAYLEN: {
                    return emitExpr(((ArrayLengthExpr) e).var()).emit(".length");
                }
                case CAST: {
                    return emit("(").emit(e.type()).emit(") ").emitExpr(((CastExpr) e).var());
                }
                case COMPARE: {
                    CompareExpr ce = (CompareExpr) e;
                    return emitExpr(ce.lhs()).emit(" ").emit(ce.op()).emit(" ").emitExpr(ce.rhs());
                }
                case INSTANCEOF: {
                    InstanceOfExpr ioe = (InstanceOfExpr) e;
                    return emitExpr(ioe.var()).emit(" instanceof ").emit(ioe.checkType());
                }
                case INVOKE: {
                    InvokeExpr ie = (InvokeExpr) e;
                    if (ie.mode() != InvokeExpr.Mode.STATIC) {
                        InvokeVirtualExpr ive = (InvokeVirtualExpr) ie;
                        emitExpr(ive.accessor());
                    } else {
                        emit(ie.owner().getClassName());
                    }
                    emit(".").emit(ie.name()).emit("(");
                    Iterator<ValueExpr<?>> it = ie.arguments().iterator();
                    while (it.hasNext()) {
                        emitExpr(it.next());
                        if (it.hasNext()) {
                            emit(", ");
                        }
                    }
                    emit(")");
                    return this;
                }
                case LOAD_ARR: {
                    LoadArrayExpr lae = (LoadArrayExpr) e;
                    return emitExpr(lae.array()).emit("[").emitExpr(lae.index()).emit("]");
                }
                case LOAD_FIELD: {
                    LoadFieldExpr lfe = (LoadFieldExpr) e;
                    if (lfe.isStatic()) {
                        emit(lfe.owner().getClassName());
                    } else {
                        LoadVirtualFieldExpr lvfe = (LoadVirtualFieldExpr) lfe;
                        emitExpr(lvfe.accessor());
                    }
                    emit(".").emit(lfe.name());
                    return this;
                }
                case NEGATE: {
                    NegateExpr ne = (NegateExpr) e;
                    return emit("-").emitExpr(ne.var());
                }
                default: {
                    throw new IllegalArgumentException("Opcode: " + e.opcode());
                }
            }
        }

        Emitter emitStmt(Stmt stmt) {
            switch (stmt.opcode()) {
                case ASSIGN_ARRAY: {
                    AssignArrayStmt aas = (AssignArrayStmt) stmt;
                    return emitExpr(aas.array()).emit("[").emitExpr(aas.index()).emit("] = ").emitExpr(aas.value());
                }
                case ASSIGN_CATCH: {
                    AssignCatchStmt acs = (AssignCatchStmt) stmt;
                    return emit(acs.var()).emit(" = catch(").emit(acs.type().getClassName()).emit(")");
                }
                case ASSIGN_FIELD: {
                    AssignFieldStmt afs = (AssignFieldStmt) stmt;
                    if (afs.isStatic()) {
                        emit(afs.owner().getClassName());
                    } else {
                        AssignVirtualFieldStmt avfs = (AssignVirtualFieldStmt) afs;
                        emitExpr(avfs.accessor());
                    }
                    emit(".").emit(afs.name()).emit(" = ").emitExpr(afs.value());
                    return this;
                }
                case ASSIGN_LOCAL: {
                    AssignLocalStmt als = (AssignLocalStmt) stmt;
                    return emit(als.var()).emit(" = ").emitExpr(als.value());
                }
                case ASSIGN_PARAM: {
                    AssignParamStmt aps = (AssignParamStmt) stmt;
                    return emit("@param ").emit(aps.var());
                }
                case CONSUME: {
                    ConsumeStmt cs = (ConsumeStmt) stmt;
                    return emit("_consume(").emitExpr(cs.expr()).emit(")");
                }
                case JUMP_COND: {
                    JumpCondStmt jcs = (JumpCondStmt) stmt;
                    emit("if(").emitExpr(jcs.lhs()).emit(" ").emit(jcs.mode()).emit(" ").emitExpr(jcs.rhs())
                            .emit(") goto ").emitTarget(jcs.target());
                    return this;
                }
                case JUMP_SWITCH: {
                    JumpSwitchStmt jss = (JumpSwitchStmt) stmt;
                    emit("switch(").emitExpr(jss.expr()).emit(")");
                    indent();
                    for (Entry<Integer, CodeBlock> c : jss.cases().entrySet()) {
                        nl().emit("case ").emit(c.getKey()).emit(": goto ").emitTarget(c.getValue());
                    }
                    nl().emit("default: goto ").emitTarget(jss.defaultCase());
                    return unindent();
                }
                case JUMP_UNCOND: {
                    JumpUncondStmt jus = (JumpUncondStmt) stmt;
                    return emit("goto ").emitTarget(jus.target());
                }
                case MONITOR: {
                    MonitorStmt ms = (MonitorStmt) stmt;
                    return emit("_mon").emit(ms.mode().name().toLowerCase()).emit("(").emitExpr(ms.var()).emit(")");
                }
                case RETURN: {
                    ReturnStmt rs = (ReturnStmt) stmt;
                    emit("return");
                    if (rs.var() != null) {
                        emit(" ").emitExpr(rs.var());
                    }
                    return this;
                }
                case THROW: {
                    ThrowStmt ts = (ThrowStmt) stmt;
                    return emit("throw ").emitExpr(ts.var());
                }
                default: {
                    throw new IllegalArgumentException("Opcode: " + stmt.opcode());
                }
            }
        }

        void emitEdge(FlowEdge e) {
            emit(e);
        }

        @Override
        public String toString() {
            return buffer.toString();
        }
    }
}