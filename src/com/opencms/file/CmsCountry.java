package com.opencms.file;

/**
 * The class is used for holding result from the table CMS_COUNTRY
 */
public class CmsCountry
{
	private int countryId;
	private String name;
	private String shortName;
	private int priority;	
/**
 * Insert the method's description here.
 * Creation date: (22-09-2000 13:00:25)
 * @param countryId int
 * @param name java.lang.String
 * @param shortName java.lang.String
 * @param priority int
 */
public CmsCountry(int countryId, String name, String shortName, int priority)
{
	this.countryId=countryId;
	this.name=name;
	this.shortName=shortName;
	this.priority=priority;
}
/**
 * Insert the method's description here.
 * Creation date: (22-09-2000 12:59:28)
 * @return int
 */
public int getCountryId() {
	return countryId;
}
/**
 * Insert the method's description here.
 * Creation date: (22-09-2000 12:59:28)
 * @return java.lang.String
 */
public java.lang.String getName() {
	return name;
}
/**
 * Insert the method's description here.
 * Creation date: (22-09-2000 12:59:28)
 * @return int
 */
public int getPriority() {
	return priority;
}
/**
 * Insert the method's description here.
 * Creation date: (22-09-2000 12:59:28)
 * @return java.lang.String
 */
public java.lang.String getShortName() {
	return shortName;
}
/**
 * Insert the method's description here.
 * Creation date: (22-09-2000 12:59:28)
 * @param newCountryId int
 */
public void setCountryId(int newCountryId) {
	countryId = newCountryId;
}
/**
 * Insert the method's description here.
 * Creation date: (22-09-2000 12:59:28)
 * @param newName java.lang.String
 */
public void setName(java.lang.String newName) {
	name = newName;
}
/**
 * Insert the method's description here.
 * Creation date: (22-09-2000 12:59:28)
 * @param newPriority int
 */
public void setPriority(int newPriority) {
	priority = newPriority;
}
/**
 * Insert the method's description here.
 * Creation date: (22-09-2000 12:59:28)
 * @param newShortName java.lang.String
 */
public void setShortName(java.lang.String newShortName) {
	shortName = newShortName;
}
}
