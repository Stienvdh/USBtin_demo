package util;

import error_detection.ErrorCorrectionCode;

import java.util.LinkedList;
import java.util.List;

public class CANAuthMessage {

    private List<Byte> message = new LinkedList<Byte>();
    private List<Byte> correctionCode = new LinkedList<Byte>();

    public CANAuthMessage(List<Byte> message) {
        this.message = message;
    }

    public void setCorrectionCode(ErrorCorrectionCode corrector) {
        this.correctionCode = corrector.getCodeForAuthMessage(this);
    }

    public List<Byte> toByteArray() {
        List<Byte> result = new LinkedList<>();
        result.addAll(this.message);
        result.addAll(this.correctionCode);
        return result;
    }

    public List<Byte> getMessage() {
        return this.message;
    }

}
