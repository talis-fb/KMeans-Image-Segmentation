import java.util.List;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {

        long startTime = System.currentTimeMillis();

        List<Point> values = Input.read(System.in);

        var K = 15;
        List<Point> initialCenters = values.stream().limit(K).toList();

        // System.err.println("Centers");
        // System.err.println(initialCenters);

        var kmeansRunner = new KmeansParallelMapBuilder();
        var output = kmeansRunner.execute(values, K, initialCenters);

        var pointsFinal = output.stream()
                .flatMap(
                    cluster -> cluster
                        .points
                        .stream()
                        .map(point -> new Point(
                                point.getLabel().orElse("--"),
                                cluster.center.getX(),
                                cluster.center.getY(),
                                cluster.center.getZ()
                            )
                        )
                )
                .toList();


        long elapsedTime = System.currentTimeMillis() - startTime;
        System.out.println("Time since startup: " + elapsedTime + " milliseconds");

        for (var point : pointsFinal) {
            System.out.println(point.display());
        }

        // System.out.println("Time after log: " + elapsedTime + " milliseconds");

    }
}