// https://upload.wikimedia.org/wikipedia/commons/5/57/Tree_edges.svg
// Nodes
0
pre { id: 0 }
post { pre: 0, post: 7, topo: 0, parent: -1 }
1
pre { id: 1  }
post { pre: 1, post: 2, topo: 5, parent: 0 }
2
pre { id: 2 }
post { pre: 2, post: 1, topo: 6, parent: 1 }
3
pre { id: 3 }
post { pre: 3, post: 0, topo: 7, parent: 2 }
4
pre { id: 4 }
post { pre: 4, post: 6, topo: 1, parent: 0 }
5
pre { id: 5 }
post { pre: 5, post: 5, topo: 2, parent: 4 }
6
pre { id: 6 }
post { pre: 6, post: 3, topo: 4, parent: 5 }
7
pre { id: 7 }
post { pre: 7, post: 4, topo: 3, parent: 5 }

// Edges
//  type is an array here because we will merge the computed sets
//  together in the test class to prevent multiple labellings
0 1
post { type: ["tree"] }
0 4
post { type: ["tree"] }
0 7
post { type: ["cross_and_forward"] }
1 2
post { type: ["tree"] }
2 3
post { type: ["tree"] }
3 1
post { type: ["back"] }
4 5
post { type: ["tree"] }
5 2
post { type: ["cross_and_forward"] }
5 6
post { type: ["tree"] }
5 7
post { type: ["tree"] }