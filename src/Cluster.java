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
        int len = this.points.size();

        if (len == 0)
            return center;

        int xMean = this.points.stream().map(Point::getX).reduce(0, Integer::sum) / len;
        int yMean = this.points.stream().map(Point::getY).reduce(0, Integer::sum) / len;
        int zMean = this.points.stream().map(Point::getZ).reduce(0, Integer::sum) / len;
        return new Point(xMean, yMean, zMean);
    }

    public Point getCenter() {
        return center;
    }

    public List<Point> getPoints() {
        return points;
    }

    public void addPoint(Point point) {
        this.points.add(point);
    }

    public synchronized void addPointSync(Point point) {
        this.addPoint(point);
    }

    @Override
    public String toString() {
        return String.format("{ Center: %s | Points: %s} ", center, points);
    }
}
