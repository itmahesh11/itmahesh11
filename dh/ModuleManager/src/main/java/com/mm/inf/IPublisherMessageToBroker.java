/**
 * @author selvakumarv
 *
 *	2021-04-01
 *
 *IPublisherMessageToBroker.java	
 */
package com.mm.inf;

import java.util.Properties;

import com.mm.util.ARSLogger;

/**
 * @author selvakumarv
 *
 */
public interface IPublisherMessageToBroker {

	
	//general method
	public void setLogger(ARSLogger logger);
	public void setProperties(Properties properties);
	
	public void systemStatusMessage(String topic,int qos,String message);
}
