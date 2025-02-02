package imd.ufrn.br.starters;

import com.fasterxml.jackson.databind.ObjectMapper;
import imd.ufrn.br.entities.Point;
import imd.ufrn.br.kmeans.KmeanStrategy;
import imd.ufrn.br.kmeans.strategies.KmeansAdder;
import imd.ufrn.br.kmeans.strategies.ThreadMode;
import imd.ufrn.br.starters.server.MsgInput;
import imd.ufrn.br.starters.server.RequestHandler;
import imd.ufrn.br.view.Input;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TCPServer {
    static ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        System.out.println("READING VALUES");
        List<Point> values = Input.read(System.in);


        System.out.println("Init server");
        ServerSocket server = null;
        try {
            server = new ServerSocket(8080);
            System.out.println("Listen In 8080");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        KmeanStrategy kmeanStrategy = new KmeansAdder(ThreadMode.PLATAFORM, 8);

        while (true) {
            try {
                Socket nextClient = server.accept();

                DataInputStream input = new DataInputStream(nextClient.getInputStream());
                DataOutputStream output = new DataOutputStream(nextClient.getOutputStream());

                var handler = new RequestHandler(nextClient, input, output, values, kmeanStrategy);

                Thread.ofVirtual().start(handler);
            } catch (IOException e) {
                System.err.println("Error around accept...");
                e.printStackTrace();
            }
        }
    }
}
