package com.opencms.workplace;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsSiteMatrix.java,v $
 * Date   : $Date: 2000/09/26 13:09:28 $
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

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;

import java.util.*;

import javax.servlet.http.*;

 /**
 * Template class for displaying a sitematrix
 * Creation date: (09/22/00 13:32:48)
 * @author: Finn Nielsen
 */
public class CmsSiteMatrix extends com.opencms.template.CmsXmlTemplate {
/**
 * Creates a matrix from the categories and sites in the system. This is quite dirty which is why this method is private.
 * Creation date: (09/22/00 13:32:01)
 * @return the dirty result, only to be used internally in this class.
 * @param categories the result of CmsObject.getAllCategories()
 * @param sites the result of CmsObject.getSiteMatrixInfo()
 */
private static Object[] createMatrix(Vector categories, Vector sites)
{
	/* Reminder of how a site hashtable was created
	a.put("siteid", new Integer(res.getInt("SITE_ID")));
	a.put("categoryid", new Integer(res.getInt("CATEGORY_ID")));
	a.put("langid", new Integer(res.getInt("LANGUAGE_ID")));
	a.put("countryid", new Integer(res.getInt("COUNTRY_ID")));
	a.put("langname", res.getString("LANG_NAME"));
	a.put("countryname", res.getString("COUNTRY_NAME"));
	*/
	Hashtable site = null;
	Hashtable category_map = new Hashtable();
	Hashtable country_map = new Hashtable();
	Vector country_names = new Vector();
	String country_key = null;
	int category_place = 0, country_place = 0, country_count = 0;

	// Map categories to rows in the matrix
	for (int i = 0; i < categories.size(); i++)
		category_map.put(new Integer(((CmsCategory) categories.elementAt(i)).getId()), new Integer(i));

	// Map countries and languages to columns in the matrix and find out the width of the matrix
	country_count = 0;
	for (int i = 0; i < sites.size(); i++)
	{
		site = (Hashtable) sites.elementAt(i);
		country_key = site.get("countryid").toString() + "x" + site.get("langid").toString();
		if (!country_map.containsKey(country_key))
		{
			country_place = country_count++;
			country_map.put(country_key, new Integer(country_place));
			country_names.addElement((String)site.get("countryname") + "/" + (String)site.get("langname"));
		}
	}

	// Create and fill out the matrix
	Hashtable matrix[][] = new Hashtable[categories.size()][country_names.size()];
	for (int i = 0; i < sites.size(); i++)
	{
		site = (Hashtable) sites.elementAt(i);
		country_key = site.get("countryid").toString() + "x" + site.get("langid").toString();
		category_place = ((Integer) category_map.get((Integer) site.get("categoryid"))).intValue();
		country_place = ((Integer) country_map.get(country_key)).intValue();
		matrix[category_place][country_place] = site;
	}

	// Collect the resulting data and return them
	Object result[] = { country_names, matrix };
	return result;
}
/**
 * Gets the content of a defined section in a given template file and its subtemplates
 * with the given parameters. 
 * 
 * @see getContent(CmsObject cms, String templateFile, String elementName, Hashtable parameters)
 * @param cms CmsObject Object for accessing system resources.
 * @param templateFile Filename of the template file.
 * @param elementName Element name of this template in our parent template.
 * @param parameters Hashtable with all template class parameters.
 * @param templateSelector template section that should be processed.
 */
public byte[] getContent(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException
{
	CmsXmlTemplateFile xmlTemplateDocument = getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);
	Vector sitelist = cms.getSiteMatrixInfo();
	Vector categories = cms.getAllCategories();
	Object[] matrixinfo = createMatrix(categories, sitelist);
	Vector country_names = (Vector) matrixinfo[0];
	Hashtable[][] matrix = (Hashtable[][]) matrixinfo[1];
	StringBuffer line = null;
	StringBuffer matrixbuffer = null;

	//
	xmlTemplateDocument.setData("domaincount", "" + country_names.size());
	xmlTemplateDocument.setData("categorycount", "" + categories.size());

	//
	line = new StringBuffer();
	for (int i = 0; i < country_names.size(); i++)
	{
		xmlTemplateDocument.setData("domainname", (String) country_names.elementAt(i));
		line.append(xmlTemplateDocument.getProcessedDataValue("domaindot"));
	}
	xmlTemplateDocument.setData("domains", line.toString());
	xmlTemplateDocument.setData("domainlist", xmlTemplateDocument.getProcessedDataValue("domainrow"));

	//
	matrixbuffer = new StringBuffer();
	for (int i = 0; i < categories.size(); i++)
	{
		line = new StringBuffer();
		xmlTemplateDocument.setData("categoryname", ((CmsCategory) categories.elementAt(i)).getName());
		for (int j = 0; j < country_names.size(); j++)
		{
			if (matrix[i][j] != null)
			{
				// set some data for the site in question
				line.append(xmlTemplateDocument.getProcessedDataValue("activedot"));
			}
			else
			{
				line.append(xmlTemplateDocument.getProcessedDataValue("emptydot"));
			}
		}
		xmlTemplateDocument.setData("dots", line.toString());
		matrixbuffer.append(xmlTemplateDocument.getProcessedDataValue((i == 0) ? "firstrow" : "row"));
	}
	xmlTemplateDocument.setData("lines", matrixbuffer.toString());

	//
	return startProcessing(cms, xmlTemplateDocument, elementName, parameters, templateSelector);
}
/**
 * Indicates if the results of this class are cacheable.
 * 
 * @param cms CmsObject Object for accessing system resources
 * @param templateFile Filename of the template file 
 * @param elementName Element name of this template in our parent template.
 * @param parameters Hashtable with all template class parameters.
 * @param templateSelector template section that should be processed.
 * @return <EM>true</EM> if cacheable, <EM>false</EM> otherwise.
 */
public boolean isCacheable(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector)
{
	return false;
}
/**
 * Insert the method's description here.
 * Creation date: (09/25/00 10:49:18)
 * @param cms com.opencms.file.CmsObject
 */
public static void printSiteMatrix(CmsObject cms) throws CmsException
{
	Vector sitelist = cms.getSiteMatrixInfo();
	Vector categories = cms.getAllCategories();
	Object[] matrixinfo = createMatrix(categories, sitelist);
	Vector country_names = (Vector) matrixinfo[0];
	Hashtable[][] matrix = (Hashtable[][]) matrixinfo[1];
	StringBuffer line = null;

	//
	line = new StringBuffer("      ");
	for (int i = 0; i < country_names.size(); i++)
	{
		line.append(((String) country_names.elementAt(i)) + " |");
	}
	System.out.println(line.toString());

	//
	for (int i = 0; i < categories.size(); i++)
	{
		line = new StringBuffer();
		line.append(((CmsCategory) categories.elementAt(i)).getName() + " |");
		for (int j = 0; j < country_names.size(); j++)
		{
			line.append((matrix[i][j] != null) ? " * |" : "   |");
		}
		System.out.println(line.toString());
	}
}
}
