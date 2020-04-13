package com.krakenrs.spade.commons.collections.graph.algo;

import com.krakenrs.spade.commons.collections.LazyCreationHashMap;
import com.krakenrs.spade.commons.collections.graph.Digraph;
import com.krakenrs.spade.commons.collections.graph.Edge;
import com.krakenrs.spade.commons.collections.graph.Vertex;

import java.util.*;

public class DominatorComputer<V extends Vertex> {
    private final Digraph<V, ? extends Edge<V>> graph;
    private final V root;
    private final boolean computeFrontiers;

    // before semidominators are computed: semi[w] = vertex[w]
    // after semidominators are computed: semi[w] = the semidominator of w
    private final Map<V, Integer> semi;

    // vertex[i] = vertex of dfs pre-time i
    private final Map<Integer, V> vertex;

    private final List<V> postOrder;

    // parent[w] = parent of w in the dfs spanning tree
    private final Map<V, V> parent;

    // idom(w) = immediate dominator of w after step 4
    private final Map<V, V> idoms;

    // bucket[w] = set of vertices with a semidominator = w
    private final Map<V, Set<V>> bucket;

    private final Map<V, V> ancestor;

    private final Map<V, V> label;

    private final Map<V, Set<V>> treeDescendants;

    private final Map<V, Set<V>> treeSuccessors;

    private final Map<V, Set<V>> frontiers;

    private final Map<V, Set<V>> iteratedFrontiers;

    private DominatorTree<V> dominatorTree;

    public DominatorComputer(
            Digraph<V, ? extends Edge<V>> graph, V root, boolean computeFrontiers) {
        this.graph = graph;
        this.root = root;
        this.computeFrontiers = computeFrontiers;

        semi = new HashMap<>();
        vertex = new HashMap<>();
        postOrder = new ArrayList<>();
        parent = new HashMap<>();
        idoms = new HashMap<>();
        bucket = new LazyCreationHashMap<>(HashSet::new);
        ancestor = new HashMap<>();
        label = new HashMap<>();
        treeDescendants = new LazyCreationHashMap<>(HashSet::new);
        treeSuccessors = new LazyCreationHashMap<>(HashSet::new);
        frontiers = new LazyCreationHashMap<>(HashSet::new);
        iteratedFrontiers = new LazyCreationHashMap<>(HashSet::new);
    }

    public void run() {
        step1();
        step2and3();
        step4();

        dominatorTree = makeDominatorTree();

        if (computeFrontiers) {
            dfrontiers();
            iteratedFrontiers();
        }
    }

    public List<V> getPostOrder() {
        return Collections.unmodifiableList(postOrder);
    }

    public List<V> getPreOrder() {
        List<V> preOrder = new ArrayList<>(vertex.size());
        for (int i = 0; i < vertex.size(); i++) {
            preOrder.add(vertex.get(i));
        }
        return Collections.unmodifiableList(preOrder);
    }

    private void dfs(V v) {
        int n = semi.size();
        semi.put(v, n);
        vertex.put(n, v);
        ancestor.put(v, null);
        label.put(v, v);

        for (Edge<V> succ : graph.getEdges(v)) {
            V w = succ.getDestination();
            if (!semi.containsKey(w)) {
                parent.put(w, v);
                dfs(w);
            }
        }

        postOrder.add(v);
    }

    private void step1() {
        dfs(root);
    }

    private void step2and3() {
        for (int i = semi.size() - 1; i > 0; i--) {
            V w = vertex.get(i);
            step2(w);
            step3(w);
        }
    }

    private void step2(V w) {
        boolean visitedAny = false;

        for (Edge<V> pred : graph.getReverseEdges(w)) {
            V v = pred.getSource();

            // Ignore unreachable.
            if (!semi.containsKey(v)) {
                continue;
            }

            V u = eval(v);
            if (semi.get(u) < semi.get(w)) {
                semi.put(w, semi.get(u));
            }
            visitedAny = true;
        }

        if (visitedAny) {
            bucket.get(vertex.get(semi.get(w))).add(w);
            ancestor.put(w, parent.get(w));
        }
    }

    private void step3(V w) {
        Set<V> wbucket = bucket.get(parent.get(w));
        for (V v : wbucket) {
            V u = eval(v);
            V dom = semi.get(u) < semi.get(v) ? u : parent.get(w);
            idoms.put(v, dom);
        }
        wbucket.clear();
    }

    private void step4() {
        for (int i = 0; i < semi.size(); i++) {
            V w = vertex.get(i);
            if (idoms.get(w) != vertex.get(semi.get(w))) {
                idoms.put(w, idoms.get(idoms.get(w)));
            }
        }
    }

    private V eval(V v) {
        if (ancestor.containsKey(v)) {
            compress(v);
            return label.get(v);
        } else {
            return v;
        }
    }

    private void compress(V v) {
        if (ancestor.get(ancestor.get(v)) != null) {
            compress(ancestor.get(v));
            if (semi.get(label.get(ancestor.get(v))) < semi.get(label.get(v))) {
                label.put(v, label.get(ancestor.get(v)));
            }
            ancestor.put(v, ancestor.get(ancestor.get(v)));
        }
    }

    private void dfrontiers() {
        for (V n : treeReverseTopoOrder()) {
            Set<V> df = frontiers.get(n);
            // DF (local)
            for (Edge<V> e : graph.getEdges(n)) {
                V succ = e.getDestination();
                if (idoms.get(succ) != n) {
                    df.add(succ);
                }
            }
            // DF (up)
            for (V f : treeSuccessors.get(n)) {
                for (V ff : frontiers.get(f)) {
                    if (idoms.get(ff) != n) {
                        df.add(ff);
                    }
                }
            }
        }
    }

    private List<V> treeReverseTopoOrder() {
        /*
         * side notes: topo sort on tree == pre order on tree (not on general DAG) ==
         * reverse post order on general DAG, therefore reverse topo sort on tree ==
         * post order of general DAG
         */
        DepthFirstSearch<V> dfs = new DepthFirstSearch<>(getDominatorTree());
        dfs.run(root);
        return dfs.getPostOrder();
    }

    public DominatorTree<V> getDominatorTree() {
        return dominatorTree;
    }

    private void iteratedFrontiers() {
        for (V n : postOrder) {
            iteratedFrontier(n);
        }
    }

    private void iteratedFrontier(V n) {
        Set<V> res = new HashSet<>();
        Set<V> workingSet = new HashSet<>();
        workingSet.add(n);

        do {
            Set<V> newWorkingSet = new HashSet<>();
            for (V n1 : workingSet) {
                for (V n2 : frontiers.get(n1)) {
                    if (!res.contains(n2)) {
                        newWorkingSet.add(n2);
                        res.add(n2);
                    }
                }
            }
            workingSet = newWorkingSet;
        } while (!workingSet.isEmpty());

        iteratedFrontiers.put(n, res);
    }

    private DominatorTree<V> makeDominatorTree() {
        DominatorTree<V> tree = new DominatorTree<>(root);
        for (V v : postOrder) {
            tree.addVertex(v);
            V idom = idoms.get(v);
            if (idom != null) {
                Set<V> decs = treeDescendants.get(idom);
                decs.add(v);
                decs.addAll(treeDescendants.get(v));

                Set<V> succs = treeSuccessors.get(idom);
                succs.add(v);

                tree.addEdge(new Edge<>(idom, v));
            }
            treeDescendants.get(v).add(v);
        }
        return tree;
    }

    public Set<V> getDominates(V v) {
        return new HashSet<>(treeDescendants.get(v));
    }

    public V getImmediateDominator(V v) {
        return idoms.get(v);
    }

    public Set<V> getDominanceFrontier(V v) {
        if (computeFrontiers) {
            return new HashSet<>(frontiers.get(v));
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public Set<V> getIteratedDominanceFrontier(V v) {
        if (computeFrontiers) {
            return new HashSet<>(iteratedFrontiers.get(v));
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public static class DominatorTree<V extends Vertex> extends Digraph<V, Edge<V>> {
        private final V root;

        public DominatorTree(V root) {
            this.root = root;
        }

        public V getRoot() {
            return root;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            if (!super.equals(o))
                return false;
            DominatorTree<?> that = (DominatorTree<?>) o;
            return Objects.equals(root, that.root);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), root);
        }
    }
}
