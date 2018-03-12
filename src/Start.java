import com.jcraft.jsch.*;

import java.io.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Start {

    private static final String SERVER = "RW.server";
    private static final String SERVER_PORT = "RW.server.port";
    private static final String READERS_NUMBER = "RW.numberOfReaders";
    private static final String READER = "RW.reader";
    private static final String WRITERS_NUMBER = "RW.numberOfWriters";
    private static final String WRITER = "RW.writer";
    private static final String ACCESS_NUMBER = "RW.numberOfAccesses";
    private static final String READER_PASSWORD = "PW.reader";
    private static final String WRITER_PASSWORD = "PW.writer";
    private static final String RMI_REGISTRY = "RW.rmiregistry.port";

    public static void main(String args[]) throws IOException, JSchException, SftpException {
        Start start = new Start();
        Properties systemProperties = new Properties();
        InputStream input = null;
        String serverIP = null;
        String serverPort = null;
        String RMIRegisteryPort = null;
        int readersNum = 0;
        List<String> readers = new ArrayList<>();
        List<String> readersPasswords = new ArrayList<>();
        int writersNum = 0;
        List<String> writers = new ArrayList<>();
        List<String> writersPasswords = new ArrayList<>();
        String accessNum = null;

        try {
            input = Start.class.getClassLoader().getResourceAsStream("system.properties");
            systemProperties.load(input);

            serverIP = systemProperties.getProperty(SERVER);
            serverPort = systemProperties.getProperty(SERVER_PORT);
            RMIRegisteryPort = systemProperties.getProperty(RMI_REGISTRY);
            readersNum = Integer.parseInt(systemProperties.getProperty(READERS_NUMBER));
            writersNum = Integer.parseInt(systemProperties.getProperty(WRITERS_NUMBER));
            accessNum = systemProperties.getProperty(ACCESS_NUMBER);
            for (int i = 0; i < readersNum; i++) {
                readers.add(systemProperties.getProperty(READER + i));
                readersPasswords.add(systemProperties.getProperty(READER_PASSWORD + i));
            }
            for (int i = 0; i < writersNum; i++) {
                writers.add(systemProperties.getProperty(WRITER + i));
                writersPasswords.add(systemProperties.getProperty(WRITER_PASSWORD + i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("serverLogR"));
        bufferedWriter.write("Readers:\n");
        bufferedWriter.write("sSeq  oVal  rID  rNum\n");
        bufferedWriter.flush();
        bufferedWriter.close();

        bufferedWriter = new BufferedWriter(new FileWriter("serverLogW"));
        bufferedWriter.write("Writers:\n");
        bufferedWriter.write("sSeq  oVal  wID\n");
        bufferedWriter.flush();
        bufferedWriter.close();

        System.out.println(serverPort);

        try {
            System.setProperty("java.rmi.server.hostname", serverIP);
            String name = "RemoteReaderWriter";
            RemoteReaderWriter remoteEngine = new RemoteServer();
            RemoteReaderWriter stub =
                    (RemoteReaderWriter) UnicastRemoteObject.exportObject(remoteEngine, Integer.parseInt(serverPort));
            Registry registry = LocateRegistry.createRegistry(Integer.parseInt(RMIRegisteryPort));
            registry.rebind(name, stub);
            System.out.println("RemoteReaderWriter bound");
        } catch (Exception e) {
            System.err.println("RemoteReaderWriter exception:");
            e.printStackTrace();
        }

        start.startThreads(readers, readersPasswords, writers, writersPasswords, serverIP, Integer.parseInt(RMIRegisteryPort), Integer.parseInt(accessNum));
    }

    public void startThreads(List<String> readers, List<String> readersPasswords, List<String> writers, List<String> writersPasswords, String serverIP, int RmiRegisteryPort, int numberOfAccesses) throws JSchException, SftpException, IOException {

        for (int i = 0; i < readers.size(); i++) {
            String user = readers.get(i);
            String[] userCred = user.split("@");
            String userName = userCred[0];
            String userIP = userCred[1];
            String password = readersPasswords.get(i);
            Executor e = new Executor("ReaderClient", userName, userIP, password, serverIP, RmiRegisteryPort, numberOfAccesses, i);
            e.start();
        }

        for (int i = 0; i < writers.size(); i++) {
            String user = writers.get(i);
            String[] userCred = user.split("@");
            String userName = userCred[0];
            String userIP = userCred[1];
            String password = writersPasswords.get(i);
            Executor e = new Executor("WriterClient", userName, userIP, password, serverIP, RmiRegisteryPort, numberOfAccesses, i);
            e.start();
        }
    }
}
