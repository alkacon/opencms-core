package com.opencms.file;

/**
 * The class is used to hold results from the table CMS_LANGUAGE
 */
public class CmsLanguage 
{
	private int languageId;
	private String name;
	private String shortName;
	private int priority;	
/**
 * Insert the method's description here.
 * Creation date: (22-09-2000 12:56:37)
 * @param languageId int
 * @param name java.lang.String
 * @param shortName java.lang.String
 * @param priority int
 */
public CmsLanguage(int languageId, String name, String shortName, int priority)
{
	this.languageId=languageId;
	this.name=name;
	this.shortName=shortName;
	this.priority=priority;
}
/**
 * Insert the method's description here.
 * Creation date: (22-09-2000 12:54:58)
 * @return int
 */
public int getLanguageId() {
	return languageId;
}
/**
 * Insert the method's description here.
 * Creation date: (22-09-2000 12:54:58)
 * @return java.lang.String
 */
public java.lang.String getName() {
	return name;
}
/**
 * Insert the method's description here.
 * Creation date: (22-09-2000 12:54:58)
 * @return int
 */
public int getPriority() {
	return priority;
}
/**
 * Insert the method's description here.
 * Creation date: (22-09-2000 12:54:58)
 * @return java.lang.String
 */
public java.lang.String getShortName() {
	return shortName;
}
/**
 * Insert the method's description here.
 * Creation date: (22-09-2000 12:54:58)
 * @param newLanguageId int
 */
public void setLanguageId(int newLanguageId) {
	languageId = newLanguageId;
}
/**
 * Insert the method's description here.
 * Creation date: (22-09-2000 12:54:58)
 * @param newName java.lang.String
 */
public void setName(java.lang.String newName) {
	name = newName;
}
/**
 * Insert the method's description here.
 * Creation date: (22-09-2000 12:54:58)
 * @param newPriority int
 */
public void setPriority(int newPriority) {
	priority = newPriority;
}
/**
 * Insert the method's description here.
 * Creation date: (22-09-2000 12:54:58)
 * @param newShortName java.lang.String
 */
public void setShortName(java.lang.String newShortName) {
	shortName = newShortName;
}
}
