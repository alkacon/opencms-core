/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/Attic/CmsXmlNewsTemplate.java,v $
 * Date   : $Date: 2000/02/15 17:44:00 $
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

package com.opencms.template;

import com.opencms.file.*;
import com.opencms.core.*;
import java.util.*;

import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * Template class for displaying news articles of the content type
 * <CODE>CmsXmlNewsContentDefinition</CODE>.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.5 $ $Date: 2000/02/15 17:44:00 $
 * @see com.opencms.template.CmsXmlNewsContentDefinition
 */
public class CmsXmlNewsTemplate extends CmsXmlTemplate implements I_CmsLogChannels {
    
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
    public boolean isCacheable(A_CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
        return true;
    }
        
    /**
     * Gets the content of a given template file.
     * <P>
     * While processing the template file the user methods
     * <code>newsList</code> and <code>article</code> will be used
     * to display data of the content type "news article".
     * 
     * @param cms A_CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file 
     * @param elementName <em>not used here</em>.
     * @param parameters <em>not used here</em>.
     * @return Unprocessed content of the given template file.
     * @exception CmsException 
     */
    public byte[] getContent(A_CmsObject cms, String templateFile, String elementName, Hashtable parameters) throws CmsException {
        String selector = "";
        
        String read = (String)parameters.get(elementName + ".read");        
        if(read != null && ! "".equals(read)) {
            selector = "read";
        }
            
        byte[] h = getContent(cms, templateFile, elementName, parameters, selector);
        return h;
    }
        
    /**
     * Gets out a list of all available news articles.
     * The news folder is selected by the <code>elem1.newsfolder</code> parameter.<BR>
     * <P>
     * Called by the template file using <code>&lt;METHOD name="newsList"&gt;</code>.
     * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object the initiating XLM document.  
     * @param userObj Hashtable with parameters.
     * @return List of all articles.
     */
    public String newsList(A_CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObj) 
            throws CmsException {
        Hashtable parameters = (Hashtable)userObj;
        String newsFolder = (String)parameters.get("elem1.newsfolder");
        if(newsFolder == null || "".equals(newsFolder)) {
            String errorMessage = "No parameter \"NEWSFOLDER\" defined in " + doc.getAbsoluteFilename();
            if(A_OpenCms.isLogging()) {
                A_OpenCms.log(C_OPENCMS_CRITICAL, "[CmsXmlNewsTemplate] " + errorMessage);
            }            
            throw new CmsException(errorMessage, CmsException.C_XML_TAG_MISSING);
        }
        
        Enumeration en = CmsXmlNewsContentDefinition.getAllArticles(cms, newsFolder);
       String result = "";
        while(en.hasMoreElements()) {
            Object o = en.nextElement();
            CmsXmlNewsContentDefinition doc2 = (CmsXmlNewsContentDefinition)o;
            result = result + "\n    <LI>" + doc2.getNewsHeadline() + "</LI>";
        }                        
        return result;
    }    
 
    /**
     * Gets a selected article with its headline and date.<BR>
     * The article is selected by the <code>elem1.read</code> parameter.<BR>
     * The news folder is selected by the <code>elem1.newsfolder</code> parameter.<BR>
     * <P>
     * Called by the template file using <code>&lt;METHOD name="article"&gt;</code>.
     * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object the initiating XLM document.  
     * @param userObj Hashtable with parameters.
     * @return Selected Article.
     */
    public String article(A_CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObj) 
            throws CmsException {
        
        Hashtable parameters = (Hashtable)userObj;
        String result = null;
        
        String read = (String)parameters.get("elem1.read");
        String folder = (String)parameters.get("elem1.newsfolder");
        if((read != null) && (folder != null) && (! "".equals(read)) && (! "".equals(folder))) {
            CmsXmlNewsContentDefinition doc2 = new CmsXmlNewsContentDefinition(cms, folder + read);
            return getArticleAsHtml(doc2);
        } else {
            throw new CmsException("Cannot read article");
        }
    }        
    
    /**
     * <EM>Not implemented</EM>. Always returns <CODE>false</CODE>.
     * @return <CODE>false</CODE>
     */
    public boolean shouldReload() {
        return false;
    }

    /**
     * Gets the news article as HTML text.
     * @param doc CmsXmlNewsContentDefinition object of the news artice file
     * @return HTML text of the article.
     * @exception CmsException
     */
    protected String getArticleAsHtml(CmsXmlNewsContentDefinition doc) throws CmsException {
        return doc.getNewsHeadline() + "<P>" 
            + doc.getNewsText() + "<P>"
            + doc.getNewsDate() + "<P>";
    }    
}
