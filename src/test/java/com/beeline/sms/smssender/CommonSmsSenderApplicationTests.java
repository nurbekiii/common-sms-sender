package com.beeline.sms.smssender;

import com.beeline.sms.enums.SenderEnum;
import com.beeline.sms.exception.InvalidReceiverException;
import com.beeline.sms.exception.InvalidSenderException;
import com.beeline.sms.smssender.validate.SenderDetect;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CommonSmsSenderApplicationTests {

    @Test
    public void testSenderDetect() throws InvalidReceiverException, InvalidSenderException {
        SenderDetect detector = new SenderDetect();
        SenderEnum bee2bee = detector.getSender("996773905665", "996225905665");
        SenderEnum bee2nur = detector.getSender("996773905665", "996703905665");
        SenderEnum nur2nur = detector.getSender("996703905665", "996703905665");
        SenderEnum nur2mega = detector.getSender("996703905665", "996558905665");
        SenderEnum nur2bee = detector.getSender("996703905665", "996220905665");
        SenderEnum alphaNum2bee = detector.getSender("Balance", "996220905665");
        SenderEnum alphaNum2mega = detector.getSender("Leti", "996558905665");
        SenderEnum alphaNum2nur = detector.getSender("8383", "996709905665");

        //Verify request succeed
        Assert.assertEquals(bee2bee, SenderEnum.Bee2Bee);
        Assert.assertEquals(bee2nur, SenderEnum.Bee2MegaNur);
        Assert.assertEquals(nur2nur, SenderEnum.MegaNur2MegaNur);
        Assert.assertEquals(nur2mega, SenderEnum.MegaNur2MegaNur);
        Assert.assertEquals(nur2bee, SenderEnum.MegaNur2Bee);
        Assert.assertEquals(alphaNum2bee, SenderEnum.Alphanum2Bee);
        Assert.assertEquals(alphaNum2mega, SenderEnum.Alphanum2MegaNur);
        Assert.assertEquals(alphaNum2nur, SenderEnum.Alphanum2MegaNur);

        Assert.assertEquals(alphaNum2mega, alphaNum2nur);
        Assert.assertEquals(nur2nur, nur2mega);

        Assert.assertNotEquals(bee2bee, bee2nur);
        Assert.assertNotEquals(alphaNum2bee, alphaNum2mega);
    }

    @Test
    public void verifiesTypeAndMessageInvalidReceiverException() {
        SenderDetect detector = new SenderDetect();
        Throwable thrown1 = Assertions.catchThrowable(() -> detector.getSender("996773905665", ""));

        String message1 = "Указано неправильное значение параметра msisdn";

        Assertions.assertThat(thrown1).isInstanceOf(InvalidReceiverException.class).hasMessageContaining(message1);

        Throwable thrown2 = Assertions.catchThrowable(() -> detector.getSender("996773905665", null));
        Assertions.assertThat(thrown2).isInstanceOf(InvalidReceiverException.class).hasMessageContaining(message1);
    }

    @Test
    public void verifiesTypeAndMessageInvalidSenderException() {
        SenderDetect detector = new SenderDetect();
        Throwable thrown1 = Assertions.catchThrowable(() -> detector.getSender("", "996773905665"));

        String message = "Указано неправильное значение параметра sender";

        Assertions.assertThat(thrown1).isInstanceOf(InvalidSenderException.class).hasMessageContaining(message);

        Throwable thrown2 = Assertions.catchThrowable(() -> detector.getSender(null, "996773905665"));
        Assertions.assertThat(thrown2).isInstanceOf(InvalidSenderException.class).hasMessageContaining(message);
    }

    @Test
    public void verifiesTypeAndMessageInvalidSenderOrReceiverException() {
        SenderDetect detector = new SenderDetect();
        Throwable thrown1 = Assertions.catchThrowable(() -> detector.getSender(null, null));

        String message = "Указано неправильное значение параметра sender";

        Assertions.assertThat(thrown1).isInstanceOf(InvalidSenderException.class).hasMessageContaining(message);

        Throwable thrown2 = Assertions.catchThrowable(() -> detector.getSender(null, ""));
        Assertions.assertThat(thrown2).isInstanceOf(InvalidSenderException.class).hasMessageContaining(message);

        Throwable thrown3 = Assertions.catchThrowable(() -> detector.getSender("", ""));
        Assertions.assertThat(thrown3).isInstanceOf(InvalidSenderException.class).hasMessageContaining(message);

        Throwable thrown4 = Assertions.catchThrowable(() -> detector.getSender("", null));
        Assertions.assertThat(thrown4).isInstanceOf(InvalidSenderException.class).hasMessageContaining(message);
    }

}
