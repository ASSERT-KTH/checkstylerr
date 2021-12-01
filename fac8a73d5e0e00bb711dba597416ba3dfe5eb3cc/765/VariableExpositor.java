package org.onetwo.common.propconf;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.onetwo.common.expr.Expression;
import org.onetwo.common.expr.ExpressionFacotry;
import org.onetwo.common.expr.ValueProvider;
import org.onetwo.common.expr.VariableSupporterProvider;

public class VariableExpositor {
	// private static final Log log =
	// LogFactory.getLog(VariableExpositor.class);
//	private static final String PRE_FIX = "${";
//	private static final String POST_FIX = "}";
//	private static final Pattern PATTERN = Pattern.compile("\\$\\{[\\w\\.]+\\}", Pattern.CASE_INSENSITIVE);

	protected Expression expression = ExpressionFacotry.newExpression("${", "}");
//	private VariableSupporter variabler;
	private Map<String, String> cache;
	private boolean cacheable;
	private ValueProvider valueProvider;

	public VariableExpositor(VariableSupporter variabler) {
		this(variabler, false);
	}

	public VariableExpositor(VariableSupporter variabler, boolean cacheable) {
		this.cache = new ConcurrentHashMap<String, String>();
		this.valueProvider = new VariableSupporterProvider(variabler);
		this.cacheable = cacheable;
	}

	public VariableExpositor(VariableSupporterProvider vp, boolean cacheable) {
		this.cache = new ConcurrentHashMap<String, String>();
		this.cacheable = cacheable;
		this.valueProvider = vp;
	}
	
	public String explainVariable(String source) {
		return explainVariable(source, true);
	}

	public String explainVariable(String source, boolean checkCache) {
		String result = null;
		if (checkCache && this.cacheable){
			result = cache.get(source);

			if (result != null || source == null)
				return result;
		}

		if (this.expression.isExpresstion(source)) {
			result = this.expression.parseByProvider(source, this.valueProvider);
		}else{
			result = source;
		}

		if (checkCache && this.cacheable)
			this.cache.put(source, result);

		return result;
	}


	public void clear() {
		cache.clear();
	}

	public boolean isCacheable() {
		return cacheable;
	}

	public void setCacheable(boolean cacheable) {
		this.cacheable = cacheable;
	}

}
