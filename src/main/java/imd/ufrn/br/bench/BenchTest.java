package imd.ufrn.br.bench;

import imd.ufrn.br.entities.Point;
import imd.ufrn.br.kmeans.strategies.*;
import imd.ufrn.br.view.Input;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class BenchTest {
    @Param({"5", "25"})
    int K;

    @Param({"8"})
    int cores = 8;

    static final List<Point> points = new ArrayList<>();

    static public List<Point> getInputValues() throws IOException {
        if (points.isEmpty()) {
            Path path = Path.of("input.csv");
            points.addAll(Input.read(Files.newInputStream(path)));
        }

        return points;
    }

    public List<Point> extractInitialCenters(int k) {
        return points.stream().distinct().limit(k).toList();
    }

    //@Benchmark
    //@Fork(value = 1)
    //@Warmup(iterations = 2)
    //@BenchmarkMode(Mode.AverageTime)
    //@OutputTimeUnit(TimeUnit.SECONDS)
    //public void adder(Blackhole bh) throws IOException {
    //    var K = this.K;
    //    var values = BenchTest.getInputValues();
    //    var initialCenters = this.extractInitialCenters(K);
    //    var threadMode = ThreadMode.PLATAFORM;
    //    var output = new KmeansAdder(threadMode, this.cores).execute(values, K, initialCenters);
    //    bh.consume(output);
    //}
    //
    //@Benchmark
    //@Fork(value = 1)
    //@Warmup(iterations = 2)
    //@BenchmarkMode(Mode.AverageTime)
    //@OutputTimeUnit(TimeUnit.SECONDS)
    //public void adder_executor(Blackhole bh) throws IOException {
    //    var K = this.K;
    //    var values = BenchTest.getInputValues();
    //    var initialCenters = this.extractInitialCenters(K);
    //    var output = new KmeansAdderExecutor(this.cores).execute(values, K, initialCenters);
    //    bh.consume(output);
    //}
    //
    //@Benchmark
    //@Fork(value = 1)
    //@Warmup(iterations = 2)
    //@BenchmarkMode(Mode.AverageTime)
    //@OutputTimeUnit(TimeUnit.SECONDS)
    //public void adder_forkjoin(Blackhole bh) throws IOException {
    //    var K = this.K;
    //    var values = BenchTest.getInputValues();
    //    var initialCenters = this.extractInitialCenters(K);
    //    var output = new KmeansAdderForkJoin(this.cores).execute(values, K, initialCenters);
    //    bh.consume(output);
    //}
    //
    //@Benchmark
    //@Fork(value = 1)
    //@Warmup(iterations = 2)
    //@BenchmarkMode(Mode.AverageTime)
    //@OutputTimeUnit(TimeUnit.SECONDS)
    //public void adder_parallel_stream(Blackhole bh) throws IOException {
    //    var K = this.K;
    //    var values = BenchTest.getInputValues();
    //    var initialCenters = this.extractInitialCenters(K);
    //    var output = new KmeansAdderParallelStream().execute(values, K, initialCenters);
    //    bh.consume(output);
    //}

    @Benchmark
    @Fork(value = 1)
    @Warmup(iterations = 2)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void adder_strucutured_conc(Blackhole bh) throws IOException {
        var K = this.K;
        var values = BenchTest.getInputValues();
        var initialCenters = this.extractInitialCenters(K);
        var output = new KmeansAdderStructuredConc(this.cores).execute(values, K, initialCenters);
        bh.consume(output);
    }

    //@Benchmark
    //@Fork(value = 1)
    //@Warmup(iterations = 2)
    //@BenchmarkMode(Mode.AverageTime)
    //@OutputTimeUnit(TimeUnit.SECONDS)
    //public void adder_concurrent_collections(Blackhole bh) throws IOException {
    //    var K = this.K;
    //    var values = BenchTest.getInputValues();
    //    var initialCenters = this.extractInitialCenters(K);
    //    var output = new KmeansConcurrentCollections().execute(values, K, initialCenters);
    //    bh.consume(output);
    //}
}
