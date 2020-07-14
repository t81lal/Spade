package com.krakenrs.spade.ir.gen;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.krakenrs.spade.commons.collections.LazyCreationHashMap;
import com.krakenrs.spade.commons.collections.graph.Edge;
import com.krakenrs.spade.commons.collections.graph.algo.DepthFirstSearch;
import com.krakenrs.spade.commons.collections.graph.algo.DominatorComputer;
import com.krakenrs.spade.ir.algo.SsaBlockLivenessAnalyser;
import com.krakenrs.spade.ir.code.CodeBlock;
import com.krakenrs.spade.ir.code.ControlFlowGraph;
import com.krakenrs.spade.ir.code.ExceptionRange;
import com.krakenrs.spade.ir.code.Expr;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.Stmt;
import com.krakenrs.spade.ir.code.analysis.SSADefUseMap;
import com.krakenrs.spade.ir.code.expr.value.LoadConstExpr;
import com.krakenrs.spade.ir.code.expr.value.LoadLocalExpr;
import com.krakenrs.spade.ir.code.observer.CodeObservationManager;
import com.krakenrs.spade.ir.code.observer.CodeObserver;
import com.krakenrs.spade.ir.code.stmt.AssignLocalStmt;
import com.krakenrs.spade.ir.code.stmt.AssignPhiStmt;
import com.krakenrs.spade.ir.code.stmt.ConsumeStmt;
import com.krakenrs.spade.ir.code.stmt.DeclareLocalStmt;
import com.krakenrs.spade.ir.code.visitor.AbstractCodeReducer;
import com.krakenrs.spade.ir.gen.ssa.ExprConstraints;
import com.krakenrs.spade.ir.gen.ssa.LatestValue;
import com.krakenrs.spade.ir.gen.ssa.LatestValue.CatchLV;
import com.krakenrs.spade.ir.gen.ssa.LatestValue.ConstLV;
import com.krakenrs.spade.ir.gen.ssa.LatestValue.LocalLV;
import com.krakenrs.spade.ir.gen.ssa.LatestValue.PhiLV;
import com.krakenrs.spade.ir.gen.ssa.LatestValue.VarLV;
import com.krakenrs.spade.ir.value.Local;
import com.krakenrs.spade.logging.shims.CodeBlockShim;
import com.krakenrs.spade.logging.shims.CodeUnitShim;

import lombok.NonNull;

public class SSAGenerator {

    public static interface Factory {
        SSAGenerator create(ControlFlowGraph cfg);
    }
    
    private final boolean optimise = true;

    private final GenerationCtx ctx;
    private final ControlFlowGraph cfg;
    
    private final Map<Local, Set<CodeBlock>> assignments;

    private final Map<Local, Integer> counter;
    private final Map<Local, Stack<Integer>> stacks;

    private final Set<CodeBlock> handlers;
    private final Map<CodeBlock, Integer> insertion;
    private final Set<Local> locals;
    private final Map<CodeBlock, Integer> process;
    private final Map<Local, ReachingDefinition> reachingDefs;

    private final Map<Local, LatestValue> latest;

    private DominatorComputer<CodeBlock> dominator;
    private SsaBlockLivenessAnalyser.Factory livenessFactory;

    /* Build this up as we go along */
    private SSADefUseMap defUseMap;

    private CodeObservationManager codeObservationManager;

	@Inject
	public SSAGenerator(@NonNull GenerationCtx ctx, @NonNull CodeObservationManager codeObservationManager,
			@NonNull SsaBlockLivenessAnalyser.Factory livenessFactory, @Assisted ControlFlowGraph cfg) {
		this.ctx = ctx;
		this.codeObservationManager = codeObservationManager;
		this.livenessFactory = livenessFactory;
		this.cfg = cfg;

		assignments = new LazyCreationHashMap<>(HashSet::new);
		counter = new LazyCreationHashMap<>(() -> 0);
		stacks = new LazyCreationHashMap<>(Stack::new);
		handlers = new HashSet<>();
		insertion = new LazyCreationHashMap<>(() -> 0);
		locals = new HashSet<>();
		process = new LazyCreationHashMap<>(() -> 0);
		reachingDefs = new HashMap<>();

		latest = new HashMap<>();

		defUseMap = new SSADefUseMap();
	}
    
    CodeObserver ssaCodeObserver = new CodeObserver() {
        @Override
        public void onStmtReplaced(Stmt oldStmt, Stmt newStmt) {
            System.out.println("SSAGenerator.ssaCodeObserver.new CodeObserver() {...}.onStmtReplaced()");
        }
        @Override
        public void onStmtRemoved(Stmt stmt) {
            System.out.println("SSAGenerator.ssaCodeObserver.new CodeObserver() {...}.onStmtRemoved()");
        }
        @Override
        public void onStmtAdded(Stmt stmt) {
            System.out.println("SSAGenerator.doTransform()");
        }
    };

    public void doTransform() {
        ctx.executePhase("SSAGenerator", () -> {
            ctx.executeStage("getHandlers", this::getHandlers);
            ctx.executeStage("updateDominators", this::updateDominators);
            ctx.executeStage("insertPhis", this::insertPhis);
            ctx.executeStage("rename", this::rename);
        });
    }

    private void getHandlers() {
        cfg.getExceptionRanges().stream().map(ExceptionRange::handler).forEach(handlers::add);
    }

    private void updateDominators() {
        dominator = new DominatorComputer<>(cfg, cfg.getEntryBlock(), true);
        dominator.run();
    }

    private void insertPhis() {
        findLocalsAndAssignments();
        
        /* we only want to compute once it here as the code will be changing and we don't want to force a recompute of the liveness info */
        SsaBlockLivenessAnalyser liveness = livenessFactory.get(cfg);

        int i = 0;
        for (var local : locals) {
            i++;

            var q = new LinkedList<CodeBlock>();
            for (CodeBlock block : assignments.get(local)) {
                process.put(block, i);
                q.add(block);
            }

            ctx.getLogger().trace(" Queue: {}", CodeBlockShim.of(q));

            while (!q.isEmpty()) {
                CodeBlock toProcess = q.poll();
                ctx.getLogger().trace("  visit: {}", CodeBlockShim.of(toProcess));
                insertPhisForBlock(liveness, toProcess, local, i, q);
            }
        }
    }

    private void findLocalsAndAssignments() {
        cfg.getVertices().forEach(this::findLocalsAndAssignments);

        ctx.getLogger().trace(" Locals found: {}", locals);
        ctx.getLogger().trace(" Assignments table:");

        for (Entry<Local, Set<CodeBlock>> e : assignments.entrySet()) {
            ctx.getLogger().trace("  {}: {}", e.getKey(), CodeBlockShim.of(e.getValue()));
        }
    }

    private void findLocalsAndAssignments(CodeBlock block) {
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

    private void insertPhisForBlock(SsaBlockLivenessAnalyser liveness, CodeBlock block, Local local, int i, LinkedList<CodeBlock> queue) {
        if (block.equals(cfg.getEntryBlock())) {
            return;
        }

        ctx.getLogger().trace("   IterDomFrontier({}) = {}", CodeBlockShim.of(block), CodeBlockShim.of(dominator.getIteratedDominanceFrontier(block)));
        
        for (var candidate : dominator.getIteratedDominanceFrontier(block)) {
            if (insertion.get(candidate) < i) {
                if (liveness.getLiveIn(candidate).contains(local)) {
                    if (local.isStack() && local.index() == 0 && handlers.contains(candidate)) {
                        // Oh dear oh dear...
                        // TODO: natural flow into handler block
                        ctx.getLogger().error(" Natural flow into {}", candidate);
                        throw new UnsupportedOperationException();
                    }

                    if (cfg.getReverseEdges(candidate).size() > 1) {
                        var arguments = new HashMap<CodeBlock, Local>();
                        for (var e : cfg.getReverseEdges(candidate)) {
                            arguments.put(e.getSource(), local);
                        }
                        var phi = new AssignPhiStmt(local, arguments);
                        ctx.getLogger().trace(" Inserting phi {} at the start of {}", CodeUnitShim.of(phi), CodeBlockShim.of(candidate));
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
        /* We are visiting the blocks in a slightly different order, but want to retain relative
         * ordering wrt multiple successor blocks, i.e. if we had B1 -> [B2, B3] before, this relative
         * ordering will be kept. */
        DepthFirstSearch<CodeBlock> topoDfs = new DepthFirstSearch<>(cfg);
        topoDfs.run(cfg.getEntryBlock());

        Set<CodeBlock> visited = new HashSet<>();
        // instead of passing topoOrder as an ArrayList and have O(n) indexOf
        // precompute the ordering hints

        List<CodeBlock> postOrder = topoDfs.getPostOrder();

        ctx.getLogger().trace(" Post order hints (reverse for topo): {}", CodeBlockShim.of(postOrder));

        Map<CodeBlock, Integer> ordering = new HashMap<>();
        int numBlocks = postOrder.size();
        // order is reverse of postorder aka topo order
        for (int i = 0; i < numBlocks; i++) {
            ordering.put(postOrder.get(i), numBlocks - i - 1);
        }

        /* do renaming */
        search(cfg.getEntryBlock(), visited, ordering);
        

        /* Build def/use map. Since we are in SSA (assumed from valid bytecode) each use is dominated
         * by it's corresponding definition, so if we visit in reverse postorder, we will initialise
         * local definitions before reaching their uses, ensuring this is a safe operation. */
        for(int i=0; i < postOrder.size(); i++) {
            CodeBlock b = postOrder.get(postOrder.size() - i - 1);
            for(Stmt stmt : b.stmts()) {
                defUseMap.addStmt(stmt);
            }
        }

        System.out.println(defUseMap);
        
        codeObservationManager.addCodeObserver(ssaCodeObserver);
        
    }

    private void search(CodeBlock current, Set<CodeBlock> visited, Map<CodeBlock, Integer> ordering) {
        if (!visited.add(current)) {
            return;
        }

        ctx.getLogger().trace(" Search {}", CodeBlockShim.of(current));

        searchImpl(current);

        /* get successors wrt the given ordering */
        List<CodeBlock> succs = cfg.getEdges(current).stream().map(Edge::getDestination)
                .sorted(Comparator.comparing(o -> ordering.get(o))).collect(Collectors.toList());

        ctx.getLogger().trace(" Successors: {}", CodeBlockShim.of(succs));

        for (CodeBlock succ : succs) {
            fixPhiArgs(current, succ);
        }

        for (CodeBlock succ : succs) {
            search(succ, visited, ordering);
        }

        unstackDefs(current);
    }

    private void unstackDefs(CodeBlock current) {
        for (Stmt stmt : current.stmts()) {
            if (stmt instanceof DeclareLocalStmt) {
                DeclareLocalStmt decl = (DeclareLocalStmt) stmt;
                unstackDef(decl.var());
            }
        }
    }

    private void unstackDef(Local local) {
        if (!local.isVersioned()) {
            /* var decl local needs to be renamed already*/
        }
        // stacks map only contains unversioned locals
        Local unversionedLocal = new Local(local.index(), local.isStack());
        stacks.get(unversionedLocal).pop();
    }

    private void searchImpl(CodeBlock current) {
        for (Stmt stmt : current.modSafeStmts()) {
            int opcode = stmt.opcode();

            if (opcode == Opcodes.CONSUME) {
                ConsumeStmt consume = (ConsumeStmt) stmt;
                if (!ExprConstraints.hasSideEffects(consume.expr())) {
                    ctx.getLogger().trace(" Kill {}", CodeUnitShim.of(consume));
                    current.removeStmt(stmt);
                    continue;
                }
            }

            boolean wasPhi = false;

            if (opcode == Opcodes.ASSIGN_PHI) {
                /* We can rename these any time as these * are visited before all other statements in a
                 * block (since they are always the starting statements of a block, if that block contains
                 * phi statements). */
                wasPhi = true;

                AssignPhiStmt phi = (AssignPhiStmt) stmt;
                DeclareLocalStmt newPhi = generate(phi);
                current.replaceStmt(phi, newPhi);
                
                ////// IMPORTANT: Replaced the loop variable!
                stmt = phi;
            } else {
                /* Translates locals into their latest SSA versioned locals.
                 * 
                 * Do this before a local assignment (x = ...) so that the target local isn't defined before the
                 * use so that copies in the form x = x; do not get mangled into x0 = x0 after SSA renaming.
                 * 
                 * We rename phi args (separately) later as the source local can originate from exotic blocks,
                 * i.e. we are not guaranteed to have visited the phi predecessors at this point, so we complete
                 * the renaming for all blocks and then we can ensure that the phi preds have been visited when
                 * we rename them.*/

                /* Since our code unit model is immutable, any changes in a statement causes the entire
                 * statement to be replaced in the block. */
                Stmt newStmt = translateFullStmt(stmt);

                if (newStmt != stmt) {
                    current.replaceStmt(stmt, newStmt);

                    /* If stmt is a decl and newStmt is a decl, then swapDefs
                     * else we remove the old stmt and add the new one to the
                     * def/use map */
                    
//                    if(stmt instanceof DeclareLocalStmt && newStmt instanceof DeclareLocalStmt) {
//                        /* Assumption here: newStmt lhs == oldStmt lhs */
//                        defUseMap.replaceDef((DeclareLocalStmt) newStmt);
//                    } else {
//                        defUseMap.removeStmt(stmt);
//                        defUseMap.addStmt(newStmt);
//                    }

                    ////// IMPORTANT: Replaced the loop variable!
                    stmt = newStmt;
                }
            }

            /* Only generate non phi declarations after translating the rhs'. */
            if (!wasPhi && stmt instanceof DeclareLocalStmt) {
                DeclareLocalStmt decl = (DeclareLocalStmt) stmt;
                DeclareLocalStmt newDecl = generate(decl);
                current.replaceStmt(decl, newDecl);
            }
        }
    }

    private DeclareLocalStmt generate(DeclareLocalStmt stmt) {
        Local oldLocal = stmt.var();

        if (oldLocal.isVersioned()) {
            throw new IllegalStateException();
        }

        int index = oldLocal.index();
        boolean isStack = oldLocal.isStack();

        int version = counter.get(oldLocal);

        stacks.get(oldLocal).push(version);
        counter.put(oldLocal, version + 1);

        Local newLocal = new Local(index, isStack, version);

        DeclareLocalStmt newDecl = stmt.copy(newLocal);

        if (optimise) {
            makeValue(newDecl);
        }
        
        /* Initialise an entry for this variable in the SSA def/use map */

        return newDecl;
    }

    private void makeValue(DeclareLocalStmt decl) {
        /* Begin collecting the constraints and value propagation
         * information for a declaration after we rename the lhs
         * to it's new SSA versioned local. */

        Local lhsVar = decl.var();
        if (latest.containsKey(lhsVar)) {
            throw new IllegalStateException("Revisit " + lhsVar);
        }
        LatestValue val;
        int sOp = decl.opcode();
        if (sOp == Opcodes.ASSIGN_LOCAL) {
            AssignLocalStmt assign = (AssignLocalStmt) decl;
            Expr expr = assign.value();
            int eOp = expr.opcode();

            if (eOp == Opcodes.LOAD_LOCAL) {
                /* x = y where x and y are locals */
                Local rhsVar = ((LoadLocalExpr) expr).value();
                if (!latest.containsKey(rhsVar)) {
                    /* in order to inherit from the local on the rhs, it needs to have already been visited and
                     * the value information computed (via renaming) */
                    throw new IllegalStateException("No rhs value: " + rhsVar);
                }
                /* lhsVar inherits from rhsVar */
                LatestValue ancestor = latest.get(rhsVar);
                val = new LatestValue(new LocalLV(rhsVar), ancestor.suggestedValue, ancestor.source);
                val.addConstraints(ancestor);
            } else if (eOp == Opcodes.LOAD_CONST) {
                /* x = cst where x is a local, cst is a constant */
                ConstLV v = new ConstLV(((LoadConstExpr<?>) expr).value());
                val = new LatestValue(v, v, null);
            } else {
                /* x = rhs where x is a local, rhs can be anything other than
                 * a catch/phi/param/copy */
                VarLV v = new VarLV(expr);
                val = new LatestValue(v, v, new LocalLV(lhsVar));
                val.collectConstraints(expr);
            }
        } else {
            if (sOp == Opcodes.ASSIGN_PARAM) {
                /* x = x, value equals itself (pure) */
                val = new LatestValue(new LocalLV(decl.var()), null, null);
            } else if (sOp == Opcodes.ASSIGN_CATCH) {
                /* x = catch(...) */
                CatchLV v = new CatchLV();
                val = new LatestValue(v, v, null);
            } else if (sOp == Opcodes.ASSIGN_PHI) {
                /* x = phi{...} */
                PhiLV v = new PhiLV((AssignPhiStmt) decl);
                val = new LatestValue(v, v, null);
            } else {
                throw new IllegalStateException();
            }
        }
        
        ctx.getLogger().trace("  Made value {} for {}", val, lhsVar);

        latest.put(lhsVar, val);
    }

    private void fixPhiArgs(CodeBlock current, CodeBlock successor) {
        RenamePhiArgCodeReducer reducer = new RenamePhiArgCodeReducer(current);

        for (Stmt stmt : successor.modSafeStmts()) {
            if (stmt.opcode() == Opcodes.ASSIGN_PHI) {
                AssignPhiStmt phiStmt = (AssignPhiStmt) stmt;
                AssignPhiStmt newStmt = (AssignPhiStmt) phiStmt.reduceStmt(reducer);
                if (phiStmt != newStmt) {
                    ctx.getLogger().trace(" Replace phi in {}: {}, {}", CodeBlockShim.of(current),
                            CodeUnitShim.of(phiStmt), CodeUnitShim.of(newStmt));
                    successor.replaceStmt(stmt, newStmt);
                }
            } else {
                /* No need to search the rest of the block * after we have visited the phis as they * precede all other statements.*/
                break;
            }
        }
    }
    
    class RenamePhiArgCodeReducer extends AbstractCodeReducer {
		private final CodeBlock predBlock;
		
    	public RenamePhiArgCodeReducer(CodeBlock predBlock) {
			this.predBlock = predBlock;
		}
    	
        @Override
        public AssignPhiStmt reduceAssignPhiStmt(AssignPhiStmt s) {
        	if(s.arguments().containsKey(predBlock)) {
                Map<CodeBlock, Local> resultArgs = new HashMap<>(s.arguments());
                
                Local oldLocal = resultArgs.get(predBlock);
                Local newLocal = getLatestVersion(oldLocal);
                resultArgs.put(predBlock, newLocal);

                return new AssignPhiStmt(s.var(), resultArgs);
        	} else {
        		return s;
        	}
        }
    }

    class RenamingCodeReducer extends AbstractCodeReducer {
        boolean shouldRename;

        void setShouldRename(boolean shouldRename) {
            this.shouldRename = shouldRename;
        }

        private Local remap(Local local) {
            LatestValue val = latest.get(local);
            System.out.println(local + "\n" + val);
            return local;
        }

        private Local reduceLocal(Local local) {
            Local ssaLocal;
            if (shouldRename) {
                ssaLocal = getLatestVersion(local);
            } else {
                ssaLocal = local;
            }
            return remap(ssaLocal);
        }

        @Override
        public Expr reduceLoadLocalExpr(LoadLocalExpr e) {
            Local mappedLocal = reduceLocal(e.value());
            LoadLocalExpr newLoadExpr = new LoadLocalExpr(e.getType(), mappedLocal);
            return newLoadExpr;
        }

        @Override
        public Stmt reduceAssignPhiStmt(AssignPhiStmt s) {
            Map<CodeBlock, Local> resultArgs = new HashMap<>();
            for (Entry<CodeBlock, Local> e : s.arguments().entrySet()) {
                Local mappedLocal = reduceLocal(e.getValue());
                resultArgs.put(e.getKey(), mappedLocal);
            }
            return new AssignPhiStmt(s.var(), resultArgs);
        }
    }

    RenamingCodeReducer reducer = new RenamingCodeReducer();

    private Stmt translateFullStmt(Stmt stmt) {
        reducer.setShouldRename(true);
        return Objects.requireNonNull(stmt.reduceStmt(reducer));
    }

    private AssignPhiStmt translatePhiStmt(AssignPhiStmt phiStmt) {
        //        reducer.setShouldRename(false);
        Stmt newStmt = phiStmt.reduceStmt(reducer);
        if (newStmt != phiStmt) {
            if (newStmt instanceof AssignPhiStmt) {
                return (AssignPhiStmt) newStmt;
            } else {
                throw new UnsupportedOperationException();
            }
        } else {
            return phiStmt;
        }
    }

    private Local getLatestVersion(Local local) {
        if (local.isVersioned()) {
            return local;
        } else {
            return new Local(local.index(), local.isStack(), stacks.get(local).peek());
        }
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
            return Objects.equals(var, that.var) && Objects.equals(block, that.block)
                    && Objects.equals(stmt, that.stmt);
        }

        @Override
        public int hashCode() {
            return Objects.hash(var, block, stmt);
        }

        @Override
        public String toString() {
            return "ReachingDefinition{" + "var=" + var + ", block=" + block + ", stmt=" + stmt + '}';
        }
    }
}
