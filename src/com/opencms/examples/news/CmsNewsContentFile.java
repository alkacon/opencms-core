/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/examples/news/Attic/CmsNewsContentFile.java,v $
 * Date   : $Date: 2000/05/18 12:23:08 $
 * Version: $Revision: 1.2 $
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

package com.opencms.examples.news;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.template.*;
import com.opencms.util.*;

import java.util.*;
import java.io.*;

import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * Sample content definition for news articles.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.2 $ $Date: 2000/05/18 12:23:08 $
 */
 public class CmsNewsContentFile extends A_CmsXmlContent implements I_CmsNewsConstants {

    /**
     * Default constructor.
     */
    public CmsNewsContentFile() throws CmsException {
        super();
    }
    
    /**
     * Constructor for creating a new object containing the content
     * of the given filename.
     * 
     * @param cms A_CmsObject object for accessing system resources.
     * @param filename Name of the body file that shoul be read.
     */        
    public CmsNewsContentFile(A_CmsObject cms, CmsFile file) throws CmsException {
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
    public CmsNewsContentFile(A_CmsObject cms, String filename) throws CmsException {
        super();            
        init(cms, filename);
    }
    
    /**
     * Gets the expected tagname for the XML documents of this content type
     * @return Expected XML tagname.
     */
    public String getXmlDocumentTagName() {
        return "NEWSARTICLE";
    }

    /**
     * Gets a description of this content type.
     * @return Content type description.
     */
    public String getContentDescription() {
        return "OpenCms news article";
    }
            
    /**
     * Gets the author.
     * @return Author
     */
    public String getNewsAuthor() throws CmsException{
		String parValue = getDataValue(C_NEWS_XML_AUTHOR);
		//Xml-encode string for processing 
		if (parValue != null && !"".equals(parValue)) {
			parValue = Encoder.escapeXml(parValue);
		}
        return parValue;
    }

    /**
     * Set the author.
     * @param author Author
     */
    public void setNewsAuthor(String author) {
        setData(C_NEWS_XML_AUTHOR, author);
    }

    /**
     * Gets the content of the headline.
     * @return Headline
     */
    public String getNewsHeadline() throws CmsException{
        String parValue = getDataValue(C_NEWS_XML_HEADLINE);
		//Xml-encode string for processing 
		if (parValue != null && !"".equals(parValue)) {
			parValue = Encoder.escapeXml(parValue);
		}
        return parValue;
    }

    /**
     * Set the content of the headline.
     * @param headline Headline
     */
    public void setNewsHeadline(String headline) {
        setData(C_NEWS_XML_HEADLINE, headline);
    }
    
    /**
     * Gets the date of the article:
     * @return Date.
     */
    public long getNewsDate() throws CmsException {
        long result = 0;
        String dateText = getDataValue(C_NEWS_XML_DATE);
        
        if(dateText != null && !"".equals(dateText)) {
            String splittetDate[] = Utils.split(dateText, ".");
            GregorianCalendar cal = new GregorianCalendar(Integer.parseInt(splittetDate[2]),
                    Integer.parseInt(splittetDate[1]) - 1,
                    Integer.parseInt(splittetDate[0]), 0, 0, 0);
            result = cal.getTime().getTime();
        }
        return result;
    }

    /**
     * Set the date of the article.
     * @param date long value of the date to be set.
     */
    public void setNewsDate(long date) {
        setData(C_NEWS_XML_DATE, Utils.getNiceShortDate(date));
    }

    /**
     * Set the date of the article.
     * @param date date to be set given as String.
     */
    public void setNewsDate(String date) {
        setData(C_NEWS_XML_DATE, date);
    }
    
    /**
     * Gets the article text of all paragraphs.
     * @param paragraphSeparator String that should be used to separate two paragraphs
     * (e.g. <code>&lt;P&gt</code> for HTML output or <code>/n/n</code> for plain text output).
     * @return Article text.
     */
    public String getNewsText(String paragraphSeparator) throws CmsException {
        Element articleElement = getData(C_NEWS_XML_TEXT);
        StringBuffer result = null;
        NodeList articleChilds = articleElement.getChildNodes();
        int numChilds = articleChilds.getLength();
        
        // Loop through all paragraphs of the article
        for(int i=0; i<numChilds; i++) {
            Node loop = articleChilds.item(i);
            if(loop.getNodeType() == loop.ELEMENT_NODE && loop.getNodeName().toLowerCase().equals("paragraph")) {
                String parValue = Utils.removeLineBreaks(getTagValue((Element)loop));
                //Xml-encode string for processing 
				if (parValue != null && !"".equals(parValue)) {
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
			System.err.println("!!![CmsNewsContentFile:TEXTOUTPUT]!!!: " + result.toString());
            return result.toString();
        }
    }

    /**
     * Set the article text.
     * @param text Article text.
     */
    public void setNewsText(Vector paragraphs) {
        int numElems = paragraphs.size();
        Document parentDoc = getXmlDocument();
        if(numElems == 0) {            
            setData(C_NEWS_XML_TEXT, "");                    
      //  } else if(numElems == 1) {  
      //      setData(C_NEWS_XML_TEXT, (String)paragraphs.elementAt(0)); 
        } else {            
            Element newDatablock = parentDoc.createElement(C_NEWS_XML_TEXT);
            for(int i=0; i< numElems; i++) {
                String temp = (String)paragraphs.elementAt(i);
                if(temp != null && !"".equals(temp)) {
                    Element newParagraph = parentDoc.createElement("paragraph");
                    newParagraph.appendChild(parentDoc.createTextNode(temp));
                    newDatablock.appendChild(newParagraph);
                }
            }
            setData(C_NEWS_XML_TEXT, newDatablock);        
        }            
    }

    /**
     * Gets the article short text.
     * @return Article short text.
     */
    public String getNewsShortText() throws CmsException {
		String parValue = Utils.removeLineBreaks(getDataValue(C_NEWS_XML_SHORTTEXT));
        
		//Xml-encode string for processing 
		if (parValue != null && !"".equals(parValue)) {
			parValue = Encoder.escapeXml(parValue);
		}
		return parValue;
    }

    /**
     * Set the article short text.
     * @param text Article short text.
     */
    public void setNewsShortText(String text) {
        setData(C_NEWS_XML_SHORTTEXT, text);
    }


    /**
     * Gets the external link.
     * @return external link..
     */
    public String getNewsExternalLink() throws CmsException {
        return getDataValue(C_NEWS_XML_EXTLINK);
    }

    /**
     * Set tan external link.
     * @param url URL of the external linkk.
     */
    public void setNewsExternalLink(String url) {
        setData(C_NEWS_XML_EXTLINK, url);
    }

    /**
     * Check if the current news article is marked as <em>active</em>.
     * @return <code>true</code> if the article is active, <code>false</code> otherwise.
     */
    public boolean isNewsActive() {
        return hasData(C_NEWS_XML_ACTIVE);
    }
    
    /**
     * Set the <em>active</em> flag of the news article.
     * @param active Value to be set.
     */
    public void setNewsActive(boolean active) {
        if(active) {
            removeData(C_NEWS_XML_INACTIVE);
            setData(C_NEWS_XML_ACTIVE, "");
        } else {
            removeData(C_NEWS_XML_ACTIVE);
            setData(C_NEWS_XML_INACTIVE, "");
        }        
    }
    
    /**
     * Gets a vaector of all active articles in a folder.
     * @param cms A_CmsObject object for accessing system resources.
     * @param folder Name of the folder to scan for articles.
     * @return Vector of all active articles.
     * @exception CmsException when read access to the articles failed.
     */
    public static Vector getAllArticles(A_CmsObject cms, String folder) throws CmsException {
        return getAllArticles(cms, folder, false);
    }

    /**
     * Gets a vector of all articles in a folder.
     * @param cms A_CmsObject object for accessing system resources.
     * @param folder Name of the folder to scan for articles.
     * @param showInactive Idicates, if all articles (including inactive articles) should be listed
     * @return Vector of all articles.
     * @exception CmsException when read access to the articles failed.
     */
    public static Vector getAllArticles(A_CmsObject cms, String folder, boolean showInactive) throws CmsException {
        Vector allFiles = null;
        
        // Read all files in the given folder
        try {
            allFiles = cms.getFilesInFolder(folder);
        } catch(Exception e) {
            if(A_OpenCms.isLogging()) {
                A_OpenCms.log(C_OPENCMS_CRITICAL,  "[CmsNewsTemplateFile] " + e);
            }
            allFiles = null;
        }
        if(allFiles == null) {
            String errorMessage = "Could not read news article files in folder " + folder;
            if(A_OpenCms.isLogging()) {
                A_OpenCms.log(C_OPENCMS_CRITICAL, "[CmsNewsTemplateFile] " + errorMessage);
            }
            throw new CmsException(errorMessage);
        }
        
        int numFiles = allFiles.size();

        // Scan all files and select only non-deleted files for further processin
        Vector selectedFiles = new Vector();
        for(int i=0; i<numFiles; i++) {
            A_CmsResource fileHeader = (A_CmsResource)allFiles.elementAt(i);                                              
            if(fileHeader.getState() != CmsFile.C_STATE_DELETED) {
                selectedFiles.addElement(fileHeader);
            }
        }
        
        // Sort the files by name
        Vector sortedFiles = Utils.sort(cms, selectedFiles, Utils.C_SORT_NAME_DOWN);
        
        // Loop through the sorted files and create a CmsNewsTemplateFile object
        // for each of them.
        Vector listFiles = new Vector();
        numFiles = sortedFiles.size();
        for(int i=0; i<numFiles; i++) {
            CmsNewsContentFile newsDoc = new CmsNewsContentFile();
            CmsFile fileHeader = (CmsFile)sortedFiles.elementAt(i);
            CmsFile file = cms.readFile(fileHeader.getAbsolutePath());
            newsDoc.init(cms, file);
            if(showInactive || newsDoc.isNewsActive()) {
                listFiles.addElement(newsDoc);
            }
        }
        return listFiles;        
    }    
 }
