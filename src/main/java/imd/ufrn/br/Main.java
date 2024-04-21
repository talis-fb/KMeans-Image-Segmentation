package imd.ufrn.br;

import imd.ufrn.br.common.ClustersUtils;
import imd.ufrn.br.common.PointsUtils;
import imd.ufrn.br.entities.Cluster;
import imd.ufrn.br.entities.Point;
import imd.ufrn.br.kmeans.strategies.*;
import imd.ufrn.br.view.Input;

import java.util.List;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        var K = 5;
        List<Point> values = Input.read(System.in);
        List<Point> initialCenters = PointsUtils.extractDistintInitialValues(values, K);

        var kmeansRunner = new KmeansParallelSemaphore(ThreadMode.PLATAFORM, 8);

        System.err.println("MODO: " + kmeansRunner.getClass().getName());
        System.err.println("Centers");
        System.err.println(initialCenters);

        List<Cluster> output = kmeansRunner.execute(values, K, initialCenters);
        var pointsFinal = ClustersUtils.extractAllPointsWithCenterValues(output);

        long elapsedTime = System.currentTimeMillis() - startTime;
        System.out.println("Time since startup: " + elapsedTime + " milliseconds");

        for (var point : pointsFinal) {
            System.out.println(point.display());
        }
    }
}