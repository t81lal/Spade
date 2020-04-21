// Nodes: the id's here are the orderHints of the generated cfg as these
// are invariant wrt to the processing order of the ASM generator

0
post { in: [], out: [lvar0, lvar1, lvar2] }
1
post { in: [lvar0, lvar1, lvar2], out: [lvar0, lvar1, lvar2] }
2
post { in: [lvar0, lvar1, lvar2], out: [lvar0, lvar1, lvar2] }
3
post { in: [lvar0, lvar1, lvar2], out: [lvar1, lvar2] }
4
post { in: [lvar1, lvar2], out: [lvar3] }
5
post { in: [lvar1, lvar2], out: [lvar3] }
6
post { in: [lvar3], out: [] }

// Edges
0 1
1 2
2 3
3 4
3 5
4 6
5 6
