package error_detection;

import util.CANAuthMessage;

import java.util.LinkedList;
import java.util.List;

public class SimpleCRC implements ErrorCorrectionCode {

    private int N;
    private String polynomial = "";

    public SimpleCRC(int n, String polynomial) {
        if (n > 0 && polynomial.length() == n+1) {
            this.N = n;
            this.polynomial = polynomial;
        }
    }

    @Override
    public int getNrCorrectingBits() {
        return this.N;
    }

    @Override
    public List<Byte> getCodeForAuthMessage(CANAuthMessage message) {
        // pad dividend
        String dividend = bytesToString(message.getMessage());
        for (int i=0; i<this.N; i++) { dividend += "0"; }

        String remainder = CRCdivision(dividend);

        return stringToBytes(remainder);
    }

    @Override
    public boolean checkCodeForAuthMessage(List<Byte> message) {
        if (message.size() < this.N*2) {
            return false;
        }

        String dividend = bytesToString(message);
        String remainder = CRCdivision(dividend);
        String wantedRemainder = "";
        for (int i=0; i<this.N; i++) { wantedRemainder += "0"; }

        return remainder.equals(wantedRemainder);
    }

    private String CRCdivision(String dividend) {
        String divisor = this.polynomial;
        while (divisor.length() < dividend.length()) { divisor += "0"; }
        String padded_divisor = divisor;

        while (dividend.contains("1") && dividend.indexOf("1")<dividend.length()-this.N) {
            // align divisor and dividend
            int offset = dividend.indexOf("1");
            divisor = padded_divisor;
            if (offset>0) { for (int i=0; i<offset; i++) { divisor = "0" + divisor; } }
            divisor = divisor.substring(0, dividend.length());

            // execute division
            String new_dividend = "";
            for (int i=0; i<dividend.length(); i++) {
                char d1 = dividend.charAt(i);
                char d2 = divisor.charAt(i);
                new_dividend += (d1 == d2) ? "0" : "1";
            }
            dividend = new_dividend;
        }

        // extract last N bits (= remainder)
        return dividend.substring(dividend.length()-this.N);
    }

    private String bytesToString(List<Byte> bytes) {
        String result = "";
        for (int i=0; i<bytes.size(); i++) {
            if (bytes.get(i).equals( (byte) 1 )) {
                result += "1";
            }
            else {
                result += "0";
            }
        }
        return result;
    }

    private List<Byte> stringToBytes(String str) {
        List<Byte> result = new LinkedList<>();
        for (int i=0; i<str.length(); i++) {
            if (str.charAt(i) == '1') {
                result.add( (byte) 1 );
            }
            else {
                result.add( (byte) 0 );
            }
        }
        return result;
    }
}
