package com.opencms.file;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsSite.java,v $
 * Date   : $Date: 2000/10/03 13:12:26 $
 * Version: $Revision: 1.5 $
 *
 * Copyright (C) 2000  The OpenCms Group 
 * 
 * This File is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For further information about OpenCms, please see the
 * OpenCms Website: http://www.opencms.com
 * 
 * You should have received a copy of the GNU General Public License
 * long with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

import java.sql.*;

/**
 * This Class represents a site, used by the multisite functionality.
 *   
 * @author Anders Fugmann
 * @author Finn Nielsen
 * @version $Revision: 1.5 $ $Date: 2000/10/03 13:12:26 $  
 * 
 */

public class CmsSite
{
	/** The id of the site. Used by the database */
	private int id;
	/** The Name of the site. */
	private String name;
	/** The Description of the site. */
	private String description;
	/** Reference to the category. */

	private int categoryId;
	/** Reference to the language used. */
	private int languageId;

	/** Reference to the country used. */
	private int countryId;

	/** The online project id used with this site. */
	private int onlineProjectId;
	
	/** 
	 * The guestuser of this site.
	 * default to guest-user
	 */
	private String guestUser = com.opencms.core.CmsConstants.C_USER_GUEST;

	/** 
	 * The guestgroup of this site. 
	 * default to guest-user
	 */

	private String guestGroup = com.opencms.core.CmsConstants.C_GROUP_GUEST;
/**
 * Insert the method's description here.
 * Creation date: (09/14/00 13:47:52)
 */
public CmsSite(int site_id, String name, String description, int category_id, int lang_id, int country_id, int onlineProjectId)
{
	this.id = site_id;
	this.name = name;
	this.description = description;
	this.categoryId = category_id;
	this.languageId = lang_id;
	this.countryId = country_id;
	this.onlineProjectId = onlineProjectId;
}
/**
 * Insert the method's description here.
 * Creation date: (09/14/00 13:47:52)
 */
public CmsSite(ResultSet res) throws SQLException
{
	this(res.getInt("SITE_ID"), res.getString("NAME"), res.getString("DESCRIPTION"), res.getInt("CATEGORY_ID"), res.getInt("LANGUAGE_ID"), res.getInt("COUNTRY_ID"), res.getInt("ONLINEPROJECT_ID"));
}
/**
 * Insert the method's description here.
 * Creation date: (08/31/00 %r)
 * @return int
 */
public int getCategoryId() {
	return categoryId;
}
/**
 * Insert the method's description here.
 * Creation date: (08/31/00 %r)
 * @return int
 */
public int getCountryId() {
	return countryId;
}
/**
 * Insert the method's description here.
 * Creation date: (08/31/00 %r)
 * @return java.lang.String
 */
public java.lang.String getDescription() {
	return description;
}
/**
 * Insert the method's description here.
 * Creation date: (08/31/00 %r)
 * @return java.lang.String
 */
public java.lang.String getGuestGroup() {
	return guestGroup;
}
/**
 * Insert the method's description here.
 * Creation date: (08/31/00 %r)
 * @return java.lang.String
 */
public java.lang.String getGuestUser() {
	return guestUser;
}
/**
 * Insert the method's description here.
 * Creation date: (08/31/00 %r)
 * @return int
 */
public int getId() {
	return id;
}
/**
 * Insert the method's description here.
 * Creation date: (08/31/00 %r)
 * @return int
 */
public int getLanguageId() {
	return languageId;
}
/**
 * Insert the method's description here.
 * Creation date: (08/31/00 %r)
 * @return java.lang.String
 */
public java.lang.String getName() {
	return name;
}
/**
 * Insert the method's description here.
 * Creation date: (08/31/00 %r)
 * @return int
 */
public int getOnlineProjectId() {
	return onlineProjectId;
}
/**
 * Insert the method's description here.
 * Creation date: (08/31/00 %r)
 * @param newCategoryId int
 */
public void setCategoryId(int newCategoryId) {
	categoryId = newCategoryId;
}
/**
 * Insert the method's description here.
 * Creation date: (08/31/00 %r)
 * @param newCountryId int
 */
public void setCountryId(int newCountryId) {
	countryId = newCountryId;
}
/**
 * Insert the method's description here.
 * Creation date: (08/31/00 %r)
 * @param newDescription java.lang.String
 */
public void setDescription(java.lang.String newDescription) {
	description = newDescription;
}
/**
 * Insert the method's description here.
 * Creation date: (08/31/00 %r)
 * @param newGuestGroup java.lang.String
 */
public void setGuestGroup(java.lang.String newGuestGroup) {
	guestGroup = newGuestGroup;
}
/**
 * Insert the method's description here.
 * Creation date: (08/31/00 %r)
 * @param newGuestUser java.lang.String
 */
public void setGuestUser(java.lang.String newGuestUser) {
	guestUser = newGuestUser;
}
/**
 * Insert the method's description here.
 * Creation date: (08/31/00 %r)
 * @param newId int
 */
public void setId(int newId) {
	id = newId;
}
/**
 * Insert the method's description here.
 * Creation date: (08/31/00 %r)
 * @param newLanguageId int
 */
public void setLanguageId(int newLanguageId) {
	languageId = newLanguageId;
}
/**
 * Insert the method's description here.
 * Creation date: (08/31/00 %r)
 * @param newName java.lang.String
 */
public void setName(java.lang.String newName) {
	name = newName;
}
/**
 * Insert the method's description here.
 * Creation date: (08/31/00 %r)
 * @param newOnlineProjectId int
 */
public void setOnlineProjectId(int newOnlineProjectId) {
	onlineProjectId = newOnlineProjectId;
}
/**
 * Insert the method's description here.
 * Creation date: (09/18/00 %r)
 * @return java.lang.String
 */
public String toString() {
	return "Site " + name + ", OnlineProject " + onlineProjectId;
}
}
