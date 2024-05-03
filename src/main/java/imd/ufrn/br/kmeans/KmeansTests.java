package imd.ufrn.br.kmeans;

import imd.ufrn.br.common.ClustersUtils;
import imd.ufrn.br.entities.Cluster;
import imd.ufrn.br.entities.Point;
import imd.ufrn.br.kmeans.strategies.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KmeansTests {
    static Stream<KmeanStrategy> allKmeansStrategy() {
        return Stream.of(
                new KmeansSerial(),
                // new KmeansParallel(ThreadMode.PLATAFORM, 1),
                // new KmeansParallel(ThreadMode.VIRTUAL, 1),

                new KmeansParallelSemaphore(ThreadMode.PLATAFORM, 1),
                new KmeansParallelSemaphore(ThreadMode.VIRTUAL, 1),

                new KmeansParallelLock(ThreadMode.PLATAFORM, 2),
                new KmeansParallelLock(ThreadMode.VIRTUAL, 2),

                // new KmeansParallelLockAndSemaphore(ThreadMode.PLATAFORM, 2),
                // new KmeansParallelLockAndSemaphore(ThreadMode.VIRTUAL, 2),

                new KmeansParallelVolatile(ThreadMode.PLATAFORM, 8),


                new KmeansParallelEachThread(ThreadMode.VIRTUAL, 2),
                new KmeansParallelEachThread(ThreadMode.VIRTUAL, 8),
                new KmeansParallelEachThread(ThreadMode.PLATAFORM, 2),
                new KmeansParallelEachThread(ThreadMode.PLATAFORM, 8),

                new KmeansAdder(ThreadMode.PLATAFORM, 8),
                new KmeansAdderFixedPool( 8),

                new KmeansAtomic(ThreadMode.PLATAFORM, 8),

                new KmeansParallelStream(),
                new KmeansSerialStreams(),
                new KmeansParallelStreamMap()
        );
    }

    static void assertSameListOfCluster(List<Cluster> cluster1, List<Cluster> cluster2) {
        List<Point> pointsCluster1 = ClustersUtils.extractAllPointsWithCenterValues(cluster1);
        List<Point> pointsCluster2 = ClustersUtils.extractAllPointsWithCenterValues(cluster2);

        List<String> valuesPointsCluster1 = pointsCluster1.stream().map(Point::display).toList();
        List<String> valuesPointsCluster2 = pointsCluster2.stream().map(Point::display).toList();

        HashSet<String> setPointsCluster1 = new HashSet<>(valuesPointsCluster1);
        HashSet<String> setPointsCluster2 = new HashSet<>(valuesPointsCluster2);

        assertEquals(setPointsCluster1, setPointsCluster2);
    }

    @ParameterizedTest
    @MethodSource("allKmeansStrategy")
    void testExecuteWithTwoPoints(KmeanStrategy kmeansStrategy) {
        var values = new ArrayList<>(List.of(
                new Point(1, 2),
                new Point(5, 8)
        ));
        var K = 2;
        var initialCenters = values.stream().limit(K).toList();

        var output = kmeansStrategy.execute(values, K, initialCenters);
        var expectedClusters = List.of(
                new Cluster(new Point(1, 2), List.of(new Point(1, 2))),
                new Cluster(new Point(5, 8), List.of(new Point(5, 8)))
        );

        assertSameListOfCluster(expectedClusters,output );
    }

    @ParameterizedTest
    @MethodSource("allKmeansStrategy")
    void testExecuteFewPoints(KmeanStrategy kmeansStrategy) {
        var values = new ArrayList<>(List.of(
                new Point(1, 2),
                new Point(2, 3),
                new Point(8, 10),
                new Point(9, 11),
                new Point(10, 12)
        ));
        var K = 2;
        var initialCenters = values.stream().limit(K).toList();

        var output = kmeansStrategy.execute(values, K, initialCenters);
        var expectedClusters = List.of(
                new Cluster(new Point(1, 2), List.of(
                        new Point(1, 2),
                        new Point(2, 3)
                )),
                new Cluster(new Point(9, 11), List.of(
                        new Point(8, 10),
                        new Point(9, 11),
                        new Point(10, 12)
                ))
        );

        assertSameListOfCluster(expectedClusters, output);
    }

    @ParameterizedTest
    @MethodSource("allKmeansStrategy")
    void testExecuteThreeClusters(KmeanStrategy kmeansStrategy) {
        var values = new ArrayList<>(List.of(
                new Point("p1", 1, 1),
                new Point("p2", 2, 2),
                new Point("p3", 8, 8),
                new Point("p4", 9, 9),
                new Point("p5", 20, 20),
                new Point("p6", 21, 21)
        ));
        var K = 3;
        var initialCenters = values.stream().limit(K).toList();

        var output = kmeansStrategy.execute(values, K, initialCenters);
        var expectedClusters = List.of(
                new Cluster(new Point(1, 1), List.of(
                        new Point("p1", 1, 1),
                        new Point("p2", 2, 2)
                )),
                new Cluster(new Point(8, 8), List.of(
                        new Point("p3", 8, 8),
                        new Point("p4", 9, 9)
                )),
                new Cluster(new Point(20, 20), List.of(
                        new Point("p5", 20, 20),
                        new Point("p6", 21, 21)
                ))
        );

        assertSameListOfCluster(expectedClusters,output );
    }
}