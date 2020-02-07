package com.beeline.sms.smssender;

import solsmsengine.SmsListener;
import solsmsengine.SmsResponse;

import java.util.ArrayList;

/**
 * @author NIsaev on 03.07.2019
 */
public class MyListener implements SmsListener {

    @Override
    public ArrayList<SmsResponse> onRequest(soul.smpp.message.DeliverSM dm, String msisdn, String requestBody, int partsCount, int partNo, int partsCommonId) {
        System.out.println("msisdn: " + msisdn + " text: " + requestBody + " partsCount: " + partsCount + " partNo: " + partNo + " partsCommonId " + partsCommonId);
        ArrayList<SmsResponse> res = new ArrayList<SmsResponse>();
        res.add(new SmsResponse.Builder(msisdn, dm.getMessageText(), dm.getSource().getAddress()).translit(false).build());
        return res;
    }

    @Override
    public ArrayList<SmsResponse> onSubmitSMResponse(soul.smpp.message.SubmitSMResp smr) {
        System.out.println("onSubmitSMResponse " + smr.getCommandStatus() + " SMSC_ID " + smr.getMessageId() + " USER_ID: " + smr.getSequenceNum());
        return null;
    }

    @Override
    public ArrayList<SmsResponse> onDeliverySM(soul.smpp.message.DeliverSM dm) {
        //System.out.println("onDeliverySM " + dm.getMessageText());
        //System.out.println("onDeliverySMResponse " + dm.getCommandStatus() + " SMSC_ID " + dm.getMessageId() + " USER_ID: " + dm.getSequenceNum());
        return null;
    }
}
