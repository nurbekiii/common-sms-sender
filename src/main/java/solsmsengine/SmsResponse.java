/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package solsmsengine;

/**
 * @author daliev
 */
public class SmsResponse {

    private String destAddress;
    private String shortMessage;
    private String sender;
    private double validityHrs;
    private int partsCount, partNo, partsCommonId;
    private boolean hasUnicode;
    private boolean translit;
    private boolean isFlash;
    private int sequenceId;
    private boolean confirmDelivery;

    public static class Builder {
        //mandatory

        private String destAddress;
        private String shortMessage;
        private String sender;
        //optional
        private int sequenceId = 0, partsCount = 1, partNo = 1, partsCommonId = -1;
        private double validityHrs = 1;
        private boolean hasUnicode = false;
        private boolean translit = true;
        private boolean isFlash = false;
        private boolean confirmDelivery = false;

        public Builder(String destAddress, String shortMessage, String sender) {
            this.destAddress = destAddress;
            this.shortMessage = shortMessage;
            this.sender = sender;
            this.hasUnicode = Util.hasUnicode(shortMessage);
        }

        public Builder validityHours(double valHrs) {
            this.validityHrs = valHrs;
            return this;
        }

        public Builder confirmDelivery(boolean confirm) {
            this.confirmDelivery = confirm;
            return this;
        }

        public Builder translit(boolean trans) {
            this.translit = trans;
            return this;
        }

        public Builder flashMessage(boolean flash) {
            this.isFlash = flash;
            return this;
        }

        public Builder sequenceId(int seqId) {
            this.sequenceId = seqId;
            return this;
        }

        public Builder partsCount(int partCnt) {
            this.partsCount = partCnt;
            return this;
        }

        public Builder partNo(int partNo) {
            this.partNo = partNo;
            return this;
        }

        public Builder partsCommonId(int partComId) {
            this.partsCommonId = partComId;
            return this;
        }

        public SmsResponse build() {
            return new SmsResponse(this);
        }
    }

    private SmsResponse(Builder builder) {
        this.destAddress = builder.destAddress;
        this.shortMessage = builder.shortMessage;
        this.sender = builder.sender;
        this.hasUnicode = builder.hasUnicode;
        this.confirmDelivery = builder.confirmDelivery;
        this.isFlash = builder.isFlash;
        this.partNo = builder.partNo;
        this.partsCount = builder.partsCount;
        this.partsCommonId = builder.partsCommonId;
        this.sequenceId = builder.sequenceId;
        this.validityHrs = builder.validityHrs;
        this.translit = builder.translit;

        if (translit) {
            this.shortMessage = Util.prepareResponseString(this.shortMessage);
            this.hasUnicode = false;
        }
    }

    public boolean isConfirmDelivery() {
        return confirmDelivery;
    }

    public String getDestAddress() {
        return destAddress;
    }

    public boolean isHasUnicode() {
        return hasUnicode;
    }

    public boolean isIsFlash() {
        return isFlash;
    }

    public int getPartNo() {
        return partNo;
    }

    public int getPartsCommonId() {
        return partsCommonId;
    }

    public int getPartsCount() {
        return partsCount;
    }

    public String getSender() {
        return sender;
    }

    public int getSequenceId() {
        return sequenceId;
    }

    public String getShortMessage() {
        return shortMessage;
    }

    public boolean isTranslit() {
        return translit;
    }

    public double getValidityHrs() {
        return validityHrs;
    }

    /*  public void setConfirmDelivery(boolean confirmDelivery) {
    this.confirmDelivery = confirmDelivery;
    }
    
    //    public void setDestAddress(String destAddress) {
    //        this.destAddress = destAddress;
    //    }
    
    public void setHasUnicode(boolean hasUnicode) {
    this.hasUnicode = hasUnicode;
    }
    
    public void setIsFlash(boolean isFlash) {
    this.isFlash = isFlash;
    }
    
    public void setPartNo(int partNo) {
    this.partNo = partNo;
    }
    
    public void setPartsCommonId(int partsCommonId) {
    this.partsCommonId = partsCommonId;
    }
    
    public void setPartsCount(int partsCount) {
    this.partsCount = partsCount;
    }
    
    //    public void setSender(String sender) {
    //        this.sender = sender;
    //    }
    
    public void setSequenceId(int sequenceId) {
    this.sequenceId = sequenceId;
    }
    
    //    public void setShortMessage(String shortMessage) {
    //        this.shortMessage = shortMessage;
    //    }
    
    public void setTranslit(boolean translit) {
    this.translit = translit;
    }
    
    public void setValidityHrs(double validityHrs) {
    this.validityHrs = validityHrs;
    }
     * */
}
