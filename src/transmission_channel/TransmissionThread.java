package transmission_channel;

import attestation.AttestationProtocol;
import error_detection.ErrorCorrectionCode;
import host_communication.CANReceiver;
import host_communication.CANSender;

public abstract class TransmissionThread extends Thread {

    private ErrorCorrectionCode corrector;
    private AttestationProtocol attestationProtocol;

    public void addAuthCorrectionCode(ErrorCorrectionCode corrector) {
        this.corrector = corrector;
    }

    public void addAttestationProtocol(AttestationProtocol prot) { this.attestationProtocol = prot; }

    public abstract void run();

    public abstract void end();

    public ErrorCorrectionCode getCorrector() { return this.corrector; }

    public AttestationProtocol getAttestationProtocol() { return this.attestationProtocol; }

}
