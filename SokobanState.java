package solver;

import java.util.*;

public class SokobanState implements Comparable<SokobanState> {
    private Coordinate playerPosition;
    private HashMap<Coordinate, Integer> cratePosition;
    private SokobanState parent;
    private String prevMove;
    private int moves, score;

    public SokobanState(Coordinate playerPosition, HashMap<Coordinate, Integer> cratePosition, SokobanState parent, String prevMove, int moves) {
        this.playerPosition = playerPosition;
        this.cratePosition = cratePosition;
        this.parent = parent;
        this.prevMove = prevMove;
        this.moves = moves;
    }
    
    public Coordinate getPlayerPosition() {
        return playerPosition;
    }
    
    public HashMap<Coordinate, Integer> getCratePosition() {
        return cratePosition;
    }
    
    public SokobanState getParent() {
        return parent;
    }
    
    public String getPrevMove() {
        return prevMove;
    }
    
    public int getMoves() {
        return moves;
    }
    
    public int getScore() {
        return score;
    }
    
    public void setHeuristicScore(int score) {
        this.score = score;
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerPosition, cratePosition);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        SokobanState that = (SokobanState) o;
        return Objects.equals(playerPosition, that.playerPosition) &&
                Objects.equals(cratePosition, that.cratePosition);
    }

    @Override
    public int compareTo(SokobanState other) {
        return Integer.compare(this.score, other.score);
    }
}
