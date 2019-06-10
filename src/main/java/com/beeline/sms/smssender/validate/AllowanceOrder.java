package com.beeline.sms.smssender.validate;

import com.beeline.sms.enums.ReplaceStrategyEnum;
import com.beeline.sms.enums.SenderEnum;
import com.beeline.sms.smssender.validate.service.Allowable;

import java.util.Arrays;
import java.util.List;

/**
 * @author NIsaev on 06.06.2019
 */
public class AllowanceOrder implements Allowable {

    @Override
    public List<Allowance> getAllowances() {

        List<ReplaceStrategyEnum> defList = Arrays.asList(ReplaceStrategyEnum.DEFAULT);
        List<ReplaceStrategyEnum> replListEx = Arrays.asList(ReplaceStrategyEnum.DEFAULT, ReplaceStrategyEnum.REPLACE_SENDER);
        List<ReplaceStrategyEnum> replList = Arrays.asList(ReplaceStrategyEnum.REPLACE_SENDER, ReplaceStrategyEnum.EXT_REPLACE_SENDER);
        List<ReplaceStrategyEnum> allList = Arrays.asList(ReplaceStrategyEnum.DEFAULT, ReplaceStrategyEnum.REPLACE_SENDER, ReplaceStrategyEnum.EXT_REPLACE_SENDER);

        Allowance bee2bee = new Allowance(SenderEnum.Bee2Bee, allList);
        Allowance bee2megaNur = new Allowance(SenderEnum.Bee2MegaNur, allList);
        Allowance megaNur2bee = new Allowance(SenderEnum.MegaNur2Bee, replListEx);
        Allowance megaNur2megaNur = new Allowance(SenderEnum.MegaNur2MegaNur, replList);
        Allowance alphaNum2bee = new Allowance(SenderEnum.Alphanum2Bee, defList);

        return Arrays.asList(
                bee2bee, bee2megaNur, megaNur2bee, megaNur2megaNur,
                alphaNum2bee
        );
    }
}
