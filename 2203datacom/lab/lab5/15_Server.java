import java.io.*;
import java.net.*;
public class Server {
    public static String xor(String a, String b) {
        StringBuilder result = new StringBuilder();
        for (int i = 1; i < b.length(); i++) {
            result.append(a.charAt(i) == b.charAt(i) ? '0' : '1');
        }
        return result.toString();
    }
    public static String calculateCRC(String data, String generator) {
        int k = generator.length();
        String dividend = data.substring(0, k);
        int curr = k;
        while (curr < data.length()) {
            if (dividend.charAt(0) == '1') {
                dividend = xor(generator, dividend) + data.charAt(curr);
            } else {
                dividend = xor("0".repeat(k), dividend) + data.charAt(curr);
            }
            curr++;
        }
        if (dividend.charAt(0) == '1') dividend = xor(generator, dividend);
        else dividend = xor("0".repeat(k), dividend);
        return dividend;
    }
    public static void main(String[] args) {
        int port = 5000;
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server started. Listening on port " + port + "...");
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected: " + clientSocket.getInetAddress());
            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
            String generator = in.readUTF();
            String codeword = in.readUTF();
            System.out.println("Received Generator Polynomial: " + generator);
            System.out.println("Received Codeword: " + codeword);
            int k = generator.length();
            int crcBits = k - 1;
            int dataLength = codeword.length() - crcBits;
            String receivedData = codeword.substring(0, dataLength);
            String receivedCRC = codeword.substring(dataLength);
            System.out.println("Extracted Data: " + receivedData);
            System.out.println("Received CRC: " + receivedCRC);
            String appendedData = receivedData + "0".repeat(crcBits);
            String recalculatedCRC = calculateCRC(appendedData, generator);
            System.out.println("Recalculated CRC: " + recalculatedCRC);
            if (recalculatedCRC.equals(receivedCRC)) System.out.println("No Error");
            else System.out.println("Error detected");
            in.close();
            clientSocket.close();
            serverSocket.close();
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }
}
