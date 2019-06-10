package com.beeline.sms.model;

/**
 * @author NIsaev on 17.05.2019
 */
public class OutSMS {

    private String destAddress;
    private String shortMessage;
    private String sender;
    private int segmentCount;
    private int segmentNo;
    private long uniqueID;
    private boolean isCyrillic;

    private String extSender;
    private String productAlias;


    public OutSMS(String destAddress, String shortMessage, String sender, int segmentCount, int segmentNo, long uniqueID, boolean isCyrillic,
                  String extSender, String productAlias) {
        this.destAddress = destAddress;
        this.shortMessage = shortMessage;
        this.sender = sender;

        this.segmentCount = segmentCount;
        this.segmentNo = segmentNo;
        this.uniqueID = uniqueID;
        this.isCyrillic = isCyrillic;

        this.extSender = extSender;
        this.productAlias = productAlias;
    }

    public String getDestAddress() {
        return destAddress;
    }

    public void setDestAddress(String destAddress) {
        this.destAddress = destAddress;
    }

    public String getShortMessage() {
        return shortMessage;
    }

    public void setShortMessage(String shortMessage) {
        this.shortMessage = shortMessage;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public int getSegmentCount() {
        return segmentCount;
    }

    public void setSegmentCount(int segmentCount) {
        this.segmentCount = segmentCount;
    }

    public int getSegmentNo() {
        return segmentNo;
    }

    public void setSegmentNo(int segmentNo) {
        this.segmentNo = segmentNo;
    }

    public long getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(long uniqueID) {
        this.uniqueID = uniqueID;
    }

    public boolean isCyrillic() {
        return isCyrillic;
    }

    public void setCyrillic(boolean cyrillic) {
        isCyrillic = cyrillic;
    }

    public String getExtSender() {
        return extSender;
    }

    public void setExtSender(String extSender) {
        this.extSender = extSender;
    }

    public String getProductAlias() {
        return productAlias;
    }

    public void setProductAlias(String productAlias) {
        this.productAlias = productAlias;
    }
}
