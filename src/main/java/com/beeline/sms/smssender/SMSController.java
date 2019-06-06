package com.beeline.sms.smssender;

import com.beeline.sms.SmsUtil;
import com.beeline.sms.enums.ReplaceStrategyEnum;
import com.beeline.sms.formatter.BeelineFormatter;
import com.beeline.sms.formatter.PhoneFormatter;
import com.beeline.sms.formatter.TypeFormatter;
import com.beeline.sms.model.Auth;
import com.beeline.sms.model.Response;
import com.beeline.sms.model.SmsRequest;
import com.beeline.sms.smssender.service.SMSService;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @author NIsaev on 17.05.2019
 */
@RestController
public class SMSController {

    private final String EMPTY_PARAMS_ERROR = "все параметры должны быть непустыми";
    private final String AUTH_PARAMS_ERROR = "параметры login и password неправильны";
    private final String MSISDN_FORMAT_ERROR = "параметр msisdn не в формате 996XXXYYYYYY";
    private final String SENDER_FORMAT_ERROR = "параметр sender не в формате 996XXXYYYYYY";
    private final String SMS_LENGTH_ERROR = "параметр text должен быть не более 126 символов в кириллице или 253 символов в латинице";
    private final String UNKNOWN_ERROR = "Произошла неизвестная ошибка при отправке СМС. Попробуйте еще раз";

    private final String PLEASE_WAIT_OK = "Сообщение принято для отправки. Пожалуйста ожидайте";

    @Autowired
    private SMSService smsService;

    private List<Auth> authList;

    private TypeFormatter beelineFormatter = BeelineFormatter.getInstance();
    private TypeFormatter phoneFormatter = PhoneFormatter.getInstance();

    @RequestMapping(value = "/sms/send", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Response> sendSmsRequest(@RequestBody SmsRequest request) throws Exception {

        String login = request.getLogin();
        String password = request.getPassword();

        String msisdn = request.getMsisdn();
        String sender = request.getSender();
        String text = request.getText();
        ReplaceStrategyEnum strategy = request.getStrategy();
        String ext_sender = request.getExt_sender();

        if (msisdn == null || text == null || sender == null || login == null || password == null ||
                msisdn.isEmpty() || text.isEmpty() || sender.isEmpty() || login.isEmpty() || password.isEmpty()) {
            return getErrorResponse(EMPTY_PARAMS_ERROR);
        }

        Auth curUser = new Auth(login, password);
        if (!authList.contains(curUser)) {
            return getErrorResponse(AUTH_PARAMS_ERROR);
        }

        boolean isMsisdn = (phoneFormatter.isValid(msisdn));
        if (!isMsisdn) {
            return getErrorResponse(MSISDN_FORMAT_ERROR);
        }

        Auth alternate = findAuth(curUser);

        boolean isSenderMsisdn = (phoneFormatter.isValid(sender));
        if (!beelineFormatter.isValid(msisdn) && !isSenderMsisdn) {
            return getErrorResponse(SENDER_FORMAT_ERROR);
        }

        boolean isCyrillic = SmsUtil.isCyrillic(text);
        if ((isCyrillic && text.length() > SmsUtil.smsLengthCyrillic) || (!isCyrillic && text.length() > SmsUtil.smsLengthLatin)) {
            return getErrorResponse(SMS_LENGTH_ERROR);
        }

        msisdn = phoneFormatter.parse(msisdn);

        boolean res = smsService.sendSms(text, msisdn, sender, strategy, ext_sender, alternate.getAlias());

        if (res) {
            return getSuccessResponse(PLEASE_WAIT_OK);
        }

        return getErrorResponse(UNKNOWN_ERROR);
    }

    private ResponseEntity<Response> getErrorResponse(String errorMessage) {
        return new ResponseEntity<>(new Response("ERROR", errorMessage), HttpStatus.valueOf(400));
    }

    private ResponseEntity<Response> getSuccessResponse(String successMessage) {
        return new ResponseEntity<>(new Response("SUCCESS", successMessage), HttpStatus.valueOf(202));
    }

    @Autowired
    public void init() {
        authList = getAuths();
    }

    private List<Auth> getAuths() {

        String appAuthUers = smsService.getAppAuthUers();

        List<Auth> list = new ArrayList<>();
        String[] pars = appAuthUers.split(",");
        for (String param : pars) {
            if (!param.contains(":"))
                continue;

            String[] pairs = param.split(":");
            Auth auth = new Auth(pairs[0], pairs[1], pairs[2]);
            list.add(auth);
        }

        return list;
    }

    private Auth findAuth(Auth auth) {
        for (Auth it : authList) {
            if (it.equals(auth))
                return it;
        }
        return null;
    }
}
