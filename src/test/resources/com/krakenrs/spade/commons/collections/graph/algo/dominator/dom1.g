// https://en.wikipedia.org/wiki/File:Dominator_control_flow_graph.svg
// https://en.wikipedia.org/wiki/File:Dominator_tree.svg

// Nodes
0
pre { id: 0 }
post { dominates: [0, 1, 2, 3, 4, 5] }

1
pre { id: 1 }
post { dominates: [1, 2, 3, 4, 5] }

2
pre { id: 2 }
post { dominates: [2], frontier: [4] }

3
pre { id: 3 }
post { dominates: [3], frontier: [4] }

4
pre { id: 4 }
post { dominates: [4], frontier: [5] }

5
pre { id: 5 }
post { dominates: [5] }

// Edges
0 1
1 2
1 3
1 5
2 4
3 4
4 5
