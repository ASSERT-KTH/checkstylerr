package com.duodian.admore.admin.utils;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 类名称：POIExcelUtil
 * 类描述：
 * 创建人：shiwei.li
 * 修改人：
 * 修改时间：2015/7/21 16:51
 * 修改备注：
 *
 * @version 1.0.0
 */
public class POIExcelHelper {

    private int totalRows = 0;// 总行数
    private int totalCells = 0;// 总列数

    public POIExcelHelper() {
    }

    // 根据文件名读取excel文件
    public List<ArrayList<String>> read(String fileName) {
        List<ArrayList<String>> dataLst = new ArrayList<ArrayList<String>>();

        // 检查文件名是否为空或者是否是Excel格式的文件
        if (fileName == null || !fileName.matches("^.+\\.(?i)((xls)|(xlsx))$")) {
            return dataLst;
        }

        boolean isExcel2003 = true;
        // 对文件的合法性进行验
        if (fileName.matches("^.+\\.(?i)(xlsx)$")) {
            isExcel2003 = false;
        }

        // 检查文件是否存在
        File file = new File(fileName);
        if (file == null || !file.exists()) {
            return dataLst;
        }

        try {
            // 读取excel
            dataLst = read(new FileInputStream(file), isExcel2003);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return dataLst;
    }

    // 根据流读取Excel文件
    public List<ArrayList<String>> read(InputStream inputStream, boolean isExcel2003) {
        List<ArrayList<String>> dataLst = null;
        try {
            // 根据版本选择创建Workbook的方式
            Workbook wb = isExcel2003 ? new HSSFWorkbook(inputStream) : new XSSFWorkbook(inputStream);
            dataLst = read(wb);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dataLst;
    }

    // 得到总行数
    public int getTotalRows() {
        return totalRows;
    }

    // 得到总列数
    public int getTotalCells() {
        return totalCells;
    }

    // 读取数据
    private List<ArrayList<String>> read(Workbook wb) {
        List<ArrayList<String>> dataLst = new ArrayList<ArrayList<String>>();

        Sheet sheet = wb.getSheetAt(0);// 得到第一个shell
        this.totalRows = sheet.getPhysicalNumberOfRows();
        if (this.totalRows >= 1 && sheet.getRow(0) != null) {
            this.totalCells = sheet.getRow(0).getPhysicalNumberOfCells();
        }

        // 循环Excel的行
        for (int r = 0; r < this.totalRows; r++) {
            Row row = sheet.getRow(r);
            if (row == null) {
                continue;
            }

            ArrayList<String> rowLst = new ArrayList<String>();

            // 循环Excel的列
            for (short c = 0; c < this.getTotalCells(); c++) {
                Cell cell = row.getCell(c);
                String cellValue = "";
                if (cell == null) {
                    rowLst.add(cellValue);
                    continue;
                }

                // 处理数字型的,自动去零
                if (Cell.CELL_TYPE_NUMERIC == cell.getCellType()) {

                    // 在excel里,日期也是数字,在此要进行判断
                    if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        cellValue = DateHelper.convertDate2String(cell.getDateCellValue());
                    } else {
                        cellValue = getRightStr(cell.getNumericCellValue() + "");
                    }
                } else if (Cell.CELL_TYPE_STRING == cell.getCellType()) {// 处理字符串型
                    cellValue = cell.getStringCellValue();
                } else if (Cell.CELL_TYPE_BOOLEAN == cell.getCellType()) {// 处理布尔型
                    cellValue = cell.getBooleanCellValue() + "";
                } else {// 其它数据类型
                    cellValue = cell.toString() + "";
                }

                rowLst.add(cellValue);
            }
            dataLst.add(rowLst);
        }
        return dataLst;
    }

    // 正确地处理整数后自动加零的情况
    private String getRightStr(String sNum) {
        DecimalFormat decimalFormat = new DecimalFormat("#.000000");
        String resultStr = decimalFormat.format(new Double(sNum));
        if (resultStr.matches("^[-+]?\\d+\\.[0]+$")) {
            resultStr = resultStr.substring(0, resultStr.indexOf("."));
        }
        return resultStr;
    }

//    public static void main(String[] args) throws Exception {
//
//        String fileName = "E:\\ExcelDemo\\电子资产使用情况.xls";
//        POIExcelHelper excelUtil = new POIExcelHelper();
//        List<ArrayList<String>> dataLst = excelUtil.read(fileName);// 读取excle，并把数据打包成list
//
//        System.out.println("	--------------------------excel内容如下--------------------------------");
//
//        for (ArrayList<String> innerLst : dataLst) {
//            StringBuffer rowData = new StringBuffer();
//            for (String dataStr : innerLst) {
//                rowData.append(",").append(dataStr);
//            }
//            if (rowData.length() > 0) {
//                System.out.println("	" + rowData.deleteCharAt(0).toString());
//            }
//        }
//
//        System.out.println("	总行数：" + excelUtil.getTotalRows() + " , 总列数：" + excelUtil.getTotalCells());
//
//    }
}
