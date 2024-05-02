package imd.ufrn.br.kmeans.strategies;

import imd.ufrn.br.entities.Cluster;
import imd.ufrn.br.entities.Point;
import imd.ufrn.br.kmeans.KmeanCommon;
import imd.ufrn.br.kmeans.KmeanStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

public class KmeansAtomic implements KmeanStrategy {
    public int threads = 1;
    public ThreadMode mode = ThreadMode.PLATAFORM;

    public KmeansAtomic(ThreadMode mode, int threads) {
        this.mode = mode;
        this.threads = threads;
    }

    @Override
    public List<Cluster> execute(List<Point> values, int k, List<Point> initialCenters) {
        List<Cluster> clusters = new ArrayList<>();
        for (Point center : initialCenters) {
            clusters.add(new Cluster(center, new ArrayList<>()));
        }

        var threadBuilder = switch (this.mode) {
            case VIRTUAL -> Thread.ofVirtual();
            case PLATAFORM -> Thread.ofPlatform();
        };

        List<Thread> threadsRunning = new ArrayList<>();

        while (true) {
            List<Point> centroids = clusters.stream().map(Cluster::getCenter).toList();

            List<ClusterAccumulator> clusterAccumulators = clusters.stream().map(el -> {
                var acc = new ClusterAccumulator();
                acc.accX = new AtomicInteger(0);
                acc.accY = new AtomicInteger(0);
                acc.accZ = new AtomicInteger(0);
                acc.couting = new AtomicInteger(0);
                return acc;
            }).toList();

            for (int i = 0 ; i < this.threads; i++) {
                ThreadRunner runner = new ThreadRunner();
                runner.setInitialIndex(i);
                runner.setIntervalIndex(this.threads);
                runner.setValues(values);
                runner.setCentroids(centroids);
                runner.setClusterAccumulators(clusterAccumulators);
                threadsRunning.add(threadBuilder.start(runner));
            }

            for (Thread thread : threadsRunning) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            threadsRunning.clear();

            List<Point> oldCenters = clusters.stream().map(Cluster::getCenter).toList();
            List<Point> newCenters = clusterAccumulators.stream().map(el -> {
                var size = el.couting.get();
                if (size == 0)
                    return new Point(0,0,0);

                var meanX = el.accX.get() / size;
                var meanY = el.accY.get() / size;
                var meanZ = el.accZ.get() / size;
                return new Point((int) meanX, (int) meanY, (int) meanZ);
            }).toList();

            if (KmeanCommon.converged(newCenters, oldCenters)) {
                for (Point value : values) {
                    var closestCluster = KmeanCommon.getIndexClosestCluster(value, clusters);
                    clusters.get(closestCluster).addPoint(value);
                }

                return clusters;
            }

            clusters = newCenters.stream().map(center -> new Cluster(center, new ArrayList<>())).toList();

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

        @Override
        public void run() {
            for (int i = this.initialIndex; i < this.values.size(); i += this.intervalIndex) {
                Point targetPoint = values.get(i);
                int clusterIndex = KmeanCommon.getIndexClosestCentroid(targetPoint, this.centroids);
                var clusterAccumulator = this.clusterAccumulators.get(clusterIndex);
                clusterAccumulator.accX.accumulateAndGet(targetPoint.getX(), Integer::sum);
                clusterAccumulator.accY.accumulateAndGet(targetPoint.getY(), Integer::sum);
                clusterAccumulator.accZ.accumulateAndGet(targetPoint.getZ(), Integer::sum);
                clusterAccumulator.couting.incrementAndGet();
            }
        }
    }

}
