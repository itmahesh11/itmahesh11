/**
 * @author selvakumarv
 *
 */
package com.mm.util;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Properties;

/**
 * @author selvakumarv
 *
 *         22-03-2021
 *
 *         MMUtil.java
 */
@SuppressWarnings("serial")
public class MMUtil implements Serializable {

	private static MMUtil mMUtil = null;
	

	public static MMUtil getInstance() {
		if (mMUtil == null) {
			mMUtil = new MMUtil();
		}
		return mMUtil;
	}

	public Properties getProperties() {
		Properties prop = new Properties();
		try {

			String propFileName = "config.properties";

			InputStream resourceStream = Thread.currentThread().getContextClassLoader()
					.getResourceAsStream(propFileName);
			prop.load(resourceStream);

		} catch (Exception e) {

			

		}
		return prop;
	}

	

}
