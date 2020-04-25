package com.krakenrs.spade.ir.irify;

import com.krakenrs.spade.ir.code.*;
import com.krakenrs.spade.ir.code.expr.*;
import com.krakenrs.spade.ir.code.expr.value.LoadConstExpr;
import com.krakenrs.spade.ir.code.expr.value.LoadLocalExpr;
import com.krakenrs.spade.ir.code.expr.value.ValueExpr;
import com.krakenrs.spade.ir.code.stmt.*;
import com.krakenrs.spade.ir.code.visitor.CodeVisitor;
import com.krakenrs.spade.ir.gen.AsmGenerationCtx;
import com.krakenrs.spade.ir.gen.AsmGenerator;
import com.krakenrs.spade.ir.type.SimpleTypeManager;
import com.krakenrs.spade.ir.value.Local;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.util.Comparator;

public class Irifier {
    static int myMethod(boolean b, int x, int y) {
        int z = 0;
        System.out.println(b);
        if (b) {
            z = x + y;
        } else {
            z = x - y;
        }
        return z;
    }

    public static void main(String[] args) throws Exception {
        var sw = new IndentedStringWriter();

        var cr = new ClassReader(Irifier.class.getName());
        var cn = new ClassNode();
        cr.accept(cn, 0);
        var mn = cn.methods.stream().filter(mn_ -> mn_.name.equals("myMethod")).findFirst().orElseThrow();
        var ctx = new AsmGenerationCtx(new SimpleTypeManager(), cn, mn);
        var cfg = AsmGenerator.run(ctx);

        print(cfg, sw);

        System.out.println(sw.toString());
    }

    public static void print(ControlFlowGraph cfg, IndentedStringWriter sw) {
        sw.println("var tm = new SimpleTypeManager();");
        sw.print("var cfg = new ControlFlowGraph(");
        sw.print("tm.asMethodType(\"");
        sw.print(cfg.getMethodType().toString());
        sw.print("\"), ");
        sw.print(Boolean.toString(cfg.isStatic()));
        sw.println(");");
        sw.newline();

        // Handle entry block.
        {
            final String name = String.format("cb%d", cfg.getEntryBlock().id());

            sw.print("var ");
            sw.print(name);
            sw.println(" = cfg.getEntryBlock();");
        }

        // Print vars first so that stmts can refer to them.
        for (var cb : cfg.getVertices()) {
            if (cb == cfg.getEntryBlock()) {
                continue;
            }

            // Ctor
            final String name = String.format("cb%d", cb.id());
            sw.print("var ");
            sw.print(name);
            sw.print(" = new CodeBlock(");
            sw.print(Integer.toString(cb.id()));
            sw.print(");");
            sw.newline();
        }
        sw.newline();

        cfg.getVertices().stream().sorted(Comparator.comparingInt(CodeBlock::getOrderHint)).forEach(cb -> {
            sw.println("{");
            sw.indent();
            print(cb, sw);

            if (cb != cfg.getEntryBlock()) {
                sw.print("cfg.addVertex(");
                sw.print("cb");
                sw.print(Integer.toString(cb.id()));
                sw.println(");");
            }

            sw.deindent();
            sw.println("}\n");
        });

        sw.println("{");
        sw.indent();
        for (var e : cfg.getEdges()) {
            print(e, sw);
        }
        sw.deindent();
        sw.println("}");

        // TODO: Exception ranges.
    }

    public static void print(CodeBlock cb, IndentedStringWriter sw) {
        final String name = String.format("cb%d", cb.id());

        // Set order hint.
        sw.print(name);
        sw.print(".setOrderHint(");
        sw.print(Integer.toString(cb.getOrderHint()));
        sw.println(");");

        final var vis = new PrintingCodeVisitor(name, sw);

        cb.stmts().forEach(stmt -> stmt.accept(vis));
    }

    public static void print(FlowEdge e, IndentedStringWriter sw) {
        if (e instanceof FlowEdge.DefaultEdge) {
            var de = (FlowEdge.DefaultEdge) e;
            sw.print("cfg.addEdge(new FlowEdge.DefaultEdge(");
            sw.print("cb");
            sw.print(Integer.toString(de.getSource().id()));
            sw.print(", cb");
            sw.print(Integer.toString(de.getDestination().id()));
            sw.println("));");
        } else if (e instanceof FlowEdge.ImmediateEdge) {
            var ie = (FlowEdge.ImmediateEdge) e;
            sw.print("cfg.addEdge(new FlowEdge.ImmediateEdge(");
            sw.print("cb");
            sw.print(Integer.toString(ie.getSource().id()));
            sw.print(", cb");
            sw.print(Integer.toString(ie.getDestination().id()));
            sw.println("));");
        } else if (e instanceof FlowEdge.JumpEdge) {
            var je = (FlowEdge.JumpEdge) e;
            sw.print("cfg.addEdge(new FlowEdge.JumpEdge(");
            sw.print("cb");
            sw.print(Integer.toString(je.getSource().id()));
            sw.print(", cb");
            sw.print(Integer.toString(je.getDestination().id()));
            sw.print(", FlowEdge.Kind.");
            sw.print(je.kind().name());
            sw.println("));");
        } else {
            throw new UnsupportedOperationException("Unimplemented flow edge " + e.getClass().getSimpleName());
        }
    }

    static class PrintingCodeVisitor implements CodeVisitor {
        private final String cb;
        private final IndentedStringWriter sw;

        PrintingCodeVisitor(String cb, IndentedStringWriter sw) {
            this.cb = cb;
            this.sw = sw;
        }

        @Override
        public void visitAssignParamStmt(AssignParamStmt s) {
            sw.print(cb);
            sw.print(".appendStmt(new AssignParamStmt(");
            visitLocal(s.var());
            sw.println("));");
        }

        @Override
        public void visitAssignPhiStmt(AssignPhiStmt s) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visitAny(CodeUnit u) {
            // This page intentionally left blank.
        }

        @Override
        public void visitAssignArrayStmt(AssignArrayStmt s) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visitAssignCatchStmt(AssignCatchStmt s) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visitAssignFieldStmt(AssignFieldStmt s) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visitAssignLocalStmt(AssignLocalStmt s) {
            sw.print(cb);
            sw.print(".appendStmt(new AssignLocalStmt(");
            visitLocal(s.var());
            sw.print(", ");
            s.value().accept(this);
            sw.println("));");
        }

        @Override
        public void visitConsumeStmt(ConsumeStmt s) {
            sw.print(cb);
            sw.print(".appendStmt(new ConsumeStmt(");
            s.expr().accept(this);
            sw.println("));");
        }

        @Override
        public void visitJumpUncondStmt(JumpUncondStmt s) {
            sw.print(cb);
            sw.print(".appendStmt(new JumpUncondStmt(");
            sw.print("cb" + s.target().id());
            sw.println("));");
        }

        @Override
        public void visitMonitorStmt(MonitorStmt s) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visitJumpCondStmt(JumpCondStmt s) {
            sw.print(cb);
            sw.print(".appendStmt(new JumpCondStmt(");
            s.lhs().accept(this);
            sw.print(", ");
            s.rhs().accept(this);
            sw.print(", JumpCondStmt.Mode.");
            sw.print(s.mode().name());
            sw.print(", ");
            sw.print("cb" + s.target().id());
            sw.println("));");
        }

        @Override
        public void visitJumpSwitchStmt(JumpSwitchStmt s) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visitReturnStmt(ReturnStmt s) {
            sw.print(cb);
            sw.print(".appendStmt(new ReturnStmt(");
            s.var().accept(this);
            sw.println("));");
        }

        @Override
        public void visitThrowStmt(ThrowStmt s) {
            throw new UnsupportedOperationException();
        }

        private void visitLocal(Local local) {
            sw.print("new Local(");
            sw.print(Integer.toString(local.index()));
            sw.print(", ");
            sw.print(Boolean.toString(local.isStack()));
            sw.print(")");
        }

        @Override
        public void visitLoadFieldExpr(LoadFieldExpr e) {
            if (e instanceof LoadFieldExpr.LoadStaticFieldExpr) {
                var ee = (LoadFieldExpr.LoadStaticFieldExpr) e;
                sw.print("new LoadFieldExpr.LoadStaticFieldExpr(");
                sw.print("tm.asClassType(\"");
                sw.print(ee.owner().toString());
                sw.print("\"), \"");
                sw.print(ee.name());
                sw.print("\", ");
                sw.print("tm.asValueType(\"");
                sw.print(ee.type().toString());
                sw.print("\"))");
            } else {
                // TODO: Implement.
                throw new UnsupportedOperationException();
            }
        }

        @Override
        public void visitValueExpr(ValueExpr<?> e) {
            switch (e.opcode()) {
                case Opcodes.LOAD_CONST: {
                    var loadConst = (LoadConstExpr<?>) e;
                    sw.print("new LoadConstExpr<>(new Constant<>(");

                    switch (loadConst.value().value().getClass().getSimpleName()) {
                        case "Byte": {
                            sw.print("(byte) ");
                            break;
                        }
                        case "Short": {
                            sw.print("(short) ");
                            break;
                        }
                        default:
                            throw new IllegalArgumentException(
                                    "Unknown const type " + loadConst.value().value().getClass().getSimpleName());
                    }

                    sw.print(loadConst.value().value().toString());
                    sw.print(", tm.asValueType(\"");
                    sw.print(loadConst.value().type().toString());
                    sw.print("\")))");
                    // ?
                    break;
                }
                case Opcodes.LOAD_LOCAL: {
                    var loadLocal = (LoadLocalExpr) e;
                    sw.print("new LoadLocalExpr(");
                    sw.print("tm.asValueType(\"");
                    sw.print(loadLocal.type().toString());
                    sw.print("\"), ");
                    visitLocal(loadLocal.value());
                    sw.print(")");
                    break;
                }
                default:
                    throw new IllegalArgumentException();
            }
        }

        @Override
        public void visitAllocArrayExpr(AllocArrayExpr e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visitAllocObjectExpr(AllocObjectExpr e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visitArithmeticExpr(ArithmeticExpr e) {
            sw.print("new ArithmeticExpr(");
            sw.print("tm.asValueType(\"");
            sw.print(e.type().toString());
            sw.print("\"), ArithmeticExpr.Operation.");
            sw.print(e.op().name());
            sw.print(", ");
            e.lhs().accept(this);
            sw.print(", ");
            e.rhs().accept(this);
            sw.print(")");
        }

        @Override
        public void visitNegateExpr(NegateExpr e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visitArrayLengthExpr(ArrayLengthExpr e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visitCastExpr(CastExpr e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visitCompareExpr(CompareExpr e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visitInstanceOfExpr(InstanceOfExpr e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visitInvokeExpr(InvokeExpr e) {
            if (e instanceof InvokeExpr.InvokeVirtualExpr) {
                var v = (InvokeExpr.InvokeVirtualExpr) e;
                sw.print("new InvokeExpr.InvokeVirtualExpr(");
                sw.print("tm.asClassType(\"");
                sw.print(v.owner().toString());
                sw.print("\"), \"");
                sw.print(v.name());
                sw.print("\", tm.asMethodType(\"");
                sw.print(v.methodType().toString());
                sw.print("\"), InvokeExpr.Mode.");
                sw.print(v.mode().name());
                sw.print(", ");
                v.accessor().accept(this);
                sw.print(", List.of(");
                for (int i = 0; i < v.arguments().size(); i++) {
                    v.arguments().get(i).accept(this);
                    if (i != v.arguments().size() - 1) {
                        sw.print(", ");
                    }
                }
                sw.print("))");
            } else {
                throw new UnsupportedOperationException();
            }
        }

        @Override
        public void visitLoadArrayExpr(LoadArrayExpr e) {
            throw new UnsupportedOperationException();
        }
    }
}