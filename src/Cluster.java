import java.util.ArrayList;
import java.util.List;

public class Cluster {
    public Cluster(Point center, List<Point> points) {
        this.center = center;
        this.points = points;
    }

    Point center;
    List<Point> points;

    static public Cluster build_with_center(Point center) {
        return new Cluster(center, new ArrayList<>());
    }

    Point calculateCenterPoint() {
        var len = this.points.size();
        var xSum = this.points.stream().map(Point::getX).reduce(0.0, Double::sum);
        var ySum = this.points.stream().map(Point::getY).reduce(0.0, Double::sum);
        var zSum = this.points.stream().map(Point::getZ).reduce(0.0, Double::sum);
        float xMean = (float) (xSum / len);
        float yMean = (float) (ySum / len);
        float zMean = (float) (zSum / len);
        return new Point(xMean, yMean, zMean);
    }

    public Point getCenter() {
        return center;
    }

    public void addPoint(Point point) {
        this.points.add(point);
    }

    @Override
    public String toString() {
        return String.format("{ Center: %s | Points: %s} ", center, points);
    }
}
