/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/examples/news/Attic/CmsNewsTemplate.java,v $
 * Date   : $Date: 2000/03/16 13:42:09 $
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

import javax.servlet.http.*;

/**
 * Template class for displaying news articles of the content type 
 * <CODE>CmsXmlNewsTemplateFile</CODE>.
 * <P>
 * If no parameter is given, the default section of the template file 
 * will be called. This may be used for displaying a list of all available news.
 * <P>
 * If the parameter <code>read</code> is set, the vaule of this parameter
 * will be taken as a filename of a news XML file that will be displayed.
 * In this case the <code>read</code> section of the template file will
 * be used.
 * <P>
 * The parameter <code>newsnum</code> may be used to display a certain
 * actual news. A <code>newsnum</code> value of <code>1</code> will display
 * the most actual news, a value of <code>2</code> will display the
 * article before this one and so on.
 *
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.2 $ $Date: 2000/03/16 13:42:09 $
 * @see com.opencms.examples.CmsXmlNewsTemplateFile
 */
public class CmsNewsTemplate extends CmsXmlTemplate implements I_CmsNewsConstants, I_CmsLogChannels {
    
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
     * @param templateSelector template section that should be processed.
     * @return Processed content of the given template file.
     * @exception CmsException 
     */
     public byte[] getContent(A_CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException {
        String read = (String)parameters.get(elementName + ".read");     
        String newsNum = (String)parameters.get(elementName + ".newsnum");        

        String newsFolder = getNewsFolder(elementName, parameters);
        CmsNewsTemplateFile newsDoc = null;
        
        if(read != null && ! "".equals(read)) {
            // The read parameter was set. So we have a certain article selected
            templateSelector = "read";
            newsDoc = new CmsNewsTemplateFile(cms, newsFolder + read);                       
        } else if(newsNum != null && ! "".equals(newsNum)){
            // The newsNum parameter was set.
            // So we have To get a list of all articles and search
            // for the requested number, beginning with the latest article
            templateSelector = "read";
            Vector v = CmsNewsTemplateFile.getAllArticles(cms, newsFolder);
            int articleNum = new Integer(newsNum).intValue();
            if(articleNum < v.size()) {
                newsDoc = (CmsNewsTemplateFile)v.elementAt(articleNum);
            }
        }
        
        // Add the article to the parameters for making it accessible for
        // the user methods.
        if(newsDoc != null) {
            parameters.put("_ARTICLE_", newsDoc);
        }
        
        CmsXmlTemplateFile xmlTemplateDocument = getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);
        return startProcessing(cms, xmlTemplateDocument, elementName, parameters, templateSelector);
    }
        
    /**
     * Gets a part of the currently selected article.
     * Which part can be selected by the tagcontent variable.
     * <P>
     * Called by the template file using 
     * <code>&lt;METHOD name="article"&gt;part&lt;METHOD&gt;</code>.
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
     * </ul>
     * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object the initiating XLM document.  
     * @param userObj Hashtable with parameters.
     * @return List of all articles.
     */
    public String article(A_CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObj) 
            throws CmsException {

        A_CmsRequestContext reqCont = cms.getRequestContext();
        HttpServletRequest orgReq = (HttpServletRequest)reqCont.getRequest().getOriginalRequest();
        String servletPath = orgReq.getServletPath();

        CmsNewsTemplateFile article = (CmsNewsTemplateFile)((Hashtable)userObj).get("_ARTICLE_");
        String result = null;
        
        if(article != null) {
            if(tagcontent.toLowerCase().equals("externallink")) {
                result = article.getNewsExternalLink();
            } else if(tagcontent.toLowerCase().equals("date")) {
                result = Utils.getNiceShortDate(article.getNewsDate());
            } else if(tagcontent.toLowerCase().equals("author")) {
                result = article.getNewsAuthor();
            } else if(tagcontent.toLowerCase().equals("headline")) {
                result = article.getNewsHeadline();
            } else if(tagcontent.toLowerCase().equals("shorttext")) {
                result = article.getNewsShortText();
            } else if(tagcontent.toLowerCase().equals("text")) {
                result = article.getNewsText();
            } else if(tagcontent.toLowerCase().equals("file")) {
                result = servletPath + C_NEWS_FOLDER_PAGE + article.getFilename() + "/index.html";
            }
        }        
        return result;
    }
        
    /**
     * Prints out a list of all available news articles.
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
        String elementName = (String)parameters.get("_ELEMENT_");
        
        String newsFolder = getNewsFolder(elementName, parameters);
        Vector v = CmsNewsTemplateFile.getAllArticles(cms, newsFolder);
        String result = "";
        CmsNewsListDefFile listDef = new CmsNewsListDefFile(cms, C_PATH_INTERNAL_TEMPLATES + C_NEWS_NEWSLISTDEF);
        
        for(int i=0; i<v.size(); i++) {
            Object o = v.elementAt(i);
            CmsNewsTemplateFile doc2 = (CmsNewsTemplateFile)o;
            result = result + listDef.getNewsListEntry(Utils.getNiceShortDate(doc2.getNewsDate()), 
                                                       doc2.getNewsHeadline(), 
                                                       doc2.getNewsShortText(),
                                                       doc2.getFilename() + "/index.html");
        }                        
        return result;
    }    
    
    /**
     * Get the newsfolder.
     * The value will be taken from the user parameters or from the news constants
     * @param elementName Name of the current subtemplate
     * @param parameters User parameters
     * @return Name of the news folder
     */
    private String getNewsFolder(String elementName, Hashtable parameters) {
        String newsFolder = (String)parameters.get(elementName + ".newsfolder");

        if(newsFolder == null || "".equals(newsFolder)) {
            newsFolder = C_NEWS_FOLDER_CONTENT; 
        }
        return newsFolder;        
    }
}
