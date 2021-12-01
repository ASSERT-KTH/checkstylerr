package org.onetwo.common.spring.ftl;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.onetwo.common.exception.BaseException;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.common.utils.StringUtils;

import freemarker.core.Environment;
import freemarker.ext.beans.BeanModel;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

/*****
 * 
 * [@foreach list=deptCode joiner=' or '; dpcode, index]
    		-- rtcs.linedept= :deptCode${index}
    		bbl.dptcode = :deptCode${index}
    	[/@foreach]
    	
 * @author way
 *
 */
@SuppressWarnings("rawtypes")
public class ForeachDirective implements NamedDirective {
	
	public static final String DIRECTIVE_NAME = "foreach";

	public static final String PARAMS_LIST = "list";
	public static final String PARAMS_SEPARATOR = "separator";
	public static final String PARAMS_JOINER = "joiner";

	@Override
	public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
		TemplateModel listModel = FtlUtils.getRequiredParameter(params, PARAMS_LIST);
		String joiner = FtlUtils.getParameterByString(params, PARAMS_JOINER, "");
		if(StringUtils.isBlank(joiner))
			joiner = FtlUtils.getParameterByString(params, PARAMS_SEPARATOR, "");//兼容当初定义错的写法
		
		Object datas = null;
		if(listModel instanceof BeanModel){
			datas = ((BeanModel) listModel).getWrappedObject();
		}else{
			throw new BaseException("error: " + listModel);
		}
		
		List<?> listDatas = LangUtils.asList(datas);
		int index = 0;
		for(Object data : listDatas){
			if(loopVars.length>=1)
				loopVars[0] = FtlUtils.wrapAsModel(data);
			if(loopVars.length>=2)
				loopVars[1] = FtlUtils.wrapAsModel(index);
			
			if(index!=0)
				env.getOut().write(joiner);
			
			body.render(env.getOut());
			index++;
		}
	}

	@Override
	public String getName() {
		return DIRECTIVE_NAME;
	}

}
