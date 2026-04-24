package com.yujianghuai.biz.fund.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yujianghuai.biz.fund.model.FundEstimateVO;
import com.yujianghuai.biz.fund.model.FundSearchVO;
import com.yujianghuai.common.exception.BizException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class FundMarketClient {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(4))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public FundMarketClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<FundSearchVO> search(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return List.of();
        }
        String encodedKeyword = URLEncoder.encode(keyword.trim(), StandardCharsets.UTF_8);
        String url = "https://fundsuggest.eastmoney.com/FundSearch/api/FundSearchAPI.ashx?m=1&key=" + encodedKeyword;
        try {
            String body = get(url);
            JsonNode root = objectMapper.readTree(body);
            JsonNode datas = root.path("Datas");
            List<FundSearchVO> funds = new ArrayList<>();
            for (JsonNode item : datas) {
                FundSearchVO vo = new FundSearchVO();
                vo.setCode(item.path("CODE").asText());
                vo.setName(item.path("NAME").asText());
                JsonNode baseInfo = item.path("FundBaseInfo");
                vo.setType(baseInfo.path("FTYPE").asText(""));
                vo.setCompany(baseInfo.path("JJGS").asText(""));
                vo.setNav(decimalOrNull(baseInfo.path("DWJZ").asText("")));
                vo.setNavDate(baseInfo.path("FSRQ").asText(""));
                funds.add(vo);
                if (funds.size() >= 12) {
                    break;
                }
            }
            return funds;
        } catch (Exception exception) {
            throw new BizException(502, "基金搜索服务暂不可用");
        }
    }

    public FundEstimateVO estimate(String code) {
        String url = "https://fundgz.1234567.com.cn/js/" + code + ".js?rt=" + System.currentTimeMillis();
        try {
            String body = get(url);
            String json = unwrapJsonp(body);
            JsonNode root = objectMapper.readTree(json);

            FundEstimateVO vo = new FundEstimateVO();
            vo.setCode(root.path("fundcode").asText(code));
            vo.setName(root.path("name").asText(code));
            vo.setNavDate(root.path("jzrq").asText(""));
            vo.setPreviousNav(decimalOrZero(root.path("dwjz").asText("0")));
            vo.setEstimateNav(decimalOrZero(root.path("gsz").asText("0")));
            vo.setEstimateRate(decimalOrZero(root.path("gszzl").asText("0")));
            vo.setEstimateTime(root.path("gztime").asText(""));
            return vo;
        } catch (Exception exception) {
            FundEstimateVO vo = new FundEstimateVO();
            vo.setCode(code);
            vo.setError("实时估值暂不可用");
            return vo;
        }
    }

    private String get(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(8))
                .header("User-Agent", "Mozilla/5.0")
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("Remote fund api status: " + response.statusCode());
        }
        return response.body();
    }

    private String unwrapJsonp(String body) {
        int start = body.indexOf('(');
        int end = body.lastIndexOf(')');
        if (start < 0 || end <= start) {
            throw new IllegalArgumentException("Invalid jsonp response");
        }
        return body.substring(start + 1, end);
    }

    private BigDecimal decimalOrNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return new BigDecimal(value);
    }

    private BigDecimal decimalOrZero(String value) {
        if (!StringUtils.hasText(value)) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(value).setScale(4, RoundingMode.HALF_UP).stripTrailingZeros();
    }
}
