// Nodes: the id's here are the orderHints of the generated cfg as these
// are invariant wrt to the processing order of the ASM generator

0
post { in: [], out: [lvar1, lvar2, lvar3] }
1
post { in: [lvar1, lvar2, lvar3], out: [lvar1, lvar2, lvar3] }
2
post { in: [lvar1, lvar2, lvar3], out: [lvar1, lvar2, lvar3] }
3
post { in: [lvar1, lvar2, lvar3], out: [lvar2, lvar3] }
4
post { in: [lvar2, lvar3], out: [lvar4] }
5
post { in: [lvar4], out: [lvar4] }
6
post { in: [lvar2, lvar3], out: [lvar4] }
7
post { in: [lvar4], out: [] }

// Edges
1 2
3 4
6 7
4 5
2 3
0 1
5 7
3 6