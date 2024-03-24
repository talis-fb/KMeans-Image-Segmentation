import java.lang.Math;

public class Point {
    private double x;
    private double y;
    private double z;

    Point(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double euclideanDistance(Point other) {
        double xDiff = this.x - other.x;
        double yDiff = this.y - other.y;
        double zDiff = this.z - other.z;
        return Math.pow(xDiff, 2) + Math.pow(yDiff, 2) + Math.pow(zDiff, 2);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }
}
