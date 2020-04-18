// Nodes
0
pre { id: 0 }
post { pre: 0, post: 4, topo: 0, parent: -1 }
1
pre { id: 1  }
post { pre: 1, post: 2, topo: 2, parent: 0 }
2
pre { id: 2 }
post { pre: 4, post: 3, topo: 1, parent: 0 }
3
pre { id: 3 }
post { pre: 2, post: 0, topo: 4, parent: 1 }
4
pre { id: 4 }
post { pre: 3, post: 1, topo: 3, parent: 1 }

// Edges
//  type is an array here because we will merge the computed sets
//  together in the test class to prevent multiple labellings
0 1
post { type: ["tree"] }
0 2
post { type: ["tree"] }
1 3
post { type: ["tree"] }
1 4
post { type: ["tree"] }