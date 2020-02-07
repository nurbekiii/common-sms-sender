package com.beeline.sms.smssender;

import com.beeline.sms.enums.ReplaceStrategyEnum;
import com.beeline.sms.model.SmsRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SMSControllerTest {

    private String login = "balance_kg";
    private String password = "st8K9phUV8$#zNbFn";

    @Autowired
    private SMSController controllerToTest;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(controllerToTest).build();
    }

    @Test
    public void testSendSMS() throws Exception {

        SmsRequest request = new SmsRequest(login, password, "996703905665", "996773905665", "Привет Вася! 0", ReplaceStrategyEnum.DEFAULT, null);
        //202  - Accepted
        getAnswer(object2JsonString(request), status().isAccepted());

        /////////
        SmsRequest request2 = new SmsRequest(login, password, "996703905665", "996709905665", "Привет Вася! 1", null, null);
        //202  - Accepted - Strategy = REPLACE_SENDER and SENDER = BeelineMSISDN(reserve)
        getAnswer(object2JsonString(request2), status().isAccepted());

        ////////////////
        SmsRequest request3 = new SmsRequest(login, password, "996703905665", "996703905665", "Привет Вася! 2", ReplaceStrategyEnum.REPLACE_SENDER, null);
        //202  - Accepted
        getAnswer(object2JsonString(request3), status().isAccepted());
        ////////////////
        SmsRequest request4 = new SmsRequest(login, password, "996773905665", "996779211843", "Привет Вася! 3", ReplaceStrategyEnum.EXT_REPLACE_SENDER, "996775211843");
        //202  - Accepted
        getAnswer(object2JsonString(request4), status().isAccepted());
        ////////////////
        SmsRequest request5 = new SmsRequest(login, password, "996703905665", "Test", "Привет Вася! 4", ReplaceStrategyEnum.EXT_REPLACE_SENDER, null);
        //406  - Not Acceptable
        getAnswer(object2JsonString(request5), status().isNotAcceptable());

        SmsRequest request6 = new SmsRequest(login, password, "996703905665", null, "Привет Вася! 5", null, null);
        //202  - Accepted
        getAnswer(object2JsonString(request6), status().isAccepted());

        SmsRequest request7 = new SmsRequest(login, password, "996773905665", null, "Привет Вася! 6", null, null);
        //202  - Accepted
        getAnswer(object2JsonString(request7), status().isAccepted());

        /////LONG Text///
        String textLong = "Воскообразная субстанция, которую производит пищеварительная система млекопитающего в результате раздражения слизистой кишечника, — это дорогостоящий материал, широко использующийся в медицине и парфюмерии. Он действует как фиксатор запаха, а в традиционной медицине и гомеопатии — как ароматизатор." +
                "Глыбу, находящуюся у рыбака дома, оценивают в 25 тысяч фунтов стерлингов (около двух миллионов рублей). Если бы рыбак не обрезал его ножом, он мог бы выручить еще больше.";

        SmsRequest request8 = new SmsRequest(login, password, "996773905665", null, textLong, null, null);
        //202  - Accepted
        getAnswer(object2JsonString(request8), status().isAccepted());

        SmsRequest request9 = new SmsRequest(login, password, "996703905665", null, textLong, null, null);
        //202  - Accepted
        getAnswer(object2JsonString(request9), status().isAccepted());
    }

    private RequestBuilder getRequestBuilder(String request) {
        //Mocking Controller
        controllerToTest = mock(SMSController.class);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/sms/send")
                .accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .content(request)
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE);

        return requestBuilder;
    }

    private void getAnswer(String request, ResultMatcher matcher) throws Exception {
        this.mockMvc.perform(getRequestBuilder(request))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(matcher)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_UTF8_VALUE));
    }

    private String object2JsonString(Object object) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }
}