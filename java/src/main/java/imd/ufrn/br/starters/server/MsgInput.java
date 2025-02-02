package imd.ufrn.br.starters.server;

import imd.ufrn.br.entities.Point;

import java.io.Serializable;
import java.util.List;

public class MsgInput implements Serializable {
    public int k;
    public List<InputPoint> points;

    public static List<Point> fromInputToPoints(MsgInput input) {
        return input.points.stream().map(p -> new Point(p.x, p.y, p.z)).toList();
    }

    public static List<InputPoint> fromPointsToInputs(List<Point> points) {
        return points.stream().map(p -> new InputPoint(p.getX(), p.getY(), p.getZ())).toList();
    }

    public static class InputPoint {
        public int x;
        public int y;
        public int z;
        public InputPoint() {
            x = 0;
            y = 0;
            z = 0;
        }
        public InputPoint(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
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

        public void setX(int x) {
            this.x = x;
        }

        public void setY(int y) {
            this.y = y;
        }

        public void setZ(int z) {
            this.z = z;
        }
    }

}
