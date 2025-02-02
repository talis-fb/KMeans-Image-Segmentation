package imd.ufrn.br.starters.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import imd.ufrn.br.entities.Cluster;
import imd.ufrn.br.entities.Point;
import imd.ufrn.br.kmeans.KmeanStrategy;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RequestHandler implements Runnable {
    Socket socket;

    DataInputStream input;
    DataOutputStream output;

    List<Point> values;
    KmeanStrategy kmeanStrategy;
    ObjectMapper objectMapper = new ObjectMapper();

    public RequestHandler(Socket socket, DataInputStream input, DataOutputStream output, List<Point> values, KmeanStrategy kmeanStrategy) {
        this.socket = socket;
        this.input = input;
        this.output = output;
        this.values = values;
        this.kmeanStrategy = kmeanStrategy;
    }

    @Override
    public void run() {
        try {
            var inputLine = this.input.readLine();
            MsgInput inputMsg = objectMapper.readValue(inputLine, MsgInput.class);

            var k = inputMsg.k;
            var initialPoints = MsgInput.fromInputToPoints(inputMsg);

            List<Cluster> clusters = this.kmeanStrategy.execute(this.values, k, initialPoints);
            List<Point> centroids = clusters.stream().map(Cluster::getCenter).toList();
            List<MsgInput.InputPoint> centroidsOutput = MsgInput.fromPointsToInputs(centroids);

            this.output.writeUTF(objectMapper.writeValueAsString(centroidsOutput));
            this.socket.close();
        } catch (IOException e) {
            System.err.println("ERRO THREAD");
            e.printStackTrace();
            try {
                this.output.writeUTF("ERROR IO: " + e.getMessage());
                this.socket.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            throw new RuntimeException(e);
        }

    }
}
