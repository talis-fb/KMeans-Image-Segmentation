# Concurrent K-Means Image Segmentation in Rust and Java

[![Technologies](https://skillicons.dev/icons?i=rust,java)]()

This repository is part of a comparative study on concurrent programming strategies for **K-Means clustering applied to image segmentation**. The study explores and contrasts different **concurrency models and techniques** in Rust and Java, analyzing their performance, synchronization mechanisms, and runtime characteristics.

## Overview

This project implements various K-Means clustering algorithms for image segmentation in both Rust and Java, focusing on different parallelization strategies. The goal is to evaluate the efficiency and scalability of each approach when applied to image processing and study purposes.

It's used the [Image to CSV CLI project](https://github.com/talis-fb/img-to-csv) to convert an image to CSV, process it with kmeans for segmentation, and convert the result back to an image.

## Image Segmentations with Different K Values

To get a concrete example of result of this program...

<table>
  <tr>
    <td>
      <table>
        <tr><th>Base Image</th></tr>
        <tr><td><img src="https://res.cloudinary.com/dfjn94vg8/image/upload/v1716473684/kmeans/input.jpg" height="300px"></td></tr>
      </table>
    </td>
    <td>
      <table>
        <tr><th>K</th><th>Image</th></tr>
        <tr><td>2</td><td><img src="https://res.cloudinary.com/dfjn94vg8/image/upload/v1716473684/kmeans/output_2.jpg" height="250px"></td></tr>
        <tr><td>5</td><td><img src="https://res.cloudinary.com/dfjn94vg8/image/upload/v1716473684/kmeans/output_5.jpg" height="250px"></td></tr>
        <tr><td>10</td><td><img src="https://res.cloudinary.com/dfjn94vg8/image/upload/v1716473684/kmeans/output_10.jpg" height="250px"></td></tr>
        <tr><td>25</td><td><img src="https://res.cloudinary.com/dfjn94vg8/image/upload/v1716473684/kmeans/output_25.jpg" height="250px"></td></tr>
        <tr><td>60</td><td><img src="https://res.cloudinary.com/dfjn94vg8/image/upload/v1716473684/kmeans/output_60.jpg" height="250px"></td></tr>
      </table>
    </td>
  </tr>
</table>

## Strategies and Tools Used

### Java Implementation

In the Java implementation, multiple strategies were employed to handle concurrency, including:

* **Thread Synchronization Techniques**: Various approaches such as locks, synchronized blocks, and atomic operations were explored.

* **Virtual Threads vs. Native Threads**: Performance was compared between traditional Java threads and virtual threads introduced in newer Java versions.

* **Performance Profiling and Benchmarking**:

  * JMH (Java Microbenchmark Harness): Used to benchmark different concurrency approaches.

  * JMC (Java Mission Control): Employed for detailed profiling and runtime analysis.

  * Java Profiling Tools: Used to inspect thread behavior, contention, and memory usage.

### Rust Implementation

In the Rust implementation, different concurrency models were tested:

  * **Tokio Runtime**: An asynchronous runtime for handling concurrent tasks.

  * **Rayon**: A data parallelism library optimized for multi-threading.

  * **Rust Standard Library (std::thread)**: A more traditional multi-threading approach.

Each runtime was analyzed based on execution speed, CPU utilization, and memory consumption.

### Example Workflow
As the tool uses STDIN and STDOUT for communication, you can use pipes and redirection to integrate it with `img-to-csv`.

This example processes an input_image.jpg image file and creates another image file called output_final_image.png, applying KMeans image segmentation with K equals 5:
```sh
img-to-csv to-csv input_image.jpg | kmeans -K 5 -m parallel | img-to-csv to-image -o output_final_image.png
```

### Workflow step-by-step
1. Convert image to CSV:
```sh
img-to-csv to-csv input_image.jpg > image.csv
```
2. Apply KMeans clustering:
```sh
kmeans -K 5 -m parallel < image.csv > segmented_image.csv
```
3. Convert CSV back to image:
```sh
img-to-csv to-image -o output_image.jpg < segmented_image.csv
```

## How It Works
The tool processes CSV files where each line represents a pixel's coordinates (X and Y) and RGB values:
```
X:Y R G B
```
* Input: CSV format from STDIN.
* Output: CSV format to STDOUT with modified RGB values representing cluster centers.

## Benchmarks

You can check the complete study [HERE](https://github.com/talis-fb/KMeans-Image-Segmentation/blob/master/Study_Report.pdf). 

| Language | Implementation | Threading Mode | Milliseconds |
|---|---|---|---|
| ![Technologies](https://skillicons.dev/icons?i=java) | Adder | Platform | 46 |
| ![Technologies](https://skillicons.dev/icons?i=rust) | Rayon (parallel iterators) | Platform | 48 |
| ![Technologies](https://skillicons.dev/icons?i=java) | Adder Confinement | Platform | 51 |
| ![Technologies](https://skillicons.dev/icons?i=rust) | Join (STD) | Native OS | 215.15 |
| ![Technologies](https://skillicons.dev/icons?i=java) | ParallelStream | - | 243 |
| ![Technologies](https://skillicons.dev/icons?i=rust) | Join (Tokio) | Green Threads | 333.45 |
| ![Technologies](https://skillicons.dev/icons?i=java) | Volatile | Virtual | 384.998 |
| ![Technologies](https://skillicons.dev/icons?i=java) | Lock And<br>Semaphore | Virtual | 392 |
| ![Technologies](https://skillicons.dev/icons?i=java) | Lock | Virtual | 395 |
| ![Technologies](https://skillicons.dev/icons?i=java) | Volatile | Platform | 396 |
| ![Technologies](https://skillicons.dev/icons?i=java) | Join | Platform | 397 |
| ![Technologies](https://skillicons.dev/icons?i=java) | Lock | Platform | 399 |
| ![Technologies](https://skillicons.dev/icons?i=java) | Lock And<br>Semaphore | Platform | 405 |
| ![Technologies](https://skillicons.dev/icons?i=java) | Semaphore | Virtual | 406 |
| ![Technologies](https://skillicons.dev/icons?i=java) | Semaphore | Platform | 408 |
| ![Technologies](https://skillicons.dev/icons?i=java) | Join | Virtual | 419 |
| ![Technologies](https://skillicons.dev/icons?i=rust) | Mutex | Native OS | + 900 |
