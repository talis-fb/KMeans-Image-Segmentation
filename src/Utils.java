import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Utils {
    public static <T> List<T> extractRandomElementFromList(List<T> originalList, int n) {
        var copyList = new ArrayList<>(originalList);
        Collections.shuffle(copyList);
        return copyList.subList(0, n);
    }
}
