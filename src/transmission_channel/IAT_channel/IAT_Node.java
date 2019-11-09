package transmission_channel.IAT_channel;

import attestation.AttestationProtocol;
import USBtin.CANMessage;
import USBtin.USBtin;
import USBtin.USBtinException;
import error_detection.ErrorCorrectionCode;
import host_communication.CANSender;
import util.CANAuthMessage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class IAT_Node {

    private long PERIOD;
    private int WINDOW_LENGTH;
    private CANAuthMessage AUTH_MESSAGE;

    private int placeInWindow = 0;
    private int indexInAuthMessage = 0;
    private boolean running = true;
    private ErrorCorrectionCode corrector;
    private AttestationProtocol protocol;
    private int silence_start;
    private int silence_end;
    private IATBitConverter converter;
    private long delta;
    private CANSender host;

    // statistics
    private long total_sent = 0;
    private FileWriter ITTwriter;
    private long nperiod;
    private long channel;

    // silence start/end
    private int silence_counter = 0;
    private boolean starting = false;
    private boolean stopping = false;

    public IAT_Node(long period, int windowLength, int silence_start, int silence_end, IATBitConverter converter,
                    long delta, long channel, long nperiod, CANSender host) {
        PERIOD = period;
        WINDOW_LENGTH = windowLength;

        this.silence_start = silence_start * WINDOW_LENGTH;
        this.silence_end = silence_end * WINDOW_LENGTH;
        this.converter = converter;
        this.delta = delta;
        this.channel = channel;
        this.nperiod = nperiod;

        this.host = host;
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

        // statistics
        try {
            new File("timings").mkdir();
            this.ITTwriter = new FileWriter("timings/ITT_" + "P" + PERIOD + "_D" + delta + "_C" +
                    channel + "_N" + nperiod + ".csv");
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (running) {
            try {
                long timeToSleep = this.getTimeToSleep()-3;
                // Save ITT
                try {
                    this.ITTwriter.append(timeToSleep + ";" + System.currentTimeMillis() + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Thread.sleep(timeToSleep);

                this.host.sendMessage(this.host.getMessageToSend());
            }
            catch (InterruptedException ex) {
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
        if (indexInAuthMessage >= auth_bytes.size()) {
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
            if (indexInAuthMessage >= auth_bytes.size()) {
                if (!stopping) {
                    stopping = true;
                    silence_counter = 1;
                    return PERIOD;
                }
                else {
                    stopping = false;
                    silence_counter = 0;
                    this.total_sent++;
                }
            }
            if (!starting) {
                if (indexInAuthMessage == 0) { indexInAuthMessage = 1; }
                else { indexInAuthMessage += this.converter.getBitsEncoded(); }
            }
            placeInWindow = 0;
        }

        if (indexInAuthMessage > auth_bytes.size()) {
            indexInAuthMessage = 0;
            silence_counter = 1;
            return PERIOD;
        }

        placeInWindow += 1;

        // bit encoding
        List<Byte> restingBytes;
        try {
            restingBytes = auth_bytes.subList(indexInAuthMessage-1,
                    indexInAuthMessage-1+this.converter.getBitsEncoded());
        } catch (IndexOutOfBoundsException ex) {
            restingBytes = auth_bytes.subList(indexInAuthMessage-1, auth_bytes.size());
        }

        return this.converter.convertToIAT(restingBytes);
    }

    public void leave() {
        running = false;
        System.out.println("Total sent: " + this.total_sent);
        try {
            this.ITTwriter.close();
        }
        catch (IOException ex) {
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
