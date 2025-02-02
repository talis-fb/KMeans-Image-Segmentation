package imd.ufrn.br.kmeans.strategies;

import imd.ufrn.br.entities.Cluster;
import imd.ufrn.br.entities.Point;
import imd.ufrn.br.kmeans.KmeanCommon;
import imd.ufrn.br.kmeans.KmeanStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

public class KmeansParallelSemaphore implements KmeanStrategy {
    public int threads = 1;

    public ThreadMode mode = ThreadMode.PLATAFORM;

    public KmeansParallelSemaphore(ThreadMode mode, int threads) {
        this.mode = mode;
        this.threads = threads;
    }

    @Override
    public List<Cluster> execute(List<Point> values, int k, List<Point> initialCenters) {
        List<Cluster> clusters = new ArrayList<>();
        for (Point center : initialCenters) {
            clusters.add(new Cluster(center, new ArrayList<>()));
        }

        var hasFinished = new AtomicBoolean(false);

        var semaphoreInitThreadComputation = new Semaphore(this.threads);
        var semaphoreEndThreadComputation = new Semaphore(0);

        var threadBuilder = switch (this.mode) {
            case VIRTUAL -> Thread.ofVirtual();
            case PLATAFORM -> Thread.ofPlatform();
        };

        List<Thread> threads = new ArrayList<>();


        for (int i = 0; i < this.threads; i++) {
            int indexOfThread = i;

            Thread thread = threadBuilder.start(() -> {
                int initialIndex = indexOfThread;

                while (true) {

                    int index = initialIndex;

                    try {
                        semaphoreInitThreadComputation.acquire();

                        if (hasFinished.get())
                            break;

                        while (index < values.size()) {
                            var targetPoint = values.get(index);
                            int indexClosestCluster = KmeanCommon.getIndexClosestCluster(targetPoint, clusters);

                            Cluster targetCluster = clusters.get(indexClosestCluster);
                            targetCluster.addPointSync(targetPoint);

                            index += this.threads;
                        }

                        semaphoreEndThreadComputation.release();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }


            });

            threads.add(thread);
        }

        while (true) {
            try {
                semaphoreEndThreadComputation.acquire(this.threads);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }


            var oldCenters = new ArrayList<Point>();
            var newCenters = new ArrayList<Point>();

            for (Cluster cluster : clusters) {
                oldCenters.add(cluster.getCenter());
                newCenters.add(cluster.calculateCenterPoint());
            }

            if (KmeanCommon.converged(newCenters, oldCenters)) {
                hasFinished.set(true);
                semaphoreInitThreadComputation.release(this.threads);

                for (Thread thread : threads) {
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                for (var c : clusters){
                    System.out.println(c.getCenter());
                    System.out.println(c.getPoints().stream().map(Point::display).toList());
                }

                return clusters;
            }

            for (int i = 0; i < newCenters.size(); i++) {
                var cluster = clusters.get(i);
                var newCenter = newCenters.get(i);
                cluster.setCenter(newCenter);
                cluster.getPoints().clear();
            }

            semaphoreInitThreadComputation.release(this.threads);
        }
    }

}