/*
 * Copyright (C) 2018 dy_only, Inc. All Rights Reserved.
 */
package com.hlj.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtil {
	public static Properties properties = new Properties();

	public static String getProperty(String key) {
		return properties.getProperty(key) == null ? "" : properties.get(key).toString();
	}

	static {
			String profile = System.getProperty("spring.profiles.active");
		    System.out.println(profile);

			String[]  props = new String[] {"profile.properties", "resource.properties" };
			for(String prop:props){
				InputStream inputStream = PropertiesUtil.class.getClassLoader().getResourceAsStream(prop);
				if (inputStream != null) {
					Properties propertiest = new Properties();
					try {
						propertiest.load(inputStream);
						properties.putAll(propertiest);

					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
	}
}
