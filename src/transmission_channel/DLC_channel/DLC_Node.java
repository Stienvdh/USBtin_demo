package transmission_channel.DLC_channel;

import USBtin.CANMessage;
import USBtin.USBtin;
import USBtin.USBtinException;
import attestation.AttestationProtocol;
import error_detection.ErrorCorrectionCode;
import host_communication.CANSender;
import util.CANAuthMessage;

import java.util.List;

import static java.lang.System.err;

public class DLC_Node {

    private long PERIOD;
    static final public int SILENCE_BIT_DLC=8;
    static final public int DLC_0 = 9;
    static final public int DLC_1 = 10;
    static final public int DLC_00 = 11;
    static final public int DLC_01 = 12;
    static final public int DLC_10 = 13;
    static final public int DLC_11 = 14;
    private CANAuthMessage AUTH_MESSAGE;
    private ErrorCorrectionCode corrector;
    private AttestationProtocol protocol;
    private boolean running = true;
    private CANSender sender;

    private int indexInAuthMessage = 0;

    public DLC_Node(long period, CANSender sender) {
        PERIOD = period;
        this.sender = sender;
    }

    public void start() {
        if (this.protocol != null) {
            this.AUTH_MESSAGE = this.protocol.getAttestationMessage();
        }
        else { return; }

        // error correction
        if (this.corrector != null) {
            this.AUTH_MESSAGE.setCorrectionCode(this.corrector);
        }

        while (running) {
            try {
                Thread.sleep(PERIOD);
                CANMessage message = this.sender.getMessageToSend();
                message.setDLC(getDLCToUse(message));
                this.sender.send(message);
            }
            catch (InterruptedException | USBtinException ex) {
                err.println(ex);
            }
        }
    }

    private int getDLCToUse(CANMessage message) {
        if (message.getData().length < 8) {
            return message.getData().length;
        }

        List<Byte> auth_bytes = this.AUTH_MESSAGE.toByteArray();

        // wrap-arounds
        if (indexInAuthMessage > auth_bytes.size()+1) {
            indexInAuthMessage = 0;
        }

        // silence bits
        if (indexInAuthMessage == 0 || indexInAuthMessage == auth_bytes.size()+1) {
            indexInAuthMessage += 1;
            return SILENCE_BIT_DLC;
        }

        // authentication bits
        if (indexInAuthMessage == auth_bytes.size()) {
            if (auth_bytes.get(indexInAuthMessage-1) == 0) {
                indexInAuthMessage += 1;
                return DLC_0;
            }
            indexInAuthMessage += 1;
            return DLC_1;
        }

        if (auth_bytes.get(indexInAuthMessage-1) == 0) {
            if (auth_bytes.get(indexInAuthMessage) == 0) {
                indexInAuthMessage += 2;
                return DLC_00;
            }
            indexInAuthMessage += 2;
            return DLC_01;
        }

        if (auth_bytes.get(indexInAuthMessage) == 0) {
            indexInAuthMessage += 2;
            return DLC_10;
        }
        indexInAuthMessage += 2;
        return DLC_11;
    }

    public void setCorrector(ErrorCorrectionCode corrector) {
        this.corrector = corrector;
    }

    public void setAttestation(AttestationProtocol protocol) {
        this.protocol = protocol;
    }

    public void leave() {
        this.running = false;
    }
}
