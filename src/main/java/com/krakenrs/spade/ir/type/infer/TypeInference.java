package com.krakenrs.spade.ir.type.infer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.krakenrs.spade.commons.collections.bitset.CachingBitSetIndexer;
import com.krakenrs.spade.commons.collections.bitset.GenericBitSet;
import com.krakenrs.spade.commons.collections.bitset.Indexer;
import com.krakenrs.spade.ir.code.ControlFlowGraph;
import com.krakenrs.spade.ir.code.Stmt;
import com.krakenrs.spade.ir.code.stmt.AssignArrayStmt;
import com.krakenrs.spade.ir.code.stmt.AssignCatchStmt;
import com.krakenrs.spade.ir.code.stmt.AssignLocalStmt;
import com.krakenrs.spade.ir.code.stmt.AssignParamStmt;
import com.krakenrs.spade.ir.code.stmt.AssignPhiStmt;
import com.krakenrs.spade.ir.code.visitor.AbstractCodeVisitor;
import com.krakenrs.spade.ir.type.TypeHierarchy;
import com.krakenrs.spade.ir.value.Local;

public class TypeInference {
    private final Indexer<Stmt> defIndexer;
    private final Map<Local, GenericBitSet<Stmt>> dependencies;

    public TypeInference(ControlFlowGraph cfg) {
        defIndexer = CachingBitSetIndexer.newSequentialIndexer();
        dependencies = new HashMap<>();

        DependencyCollector initVisitor = new DependencyCollector();
        cfg.getVertices().stream().flatMap(b -> b.stmts().stream()).forEach(s -> s.accept(initVisitor));
    }
    
    private void addDepdendency(Local local, Stmt dependsOn) {
        if(!dependencies.containsKey(local)) {
            dependencies.put(local, new GenericBitSet<>(defIndexer));
        }
        dependencies.get(local).add(dependsOn);
    }

    public Collection<Typing> checkConstraints(Typing typing, TypeEvaluator evalFunc, TypeHierarchy hierarchy) {
        List<Typing> sigma = new ArrayList<>();
        sigma.add(typing);

        List<Typing> r = new ArrayList<>();
        
        Map<Typing, GenericBitSet<Stmt>> worklists = new HashMap<>();
    }
    
    class DependencyCollector extends AbstractCodeVisitor {
        @Override
        public void visitAssignArrayStmt(AssignArrayStmt s) {
            super.visitAssignArrayStmt(s);
        }

        @Override
        public void visitAssignCatchStmt(AssignCatchStmt s) {
        }
        
        @Override
        public void visitAssignLocalStmt(AssignLocalStmt s) {
            super.visitAssignLocalStmt(s);
        }

        @Override
        public void visitAssignParamStmt(AssignParamStmt s) {
        }

        @Override
        public void visitAssignPhiStmt(AssignPhiStmt s) {
        }
    }
}
