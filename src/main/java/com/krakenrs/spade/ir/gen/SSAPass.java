package com.krakenrs.spade.ir.gen;

import com.krakenrs.spade.commons.collections.LazyCreationHashMap;
import com.krakenrs.spade.commons.collections.graph.algo.DominatorComputer;
import com.krakenrs.spade.ir.algo.SsaBlockLivenessAnalyser;
import com.krakenrs.spade.ir.code.CodeBlock;
import com.krakenrs.spade.ir.code.ExceptionRange;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.Stmt;
import com.krakenrs.spade.ir.code.stmt.AssignPhiStmt;
import com.krakenrs.spade.ir.code.stmt.DeclareLocalStmt;
import com.krakenrs.spade.ir.value.Local;

import java.util.*;

public class SSAPass {
    private final GenerationCtx ctx;
    private final Map<Local, Set<CodeBlock>> assignments;
    private final Map<Local, Integer> counter;
    private final Set<CodeBlock> handlers;
    private final Map<CodeBlock, Integer> insertion;
    private final Set<Local> locals;
    private final Map<CodeBlock, Integer> process;
    private final Map<Local, ReachingDefinition> reachingDefs;

    private DominatorComputer<CodeBlock> dominator;
    private SsaBlockLivenessAnalyser liveness;

    public SSAPass(GenerationCtx ctx) {
        this.ctx = Objects.requireNonNull(ctx);
        assignments = new LazyCreationHashMap<>(HashSet::new);
        counter = new LazyCreationHashMap<>(() -> 0);
        handlers = new HashSet<>();
        insertion = new LazyCreationHashMap<>(() -> 0);
        locals = new HashSet<>();
        process = new LazyCreationHashMap<>(() -> 0);
        reachingDefs = new HashMap<>();
    }

    public void doTransform() {
    	ctx.executeStage("getHandlers", this::getHandlers);
    	ctx.executeStage("updateDominators", this::updateDominators);
    	ctx.executeStage("updateLiveness", this::updateLiveness);
    	ctx.executeStage("insertPhis", this::insertPhis);
    	ctx.executeStage("rename", this::rename);
    }

    private void getHandlers() {
        ctx.getGraph().getExceptionRanges().stream().map(ExceptionRange::handler).forEach(handlers::add);
    }

    private void updateDominators() {
        dominator = new DominatorComputer<>(ctx.getGraph(), ctx.getGraph().getEntryBlock(), true);
        dominator.run();
    }

    private void updateLiveness() {
        liveness = new SsaBlockLivenessAnalyser(ctx.getGraph());
    }

    private void insertPhis() {
        findLocals();

        int i = 0;
        for (var local : locals) {
            i++;

            var q = new LinkedList<CodeBlock>();
            for (CodeBlock block : assignments.get(local)) {
                process.put(block, i);
                q.add(block);
            }
            while (!q.isEmpty()) {
                insertPhisForBlock(q.poll(), local, i, q);
            }
        }
    }

    private void findLocals() {
        ctx.getGraph().getVertices().forEach(this::findLocals);
    }

    private void findLocals(CodeBlock block) {
        List<Stmt> stmts = block.stmts();
        ListIterator<Stmt> it = stmts.listIterator(stmts.size());
        while (it.hasPrevious()) {
            Stmt stmt = it.previous();
            int opcode = stmt.opcode();

            if (opcode == Opcodes.ASSIGN_PARAM || opcode == Opcodes.ASSIGN_LOCAL || opcode == Opcodes.ASSIGN_CATCH) {
                var decl = (DeclareLocalStmt) stmt;
                var var = decl.var();
                assignments.get(var).add(block);
            }

            locals.addAll(stmt.getUses());
        }
    }

    private void insertPhisForBlock(CodeBlock block, Local local, int i, LinkedList<CodeBlock> queue) {
        if (block == ctx.getGraph().getEntryBlock()) {
            return;
        }

        for (var candidate : dominator.getIteratedDominanceFrontier(block)) {
            if (insertion.get(candidate) < i) {
                if (liveness.getLiveIn(candidate).contains(local)) {
                    if (local.isStack() && local.index() == 0 && handlers.contains(candidate)) {
                        // Oh dear oh dear...
                        // TODO:
                        throw new UnsupportedOperationException();
                    }

                    if (ctx.getGraph().getReverseEdges(candidate).size() > 1) {
                        var arguments = new HashMap<CodeBlock, Local>();
                        for (var e : ctx.getGraph().getReverseEdges(candidate)) {
                            arguments.put(e.getSource(), local);
                        }
                        ctx.getLogger().trace("Inserting phi at the start of block L{}", candidate.id());
                        var phi = new AssignPhiStmt(local, arguments);
                        candidate.preprendStmt(phi);
                    }
                }

                insertion.put(candidate, i);
                if (process.get(candidate) < i) {
                    process.put(candidate, i);
                    queue.add(candidate);
                }
            }
        }
    }

    private void rename() {

    }

    private void updateReachingDef(Local local, CodeBlock block, Stmt stmt) {
        var rd = reachingDefs.get(local);
        while (!(rd == null || dominates(rd, block, stmt))) {
            rd = reachingDefs.get(local);
        }
        reachingDefs.put(local, rd);
    }

    private boolean dominates(ReachingDefinition rd, CodeBlock block, Stmt stmt) {
        if (rd.block.equals(block)) {
            return block.indexOf(rd.stmt) < block.indexOf(stmt);
        } else {
            return dominator.getDominates(rd.block).contains(block);
        }
    }

    private static class ReachingDefinition {
        private final Local var;
        private final CodeBlock block;
        private final Stmt stmt;

        public ReachingDefinition(Local var, CodeBlock block, Stmt stmt) {
            this.var = Objects.requireNonNull(var);
            this.block = Objects.requireNonNull(block);
            this.stmt = Objects.requireNonNull(stmt);

            if (stmt.getBlock() != block) {
                throw new IllegalArgumentException();
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            ReachingDefinition that = (ReachingDefinition) o;
            return Objects.equals(var, that.var) &&
                    Objects.equals(block, that.block) &&
                    Objects.equals(stmt, that.stmt);
        }

        @Override
        public int hashCode() {
            return Objects.hash(var, block, stmt);
        }

        @Override
        public String toString() {
            return "ReachingDefinition{" +
                    "var=" + var +
                    ", block=" + block +
                    ", stmt=" + stmt +
                    '}';
        }
    }
}
