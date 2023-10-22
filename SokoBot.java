package solver;

import java.util.*;

public class SokoBot {
    Set<Coordinate> walls = new HashSet<>();
    Set<Coordinate> goals = new HashSet<>();
    PriorityQueue<SokobanState> pqueue = new PriorityQueue<>();
    List<SokobanState> explored = new ArrayList<>();
    List<Coordinate> deadTiles = new ArrayList<>();
    ArrayList<Integer> goalXList = new ArrayList<>();
    ArrayList<Integer> goalYList = new ArrayList<>();

    public String solveSokobanPuzzle(int width, int height, char[][] mapData, char[][] itemsData) {
        try {
            Coordinate initialPlayerPosition = new Coordinate(-1, -1);
            HashMap<Coordinate, Integer> initialBoxPosition = new HashMap<>();

            // This initializes the walls, player position, crate/box and goal coordinates.
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    if (mapData[i][j] == '#') {
                        walls.add(new Coordinate(j, i));
                    }
                    else if (mapData[i][j] == '.') {
                        goals.add(new Coordinate(j, i));
                        goalXList.add(j);
                        goalYList.add(i);
                    }

                    if (itemsData[i][j] == '@') {
                        initialPlayerPosition = new Coordinate(j, i);
                    }
                    else if (itemsData[i][j] == '$') {
                        initialBoxPosition.put(new Coordinate(j, i), 0);
                    }

                    if (i > 0 && j > 0 && i < height - 1 && j < width - 1) {
                        if (mapData[i][j] == ' ') {
                            if ((mapData[i - 1][j] == '#' && (mapData[i][j - 1] == '#' || mapData[i][j + 1] == '#'))) { // left
                                mapData[i][j] = 'X';
                                deadTiles.add(new Coordinate(j, i));
                            }
                            if ((mapData[i + 1][j] == '#' && (mapData[i][j - 1] == '#' || mapData[i][j + 1] == '#'))) { // left
                                mapData[i][j] = 'X';
                                deadTiles.add(new Coordinate(j, i));
                            }
                        }
                    }
                }
            }

            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    if ((j == 1 || j == width - 2) && !goalXList.contains(j) || ((i == 1 || i == height - 2) && !goalYList.contains(i))) {
                        if (mapData[i][j] == ' ') {
                            mapData[i][j] = 'X';
                            deadTiles.add(new Coordinate(j, i));
                        }
                    }
                }
            }

            // Initial state is set and pushed into the stack
            SokobanState initialState = new SokobanState(initialPlayerPosition, initialBoxPosition, null, "", 0);
            pqueue.add(initialState);

            while (!pqueue.isEmpty()) {
                SokobanState frontier = pqueue.poll();

                // If the boxes are in goal, terminate and return string
                if (isGoal(frontier)) {
                    pqueue.clear();
                    try {
                        return getMove(frontier);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                        return null;
                    }
                    // Continue otherwise.
                }
                else {
                    // Add frontier explored state
                    explored.add(frontier);
                    List<SokobanState> generatedStates = new ArrayList<>();
                    // Generate possible moves based on the player position.
                    generatedStates = generateSubstates(frontier);
                    // Push possible moves into the stack, and calculate heuristic score.
                    for (SokobanState state : generatedStates) {
                        // This is changeble, I simply found that if there is less than 2 boxes, it
                        // produces less moves when A* heuristic is used.
                        if (goals.size() <= 2) {
                            state.setHeuristicScore(calculateManHDistBoxesToGoals(state) + calculateManHDistPlayerToBoxes(state) + state.getMoves());
                        } else {
                            state.setHeuristicScore(calculateManHDistBoxesToGoals(state) + calculateManHDistPlayerToBoxes(state));
                        }
                        pqueue.add(state);
                    }
                }

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("All possibilities calculated!");
        return null;
    }

    /**
     * This is a helper function that returns the move sequence of the solver.
     *
     * @param state - The state where goal is reached
     * @return Move sequence in String to arrive on the goal state.
     */
    public String getMove(SokobanState state) {
        StringBuilder s = new StringBuilder();
        for (; state.getParent() != null; state = state.getParent()) {
            s.append(state.getPrevMove());
        }
        System.out.println(s.reverse().toString());
        return s.toString();
    }

    /**
     * This method generates the possible moves for the user.
     * It involves valid move checking and tries to geenrate moves that will result into a deadlock state and
     *
     * @param frontier - The current state of the game
     *
     * @return A List of possible moves (At most size of 4)
     */
    public List<SokobanState> generateSubstates(SokobanState frontier) {
        List<SokobanState> generatedStates = new ArrayList<>();
        HashMap<Coordinate, Integer> newCratePosition;
        SokobanState temporaryState;
        Character[] direction = {'u','d','l','r'};
        int[][] xy = {{0,-1},{0,1},{-1,0},{1,0}};
        for (int i = 0; i < 4; i++) {
            // If the move is valid for movement.
            if (isValid(frontier, direction[i])) {
                newCratePosition = new HashMap<>(frontier.getCratePosition());
                // Hard copy the state to avoid issues related to memory address.
                temporaryState = new SokobanState(
                        new Coordinate(frontier.getPlayerPosition().getX() + xy[i][0], frontier.getPlayerPosition().getY() + xy[i][1]),
                        newCratePosition, // Use the new cratePosition map
                        frontier, direction[i].toString(), frontier.getMoves() + 1);

                // If there is a box infront of the user, move it forward (The error handling of this is already handled in isValid() method.
                if (temporaryState.getCratePosition().get(temporaryState.getPlayerPosition()) != null) {
                    temporaryState.getCratePosition().remove(temporaryState.getPlayerPosition());
                    temporaryState.getCratePosition().put(
                            new Coordinate(frontier.getPlayerPosition().getX() + xy[i][0] * 2, frontier.getPlayerPosition().getY() + xy[i][1] * 2), 0);
                }

                // If this state already exist, then there is no need to add it to the generated state.
                if (!explored.contains(temporaryState)) {
                    generatedStates.add(temporaryState);
                }
            }
        }
        return generatedStates;
    }

    /**
     * This method checks if the state is in goal.
     *
     * @param state - Current state
     *
     * @return True if the goal is reached, false otherwise.
     */
    public boolean isGoal(SokobanState state) {
        int counter = 0;
        for (Coordinate g : goals) {
            if (state.getCratePosition().get(g) != null) {
                counter++;
            }
        }
        return counter == goals.size();
    }

    /**
     * This method checks the possible moves of the user before cloning it on the generateSubstates() method.
     *
     * Currently, it will return false if any of these is true.
     * 1. If there is a wall infront of the player.
     * 2. If there is a box infront of the box.
     * 3. If the box is pushed in a corner resulting into a deadlock
     * 4. If the direction in front of where the box is pushed is a wall, and the box is adjacent to another box perpendicular to the box pushed.
     *
     * @param state     - The current state
     * @param direction - The direction of movement 'u', 'd', 'l', 'r'
     * @return True if the movement is valid, false otherwise.
     */
    public boolean isValid(SokobanState state, char direction) {
        int playerx = state.getPlayerPosition().getX();
        int playery = state.getPlayerPosition().getY();
        switch (direction) {
            case 'd': {
                // If there is a wall infront of the player checker.
                if (walls.contains(new Coordinate(playerx, playery + 1))) {
                    return false;
                }

                if (state.getCratePosition().get(new Coordinate(playerx, playery + 1)) != null) { // if there is box
                    if (deadTiles.contains(new Coordinate(playerx, playery + 2))) {
                        return false;
                    }
                    if (state.getCratePosition().get(new Coordinate(playerx, playery + 2)) != null) {
                        return false;
                    }
                }

                // If the box is adjacent and PARALLEL to the wall, and the box is pushed adjacent to another box on the direction
                // where it is pushed.
                if (state.getCratePosition().get(new Coordinate(playerx, playery + 1)) != null) {
                    if (!goals.contains(new Coordinate(playerx, playery + 2))) {
                        if (state.getCratePosition().get(new Coordinate(playerx, playery + 3)) != null
                                && ((walls.contains(new Coordinate(playerx - 1, playery + 2))
                                && walls.contains(new Coordinate(playerx - 1, playery + 3))) ||
                                (walls.contains(new Coordinate(playerx + 1, playery + 2))
                                    && walls.contains(new Coordinate(playerx + 1, playery + 3))))) {
                            return false;
                        }
                    }
                }

                // If the box is adjacent and PERPENDICULAR to the wall, and the box is pushed adjacent to another box on the direction
                // where it is pushed.
                if (state.getCratePosition().get(new Coordinate(playerx, playery + 1)) != null) { // if there is box
                    if (!goals.contains(new Coordinate(playerx, playery + 2))) {
                        if ((state.getCratePosition().get(new Coordinate(playerx + 1, playery + 2)) != null || // box adjacency left
                                state.getCratePosition().get(new Coordinate(playerx - 1, playery + 2)) != null) // box adjacency right
                                && walls.contains(new Coordinate(playerx, playery + 3)) && // wall below first box
                                (walls.contains(new Coordinate(playerx - 1, playery + 3)) || // wall below second box
                                    walls.contains(new Coordinate(playerx + 1, playery + 3)))) {
                            return false;
                        }
                    }
                }
                break;
            }
            case 'u': {
                if (walls.contains(new Coordinate(playerx, playery - 1))) {
                    return false;
                }

                if (state.getCratePosition().get(new Coordinate(playerx, playery - 1)) != null) { // if there is box
                    if (deadTiles.contains(new Coordinate(playerx, playery - 2))) {
                        return false;
                    }
                    if (state.getCratePosition().get( new Coordinate(playerx, playery - 2)) != null) {
                        return false;
                    }
                }

                if (state.getCratePosition().get(new Coordinate(playerx, playery - 1)) != null) { // if there is box
                    if (!goals.contains(new Coordinate(playerx, playery - 2))) {
                        if (state.getCratePosition().get(new Coordinate(playerx, playery - 3)) != null
                                && ((walls.contains(new Coordinate(playerx - 1, playery - 2))
                                && walls.contains(new Coordinate(playerx - 1, playery - 3))) ||
                                (walls.contains(new Coordinate(playerx + 1, playery - 2))
                                    && walls.contains(new Coordinate(playerx + 1, playery - 3))))) {
                            return false;
                        }
                    }
                }

                if (state.getCratePosition().get(new Coordinate(playerx, playery - 1)) != null) { // if there is box
                    if (!goals.contains(new Coordinate(playerx, playery - 2))) {
                        if ((state.getCratePosition().get(new Coordinate(playerx + 1, playery - 2)) != null || // box adjacency left
                                state.getCratePosition().get(new Coordinate(playerx - 1, playery - 2)) != null) // box adjacency right
                                && walls.contains(new Coordinate(playerx, playery - 3)) && // wall below first box
                                (walls.contains(new Coordinate(playerx - 1, playery - 3)) || // wall below second box
                                    walls.contains(new Coordinate(playerx + 1,playery - 3)))) {
                            return false;
                        }
                    }
                }
                break;
            }
            case 'l': {
                if (walls.contains(new Coordinate(playerx - 1, playery))) {
                    return false;
                }

                if (state.getCratePosition().get(new Coordinate(playerx - 1, playery)) != null) { // if there is box
                    if (deadTiles.contains(new Coordinate(playerx - 2, playery))) {
                        return false;
                    }
                    if (state.getCratePosition().get(new Coordinate(playerx - 2, playery)) != null) {
                        return false;
                    }
                }

                if (state.getCratePosition().get(new Coordinate(playerx - 1, playery)) != null) {
                    if (!goals.contains(new Coordinate(playerx - 2, playery))) {
                        if (state.getCratePosition().get(new Coordinate(playerx - 3, playery)) != null
                                && ((walls.contains(new Coordinate(playerx - 2, playery - 1))
                                && walls.contains(new Coordinate(playerx - 3, playery - 1))) ||
                                (walls.contains(new Coordinate(playerx - 2, playery + 1))
                                    && walls.contains(new Coordinate(playerx - 3, playery + 1))))) {
                            return false;
                        }
                    }
                }

                if (state.getCratePosition().get(new Coordinate(playerx - 1, playery)) != null) { // if there is box
                    if (!goals.contains(new Coordinate(playerx - 2, playery))) {
                        if ((state.getCratePosition().get(new Coordinate(playerx - 2, playery + 1)) != null || // box adjacency left
                                state.getCratePosition().get(new Coordinate(playerx - 2, playery - 1)) != null) // box adjacency right
                                && walls.contains(new Coordinate(playerx - 3, playery)) && // wall below first box
                                (walls.contains(new Coordinate(playerx - 3, playery + 1)) || // wall below second box
                                    walls.contains(new Coordinate(playerx - 3, playery - 1)))) {
                            return false;
                        }
                    }
                }
                break;
            }
            case 'r': {
                if (walls.contains(new Coordinate(playerx + 1, playery))) {
                    return false;
                }

                if (state.getCratePosition().get(new Coordinate(playerx + 1, playery)) != null) { // if there is box
                    if (deadTiles.contains(new Coordinate(playerx + 2, playery))) {
                        return false;
                    }
                    if (state.getCratePosition().get(new Coordinate(playerx + 2, playery)) != null) {
                        return false;
                    }
                }

                if (state.getCratePosition().get(new Coordinate(playerx + 1, playery)) != null) {
                    if (!goals.contains(new Coordinate(playerx + 2, playery))) {
                        if (state.getCratePosition().get(new Coordinate(playerx + 3, playery)) != null
                                && ((walls.contains(new Coordinate(playerx + 2, playery - 1))
                                && walls.contains(new Coordinate(playerx + 3, playery - 1))) ||
                                (walls.contains(new Coordinate(playerx + 2, playery + 1))
                                    && walls.contains(new Coordinate(playerx + 3, playery + 1))))) {
                            return false;
                        }
                    }
                }

                if (state.getCratePosition().get(new Coordinate(playerx + 1, playery)) != null) {// if there is box
                    if (!goals.contains(new Coordinate(playerx + 2, playery))) {
                        if ((state.getCratePosition().get(new Coordinate(playerx + 2, playery + 1)) != null || // box adjacency left
                                state.getCratePosition().get(new Coordinate(playerx + 2, playery - 1)) != null) // box adjacency right
                                && walls.contains(new Coordinate(playerx + 3, playery)) && // wall below first box
                                (walls.contains(new Coordinate(playerx + 3, playery + 1)) || // wall below second box
                                    walls.contains(new Coordinate(playerx + 3, playery - 1)))) {
                            return false;
                        }
                    }
                }
                break;
            }
        }
        return true;
    }

    /**
     * This method calculates the manhattan distance of the boxes to the goals.
     *
     * @param state - Current State
     * @return The box with the minimum distance to the goal.
     */
    int calculateManHDistBoxesToGoals(SokobanState state) {
        int minimum = Integer.MAX_VALUE; // Initialize with a high value to find the minimum distance.
        for (Coordinate goalCoordinate : goals) {
            for (Coordinate cratePosition : state.getCratePosition().keySet()) {
                int manhattanDistance = Math.abs(cratePosition.getX() - goalCoordinate.getX()) + Math.abs(cratePosition.getY() - goalCoordinate.getY());
                minimum = Math.min(minimum, manhattanDistance);
            }
        }
        return minimum;
    }

    /**
     * This method calculates the manhattan distance of the player to the boxes.
     * It forms an attraction to the box, so that the player does not wander
     * anywhere in the map.
     *
     * @param state - Current State
     * @return The minimum manhattan distance of the player to the box.
     */

    public int calculateManHDistPlayerToBoxes(SokobanState state) {
        int minimum = Integer.MAX_VALUE; // Initialize with a high value to find the minimum distance.
        for (Coordinate cratePosition : state.getCratePosition().keySet()) {
            int manhattanDistance = Math.abs(cratePosition.getX() - state.getPlayerPosition().getX()) + Math.abs(cratePosition.getY() - state.getPlayerPosition().getY());
            minimum = Math.min(minimum, manhattanDistance);
        }
        return minimum;
    } 
}
