package com.opencms.file;

/**
 * Insert the type's description here.
 * Creation date: (21-09-2000 16:16:09)
 * @author: Administrator
 */
public class CmsCategory
{
	private int id;
	private String name;
	private String description;
	private String shortName;
	private int priority;
/**
 * CmsCategory constructor comment.
 */
public CmsCategory(int id, String name, String description, String shortName, int priority) 
{
	this.id=id;
	this.name=name;
	this.description=description;
	this.shortName=shortName;
	this.priority=priority;
}
/**
 * Insert the method's description here.
 * Creation date: (21-09-2000 16:20:09)
 * @return java.lang.String
 */
public java.lang.String getDescription() {
	return description;
}
/**
 * Insert the method's description here.
 * Creation date: (21-09-2000 16:20:09)
 * @return int
 */
public int getId() {
	return id;
}
/**
 * Insert the method's description here.
 * Creation date: (21-09-2000 16:20:09)
 * @return java.lang.String
 */
public java.lang.String getName() {
	return name;
}
/**
 * Insert the method's description here.
 * Creation date: (21-09-2000 16:20:09)
 * @return int
 */
public int getPriority() {
	return priority;
}
/**
 * Insert the method's description here.
 * Creation date: (21-09-2000 16:20:09)
 * @return java.lang.String
 */
public java.lang.String getShortName() {
	return shortName;
}
/**
 * Insert the method's description here.
 * Creation date: (21-09-2000 16:20:09)
 * @param newDescription java.lang.String
 */
public void setDescription(java.lang.String newDescription) {
	description = newDescription;
}
/**
 * Insert the method's description here.
 * Creation date: (21-09-2000 16:20:09)
 * @param newId int
 */
public void setId(int newId) {
	id = newId;
}
/**
 * Insert the method's description here.
 * Creation date: (21-09-2000 16:20:09)
 * @param newName java.lang.String
 */
public void setName(java.lang.String newName) {
	name = newName;
}
/**
 * Insert the method's description here.
 * Creation date: (21-09-2000 16:20:09)
 * @param newPriority int
 */
public void setPriority(int newPriority) {
	priority = newPriority;
}
/**
 * Insert the method's description here.
 * Creation date: (21-09-2000 16:20:09)
 * @param newShortName java.lang.String
 */
public void setShortName(java.lang.String newShortName) {
	shortName = newShortName;
}
}
