package org.onetwo.common.propconf;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.onetwo.common.reflect.Intro;
import org.onetwo.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"rawtypes"})
public class PropConfig implements VariableSupporter {
	
	public static final String CONFIG_KEY = "config";
//	private static String DEBUG_KEY = "debug";

	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	protected JFishProperties config;

	protected List<String> files = new ArrayList<String>(5);
	
	protected String configName;
	
/*	protected VariableExpositor expositor;
	
	protected Expression expression = Expression.AT;
	
	private Map cache = new HashMap();*/
	

	/*public PropConfig(){
		expositor = new VariableExpositor(this, true);
	}
	public PropConfig(boolean cacheable){
		expositor = new VariableExpositor(this, cacheable);
	}*/
	
	public PropConfig(JFishProperties config){
		this.config = config;
	}

	protected PropConfig(String configName) {
		this(configName, true);//实例化时首先直接加载第一个配置文件
	}

	protected PropConfig(String configName, boolean cacheable) {
		config = new JFishProperties(cacheable);
		this.configName = configName;
		addConfigFile(configName, true);
	}

/*	protected PropConfig(String configName, VariableExpositor expositor) {
		this.configName = configName;
		addConfigFile(configName, false);
		this.expositor = expositor;
	}*/

	protected PropConfig(String name, File configFile, boolean cacheable) {
		config = new JFishProperties(cacheable);
		this.configName = name;
		addConfigFile(configFile.getPath(), true);
	}
	
	/*protected void addConfigFile(String fileName){
		addConfigFile(fileName, false);
	}*/
	
	protected void addConfigFile(String fileName, boolean load){
//		if(fileName!=null && fileName.indexOf(".")!=-1){
		if(StringUtils.isNotBlank(fileName)){
			this.files.add(fileName);
			if(load)
				this.config.load(fileName);
		}
	}

	/*public void saveToFile(String filepath) {
		try {
			FileOutputStream fout = new FileOutputStream(filepath);
			config.store(fout, null);
			System.out.println("file : " + filepath);
			fout.close();
		} catch (Exception e) {
			throw new BaseException("save config error!", e);
		}
	}*/
	
	/*protected void load(){
		if(this.files==null || this.files.isEmpty())
			return ;
		for(String f : this.files){
			if(f.equals(configName))
				continue;
			this.config.load(f);
		}
	}*/

	public void reload(){
		config.clear();
//		this.initAppConfig(true);
	}
	
	public void remove(String key){
		this.config.remove(key);
	}
	
	public String getProperty(String key, String defaultValue) {
		return config.getProperty(key, defaultValue);
	}

	public List<String> getStringList(String key, String split) {
		return config.getPropertyWithSplit(key, split);
	}

	/*public Enumeration<String> keys() {
		return (Enumeration<String>) config.propertyNames();
	}*/

	@Override
	public String getVariable(String key, boolean checkCache) {
		return config.getVariable(key, checkCache);
	}

	public String getVariable(String key) {
		return getVariable(key, false);
	}

	
	public String formatVariable(String key, Object...values){
		return config.formatVariable(key, values);
	}

	public Properties getPropertiesStartWith(String keyStartWith) {
		return config.getPropertiesStartWith(keyStartWith);
	}

	public List<String> getPropertyWithSplit(String key, String split) {
		return config.getPropertyWithSplit(key, split);
	}
	

	public List<? extends Enum<?>> getEnums(String key, Class<? extends Enum<?>> clazz){
		return config.getEnums(key, clazz);
	}

	public String getProperty(String key) {
		return config.getProperty(key);
	}

	public String getAndThrowIfEmpty(String key) {
		return config.getAndThrowIfEmpty(key);
	}

/*	public String getProperty(String key, boolean checkCache) {
		return config.getProperty(key, checkCache);
	}*/

	public Integer getInteger(String key, Integer def) {
		return config.getInteger(key, def);
	}
	
	public int getInt(String key){
		return config.getInteger(key);
	}
	
	public int getInt(String key, int def){
		return config.getInteger(key, def);
	}

	public Integer getInteger(String key) {
		return config.getInteger(key);
	}

	public Class getClass(String key, Class cls) {
		return config.getClass(key, cls);
	}

	public Collection<Class> getClasses(String key) {
		return config.getClasses(key);
	}

	public Collection<Class> getClasses(String key, Class... defClasses) {
		return config.getClasses(key, defClasses);
	}

	public List<Class> getClassList(String key) {
		return config.getClassList(key);
	}

	public Long getLong(String key, Long def) {
		return config.getLong(key, def);
	}
	

	public Boolean getBoolean(String key) {
		return config.getBoolean(key);
	}

	public Boolean getBoolean(String key, boolean def) {
		return config.getBoolean(key, def);
	}

	public Date getDate(String key, Date def) {
		return config.getDate(key, def);
	}
	

	public JFishProperties getConfig() {
		return config;
	}
	

	public String getPath(String key, String def){
		return config.getPath(key, def);
	}

	public String getDir(String key, String def){
		return config.getDir(key, def);
	}
	
	public Double getDouble(String key, Double def) {
		return config.getDouble(key, def);
	}

	public void setProperty(String key, String value){
		config.setProperty(key, value);
	}

	public void clear() {
		this.config.clear();
	}
	
	public Enumeration configNames() {
		return config.configNames();
	}

	public String getConfigName() {
		return configName;
	}
	
	public boolean containsKey(Object key){
		return this.config.containsKey(key);
	}
	
	public <T> T asObject(Class<T> clazz){
		Intro<T> jc = Intro.wrap(clazz);
		T bean = jc.newFrom((Map<Object, Object>)this.config);
		return bean;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("configName:").append(configName).append(", config:").append(config.toString());
		return sb.toString();
	}

	public static void main(String[] args) throws Exception {
		PropConfig p = new PropConfig("siteConfig-base.properties");
//		p.loadSilent();
		System.out.println(p.getProperty("app.environment"));
	}
	
}
