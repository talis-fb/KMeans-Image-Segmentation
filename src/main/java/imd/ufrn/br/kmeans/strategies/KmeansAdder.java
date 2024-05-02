package imd.ufrn.br.kmeans.strategies;

import imd.ufrn.br.entities.Cluster;
import imd.ufrn.br.entities.Point;
import imd.ufrn.br.kmeans.KmeanCommon;
import imd.ufrn.br.kmeans.KmeanStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.LongAdder;

public class KmeansAdder implements KmeanStrategy {
    public int threads = 1;
    public ThreadMode mode = ThreadMode.PLATAFORM;

    public KmeansAdder(ThreadMode mode, int threads) {
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

            List<CoutingClusterValues> counters = clusters.stream().map(el -> {
                var couting = new CoutingClusterValues();
                couting.accX = new LongAdder();
                couting.accY = new LongAdder();
                couting.accZ = new LongAdder();
                couting.couting = new LongAdder();
                return couting;
            }).toList();

            for (int i = 0 ; i < this.threads; i++) {
                ThreadsRunner threadRunner = new ThreadsRunner();
                threadRunner.setInitialIndex(i);
                threadRunner.setIntervalIndex(this.threads);
                threadRunner.setValues(values);
                threadRunner.setCentroids(centroids);
                threadRunner.setClustersAccumulates(counters);
                threadsRunning.add(threadBuilder.start(threadRunner));
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
            List<Point> newCenters = counters.stream().map(el -> {
                var size = el.couting.sum();
                if (size == 0)
                    return new Point(0,0,0);

                var meanX = el.accX.sum() / size;
                var meanY = el.accY.sum() / size;
                var meanZ = el.accZ.sum() / size;
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

    private class CoutingClusterValues {
        public LongAdder accX;
        public LongAdder accY;
        public LongAdder accZ;
        public LongAdder couting;
    }

    private class ThreadsRunner implements Runnable {
        private int initialIndex;
        private int intervalIndex;
        private List<Point> values;
        private List<Point> centroids;
        private List<CoutingClusterValues> clustersAccumulates;

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

        public void setClustersAccumulates(List<CoutingClusterValues> clustersAccumulates) {
            this.clustersAccumulates = clustersAccumulates;
        }

        @Override
        public void run() {
            int iteration = this.initialIndex;
            while (iteration < this.values.size()) {
                Point targetPoint = values.get(iteration);
                System.out.println("Thread: " + this.initialIndex + " iteration " + iteration +
                        "\n\t value: "+ targetPoint +
                        "\n\t centroids: "+ this.centroids
                );
                int clusterIndex = KmeanCommon.getIndexClosestCentroid(targetPoint, this.centroids);

                var him = this.clustersAccumulates.get(clusterIndex);

                him.accX.add(targetPoint.getX());
                him.accY.add(targetPoint.getY());
                him.accZ.add(targetPoint.getZ());

                him.couting.increment();

                System.out.println("Thread: " + this.initialIndex + " iteration " + iteration +
                        " \n\t cluster(" + clusterIndex +
                        ") : " + him.accX +"," + him.accY + ", " + him.accZ);

                iteration += this.intervalIndex;
            }
        }
    }

}
