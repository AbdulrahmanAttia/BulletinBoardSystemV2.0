
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class BulletinBoardServerThread extends Thread{

    private Socket socket;
    private static AtomicInteger seqNumber = new AtomicInteger(1);
    private static AtomicInteger rSeq = new AtomicInteger(1);
    private Serializer serializer = new Serializer();
    private static Integer board = -1;
    private static Semaphore readLock = new Semaphore(1);
    private static Semaphore writeLock = new Semaphore(1);
    private static AtomicInteger readCount = new AtomicInteger(0);
    private static final String DELIMITER = ",";
    public BulletinBoardServerThread(Socket serverSocket) {
        this.socket = serverSocket;
    }

    @Override
    public void run() {
        try (
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(
                                    socket.getInputStream()));
            ) {
                String inputLine, outputLine;

                int seq;
                while ((inputLine = in.readLine()) != null) {
                    if (inputLine.split(",")[0].equals("READ")) {
                        read(inputLine, out);
                    }else{
                        write(inputLine, out);
                    }
                }
                // writeLog();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Thread.currentThread().stop();
        System.out.println(Thread.activeCount());
    }

    private  synchronized int getSSeqR(BufferedWriter bufferedWriter, String ret, int rNum) throws IOException {
        ReaderEntry readerEntry = serializer.SerializeReaderEntry(ret, seqNumber.get(), rNum);
        String logLine = serializer.deSerializeReaderEntry(readerEntry);
        bufferedWriter.append(logLine);
        bufferedWriter.flush();
        bufferedWriter.close();
        return  seqNumber.getAndIncrement();
    }

    private  synchronized int  getSSeqW(BufferedWriter bufferedWriter, String ret) throws IOException {
        String logLine = seqNumber.get() + "      " + ret + "    " + ret + "\n";
        bufferedWriter.append(logLine);
        bufferedWriter.flush();
        bufferedWriter.close();
        return  seqNumber.getAndIncrement();
    }
    private void read(String request, PrintWriter out){
        String []tokens = request.split(DELIMITER);
        String type = tokens[0];
        String id = tokens[1];
        int currentRSeq = rSeq.getAndIncrement();
        try {
            readLock.acquire();
            readCount.getAndIncrement();
            if(readCount.intValue() == 1){
                writeLock.acquire();
            }
            readLock.release();

            int currentReaders = readCount.get();
            String ret = board + "," + id ;
            Random random = new Random();
            int wait = random.nextInt(10001);
            Thread.sleep(wait);
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("serverLogR", true));
            int currentSSeq = getSSeqR(bufferedWriter, ret, currentReaders);
            ret = currentRSeq + "," + currentSSeq + "," + board;
            out.println(ret);
            readLock.acquire();
            readCount.getAndDecrement();
            if(readCount.get() == 0){
                writeLock.release();
            }
            readLock.release();
        } catch(InterruptedException ie) {
            ie.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void write(String request, PrintWriter out) throws InterruptedException, IOException {
        String []tokens = request.split(DELIMITER);
        String type = tokens[0];
        String id = tokens[1];
        int currentRSeq = rSeq.getAndIncrement();
        writeLock.acquire();
        board = Integer.parseInt(id);
        String ret = id ;
        Random random = new Random();
        int wait = random.nextInt(10001);
        Thread.sleep(wait);
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("serverLogW", true));
        int currentSSeq = getSSeqW(bufferedWriter, ret);
         ret = currentRSeq + "," + currentSSeq;
        out.println(ret);
        writeLock.release();
    }
}
