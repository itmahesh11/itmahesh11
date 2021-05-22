/**
 * @author selvakumarv
 *
 * 22-03-2021
 *
 *IModuleManagerProcess.java
 */
package com.mm.service;

import com.mm.config.Configuration;
import com.mm.entity.MMEntity;

/**
 * @author selvakumarv
 *
 */
public interface IModuleManagerProcess {
	
	
	
	default  Boolean startComponents() {
		return false;
	}
	
	default  Boolean stopComponents() {
		return false;
	}
	default  Boolean isAliveAllComponents() {
		return false;
	}
	default   Boolean isAliveOneComponent(long pid,MMEntity mmEntity) {
		return false;
	}
	default   Boolean restartComponent(long pid,MMEntity mmEntity) {
		return false;
	}
	public void getLogger(Configuration configuration);
}
