
public class Serializer {

   public ReaderEntry SerializeReaderEntry(String response, int seqNumber, int rNum){
       String [] parameters = response.split(",");
       int boardValue = Integer.parseInt(parameters[0]);
       int id = Integer.parseInt(parameters[1]);
       return (new ReaderEntry(seqNumber, boardValue, id, rNum));
   }
    public String deSerializeReaderEntry(ReaderEntry readerEntry){
        StringBuilder stringBuilder = new StringBuilder();
        int seqNumberLength = (readerEntry.getSeqNumber() + "").length();
        int boardValLength = (readerEntry.getBoardVal() + "").length();
        if(seqNumberLength == 1) {
            stringBuilder.append(readerEntry.getSeqNumber() + "      ");
        } else {
            stringBuilder.append(readerEntry.getSeqNumber() + "     ");
        }
        if(boardValLength == 1) {
            stringBuilder.append(readerEntry.getBoardVal() + "      ");
        } else {
            stringBuilder.append(readerEntry.getBoardVal() + "     ");
        }
        stringBuilder.append(readerEntry.getId() + "    ");
        stringBuilder.append(readerEntry.getrNum());
        stringBuilder.append("\n");
        return stringBuilder.toString();
    }

    //TODO:
    public ReaderEntry SerializeWriterEntry(){
        return null;
    }
    public ReaderEntry deSerializeWriterEntry(){
        return null;
    }
}
