/**
 * @author selvakumarv
 *
 *	2021-04-07
 *
 *DiskspaceStatus.java	
 */
package com.mm.entity;

/**
 * @author selvakumarv
 *
 */
public class DiskspaceStatus {
	
	 //Component-Disk, property - Space, type warning,Code -<TODO>, Text - Disk space less than <limit>.
	
private String component="Disk";
private String property="Space";
private int type;
private int code;
private String text="Disk space less than";



/**
 * @return the component
 */
public String getComponent() {
	return component;
}
/**
 * @param component the component to set
 */
public void setComponent(String component) {
	this.component = component;
}
/**
 * @return the property
 */
public String getProperty() {
	return property;
}
/**
 * @param property the property to set
 */
public void setProperty(String property) {
	this.property = property;
}
/**
 * @return the type
 */
public int getType() {
	return type;
}
/**
 * @param type the type to set
 */
public void setType(int type) {
	this.type = type;
}
/**
 * @return the code
 */
public int getCode() {
	return code;
}
/**
 * @param code the code to set
 */
public void setCode(int code) {
	this.code = code;
}
/**
 * @return the text
 */
public String getText() {
	return text;
}
/**
 * @param text the text to set
 */
public void setText(String text) {
	this.text = text;
}




}
