package com.yujianghuai.common.web;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
@Schema(description = "分页响应结果")
public class PageResult<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "数据列表")
    private List<T> records;

    @Schema(description = "总记录数")
    private Long total;

    @Schema(description = "当前页码")
    private Long pageNum;

    @Schema(description = "每页条数")
    private Long pageSize;

    @Schema(description = "总页数")
    private Long pages;

    public static <T> PageResult<T> of(List<T> records, Long total, Long pageNum, Long pageSize) {
        PageResult<T> result = new PageResult<>();
        result.setRecords(records);
        result.setTotal(total);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        result.setPages(pageSize == null || pageSize == 0 ? 0L : (total + pageSize - 1) / pageSize);
        return result;
    }

    public static <T> PageResult<T> of(IPage<?> page, List<T> records) {
        return of(records, page.getTotal(), page.getCurrent(), page.getSize());
    }
}
