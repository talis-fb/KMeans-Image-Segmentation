package imd.ufrn.br.common;

import imd.ufrn.br.entities.Cluster;
import imd.ufrn.br.entities.Point;

import java.util.List;

public class ClustersUtils {
    public static List<Point> extractAllPointsWithCenterValues(List<Cluster> clusters) {
        return clusters.stream()
              .flatMap(
                      cluster -> cluster
                              .getPoints()
                              .stream()
                              .map(point -> new Point(
                                              point.getLabel().orElse("--"),
                                              cluster.getCenter().getX(),
                                              cluster.getCenter().getY(),
                                              cluster.getCenter().getZ()
                                      )
                              )
              )
              .toList();
    }
}
