// LT79 Fig. 1

// Nodes
0
pre { id: 0 }
post { dominates: [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12], frontier: [0] }

1
pre { id: 1 }
post { dominates: [1], frontier: [4] }

2
pre { id: 2 }
post { dominates: [2], frontier: [1, 4, 5] }

3
pre { id: 3 }
post { dominates: [3, 6, 7, 10], frontier: [9] }

4
pre { id: 4 }
post { dominates: [4, 12], frontier: [8] }

5
pre { id: 5 }
post { dominates: [5], frontier: [8] }

6
pre { id: 6 }
post { dominates: [6], frontier: [9] }

7
pre { id: 7 }
post { dominates: [7, 10], frontier: [9] }

8
pre { id: 8 }
post { dominates: [8], frontier: [5, 11] }

9
pre { id: 9 }
post { dominates: [9], frontier: [11] }

10
pre { id: 10 }
post { dominates: [10], frontier: [9] }

11
pre { id: 11 }
post { dominates: [11], frontier: [0, 9] }

12
pre { id: 12 }
post { dominates: [12], frontier: [8] }

// Edges
0 1
0 2
0 3
1 4
2 1
2 4
2 5
3 6
3 7
4 12
5 8
6 9
7 9
7 10
8 5
8 11
9 11
10 9
11 9
11 0
12 8
