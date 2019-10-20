package error_detection;

import util.CANAuthMessage;

import java.util.List;

public interface ErrorCorrectionCode {

    int getNrCorrectingBits();

    List<Byte> getCodeForAuthMessage(CANAuthMessage message);

    boolean checkCodeForAuthMessage(List<Byte> message);
}
