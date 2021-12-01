package org.onetwo.ext.poi.excel.generator;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.onetwo.ext.poi.excel.data.WorkbookData;

/***
 * excel（多sheet）生成器
 * @author weishao
 *
 */
public class WorkbookExcelGeneratorImpl extends AbstractWorkbookExcelGenerator {
	
	private WorkbookData workbookData;
//	private Map<String, Object> context;

	public WorkbookExcelGeneratorImpl(WorkbookModel workbookModel, Map<String, Object> context){
		DefaultExcelValueParser excelValueParser = new DefaultExcelValueParser(context);
//		this.context = context;
//		Object data = context.get("reportData0");
		WorkbookListener workbookListener = null;
		if(StringUtils.isNotBlank(workbookModel.getListener()))
			workbookListener = (WorkbookListener)excelValueParser.parseValue(workbookModel.getListener(), workbookModel, context);
		if(workbookListener==null)
			workbookListener = WorkbookData.EMPTY_WORKBOOK_LISTENER;
		Workbook workbook = PoiModel.FORMAT_XLSX.equalsIgnoreCase(workbookModel.getFormat())?new XSSFWorkbook():new HSSFWorkbook();
		this.workbookData = new WorkbookData(workbookModel, workbook, excelValueParser, workbookListener);
		this.workbookData.initData();
	}
	@Override
	public Workbook generateIt() {
		this.workbookData.getWorkbookListener().afterCreateWorkbook(getWorkbook());
		for(TemplateModel template : workbookData.getWorkbookModel().getSheets()){
			PoiExcelGenerator pg = ExcelGenerators.createExcelGenerator(workbookData, template);
			pg.generateIt();
		}
		return workbookData.getWorkbook();
	}

	@Override
	public Workbook getWorkbook() {
		return workbookData.getWorkbook();
	}
	@Override
	public WorkbookData getWorkbookData() {
		return workbookData;
	}

}
