package imd.ufrn.br.starters;

import com.fasterxml.jackson.databind.ObjectMapper;
import imd.ufrn.br.entities.Point;
import imd.ufrn.br.starters.server.MsgInput;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class TCPServer {
    public static void main(String[] args) {
        ObjectMapper objectMapper = new ObjectMapper();
        ServerSocket server = null;
        try {
            server = new ServerSocket(8080);
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        while (true) {
            try {
                Socket nextClient = server.accept();
                InputStream inputStream = nextClient.getInputStream();

                MsgInput input = objectMapper.readValue(inputStream, MsgInput.class);

                System.out.println("ele " + input);

                OutputStream outputStream = nextClient.getOutputStream();

                objectMapper.writeValue(outputStream, input);
            } catch (Exception e) {
                System.out.println("Quebour " + e);
                e.printStackTrace();
                try {
                    server.close();
                    System.out.println("FECHOU");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                break;
            }
        }
    }
}
