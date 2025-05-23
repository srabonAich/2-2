import java.io.*;
import java.net.*;

public class Server {
    public static void main(String[] args) {
        final int PORT = 5000;
        final int N = 3;
        final int T = 2;
        final byte GARBAGE = '#';

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is connected at port no: " + PORT);
            System.out.println("Server is connecting");
            System.out.println("Waiting for the client");

            Socket socket = serverSocket.accept();
            System.out.println("Client request is accepted at port no: " + socket.getPort());
            System.out.println("Server’s Communication Port: " + socket.getLocalPort());

            DataInputStream dis = new DataInputStream(socket.getInputStream());

            FileOutputStream[] outputs = new FileOutputStream[N];
            for (int i = 0; i < N; i++) {
                outputs[i] = new FileOutputStream("output" + (i + 1) + ".txt");
            }
            byte[] packet = new byte[N * T];
            int bytesRead;
            int round = 1;
            while ((bytesRead = dis.read(packet))  != -1) {
                boolean isAllGarbage = true;
                for (int i = 0; i < bytesRead; i++) {
                    if (packet[i] != GARBAGE) {
                        isAllGarbage = false;
                        break;
                    }
                }
                if (isAllGarbage) break;

                System.out.print("Round " + round + ": ");
                for (int i = 0; i < N; i++) {
                    for (int j = 0; j < T; j++) {
                        int index = i * T + j;
                        if (index < bytesRead) {
                            System.out.print((char) packet[index]);
                        } else {
                            System.out.print(GARBAGE);
                        }
                    }
                    System.out.print(" ");
                }
                System.out.println();

                for (int i = 0; i < N; i++) {
                    for (int j = 0; j < T; j++) {
                        int index = i * T + j;
                        if (index < bytesRead && packet[index] != GARBAGE) {
                            outputs[i].write(packet[index]);
                        }
                    }
                }
                round++;
            }

            for (FileOutputStream fos : outputs) fos.close();
            dis.close();
            socket.close();

            for (int i = 0; i < N; i++) {
                String outputFile = "output" + (i + 1) + ".txt";
                System.out.print(outputFile + ": ");
                try (BufferedReader br = new BufferedReader(new FileReader(outputFile))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        System.out.print(line);
                    }
                }
                System.out.println();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
