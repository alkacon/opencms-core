/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/xmlmodules/Attic/A_CmsModuleContent.java,v $
 * Date   : $Date: 2000/07/11 15:01:18 $
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

package com.opencms.xmlmodules;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.template.*;
import com.opencms.util.*;

import java.util.*;

import javax.servlet.http.*;

/**
 * Template class for displaying articles of a certain content type, 
 * e.g. <CODE>CmsNewsContent</CODE>.
 * <P>
 * If no parameter is given, the default section of the template file 
 * will be called. This may be used for displaying a list of all available news.
 * <P>
 * If the parameter <code>read</code> is set, the vaule of this parameter
 * will be taken as a filename of a news XML file that will be displayed.
 * In this case the <code>read</code> section of the template file will
 * be used.
 * <P>
 * The parameter <code>number</code> may be used to display a certain
 * actual news. A <code>number</code> value of <code>1</code> will display
 * the most actual news, a value of <code>2</code> will display the
 * article before this one and so on.
 *
 * 
 * @author Matthias Schreiber
 * @version $Revision: 1.2 $ $Date: 2000/07/11 15:01:18 $
 * @see com.opencms.xmlmodules.news.CmsNewsContent
 */
public abstract class A_CmsModuleContent extends CmsXmlTemplate implements I_CmsModuleConstants, I_CmsLogChannels {
    
	
    /**
     * Indicates if the results of this class are cacheable.
     * 
     * @param cms A_CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file 
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return <EM>true</EM> if cacheable, <EM>false</EM> otherwise.
     */
    public boolean isCacheable(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
        return true;
    }
        
    /**
     * Gets the content of a given template file.
     * <P>
     * While processing the template file the user methods
     * <code>getArticleList</code> and <code>getTagContent</code> will be used
     * to display data.
     * 
     * @param cms A_CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file 
     * @param elementName <em>not used here</em>.
     * @param parameters <em>not used here</em>.
     * @param templateSelector template section that should be processed.
     * @return Processed content of the given template file.
     * @exception CmsException 
     */
     public abstract byte[] getContent(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector)
			throws CmsException;
        
    /**
     * Gets a part of the currently selected article.
     * Which part can be selected by the tagcontent variable.
     * <P>
     * Called by the template file using 
     * <code>&lt;METHOD name="getTagContent"&gt;part&lt;METHOD&gt;</code>.
     * <P>
     * <code>part</code> may be replaced by:
     * <ul>
     * <li><code>externallink</code></li>
     * <li><code>date</code></li>
     * <li><code>author</code></li>
     * <li><code>headline</code></li>
     * <li><code>shorttext</code></li>
     * <li><code>text</code></li>
     * <li><code>file</code></li>
     * <li><code>index</code></li>
     * </ul>
     * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object the initiating XML document.  
     * @param userObj Hashtable with parameters.
     * @return Content of requested XML-tag.
     */
	 
    public abstract String getTagContent(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObj) 
            throws CmsException;
		
     /**
     * Prints out a list of all available articles.
     * <P>
     * Called by the template file using <code>&lt;METHOD name="getArticleList"&gt;</code>.
     * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object the initiating XML document.  
     * @param userObj Hashtable with parameters.
     * @return List of all articles.
     */
    public String getArticleList(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObj) 
            throws CmsException {
        CmsRequestContext reqCont = cms.getRequestContext();
        HttpServletRequest orgReq = (HttpServletRequest)reqCont.getRequest().getOriginalRequest();
        String servletPath = orgReq.getServletPath();

        Hashtable parameters = (Hashtable)userObj;
        String elementName = (String)parameters.get("_ELEMENT_");
        CmsXmlTemplateFile templateFile = (CmsXmlTemplateFile)doc;
        
        // Get a vector of all available articles using the content definition
        String folder = getFolder(elementName, parameters);
        Vector v = getAllArticles(cms,folder);
	
        // set the application specific data for default clients (HTML-browsers)      
        return getListData(templateFile,v,servletPath);
    }
	
	/**
     * Gets application-specific article list-data to display
     * on default clients.
     * @param template Template file to access
     * @param servletPath Path to the OpenCms servlet zone
     * @param v Vector that contains all articles
	 * @return List of all articles.
     */
	protected abstract String getListData(CmsXmlTemplateFile template, Vector v, String servletPath) 
            throws CmsException;
	
	/**
     * Gets application-specific article-list data to display 
     * on wap-enabled clients.
     * @param template Template file to access
     * @param servletPath Path to the OpenCms servlet zone
     * @param v Vector that contains all articles
	 * @return List of all articles.
     */
	protected abstract String getWapData(CmsXmlTemplateFile template, Vector v, int selection, boolean isNavDeck) 
            throws CmsException;
    
    /**
     * Get the newsfolder.
     * The value will be taken from the user parameters or from the news constants
     * @param elementName Name of the current subtemplate
     * @param parameters User parameters
     * @return Name of the news folder
     */
    protected abstract String getFolder(String elementName, Hashtable parameters);
	
	/**
     * Get the maximum number of articles to be displayed on a navigation deck.
     * The value will be taken from the user parameters or from the news constants.
     * @param elementName Name of the current subtemplate
     * @param parameters User parameters
     * @return Maximum number of articles
     */
	protected String getMaxLinks(String elementName, Hashtable parameters) {
        String maxLinks = (String)parameters.get(elementName + ".max");

		if(maxLinks == null || "".equals(maxLinks)) {
            maxLinks = C_WAP_LINKSPERDECK; 
        }
        return maxLinks;        
    }
	
	/**
     * Get the index of requested article. It's specified by the URL parameter <code>article</code>.
     * The value will be taken from the user parameters.
     * @param elementName Name of the current subtemplate
	 * @param parameters User parameters
     * @return article index
     */
	protected String getSelectedArticle(String elementName, Hashtable parameters) {
		String selection = (String)parameters.get(elementName + ".article");
        
		return selection;       
    }
	
	/**
     * Get the index of requested navigation deck. It's specified by the URL parameter <code>nav</code>.
     * The value will be taken from the user parameters or from the news constants.
     * @param elementName Name of the current subtemplate
	 * @param parameters User parameters
     * @return navigation index
     */
	protected String getNavDeck(String elementName, Hashtable parameters) {
		String navDeck = (String)parameters.get(elementName + ".nav");

		if(navDeck == null || "".equals(navDeck)) {
            navDeck = "0"; 
        }
        return navDeck;        
    }
	
	/**
     * Prints a WML document title (parameter or constant).
     * <P>
     * Called by the template file using <code>&lt;METHOD name="getWapTitle"&gt;</code>.
     * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object the initiating XLM document.  
     * @param userObj Hashtable with parameters.
     * @return String with document title.
     */
	public String getWapTitle(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObj) 
            throws CmsException {
		Hashtable parameters = (Hashtable)userObj;
        String elementName = (String)parameters.get("_ELEMENT_");
		String wmlDeckTitle = (String)parameters.get(elementName + ".title");

		if(wmlDeckTitle == null || "".equals(wmlDeckTitle)) {
            wmlDeckTitle = C_WAP_DECKTITLE; 
        }
        return wmlDeckTitle;        
    }
		
	/**
     * Creates a WML deck which either holds navigation or a certain article.
     * <P>
     * Called by the template file using <code>&lt;METHOD name="getWapContent"&gt;</code>.
     * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object the initiating XLM document.  
     * @param userObj Hashtable with parameters.
     * @return WML code (navigation or article).
     */
    public String getWapContent(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObj) 
            throws CmsException {
        Hashtable parameters = (Hashtable)userObj;
        String elementName = (String)parameters.get("_ELEMENT_");
		String result = "";
        CmsXmlTemplateFile templateFile = (CmsXmlTemplateFile)doc;
        
        // Get a vector of all available articles using the content definition
        String folder = getFolder(elementName, parameters);
        Vector v = getAllArticles(cms, folder);
								
		String maxArticles = getMaxLinks(elementName, parameters); 
		int max = new Integer(maxArticles).intValue();
		String navDeck = getNavDeck(elementName, parameters); 
		int nav = new Integer(navDeck).intValue();
		String selectedArticle = getSelectedArticle(elementName, parameters);
		
		 if(selectedArticle == null || "".equals(selectedArticle)) {
			// There's no URL parameter <code>article</code> which defines a certain article to be displayed, 
			// so a navigation deck is created. 
			
			// Loop through the vector for all articles and generate navigation, number of links depends on user parameter "max"
			for(int i=(nav)*max; i<((nav+1)*max); i++) {
				if (i<v.size()) {
					result = result + getWapData(templateFile,v,i,true);
				}
			}
			// More articles in the archive than user parameter <code>max<code> allows to be displayed 
			// on one navigation deck, then insert a link to the next navigation deck.
			nav+=1;
			if (nav*max < v.size()) {
				String linktext;
				templateFile.setData("selection", "?body.nav=" + nav);
				try {
					linktext = templateFile.getDataValue(C_TAG_WMLLINKLABEL);
				} catch(CmsException e) { 
					linktext = C_DEF_WMLLINKLABEL;
				}
				templateFile.setData("headline", linktext);
				result = result + templateFile.getProcessedDataValue(C_TAG_WMLNAV);
			}
		}
		else { 
			// Article deck requested, selected article is specified by URL parameter <code>article</code>
			int article = new Integer(selectedArticle).intValue();
			
			if (article < v.size()) {
				result = result + getWapData(templateFile,v,article,false);
			}
		}
		return result;
	}
	
	/**
     * Get a vector of CmsNewsObjects with one entry for every article.
     * @param cms A_CmsObject object for accessing system resources.
     * @param sortedFiles Vector with all files availables, sorted by date
     * @param showInactive Indicates, whether inactive articles should be added to result
	 * @return List of articles available.
     */
	protected abstract Vector getFileObjects(CmsObject cms, Vector sortedFiles, boolean showInactive) 
            throws CmsException;
	
	/**
     * Gets a vector of all active articles in a folder.
     * @param cms A_CmsObject object for accessing system resources.
     * @param folder Name of the folder to scan for articles.
     * @return Vector of all active articles.
     * @exception CmsException when read access to the articles failed.
     */
    public Vector getAllArticles(CmsObject cms, String folder) throws CmsException {
        return getAllArticles(cms, folder, false);
    }
	
	/**
     * Gets a vector of all articles in a folder.
     * @param cms A_CmsObject object for accessing system resources.
     * @param folder Name of the folder to scan for articles.
     * @param showInactive Indicates, whether inactive articles should be added to result
     * @return Vector of all articles available.
     * @exception CmsException when read access to the articles failed.
     */
    public Vector getAllArticles(CmsObject cms, String folder, boolean showInactive) throws CmsException {
        Vector allFiles = null;
        
        // Read all files in the given folder
        try {
            allFiles = cms.getFilesInFolder(folder);
        } catch(Exception e) {
            if(A_OpenCms.isLogging()) {
                A_OpenCms.log(C_OPENCMS_CRITICAL,  "[CmsNewsContent] " + e);
            }
            allFiles = null;
        }
        if(allFiles == null) {
            String errorMessage = "Could not read the article files in folder " + folder;
            if(A_OpenCms.isLogging()) {
                A_OpenCms.log(C_OPENCMS_CRITICAL, "[CmsNewsContent] " + errorMessage);
            }
            throw new CmsException(errorMessage);
        }
        
        int numFiles = allFiles.size();

        // Scan all files and select only non-deleted files for further processing
        Vector selectedFiles = new Vector();
        for(int i=0; i<numFiles; i++) {
            CmsResource fileHeader = (CmsResource)allFiles.elementAt(i);                                              
            if(fileHeader.getState() != CmsFile.C_STATE_DELETED) {
                selectedFiles.addElement(fileHeader);
            }
        }
        // Sort the files by name
        Vector sortedFiles = Utils.sort(cms, selectedFiles, Utils.C_SORT_NAME_DOWN);
        
		return getFileObjects(cms,sortedFiles,showInactive);
    }    
	
}
