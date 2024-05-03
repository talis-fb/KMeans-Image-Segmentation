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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

public class KmeansAtomicFixedPool implements KmeanStrategy {
    public int threads = 1;

    public KmeansAtomicFixedPool(int threads) {
        this.threads = threads;
    }

    @Override
    public List<Cluster> execute(List<Point> values, int k, List<Point> initialCenters) {
        List<Cluster> clusters = new ArrayList<>();
        for (Point center : initialCenters) {
            clusters.add(new Cluster(center, new ArrayList<>()));
        }


        ExecutorService executorService = Executors.newFixedThreadPool(this.threads);
        List<Future<?>> futures = new ArrayList<>();

        List<ClusterAccumulator> clusterAccumulators = clusters.stream().map(_el -> {
            var acc = new ClusterAccumulator();
            acc.accX = new AtomicInteger(0);
            acc.accY = new AtomicInteger(0);
            acc.accZ = new AtomicInteger(0);
            acc.couting = new AtomicInteger(0);
            return acc;
        }).toList();

        System.out.println("initial: " + initialCenters);

        while (true) {
            List<Point> centroids = clusters.stream().map(Cluster::getCenter).toList();

            clusterAccumulators.forEach(acc -> {
                acc.accX.set(0);
                acc.accY.set(0);
                acc.accZ.set(0);
                acc.couting.set(0);
            });


            for (int i = 0 ; i < this.threads; i++) {
                ThreadRunner runner = new ThreadRunner();
                runner.setInitialIndex(i);
                runner.setIntervalIndex(this.threads);
                runner.setValues(values);
                runner.setCentroids(centroids);
                runner.setClusterAccumulators(clusterAccumulators);
                futures.add(executorService.submit(runner));
            }

            for (var future: futures) {
                try {
                    future.get();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }

            List<Point> oldCenters = clusters.stream().map(Cluster::getCenter).toList();
            List<Point> newCenters = clusterAccumulators.stream().map(acc -> {
                int size = acc.couting.get();
                if (size == 0)
                    return new Point(0,0,0);


                int meanX = acc.accX.get() / size;
                int meanY = acc.accY.get() / size;
                int meanZ = acc.accZ.get() / size;
                return new Point(meanX, meanY, meanZ);
            }).toList();

            if (KmeanCommon.converged(newCenters, oldCenters)) {

                executorService.shutdown();

                for (Point value : values) {
                    var closestCluster = KmeanCommon.getIndexClosestCluster(value, clusters);
                    clusters.get(closestCluster).addPoint(value);
                }

                return clusters;
            }

            for (int i = 0; i < newCenters.size(); i++) {
                var newCentroid = newCenters.get(i);
                var cluster = clusters.get(i);
                cluster.setCenter(newCentroid);
                cluster.getPoints().clear();
            }
        }
    }

    private class ClusterAccumulator {
        public AtomicInteger accX;
        public AtomicInteger accY;
        public AtomicInteger accZ;
        public AtomicInteger couting;
    }

    private class ThreadRunner implements Runnable {
        private int initialIndex;
        private int intervalIndex;
        private List<Point> values;
        private List<Point> centroids;
        private List<ClusterAccumulator> clusterAccumulators;

        @Override
        public void run() {
            for (int i = this.initialIndex; i < this.values.size(); i += this.intervalIndex) {
                Point targetPoint = values.get(i);
                int clusterIndex = KmeanCommon.getIndexClosestCentroid(targetPoint, this.centroids);
                var clusterAccumulator = this.clusterAccumulators.get(clusterIndex);
                clusterAccumulator.accX.addAndGet(targetPoint.getX());
                clusterAccumulator.accY.addAndGet(targetPoint.getY());
                clusterAccumulator.accZ.addAndGet(targetPoint.getZ());
                clusterAccumulator.couting.incrementAndGet();
            }
        }

        public void setInitialIndex(int initialIndex) {
            this.initialIndex = initialIndex;
        }

        public void setIntervalIndex(int intervalIndex) {
            this.intervalIndex = intervalIndex;
        }

        public void setValues(List<Point> values) {
            this.values = values;
        }

        public void setCentroids(List<Point> centroids) {
            this.centroids = centroids;
        }

        public void setClusterAccumulators(List<ClusterAccumulator> clusterAccumulators) {
            this.clusterAccumulators = clusterAccumulators;
        }

    }

}
