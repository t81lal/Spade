// https://cseweb.ucsd.edu/classes/fa03/cse231/lec5seq.pdf

// Nodes
En
pre { id: 0 }
post { dominates: [0, 1, 2, 3, 4, 6, 7, 8, 9, 10], frontier: [] }

A
pre { id: 1 }
post { dominates: [1, 2, 3, 4, 9, 10], frontier: [7, 8] }

B
pre { id: 2 }
post { dominates: [2, 3, 4, 9, 10], frontier: [8] }

C
pre { id: 3 }
post { dominates: [3, 4, 9, 10], frontier: [] }

D
pre { id: 4 }
post { dominates: [4], frontier: [10] }

E
pre { id: 5 }
post { dominates: [], frontier: [] }

F
pre { id: 6 }
post { dominates: [6], frontier: [7] }

G
pre { id: 7 }
post { dominates: [7], frontier: [8] }

H
pre { id: 8 }
post { dominates: [8], frontier: [] }

I
pre { id: 9 }
post { dominates: [9], frontier: [10] }

J
pre { id: 10 }
post { dominates: [10], frontier: [] }

// Edges
En A
En F
A B
A G
B C
B H
C D
C I
D J
F G
G H
I J
