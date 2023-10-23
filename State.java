package solver;

import java.util.ArrayList;

public class State {
    private Coordinate playerPosition = null;
    private ArrayList<Coordinate> crateCoordinates = new ArrayList<>();

    public State() {
        // Default constructor
    }

    public State(Coordinate playerPosition, ArrayList<Coordinate> crateCoordinates) {
        this.playerPosition = playerPosition;
        this.crateCoordinates = crateCoordinates;
    }

    public static State copy(State other) {
        Coordinate playerPosition = new Coordinate(other.playerPosition.getX(), other.playerPosition.getY());
        ArrayList<Coordinate> crateCoordinates = new ArrayList<>();
        for (Coordinate crate : other.crateCoordinates) {
            crateCoordinates.add(new Coordinate(crate.getX(), crate.getY()));
        }

        return new State(playerPosition, crateCoordinates);
    }

    public Coordinate getPlayerPosition() {
        return this.playerPosition;
    }

    public ArrayList<Coordinate> getCrateCoordinates() {
        return this.crateCoordinates;
    }

    public void setPlayerPosition(Coordinate playerPosition) {
        this.playerPosition = playerPosition;
    }

    public void moveCrate(int index, Coordinate newCrateCoordinate) {
        this.crateCoordinates.set(index, newCrateCoordinate);
    }
}
