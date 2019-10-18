package attestation;

import IAT_channel.CANAuthMessage;
import de.fischl.usbtin.USBtin;

public interface AttestationProtocol {
    CANAuthMessage getAttestationMessage(USBtin node);

    void checkAttestationMessage(CANAuthMessage message);
}
