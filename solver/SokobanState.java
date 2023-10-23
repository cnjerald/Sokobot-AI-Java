package solver;

import java.util.*;

public class SokobanState{
    private Coordinate playerPosition;
    private HashMap<Coordinate, Integer> cratePosition;
    private SokobanState parent;
    private double score;
    private int moves;
    private String prevMove;

    public SokobanState(Coordinate playerPosition, HashMap<Coordinate, Integer> cratePositions, SokobanState parent, String prevMove, int moves) {
        this.playerPosition = playerPosition;
        this.cratePosition = cratePositions;
        this.parent = parent;
        this.prevMove = prevMove;
        this.moves = moves;
    }

    public int getMoves() {
        return this.moves;
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

    public void setHeuristicScore(double score) {
        this.score = score;
    }

    public double getScore() {
        return this.score;
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
    public int hashCode() {
        return Objects.hash(playerPosition, cratePosition);
    }
}
