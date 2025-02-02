package imd.ufrn.br.kmeans.strategies;

import imd.ufrn.br.entities.Cluster;
import imd.ufrn.br.entities.Point;
import imd.ufrn.br.kmeans.KmeanCommon;
import imd.ufrn.br.kmeans.KmeanStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.LongAdder;

public class KmeansAdderParallelStream implements KmeanStrategy {

    @Override
    public List<Cluster> execute(List<Point> values, int k, List<Point> initialCenters) {
        List<ClusterAccumulator> clusterAccumulators = initialCenters.stream().map(_el -> new ClusterAccumulator()).toList();

        System.out.println("initial: " + initialCenters);

        List<Point> centroids = initialCenters;

        while (true) {
            clusterAccumulators.forEach(ClusterAccumulator::reset);

            List<Point> finalCentroids = centroids;
            values.parallelStream()
                    .forEach(point -> {
                        int clusterIndex = KmeanCommon.getIndexClosestCentroid(point, finalCentroids);
                        var clusterAccumulator = clusterAccumulators.get(clusterIndex);
                        clusterAccumulator.addPoint(point);
                    });

            List<Point> newCentroids = clusterAccumulators.parallelStream().map(ClusterAccumulator::getMean).toList();

            if (KmeanCommon.converged(newCentroids, centroids)) {
                List<Cluster> clusters = newCentroids.stream().map(center -> new Cluster(center, new ArrayList<>())).toList();

                values.parallelStream()
                        .forEach(p -> {
                            int closestClusterIndex = KmeanCommon.getIndexClosestCentroid(p, newCentroids);
                            var cluster = clusters.get(closestClusterIndex);
                            synchronized (cluster) {
                                cluster.addPoint(p);
                            }
                        });

                return clusters;
            } else {
                centroids = newCentroids;
            }
        }
    }

    private static class ClusterAccumulator {
        public final LongAdder accX;
        public final LongAdder accY;
        public final LongAdder accZ;
        public final LongAdder couting;

        public ClusterAccumulator() {
            this.accX = new LongAdder();
            this.accY = new LongAdder();
            this.accZ = new LongAdder();
            this.couting = new LongAdder();
        }
        public void reset() {
            this.accX.reset();
            this.accY.reset();
            this.accZ.reset();
            this.couting.reset();
        }
        public void addPoint(Point point) {
            this.accX.add(point.getX());
            this.accY.add(point.getY());
            this.accZ.add(point.getZ());
            this.couting.increment();
        }
        public Point getMean() {
            var size = this.couting.sum();
            if (size == 0)
                return new Point(0,0,0);

            var meanX = this.accX.sum() / size;
            var meanY = this.accY.sum() / size;
            var meanZ = this.accZ.sum() / size;
            return new Point((int) meanX, (int) meanY, (int) meanZ);
        }
    }
}
