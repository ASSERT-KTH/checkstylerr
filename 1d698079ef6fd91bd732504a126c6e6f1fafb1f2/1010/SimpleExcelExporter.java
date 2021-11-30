package com.duodian.admore.admin.module.report.helper;

import com.duodian.admore.core.helper.HeaderHelper;
import org.apache.commons.io.IOUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.List;

/**
 * 类描述： 简单excel导出类
 * 创建人： j.sh
 * 创建时间： 07/02/2017
 * version：1.0.0
 */
public class SimpleExcelExporter {

    private static final int WORD_UNIT = 256;

    private String[] headers;
    private String[] contentFields;
    private Integer[] columnWidths;

    private String fileName;
    private String sheetName;
    private List content;

    public SimpleExcelExporter(String fileName, String sheetName, List content) {
        this.fileName = fileName;
        this.sheetName = sheetName;
        this.content = content;
    }

    public SimpleExcelExporter withHeaders(String[] headers){
        this.headers = headers;
        return this;
    }

    public SimpleExcelExporter withContentFields(String[] contentFields){
        this.contentFields = contentFields;
        return this;
    }

    public SimpleExcelExporter withColumnWidths(Integer[] columnWidths){
        this.columnWidths = columnWidths;
        return this;
    }

    public void exportTo(HttpServletResponse response) {
        if (headers == null || headers.length == 0){
            throw new IllegalArgumentException("headers not set yet");
        }
        if (contentFields == null || contentFields.length == 0){
            throw new IllegalArgumentException("contentFields not set yet");
        }

        HSSFWorkbook workbook = null;
        try {
            response.setContentType("APPLICATION/OCTET-STREAM");

            HeaderHelper.setResponseHeader(response,fileName);
//            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));

            workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet(sheetName);

            this.setColumnWidth(sheet);
            this.createTitle(workbook,sheet);
            this.createContent(workbook,sheet, content);

            workbook.write(response.getOutputStream());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(workbook);
        }
    }

    private HSSFCellStyle createIntegerCellStyle(HSSFWorkbook workbook) {
        DataFormat format = workbook.createDataFormat();
        HSSFCellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setDataFormat(format.getFormat("#,##0"));
        cellStyle.setAlignment(HorizontalAlignment.RIGHT);
        return cellStyle;
    }

    private HSSFCellStyle createFloatCellStyle(HSSFWorkbook workbook) {
        DataFormat format = workbook.createDataFormat();
        HSSFCellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setDataFormat(format.getFormat("#,##0.00"));
        cellStyle.setAlignment(HorizontalAlignment.RIGHT);
        return cellStyle;
    }

    private HSSFCellStyle createTitleCellStyle(HSSFWorkbook workbook) {
        HSSFCellStyle cellStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        cellStyle.setFont(font);

        cellStyle.setFillForegroundColor(HSSFColor.LIME.index);
        cellStyle.setFillPattern(FillPatternType.THICK_FORWARD_DIAG);
        cellStyle.setFillBackgroundColor(IndexedColors.GREY_25_PERCENT.index);
        return cellStyle;
    }

    private void setColumnWidth(HSSFSheet sheet) {
        if (columnWidths == null || columnWidths.length == 0){
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
        } else {
            for (int i = 0; i < columnWidths.length; i++) {
                sheet.setColumnWidth(i, columnWidths[i] * WORD_UNIT);
            }
        }
    }

    private void createTitle(HSSFWorkbook workbook,HSSFSheet sheet) {
        HSSFCellStyle titleStyle = this.createTitleCellStyle(workbook);

        HSSFRow row = sheet.createRow(0);

        for (int i = 0; i < headers.length; i++) {
            HSSFCell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(titleStyle);
        }
    }

    private void createContent(HSSFWorkbook workbook,HSSFSheet sheet, List content) {
        HSSFCellStyle integerStyle = this.createIntegerCellStyle(workbook);
        HSSFCellStyle floatStyle = this.createFloatCellStyle(workbook);

        for (int i = 0; i < content.size(); i++) {
            HSSFRow row = sheet.createRow(i + 1); //排除标题行，从1开始
            BeanWrapper wrapper = new BeanWrapperImpl(content.get(i));

            for (int j = 0; j < contentFields.length; j++) {
                HSSFCell cell = row.createCell(j);

                Object value = wrapper.getPropertyValue(contentFields[j]);
                if (value == null){
                    value = "";
                }

                this.setCellValue(cell,value,integerStyle,floatStyle);
            }
        }
    }

    private void setCellValue(HSSFCell cell,Object value,HSSFCellStyle integerStyle,HSSFCellStyle floatStyle){
        if (value instanceof String){
            cell.setCellValue(value.toString());
        } else if (value instanceof Integer || value instanceof Long){
            cell.setCellValue(Long.parseLong(value.toString()));
            cell.setCellStyle(integerStyle);
        } else if (value instanceof Float || value instanceof Double || value instanceof BigDecimal){
            cell.setCellValue(Double.parseDouble(value.toString()));
            cell.setCellStyle(floatStyle);
        }
    }

}
