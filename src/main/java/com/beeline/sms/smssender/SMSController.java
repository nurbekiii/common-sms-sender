package com.beeline.sms.smssender;

import com.beeline.sms.enums.ReplaceStrategyEnum;
import com.beeline.sms.enums.SenderEnum;
import com.beeline.sms.exception.InvalidReceiverException;
import com.beeline.sms.exception.InvalidSenderException;
import com.beeline.sms.formatter.BeelineFormatter;
import com.beeline.sms.formatter.PhoneFormatter;
import com.beeline.sms.formatter.TypeFormatter;
import com.beeline.sms.model.Auth;
import com.beeline.sms.model.Response;
import com.beeline.sms.model.SmsRequest;
import com.beeline.sms.smssender.service.SMSService;
import com.beeline.sms.smssender.validate.Allowance;
import com.beeline.sms.smssender.validate.AllowanceOrder;
import com.beeline.sms.smssender.validate.SenderDetect;
import com.beeline.sms.util.SmsUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author NIsaev on 17.05.2019
 */

@RestController
@EnableAutoConfiguration
public class SMSController {

    private final String EMPTY_PARAMS_ERROR = "все параметры должны быть непустыми";
    private final String AUTH_PARAMS_ERROR = "параметры login и password неправильны";
    private final String MSISDN_FORMAT_ERROR = "параметр msisdn не в формате 996XXXYYYYYY";
    private final String SENDER_MSISDN_FORMAT_ERROR = "параметр sender не в формате 996XXXYYYYYY или msisdn указан неверно";
    private final String SENDER_FORMAT_ERROR_EX = "параметр sender или strategy указаны неверно";
    private final String EXT_SENDER_FORMAT_ERROR = "параметр ext_sender или strategy указаны неверно";
    private final String SENDER_NOT_BEE_ERROR = "параметр sender не Beeline отправитель";
    private final String SMS_LENGTH_ERROR = "параметр text должен быть не более 600 символов";
    private final String UNKNOWN_ERROR = "Произошла неизвестная ошибка при отправке СМС. Попробуйте еще раз";
    private final String UNKNOWN_IO_ERROR = "Произошла неизвестная ошибка при отправке СМС. Попробуйте еще раз. ";

    private final String PLEASE_WAIT_OK = "Сообщение принято для отправки. Пожалуйста ожидайте";

    private static Logger logger = LogManager.getLogger(SMSController.class);

    @Autowired
    private SMSService smsService;

    private String firstBeeSender;

    private List<Auth> authList;
    private List<Allowance> allowances;

    private TypeFormatter beelineFormatter = BeelineFormatter.getInstance();
    private TypeFormatter phoneFormatter = PhoneFormatter.getInstance();

    @Autowired
    public void init() {
        //get login, password accounts
        authList = getAuths();
        //get allowance rules
        allowances = getAllowances();

        firstBeeSender = smsService.getFirstBeeSender();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleMyException(Exception ex) {
        logger.error("Exception {0}", ex);
        return getErrorResponse(ex.getMessage());
    }

    @RequestMapping(value = "/sms/send", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Response> sendSmsRequest(@RequestBody SmsRequest request) throws InvalidReceiverException, InvalidSenderException {
        return handleRequest(request);
    }

    private ResponseEntity<Response> handleRequest(SmsRequest request) throws InvalidReceiverException, InvalidSenderException {
        logger.info("SmsRequest {}", request);

        String login = request.getLogin();
        String password = request.getPassword();

        String msisdn = request.getMsisdn();
        String sender = request.getSender();
        String text = request.getText();
        //если ничего нет, использовать стратегию DEFAULT
        ReplaceStrategyEnum strategy = (request.getStrategy() != null ? request.getStrategy() : ReplaceStrategyEnum.DEFAULT);
        boolean wasStrategy = (request.getStrategy() != null);
        String ext_sender = request.getExt_sender();

        //пустые основные параметры
        if (msisdn == null || text == null || login == null || password == null ||
                msisdn.isEmpty() || text.isEmpty() || login.isEmpty() || password.isEmpty()) {
            return getErrorResponseNotAcceptable(EMPTY_PARAMS_ERROR); //"Not Acceptable"
        }

        //логин пароль указаны верно?
        Auth curUser = new Auth(login, password);
        if (!authList.contains(curUser)) {
            return getErrorResponseCode(AUTH_PARAMS_ERROR, 401); //Unauthorized
        }

        //получатель СМС не валидный номер телефона
        boolean isMsisdn = (phoneFormatter.isValid(msisdn));
        if (!isMsisdn) {
            return getErrorResponseNotAcceptable(MSISDN_FORMAT_ERROR);
        }

        //находим Alias для отправителя-продукта
        Auth alternate = findAuth(curUser);
        boolean addAliasToSmsText = true;
        //////////////
        if (beelineFormatter.isValid(msisdn)) {
            if (sender != null && !sender.isEmpty()) {
                if (sender.equalsIgnoreCase(alternate.getAlias())) {
                    addAliasToSmsText = false;
                }
            } else {
                sender = alternate.getAlias();
                request.setSender(sender);
                addAliasToSmsText = false;
            }
        } else {
            if (!wasStrategy || sender == null || sender.isEmpty()) {
                strategy = (request.getStrategy() != null ? request.getStrategy() : ReplaceStrategyEnum.REPLACE_SENDER);
                sender = firstBeeSender;
                request.setSender(sender);
                request.setStrategy(strategy);
            }
        }
        //////////////

        //не Билайн получателю запрещается отправлять от Alphanumeric отправителя
        boolean isSenderMsisdn = (phoneFormatter.isValid(sender));
        if (!isSenderMsisdn && !beelineFormatter.isValid(msisdn)) {
            return getErrorResponseNotAcceptable(SENDER_MSISDN_FORMAT_ERROR);
        }

        //если стратегия замены номера указана, но получатель и отправитель-заменитель не Билайн номера одновременно
        // (т.е отправка на MegaNur2MegaNur - не работает)
        //но возможны ситуации MegaNur2Bee и Bee2MegaNur
        if (ext_sender != null && strategy == ReplaceStrategyEnum.EXT_REPLACE_SENDER) {
            if (!beelineFormatter.isValid(ext_sender) && !beelineFormatter.isValid(msisdn)) {
                return getErrorResponseNotAcceptable(EXT_SENDER_FORMAT_ERROR);
            }
        }

        //не найдено правило по номерам и стратегии
        boolean strategyFound = allowedParams(sender, msisdn, strategy);
        if (!strategyFound) {
            return getErrorResponseNotAcceptable(wasStrategy ? SENDER_FORMAT_ERROR_EX : SENDER_NOT_BEE_ERROR);
        }

        //стратегия подмены на параметр ext_sender
        if (strategy == ReplaceStrategyEnum.EXT_REPLACE_SENDER) {
            if (ext_sender == null || ext_sender.isEmpty()) {
                return getErrorResponseNotAcceptable(EXT_SENDER_FORMAT_ERROR);
            }

            //не найдено правило по номерам и стратегии
            boolean strategyFoundEx = allowedParams(ext_sender, msisdn, strategy);
            if (!strategyFoundEx) {
                return getErrorResponseNotAcceptable(EXT_SENDER_FORMAT_ERROR);
            }
        }

        //текст СМС длиннее допустимого значения
        if (text.length() > SmsUtil.smsLength) {
            return getErrorResponseNotAcceptable(SMS_LENGTH_ERROR);
        }

        //определение текущего правила
        SenderDetect detector = new SenderDetect();
        SenderEnum factSender = detector.getSender(sender, msisdn);
        if (factSender == null) {
            return getErrorResponseNotAcceptable(SENDER_MSISDN_FORMAT_ERROR);
        }
        //текущеее правило
        Allowance curAllowance = new Allowance(factSender, Arrays.asList(strategy));

        try {
            boolean res = smsService.sendSms(request, curAllowance, alternate.getAlias(), addAliasToSmsText);

            if (res) {
                //все ок
                return getSuccessResponse(PLEASE_WAIT_OK);
            }
        } catch (IOException t) {
            t.printStackTrace();
            //неизвестная ошибка
            return getErrorResponseCode(UNKNOWN_IO_ERROR + "\n\r" + t.getMessage(), 500); //"Internal Server Error"
        }
        //неизвестная ошибка
        return getErrorResponseCode(UNKNOWN_ERROR, 500); //"Internal Server Error"
    }

    private List<Auth> getAuths() {
        String appAuthUers = smsService.getAppAuthUsers();

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

    private List<Allowance> getAllowances() {
        AllowanceOrder order = new AllowanceOrder();
        return order.getAllowances();
    }

    private boolean allowedParams(String sender, String msisdn, ReplaceStrategyEnum strategy) throws InvalidReceiverException, InvalidSenderException {
        SenderDetect detector = new SenderDetect();
        SenderEnum factSender = detector.getSender(sender, msisdn);
        for (Allowance rule : allowances) {
            if (rule.getSender() == factSender) {
                if (rule.getStrategies().contains(strategy)) {
                    return true;
                }
            }
        }
        return false;
    }

    private ResponseEntity<Response> getSuccessResponse(String successMessage) {
        logger.info("Success message: {}", successMessage);
        return new ResponseEntity<>(new Response("SUCCESS", successMessage), HttpStatus.valueOf(202));
    }

    private ResponseEntity<Response> getErrorResponse(String errorMessage) {
        logger.error("Bad request error: {}", errorMessage);
        return new ResponseEntity<>(new Response("ERROR", errorMessage), HttpStatus.valueOf(400));
    }

    private ResponseEntity<Response> getErrorResponseCode(String errorMessage, int errorCode) {
        logger.error("Bad request error: {}", errorMessage);
        return new ResponseEntity<>(new Response("ERROR", errorMessage), HttpStatus.valueOf(errorCode));
    }

    private ResponseEntity<Response> getErrorResponseNotAcceptable(String errorMessage) {
        return getErrorResponseCode(errorMessage, 406); //"Not Acceptable"
    }
}
