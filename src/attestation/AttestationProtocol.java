package attestation;

import util.CANAuthMessage;
import USBtin.USBtin;

public interface AttestationProtocol {
    CANAuthMessage getAttestationMessage(USBtin node);

    boolean checkAttestationMessage(CANAuthMessage message);
}
