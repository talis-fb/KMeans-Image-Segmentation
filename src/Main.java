import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        Scanner sca = new Scanner(System.in);
        sca.useDelimiter("\n");

        var points = new ArrayList<Point>();

        while (sca.hasNext()){
            String[] parts = sca.next().trim().split("\\s+");
            String label = parts[0];
            int red = Integer.parseInt(parts[1]);
            int green = Integer.parseInt(parts[2]);
            int blue = Integer.parseInt(parts[3]);
            points.add(new Point(label, red, green, blue));
        }

        // System.out.println("saida");
        // System.out.println(points);

        var K = 20;

        var kmeans = new KmeansSerialBuilder();
        kmeans.setValues(points);
        kmeans.setK(K);

        var output = kmeans.execute();

        var pointsFinal = output
            .stream()
            .flatMap(cluster ->
                cluster.points
                    .stream()
                    .map(point ->
                        new Point(point.getLabel().orElse("--"), cluster.center.getX(), cluster.center.getY(), cluster.center.getZ())
                    )
            ).toList();

        // System.out.println("Saida...");
        for (var point : pointsFinal) {
            System.out.println(point.display());
        }
    }
}