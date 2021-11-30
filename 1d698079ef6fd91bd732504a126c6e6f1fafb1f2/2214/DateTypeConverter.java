package cn.itcast.type.converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import com.opensymphony.xwork2.conversion.impl.DefaultTypeConverter;

public class DateTypeConverter extends DefaultTypeConverter {
	/**
	 * value 这个是请求参数
	 * toType 是转化成的类型
	 */
	@Override
	public Object convertValue(Map<String, Object> context, Object value, Class toType) {
		//这个是格式，将字符串转化时间
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		try { 
			if(toType == Date.class){
				//字符串转化为时间
				String[] params = (String[]) value;// request.getParameterValues() ，所以这个是数组
				return dateFormat.parse(params[0]);
			}else if(toType == String.class){
				//时间转化为字符串
				Date date = (Date) value;
				return dateFormat.format(date);
			}
		} catch (ParseException e) {}
		return null;
	}
}
