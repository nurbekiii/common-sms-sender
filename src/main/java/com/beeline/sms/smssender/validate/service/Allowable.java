package com.beeline.sms.smssender.validate.service;

import com.beeline.sms.smssender.validate.Allowance;

import java.util.List;

/**
 * @author NIsaev on 06.06.2019
 */
public interface Allowable {
    List<Allowance> getAllowances();
}
