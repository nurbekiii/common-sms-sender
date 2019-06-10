package com.beeline.sms.smssender.validate;

import com.beeline.sms.enums.ReplaceStrategyEnum;
import com.beeline.sms.enums.SenderEnum;

import java.util.List;

/**
 * @author NIsaev on 06.06.2019
 */
public class Allowance {

    private SenderEnum sender;
    private List<ReplaceStrategyEnum> strategies;

    public Allowance(SenderEnum sender, List<ReplaceStrategyEnum> strategies) {
        this.sender = sender;
        this.strategies = strategies;
    }

    public SenderEnum getSender() {
        return sender;
    }

    public void setSender(SenderEnum sender) {
        this.sender = sender;
    }

    public List<ReplaceStrategyEnum> getStrategies() {
        return strategies;
    }

    public void setStrategies(List<ReplaceStrategyEnum> strategies) {
        this.strategies = strategies;
    }
}
