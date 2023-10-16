package solver;

import java.util.*;

public class SokobanState {
    Coordinate playerPosition;
    HashMap<Integer, Coordinate> cratePosition = new HashMap<>();
    SokobanState parent = null;

    int score;

    int moves;

    String prevMove;


    SokobanState(Coordinate playerPosition, HashMap<Integer, Coordinate> cratePositions, SokobanState parent,String prevMove,int moves) {
        this.playerPosition = playerPosition;
        this.cratePosition = cratePositions;
        this.parent = parent;
        this.prevMove = prevMove;
        this.moves = moves;
    }

    public int getMoves(){
        return this.moves;
    }

    public Coordinate getPlayerPosition() {
        return playerPosition;
    }

    public HashMap<Integer, Coordinate> getCratePosition() {
        return cratePosition;
    }

    public void setHeuristicScore(int score){
        this.score = score;
    }
    public int getScore(){
        return  this.score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SokobanState that = (SokobanState) o;
        return Objects.equals(playerPosition, that.playerPosition) &&
                Objects.equals(cratePosition, that.cratePosition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerPosition, cratePosition);
    }
}
