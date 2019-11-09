package transmission_channel.IAT_channel;

import attestation.AttestationProtocol;
import USBtin.CANMessage;
import USBtin.USBtin;
import USBtin.USBtinException;
import error_detection.ErrorCorrectionCode;
import host_communication.CANReceiver;
import host_communication.CANSender;
import transmission_channel.TransmissionThread;

public class IAT_Thread extends TransmissionThread {

    private long PERIOD;
    private long DELTA;
    private int WATCH_ID;
    private int WINDOW_LENGTH;
    private long NOISE_PERIOD;
    private int CHANNEL;

    private CANReceiver receiver;
    private CANSender sender;

    private IAT_Monitor IAT_receiver;
    private IAT_Node IAT_sender;

    private ErrorCorrectionCode corrector;
    private AttestationProtocol protocol;
    private int silence_start;
    private int silence_end;
    private IATBitConverter converter;

    public IAT_Thread(long period, long delta, int window_length, int watchid,
                      long nperiod, int silence_start, int silence_end, IATBitConverter converter,
                      CANReceiver receiver, CANSender sender, int channel) {
        this.PERIOD = period;
        this.DELTA = delta;
        this.WINDOW_LENGTH = window_length;
        this.WATCH_ID = watchid;
        this.NOISE_PERIOD = nperiod;
        this.CHANNEL = channel;

        this.silence_start = silence_start;
        this.silence_end = silence_end;
        this.converter = converter;

        this.receiver = receiver;
        this.sender = sender;
    }

    public void addAuthCorrectionCode(ErrorCorrectionCode corrector) {
        this.corrector = corrector;
    }

    public void addAttestationProtocol(AttestationProtocol prot) { this.protocol = prot; }

    public void run() {
        // create IAT-sender and -receiver
        IAT_Node IAT_sender = new IAT_Node(PERIOD, WINDOW_LENGTH, silence_start, silence_end, converter,
                DELTA, CHANNEL, NOISE_PERIOD, this.sender);
        IAT_Monitor IAT_receiver = new IAT_Monitor(PERIOD, DELTA, WINDOW_LENGTH, WATCH_ID, CHANNEL, NOISE_PERIOD,
                silence_start, silence_end, converter);

        this.IAT_sender = IAT_sender;
        this.IAT_receiver = IAT_receiver;

        // add authentication error correction
        if (this.corrector != null) {
            this.IAT_sender.setCorrector(this.corrector);
            this.IAT_receiver.setCorrector(this.corrector);
        }

        // add attestation protocol
        if (this.protocol != null) {
            this.IAT_sender.setProtocol(this.protocol);
            this.IAT_receiver.setProtocol(this.protocol);
        }

        // connect physical receiver to IAT-receiver
        receiver.addMessageListener(this.IAT_receiver);

        // start IAT-sender
        IAT_sender.start();
    }

    public void end() {
        this.IAT_sender.leave();
        this.IAT_receiver.leave();

        this.sender.closedCC();
        this.receiver.closedCC();
    }
}
