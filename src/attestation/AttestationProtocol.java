package attestation;

import util.CANAuthMessage;
import USBtin.USBtin;

public interface AttestationProtocol {
    CANAuthMessage getAttestationMessage();

    boolean checkAttestationMessage(CANAuthMessage message);
}
