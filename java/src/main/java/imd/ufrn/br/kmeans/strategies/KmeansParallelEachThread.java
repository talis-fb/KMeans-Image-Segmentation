package imd.ufrn.br.kmeans.strategies;

import imd.ufrn.br.entities.Cluster;
import imd.ufrn.br.entities.Point;
import imd.ufrn.br.kmeans.KmeanCommon;
import imd.ufrn.br.kmeans.KmeanStrategy;

import java.util.ArrayList;
import java.util.List;

public class KmeansParallelEachThread implements KmeanStrategy {
    public int threads = 1;

    public ThreadMode mode = ThreadMode.PLATAFORM;

    public KmeansParallelEachThread(ThreadMode mode, int threads) {
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

        while (true) {
            List<Thread> threads = new ArrayList<>();
            for (int i = 0 ; i < this.threads; i++) {
                int indexOfThread = i;
                Thread thread = threadBuilder.start(() -> {
                    int iteration = indexOfThread;
                    while (iteration < values.size()) {
                        var targetPoint = values.get(iteration);
                        int indexClosestCluster = KmeanCommon.getIndexClosestCluster(targetPoint, clusters);
                        Cluster targetCluster = clusters.get(indexClosestCluster);
                        synchronized (targetCluster) {
                            targetCluster.addPoint(targetPoint);
                        }

                        iteration += this.threads;
                    }
                });

                threads.add(thread);
            }

            for (Thread thread : threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            var oldCenters = new ArrayList<Point>();
            var newCenters = new ArrayList<Point>();

            for (Cluster cluster : clusters) {
                oldCenters.add(cluster.getCenter());
                newCenters.add(cluster.calculateCenterPoint());
            }

            if (KmeanCommon.converged(newCenters, oldCenters)) {
                return clusters;
            }

            for (int i = 0; i < newCenters.size(); i++) {
                var cluster = clusters.get(i);
                var newCenter = newCenters.get(i);
                cluster.setCenter(newCenter);
                cluster.getPoints().clear();
            }
        }
    }

}

