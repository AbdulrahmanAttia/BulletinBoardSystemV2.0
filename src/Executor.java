import com.jcraft.jsch.*;

import java.io.*;

public class Executor extends Thread{


    String fileName;
    String userName;
    String userIP;
    String password;
    String serverIP;
    int serverPort;
    int numberOfAccesses;
    int id;
    private static final String ENGINE = "RemoteReaderWriter";
    private  JSch jSch = new JSch();
    public Executor(String fileName, String userName, String userIP,String password, String serverIP, int serverPort, int numberOfAccesses, int id){
        this.fileName = fileName;
        this.userName = userName;
        this.userIP = userIP;
        this.password = password;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.numberOfAccesses = numberOfAccesses;
        this.id = id;
        JSch.setConfig("StrictHostKeyChecking", "no");
    }

    private void transferFile(String fileName, ChannelSftp sftpChannel){
        File initialFile = new File(fileName + ".java");
        InputStream targetStream = null;
        try {
            targetStream = new FileInputStream(initialFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            sftpChannel.put(targetStream, "./" + fileName + ".java");
        } catch (SftpException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        Session session = null;
        try {
            session = jSch.getSession(userName, userIP, 22);
        } catch (JSchException e) {
            e.printStackTrace();
        }
        session.setPassword(password);
        try {
            session.connect();
        } catch (JSchException e) {
            e.printStackTrace();
        }
        ChannelSftp sftpChannel = null;
        try {
            sftpChannel = (ChannelSftp) session.openChannel("sftp");
        } catch (JSchException e) {
            e.printStackTrace();
        }
        try {
            sftpChannel.connect();
        } catch (JSchException e) {
            e.printStackTrace();
        }

        transferFile(fileName, sftpChannel);
        transferFile(ENGINE, sftpChannel);


        String compile = String.format("javac %s", fileName + ".java");
        System.out.println("Execuotr " + serverIP + " " + serverPort);
        String run = String.format("java %s %s %d %d %d", fileName, serverIP, serverPort, numberOfAccesses, id);


        String command = compile + " && " + run;
        Channel channel = null;
        try {
            channel = session.openChannel("exec");
        } catch (JSchException e) {
            e.printStackTrace();
        }
        ((ChannelExec) channel).setCommand(command);

        channel.setInputStream(null);
        ((ChannelExec) channel).setErrStream(System.err);
        InputStream in = null;
        try {
            in = channel.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            channel.connect();
        } catch (JSchException e) {
            e.printStackTrace();
        }

        byte[] tmp = new byte[1024];
        while (true) {
            assert in != null;
            try {
                while (in.available() > 0) {
                    int j = 0;
                    try {
                        j = in.read(tmp, 0, 1024);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (j < 0)
                        break;
                    System.out.print(new String(tmp, 0, j));
                }
            }catch (IOException ee){

            }
            if (channel.isClosed()) {
                try {
                    if (in.available() > 0)
                        continue;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("exit-status: " + channel.getExitStatus());
                break;
            }
            try{
                Thread.sleep(1000);
            } catch (Exception ee) { }
        }
    }

}
