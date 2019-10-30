package transmission_channel.IAT_channel;

import attestation.AttestationProtocol;
import USBtin.CANMessage;
import USBtin.USBtin;
import USBtin.USBtinException;
import error_detection.ErrorCorrectionCode;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortTimeoutException;
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
    private int silence_start;
    private int silence_end;

    // for silence start/end
    private int silence_counter = 0;
    private boolean starting = false;
    private boolean stopping = false;

    public IAT_Node(long period, long delta, int windowLength, int silence_start, int silence_end) {
        PERIOD = period;
        DELTA = delta;
        WINDOW_LENGTH = windowLength;

        this.silence_start = silence_start * WINDOW_LENGTH;
        this.silence_end = silence_end * WINDOW_LENGTH;
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
                long timeToSleep = this.getTimeToSleep();
                Thread.sleep(timeToSleep);
                this.send(message);
            }
            catch (InterruptedException | USBtinException ex) {
                System.err.println(ex);
            }
        }
    }

    public long getTimeToSleep() {
        List<Byte> auth_bytes = this.AUTH_MESSAGE.toByteArray();

        // start silence bits
        if (indexInAuthMessage == 0) {
            if (!starting) {
                silence_counter++;
                starting = true;
                placeInWindow = WINDOW_LENGTH;
                return PERIOD;
            }
            if (silence_counter < silence_start) {
                starting = true;
                silence_counter++;
                return PERIOD;
            }
        }
        starting = false;

        // stop silence bits
        if (indexInAuthMessage == auth_bytes.size()) {
            if (stopping) {
                if (silence_counter < silence_end) {
                    silence_counter++;
                    return PERIOD;
                }
                else {
                    placeInWindow = WINDOW_LENGTH;
                    silence_counter = 0;
                }
            }
        }

        // wrap-arounds
        if (placeInWindow >= WINDOW_LENGTH) {
            if (indexInAuthMessage == auth_bytes.size()) {
                if (!stopping) {
                    stopping = true;
                    silence_counter = 1;
                    return PERIOD;
                }
                else {
                    stopping = false;
                    silence_counter = 0;
                }
            }
            if (!starting) { indexInAuthMessage += 1; }
            placeInWindow = 0;
        }

        if (indexInAuthMessage > auth_bytes.size()) {
            indexInAuthMessage = 0;
            silence_counter = 1;
            return PERIOD;
        }

        placeInWindow += 1;

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
