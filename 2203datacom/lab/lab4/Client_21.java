import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) {
        final String serverIP = "10.33.2.81";
        final int port= 5000;
        final int T=2;  
        final byte garbage='#';

        String[] fileNames = {"input1.txt","input2.txt","input3.txt"};
        int N=fileNames.length;

        try (Socket socket = new Socket(serverIP,port)) {
            System.out.println("Client connected to the server on Handshaking port "+port);
            System.out.println("Client’s Communication Port: " + socket.getLocalPort());
            System.out.println("Client is Connected");

            DataOutputStream output=new DataOutputStream(socket.getOutputStream());

            FileInputStream[] inputs=new FileInputStream[N];
            boolean[] finished=new boolean[N];
            for (int i=0;i<N;i++) {
                inputs[i]=new FileInputStream(fileNames[i]);
                finished[i]=false;
            }

           
            boolean allFinished = false;
            int round = 1;
            
            while (!allFinished) {
                allFinished=true;
                byte[] packet=new byte[N*T];
                StringBuilder roundOutput = new StringBuilder("Round " + round++ + ": ");
            
                for (int i=0;i<N;i++) {
                    for (int j=0;j<T;j++) {
                        int index=i*T+j;
                        if (!finished[i]) {
                            int data=inputs[i].read();
                            if (data==-1) {
                                packet[index]=garbage;
                                finished[i]=true;
                            } else {
                                packet[index]=(byte) data;
                            }
                        } else {
                            packet[index]=garbage;
                        }
                        roundOutput.append((char) packet[index]);
                    }
                    roundOutput.append(" ");
                    if (!finished[i]) allFinished = false;
                }
            
                
                boolean sendPacket =false;
                for (byte b : packet) {
                    if (b != garbage) {
                        sendPacket = true;
                        break;
                    }
                }
            
                if (sendPacket) {
                    System.out.println(roundOutput.toString().trim());
                    output.write(packet);
                    output.flush();
                }
            }
            
            for (FileInputStream fis : inputs) fis.close();
            output.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
