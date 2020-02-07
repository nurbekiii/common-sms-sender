/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package solsmsengine;

import soul.smpp.message.DeliverSM;
import soul.smpp.message.SubmitSMResp;

import java.util.ArrayList;

/**
 * @author daliev
 */
public interface SmsListener {

    public ArrayList<SmsResponse> onRequest(DeliverSM dm, String msisdn, String requestBody, int partsCount, int partNo, int partsCommonId);

    public ArrayList<SmsResponse> onSubmitSMResponse(SubmitSMResp smr);

    public ArrayList<SmsResponse> onDeliverySM(DeliverSM dm);
}
