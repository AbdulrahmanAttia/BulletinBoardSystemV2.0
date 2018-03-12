import java.io.*;
import java.rmi.RemoteException;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class RemoteServer implements RemoteReaderWriter {


    private static AtomicInteger seqNumber = new AtomicInteger(1);
    private static AtomicInteger rSeq = new AtomicInteger(1);
    private Serializer serializer = new Serializer();
    private static Integer board = -1;
    private static Semaphore readLock = new Semaphore(1);
    private static Semaphore writeLock = new Semaphore(1);
    private static AtomicInteger readCount = new AtomicInteger(0);

    private  synchronized int getSSeqR(BufferedWriter bufferedWriter, String ret, int rNum) throws IOException {
        ReaderEntry readerEntry = serializer.SerializeReaderEntry(ret, seqNumber.get(), rNum);
        String logLine = serializer.deSerializeReaderEntry(readerEntry);
        bufferedWriter.append(logLine);
        bufferedWriter.flush();
        bufferedWriter.close();
        return  seqNumber.getAndIncrement();
    }

    private synchronized int  getSSeqW(BufferedWriter bufferedWriter, String ret) throws IOException {
        String logLine = seqNumber.get() + "      " + ret + "    " + ret + "\n";
        bufferedWriter.append(logLine);
        bufferedWriter.flush();
        bufferedWriter.close();
        return  seqNumber.getAndIncrement();
    }
    @Override
    public String read(String id) throws RemoteException{
        String ret = "";
        int currentRSeq = rSeq.getAndIncrement();
        try {
            readLock.acquire();
            readCount.getAndIncrement();
            if(readCount.intValue() == 1){
                writeLock.acquire();
            }
            readLock.release();

            int currentReaders = readCount.get();
            ret = board + "," + id ;
            Random random = new Random();
            int wait = random.nextInt(10001);
            Thread.sleep(wait);
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("serverLogR", true));
            int currentSSeq = getSSeqR(bufferedWriter, ret, currentReaders);
            ret = currentRSeq + "," + currentSSeq + "," + board;
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

        return ret;
    }

    @Override
    public String write(String id) throws RemoteException {
        int currentRSeq = rSeq.getAndIncrement();
        String ret = "";
        try {
            writeLock.acquire();
            board = Integer.parseInt(id);
            ret = id ;
            Random random = new Random();
            int wait = random.nextInt(10001);
            Thread.sleep(wait);
            BufferedWriter bufferedWriter = null;
            bufferedWriter = new BufferedWriter(new FileWriter("serverLogW", true));
            int currentSSeq = 0;
            currentSSeq = getSSeqW(bufferedWriter, ret);
            ret = currentRSeq + "," + currentSSeq;
            writeLock.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }
}