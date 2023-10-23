package solver;

import java.util.*;

public class SokoBot {
    HashSet<Coordinate> walls = new HashSet<>();
    HashSet<Coordinate> goals = new HashSet<>();
    PriorityQueue<SokobanState> pqueue = new PriorityQueue<>(Comparator.comparingInt(SokobanState -> SokobanState.getScore()));
    HashSet<SokobanState> explored = new HashSet<>();
    HashSet<Coordinate> deadTiles = new HashSet<>();
    ArrayList<Integer> goalXList = new ArrayList<>();
    ArrayList<Integer> goalYList = new ArrayList<>();

    public String solveSokobanPuzzle(int width, int height, char[][] mapData, char[][] itemsData) {
        try {
            Coordinate initialPlayerPosition = new Coordinate(-1, -1);
            HashMap<Coordinate, Integer> initialCratePosition = new HashMap<>();

            // This initializes the walls, player position, crates and goal coordinates
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
                        initialCratePosition.put(new Coordinate(j, i), 0);
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

            // Initial state is set and added to the priority queue
            SokobanState initialState = new SokobanState(initialPlayerPosition, initialCratePosition, null, "", 0);
            pqueue.add(initialState);

            while (!pqueue.isEmpty()) {
                SokobanState frontier = pqueue.poll();

                // If the crates are in goal, terminate and return string
                if (isGoal(frontier)) {
                    try {
                        return getMove(frontier);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                        return null;
                    }
                } // Continue otherwise
                else {
                    // Add frontier explored state
                    explored.add(frontier);
                    List<SokobanState> generatedStates = new ArrayList<>();
                    // Generate possible moves based on the player position
                    generatedStates = generateSubstates(frontier);
                    // Add possible moves to the priority queue, and calculate heuristic score
                    for (SokobanState state : generatedStates) {
                        state.setHeuristicScore(calculateManHDistCratesToGoals(state) + calculateManHDistPlayerToCrates(state) + state.getMoves());
                        pqueue.add(state);
                    }
                }

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * This is a helper function that returns the move sequence of the solver.
     *
     * @param state - The state where goal is reached
     * @return Move sequence in String to arrive on the goal state
     */
    public String getMove(SokobanState state) {
        StringBuilder s = new StringBuilder();
        for (; state.getParent() != null; state = state.getParent()) {
            s.append(state.getPrevMove());
        }
        return s.reverse().toString();
    }

    /**
     * This method generates the possible moves for the user.
     * It involves valid move checking and tries to generate moves that will result into a deadlock state.
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
            // If the move is valid for movement
            if (isValid(frontier, direction[i])) {
                newCratePosition = new HashMap<>(frontier.getCratePosition());
                // Hard copy the state to avoid issues related to memory address
                temporaryState = new SokobanState(
                        new Coordinate(frontier.getPlayerPosition().getX() + xy[i][0], frontier.getPlayerPosition().getY() + xy[i][1]),
                        newCratePosition, // Use the new cratePosition map
                        frontier, direction[i].toString(), frontier.getMoves() + 1);

                // If there is a crate in front of the user, move it forward
                if (temporaryState.getCratePosition().get(temporaryState.getPlayerPosition()) != null) {
                    temporaryState.getCratePosition().remove(temporaryState.getPlayerPosition());
                    temporaryState.getCratePosition().put(new Coordinate(frontier.getPlayerPosition().getX() + xy[i][0] * 2, frontier.getPlayerPosition().getY() + xy[i][1] * 2), 0);
                }

                // If this state already exists, then there is no need to add it to the generated states
                if (!explored.contains(temporaryState)) {
                    generatedStates.add(temporaryState);
                }
            }
        }
        return generatedStates;
    }

    /**
     * This method checks if the state is the goal state.
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
     * This method checks the possible moves of the user
     *
     * Currently, it will return false if any of these is true.
     * 1. If there is a wall in front of the player.
     * 2. If there is a crate in front of the crate.
     * 3. If the crate is pushed in a corner resulting into a deadlock.
     * 4. If the direction in front of where the crate is pushed is a wall, and the crate is adjacent to another crate perpendicular to the crate pushed.
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
                // If there is a wall infront of the player checker
                if (walls.contains(new Coordinate(playerx, playery + 1))) {
                    return false;
                }

                if (state.getCratePosition().get(new Coordinate(playerx, playery + 1)) != null) { // if there is a crate
                    if (deadTiles.contains(new Coordinate(playerx, playery + 2))) {
                        return false;
                    }
                    if (state.getCratePosition().get(new Coordinate(playerx, playery + 2)) != null) {
                        return false;
                    }
                }

                if (state.getCratePosition().get(new Coordinate(playerx, playery + 1)) != null) {
                    if (!goals.contains(new Coordinate(playerx , playery + 2))) {
                        if (state.getCratePosition().get(new Coordinate(playerx, playery + 3)) != null) {
                            if (walls.contains(new Coordinate(playerx + 1, playery + 3)) && walls.contains(new Coordinate(playerx + 1, playery + 2))) {
                                return false;
                            }
                            else if (walls.contains(new Coordinate(playerx - 1, playery + 3)) && walls.contains(new Coordinate(playerx - 1, playery + 2))) {
                                return false;
                            }
                        }
                    }
                }

                if (state.getCratePosition().get(new Coordinate(playerx , playery + 1)) != null) { // if there is a crate
                    if (!goals.contains(new Coordinate(playerx , playery + 2))) {
                        if(walls.contains(new Coordinate(playerx, playery + 3))) {
                            if (state.getCratePosition().get(new Coordinate(playerx + 1, playery + 2)) != null
                                && walls.contains(new Coordinate(playerx + 1, playery + 3))) {
                                return false;
                            }
                            else if (state.getCratePosition().get(new Coordinate(playerx - 1, playery + 2)) != null
                                && walls.contains(new Coordinate(playerx - 1, playery + 3))) {
                                return false;
                            }
                        }
                    }
                }
                break;
            }
            case 'u': {
                if (walls.contains(new Coordinate(playerx, playery - 1))) {
                    return false;
                }

                if (state.getCratePosition().get(new Coordinate(playerx, playery - 1)) != null) { // if there is a crate
                    if (deadTiles.contains(new Coordinate(playerx, playery - 2))) {
                        return false;
                    }
                    if (state.getCratePosition().get( new Coordinate(playerx, playery - 2)) != null) {
                        return false;
                    }
                }

                if (state.getCratePosition().get(new Coordinate(playerx, playery - 1)) != null) {
                    if (!goals.contains(new Coordinate(playerx , playery - 2))) {
                        if (state.getCratePosition().get(new Coordinate(playerx, playery - 3)) != null) {
                            if (walls.contains(new Coordinate(playerx + 1, playery - 3)) && walls.contains(new Coordinate(playerx + 1, playery - 2))) {
                                return false;
                            }
                            else if (walls.contains(new Coordinate(playerx - 1, playery - 3)) && walls.contains(new Coordinate(playerx - 1, playery - 2))) {
                                return false;
                            }
                        }
                    }
                }

                if (state.getCratePosition().get(new Coordinate(playerx , playery - 1)) != null) { // if there is a crate
                    if (!goals.contains(new Coordinate(playerx , playery - 2))) {
                        if(walls.contains(new Coordinate(playerx, playery - 3))) {
                            if (state.getCratePosition().get(new Coordinate(playerx + 1, playery - 2)) != null
                                && walls.contains(new Coordinate(playerx + 1, playery - 3))) {
                                return false;
                            }
                            else if (state.getCratePosition().get(new Coordinate(playerx - 1, playery - 2)) != null
                                && walls.contains(new Coordinate(playerx - 1, playery - 3))) {
                                return false;
                            }
                        }
                    }
                }
                break;
            }
            case 'l': {
                if (walls.contains(new Coordinate(playerx - 1, playery))) {
                    return false;
                }

                if (state.getCratePosition().get(new Coordinate(playerx - 1, playery)) != null) { // if there is a crate
                    if (deadTiles.contains(new Coordinate(playerx - 2, playery))) {
                        return false;
                    }
                    if (state.getCratePosition().get(new Coordinate(playerx - 2, playery)) != null) {
                        return false;
                    }
                }

                if (state.getCratePosition().get(new Coordinate(playerx - 1, playery)) != null) {
                    if (!goals.contains(new Coordinate(playerx - 2, playery))) {
                        if (state.getCratePosition().get(new Coordinate(playerx - 3, playery)) != null) {
                            if (walls.contains(new Coordinate(playerx - 3, playery + 1)) && walls.contains(new Coordinate(playerx - 2, playery + 1))) {
                                return false;
                            }
                            else if (walls.contains(new Coordinate(playerx - 3, playery - 1)) && walls.contains(new Coordinate(playerx - 2, playery - 1))) {
                                return false;
                            }   
                        }
                    }
                }

                if (state.getCratePosition().get(new Coordinate(playerx - 1, playery)) != null) { // if there is a crate
                    if (!goals.contains(new Coordinate(playerx - 2, playery))) {
                        if(walls.contains(new Coordinate(playerx - 3, playery))) {
                            if (state.getCratePosition().get(new Coordinate(playerx - 2, playery + 1)) != null
                                && walls.contains(new Coordinate(playerx - 3, playery +1 ))) {
                                return false;
                            }
                            else if (state.getCratePosition().get(new Coordinate(playerx - 2, playery - 1)) != null
                                && walls.contains(new Coordinate(playerx - 3, playery - 1))) {
                                return false;
                            }
                        }
                    }
                }
                break;
            }
            case 'r': {
                if (walls.contains(new Coordinate(playerx + 1, playery))) {
                    return false;
                }

                if (state.getCratePosition().get(new Coordinate(playerx + 1, playery)) != null) { // if there is a crate
                    if (deadTiles.contains(new Coordinate(playerx + 2, playery))) {
                        return false;
                    }
                    if (state.getCratePosition().get(new Coordinate(playerx + 2, playery)) != null) {
                        return false;
                    }
                }

                if (state.getCratePosition().get(new Coordinate(playerx + 1, playery)) != null) {
                    if (!goals.contains(new Coordinate(playerx + 2, playery))) {
                        if (state.getCratePosition().get(new Coordinate(playerx + 3, playery)) != null) {
                            if (walls.contains(new Coordinate(playerx + 3, playery + 1)) && walls.contains(new Coordinate(playerx + 2, playery + 1))) {
                                return false;
                            }
                            else if (walls.contains(new Coordinate(playerx + 3, playery - 1)) && walls.contains(new Coordinate(playerx + 2, playery - 1))) {
                                return false;
                            }
                        }
                    }
                }

                if (state.getCratePosition().get(new Coordinate(playerx + 1, playery)) != null) { // if there is a crate
                    if (!goals.contains(new Coordinate(playerx + 2, playery))) {
                        if(walls.contains(new Coordinate(playerx + 3, playery))) {
                            if (state.getCratePosition().get(new Coordinate(playerx + 2, playery + 1)) != null
                                && walls.contains(new Coordinate(playerx + 3, playery + 1))) {
                                return false;
                            }
                            else if (state.getCratePosition().get(new Coordinate(playerx + 2, playery - 1)) != null
                                && walls.contains(new Coordinate(playerx + 3, playery - 1))) {
                                return false;
                            }
                        }
                    }
                }
                break;
            }
        }
        return true;
    }

    /**
     * This method calculates the Manhattan distance of the crates to the goals.
     *
     * @param state - Current State
     * @return The crate with the minimum Manhattan distance to the goal
     */
    public int calculateManHDistCratesToGoals(SokobanState state) {
        int minimum = Integer.MAX_VALUE; // Initialize with a high value to find the minimum distance
        int counter = 0;
        for (Coordinate cratePosition : state.getCratePosition().keySet()) {
            for (Coordinate goalCoordinate : goals) {
                int manhattanDistance = Math.abs(cratePosition.getX() - goalCoordinate.getX()) + Math.abs(cratePosition.getY() - goalCoordinate.getY());
                if (manhattanDistance != 0) {
                    minimum = Math.min(minimum, manhattanDistance);
                }
                else {
                    counter++;
                }
            }
        }
        return minimum + (goals.size() - counter);
    }

    /**
     * This method calculates the Manhattan distance of the player to the crates.
     * It forms an attraction to the crate, so that the player does not wander anywhere in the map.
     *
     * @param state - Current State
     * @return The minimum Manhattan distance of the player to the crate
     */

    public int calculateManHDistPlayerToCrates(SokobanState state) {
        int minimum = Integer.MAX_VALUE; // Initialize with a high value to find the minimum distance
        for (Coordinate cratePosition : state.getCratePosition().keySet()) {
            int manhattanDistance = Math.abs(cratePosition.getX() - state.getPlayerPosition().getX()) + Math.abs(cratePosition.getY() - state.getPlayerPosition().getY());
            minimum = Math.min(minimum, manhattanDistance);
        }
        return minimum;
    }
}   
