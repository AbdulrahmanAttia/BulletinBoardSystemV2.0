public class ReaderEntry {
    private int seqNumber;
    private int boardVal;
    private int id;
    private int rNum;

    public ReaderEntry(int seqNumber, int boardVal, int id, int rNum){
        this.seqNumber = seqNumber;
        this.boardVal = boardVal;
        this.id = id;
        this.rNum = rNum;
    }

    public int getBoardVal() {
        return boardVal;
    }

    public int getSeqNumber() {

        return seqNumber;
    }

    public int getId() {
        return id;
    }

    public int getrNum() {
        return rNum;
    }
}
