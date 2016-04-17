package student;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Stack;

import game.EscapeState;
import game.ExplorationState;
import game.Node;
import game.NodeStatus;
import game.Tile;

public class Explorer {
    /**
     * Explore the cavern, trying to find the orb in as few steps as possible.
     * Once you find the orb, you must return from the function in order to pick
     * it up. If you continue to move after finding the orb rather
     * than returning, it will not count.
     * If you return from this function while not standing on top of the orb,
     * it will count as a failure.
     * <p>
     * There is no limit to how many steps you can take, but you will receive
     * a score bonus multiplier for finding the orb in fewer steps.
     * <p>
     * At every step, you only know your current tile's ID and the ID of all
     * open neighbor tiles, as well as the distance to the orb at each of these tiles
     * (ignoring walls and obstacles).
     * <p>
     * To get information about the current state, use functions
     * getCurrentLocation(),
     * getNeighbours(), and
     * getDistanceToTarget()
     * in ExplorationState.
     * You know you are standing on the orb when getDistanceToTarget() is 0.
     * <p>
     * Use function moveTo(long id) in ExplorationState to move to a neighboring
     * tile by its ID. Doing this will change state to reflect your new position.
     * <p>
     * A suggested first implementation that will always find the orb, but likely won't
     * receive a large bonus multiplier, is a depth-first search.
     *
     * @param state the information available at the current state
     */
    public void explore(ExplorationState state) {
        explore(state, -1, new HashSet<Long>());
    }
    
    /**
     * Escape from the cavern before the ceiling collapses, trying to collect as much
     * gold as possible along the way. Your solution must ALWAYS escape before time runs
     * out, and this should be prioritized above collecting gold.
     * <p>
     * You now have access to the entire underlying graph, which can be accessed through EscapeState.
     * getCurrentNode() and getExit() will return you Node objects of interest, and getVertices()
     * will return a collection of all nodes on the graph.
     * <p>
     * Note that time is measured entirely in the number of steps taken, and for each step
     * the time remaining is decremented by the weight of the edge taken. You can use
     * getTimeRemaining() to get the time still remaining, pickUpGold() to pick up any gold
     * on your current tile (this will fail if no such gold exists), and moveTo() to move
     * to a destination node adjacent to your current node.
     * <p>
     * You must return from this function while standing at the exit. Failing to do so before time
     * runs out or returning from the wrong location will be considered a failed run.
     * <p>
     * You will always have enough time to escape using the shortest path from the starting
     * position to the exit, although this will not collect much gold.
     *
     * @param state the information available at the current state
     */
    public void escape(EscapeState state) {
        Node source = state.getCurrentNode();
        
        // Dijskra's search
        HashMap<Node, Node> prev = escapeSearch(state.getVertices(), source);
        
        // Determine path
        Stack<Node> path = determineEscapePath(prev, source, state.getExit());
        
        // Walk to exit node
        while (!path.isEmpty()) {
            Node node = path.pop();
            
            // Move to node
            state.moveTo(node);
            
            // Pick up gold
            Tile tile = node.getTile();
            if (tile.getGold() != 0)
                state.pickUpGold();
        }
    }
    
    /** Recursivelly explore the maze until we find the orb.
     *
     * We select the assumed best node by choosing the neighbour node with the
     * smallest distance to the orb.
     *
     * @param state the exploration state
     * @param previousLocation the previous location on the path taken
     * @param visited the set of visited locations
     * @return false if a dead end; true otherwise
     */
    private boolean explore(ExplorationState state, long previousLocation,
        Set<Long> visited
    ) {
        // Are we done?
        if (state.getDistanceToTarget() == 0)
            return true;
        
        // Select the best node to move to
        PriorityQueue<NodeStatus> priorityQueue = new PriorityQueue<>();
        
        Collection<NodeStatus> nodes = state.getNeighbours();
        for (NodeStatus node : nodes) {
            // Only add if not previously visited
            if (!visited.contains(node.getId()))
                priorityQueue.add(node);
        }
        
        // Explore from assumed best node until we find a path to the orb
        long currentLocation = state.getCurrentLocation();
        NodeStatus node;
        
        while ((node = priorityQueue.poll()) != null) {
            // Move to the best location
            long id = node.getId();
            state.moveTo(id);
            
            // Add to visited set
            visited.add(id);
            
            // Continue exploration
            if (explore(state, currentLocation, visited))
                return true;
        }
        
        // We reached a dead end, move back
        state.moveTo(previousLocation);
        return false;
    }
    
    /** Search for the smallest path.
     *
     * The current implementation uses the Dijskra's algo to search for the
     * smallest path.
     *
     * @param graph the graph
     * @param source the source node
     * @return the map with the prev node
     */
    private HashMap<Node, Node> escapeSearch(Collection<Node> graph,
        Node source
    ) {
        HashSet<Node> visited = new HashSet<>();
        HashMap<Node, Integer> dist = new HashMap<>();
        HashMap<Node, Node> prev = new HashMap<>();
    
        // Initialise distances
        for (Node node : graph)
            dist.put(node, Integer.MAX_VALUE);
        
        dist.put(source, 0);
        
        // Search
        for (int i = 0; i < graph.size(); i++) {
            Node next = getMinNode(dist, visited);
            visited.add(next);
            
            Collection<Node> childs = next.getNeighbours();
            for (Node child : childs) {
                int d = dist.get(next) + next.getEdge(child).length;
                if (dist.get(child) > d) {
                    dist.put(child, d);
                    prev.put(child, next);
                }
            }
        }
        return prev;
    }
    
    /** Selects the best next node to walk to.
     *
     * @param dist the distances to the source node
     * @param visited the visited nodes set
     * @return the best next node
     */
    private Node getMinNode(HashMap<Node, Integer> dist,
        HashSet<Node> visited
    ) {
        Node min = null;
        int d = Integer.MAX_VALUE;
        
        // Find next best node
        for (Node node : dist.keySet()) {
            if (!visited.contains(node)) {
                int cur = dist.get(node);
                if (cur < d) {
                    min = node;
                    d = cur;
                }
            }
        }
        return min;
    }
    
    /** Returns the escape path.
     *
     * @param prev the map with previous nodes
     * @param source the source node
     * @param target the target node
     * @return the path
     */
    private Stack<Node> determineEscapePath(Map<Node, Node> prev, Node source,
        Node target
    ) {
        Stack<Node> path = new Stack<>();
        
        // Walk from target to source node
        Node node = target;
        while (node != source) {
            path.push(node);
            node = prev.get(node);
        }
        return path;
    }
}
