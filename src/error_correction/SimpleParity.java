package error_correction;

import IAT_channel.CANAuthMessage;

import java.util.List;

public class SimpleParity implements ErrorCorrectionCode {

    @Override
    public int getNrCorrectingBits() {
        return 1;
    }

    @Override
    public byte[] getCodeForAuthMessage(CANAuthMessage message) {
        int paritycounter = 0;
        for (int i=0 ; i<message.getMessage().length ; i++) {
            if (message.getMessage()[i] == 1) {
                paritycounter += 1;
            }
        }
        if (paritycounter%2 == 0) { return new byte[]{0}; }
        return new byte[]{1};
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
