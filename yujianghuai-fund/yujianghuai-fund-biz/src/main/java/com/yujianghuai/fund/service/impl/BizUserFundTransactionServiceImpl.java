package com.yujianghuai.fund.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yujianghuai.common.exception.BusinessException;
import com.yujianghuai.common.exception.ErrorCode;
import com.yujianghuai.common.web.PageResult;
import com.yujianghuai.fund.entity.BizFundInfo;
import com.yujianghuai.fund.entity.BizUserFundHolding;
import com.yujianghuai.fund.entity.BizUserFundTransaction;
import com.yujianghuai.fund.entity.BizUserPortfolio;
import com.yujianghuai.fund.mapper.BizFundInfoMapper;
import com.yujianghuai.fund.mapper.BizUserFundHoldingMapper;
import com.yujianghuai.fund.mapper.BizUserFundTransactionMapper;
import com.yujianghuai.fund.mapper.BizUserPortfolioMapper;
import com.yujianghuai.fund.model.dto.TransactionConfirmRequest;
import com.yujianghuai.fund.model.dto.TransactionRecordQueryRequest;
import com.yujianghuai.fund.model.dto.TransactionRecordRequest;
import com.yujianghuai.fund.model.enums.TransactionStatusEnum;
import com.yujianghuai.fund.model.enums.TransactionTypeEnum;
import com.yujianghuai.fund.model.vo.TransactionRecordVO;
import com.yujianghuai.fund.service.BizUserFundTransactionService;
import com.yujianghuai.fund.support.CurrentFundUserService;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 用户基金交易记录服务实现。
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class BizUserFundTransactionServiceImpl extends ServiceImpl<BizUserFundTransactionMapper, BizUserFundTransaction>
        implements BizUserFundTransactionService {

    private static final String DEL_FLAG_NORMAL = "0";
    private static final BigDecimal ZERO_AMOUNT = BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
    private static final String[] EXPORT_HEADERS = {
            "交易记录ID", "基金代码", "基金名称", "交易类型", "交易状态", "交易时间", "确认日期",
            "交易金额", "手续费", "确认净值", "确认份额", "交易前份额", "交易后份额",
            "交易前金额", "交易后金额", "目标基金代码", "目标基金名称", "交易渠道", "外部交易流水号", "备注"
    };

    private final BizUserFundTransactionMapper transactionMapper;
    private final BizFundInfoMapper fundInfoMapper;
    private final BizUserFundHoldingMapper holdingMapper;
    private final BizUserPortfolioMapper portfolioMapper;
    private final CurrentFundUserService currentUserService;

    @Override
    public PageResult<TransactionRecordVO> page(TransactionRecordQueryRequest request) {
        Long tenantId = currentUserService.currentTenantId();
        Long userId = currentUserService.currentUserId(tenantId);
        validateDateRange(request);

        Page<BizUserFundTransaction> page = transactionMapper.selectPage(
                new Page<>(request.getPageNum(), request.getPageSize()),
                buildQueryWrapper(tenantId, userId, request));
        return PageResult.of(page, toVOList(page.getRecords()));
    }

    @Override
    public TransactionRecordVO detail(Long id) {
        Long tenantId = currentUserService.currentTenantId();
        Long userId = currentUserService.currentUserId(tenantId);
        return toVO(getOwnedTransaction(tenantId, userId, id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TransactionRecordVO create(TransactionRecordRequest request) {
        Long tenantId = currentUserService.currentTenantId();
        Long userId = currentUserService.currentUserId(tenantId);
        BizFundInfo fundInfo = getTenantFund(tenantId, request.getFundId());
        BizFundInfo targetFundInfo = request.getTargetFundId() == null ? null : getTenantFund(tenantId, request.getTargetFundId());
        validatePortfolio(tenantId, userId, request.getPortfolioId());
        validateHolding(tenantId, userId, request.getHoldingId(), fundInfo.getId());

        BizUserFundTransaction transaction = new BizUserFundTransaction();
        transaction.setTenantId(tenantId);
        transaction.setUserId(userId);
        transaction.setFundId(fundInfo.getId());
        transaction.setFundCode(fundInfo.getFundCode());
        transaction.setFundName(fundInfo.getFundName());
        fillTargetFund(transaction, targetFundInfo);
        copy(request, transaction, true);
        fillDefaults(transaction);
        transactionMapper.insert(transaction);

        log.info("新增交易记录 tenantId={}, userId={}, fundCode={}, tradeType={}, amount={}",
                tenantId, userId, transaction.getFundCode(), transaction.getTradeType(), transaction.getAmount());
        return detail(transaction.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TransactionRecordVO update(Long id, TransactionRecordRequest request) {
        Long tenantId = currentUserService.currentTenantId();
        Long userId = currentUserService.currentUserId(tenantId);
        BizUserFundTransaction transaction = getOwnedTransaction(tenantId, userId, id);
        assertPending(transaction, "仅待确认交易允许编辑");

        Long fundId = request.getFundId() == null ? transaction.getFundId() : request.getFundId();
        BizFundInfo fundInfo = getTenantFund(tenantId, fundId);
        BizFundInfo targetFundInfo = request.getTargetFundId() == null ? null : getTenantFund(tenantId, request.getTargetFundId());
        Long portfolioId = request.getPortfolioId() == null ? transaction.getPortfolioId() : request.getPortfolioId();
        Long holdingId = request.getHoldingId() == null ? transaction.getHoldingId() : request.getHoldingId();
        validatePortfolio(tenantId, userId, portfolioId);
        validateHolding(tenantId, userId, holdingId, fundInfo.getId());

        transaction.setFundId(fundInfo.getId());
        transaction.setFundCode(fundInfo.getFundCode());
        transaction.setFundName(fundInfo.getFundName());
        if (request.getTargetFundId() != null) {
            fillTargetFund(transaction, targetFundInfo);
        }
        copy(request, transaction, false);
        fillDefaults(transaction);
        transactionMapper.updateById(transaction);

        log.info("修改交易记录 tenantId={}, userId={}, transactionId={}", tenantId, userId, id);
        return detail(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id) {
        Long tenantId = currentUserService.currentTenantId();
        Long userId = currentUserService.currentUserId(tenantId);
        BizUserFundTransaction transaction = getOwnedTransaction(tenantId, userId, id);
        if (isStatus(transaction, TransactionStatusEnum.CONFIRMED)) {
            throw new BusinessException(ErrorCode.CONFLICT, "已确认交易不允许删除");
        }
        transactionMapper.deleteById(id);
        log.info("删除交易记录 tenantId={}, userId={}, transactionId={}", tenantId, userId, id);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TransactionRecordVO confirm(Long id, TransactionConfirmRequest request) {
        Long tenantId = currentUserService.currentTenantId();
        Long userId = currentUserService.currentUserId(tenantId);
        BizUserFundTransaction transaction = getOwnedTransaction(tenantId, userId, id);
        assertPending(transaction, "仅待确认交易允许确认");

        TransactionConfirmRequest confirmRequest = request == null ? new TransactionConfirmRequest() : request;
        transaction.setTradeStatus(TransactionStatusEnum.CONFIRMED.getCode());
        transaction.setConfirmDate(confirmRequest.getConfirmDate() == null ? LocalDate.now() : confirmRequest.getConfirmDate());
        if (confirmRequest.getConfirmNav() != null) {
            transaction.setConfirmNav(scaleNav(confirmRequest.getConfirmNav()));
        }
        if (confirmRequest.getConfirmShares() != null) {
            transaction.setConfirmShares(scaleAmount(confirmRequest.getConfirmShares()));
        }
        if (confirmRequest.getFee() != null) {
            transaction.setFee(scaleAmount(confirmRequest.getFee()));
        }
        if (confirmRequest.getBeforeShares() != null) {
            transaction.setBeforeShares(scaleAmount(confirmRequest.getBeforeShares()));
        }
        if (confirmRequest.getAfterShares() != null) {
            transaction.setAfterShares(scaleAmount(confirmRequest.getAfterShares()));
        }
        if (confirmRequest.getBeforeAmount() != null) {
            transaction.setBeforeAmount(scaleAmount(confirmRequest.getBeforeAmount()));
        }
        if (confirmRequest.getAfterAmount() != null) {
            transaction.setAfterAmount(scaleAmount(confirmRequest.getAfterAmount()));
        }
        transactionMapper.updateById(transaction);

        log.info("确认交易记录 tenantId={}, userId={}, transactionId={}", tenantId, userId, id);
        return detail(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TransactionRecordVO cancel(Long id) {
        Long tenantId = currentUserService.currentTenantId();
        Long userId = currentUserService.currentUserId(tenantId);
        BizUserFundTransaction transaction = getOwnedTransaction(tenantId, userId, id);
        assertPending(transaction, "仅待确认交易允许撤销");

        transaction.setTradeStatus(TransactionStatusEnum.CANCELED.getCode());
        transactionMapper.updateById(transaction);

        log.info("撤销交易记录 tenantId={}, userId={}, transactionId={}", tenantId, userId, id);
        return detail(id);
    }

    @Override
    public void export(TransactionRecordQueryRequest request, HttpServletResponse response) {
        Long tenantId = currentUserService.currentTenantId();
        Long userId = currentUserService.currentUserId(tenantId);
        validateDateRange(request);

        List<BizUserFundTransaction> transactions = transactionMapper.selectList(buildQueryWrapper(tenantId, userId, request));
        writeExcel(toVOList(transactions), response);
    }

    private LambdaQueryWrapper<BizUserFundTransaction> buildQueryWrapper(Long tenantId,
                                                                         Long userId,
                                                                         TransactionRecordQueryRequest request) {
        LambdaQueryWrapper<BizUserFundTransaction> wrapper = new LambdaQueryWrapper<BizUserFundTransaction>()
                .eq(BizUserFundTransaction::getTenantId, tenantId)
                .eq(BizUserFundTransaction::getUserId, userId)
                .eq(BizUserFundTransaction::getDelFlag, DEL_FLAG_NORMAL)
                .eq(StringUtils.hasText(request.getFundCode()), BizUserFundTransaction::getFundCode, request.getFundCode())
                .like(StringUtils.hasText(request.getFundName()), BizUserFundTransaction::getFundName, request.getFundName())
                .eq(request.getTradeType() != null, BizUserFundTransaction::getTradeType,
                        request.getTradeType() == null ? null : request.getTradeType().getCode())
                .eq(request.getTradeStatus() != null, BizUserFundTransaction::getTradeStatus,
                        request.getTradeStatus() == null ? null : request.getTradeStatus().getCode())
                .ge(request.getTradeStartDate() != null, BizUserFundTransaction::getTradeTime,
                        request.getTradeStartDate() == null ? null : request.getTradeStartDate().atStartOfDay())
                .lt(request.getTradeEndDate() != null, BizUserFundTransaction::getTradeTime,
                        request.getTradeEndDate() == null ? null : request.getTradeEndDate().plusDays(1).atStartOfDay())
                .ge(request.getConfirmStartDate() != null, BizUserFundTransaction::getConfirmDate, request.getConfirmStartDate())
                .le(request.getConfirmEndDate() != null, BizUserFundTransaction::getConfirmDate, request.getConfirmEndDate())
                .orderByDesc(BizUserFundTransaction::getTradeTime)
                .orderByDesc(BizUserFundTransaction::getCreateTime);
        return wrapper;
    }

    private BizUserFundTransaction getOwnedTransaction(Long tenantId, Long userId, Long id) {
        BizUserFundTransaction transaction = transactionMapper.selectOne(new LambdaQueryWrapper<BizUserFundTransaction>()
                .eq(BizUserFundTransaction::getTenantId, tenantId)
                .eq(BizUserFundTransaction::getUserId, userId)
                .eq(BizUserFundTransaction::getId, id)
                .eq(BizUserFundTransaction::getDelFlag, DEL_FLAG_NORMAL));
        if (transaction == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "交易记录不存在");
        }
        return transaction;
    }

    private BizFundInfo getTenantFund(Long tenantId, Long fundId) {
        BizFundInfo fundInfo = fundInfoMapper.selectOne(new LambdaQueryWrapper<BizFundInfo>()
                .eq(BizFundInfo::getTenantId, tenantId)
                .eq(BizFundInfo::getId, fundId)
                .eq(BizFundInfo::getDelFlag, DEL_FLAG_NORMAL));
        if (fundInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "基金不存在");
        }
        return fundInfo;
    }

    private void validatePortfolio(Long tenantId, Long userId, Long portfolioId) {
        if (portfolioId == null) {
            return;
        }
        Long count = portfolioMapper.selectCount(new LambdaQueryWrapper<BizUserPortfolio>()
                .eq(BizUserPortfolio::getTenantId, tenantId)
                .eq(BizUserPortfolio::getUserId, userId)
                .eq(BizUserPortfolio::getId, portfolioId)
                .eq(BizUserPortfolio::getDelFlag, DEL_FLAG_NORMAL));
        if (count == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "投资组合不存在");
        }
    }

    private void validateHolding(Long tenantId, Long userId, Long holdingId, Long fundId) {
        if (holdingId == null) {
            return;
        }
        BizUserFundHolding holding = holdingMapper.selectOne(new LambdaQueryWrapper<BizUserFundHolding>()
                .eq(BizUserFundHolding::getTenantId, tenantId)
                .eq(BizUserFundHolding::getUserId, userId)
                .eq(BizUserFundHolding::getId, holdingId)
                .eq(BizUserFundHolding::getDelFlag, DEL_FLAG_NORMAL));
        if (holding == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "持仓不存在");
        }
        if (!Objects.equals(holding.getFundId(), fundId)) {
            throw new BusinessException(ErrorCode.CONFLICT, "持仓基金与交易基金不一致");
        }
    }

    private void copy(TransactionRecordRequest request, BizUserFundTransaction transaction, boolean create) {
        if (request.getPortfolioId() != null || create) {
            transaction.setPortfolioId(request.getPortfolioId());
        }
        if (request.getHoldingId() != null || create) {
            transaction.setHoldingId(request.getHoldingId());
        }
        if (request.getTradeType() != null) {
            transaction.setTradeType(request.getTradeType().getCode());
        }
        if (request.getTradeStatus() != null) {
            transaction.setTradeStatus(request.getTradeStatus().getCode());
        }
        if (request.getTradeTime() != null) {
            transaction.setTradeTime(request.getTradeTime());
        }
        if (request.getConfirmDate() != null) {
            transaction.setConfirmDate(request.getConfirmDate());
        }
        if (request.getAmount() != null) {
            transaction.setAmount(scaleAmount(request.getAmount()));
        }
        if (request.getFee() != null) {
            transaction.setFee(scaleAmount(request.getFee()));
        }
        if (request.getConfirmNav() != null) {
            transaction.setConfirmNav(scaleNav(request.getConfirmNav()));
        }
        if (request.getConfirmShares() != null) {
            transaction.setConfirmShares(scaleAmount(request.getConfirmShares()));
        }
        if (request.getBeforeShares() != null) {
            transaction.setBeforeShares(scaleAmount(request.getBeforeShares()));
        }
        if (request.getAfterShares() != null) {
            transaction.setAfterShares(scaleAmount(request.getAfterShares()));
        }
        if (request.getBeforeAmount() != null) {
            transaction.setBeforeAmount(scaleAmount(request.getBeforeAmount()));
        }
        if (request.getAfterAmount() != null) {
            transaction.setAfterAmount(scaleAmount(request.getAfterAmount()));
        }
        if (StringUtils.hasText(request.getSourceChannel())) {
            transaction.setSourceChannel(request.getSourceChannel());
        }
        if (StringUtils.hasText(request.getExternalTradeNo())) {
            transaction.setExternalTradeNo(request.getExternalTradeNo());
        }
        if (request.getRemark() != null) {
            transaction.setRemark(request.getRemark());
        }
    }

    private void fillTargetFund(BizUserFundTransaction transaction, BizFundInfo targetFundInfo) {
        if (targetFundInfo == null) {
            transaction.setTargetFundId(null);
            transaction.setTargetFundCode(null);
            transaction.setTargetFundName(null);
            return;
        }
        transaction.setTargetFundId(targetFundInfo.getId());
        transaction.setTargetFundCode(targetFundInfo.getFundCode());
        transaction.setTargetFundName(targetFundInfo.getFundName());
    }

    private void fillDefaults(BizUserFundTransaction transaction) {
        if (!StringUtils.hasText(transaction.getTradeStatus())) {
            transaction.setTradeStatus(TransactionStatusEnum.PENDING.getCode());
        }
        if (transaction.getFee() == null) {
            transaction.setFee(ZERO_AMOUNT);
        }
        transaction.setAmount(scaleAmount(transaction.getAmount()));
    }

    private void assertPending(BizUserFundTransaction transaction, String message) {
        if (!isStatus(transaction, TransactionStatusEnum.PENDING)) {
            throw new BusinessException(ErrorCode.CONFLICT, message);
        }
    }

    private boolean isStatus(BizUserFundTransaction transaction, TransactionStatusEnum status) {
        return status.getCode().equals(transaction.getTradeStatus());
    }

    private void validateDateRange(TransactionRecordQueryRequest request) {
        if (request.getTradeStartDate() != null && request.getTradeEndDate() != null
                && request.getTradeStartDate().isAfter(request.getTradeEndDate())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "交易开始日期不能晚于交易结束日期");
        }
        if (request.getConfirmStartDate() != null && request.getConfirmEndDate() != null
                && request.getConfirmStartDate().isAfter(request.getConfirmEndDate())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "确认开始日期不能晚于确认结束日期");
        }
    }

    private List<TransactionRecordVO> toVOList(List<BizUserFundTransaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return Collections.emptyList();
        }
        return transactions.stream().map(this::toVO).toList();
    }

    private TransactionRecordVO toVO(BizUserFundTransaction transaction) {
        TransactionRecordVO vo = new TransactionRecordVO();
        vo.setId(transaction.getId());
        vo.setPortfolioId(transaction.getPortfolioId());
        vo.setHoldingId(transaction.getHoldingId());
        vo.setFundId(transaction.getFundId());
        vo.setFundCode(transaction.getFundCode());
        vo.setFundName(transaction.getFundName());
        TransactionTypeEnum tradeType = safeTradeType(transaction.getTradeType());
        vo.setTradeType(tradeType);
        vo.setTradeTypeName(tradeType == null ? transaction.getTradeType() : tradeType.getDescription());
        TransactionStatusEnum tradeStatus = safeTradeStatus(transaction.getTradeStatus());
        vo.setTradeStatus(tradeStatus);
        vo.setTradeStatusName(tradeStatus == null ? transaction.getTradeStatus() : tradeStatus.getDescription());
        vo.setTradeTime(transaction.getTradeTime());
        vo.setConfirmDate(transaction.getConfirmDate());
        vo.setAmount(scaleAmount(transaction.getAmount()));
        vo.setFee(scaleAmount(transaction.getFee()));
        vo.setConfirmNav(scaleNav(transaction.getConfirmNav()));
        vo.setConfirmShares(scaleAmount(transaction.getConfirmShares()));
        vo.setBeforeShares(scaleAmount(transaction.getBeforeShares()));
        vo.setAfterShares(scaleAmount(transaction.getAfterShares()));
        vo.setBeforeAmount(scaleAmount(transaction.getBeforeAmount()));
        vo.setAfterAmount(scaleAmount(transaction.getAfterAmount()));
        vo.setTargetFundId(transaction.getTargetFundId());
        vo.setTargetFundCode(transaction.getTargetFundCode());
        vo.setTargetFundName(transaction.getTargetFundName());
        vo.setSourceChannel(transaction.getSourceChannel());
        vo.setExternalTradeNo(transaction.getExternalTradeNo());
        vo.setRemark(transaction.getRemark());
        vo.setCreateTime(transaction.getCreateTime());
        vo.setUpdateTime(transaction.getUpdateTime());
        return vo;
    }

    private TransactionTypeEnum safeTradeType(String value) {
        try {
            return StringUtils.hasText(value) ? TransactionTypeEnum.of(value) : null;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private TransactionStatusEnum safeTradeStatus(String value) {
        try {
            return StringUtils.hasText(value) ? TransactionStatusEnum.of(value) : null;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private void writeExcel(List<TransactionRecordVO> records, HttpServletResponse response) {
        String fileName = "交易记录_" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".xlsx";
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);

        try (Workbook workbook = new SXSSFWorkbook();
             ServletOutputStream outputStream = response.getOutputStream()) {
            Sheet sheet = workbook.createSheet("交易记录");
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            writeHeader(sheet, headerStyle);
            writeRows(sheet, records);
            workbook.write(outputStream);
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "导出交易记录失败");
        }
    }

    private void writeHeader(Sheet sheet, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < EXPORT_HEADERS.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(EXPORT_HEADERS[i]);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, 18 * 256);
        }
    }

    private void writeRows(Sheet sheet, List<TransactionRecordVO> records) {
        for (int i = 0; i < records.size(); i++) {
            TransactionRecordVO record = records.get(i);
            Row row = sheet.createRow(i + 1);
            int column = 0;
            row.createCell(column++).setCellValue(toText(record.getId()));
            row.createCell(column++).setCellValue(toText(record.getFundCode()));
            row.createCell(column++).setCellValue(toText(record.getFundName()));
            row.createCell(column++).setCellValue(toText(record.getTradeTypeName()));
            row.createCell(column++).setCellValue(toText(record.getTradeStatusName()));
            row.createCell(column++).setCellValue(toText(record.getTradeTime()));
            row.createCell(column++).setCellValue(toText(record.getConfirmDate()));
            row.createCell(column++).setCellValue(toText(record.getAmount()));
            row.createCell(column++).setCellValue(toText(record.getFee()));
            row.createCell(column++).setCellValue(toText(record.getConfirmNav()));
            row.createCell(column++).setCellValue(toText(record.getConfirmShares()));
            row.createCell(column++).setCellValue(toText(record.getBeforeShares()));
            row.createCell(column++).setCellValue(toText(record.getAfterShares()));
            row.createCell(column++).setCellValue(toText(record.getBeforeAmount()));
            row.createCell(column++).setCellValue(toText(record.getAfterAmount()));
            row.createCell(column++).setCellValue(toText(record.getTargetFundCode()));
            row.createCell(column++).setCellValue(toText(record.getTargetFundName()));
            row.createCell(column++).setCellValue(toText(record.getSourceChannel()));
            row.createCell(column++).setCellValue(toText(record.getExternalTradeNo()));
            row.createCell(column).setCellValue(toText(record.getRemark()));
        }
    }

    private String toText(Object value) {
        return value == null ? "" : value.toString();
    }

    private BigDecimal scaleAmount(BigDecimal value) {
        return value == null ? null : value.setScale(4, RoundingMode.HALF_UP);
    }

    private BigDecimal scaleNav(BigDecimal value) {
        return value == null ? null : value.setScale(6, RoundingMode.HALF_UP);
    }
}
