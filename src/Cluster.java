import java.util.List;

public class Cluster {
    Point center;
    List<Point> points;

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

    void assignPoints(List<Point> points) {

    }
}
