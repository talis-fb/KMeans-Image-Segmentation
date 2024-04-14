import java.lang.Math;
import java.util.Optional;

public class Point {
    private Optional<String> label = Optional.empty();
    private int x;
    private int y;
    private int z;

    Point(int x, int y) {
        this.x = x;
        this.y = y;
        this.z = 0;
    }

    Point(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    Point(String label, int x, int y, int z) {
        this.label = Optional.of(label);
        this.x = x;
        this.y = y;
        this.z = z;
    }


    public boolean isEquals(Point other) {
        return (this.x == other.x) && (this.y == other.y) && (this.z == other.z);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Point p) {
            return this.isEquals(p);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return x + y + z;
    }

    public double euclideanDistance(Point other) {
        double xDiff = this.x - other.x;
        double yDiff = this.y - other.y;
        double zDiff = this.z - other.z;
        return Math.pow(xDiff, 2) + Math.pow(yDiff, 2) + Math.pow(zDiff, 2);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public Optional<String> getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return String.format("(%s, %s, %s)", x, y, z);
    }

    public String display() {
        return String.format(
            "%s %s %s %s",
            this.getLabel().orElse("--"),
            this.getX(),
            this.getY(),
            this.getZ()
        );
    }
}
