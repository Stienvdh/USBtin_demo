package IAT_channel;

import attestation.AttestationProtocol;
import de.fischl.usbtin.CANMessage;
import de.fischl.usbtin.CANMessageListener;
import error_correction.ErrorCorrectionCode;

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

    public IAT_Monitor(long period, long delta, int windowLength, int watchid, int channel, long nperiod) {
        this.PERIOD = period;
        this.DELTA = delta;
        this.WINDOW_LENGTH = windowLength;
        this.WATCH_ID = watchid;
        this.CHANNEL = channel;
        this.NOISE_PERIOD = nperiod;

        // statistics
        try {
            new File("timings").mkdir();
            new File("reliability").mkdir();
            this.filewriterIAT = new FileWriter("timings/IAT_" + "P" + PERIOD + "_D" + DELTA + "_C" +
                    CHANNEL + "_N" + NOISE_PERIOD + ".csv");
            this.filewriterREL = new FileWriter("reliability/IATrel_" + "P" + PERIOD + "_D" + DELTA + "_C" +
                    CHANNEL + "_N" + NOISE_PERIOD + ".csv");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void receiveCANMessage(CANMessage message) {
        if (message.getId()==this.WATCH_ID) {
            long currentTime = System.currentTimeMillis();

            // first message received
            if (lastArrival == 0) {
                lastArrival = currentTime;
                window.add(PERIOD);
                return;
            }

            long IAT = currentTime - lastArrival;

            // Save IAT
            try {
                this.filewriterIAT.append(IAT + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }

            lastArrival = currentTime;

            // sample running average
            window.add(IAT);
            if (window.size() == WINDOW_LENGTH) {
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

        if (detecting) {
            if (avg >= PERIOD + DELTA/2.0) {
                authMessage.add( (byte) 0 );
                return "0";
            }

            if (avg <= PERIOD - DELTA/2.0) {
                authMessage.add( (byte) 1 );
                return "1";
            }
        }

        if (PERIOD - DELTA/2.0 < avg && avg < PERIOD + DELTA/2.0) {
            if (detecting) {
                // end of message detected, check error detection
                try {
                    this.filewriterREL.append(authMessage.toString() + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (this.corrector == null) {
                    System.out.println("DETECTED MESSAGE: " + authMessage);
                }
                else if (this.corrector.checkCodeForAuthMessage(authMessage)) {
                    if (authMessage.size() - this.corrector.getNrCorrectingBits() < 0) {
                        System.out.println("DETECTED MESSAGE: " + authMessage);
                    }
                    else {
                        List<Byte> mess = authMessage.subList(0, authMessage.size() - this.corrector.getNrCorrectingBits());
                        System.out.println("DETECTED MESSAGE: " + mess);
                    }
                }
                else {
                    System.out.println("Error in transmission detected!");
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
            detecting = !detecting;
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
            this.filewriterIAT.close();
            this.filewriterREL.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
