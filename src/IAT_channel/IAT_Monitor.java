package IAT_channel;

import de.fischl.usbtin.CANMessage;
import de.fischl.usbtin.CANMessageListener;

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
    private List<Integer> authMessage = new LinkedList<>();
    private FileWriter filewriter;

    public IAT_Monitor(long period, long delta, int windowLength, int watchid, int channel, long nperiod) {
        this.PERIOD = period;
        this.DELTA = delta;
        this.WINDOW_LENGTH = windowLength;
        this.WATCH_ID = watchid;
        this.CHANNEL = channel;
        this.NOISE_PERIOD = nperiod;

        try {
            new File("timings").mkdir();
            this.filewriter = new FileWriter("timings/IAT_" + "P" + PERIOD + "_D" + DELTA + "_C" +
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
                this.filewriter.append(IAT + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }

            lastArrival = currentTime;

            // sample running average
            window.add(IAT);
            if (window.size() == WINDOW_LENGTH) {
                System.out.println("Detected bit: " + detectBit(window));
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
                authMessage.add(0);
                return "0";
            }

            if (avg <= PERIOD - DELTA/2.0) {
                authMessage.add(1);
                return "1";
            }
        }

        if (PERIOD - DELTA/2.0 < avg && avg < PERIOD + DELTA/2.0) {
            if (detecting) {
                System.out.print("DETECTED MESSAGE: " + authMessage + " ");
            }
            authMessage = new LinkedList<>();
            detecting = !detecting;
            return "Silence bit";
        }

        return "No bit detected";
    }

    public void leave() {
        try {
            this.filewriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
