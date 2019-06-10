package com.beeline.sms.smssender.validate;

import com.beeline.sms.enums.SenderEnum;
import com.beeline.sms.exception.InvalidReceiverException;
import com.beeline.sms.exception.InvalidSenderException;
import com.beeline.sms.formatter.*;
import com.beeline.sms.smssender.validate.service.Checkable;

/**
 * @author NIsaev on 06.06.2019
 */
public class SenderDetect implements Checkable {

    private static TypeFormatter beelineFormatter = BeelineFormatter.getInstance();
    private static TypeFormatter megaFormatter = MegaFormatter.getInstance();
    private static TypeFormatter nurFormatter = NurFormatter.getInstance();

    private static TypeFormatter phoneFormatter = PhoneFormatter.getInstance();

    @Override
    public SenderEnum getSender(String sender, String msisdn) throws InvalidReceiverException, InvalidSenderException {

        if (sender == null || sender.isEmpty())
            throw new InvalidSenderException("Указано неправильное значение параметра sender");

        if (!phoneFormatter.isValid(msisdn) || msisdn == null || msisdn.isEmpty())
            throw new InvalidReceiverException("Указано неправильное значение параметра msisdn");

        if (beelineFormatter.isValid(sender) && beelineFormatter.isValid(msisdn))
            return SenderEnum.Bee2Bee;

        if (beelineFormatter.isValid(sender) && (megaFormatter.isValid(msisdn) || nurFormatter.isValid(msisdn)))
            return SenderEnum.Bee2MegaNur;

        if (beelineFormatter.isValid(msisdn) && (megaFormatter.isValid(sender) || nurFormatter.isValid(sender)))
            return SenderEnum.MegaNur2Bee;

        if (phoneFormatter.isValid(msisdn) && (megaFormatter.isValid(sender) || nurFormatter.isValid(sender)))
            return SenderEnum.MegaNur2MegaNur;

        if (beelineFormatter.isValid(msisdn) && (!megaFormatter.isValid(sender) && !nurFormatter.isValid(sender)))
            return SenderEnum.Alphanum2Bee;

        if (!beelineFormatter.isValid(sender) && (megaFormatter.isValid(msisdn) || nurFormatter.isValid(msisdn)))
            return SenderEnum.Alphanum2MegaNur;

        return null;
    }
}
