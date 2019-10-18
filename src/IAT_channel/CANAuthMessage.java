package IAT_channel;

import error_correction.ErrorCorrectionCode;

public class CANAuthMessage {

    private byte[] message = new byte[0];
    private byte[] correctionCode = new byte[0];

    public CANAuthMessage(byte[] message) {
        this.message = message;
    }

    public void setCorrectionCode(ErrorCorrectionCode corrector) {
        this.correctionCode = corrector.getCodeForAuthMessage(this);
    }

    public byte[] toByteArray() {
        byte[] result = new byte[this.message.length + this.correctionCode.length];
        System.arraycopy(this.message, 0, result, 0, this.message.length);
        System.arraycopy(this.correctionCode, 0, result, this.message.length, this.correctionCode.length);
        return result;
    }

    public byte[] getMessage() {
        return this.message;
    }

}
