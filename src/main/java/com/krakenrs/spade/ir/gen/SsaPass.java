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

public class SsaPass {
    private final GenerationCtx ctx;
    private final Map<Local, Set<CodeBlock>> assignments;
    private final Map<Local, Integer> counter;
    private final Set<CodeBlock> handlers;
    private final Map<CodeBlock, Integer> insertion;
    private final Set<Local> locals;
    private final Map<CodeBlock, Integer> process;

    private DominatorComputer<CodeBlock> dominator;
    private SsaBlockLivenessAnalyser liveness;

    public SsaPass(GenerationCtx ctx) {
        this.ctx = Objects.requireNonNull(ctx);
        assignments = new LazyCreationHashMap<>(HashSet::new);
        counter = new LazyCreationHashMap<>(() -> 0);
        handlers = new HashSet<>();
        insertion = new LazyCreationHashMap<>(() -> 0);
        locals = new HashSet<>();
        process = new LazyCreationHashMap<>(() -> 0);
    }

    public void doTransform() {
        getHandlers();
        updateDominators();
        updateLiveness();
        insertPhis();
        rename();
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
                insertPhis(q.poll(), local, i, q);
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

    private void insertPhis(CodeBlock block, Local local, int i, LinkedList<CodeBlock> queue) {
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
}
