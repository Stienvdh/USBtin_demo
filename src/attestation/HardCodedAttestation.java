package attestation;

import util.CANAuthMessage;
import USBtin.USBtin;

import java.util.LinkedList;
import java.util.List;

public class HardCodedAttestation implements AttestationProtocol {

    private List<Byte> message;

    public HardCodedAttestation(byte[] mess) {
        this.message = new LinkedList<>();
        for (int i=0; i<mess.length; i++) {
            this.message.add(mess[i]);
        }
    }

    @Override
    public CANAuthMessage getAttestationMessage() {
        return new CANAuthMessage(this.message);
    }

    @Override
    public boolean checkAttestationMessage(CANAuthMessage message) {
        return this.message.equals(message.getMessage());
    }
}
