package me.test.first.chanpay.api.scan.impl.converter;

import me.test.first.chanpay.api.scan.*;
import me.test.first.chanpay.api.scan.dto.*;
import org.junit.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

public class RespConverterTest extends BaseTest {

    /**
     * 正常例子
     */
    @Test
    public void converer01() {


        ZonedDateTime zdt = ZonedDateTime.of(2000, 1, 2, 11, 22, 33, 444 * 1000 * 1000, ZoneId.of("Asia/Shanghai"));

        Date tradeTime = Date.from(zdt.toInstant());

        Resp resp = new Resp();
        resp.setAcceptStatus("S");
        resp.setPartnerId("partnerId");
        resp.setTradeDate("20001122");
        resp.setTradeTime("223344");
        resp.setInputCharset("UTF-8");
        resp.setMchId("1111");
        resp.setMemo("memo");
        resp.setSign("sign");
        resp.setSignType("RSA");


        Map actural = conversionService.convert(resp, Map.class);
        Map exptected = new HashMap<String, String>();
        exptected.put("AcceptStatus", "S");
        exptected.put("PartnerId", "partnerId");
        exptected.put("TradeDate", "20001122");
        exptected.put("TradeTime", "223344");
        exptected.put("InputCharset", "UTF-8");
        exptected.put("MchId", "1111");
        exptected.put("Memo", "memo");
        exptected.put("Sign", "sign");
        exptected.put("SignType", "RSA");

        assertThat(actural).isEqualTo(exptected);
    }


    /**
     * 测试空字符串
     */
    @Test
    public void converer02() {

        Resp resp = new Resp();
        resp.setAcceptStatus("");
        resp.setPartnerId("");
        resp.setTradeDate("");
        resp.setTradeTime("");
        resp.setInputCharset("");
        resp.setMchId("");
        resp.setMemo("");
        resp.setSign("");
        resp.setSignType("");


        Map actural = conversionService.convert(resp, Map.class);
        Map exptected = new HashMap<String, String>();

        assertThat(actural).isEqualTo(exptected);
    }


    /**
     * 测试 null
     */
    @Test
    public void converer03() {

        Resp resp = new Resp();
        resp.setAcceptStatus("");
        resp.setPartnerId(null);
        resp.setTradeDate("");
        resp.setTradeTime("");
        resp.setInputCharset("");
        resp.setMchId(null);
        resp.setMemo(null);
        resp.setSign("");
        resp.setSignType("");

        Map actural = conversionService.convert(resp, Map.class);
        Map exptected = new HashMap<String, String>();

        assertThat(actural).isEqualTo(exptected);
    }

}
