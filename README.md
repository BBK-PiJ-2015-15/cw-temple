# Coursework 4
This implementation of cousework 4 was developed by myself alone.

# Exploration Phase
In the exploration phase, the node selection works by comparing all the neighbour nodes by distance to the orb. The one that has the smallest distance is the one George will move to. A PriorityQueue is used to simplify the code but it also provides a minor optimization of the node selection.

# Escape Phase
In the escape phase, Dijskra's algorithm to find the shortest path is used. This will not give the best bonus possible but it will always find an escape path in time. To try and get more gold, if there's enough time left, the algorithm will move George forwards and backwards to any neighbour nodes with gold.
