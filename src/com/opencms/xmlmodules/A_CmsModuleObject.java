package com.opencms.xmlmodules;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/xmlmodules/Attic/A_CmsModuleObject.java,v $
 * Date   : $Date: 2000/08/08 14:08:34 $
 * Version: $Revision: 1.3 $
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
import com.opencms.template.*;
import com.opencms.util.*;

import java.util.*;
import java.io.*;

import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * Content definition for articles in all XML-modules.
 * 
 * @author Matthias Schreiber
 * @version $Revision: 1.3 $ $Date: 2000/08/08 14:08:34 $
 */
 public abstract class A_CmsModuleObject extends A_CmsXmlContent implements I_CmsModuleConstants, I_CmsXmlContent {
	
 
	/**
	 * Default constructor.
	 */
	public A_CmsModuleObject() throws CmsException {
		super();
	}
	/**
	 * Constructor for creating a new object containing the content
	 * of the given filename.
	 * 
	 * @param cms A_CmsObject object for accessing system resources.
	 * @param filename Name of the body file that shoul be read.
	 */        
	public A_CmsModuleObject(CmsObject cms, CmsFile file) throws CmsException {
		super();
		init(cms, file);
	}
	/**
	 * Constructor for creating a new object containing the content
	 * of the given filename.
	 * 
	 * @param cms A_CmsObject object for accessing system resources.
	 * @param filename Name of the body file that shoul be read.
	 */        
	public A_CmsModuleObject(CmsObject cms, String filename) throws CmsException {
		super();            
		init(cms, filename);
	}
	 /**
	 * Set the <em>active</em> flag of the article.
	 * @param active Value to be set.
	 */
	protected String extractText(Element elem, String paragraphSeparator, boolean escape) throws CmsException {
		StringBuffer result = null;
		NodeList articleChilds = elem.getChildNodes();
		int numChilds = articleChilds.getLength();
		
		// Loop through all paragraphs of the article
		for(int i=0; i<numChilds; i++) {
			Node loop = articleChilds.item(i);
			if(loop.getNodeType() == loop.ELEMENT_NODE && loop.getNodeName().toLowerCase().equals("paragraph")) {
				String parValue = Utils.removeLineBreaks(getTagValue((Element)loop));
				//Xml-encode string for processing 
				if (parValue != null && !"".equals(parValue) && escape==true) {
					parValue = Encoder.escapeXml(parValue);
				}
				if(result == null) {
					result = new StringBuffer(parValue);
				} else {
					result.append(paragraphSeparator);
					result.append(parValue);
				}                                  
			}
		}
		if(result == null) {
			return "";
		} else {
			return result.toString();
		}
	}
	/**
	 * Gets the author.
	 * @return Author
	 */
	public String getAuthor() throws CmsException{
		String parValue = getDataValue(C_XML_AUTHOR);
		return parValue;
	}
	/**
	 * Gets a description of this content type.
	 * @return Content type description.
	 */
	public abstract String getContentDescription();
	/**
	 * Gets the date of the article:
	 * @return Date.
	 */
	public long getDate() throws CmsException {
		String dateText = getDataValue(C_XML_DATE);
		return Utils.splitDate(dateText);
	}
	/**
	 * Gets the date of the article:
	 * @return Date.
	 */
	public String getDateString() throws CmsException {
		String dateText = getDataValue(C_XML_DATE);
		long date = Utils.splitDate(dateText);
		return Utils.getNiceShortDate(date);
			
	}
	/**
	 * Gets the content of the headline.
	 * @return Headline
	 */
	public String getHeadline() throws CmsException{
		String parValue = getDataValue(C_XML_HEADLINE);
		return parValue;
	}
	 /**
	 * Gets the article text of all paragraphs.
	 * @param paragraphSeparator String that should be used to separate two paragraphs
	 * @param escape Boolean value has to be set 'true' when it's necessary to escape the text
	 * (e.g. <code>&lt;P&gt</code> for HTML output or <code>/n/n</code> for plain text output).
	 * @return Article text.
	 */
	public String getText() throws CmsException {
		String parValue = getDataValue(C_XML_TEXT);
		return parValue;
	}
	/**
	 * Gets the article text of all paragraphs.
	 * @param paragraphSeparator String that should be used to separate two paragraphs
	 * @param escape Boolean value has to be set 'true' when it's necessary to escape the text
	 * (e.g. <code>&lt;P&gt</code> for HTML output or <code>/n/n</code> for plain text output).
	 * @return Article text.
	 */
	public String getText(String paragraphSeparator, boolean escape) throws CmsException {
		Element articleElement = getData(C_XML_TEXT);
		return extractText(articleElement, paragraphSeparator, escape);
	}
	/**
	 * Gets the expected tagname for the XML documents of this content type
	 * @return Expected XML tagname.
	 */
	public abstract String getXmlDocumentTagName();
	/**
	 * Check if the current article is marked as <em>active</em>.
	 * @return <code>true</code> if the article is active, <code>false</code> otherwise.
	 */
	public boolean isActive() {
		return hasData(C_STATE_ACTIVE);
	}
	/**
	 * Set the <em>active</em> flag of the article.
	 * @param active Value to be set.
	 */
	public void setActive(boolean active) {
		if(active) {
			removeData(C_STATE_INACTIVE);
			setData(C_STATE_ACTIVE, "");
		} else {
			removeData(C_STATE_ACTIVE);
			setData(C_STATE_INACTIVE, "");
		}        
	}
	/**
	 * Set the author.
	 * @param author Author
	 */
	public void setAuthor(String author) {
		setData(C_XML_AUTHOR, author);
	}
	/**
	 * Set the date of the article.
	 * @param date long value of the date to be set.
	 */
	public void setDate(long date) {
		setData(C_XML_DATE, Utils.getNiceShortDate(date));
	}
	/**
	 * Set the date of the article.
	 * @param date date to be set given as String.
	 */
	public void setDate(String date) {
		setData(C_XML_DATE, date);
	}
	/**
	 * Set the content of the headline.
	 * @param headline Headline
	 */
	public void setHeadline(String headline) {
		setData(C_XML_HEADLINE, headline);
	}
	/**
	 * Set the article text.
	 * @param text Article text.
	 */
	public void setText(Vector paragraphs) {
		int numElems = paragraphs.size();
		Document parentDoc = getXmlDocument();
		if(numElems == 0) {            
			setData(C_XML_TEXT, "");                    
		} 
		else {            
			Element newDatablock = parentDoc.createElement(C_XML_TEXT);
			for(int i=0; i< numElems; i++) {
				String temp = (String)paragraphs.elementAt(i);
				if(temp != null && !"".equals(temp)) {
					Element newParagraph = parentDoc.createElement("paragraph");
					newParagraph.appendChild(parentDoc.createTextNode(temp));
					newDatablock.appendChild(newParagraph);
				}
			}
			setData(C_XML_TEXT, newDatablock);        
		}            
	}
}  
