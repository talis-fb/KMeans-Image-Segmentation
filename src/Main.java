import java.util.ArrayList;
import java.util.List;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        var data = new ArrayList<>(List.of(
                new Point(1, 2, 0),
                new Point(2, 3, 0),
                new Point(8, 10, 0),
                new Point(9, 11, 0),
                new Point(10, 12, 0)
        ));

        var K = 2;

        var kmeans = new KmeansSerialBuilder();
        kmeans.setValues(data);
        kmeans.setK(K);

        var output = kmeans.execute();

        System.out.println("Saida...");
        for (var cluster : output) {
            System.out.println(cluster.toString());
        }
    }
}