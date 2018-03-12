import java.io.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Random;

import static java.lang.Thread.sleep;

public class WriterClient{


    private static final String NAME = "RemoteReaderWriter";
    public static void main(String args[]) throws IOException, InterruptedException {

        String  hostName = args[0];
        int  registryPortNumber = Integer.parseInt(args[1]);
        int numberOfAccesses = Integer.parseInt(args[2]);
        int id = Integer.parseInt(args[3]);



        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("./Writer" + id));
        bufferedWriter.write("Client type: Writer\n");
        bufferedWriter.write("Client name: " +  id + "\n");
        bufferedWriter.write("rSeq   sSeq\n");


        try {
            Registry registry = LocateRegistry.getRegistry(hostName , registryPortNumber);
            RemoteReaderWriter server = (RemoteReaderWriter) registry.lookup(NAME);
            String fromServer;
            int currentAccess = 0;
            Random random = new Random();
            while(currentAccess < numberOfAccesses) {
                int wait = random.nextInt(10000);
                if(currentAccess < numberOfAccesses) {
                    sleep(wait);
                }
                fromServer = server.write(id + "");
                String []output = fromServer.split(",");
                System.out.println("from Server: " + fromServer);
                bufferedWriter.append(output[0] + "      " + output[1] + "\n");
                currentAccess++;
            }
        } catch (Exception e) {
            System.out.println(hostName + " " + registryPortNumber + " " + NAME);
            e.printStackTrace();
        }



        bufferedWriter.flush();
        bufferedWriter.close();


    }

}
