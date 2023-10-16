package solver;

public class Coordinate {

    private int x;
    private int y;

    Coordinate(int x, int y){
        this.x = x;
        this.y = y;
    }


    public int getX(){
        return this.x;
    }
    public int getY(){
        return this.y;
    }

    @Override
    public int hashCode() {
        return x*1000 + y;
    }

    // equals method used for contains()
    @Override
    public boolean equals(Object object){
        if (object == null) return false;
        if (object == this) return true;
        if (this.getClass() != object.getClass()) return false;
        Coordinate c = (Coordinate) object;
        if(this.hashCode()== c.hashCode()) return true;
        return ((this.x == c.x) && (this.y == c.y));
    }




}
