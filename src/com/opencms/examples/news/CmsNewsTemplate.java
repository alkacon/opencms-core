
/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/examples/news/Attic/CmsNewsTemplate.java,v $
* Date   : $Date: 2001/01/24 09:42:14 $
* Version: $Revision: 1.17 $
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
 * @deprecated Classes in com.opencms.examples.news are deprecated since
 *	           there is a more generic solution in com.opencms.xmlmodules.news.
 *             Some changes will be necessary in coding the templates which work 
 *             with the newer classes.  
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.17 $ $Date: 2001/01/24 09:42:14 $
 * @see com.opencms.examples.CmsXmlNewsTemplateFile
 */
public class CmsNewsTemplate extends CmsXmlTemplate implements I_CmsNewsConstants,I_CmsLogChannels {
    
    /** XML tag used for the paragraph separator definition */
    private final static String C_TAG_PARAGRAPHSEP = "paragraphsep";
    
    /** XML tag used for the news list entry definition */
    private final static String C_TAG_NEWSLISTENTRY = "newslistentry";
    
    /** XML tag used for the wml navigation definition */
    private final static String C_TAG_WMLNAV = "wmlnav";
    
    /** XML tag used for the wml content definition */
    private final static String C_TAG_WMLARTICLE = "wmlarticle";
    
    /** XML tag used for the wml content definition */
    private final static String C_TAG_WMLLINKLABEL = "linklabel";
    
    /** XML tag used for the wml content definition */
    public final static String C_WMLLINKLABEL_DEF = "mehr";
    
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
     * <li><code>index</code></li>
     * </ul>
     * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object the initiating XML document.  
     * @param userObj Hashtable with parameters.
     * @return List of all articles.
     */
    public String article(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObj) throws CmsException {
        CmsRequestContext reqCont = cms.getRequestContext();
        HttpServletRequest orgReq = (HttpServletRequest)reqCont.getRequest().getOriginalRequest();
        String servletPath = orgReq.getServletPath();
        Hashtable parameters = (Hashtable)userObj;
        CmsXmlTemplateFile templateFile = (CmsXmlTemplateFile)doc;
        CmsNewsContentFile article = (CmsNewsContentFile)parameters.get("_ARTICLE_");
        String elementName = (String)parameters.get("_ELEMENT_");
        String result = null;
        if(article != null) {
            if(tagcontent.toLowerCase().equals("externallink")) {
                result = article.getNewsExternalLink();
            }
            else {
                if(tagcontent.toLowerCase().equals("date")) {
                    long newsDate = article.getNewsDate();
                    if(newsDate != 0) {
                        result = Utils.getNiceShortDate(article.getNewsDate());
                    }
                }
                else {
                    if(tagcontent.toLowerCase().equals("author")) {
                        result = Encoder.escapeXml(article.getNewsAuthor());
                    }
                    else {
                        if(tagcontent.toLowerCase().equals("headline")) {
                            result = Encoder.escapeXml(article.getNewsHeadline());
                        }
                        else {
                            if(tagcontent.toLowerCase().equals("shorttext")) {
                                result = Encoder.escapeXml(article.getNewsShortText());
                            }
                            else {
                                if(tagcontent.toLowerCase().equals("text")) {
                                    result = article.getNewsText(templateFile.getDataValue(C_TAG_PARAGRAPHSEP), true);
                                }
                                else {
                                    if(tagcontent.toLowerCase().equals("file")) {
                                        result = servletPath + C_NEWS_FOLDER_PAGE + article.getFilename() + "/index.html";
                                    }
                                    else {
                                        if(tagcontent.toLowerCase().equals("linktext")) {
                                            result = "Artikel lesen";
                                        }
                                        else {
                                            if(tagcontent.toLowerCase().equals("path")) {
                                                result = servletPath;
                                            }
                                            else {
                                                if(tagcontent.toLowerCase().equals("index")) {
                                                    
                                                    // gets the index of currently selected article
                                                    String newsFolder = getNewsFolder(elementName, parameters);
                                                    Vector v = CmsNewsContentFile.getAllArticles(cms, newsFolder);
                                                    for(int i = 0;i < v.size();i++) {
                                                        Object o = v.elementAt(i);
                                                        CmsNewsContentFile doc2 = (CmsNewsContentFile)o;
                                                        if(doc2.getAbsoluteFilename().equals(article.getAbsoluteFilename())) {
                                                            result = "" + i;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
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
    public byte[] getContent(CmsObject cms, String templateFile, String elementName, Hashtable parameters, 
            String templateSelector) throws CmsException {
        String read = (String)parameters.get(elementName + ".read");
        String newsNum = (String)parameters.get(elementName + ".newsnum");
        String newsFolder = getNewsFolder(elementName, parameters);
        CmsNewsContentFile newsDoc = null;
        if(read != null && !"".equals(read)) {
            
            // The read parameter was set. So we have a certain article selected
            templateSelector = "read";
            newsDoc = new CmsNewsContentFile(cms, newsFolder + read);
        }
        else {
            if(newsNum != null && !"".equals(newsNum)) {
                
                // The newsNum parameter was set.                
                // So we have To get a list of all articles and search                
                // for the requested number, beginning with the latest article
                templateSelector = "read";
                Vector v = CmsNewsContentFile.getAllArticles(cms, newsFolder);
                int articleNum = new Integer(newsNum).intValue();
                if(articleNum < v.size()) {
                    newsDoc = (CmsNewsContentFile)v.elementAt(articleNum);
                }
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
     * Get the maximum number of articles to be displayed on a navigation deck.
     * The value will be taken from the user parameters or from the news constants
     * @param elementName Name of the current subtemplate
     * @param parameters User parameters
     * @return Maximum number of articles
     */
    private String getMaxArticles(String elementName, Hashtable parameters) {
        String maxArticles = (String)parameters.get(elementName + ".max");
        if(maxArticles == null || "".equals(maxArticles)) {
            maxArticles = C_NEWS_WML_MAXARTICLES;
        }
        return maxArticles;
    }
    
    /**
     * Get the index of requested navigation deck. It's specified by the URL parameter <code>nav</code>.
     * The value will be taken from the user parameters or from the news constants.
     * @param parameters User parameters
     * @return navigation index
     */
    private String getNavDeck(Hashtable parameters) {
        String navDeck = (String)parameters.get("nav");
        if(navDeck == null || "".equals(navDeck)) {
            navDeck = C_NEWS_WML_FIRSTNAVDECK;
        }
        return navDeck;
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
    
    /**
     * Get the index of requested article. It's specified by the URL parameter <code>article</code>.
     * The value will be taken from the user parameters or from the news constants.
     * @param parameters User parameters
     * @return article index
     */
    private String getSelectedArticle(Hashtable parameters) {
        String selectedArticle = (String)parameters.get("article");
        return selectedArticle;
    }
    
    /**
     * Prints a WML document title (parameter or constant).
     * <P>
     * Called by the template file using <code>&lt;METHOD name="getWmlTitle"&gt;</code>.
     * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object the initiating XLM document.  
     * @param userObj Hashtable with parameters.
     * @return document title.
     */
    public String getWmlTitle(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObj) throws CmsException {
        Hashtable parameters = (Hashtable)userObj;
        String elementName = (String)parameters.get("_ELEMENT_");
        String wmlDeckTitle = (String)parameters.get(elementName + ".title");
        if(wmlDeckTitle == null || "".equals(wmlDeckTitle)) {
            wmlDeckTitle = C_NEWS_WML_DECKTITLE;
        }
        return wmlDeckTitle;
    }
    
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
    public String newsList(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObj) throws CmsException {
        CmsRequestContext reqCont = cms.getRequestContext();
        HttpServletRequest orgReq = (HttpServletRequest)reqCont.getRequest().getOriginalRequest();
        String servletPath = orgReq.getServletPath();
        Hashtable parameters = (Hashtable)userObj;
        String elementName = (String)parameters.get("_ELEMENT_");
        CmsXmlTemplateFile newsTemplateFile = (CmsXmlTemplateFile)doc;
        
        // Get a vector of all available articles using the news content definition
        String newsFolder = getNewsFolder(elementName, parameters);
        Vector v = CmsNewsContentFile.getAllArticles(cms, newsFolder);
        String result = "";
        
        // Loop through the vector for all articles and generate output for each of them
        for(int i = 0;i < v.size();i++) {
            Object o = v.elementAt(i);
            CmsNewsContentFile doc2 = (CmsNewsContentFile)o;
            newsTemplateFile.setData("date", Utils.getNiceShortDate(doc2.getNewsDate()));
            newsTemplateFile.setData("headline", Encoder.escapeXml(doc2.getNewsHeadline()));
            newsTemplateFile.setData("author", Encoder.escapeXml(doc2.getNewsAuthor()));
            newsTemplateFile.setData("shorttext", Encoder.escapeXml(doc2.getNewsShortText()));
            newsTemplateFile.setData("link", servletPath + C_NEWS_FOLDER_PAGE + doc2.getFilename() + "/index.html");
            newsTemplateFile.setData("path", servletPath);
            newsTemplateFile.setData("index", "" + i);
            result = result + newsTemplateFile.getProcessedDataValue(C_TAG_NEWSLISTENTRY, this, userObj);
        }
        return result;
    }
    
    /**
     * Creates a WML deck which either holds navigation or a certain news article.
     * <P>
     * Called by the template file using <code>&lt;METHOD name="newsWmlFactory"&gt;</code>.
     * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object the initiating XLM document.  
     * @param userObj Hashtable with parameters.
     * @return WML code (navigation or articles).
     */
    public String newsWmlFactory(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObj) throws CmsException {
        Hashtable parameters = (Hashtable)userObj;
        String elementName = (String)parameters.get("_ELEMENT_");
        String result = "";
        CmsXmlTemplateFile newsTemplateFile = (CmsXmlTemplateFile)doc;
        
        // Get a vector of all available articles using the news content definition
        String newsFolder = getNewsFolder(elementName, parameters);
        Vector v = CmsNewsContentFile.getAllArticles(cms, newsFolder);
        String maxArticles = getMaxArticles(elementName, parameters);
        int max = new Integer(maxArticles).intValue();
        String navDeck = getNavDeck(parameters);
        int nav = new Integer(navDeck).intValue();
        String selectedArticle = getSelectedArticle(parameters);
        if(selectedArticle == null || "".equals(selectedArticle)) {
            
            // There's no URL parameter <code>article</code> which defines a certain article to be displayed,             
            // so a navigation deck is created.             
            // Loop through the vector for all articles and generate navigation, number of links depends on user parameter "max"
            for(int i = (nav) * max;i < ((nav + 1) * max);i++) {
                if(i < v.size()) {
                    Object o = v.elementAt(i);
                    CmsNewsContentFile doc2 = (CmsNewsContentFile)o;
                    newsTemplateFile.setData("selection", "?article=" + i);
                    newsTemplateFile.setData("headline", Encoder.escapeXml(doc2.getNewsHeadline()));
                    result = result + newsTemplateFile.getProcessedDataValue(C_TAG_WMLNAV);
                }
            }
            
            // More articles in the archive than user parameter <code>max<code> allows to be displayed             
            // on one navigation deck, then insert a link to the next navigation deck.
            nav += 1;
            if(nav * max < v.size()) {
                String linktext;
                newsTemplateFile.setData("selection", "?nav=" + nav);
                try {
                    linktext = newsTemplateFile.getDataValue(C_TAG_WMLLINKLABEL);
                }
                catch(CmsException e) {
                    linktext = C_WMLLINKLABEL_DEF;
                }
                newsTemplateFile.setData("headline", linktext);
                result = result + newsTemplateFile.getProcessedDataValue(C_TAG_WMLNAV);
            }
        }
        else {
            
            // Article deck requested, selected article is specified by URL parameter <code>article</code>
            int selected = new Integer(selectedArticle).intValue();
            if(selected < v.size()) {
                Object o = v.elementAt(selected);
                CmsNewsContentFile doc2 = (CmsNewsContentFile)o;
                newsTemplateFile.setData("date", Utils.getNiceShortDate(doc2.getNewsDate()));
                newsTemplateFile.setData("headline", Encoder.escapeXml(doc2.getNewsHeadline()));
                newsTemplateFile.setData("shorttext", Encoder.escapeXml(doc2.getNewsShortText()));
                newsTemplateFile.setData("author", Encoder.escapeXml(doc2.getNewsAuthor()));
                result = result + newsTemplateFile.getProcessedDataValue(C_TAG_WMLARTICLE);
            }
        }
        return result;
    }
}
