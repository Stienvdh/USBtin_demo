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
    private boolean stopping;
    private boolean starting;
    private int total_received;

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
            this.filewriterIAT = new FileWriter("timings/IAT_" + "P" + PERIOD + "_D" + DELTA + "_C" +
                    CHANNEL + "_N" + NOISE_PERIOD + ".csv");
            // this.filewriterREL = new FileWriter("reliability/IATrel_" + "_D" + DELTA + "_C" +
            //        CHANNEL + "_N" + NOISE_PERIOD + ".csv");
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
            try {
                this.filewriterIAT.append(IAT + ";" + System.currentTimeMillis() + "\n");
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

        int intervals = this.converter.getIntervals(avg);

        if (intervals != 0) {
            if (starting) {
                // Correct behaviour : after start silence
                if (silence_counter >= silence_start) {
                    detecting = true;
                    starting = false;
                    silence_counter = 0;
                    this.authMessage.addAll(this.converter.convertFromIntervals(intervals));
                    return authMessage.toString();
                }
                else {
                    starting = false;
                    silence_counter = 0;
                    authMessage = new LinkedList<>();
                    return "Garbage bit";
                }
            }

            else if (stopping) {
                stopping = false;
                silence_counter = 0;
                this.authMessage = new LinkedList<>();
                return "Garbage bit";
            }

            // Correct behaviour : during detection
            if (detecting) {
                this.authMessage.addAll(this.converter.convertFromIntervals(intervals));
                return this.authMessage.toString();
            }
        }

        else {
            silence_counter++;

            // Correct behaviour : start of start silence
            if (!starting && !detecting && !stopping) {
                starting = true;
                silence_counter = 1;
                authMessage = new LinkedList<>();
                return "Silence bit";
            }

            // Correct behaviour : start of end silence
            if (detecting) {
                detecting = false;
                stopping = true;
                silence_counter = 1;
            }

            if (stopping) {
                // Correct behaviour : end of end silence -> end of message
                if (silence_counter >= silence_end) {
                    stopping = false;
                    silence_counter = 0;

                    int attSize = authMessage.size();

                    // Check error detection
                    if (this.corrector == null) {
                        System.out.println("DETECTED MESSAGE: " + authMessage);
                    }
                    else if (this.corrector.checkCodeForAuthMessage(authMessage)) {
                        List<Byte> mess = authMessage.subList(0, authMessage.size() - this.corrector.getNrCorrectingBits());
                        attSize = mess.size();
                        System.out.println("DETECTED MESSAGE: " + mess + " COUNTER: " + this.counter);
                    }
                    else {
//                        try {
//                            this.filewriterREL.append("O\n");
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }

                        System.out.println("Error in transmission detected! Received: " + authMessage + " COUNTER: " + counter);
                        this.authMessage = new LinkedList<>();
                        return "Silence bit";
                    }

                    // Check attestation
                    if (this.protocol != null) {
                        CANAuthMessage auth = new CANAuthMessage(authMessage.subList(0, attSize));
                        if (this.protocol.checkAttestationMessage(auth)) {
                            System.out.println("Attestation OK");
                            this.total_received++;

//                            try {
//                                this.filewriterREL.append("1\n");
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
                        }
                        else {
                            System.out.println("Attestation NOK");

//                            try {
//                                this.filewriterREL.append("O\n");
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
                        }
                    }
                    else {
                        this.total_received++;

//                        try {
//                            this.filewriterREL.append("1\n");
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
                    }

                    this.authMessage = new LinkedList<>();
                    return "Silence bit";
                }
            }
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
        // statistics
        System.out.println("Total received: " + this.total_received);
        try {
            this.filewriterIAT.close();
            // this.filewriterREL.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
