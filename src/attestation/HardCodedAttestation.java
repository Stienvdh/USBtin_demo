package attestation;

import IAT_channel.CANAuthMessage;
import de.fischl.usbtin.USBtin;

public class HardCodedAttestation implements AttestationProtocol {

    private byte[] message;

    public HardCodedAttestation(byte[] mess) {
        this.message = mess;
    }

    @Override
    public CANAuthMessage getAttestationMessage(USBtin node) {
        return new CANAuthMessage(this.message);
    }

    @Override
    public void checkAttestationMessage(CANAuthMessage message) {
    }
}
