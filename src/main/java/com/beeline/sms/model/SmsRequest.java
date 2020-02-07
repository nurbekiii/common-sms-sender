package com.beeline.sms.model;

import com.beeline.sms.enums.ReplaceStrategyEnum;

/**
 * @author NIsaev on 20.05.2019
 */
public class SmsRequest {
    private String login;
    private String password;

    private String msisdn;
    private String sender;
    private String text;
    private ReplaceStrategyEnum strategy; //DEFAULT, REPLACE_SENDER, EXT_REPLACE_SENDER
    private String ext_sender;

    public SmsRequest() {

    }

    public SmsRequest(String login, String password, String msisdn, String sender, String text, ReplaceStrategyEnum strategy, String ext_sender) {
        this.login = login;
        this.password = password;
        this.msisdn = msisdn;
        this.sender = sender;
        this.text = text;
        this.strategy = strategy;
        this.ext_sender = ext_sender;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public ReplaceStrategyEnum getStrategy() {
        return strategy;
    }

    public void setStrategy(ReplaceStrategyEnum strategy) {
        this.strategy = strategy;
    }

    public String getExt_sender() {
        return ext_sender;
    }

    public void setExt_sender(String ext_sender) {
        this.ext_sender = ext_sender;
    }

    @Override
    public String toString() {
        return "{" +
                "\"login\":\"" + login + '\"' +
                ", \"password\":\"" + password + '\"' +
                ", \"msisdn\":\"" + msisdn + '\"' +
                ", \"sender\":\"" + sender + '\"' +
                ", \"text\":\"" + text + '\"' +
                ", \"strategy\":\"" + strategy + '\"' +
                ", \"ext_sender\":\"" + ext_sender + '\"' +
                "}";
    }


}
