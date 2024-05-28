package imd.ufrn.br.kmeans.strategies;

import imd.ufrn.br.entities.Cluster;
import imd.ufrn.br.entities.Point;
import imd.ufrn.br.kmeans.KmeanCommon;
import imd.ufrn.br.kmeans.KmeanStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.LongAdder;

public class KmeansAdderStructuredConc implements KmeanStrategy {
    public final int threads;

    public KmeansAdderStructuredConc(int threads) {
        this.threads = threads;
    }

    @Override
    public List<Cluster> execute(List<Point> values, int k, List<Point> initialCenters) {
        try (var taskScope = new StructuredTaskScope<>()) {
            List<ClusterAccumulator> clusterAccumulators = initialCenters.stream().map(_el ->  new ClusterAccumulator()).toList();

            System.out.println("initial: " + initialCenters);

            List<Point> centroids = initialCenters;

            while (true) {
                clusterAccumulators.forEach(ClusterAccumulator::reset);

                for (int futureIndex = 0 ; futureIndex < this.threads; futureIndex++) {
                    int finalFutureIndex = futureIndex;
                    List<Point> finalCentroids = centroids;
                    taskScope.fork(() -> {
                        for (int i = finalFutureIndex; i < values.size(); i += this.threads) {
                            Point targetPoint = values.get(i);
                            int clusterIndex = KmeanCommon.getIndexClosestCentroid(targetPoint, finalCentroids);
                            var clusterAccumulator = clusterAccumulators.get(clusterIndex);
                            clusterAccumulator.addPoint(targetPoint);
                        }
                        return null;
                    });
                }

                taskScope.join();

                List<Point> newCentroids = clusterAccumulators.stream().map(ClusterAccumulator::getMean).toList();

                if (KmeanCommon.converged(newCentroids, centroids)) {
                    List<Cluster> clusters = newCentroids.stream().map(center -> new Cluster(center, new ArrayList<>())).toList();

                    for (Point value : values) {
                        var closestCluster = KmeanCommon.getIndexClosestCentroid(value, newCentroids);
                        clusters.get(closestCluster).addPoint(value);
                    }

                    return clusters;
                } else {
                    centroids = newCentroids;
                }
            }
        } catch (InterruptedException err ) {
            err.printStackTrace();
            throw new RuntimeException(err);
        }
    }

    private class ClusterAccumulator {
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
