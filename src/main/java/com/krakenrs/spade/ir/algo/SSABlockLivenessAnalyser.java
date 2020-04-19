package com.krakenrs.spade.ir.algo;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.function.Supplier;

import com.krakenrs.spade.commons.collections.LazyCreationHashMap;
import com.krakenrs.spade.commons.collections.bitset.CachingBitSetIndexer;
import com.krakenrs.spade.commons.collections.bitset.GenericBitSet;
import com.krakenrs.spade.commons.collections.bitset.Indexer;
import com.krakenrs.spade.ir.code.CodeBlock;
import com.krakenrs.spade.ir.code.ControlFlowGraph;
import com.krakenrs.spade.ir.code.FlowEdge;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.Stmt;
import com.krakenrs.spade.ir.code.stmt.AssignPhiStmt;
import com.krakenrs.spade.ir.code.stmt.DeclareLocalStmt;
import com.krakenrs.spade.ir.value.Local;

public class SSABlockLivenessAnalyser {

    private final ControlFlowGraph graph;

    private final Supplier<GenericBitSet<Local>> bitsetCreator;
    private final Map<CodeBlock, GenericBitSet<Local>> use, def, out, in, phiDef;
    private final Map<CodeBlock, Map<CodeBlock, GenericBitSet<Local>>> phiUse;

    private Queue<CodeBlock> queue;

    public SSABlockLivenessAnalyser(ControlFlowGraph graph) {
        this.graph = graph;

        Indexer<Local> localIndexer = CachingBitSetIndexer.newSequentialIndexer();
        this.bitsetCreator = () -> new GenericBitSet<>(localIndexer);

        this.use = createBlockLocalsMap();
        this.def = createBlockLocalsMap();
        this.out = createBlockLocalsMap();
        this.in = createBlockLocalsMap();
        this.phiDef = createBlockLocalsMap();
        this.phiUse = new LazyCreationHashMap<>(() -> createBlockLocalsMap());
        
        this.queue = new LinkedList<>();

        init();
        compute();
    }

    public GenericBitSet<Local> getLiveIn(CodeBlock block) {
        if (!in.containsKey(block)) {
            throw new IllegalArgumentException();
        }
        return in.get(block);
    }

    public GenericBitSet<Local> getLiveOut(CodeBlock block) {
        if (!out.containsKey(block)) {
            throw new IllegalArgumentException();
        }
        return out.get(block);
    }

    private Map<CodeBlock, GenericBitSet<Local>> createBlockLocalsMap() {
        return new LazyCreationHashMap<>(bitsetCreator);
    }
    
    private void enqueue(CodeBlock block) {
        if (!queue.contains(block)) {
            queue.add(block);
        }
    }

    private void init() {
        queue.addAll(graph.getVertices());
        graph.getVertices().stream().forEach(this::precompute);
    }

    private void precompute(CodeBlock block) {
        List<Stmt> stmts = block.stmts();
        /* Iterate in reverse order so that a local that is both defined and used in the same block
         * does not cause the def to be killed. */
        ListIterator<Stmt> it = stmts.listIterator(stmts.size());
        while (it.hasPrevious()) {
            Stmt stmt = it.previous();
            int opcode = stmt.opcode();

            if (opcode == Opcodes.ASSIGN_PHI) {
                AssignPhiStmt phiStmt = (AssignPhiStmt) stmt;
                phiDef.get(block).add(phiStmt.var());

                for (Entry<CodeBlock, Local> e : phiStmt.arguments().entrySet()) {
                    phiUse.get(block).get(e.getKey()).add(e.getValue());
                }

                // Technically don't need this continue as locals in phi's are not expressions
                // and won't be picked up by getUses(), but we can bail early here.
                continue;
            }

            if (opcode == Opcodes.ASSIGN_PARAM || opcode == Opcodes.ASSIGN_LOCAL || opcode == Opcodes.ASSIGN_CATCH) {
                DeclareLocalStmt declStmt = (DeclareLocalStmt) stmt;
                Local var = declStmt.var();
                def.get(block).add(var);
                use.get(block).remove(var);
            }

            for (Local var : stmt.getUses()) {
                use.get(block).add(var);
            }
        }
    }
    
    private void compute() {
        /* +use/-def affects out
         * -use/+def affects in
         * negative handling is always done after positive handling and additions */

        while (!queue.isEmpty()) {
            CodeBlock block = queue.remove();

            GenericBitSet<Local> prevIn = in.get(block).copy(),
                                 currIn = use.get(block).copy(),
                                 currOut = bitsetCreator.get();

            // out[n] = U(s in succ[n])(in[s])
            for (FlowEdge succ : graph.getEdges(block)) {
                currOut.addAll(in.get(succ.getDestination()));
            }

            // negative phi handling for defs
            for (FlowEdge succ : graph.getEdges(block)) {
                currOut.removeAll(phiDef.get(succ.getDestination()));
            }

            // positive phi handling for uses, see §5.4.2 "Meaning of copy statements in Sreedhar's method"
            for (FlowEdge succ : graph.getEdges(block)) {
                currOut.addAll(phiUse.get(succ.getDestination()).get(block));
            }

            // negative phi handling for uses
            for (FlowEdge pred : graph.getReverseEdges(block)) {
                currIn.removeAll(phiUse.get(block).get(pred.getSource()));
            }

            // positive phi handling for defs
            currIn.addAll(phiDef.get(block));

            // in[n] = use[n] U(out[n] - def[n])
            currIn.addAll(currOut.relativeComplement(def.get(block)));

            // update results
            in.put(block, currIn);
            out.put(block, currOut);

            if (!prevIn.equals(currIn)) {
                for (FlowEdge pred : graph.getReverseEdges(block)) {
                    enqueue(pred.getSource());
                }
            }
        }
    }
}
