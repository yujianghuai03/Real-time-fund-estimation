package com.yujianghuai.biz.fund.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yujianghuai.biz.fund.entity.UserFund;
import com.yujianghuai.biz.fund.mapper.UserFundMapper;
import com.yujianghuai.biz.fund.model.FundEstimateVO;
import com.yujianghuai.biz.fund.model.FundSearchVO;
import com.yujianghuai.biz.fund.model.FundWatchRequest;
import com.yujianghuai.common.exception.BizException;
import com.yujianghuai.common.tenant.TenantContext;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.Principal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class UserFundService {

    private final UserFundMapper userFundMapper;
    private final FundMarketClient fundMarketClient;

    public UserFundService(UserFundMapper userFundMapper, FundMarketClient fundMarketClient) {
        this.userFundMapper = userFundMapper;
        this.fundMarketClient = fundMarketClient;
    }

    public List<FundSearchVO> search(String keyword) {
        return fundMarketClient.search(keyword);
    }

    public List<FundEstimateVO> listWithRealtimeEstimate(Principal principal) {
        String username = currentUsername(principal);
        return userFundMapper.selectList(new LambdaQueryWrapper<UserFund>()
                        .eq(UserFund::getUsername, username)
                        .eq(UserFund::getDelFlag, "0")
                        .orderByAsc(UserFund::getSortOrder)
                        .orderByDesc(UserFund::getId))
                .stream()
                .map(this::toEstimate)
                .toList();
    }

    @Transactional
    public FundEstimateVO add(Principal principal, FundWatchRequest request) {
        String username = currentUsername(principal);
        String code = normalizeCode(request.getCode());
        UserFund exists = userFundMapper.selectOne(new LambdaQueryWrapper<UserFund>()
                .eq(UserFund::getUsername, username)
                .eq(UserFund::getFundCode, code));
        if (exists != null) {
            exists.setDelFlag("0");
            exists.setFundName(resolveName(code, request.getName()));
            exists.setHoldingAmount(nullToZero(request.getHoldingAmount()));
            userFundMapper.updateById(exists);
            return toEstimate(exists);
        }

        UserFund fund = new UserFund();
        fund.setTenantId(Long.parseLong(TenantContext.getRequiredTenantId()));
        fund.setUsername(username);
        fund.setFundCode(code);
        fund.setFundName(resolveName(code, request.getName()));
        fund.setHoldingAmount(nullToZero(request.getHoldingAmount()));
        fund.setSortOrder(0);
        fund.setDelFlag("0");
        userFundMapper.insert(fund);
        return toEstimate(fund);
    }

    @Transactional
    public Boolean updateHolding(Principal principal, String code, BigDecimal holdingAmount) {
        String username = currentUsername(principal);
        int updated = userFundMapper.update(new LambdaUpdateWrapper<UserFund>()
                .set(UserFund::getHoldingAmount, nullToZero(holdingAmount))
                .eq(UserFund::getUsername, username)
                .eq(UserFund::getFundCode, normalizeCode(code))
                .eq(UserFund::getDelFlag, "0"));
        if (updated == 0) {
            throw new BizException(404, "自选基金不存在");
        }
        return Boolean.TRUE;
    }

    @Transactional
    public Boolean delete(Principal principal, String code) {
        String username = currentUsername(principal);
        userFundMapper.update(new LambdaUpdateWrapper<UserFund>()
                .set(UserFund::getDelFlag, "1")
                .eq(UserFund::getUsername, username)
                .eq(UserFund::getFundCode, normalizeCode(code)));
        return Boolean.TRUE;
    }

    private FundEstimateVO toEstimate(UserFund fund) {
        FundEstimateVO estimate = fundMarketClient.estimate(fund.getFundCode());
        estimate.setId(fund.getId());
        estimate.setCode(fund.getFundCode());
        estimate.setName(StringUtils.hasText(estimate.getName()) && estimate.getError() == null
                ? estimate.getName() : fund.getFundName());
        estimate.setHoldingAmount(nullToZero(fund.getHoldingAmount()));
        BigDecimal rate = estimate.getEstimateRate() == null ? BigDecimal.ZERO : estimate.getEstimateRate();
        BigDecimal profit = estimate.getHoldingAmount().multiply(rate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        estimate.setEstimateProfit(profit);
        estimate.setEstimateMarketValue(estimate.getHoldingAmount().add(profit));
        return estimate;
    }

    private String resolveName(String code, String requestName) {
        if (StringUtils.hasText(requestName)) {
            return requestName.trim();
        }
        FundEstimateVO estimate = fundMarketClient.estimate(code);
        return StringUtils.hasText(estimate.getName()) ? estimate.getName() : code;
    }

    private String normalizeCode(String code) {
        if (!StringUtils.hasText(code)) {
            throw new BizException(400, "基金代码不能为空");
        }
        return code.trim();
    }

    private BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String currentUsername(Principal principal) {
        if (principal == null || !StringUtils.hasText(principal.getName())) {
            throw new BizException(401, "请先登录");
        }
        return principal.getName();
    }
}
