import transmission_channel.DLC_channel.DLC_Thread;
import transmission_channel.IAT_channel.IAT_Thread;
import attestation.AttestationProtocol;
import attestation.HardCodedAttestation;
import USBtin.*;
import error_detection.ErrorCorrectionCode;
import error_detection.SimpleCRC;
import noise.NoiseThread;
import transmission_channel.TransmissionThread;

import java.io.IOException;

public class USBtinLibDemo {

    private static final long PERIOD = 200;
    private static final long NOISE_PERIOD = 100; // NOISE_PERIOD=0 --> no noise
    private static final long DELTA = 10;
    private static final int WINDOW_LENGTH = 4;
    private static final int CHANNEL = 50000;

    private static final String SENDER_PORT = "/dev/tty.usbmodemA02183211";
    private static final String RECEIVER_PORT = "/dev/tty.usbmodemA02102821";
    private static final String NOISE_PORT = "/dev/tty.usbmodemA021CFBA1";
    private static final int WATCHID = 0x100;

    private static ErrorCorrectionCode AUTH_CORRECTOR =
            new SimpleCRC(2, "101"); // Set error correction instance here
    private static AttestationProtocol AUTH_PROTOCOL =
            new HardCodedAttestation(new byte[]{1,1,0,0,1}); // Set attestation protocol here
    private static TransmissionThread TRANSMISSION_CHANNEL = // Set transmission channel here
             //new IAT_Thread(PERIOD, DELTA, WINDOW_LENGTH, WATCHID, SENDER_PORT, RECEIVER_PORT, CHANNEL,
             //       new CANMessage(WATCHID, new byte[]{0x11, 0x22, 0x33}), NOISE_PERIOD);
             new DLC_Thread(PERIOD, WATCHID, SENDER_PORT, RECEIVER_PORT, CHANNEL,
                    new CANMessage("t100811223344"));

    public static void main(String[] args) {
        // Run the transmission thread
        TRANSMISSION_CHANNEL.addAuthCorrectionCode(AUTH_CORRECTOR);
        TRANSMISSION_CHANNEL.addAttestationProtocol(AUTH_PROTOCOL);
        TRANSMISSION_CHANNEL.start();

        // Run a noise thread
        NoiseThread noise = new NoiseThread(NOISE_PERIOD, CHANNEL, NOISE_PORT,
                new CANMessage(0x200, new byte[]{0x11, 0x22, 0x33}));
        noise.start();

        // End the channel
        try {
            System.in.read();
            TRANSMISSION_CHANNEL.end();
            noise.end();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
