package com.beeline.sms;

import com.beeline.sms.enums.ReplaceStrategyEnum;

/**
 * @author NIsaev on 17.05.2019
 */
public class OutSMS {

    String destAddress;
    String shortMessage;
    String sender;
    int segmentCount;
    int segmentNo;
    long uniqueID;
    boolean isCyrillic;

    ReplaceStrategyEnum strategy;
    String extSender;
    String productAlias;


    public OutSMS(String destAddress, String shortMessage, String sender, int segmentCount, int segmentNo, long uniqueID, boolean isCyrillic,
                  ReplaceStrategyEnum strategy, String extSender, String productAlias
    ) {
        this.destAddress = destAddress;
        this.shortMessage = shortMessage;
        this.sender = sender;

        this.segmentCount = segmentCount;
        this.segmentNo = segmentNo;
        this.uniqueID = uniqueID;
        this.isCyrillic = isCyrillic;

        this.strategy = strategy;
        this.extSender = extSender;
        this.productAlias = productAlias;
    }
}
