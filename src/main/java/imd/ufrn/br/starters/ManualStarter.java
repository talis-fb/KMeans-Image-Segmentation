package imd.ufrn.br.starters;

import imd.ufrn.br.common.PointsUtils;
import imd.ufrn.br.entities.Cluster;
import imd.ufrn.br.entities.Point;
import imd.ufrn.br.kmeans.strategies.*;
import imd.ufrn.br.view.Input;

import java.util.List;

public class ManualStarter {
    public static void main(String[] args) {
        System.err.println(" [BEGIN] ");

        // Check memory available
        long heapFreeSize = Runtime.getRuntime().freeMemory();
        long totalFreeMemoryInMB = heapFreeSize / (1024 * 1024);
        long heapMaxSize = Runtime.getRuntime().maxMemory();
        long maxMemoryInMB = heapMaxSize / (1024 * 1024);
        System.out.println("Heap Max Size = " + maxMemoryInMB);
        System.out.println("Heap Available = " + totalFreeMemoryInMB);

        // Start couting
        long timeMsStart = System.currentTimeMillis();

        var K = 5;
        List<Point> values = Input.read(System.in);
        List<Point> initialCenters = PointsUtils.extractDistintInitialValues(values, K);

        System.err.println("Get values");
        var threadMode = ThreadMode.PLATAFORM;
        var kmeansRunner = new KmeansSerialStreams();
        // var kmeansRunner = new KmeansParallelStream();

        System.err.println("MODO: " + kmeansRunner.getClass().getName());
        System.err.println("  ThreadMode : " + threadMode);

        long elapsedTimeStartKmeans = System.currentTimeMillis() - timeMsStart;
        System.out.println("Time before Kmeans: " + elapsedTimeStartKmeans);

        List<Cluster> output = kmeansRunner.execute(values, K, initialCenters);

        long elapsedTimeMsEnd = System.currentTimeMillis() - timeMsStart;
        System.out.println("Time total MS: " + elapsedTimeMsEnd);

    }
}