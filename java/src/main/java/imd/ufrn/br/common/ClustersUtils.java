package imd.ufrn.br.common;

import imd.ufrn.br.entities.Cluster;
import imd.ufrn.br.entities.Point;

import java.io.PrintStream;
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

    public static void writeAllPointsWithCenterValues(List<Cluster> clusters, PrintStream printStream) {
        clusters
            .parallelStream()
            .forEach(cluster -> {
                Point centroid = cluster.getCenter();
                cluster
                    .getPoints()
                    .parallelStream()
                    .map(p -> new Point(p.getLabel().orElse("--"), centroid.getX(), centroid.getY(), centroid.getZ()))
                    .forEach(point -> {
                        synchronized (printStream){
                            printStream.println(point.display());
                        }
                    });
            });

    }
}
