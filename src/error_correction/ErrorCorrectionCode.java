package error_correction;

import IAT_channel.CANAuthMessage;
import de.fischl.usbtin.CANMessage;

import java.util.List;

public interface ErrorCorrectionCode {

    int getNrCorrectingBits();

    List<Byte> getCodeForAuthMessage(CANAuthMessage message);

    boolean checkCodeForAuthMessage(List<Byte> message);
}
