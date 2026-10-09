package com.htyoudao.youdao.module.order.service.report.export;

import com.alibaba.excel.write.handler.SheetWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.List;

public class HeaderMergeSheetWriteHandler implements SheetWriteHandler {

    private final List<CellRangeAddress> mergeRegions;

    public HeaderMergeSheetWriteHandler(List<CellRangeAddress> mergeRegions) {
        this.mergeRegions = mergeRegions;
    }

    @Override
    public void afterSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {
        if (mergeRegions == null || mergeRegions.isEmpty()) {
            return;
        }
        for (CellRangeAddress mergeRegion : mergeRegions) {
            writeSheetHolder.getSheet().addMergedRegionUnsafe(mergeRegion);
        }
    }
}
