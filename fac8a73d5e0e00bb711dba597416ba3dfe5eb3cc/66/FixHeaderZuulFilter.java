package org.onetwo.cloud.zuul;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.onetwo.cloud.core.BootJfishCloudConfig;
import org.onetwo.cloud.core.BootJfishCloudConfig.FixHeadersConfig;
import org.onetwo.cloud.core.BootJfishCloudConfig.PathMatcher;
import org.onetwo.common.expr.Expression;
import org.onetwo.common.expr.ExpressionFacotry;
import org.onetwo.common.file.FileUtils;
import org.onetwo.common.web.utils.RequestUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.util.AntPathMatcher;

import com.google.common.collect.Sets;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;


/**
 * @author wayshall
 * <br/>
 */
@Slf4j
public class FixHeaderZuulFilter extends ZuulFilter implements InitializingBean {
	
	private static final Set<String> DEFAULT_EXCLUDE_POSTFIX = Sets.newHashSet("png", "jpg", "jpeg", "bpm", "gif", "js", "css", "mp3", "mp4", "html", "htm");
	

	@Autowired
    private BootJfishCloudConfig cloudConfig;
	
//	private List<FixHeadersConfig> fixHeaders;
	private AntPathMatcher pathMatcher = new AntPathMatcher();
	private Expression expression = ExpressionFacotry.newExpression("(", ")");
	private Set<String> excludePostfix = DEFAULT_EXCLUDE_POSTFIX;
	
	

	@Override
	public void afterPropertiesSet() throws Exception {
	}

	@Override
	public boolean shouldFilter() {
		String path = getRequestPath();
		String ext = FileUtils.getExtendName(path);
		if(StringUtils.isNotBlank(ext) && excludePostfix.contains(ext.toLowerCase())){
			return false;
		}
		return true;
	}
	
	protected String getRequestPath(){
		HttpServletRequest request = RequestContext.getCurrentContext().getRequest();
		String path = RequestUtils.getServletPath(request);
		return path;
	}

	@Override
	public Object run() {
		List<FixHeadersConfig> fixHeaders = this.cloudConfig.getZuul().getFixHeaders();
		if(fixHeaders==null){
			return null;
		}
		
		String path = getRequestPath();
		
		fixHeaders.stream().forEach(fix->{
			if(fix.getMatcher()==PathMatcher.ANT){
				doAntMatcher(fix, path);
			}else{
				doRegexMatcher(fix, path);
			}
		});
		
		return null;
	}
	
	private void doAntMatcher(FixHeadersConfig fix, String path){
		boolean match = fix.getPathPatterns().stream().anyMatch(pattern->{
			return pathMatcher.match(pattern, path);
		});
		if(match){
			if(log.isDebugEnabled()){
				log.debug("add header[{}] for path {}", fix.getHeader(), path);
			}
			RequestContext.getCurrentContext().addZuulRequestHeader(fix.getHeader(), fix.getValue());
		}
	}
	
	private void doRegexMatcher(FixHeadersConfig fix, String path){
		for(Entry<String, Pattern> entry : fix.getPatterns().entrySet()){
			Matcher matcher = entry.getValue().matcher(path);
			boolean isMatch = matcher.matches();
			if(isMatch){
				int count = matcher.groupCount();
				List<String> groups = new ArrayList<>(count);
				for (int i = 0; i <= count; i++) {
					String val = matcher.group(i);
					groups.add(val);
				}
				String value = expression.parse(fix.getValue(), groups);
				if(log.isDebugEnabled()){
					log.debug("add header[{}] for path {}", fix.getHeader(), path);
				}
				RequestContext.getCurrentContext().addZuulRequestHeader(fix.getHeader(), value);
			}
		}
	}

	@Override
	public String filterType() {
		return "pre";
	}

	@Override
	public int filterOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}

}
