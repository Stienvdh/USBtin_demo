package transmission_channel.IAT_channel;

import attestation.AttestationProtocol;
import USBtin.CANMessage;
import USBtin.CANMessageListener;
import error_detection.ErrorCorrectionCode;
import util.CANAuthMessage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class IAT_Monitor implements CANMessageListener {

    private final int WINDOW_LENGTH;
    private final long PERIOD;
    private final long DELTA;
    private final long WATCH_ID;
    private final int CHANNEL;
    private final long NOISE_PERIOD;

    private long lastArrival;
    private List<Long> window = new LinkedList<>();
    private boolean detecting = false;
    private List<Byte> authMessage = new LinkedList<>();
    private FileWriter filewriterIAT;
    private FileWriter filewriterREL;
    private ErrorCorrectionCode corrector;
    private AttestationProtocol protocol;
    private long counter;
    private int silence_start;
    private int silence_end;
    private int silence_counter;
    private IATBitConverter converter;

    public IAT_Monitor(long period, long delta, int windowLength, int watchid, int channel, long nperiod,
                       int silence_start, int silence_end, IATBitConverter converter) {
        this.PERIOD = period;
        this.DELTA = delta;
        this.WINDOW_LENGTH = windowLength;
        this.WATCH_ID = watchid;
        this.CHANNEL = channel;
        this.NOISE_PERIOD = nperiod;
        this.silence_start = silence_start;
        this.silence_end = silence_end;
        this.converter = converter;

        // statistics
        try {
            new File("timings").mkdir();
            new File("reliability").mkdir();
            //this.filewriterIAT = new FileWriter("timings/IAT_" + "P" + PERIOD + "_D" + DELTA + "_C" +
            //        CHANNEL + "_N" + NOISE_PERIOD + ".csv");
            this.filewriterREL = new FileWriter("reliability/IATrel_" + "P" + PERIOD + "_D" + DELTA + "_C" +
                    CHANNEL + "_N" + NOISE_PERIOD + ".csv");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void receiveCANMessage(CANMessage message) {
        if (message.getId()==this.WATCH_ID) {

            this.counter += 1;

            long currentTime = System.currentTimeMillis();

            // first message received
            if (lastArrival == 0) {
                lastArrival = currentTime;
                window.add(PERIOD);
                return;
            }

            long IAT = currentTime - lastArrival;

            // Save IAT
//            try {
//                this.filewriterIAT.append(IAT + "\n");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

            lastArrival = currentTime;

            // sample running average
            window.add(IAT);
            if (window.size() == WINDOW_LENGTH) {
                System.out.println(window);
                detectBit(window);
                window = new LinkedList<>();
            }
        }
    }

    private String detectBit(List<Long> fullWindow) {

        long sum = 0L;
        for (long v : fullWindow) {
            sum += v;
        }
        long avg = sum / fullWindow.size();

        int intervals = this.converter.getIntervals(avg);

        if (detecting) {
            if (intervals != 0) {
                if (silence_counter > 0) {
                    silence_counter = 0;
                    this.authMessage = new LinkedList<>();
                }

                System.out.println("detected: " + this.converter.convertFromIntervals(intervals) + " intervals: " + intervals);
                this.authMessage.addAll(this.converter.convertFromIntervals(intervals));
                return this.authMessage.toString();
            }

            // start of end silence
            else {
                silence_counter = 1;
            }
        }

        if (intervals == 0) {
            silence_counter++;

            if ( (!detecting) && silence_counter < silence_start) { return "Silence bit"; }

            // detect start silence
            if (!detecting) {
                detecting = true;
                return "Silence bit";
            }

            // detect end silence
            if (silence_counter >= silence_end) {

                // end of message detected, check error detection
                detecting = false;
                silence_counter = 0;
                if (this.corrector == null) {
                    System.out.println("DETECTED MESSAGE: " + authMessage);
                }
                else if (this.corrector.checkCodeForAuthMessage(authMessage)) {
                    try {
                        this.filewriterREL.append("1\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (authMessage.size() - this.corrector.getNrCorrectingBits() < 0) {
                        System.out.println("DETECTED MESSAGE: " + authMessage);
                    }
                    else {
                        List<Byte> mess = authMessage.subList(0, authMessage.size() - this.corrector.getNrCorrectingBits());
                        System.out.println("DETECTED MESSAGE: " + mess + " COUNTER: " + this.counter);
                    }
                }
                else {
                    try {
                        this.filewriterREL.append("0\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Error in transmission detected! Received: " + authMessage);
                }

                // check attestation
                int size = authMessage.size() - this.corrector.getNrCorrectingBits() > 0 ?
                        authMessage.size() - this.corrector.getNrCorrectingBits() :
                        0;
                CANAuthMessage canAuthMessage = this.corrector==null ?
                        new CANAuthMessage(authMessage) :
                        new CANAuthMessage(authMessage.subList(0, size));

                if (this.protocol.checkAttestationMessage(canAuthMessage)) {
                    System.out.println("Attestation OK");
                }
                else { System.out.println("Attestation NOK"); }
            }
            authMessage = new LinkedList<>();
            return "Silence bit";
        }

        return "No bit detected";
    }

    public void setCorrector(ErrorCorrectionCode corrector) {
        this.corrector = corrector;
    }

    public void setProtocol(AttestationProtocol prot) {
        this.protocol = prot;
    }

    public void leave() {
        try {
            // this.filewriterIAT.close();
            this.filewriterREL.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
