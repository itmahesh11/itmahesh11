/**
 * @author selvakumarv
 *
 * 25-03-2021
 *
 *ISubscribeToInternalBroker.java
 */
package com.mm.inf;


/**
 * @author selvakumarv
 *
 */
public interface ISubscribeToInternalBroker {

	public void keepAliveMessage(String receivedKeepAlivemsg);
	public void logMessage(String receivedLogmsg);
	public void metericMessage(String receivedMetricmsg);
	public void componentStatusMessage(String receivedcomponentmsg);
}
