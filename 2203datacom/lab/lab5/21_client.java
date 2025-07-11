import java.io.*;
import java.net.*;
import java.util.*;

public class client {
    public static String toBinary(String text) {
        StringBuilder binary = new StringBuilder();
        for (char c : text.toCharArray()) {
            binary.append(String.format("%8s", Integer.toBinaryString(c)).replace(' ', '0'));
        }
        return binary.toString();
    }

    private static String xor(String a, String b) {
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

        if (dividend.charAt(0) == '1')   dividend = xor(generator, dividend);
        else     dividend = xor("0".repeat(k), dividend);

        return dividend;
    }

    public static String introduceBitError(String codeword) {
        Random random = new Random();
        int randomBitPosition = random.nextInt(codeword.length());
        char flippedBit = codeword.charAt(randomBitPosition) == '0' ? '1' : '0';
        return codeword.substring(0, randomBitPosition) + flippedBit + codeword.substring(randomBitPosition + 1);
    }

    public static String singleBitErrorCorrection(String codeword, String generator) {
        for (int i = 0; i < codeword.length(); i++) {
            String modifiedCodeword = codeword.substring(0, i) + (codeword.charAt(i) == '0' ? '1' : '0') + codeword.substring(i + 1);

            String recalculatedCRC = calculateCRC(modifiedCodeword, generator);
            String receivedCRC = modifiedCodeword.substring(modifiedCodeword.length() - (generator.length() - 1));

            if (recalculatedCRC.equals(receivedCRC)) {
                return modifiedCodeword;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        String serverIP = "127.0.0.1";
        int serverPort = 5000;

        try {
            // Read the message from the file
            BufferedReader fileReader = new BufferedReader(new FileReader("./input.txt"));
            String message = fileReader.readLine().trim();
            fileReader.close();
            System.out.println("File Content: " + message);

            String binaryData = toBinary(message);
            System.out.println("Converted Binary Data: " + binaryData);

            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter Generator Polynomial. CRC-8=100000111, CRC-10=11100000011 \n" +
                    "CRC-16=11000000000000101, CRC-32=100000100110000010001110110110111: ");
            String generator = scanner.nextLine().trim();
            int k = generator.length();

            String appendedData = binaryData + "0".repeat(k - 1);
            System.out.println("After Appending zeros Data to Divide: " + appendedData);

            String crc = calculateCRC(appendedData, generator);
            System.out.println("CRC Remainder: " + crc);

            String codeword = binaryData + crc;
            System.out.println("Transmitted Codeword to Server: " + codeword);

            Random random = new Random();
            boolean introduceError = random.nextBoolean();
            if (introduceError) {
                codeword = introduceBitError(codeword);
                System.out.println("Codeword with Random Bit Error: " + codeword);
            } else {
                System.out.println("Sending correct codeword: " + codeword);
            }

            String correctedCodeword = singleBitErrorCorrection(codeword, generator);
            if (correctedCodeword != null) {
                System.out.println("Corrected Codeword: " + correctedCodeword);
                codeword = correctedCodeword;
            } else {
                System.out.println("Uncorrectable Error Detected");
            }

            Socket socket = new Socket(serverIP, serverPort);
            System.out.println("Client connected to the server on Handshaking port " + serverPort);
            System.out.println("Client’s Communication Port: " + socket.getLocalPort());
            System.out.println("Client is Connected");

            DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
            dout.writeUTF(generator);
            dout.writeUTF(codeword);
            dout.flush();

            dout.close();
            socket.close();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
