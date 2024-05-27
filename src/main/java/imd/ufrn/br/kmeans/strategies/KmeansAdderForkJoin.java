package imd.ufrn.br.kmeans.strategies;

import imd.ufrn.br.entities.Cluster;
import imd.ufrn.br.entities.Point;
import imd.ufrn.br.kmeans.KmeanCommon;
import imd.ufrn.br.kmeans.KmeanStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.LongAdder;

public class KmeansAdderForkJoin implements KmeanStrategy {
    public int threads = 1;

    public KmeansAdderForkJoin(int threads) {
        this.threads = threads;
    }

    @Override
    public List<Cluster> execute(List<Point> values, int k, List<Point> initialCenters) {
        var forkJoinPool = ForkJoinPool.commonPool();

        List<KmeansAdderForkJoin.ClusterAccumulator> clusterAccumulators = initialCenters.stream().map(_el ->  new KmeansAdderForkJoin.ClusterAccumulator()).toList();

        System.out.println("initial: " + initialCenters);

        List<Point> centroids = initialCenters;

        while (true) {
            clusterAccumulators.forEach(ClusterAccumulator::reset);

            var task = new MeanTask(centroids, clusterAccumulators, values);
            forkJoinPool.invoke(task);

            List<Point> newCentroids = clusterAccumulators.stream().map(ClusterAccumulator::getMean).toList();

            if (KmeanCommon.converged(newCentroids, centroids)) {
                forkJoinPool.shutdown();

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
    }

    public class MeanTask extends  RecursiveAction {
        private static final int THRESHOLD = 20;

        private final List<Point> centroids;
        private final List<ClusterAccumulator> accumulators;
        private final List<Point> values;

        public MeanTask(List<Point> centroids, List<ClusterAccumulator> accumulators, List<Point> values) {
            this.centroids = centroids;
            this.accumulators = accumulators;
            this.values = values;
        }

        @Override
        protected void compute() {
            if (this.values.size() <= THRESHOLD) {
                for (Point value : values) {
                    int clusterIndex = KmeanCommon.getIndexClosestCentroid(value, this.centroids);
                    var clusterAccumulator = this.accumulators.get(clusterIndex);
                    clusterAccumulator.addPoint(value);
                }
            } else {
                int half = this.values.size() / 2;
                List<Point> left = this.values.subList(0, half);
                List<Point> right = this.values.subList(half, this.values.size());

                // Create subtasks for each half
                MeanTask leftTask = new MeanTask(this.centroids,  this.accumulators, left);
                MeanTask rightTask = new MeanTask(this.centroids, this.accumulators, right);

                // Fork the subtasks
                leftTask.fork();
                rightTask.fork();

                // Join the subtasks and combine results
                leftTask.join();
                rightTask.join();
            }
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
            long size = this.couting.sum();
            if (size == 0)
                return new Point(0,0,0);

            long meanX = this.accX.sum() / size;
            long meanY = this.accY.sum() / size;
            long meanZ = this.accZ.sum() / size;
            return new Point((int) meanX, (int) meanY, (int) meanZ);
        }
    }

}
