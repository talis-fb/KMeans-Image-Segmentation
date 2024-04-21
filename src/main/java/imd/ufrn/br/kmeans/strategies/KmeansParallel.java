package imd.ufrn.br.kmeans.strategies;

import imd.ufrn.br.entities.Cluster;
import imd.ufrn.br.entities.Point;
import imd.ufrn.br.kmeans.KmeanCommon;
import imd.ufrn.br.kmeans.KmeanStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class KmeansParallel implements KmeanStrategy {
    public int threads = 1;

    public ThreadMode mode = ThreadMode.PLATAFORM;

    public KmeansParallel(ThreadMode mode, int threads) {
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

        System.out.println("AAAAAAAAAAAAAAAAAAaa");

        for (int i = 0; i < this.threads; i++) {
            if (i >= values.size())
                continue;

            int indexOfThread = i;

            Thread thread = threadBuilder.start(() -> {
                int initialIndex = indexOfThread;
                // System.out.println(indexOfThread + ") >>> AQUI:  " + initialIndex);

                while (true) {
                    System.out.println(indexOfThread + ") restart loop");

                    int index = initialIndex;
                    // System.out.println(indexOfThread + ") >>> AQUI while index:  " + index);
                    // System.out.println(indexOfThread + ") >>> AQUI initial:  " + initialIndex);

                    try {
                        // System.out.println("ini semap " + indexOfThread);
                        System.out.println(indexOfThread + ") wait semaphore init");
                        semaphoreInitThreadComputation.acquire();
                        System.out.println(indexOfThread + ") acquire semaphore init");


                        if (hasFinished.get())
                            break;

                        while (index < values.size()) {
                            var targetPoint = values.get(index);
                            int indexClosestCluster = KmeanCommon.getIndexClosestCluster(targetPoint, clusters);

                            Cluster targetCluster = clusters.get(indexClosestCluster);
                            // System.out.println("(" + indexOfThread + ")" + "para add += "+ indexClosestCluster + " / " + targetPoint.getLabel());
                            System.out.println(indexOfThread + ") adding in C "+ indexClosestCluster +" =" + targetPoint);
                            targetCluster.addPointSync(targetPoint);

                            //synchronized (targetCluster) {
                              //  targetCluster.addPoint(targetPoint);
                            // }

                            index += this.threads;
                        }

                        semaphoreEndThreadComputation.release();
                        System.out.println(indexOfThread + ") release semaphore end");

                        // System.out.println("### FIM DE ### " + indexOfThread);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    // System.out.println(indexOfThread + ") >>> AQUI while index:  " + index);
                }


            });

            threads.add(thread);
        }

        while (true) {
            try {
                System.out.println("[MAIN] wait end semaphores");
                semaphoreEndThreadComputation.acquire(this.threads);
                System.out.println("[MAIN] acquire end semaphores");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }


            /*System.out.println("esperando " + this.threads);
            System.out.println("eita " + coutingThreadFinished.get());
            System.out.println("aq");
            while (coutingThreadFinished.get() < this.threads) {
                System.out.println("foi? " + coutingThreadFinished.get());
                // waiting
            }*/

            var oldCenters = new ArrayList<Point>();
            var newCenters = new ArrayList<Point>();

            for (Cluster cluster : clusters) {
                oldCenters.add(cluster.getCenter());
                newCenters.add(cluster.calculateCenterPoint());
            }

            System.out.println("[MAIN] Init conv");
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

                System.out.println("FINAL");
                for (var c : clusters){
                    System.out.println(c.getCenter());
                    System.out.println(c.getPoints().stream().map(Point::display).toList());
                }

                return clusters;
            }
            System.out.println("[MAIN] end conv");

            for (int i = 0; i < newCenters.size(); i++) {
                var cluster = clusters.get(i);
                var newCenter = newCenters.get(i);
                cluster.setCenter(newCenter);
                cluster.getPoints().clear();
            }

            System.out.println("[MAIN] restart init semaphores");
            semaphoreInitThreadComputation.release(this.threads);
        }
    }

}