package error_detection;

import util.CANAuthMessage;

import java.util.LinkedList;
import java.util.List;

public class SimpleParity implements ErrorCorrectionCode {

    @Override
    public int getNrCorrectingBits() {
        return 1;
    }

    @Override
    public List<Byte> getCodeForAuthMessage(CANAuthMessage message) {
        int paritycounter = 0;
        for (int i=0 ; i<message.getMessage().size() ; i++) {
            if (message.getMessage().get(i) == ((byte) 1)) {
                paritycounter += 1;
            }
        }
        List<Byte> result = new LinkedList<Byte>();
        if (paritycounter%2 == 0) { result.add( (byte) 0 ); }
        else { result.add( (byte) 1 ); }
        return result;
    }

    @Override
    public boolean checkCodeForAuthMessage(List<Byte> message) {
        int paritycounter = 0;
        for (int i=0 ; i<message.size() ; i++) {
            if (message.get(i).equals( (byte) 1 )) {
                paritycounter += 1;
            }
        }
        return (paritycounter%2 == 0);
    }
}
