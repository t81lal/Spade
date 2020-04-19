// https://www.boost.org/doc/libs/1_55_0/libs/graph/doc/lengauer_tarjan_dominator.htm

// Nodes
0
pre { id: 0 }
post { dominates: [0, 1, 2, 3, 4, 5, 6, 7] }

1
pre { id: 1 }
post { dominates: [1, 2, 3, 4, 5, 6, 7] }

2
pre { id: 2 }
post { dominates: [2], frontier: [7] }

3
pre { id: 3 }
post { dominates: [3, 4, 5, 6], frontier: [7] }

4
pre { id: 4 }
post { dominates: [4, 5, 6], frontier: [4, 7] }

5
pre { id: 5 }
post { dominates: [5], frontier: [7] }

6
pre { id: 6 }
post { dominates: [6], frontier: [4] }

7
pre { id: 7 }
post { dominates: [7] }

// Edges
0 1
1 2
1 3
2 7
3 4
4 5
4 6
5 7
6 4
