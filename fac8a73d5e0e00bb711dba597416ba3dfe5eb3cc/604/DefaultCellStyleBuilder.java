package org.onetwo.ext.poi.excel.generator;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.onetwo.ext.poi.excel.data.CellContextData;
import org.onetwo.ext.poi.utils.ClassIntroManager;
import org.onetwo.ext.poi.utils.ExcelUtils;
import org.springframework.beans.BeanWrapper;

public class DefaultCellStyleBuilder {

	private final PoiExcelGenerator generator;
	private ConcurrentHashMap<String, CellStyle> styleCache = new ConcurrentHashMap<String, CellStyle>();

	public DefaultCellStyleBuilder(PoiExcelGenerator generator) {
		super();
		this.generator = generator;
	}

	public String getStyle(FieldModel field){
		return field.getStyle();
	}
	
	public String getFont(FieldModel field){
		return field.getFont();
	}

	protected CellStyle buildCellStyle(CellContextData cellContext){
		FieldModel field = cellContext.getFieldModel();
		
		String styleString = getStyle(field);
		String fontString = getFont(field);
		if(ExcelUtils.isBlank(field.getDataFormat()) && ExcelUtils.isBlank(styleString) && ExcelUtils.isBlank(fontString)){
			return null;
		}
		
		if(!ExcelUtils.isBlank(styleString) && styleString.startsWith("#")){
			styleString = (String)cellContext.parseValue(styleString);
		}

		if(!ExcelUtils.isBlank(field.getDataFormat())){
			styleString += ";dataFormat:"+HSSFDataFormat.getBuiltinFormat(field.getDataFormat());
		}
		
		if(!ExcelUtils.isBlank(fontString) && fontString.startsWith("#")){
			fontString = (String)cellContext.parseValue(fontString);
		}
		
		String key = styleString + fontString;
		CellStyle cstyle = this.styleCache.get(key);
		if(cstyle!=null){
//			System.out.println("get style from cache");
			return cstyle;
		}
		
		cstyle = this.generator.getWorkbook().createCellStyle();
		BeanWrapper bw = ExcelUtils.newBeanWrapper(cstyle);
		
		Map<String, String> styleMap = this.generator.getPropertyStringParser().parseStyle(styleString);
		try {
			for(Entry<String, String> entry : styleMap.entrySet()){
				/*if(isStaticField(CellStyle.class, entry.getValue())){
					Object styleValue = ReflectUtils.getStaticFieldValue(CellStyle.class, getStaticField(entry.getValue()));
					ReflectUtils.setProperty(cstyle, entry.getKey(), styleValue);
				}else{
					bw.setPropertyValue(entry.getKey(), entry.getValue());
				}*/
				bw.setPropertyValue(entry.getKey(), getStyleValue(entry.getValue()));
			}
		} catch (Exception e) {
			throw ExcelUtils.wrapAsUnCheckedException(cellContext.getLocation()+" buildCellStyle error: " + e.getMessage(), e);
		}
		
		Font font = buildCellFont(cellContext, fontString);
		if(font!=null){
			cstyle.setFont(font);
		}
		
		this.styleCache.putIfAbsent(key, cstyle);
		
		return cstyle;
	}

	protected boolean isStaticField(Class<?> clazz, String value){
		if(value.startsWith("@")){
			return true;
		}
		return ClassIntroManager.getInstance().getIntro(clazz).containsField(value, true);
	}
	protected String getStaticField(String value){
		if(value.startsWith("@"))
			return value.substring(1);
		return value;
	}
	
	protected Object getFontValue(String value){
		if(isStaticField(Font.class, value)){
			Object fontValue = ClassIntroManager.getInstance().getIntro(Font.class).getFieldValue(Font.class, getStaticField(value));
			return fontValue;
		}else{
			//extends : map.get(value);
			return value;
		}
	}
	
	protected Font buildCellFont(CellContextData cellContext, String fontString){
		if(ExcelUtils.isBlank(fontString))
			return null;
		
		Font font = this.generator.getWorkbook().createFont();
		BeanWrapper bw = ExcelUtils.newBeanWrapper(font);

		Map<String, String> fontMap = this.generator.getPropertyStringParser().parseStyle(fontString);
		for(Entry<String, String> entry : fontMap.entrySet()){
			bw.setPropertyValue(entry.getKey(), getFontValue(entry.getValue()));
		}
		return font;
	}
	

	protected Object getStyleValue(String value){
		if(isStaticField(CellStyle.class, value)){
			Object fontValue = ClassIntroManager.getInstance().getIntro(CellStyle.class).getFieldValue(CellStyle.class, getStaticField(value));
			return fontValue;
		}else{
			//extends : map.get(value);
			return value;
		}
	}
}
