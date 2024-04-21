package imd.ufrn.br.bench;

import imd.ufrn.br.entities.Point;
import imd.ufrn.br.kmeans.strategies.*;
import imd.ufrn.br.view.Input;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class BenchTest {
    @Param({"5"})
    int K;

    @Param({"8"})
    int cores = 8;

    public List<Point> getInputValues() throws IOException {
        Path path = Path.of("input.csv");
        return Input.read(Files.newInputStream(path));
    }

    public List<Point> extractInitialCenters(List<Point> values, int k) {
        return values.stream().distinct().limit(k).toList();
    }

    /*
    @Benchmark
    @Fork(value = 1)
    @Warmup(iterations = 5)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void serial(Blackhole bh) throws IOException {
        var K = this.K;
        var values = this.getInputValues();
        var initialCenters = this.extractInitialCenters(values, K);
        var output = new KmeansSerial().execute(values, K, initialCenters);
        bh.consume(output);
    }

    @Benchmark
    @Fork(value = 1)
    @Warmup(iterations = 5)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void serialStream(Blackhole bh) throws IOException {
        var K = this.K;
        var values = this.getInputValues();
        var initialCenters = this.extractInitialCenters(values, K);
        var output = new KmeansSerialStreams().execute(values, K, initialCenters);
        bh.consume(output);
    }
    */

    @Benchmark
    @Fork(value = 1)
    @Warmup(iterations = 5)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void parallelWithPlataform(Blackhole bh) throws IOException {
        var K = this.K;
        var values = this.getInputValues();
        var initialCenters = this.extractInitialCenters(values, K);
        var output = new KmeansParallel(ThreadMode.PLATAFORM, this.cores).execute(values, K, initialCenters);
        bh.consume(output);
    }

    @Benchmark
    @Fork(value = 1)
    @Warmup(iterations = 5)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void parallelWithVirtual(Blackhole bh) throws IOException {
        var K = this.K;
        var values = this.getInputValues();
        var initialCenters = this.extractInitialCenters(values, K);
        var output = new KmeansParallel(ThreadMode.VIRTUAL, this.cores).execute(values, K, initialCenters);
        bh.consume(output);
    }

    /* @Benchmark
    @Fork(value = 1)
    @Warmup(iterations = 5)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void parallelStream(Blackhole bh) throws IOException {
        var K = this.K;
        var values = this.getInputValues();
        var initialCenters = this.extractInitialCenters(values, K);
        var output = new KmeansParallelStream().execute(values, K, initialCenters);
        bh.consume(output);
    }*/

}
