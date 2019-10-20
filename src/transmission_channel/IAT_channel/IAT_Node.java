package transmission_channel.IAT_channel;

import attestation.AttestationProtocol;
import USBtin.CANMessage;
import USBtin.USBtin;
import USBtin.USBtinException;
import error_detection.ErrorCorrectionCode;
import util.CANAuthMessage;

import java.util.List;

public class IAT_Node extends USBtin {

    private long PERIOD;
    private long DELTA;
    private int WINDOW_LENGTH;
    private CANAuthMessage AUTH_MESSAGE;

    private int placeInWindow = 0;
    private int indexInAuthMessage = 0;
    private boolean running=true;
    private ErrorCorrectionCode corrector;
    private AttestationProtocol protocol;

    public IAT_Node(long period, long delta, int windowLength) {
        PERIOD = period;
        DELTA = delta;
        WINDOW_LENGTH = windowLength;
    }

    public void start(CANMessage message) {
        if (this.protocol != null) {
            this.AUTH_MESSAGE = this.protocol.getAttestationMessage(this);
        }
        else { return; }

        // error correction
        if (this.corrector != null) {
            this.AUTH_MESSAGE.setCorrectionCode(this.corrector);
        }

        while (running) {
            try {
                Thread.sleep(this.getTimeToSleep());
                this.send(message);
            }
            catch (InterruptedException | USBtinException ex) {
                System.err.println(ex);
            }
        }
    }

    public long getTimeToSleep() {
        List<Byte> auth_bytes = this.AUTH_MESSAGE.toByteArray();

        // wrap-arounds
        if (placeInWindow >= WINDOW_LENGTH) {
            indexInAuthMessage += 1;
            placeInWindow = 0;
        }

        if (indexInAuthMessage > auth_bytes.size()+1) {
            indexInAuthMessage = 0;
        }

        placeInWindow += 1;

        // silence bits
        if (indexInAuthMessage == 0 || indexInAuthMessage == auth_bytes.size()+1) {
            return PERIOD;
        }

        if (auth_bytes.get(indexInAuthMessage-1).equals( (byte) 0)) {
            return PERIOD + DELTA;
        }

        return PERIOD - DELTA;
    }

    public void leave() {
        running = false;
        try {
            this.closeCANChannel();
            this.disconnect();
        }
        catch (USBtinException ex) {
            ex.printStackTrace();
        }
    }

    public void setCorrector(ErrorCorrectionCode corrector) {
        this.corrector = corrector;
    }

    public void setProtocol(AttestationProtocol prot) {
        this.protocol = prot;
    }
}