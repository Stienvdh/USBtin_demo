package transmission_channel.IAT_channel;

import attestation.AttestationProtocol;
import USBtin.CANMessage;
import USBtin.USBtin;
import USBtin.USBtinException;
import error_detection.ErrorCorrectionCode;
import transmission_channel.TransmissionThread;

public class IAT_Thread extends TransmissionThread {

    private long PERIOD;
    private long DELTA;
    private int WATCH_ID;
    private int WINDOW_LENGTH;
    private int CHANNEL;
    private String SENDER_PORT;
    private String RECEIVER_PORT;
    private long NOISE_PERIOD;

    private CANMessage message;
    private USBtin receiver;
    private IAT_Node sender;
    private IAT_Monitor monitor;
    private ErrorCorrectionCode corrector;
    private AttestationProtocol protocol;
    private int silence_start;
    private int silence_end;

    public IAT_Thread(long period, long delta, int window_length, int watchid, String sender, String receiver, int channel,
                      CANMessage mess, long nperiod, int silence_start, int silence_end) {
        this.PERIOD = period;
        this.DELTA = delta;
        this.WINDOW_LENGTH = window_length;
        this.WATCH_ID = watchid;
        this.NOISE_PERIOD = nperiod;

        this.SENDER_PORT = sender;
        this.RECEIVER_PORT = receiver;
        this.CHANNEL = channel;
        this.message = mess;
        this.silence_start = silence_start;
        this.silence_end = silence_end;
    }

    public void addAuthCorrectionCode(ErrorCorrectionCode corrector) {
        this.corrector = corrector;
    }

    public void addAttestationProtocol(AttestationProtocol prot) { this.protocol = prot; }

    public void run() {
        try {
            // create the instances
            IAT_Node sender = new IAT_Node(PERIOD, DELTA, WINDOW_LENGTH, silence_start, silence_end);
            USBtin listener = new USBtin();
            IAT_Monitor monitor = new IAT_Monitor(PERIOD, DELTA, WINDOW_LENGTH, WATCH_ID, CHANNEL, NOISE_PERIOD,
                silence_start, silence_end);

            this.sender = sender;
            this.receiver = listener;
            this.monitor = monitor;

            // add authentication error correction
            if (this.corrector != null) {
                this.sender.setCorrector(this.corrector);
                this.monitor.setCorrector(this.corrector);
            }

            // add attestation protocol
            if (this.protocol != null) {
                this.sender.setProtocol(this.protocol);
                this.monitor.setProtocol(this.protocol);
            }

            // connect to USBtin.USBtin and open CAN channel in Active-Mode
            sender.connect(SENDER_PORT);
            listener.connect(RECEIVER_PORT);
            listener.addMessageListener(monitor);

            sender.openCANChannel(CHANNEL, USBtin.OpenMode.ACTIVE);
            listener.openCANChannel(CHANNEL, USBtin.OpenMode.ACTIVE);

            sender.start(message);
        } catch (USBtinException ex) {
            ex.printStackTrace();
        }
    }

    public void end() {
        this.sender.leave();
        this.monitor.leave();

        try {
            this.receiver.closeCANChannel();
            this.receiver.disconnect();
        } catch (USBtinException e) {
            e.printStackTrace();
        }
    }
}
