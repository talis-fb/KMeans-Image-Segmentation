package imd.ufrn.br.common;

import imd.ufrn.br.entities.Point;

import java.util.List;

public class PointsUtils {
    public static List<Point> extractDistintInitialValues(List<Point> values, int n) {
        return values.stream().distinct().limit(n).toList();
    }
}
