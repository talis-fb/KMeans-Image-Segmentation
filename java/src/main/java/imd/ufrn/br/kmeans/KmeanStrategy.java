package imd.ufrn.br.kmeans;

import imd.ufrn.br.entities.Cluster;
import imd.ufrn.br.entities.Point;

import java.util.List;

public interface  KmeanStrategy {
    List<Cluster> execute(List<Point> values, int k, List<Point> initialCenters);
}
