package com.krakenrs.spade.ir.gen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.util.Printer;

import com.krakenrs.spade.commons.collections.tuple.Tuple3.ImmutableTuple3;
import com.krakenrs.spade.ir.code.CodeBlock;
import com.krakenrs.spade.ir.code.CodePrinter;
import com.krakenrs.spade.ir.code.ControlFlowGraph;
import com.krakenrs.spade.ir.code.ExceptionRange;
import com.krakenrs.spade.ir.code.Expr;
import com.krakenrs.spade.ir.code.FlowEdge;
import com.krakenrs.spade.ir.code.FlowEdge.DefaultEdge;
import com.krakenrs.spade.ir.code.FlowEdge.ExceptionEdge;
import com.krakenrs.spade.ir.code.FlowEdge.ImmediateEdge;
import com.krakenrs.spade.ir.code.FlowEdge.JumpEdge;
import com.krakenrs.spade.ir.code.FlowEdge.SwitchEdge;
import com.krakenrs.spade.ir.code.Stmt;
import com.krakenrs.spade.ir.code.expr.AllocArrayExpr;
import com.krakenrs.spade.ir.code.expr.AllocObjectExpr;
import com.krakenrs.spade.ir.code.expr.ArithmeticExpr;
import com.krakenrs.spade.ir.code.expr.ArrayLengthExpr;
import com.krakenrs.spade.ir.code.expr.CastExpr;
import com.krakenrs.spade.ir.code.expr.CompareExpr;
import com.krakenrs.spade.ir.code.expr.InstanceOfExpr;
import com.krakenrs.spade.ir.code.expr.InvokeExpr;
import com.krakenrs.spade.ir.code.expr.LoadArrayExpr;
import com.krakenrs.spade.ir.code.expr.LoadFieldExpr;
import com.krakenrs.spade.ir.code.expr.NegateExpr;
import com.krakenrs.spade.ir.code.expr.value.LoadConstExpr;
import com.krakenrs.spade.ir.code.expr.value.LoadLocalExpr;
import com.krakenrs.spade.ir.code.expr.value.ValueExpr;
import com.krakenrs.spade.ir.code.stmt.AssignArrayStmt;
import com.krakenrs.spade.ir.code.stmt.AssignCatchStmt;
import com.krakenrs.spade.ir.code.stmt.AssignFieldStmt;
import com.krakenrs.spade.ir.code.stmt.AssignLocalStmt;
import com.krakenrs.spade.ir.code.stmt.AssignParamStmt;
import com.krakenrs.spade.ir.code.stmt.ConsumeStmt;
import com.krakenrs.spade.ir.code.stmt.JumpCondStmt;
import com.krakenrs.spade.ir.code.stmt.JumpSwitchStmt;
import com.krakenrs.spade.ir.code.stmt.JumpUncondStmt;
import com.krakenrs.spade.ir.code.stmt.MonitorStmt;
import com.krakenrs.spade.ir.code.stmt.ReturnStmt;
import com.krakenrs.spade.ir.code.stmt.ThrowStmt;
import com.krakenrs.spade.ir.gen.LocalStack.TypedLocal;
import com.krakenrs.spade.ir.type.ArrayType;
import com.krakenrs.spade.ir.type.ClassType;
import com.krakenrs.spade.ir.type.MethodType;
import com.krakenrs.spade.ir.type.PrimitiveType;
import com.krakenrs.spade.ir.type.ValueType;
import com.krakenrs.spade.ir.value.Constant;
import com.krakenrs.spade.ir.value.Local;

public class AsmGenerator {
    public static ControlFlowGraph run(AsmGenerationCtx ctx) {
        AsmGenerationState state = new AsmGenerationState(ctx);

        ctx.executeStage("GenerateEntry", state::generateEntry);
        ctx.executeStage("GenerateHandlers", state::generateHandlers);
        ctx.executeStage("ProcessQueue", state::processQueue);
        ctx.executeStage("EnsureMarks", state::ensureMarks);
        
        List<CodeBlock> codeOrderedBlocks = ctx.executeStage("GetOrderedBlocks", state::getCodeOrderedBlocks);

        ctx.executeStage("GenerateProtectedRanges", () -> state.generateProtectedRanges(codeOrderedBlocks));
        ctx.executeStage("SplitHandlers", state::splitHandlers);

        SsaPass ssa = new SsaPass(ctx);
        ctx.executeStage("SsaTransform", ssa::doTransform);

        return state.graph;
    }

    static class AsmGenerationState {
        final AsmGenerationCtx ctx;

        final Set<CodeBlock> finished;
        final Set<CodeBlock> stacks;
        final Map<CodeBlock, LocalStack> inputStacks;

        final Set<LabelNode> marks;
        final Deque<LabelNode> queue;

        final Map<LabelNode, CodeBlock> labelBlock;
        final Map<CodeBlock, LabelNode> blockLabel;

        final Map<String, ExceptionRange> ranges;
        
        final ControlFlowGraph graph;

        AsmGenerationState(AsmGenerationCtx ctx) {
            this.ctx = ctx;

            this.finished = new HashSet<>();
            this.stacks = new HashSet<>();
            this.inputStacks = new HashMap<>();

            this.marks = new HashSet<>();
            this.queue = new LinkedList<>();

            this.labelBlock = new HashMap<>();
            this.blockLabel = new HashMap<>();
            
            this.ranges = new HashMap<>();

            this.graph = ctx.getGraph();

            // generateEntry
            // generateHandlers
            // processQueue
            // ensureMarks
            // reorderBlocks
            // generateProtectedRanges
            // splitHandlers
        }

        void enqueue(LabelNode label) {
            if (!queue.contains(label)) {
                queue.addLast(label);
            }
        }

        void processQueue() {
            while (!queue.isEmpty()) {
                process(queue.removeFirst());
            }
        }

        CodeBlock preprocess(LabelNode label) {
            /* it may not be properly initialised yet, however. */
            CodeBlock block = labelBlock.get(label);

            /* if it is, we don't need to process it */
            if (block != null && finished.contains(block)) {
                return null;
            } else if (block == null) {
                block = getOrMakeBlock(label);
            } else {
                /* i.e. not finished */
            }

            finished.add(block);
            stacks.add(block);

            return block;
        }

        void process(LabelNode label) {
            CodeBlock block = preprocess(label);
            if (block == null)
                return;
            
            AsmInterpCtx interpCtx = new AsmInterpCtx(block, inputStacks.get(block).copy());

            ctx.getLogger().trace(" Processing block {}", block.id());

            InsnList insns = ctx.getMethod().instructions;
            for (int i = insns.indexOf(label) + 1, codeLen = insns.size(); i < codeLen; i++) {
                AbstractInsnNode insn = insns.get(i);
                
                if (insn.getOpcode() != -1) {
                    ctx.getLogger().trace(" Executing: {}", Printer.OPCODES[insn.getOpcode()]);
                    interpCtx.execute(insn);
                }

                // updateTransitions returns true if the current instruction is terminal for this block
                if (updateTransitions(interpCtx, insns, i, insn)) {
                    break;
                }
            }

            updateSuccessorStacks(interpCtx);
        }

        void updateSuccessorStacks(AsmInterpCtx interpCtx) {
            // graph.getEdgeStream(interpCtx.block, FlowEdge.Kind.IMMEDIATE)
            graph.getEdges(interpCtx.block).forEach(e -> {
                updateTargetStack(interpCtx, e.getDestination());
            });
        }

        void addEdge(FlowEdge e) {
            ctx.getLogger().trace(" Edge: {}", e);
            graph.addEdge(e);
        }

        boolean updateTransitions(AsmInterpCtx interpCtx, InsnList insns, int codeIndex, AbstractInsnNode insn) {
            int opcode = insn.getOpcode(), type = insn.getType();
            switch (type) {
                case AbstractInsnNode.LABEL: {
                    // Found a new label, blockify and enqueue
                    CodeBlock succ = getOrMakeBlock((LabelNode) insn);
                    addEdge(new ImmediateEdge(interpCtx.block, succ));
                    return true;
                }
                case AbstractInsnNode.JUMP_INSN: {
                    CodeBlock target = getOrMakeBlock(((JumpInsnNode) insn).label);
                    if(opcode == Opcodes.JSR) {
                        throw new UnsupportedOperationException();
                    } else if(opcode == Opcodes.GOTO) {
                        addEdge(new JumpEdge(interpCtx.block, target, FlowEdge.Kind.UNCONDITIONAL));
                    } else {
                        addEdge(new JumpEdge(interpCtx.block, target, FlowEdge.Kind.CONDITIONAL));

                        // We need a fall through label for the false branch. If it doesn't exist for some reason
                        // e.g. there are no jump offsets that target it, then we create a block at this point.
                        AbstractInsnNode nextInsn = insns.get(codeIndex + 1);
                        if(!(nextInsn instanceof LabelNode)) {
                            LabelNode immediateLabel = new LabelNode();
                            insns.insert(insn, immediateLabel);
                            nextInsn = immediateLabel;
                        }
                        CodeBlock immediateSucc = getOrMakeBlock((LabelNode) nextInsn);
                        addEdge(new ImmediateEdge(interpCtx.block, immediateSucc));
                    }
                    return true;
                }
                case AbstractInsnNode.LOOKUPSWITCH_INSN: {
                    LookupSwitchInsnNode lsin = (LookupSwitchInsnNode) insn;
                    for (int i = 0; i < lsin.keys.size(); i++) {
                        CodeBlock target = getOrMakeBlock(lsin.labels.get(i));
                        addEdge(new SwitchEdge(interpCtx.block, target, lsin.keys.get(i)));
                    }
                    CodeBlock defaultTarget = getOrMakeBlock(lsin.dflt);
                    addEdge(new DefaultEdge(interpCtx.block, defaultTarget));
                    return true;
                }
                case AbstractInsnNode.TABLESWITCH_INSN: {
                    TableSwitchInsnNode tsin = (TableSwitchInsnNode) insn;
                    for (int i = tsin.min; i <= tsin.max; i++) {
                        CodeBlock target = getOrMakeBlock(tsin.labels.get(i - tsin.min));
                        addEdge(new SwitchEdge(interpCtx.block, target, i));
                    }
                    CodeBlock defaultTarget = getOrMakeBlock(tsin.dflt);
                    addEdge(new DefaultEdge(interpCtx.block, defaultTarget));
                    return true;
                }
                default: {
                    switch (opcode) {
                        case Opcodes.RET:
                            throw new IllegalArgumentException();
                        case Opcodes.ATHROW:
                        case Opcodes.RETURN:
                        case Opcodes.IRETURN:
                        case Opcodes.LRETURN:
                        case Opcodes.FRETURN:
                        case Opcodes.DRETURN:
                        case Opcodes.ARETURN:
                            return true;
                        default:
                            return false;
                    }
                }
            }
        }

        void setInputStack(CodeBlock block, LocalStack stack) {
            if (inputStacks.containsKey(block)) {
                throw new IllegalStateException();
            } else {
                inputStacks.put(block, stack);
            }
        }

        void updateTargetStack(AsmInterpCtx interpCtx, CodeBlock target) {
            LocalStack currentStack = interpCtx.stack;
            if (!stacks.contains(target)) {
                /* No input stack is set for the target block, just set it to the output
                 * stack of the current block. */
                setInputStack(target, currentStack.copy());
                stacks.add(target);
                if (!queue.contains(blockLabel.get(target))) {
                    throw new RuntimeException("assert");
                }
            } else if (!canSucceed(inputStacks.get(target), currentStack)) {
                throw new IllegalStateException();
            }
        }

        boolean canSucceed(LocalStack src0, LocalStack dst0) {
            if (src0.height() != dst0.height())
                return false;

            LocalStack src = src0.copy(), dst = dst0.copy();
            while (src.height() > 0) {
                TypedLocal t0 = src.pop(), t1 = dst.pop();

                if (!t0.getA().equals(t1.getA())) {
                    return false;
                } else if (t0.getB().getSize() != t1.getB().getSize()) {
                    return false;
                }
            }
            return src.height() == 0 && dst.height() == 0;
        }

        CodeBlock getOrMakeBlock(LabelNode label) {
            CodeBlock block = labelBlock.get(label);
            if (block == null) {
                block = graph.makeBlock();
                graph.addVertex(block);
                blockLabel.put(block, label);
                labelBlock.put(label, block);
                enqueue(label);
                labelBlock.put(label, block);
            }
            return block;
        }

        LabelNode getInitialLabel() {
            InsnList insns = ctx.getMethod().instructions;
            AbstractInsnNode first = insns.getFirst();
            if (first == null) {
                first = new LabelNode();
                insns.add(first);
            } else if (!(first instanceof LabelNode)) {
                LabelNode newFirstInsn = new LabelNode();
                insns.insertBefore(first, newFirstInsn);
                first = newFirstInsn;
            }
            return (LabelNode) first;
        }

        void generateEntry() {
            CodeBlock entryBlock = graph.getEntryBlock();
            generateParameters(entryBlock);
            // Stack is initially empty on method entry
            setInputStack(entryBlock, new LocalStack(16));

            /* The entry of the actual control flow graph instance is a graph intrinsic node
             * (via cfg.getEntryBlock()). we use that entry node for the synthetic parameter
             * declarations and any other of our own initialisation code so that it does not
             * interfere with the real method code at all. therefore we create a new block
             * (actually this is the second block added to the graph) to correspond to the
             * actual code. */

            LabelNode initialCodeLabel = getInitialLabel();
            CodeBlock initialCodeBlock = getOrMakeBlock(initialCodeLabel);
            setInputStack(initialCodeBlock, new LocalStack(16));
            addEdge(new ImmediateEdge(entryBlock, initialCodeBlock));
        }

        void generateParameters(CodeBlock block) {
            int index = 0;
            if (!ctx.isStaticMethod()) {
                block.appendStmt(new AssignParamStmt(new Local(index++, false)));
            }
            for (ValueType paramType : ctx.getMethodType().getParamTypes()) {
                block.appendStmt(new AssignParamStmt(new Local(index, false)));
                index += paramType.getSize();
            }
        }

        void generateHandler(TryCatchBlockNode tcbn) {
            CodeBlock handlerBlock = getOrMakeBlock(tcbn.handler);

            marks.add(tcbn.start);
            marks.add(tcbn.end);

            LocalStack handlerStack = new LocalStack();
            {
                // Setup handler stack and handler block catch var
                Local svar0 = new Local(0, true);
                ClassType catchClassType;
                if (tcbn.type != null) {
                    catchClassType = ctx.getTypeManager().asClassType(tcbn.type);
                } else {
                    catchClassType = ctx.getTypeManager().asClassType(Throwable.class);
                }
                ValueType catchType = catchClassType.asValueType();

                handlerStack.push(new TypedLocal(svar0, catchType));
                handlerBlock.appendStmt(new AssignCatchStmt(svar0, catchClassType));
            }
            setInputStack(handlerBlock, handlerStack);

            // Mark handler block stack as finalised
            stacks.add(handlerBlock);
            enqueue(tcbn.handler);
        }

        void generateHandlers() {
            ctx.getMethod().tryCatchBlocks.forEach(this::generateHandler);
        }

        List<CodeBlock> computeProtectedRange(List<CodeBlock> orderedBlocks, int start, int end) {
            return orderedBlocks.subList(start, end + 1);
        }

        ExceptionRange getOrCreateRange(List<CodeBlock> range, CodeBlock handlerBlock, ClassType catchType,
                String key) {
            ExceptionRange erange;
            if (ranges.containsKey(key)) {
                erange = ranges.get(key);
            } else {
                erange = new ExceptionRange();
                ranges.put(key, erange);

                erange.setHandler(handlerBlock);
                erange.addProtectedBlocks(range);
                graph.addExceptionRange(erange);
            }
            erange.addCatchType(catchType);
            return erange;
        }

        void generateProtectedRange(List<CodeBlock> order, TryCatchBlockNode tc) {
            if (tc.start == tc.end)
                return;

            int start = order.indexOf(labelBlock.get(tc.start));
            int end = order.indexOf(labelBlock.get(tc.end)) - 1;
            CodeBlock handlerBlock = labelBlock.get(tc.handler);
            int handler = order.indexOf(handlerBlock);
            List<CodeBlock> range = computeProtectedRange(order, start, end);
            String key = String.format("%d:%d:%d", start, end, handler);

            ExceptionRange erange = getOrCreateRange(order, handlerBlock, ctx.getTypeManager().asClassType(tc.type),
                    key);

            range.forEach(block -> addEdge(new ExceptionEdge(block, erange.handler(), erange)));
        }

        void generateProtectedRanges(List<CodeBlock> order) {
            ctx.getMethod().tryCatchBlocks.stream().forEach(tc -> generateProtectedRange(order, tc));
        }

        void ensureMarks() {
            marks.forEach(this::getOrMakeBlock);
        }

        int codeIndexOf(CodeBlock block) {
            return ctx.getMethod().instructions.indexOf(blockLabel.get(block));
        }

        List<CodeBlock> getCodeOrderedBlocks() {
            CodeBlock entryBlock = graph.getEntryBlock();
            List<CodeBlock> blocks = new ArrayList<>(graph.getVertices());
            
            blocks.sort((a, b) -> {
                if (entryBlock.equals(a)) {
                    return -1;
                } else if (entryBlock.equals(b)) {
                    return 1;
                } else {
                    return Integer.compare(codeIndexOf(a), codeIndexOf(b));
                }
            });

            for (int i = 0; i < blocks.size(); i++) {
                blocks.get(i).setOrderHint(i);
            }

            return blocks;
        }

        boolean hasForcedJump(CodeBlock target) {
            Optional<FlowEdge> anyEdge = graph.getReverseEdges(target).stream()
                    .filter(e -> !e.kind().equals(FlowEdge.Kind.EXCEPTION)).findAny();
            return anyEdge.isPresent();
        }

        CodeBlock makeHandlerBlock(ClassType catchType) {
            CodeBlock block = graph.makeBlock();
            graph.addVertex(block);
            Local svar0 = new Local(0, true);
            block.appendStmt(new AssignCatchStmt(svar0, catchType));
            return block;
        }

        void redirectExceptions(CodeBlock oldHandler, CodeBlock newHandler) {
            for(FlowEdge e : graph.getReverseEdges(oldHandler)) {
                if(e.kind().equals(FlowEdge.Kind.EXCEPTION)) {
                    ExceptionEdge ee = (ExceptionEdge) e;
                    addEdge(new ExceptionEdge(e.getSource(), newHandler, ee.range()));
                    graph.removeEdge(e);
                }
            }
        }

        void splitHandlers() {
            /* Here we decapitate handler blocks such that the catch copy is in it's own block
             * and the main body catch body is in a separate block. Consider the following code:
             * 
             * L0: svar0 = "no exception"
             *     goto L2
             * L1: exception()
             *     goto L3
             * L2: svar0 = catch()
             *     System.out.println(svar0)
             * L3: return
             * 
             * Where [L1, L2] is protected by L2. The exception() trap causes an exception to be
             * thrown, however, if the branch from L0 to L2 is taken with svar0 being live into
             * L2, the the wrong value of svar0 is printed.
             * 
             * Therefore moving the println call into another block L2_2: and redirecting the goto L2
             * statement to this new block is the approach we take (while keeping the original L2 block
             * as the handler for the protected region). */

            Map<CodeBlock, CodeBlock> splitHandlers = new HashMap<>();
            
            for (ExceptionRange erange : ranges.values()) {
                CodeBlock realHandler = erange.handler();
                
                // Already processed or don't need to split
                if (splitHandlers.containsKey(realHandler) || !hasForcedJump(realHandler))
                    continue;

                // Create decapitated head block
                CodeBlock newHandler = makeHandlerBlock(ctx.getTypeManager().lca(erange.catchTypes()));
                erange.setHandler(newHandler);
                splitHandlers.put(realHandler, newHandler);

                // Delete original catch copy
                realHandler.removeStmt(0);

                // Redirect incoming exception edges from the old handler to the new one
                redirectExceptions(realHandler, newHandler);

                // Connect decapitated head so that it flows into the catch body
                addEdge(new ImmediateEdge(realHandler, newHandler));
            }

            /* Protected ranges and handlers may be nested inside of other protected ranges, so we need to
             * add the decapitated head blocks into ranges that protected the original handler block. */

            for (ExceptionRange erange : ranges.values()) {
                for (Entry<CodeBlock, CodeBlock> remappedHandlers : splitHandlers.entrySet()) {
                    if (erange.containsProtectedBlock(remappedHandlers.getKey())) {
                        // This handler was inside the current range, so the new handler also is
                        erange.addProtectedBlock(remappedHandlers.getValue());
                    }
                }
            }
        }

        class AsmInterpCtx {
            final CodeBlock block;
            final LocalStack stack;

            AsmInterpCtx(CodeBlock block, LocalStack stack) {
                this.block = block;
                this.stack = stack;
            }

            void verifyStack() {
                if (stack.isEmpty()) {
                    return;
                }

                // Check each stack variable index is contiguous
                for (int i = stack.size() - 1, vIndex = 0; i >= 0; i--, vIndex++) {
                    TypedLocal l = stack.peek(i);
                    if (l.getA().index() != vIndex || !l.getA().isStack())
                        throw new IllegalStateException();
                    if (l.getB().equals(PrimitiveType.VOID)) {
                        throw new IllegalStateException();
                    }
                }
            }

            void verifyOperation(Runnable operation) {
                verifyStack();
                operation.run();
                verifyStack();
            }
            
            void push(Expr e) {
                verifyOperation(() -> _push(e));
            }
            
            void addStmt(Stmt stmt) {
                block.appendStmt(stmt);
                ctx.getLogger().trace("    Stmt: {}", CodePrinter.toString(stmt));
            }

            void _push(Expr e) {
                Local nextSvar = new Local(stack.size(), true);

                if (e.opcode() == com.krakenrs.spade.ir.code.Opcodes.LOAD_LOCAL) {
                    LoadLocalExpr lle = (LoadLocalExpr) e;
                    if (lle.value().equals(nextSvar)) {
                        // Don't need to generate x = x copy
                        stack.push(new TypedLocal(nextSvar, e.type()));
                        return;
                    }
                }

                // Generate svarY = e for stack size Y
                addStmt(new AssignLocalStmt(nextSvar, e));
                stack.push(new TypedLocal(nextSvar, e.type()));

                ctx.getLogger().trace("    FinalStack: {}", stack);
            }

            LoadLocalExpr pop() {
                TypedLocal tl = stack.pop();
                return new LoadLocalExpr(tl.getB(), tl.getA());
            }

            <T> void _const(T cst, ValueType type) {
                push(new LoadConstExpr<>(new Constant<>(cst, type)));
            }

            void _store(int index, ValueType type) {
                addStmt(new AssignLocalStmt(new Local(index, false), pop()));
            }

            void _load(int index, ValueType type) {
                push(new LoadLocalExpr(type, new Local(index, false)));
            }

            void _compare(CompareExpr.Operation op) {
                LoadLocalExpr rhs = pop(), lhs = pop();
                push(new CompareExpr(lhs, rhs, op));
            }

            void _inc(int index, int amt) {
                // Doesn't affect stack at all :D
                Local var = new Local(index, false);
                ArithmeticExpr addExpr = new ArithmeticExpr(PrimitiveType.INT, ArithmeticExpr.Operation.ADD,
                        new LoadLocalExpr(PrimitiveType.INT, var),
                        new LoadConstExpr<>(new Constant<>(amt, PrimitiveType.INT)));
                addStmt(new AssignLocalStmt(var, addExpr));
            }

            void _arithmetic(ArithmeticExpr.Operation op, ValueType type) {
                LoadLocalExpr rhs = pop(), lhs = pop();
                push(new ArithmeticExpr(type, op, lhs, rhs));
            }

            void _negate() {
                push(new NegateExpr(pop()));
            }

            void _arraylength() {
                push(new ArrayLengthExpr(pop()));
            }

            void _loadarray(ValueType elementType) {
                LoadLocalExpr index = pop(), array = pop();
                push(new LoadArrayExpr(elementType, array, index));
            }

            void _storearray(ValueType elementType) {
                LoadLocalExpr value = pop(), index = pop(), array = pop();
                addStmt(new AssignArrayStmt(array, index, value));
            }
            
            void _jmp_cmp(CodeBlock target, JumpCondStmt.Mode mode, ValueExpr<?> lhs, ValueExpr<?> rhs) {
                addStmt(new JumpCondStmt(lhs, rhs, mode, target));
            }

            void _jmp_cmp(CodeBlock target, JumpCondStmt.Mode mode) {
                LoadLocalExpr rhs = pop(), lhs = pop();
                _jmp_cmp(target, mode, lhs, rhs);
            }

            void _jmp_cmp0(CodeBlock target, JumpCondStmt.Mode mode) {
                _jmp_cmp(target, mode, pop(), new LoadConstExpr<>(new Constant<>((byte) 0, PrimitiveType.BYTE)));
            }

            void _jmp_uncond(CodeBlock target) {
                addStmt(new JumpUncondStmt(target));
            }

            void _jmp_null(CodeBlock target, boolean eq) {
                JumpCondStmt.Mode mode = eq ? JumpCondStmt.Mode.EQ : JumpCondStmt.Mode.NE;
                _jmp_cmp(target, mode, pop(), new LoadConstExpr<>(new Constant<>(null, PrimitiveType.NULL)));
            }

            void _switch(Map<Integer, CodeBlock> cases, CodeBlock defaultTarget) {
                addStmt(new JumpSwitchStmt(pop(), cases, defaultTarget));
            }

            void _loadfield(String owner, String name, String desc, boolean isStatic) {
                LoadFieldExpr lfe;
                ClassType ownerType = ctx.getTypeManager().asClassType(owner);
                ValueType fieldType = ctx.getTypeManager().asValueType(desc);
                if (isStatic) {
                    lfe = new LoadFieldExpr.LoadStaticFieldExpr(ownerType, name, fieldType);
                } else {
                    lfe = new LoadFieldExpr.LoadVirtualFieldExpr(ownerType, name, fieldType, pop());
                }
                push(lfe);
            }

            void _storefield(String owner, String name, String desc, boolean isStatic) {
                AssignFieldStmt afs;
                ClassType ownerType = ctx.getTypeManager().asClassType(owner);
                ValueType fieldType = ctx.getTypeManager().asValueType(desc);
                LoadLocalExpr value = pop();
                if(isStatic) {
                    afs = new AssignFieldStmt.AssignStaticFieldStmt(ownerType, name, fieldType, value);
                } else {
                    afs = new AssignFieldStmt.AssignVirtualFieldStmt(ownerType, name, fieldType, value, pop());
                }
                addStmt(afs);
            }

            void _return(ValueType type) {
                LoadLocalExpr val = null;
                if (!type.equals(PrimitiveType.VOID)) {
                    val = pop();
                }
                addStmt(new ReturnStmt(val));
            }

            void _throw() {
                addStmt(new ThrowStmt(pop()));
            }

            void _monitor(MonitorStmt.Mode mode) {
                addStmt(new MonitorStmt(pop(), mode));
            }

            void _pop() {
                pop();
            }

            void _pop2() {
                if (stack.peek().getB().getSize() == 2) {
                    _pop();
                } else {
                    _pop();
                    _pop();
                }
            }

            void _dup() {
                TypedLocal topE = stack.peek();
                push(new LoadLocalExpr(topE.getB(), topE.getA()));
            }

            void _dup2() {
                if (stack.peek().getB().getSize() == 2) {
                    _dup();
                } else {
                    _dup();
                    _dup();
                }
            }

            void __dupx(int blockWidth, int offset) {
                Set<TypedLocal> queuedLocals = new TreeSet<>(
                        (t1, t2) -> Integer.compare(t1.getA().index(), t2.getA().index()));
                for (ImmutableTuple3<Integer, Integer, ValueType> e : CopyHelper.dupx(stack, blockWidth,
                        offset)) {
                    Local dst = new Local(e.getA(), true);
                    Local src = new Local(e.getB(), true);
                    addStmt(new AssignLocalStmt(dst, new LoadLocalExpr(e.getC(), src)));
                    if (e.getA() >= stack.size()) {
                        queuedLocals.add(new TypedLocal(dst, e.getC()));
                    }
                }
                for (TypedLocal t : queuedLocals) {
                    stack.push(t);
                }
            }

            void _dupx(int blockWidth, int offset) {
                verifyOperation(() -> __dupx(blockWidth, offset));
            }

            void _swap() {
                Local temp = new Local(stack.size(), true);
                // [var1, var0,...] => [var0, var1,...]
                TypedLocal var1 = stack.pop();
                TypedLocal var0 = stack.pop();
                addStmt(new AssignLocalStmt(temp, new LoadLocalExpr(var0.getB(), var0.getA())));
                addStmt(new AssignLocalStmt(var0.getA(), new LoadLocalExpr(var1.getB(), var1.getA())));
                addStmt(new AssignLocalStmt(var1.getA(), new LoadLocalExpr(var0.getB(), temp)));
                stack.push(var1);
                stack.push(var0);
            }

            void _cast(ValueType type) {
                push(new CastExpr(type, pop()));
            }

            void _instanceof(ValueType type) {
                push(new InstanceOfExpr(pop(), type));
            }

            void _new(ClassType type) {
                push(new AllocObjectExpr(type));
            }
            
            void _pushInvoke(InvokeExpr e) {
                if (e.methodType().getReturnType().equals(PrimitiveType.VOID)) {
                    addStmt(new ConsumeStmt(e));
                } else {
                    push(e);
                }
            }

            void _callStatic(String owner, String name, String desc) {
                MethodType type = ctx.getTypeManager().asMethodType(desc);
                ValueExpr<?>[] args = new ValueExpr<?>[type.getParamTypes().size()];
                for (int i = args.length - 1; i >= 0; i--) {
                    args[i] = pop();
                }
                ClassType ownerType = ctx.getTypeManager().asClassType(owner);
                _pushInvoke(new InvokeExpr.InvokeStaticExpr(ownerType, name, type, Arrays.asList(args)));
            }

            void _callVirtual(InvokeExpr.Mode mode, String owner, String name, String desc) {
                MethodType type = ctx.getTypeManager().asMethodType(desc);
                ValueExpr<?>[] args = new ValueExpr<?>[type.getParamTypes().size()];
                for (int i = args.length - 1; i >= 0; i--) {
                    args[i] = pop();
                }
                LoadLocalExpr instance = pop();
                ClassType ownerType = ctx.getTypeManager().asClassType(owner);
                _pushInvoke(
                        new InvokeExpr.InvokeVirtualExpr(ownerType, name, type, mode, instance, Arrays.asList(args)));
            }

            void execute(AbstractInsnNode insn) {
                int opcode = insn.getOpcode();
                if (opcode == -1 && insn.getType() == AbstractInsnNode.LABEL) {
                    throw new IllegalStateException();
                }

                switch (opcode) {
                    case Opcodes.BIPUSH:
                        _const((byte) ((IntInsnNode) insn).operand, PrimitiveType.BYTE);
                        break;
                    case Opcodes.SIPUSH:
                        _const((short) ((IntInsnNode) insn).operand, PrimitiveType.SHORT);
                        break;
                    case Opcodes.ACONST_NULL:
                        _const(null, PrimitiveType.NULL);
                        break;
                    case Opcodes.ICONST_M1:
                    case Opcodes.ICONST_0:
                    case Opcodes.ICONST_1:
                    case Opcodes.ICONST_2:
                    case Opcodes.ICONST_3:
                    case Opcodes.ICONST_4:
                    case Opcodes.ICONST_5:
                        _const((byte) (opcode - Opcodes.ICONST_M1 - 1), PrimitiveType.BYTE);
                        break;
                    case Opcodes.LCONST_0:
                    case Opcodes.LCONST_1:
                        _const((long) (opcode - Opcodes.LCONST_0), PrimitiveType.LONG);
                        break;
                    case Opcodes.FCONST_0:
                    case Opcodes.FCONST_1:
                    case Opcodes.FCONST_2:
                        _const((float) (opcode - Opcodes.FCONST_0), PrimitiveType.FLOAT);
                        break;
                    case Opcodes.DCONST_0:
                    case Opcodes.DCONST_1:
                        _const((double) (opcode - Opcodes.DCONST_0), PrimitiveType.DOUBLE);
                        break;
                    case Opcodes.LDC: {
                        Object cst = ((LdcInsnNode) insn).cst;
                        if (cst instanceof Type) {
                            ValueType cstType;
                            // ClassRef constant entry gets turned into:
                            //  cst = ClassType(classRef)
                            //  cstType = ObjectType(Class.class)
                            Type refCst = (Type) cst;
                            if (refCst.getSort() == Type.OBJECT) {
                                cst = ctx.getTypeManager().asClassType(refCst.getClassName());
                                cstType = ctx.getTypeManager().asClassType(Class.class).asValueType();
                            } else {
                                throw new UnsupportedOperationException(
                                        cst + " :: " + refCst.toString() + " | " + refCst.getSort());
                            }
                            _const(cst, cstType);
                        } else {
                            _const(cst, TypeHelper.getValueType(ctx.getTypeManager(), cst));
                        }
                        break;
                    }
                    case Opcodes.LCMP:
                        _compare(CompareExpr.Operation.NONE);
                        break;
                    case Opcodes.FCMPG:
                    case Opcodes.DCMPG:
                        _compare(CompareExpr.Operation.GT);
                        break;
                    case Opcodes.FCMPL:
                    case Opcodes.DCMPL:
                        _compare(CompareExpr.Operation.LT);
                        break;
                    case Opcodes.NEWARRAY: {
                        IntInsnNode iin = (IntInsnNode) insn;
                        PrimitiveType elementType = TypeHelper.getPrimitiveType(iin.operand);
                        push(new AllocArrayExpr(ctx.getTypeManager().asArrayType(elementType, 1),
                                Arrays.asList(pop())));
                        break;
                    }
                    case Opcodes.ANEWARRAY: {
                        TypeInsnNode tin = (TypeInsnNode) insn;
                        ArrayType type;
                        if (tin.desc.startsWith("[")) {
                            type = (ArrayType) ctx.getTypeManager().asValueType(tin.desc);
                        } else {
                            type = ctx.getTypeManager()
                                    .asArrayType(ctx.getTypeManager().asClassType(tin.desc).asValueType(), 1);
                        }
                        push(new AllocArrayExpr(type, Arrays.asList(pop())));
                        break;
                    }
                    case Opcodes.MULTIANEWARRAY: {
                        MultiANewArrayInsnNode main = (MultiANewArrayInsnNode) insn;
                        ValueExpr<?>[] bounds = new ValueExpr<?>[main.dims];
                        for (int i = bounds.length - 1; i >= 0; i--) {
                            bounds[i] = pop();
                        }
                        ArrayType arrayType = (ArrayType) ctx.getTypeManager().asValueType(main.desc);
                        push(new AllocArrayExpr(arrayType, Arrays.asList(bounds)));
                        break;
                    }
                    case Opcodes.ILOAD:
                    case Opcodes.LLOAD:
                    case Opcodes.FLOAD:
                    case Opcodes.DLOAD:
                    case Opcodes.ALOAD:
                        _load(((VarInsnNode) insn).var, TypeHelper.getLoadType(ctx.getTypeManager(), opcode));
                        break;
                    case Opcodes.ISTORE:
                    case Opcodes.LSTORE:
                    case Opcodes.FSTORE:
                    case Opcodes.DSTORE:
                    case Opcodes.ASTORE:
                        _store(((VarInsnNode) insn).var, TypeHelper.getStoreType(ctx.getTypeManager(), opcode));
                        break;
                    case Opcodes.IINC: {
                        IincInsnNode iinc = (IincInsnNode) insn;
                        _inc(iinc.var, iinc.incr);
                        break;
                    }
                    case Opcodes.IADD:
                    case Opcodes.LADD:
                    case Opcodes.FADD:
                    case Opcodes.DADD:
                        _arithmetic(ArithmeticExpr.Operation.ADD, TypeHelper.getArithmeticType(opcode));
                        break;
                    case Opcodes.ISUB:
                    case Opcodes.LSUB:
                    case Opcodes.FSUB:
                    case Opcodes.DSUB:
                        _arithmetic(ArithmeticExpr.Operation.SUB, TypeHelper.getArithmeticType(opcode));
                        break;
                    case Opcodes.IDIV:
                    case Opcodes.LDIV:
                    case Opcodes.FDIV:
                    case Opcodes.DDIV:
                        _arithmetic(ArithmeticExpr.Operation.DIV, TypeHelper.getArithmeticType(opcode));
                        break;
                    case Opcodes.IMUL:
                    case Opcodes.LMUL:
                    case Opcodes.FMUL:
                    case Opcodes.DMUL:
                        _arithmetic(ArithmeticExpr.Operation.MUL, TypeHelper.getArithmeticType(opcode));
                        break;
                    case Opcodes.IREM:
                    case Opcodes.LREM:
                    case Opcodes.FREM:
                    case Opcodes.DREM:
                        _arithmetic(ArithmeticExpr.Operation.MOD, TypeHelper.getArithmeticType(opcode));
                        break;
                    case Opcodes.ISHL:
                    case Opcodes.LSHL:
                        _arithmetic(ArithmeticExpr.Operation.SHL, TypeHelper.getArithmeticType(opcode));
                        break;
                    case Opcodes.ISHR:
                    case Opcodes.LSHR:
                        _arithmetic(ArithmeticExpr.Operation.SHR, TypeHelper.getArithmeticType(opcode));
                        break;
                    case Opcodes.IUSHR:
                    case Opcodes.LUSHR:
                        _arithmetic(ArithmeticExpr.Operation.USHR, TypeHelper.getArithmeticType(opcode));
                        break;
                    case Opcodes.IAND:
                    case Opcodes.LAND:
                        _arithmetic(ArithmeticExpr.Operation.AND, TypeHelper.getArithmeticType(opcode));
                        break;
                    case Opcodes.IOR:
                    case Opcodes.LOR:
                        _arithmetic(ArithmeticExpr.Operation.OR, TypeHelper.getArithmeticType(opcode));
                        break;
                    case Opcodes.IXOR:
                    case Opcodes.LXOR:
                        _arithmetic(ArithmeticExpr.Operation.XOR, TypeHelper.getArithmeticType(opcode));
                        break;
                    case Opcodes.INEG:
                    case Opcodes.LNEG:
                    case Opcodes.DNEG:
                    case Opcodes.FNEG:
                        _negate();
                        break;
                    case Opcodes.ARRAYLENGTH:
                        _arraylength();
                        break;
                    case Opcodes.IALOAD:
                    case Opcodes.LALOAD:
                    case Opcodes.FALOAD:
                    case Opcodes.DALOAD:
                    case Opcodes.AALOAD:
                    case Opcodes.BALOAD:
                    case Opcodes.CALOAD:
                    case Opcodes.SALOAD:
                        _loadarray(TypeHelper.getArrayLoadType(ctx.getTypeManager(), opcode));
                        break;
                    case Opcodes.IASTORE:
                    case Opcodes.LASTORE:
                    case Opcodes.FASTORE:
                    case Opcodes.DASTORE:
                    case Opcodes.AASTORE:
                    case Opcodes.BASTORE:
                    case Opcodes.CASTORE:
                    case Opcodes.SASTORE:
                        _storearray(TypeHelper.getArrayStoreType(ctx.getTypeManager(), opcode));
                        break;
                    case Opcodes.IFEQ:
                    case Opcodes.IFNE:
                    case Opcodes.IFLT:
                    case Opcodes.IFGE:
                    case Opcodes.IFGT:
                    case Opcodes.IFLE:
                        _jmp_cmp0(getOrMakeBlock(((JumpInsnNode) insn).label),
                                TypeHelper.getConditionalCompareMode(opcode));
                        break;
                    case Opcodes.GOTO:
                        _jmp_uncond(getOrMakeBlock(((JumpInsnNode) insn).label));
                        break;
                    case Opcodes.IFNULL:
                    case Opcodes.IFNONNULL:
                        _jmp_null(getOrMakeBlock(((JumpInsnNode) insn).label), opcode == Opcodes.IFNULL);
                        break;
                    case Opcodes.IF_ICMPEQ:
                    case Opcodes.IF_ICMPNE:
                    case Opcodes.IF_ICMPLT:
                    case Opcodes.IF_ICMPGE:
                    case Opcodes.IF_ICMPGT:
                    case Opcodes.IF_ICMPLE:
                    case Opcodes.IF_ACMPEQ:
                    case Opcodes.IF_ACMPNE:
                        _jmp_cmp(getOrMakeBlock(((JumpInsnNode) insn).label),
                                TypeHelper.getConditionalCompareMode(opcode));
                        break;
                    case Opcodes.TABLESWITCH: {
                        TableSwitchInsnNode tsin = (TableSwitchInsnNode) insn;
                        Map<Integer, CodeBlock> targets = new LinkedHashMap<>();
                        for (int i = tsin.min; i <= tsin.max; i++) {
                            CodeBlock targ = getOrMakeBlock(tsin.labels.get(i - tsin.min));
                            targets.put(i, targ);
                        }
                        _switch(targets, getOrMakeBlock(tsin.dflt));
                        break;
                    }
                    case Opcodes.LOOKUPSWITCH: {
                        LookupSwitchInsnNode lsin = (LookupSwitchInsnNode) insn;
                        LinkedHashMap<Integer, CodeBlock> targets = new LinkedHashMap<>();
                        for (int i = 0; i < lsin.keys.size(); i++) {
                            int key = lsin.keys.get(i);
                            CodeBlock targ = getOrMakeBlock(lsin.labels.get(i));
                            targets.put(key, targ);
                        }
                        _switch(targets, getOrMakeBlock(lsin.dflt));
                        break;
                    }
                    case Opcodes.GETFIELD:
                    case Opcodes.GETSTATIC: {
                        FieldInsnNode fin = (FieldInsnNode) insn;
                        _loadfield(fin.owner, fin.name, fin.desc, opcode == Opcodes.GETSTATIC);
                        break;
                    }
                    case Opcodes.PUTFIELD:
                    case Opcodes.PUTSTATIC: {
                        FieldInsnNode fin = (FieldInsnNode) insn;
                        _storefield(fin.owner, fin.name, fin.desc, opcode == Opcodes.PUTSTATIC);
                        break;
                    }
                    case Opcodes.RETURN:
                        _return(PrimitiveType.VOID);
                        break;
                    case Opcodes.ATHROW:
                        _throw();
                        break;
                    case Opcodes.MONITORENTER:
                        _monitor(MonitorStmt.Mode.ENTER);
                        break;
                    case Opcodes.MONITOREXIT:
                        _monitor(MonitorStmt.Mode.EXIT);
                        break;
                    case Opcodes.IRETURN:
                    case Opcodes.LRETURN:
                    case Opcodes.FRETURN:
                    case Opcodes.DRETURN:
                    case Opcodes.ARETURN:
                        _return(ctx.getMethodType().getReturnType());
                        break;
                    case Opcodes.POP:
                        _pop();
                        break;
                    case Opcodes.POP2:
                        _pop2();
                        break;
                    case Opcodes.DUP:
                        _dup();
                        break;
                    case Opcodes.DUP2:
                        _dup2();
                        break;
                    case Opcodes.DUP_X1:
                        _dupx(1, 1);
                        break;
                    case Opcodes.DUP_X2:
                        _dupx(1, 2);
                        break;
                    case Opcodes.DUP2_X1:
                        _dupx(2, 1);
                        break;
                    case Opcodes.DUP2_X2:
                        _dupx(2, 2);
                        break;
                    case Opcodes.SWAP:
                        _swap();
                        break;
                    case Opcodes.I2L:
                    case Opcodes.I2F:
                    case Opcodes.I2D:
                    case Opcodes.L2I:
                    case Opcodes.L2F:
                    case Opcodes.L2D:
                    case Opcodes.F2I:
                    case Opcodes.F2L:
                    case Opcodes.F2D:
                    case Opcodes.D2I:
                    case Opcodes.D2L:
                    case Opcodes.D2F:
                    case Opcodes.I2B:
                    case Opcodes.I2C:
                    case Opcodes.I2S:
                        _cast(TypeHelper.getCastType(opcode));
                        break;
                    case Opcodes.CHECKCAST: {
                        TypeInsnNode tin = (TypeInsnNode) insn;
                        if (tin.desc.startsWith("[")) {
                            _cast(ctx.getTypeManager().asValueType(tin.desc));
                        } else {
                            _cast(ctx.getTypeManager().asClassType(tin.desc).asValueType());
                        }
                        break;
                    }
                    case Opcodes.INSTANCEOF: {
                        TypeInsnNode tin = (TypeInsnNode) insn;
                        if (tin.desc.startsWith("[")) {
                            _instanceof(ctx.getTypeManager().asValueType(tin.desc));
                        } else {
                            _instanceof(ctx.getTypeManager().asClassType(tin.desc).asValueType());
                        }
                        break;
                    }
                    case Opcodes.NEW: {
                        TypeInsnNode tin = (TypeInsnNode) insn;
                        _new(ctx.getTypeManager().asClassType(tin.desc));
                        break;
                    }
                    case Opcodes.INVOKESTATIC: {
                        MethodInsnNode min = (MethodInsnNode) insn;
                        _callStatic(min.owner, min.name, min.desc);
                        break;
                    }
                    case Opcodes.INVOKEVIRTUAL:
                    case Opcodes.INVOKEINTERFACE:
                    case Opcodes.INVOKESPECIAL: {
                        MethodInsnNode min = (MethodInsnNode) insn;
                        _callVirtual(TypeHelper.getInvokeExprMode(opcode), min.owner, min.name, min.desc);
                        break;
                    }
                    case Opcodes.INVOKEDYNAMIC: {
                        InvokeDynamicInsnNode indy = (InvokeDynamicInsnNode) insn;
                        ctx.getLogger().trace("Target: {} {}", indy.name, indy.desc);
                        ctx.getLogger().trace("BSM: ", indy.bsm);
                        ctx.getLogger().trace("BSM Static Args{}: {}", indy.bsmArgs.length, indy.bsmArgs);
                    }
                    default:
                        throw new UnsupportedOperationException(
                                opcode <= Printer.OPCODES.length - 1 ? Printer.OPCODES[opcode]
                                        : String.valueOf(opcode));
                }

            }
        }
    }
}
