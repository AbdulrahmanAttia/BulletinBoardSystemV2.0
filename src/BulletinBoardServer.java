import com.jcraft.jsch.JSch;

import java.io.IOException;
import java.net.ServerSocket;


public class BulletinBoardServer implements Runnable {

    private static String portNumber;
    private int numberOfClients;
    public  BulletinBoardServer(String portNumber, int numberOfClients){
        this.portNumber = portNumber;
        this.numberOfClients = numberOfClients;

    }
    @Override
    public void run(){
        try (ServerSocket serverSocket = new ServerSocket(Integer.parseInt(portNumber))) {

            for(int i = 0; i < numberOfClients; i++){
                new BulletinBoardServerThread(serverSocket.accept()).start();
           }
        } catch (IOException e) {
            System.err.println("Could not listen on port " + portNumber);
            System.exit(-1);
        }
    }

}
