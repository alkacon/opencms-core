
/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/xmlmodules/news/Attic/CmsNewsContent.java,v $
* Date   : $Date: 2001/01/24 09:43:48 $
* Version: $Revision: 1.7 $
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

package com.opencms.xmlmodules.news;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.template.*;
import com.opencms.util.*;
import com.opencms.xmlmodules.*;
import java.util.*;
import javax.servlet.http.*;

/**
 * Template class for displaying news articles, inherits form class
 * <CODE>A_CmsModuleContent</CODE>
 * <P>
 * If no parameter is given, the default section of the template file 
 * will be called. This may be used for displaying a list of all available articles.
 * <P>
 * If the parameter <code>read</code> is set, the vaule of this parameter
 * will be taken as a filename of a XML content file that will be displayed.
 * In this case the <code>read</code> section of the template file will
 * be used.
 * <P>
 * The parameter <code>number</code> may be used to display a certain
 * article. A <code>number</code> value of <code>1</code> will display
 * the most actual article, a value of <code>2</code> will display the
 * article before this one and so on.
 *
 * 
 * @author Matthias Schreiber
 * @version $Revision: 1.7 $ $Date: 2001/01/24 09:43:48 $
 * @see com.opencms.xmlmodules.A_CmsModuleContent
 */
public class CmsNewsContent extends A_CmsModuleContent implements I_CmsNewsConstants {
    
    /**
     * Gets the content of a given template file.
     * <P>
     * While processing the template file the user methods
     * <code>getArticleList</code> and <code>getTagContent</code> will be used
     * to display data of the content type "newsarticle".
     * 
     * @param cms A_CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file 
     * @param elementName <em>not used here</em>.
     * @param parameters <em>not used here</em>.
     * @param templateSelector template section that should be processed.
     * @return Processed content of the given template file.
     * @exception CmsException 
     */
    public byte[] getContent(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException {
        String read = (String)parameters.get(elementName + ".read");
        String number = (String)parameters.get(elementName + ".number");
        String folder = getFolder(elementName, parameters);
        CmsNewsObject doc = null;
        if(read != null && !"".equals(read)) {
            
            // The read parameter was set. So we have a certain article selected
            templateSelector = "read";
            doc = new CmsNewsObject(cms, folder + read);
        }
        else {
            if(number != null && !"".equals(number)) {
                
                // The number parameter was set.
                
                // So we have to get a list of all articles and search
                
                // for the requested number, beginning with the latest article
                templateSelector = "read";
                Vector v = getAllArticles(cms, folder);
                int articleNum = new Integer(number).intValue();
                if(articleNum < v.size()) {
                    doc = (CmsNewsObject)v.elementAt(articleNum);
                }
            }
        }
        
        // Add the article to the parameters for making it accessible for
        
        // the user methods.
        if(doc != null) {
            parameters.put("_ARTICLE_", doc);
        }
        CmsXmlTemplateFile xmlTemplateDocument = getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);
        return startProcessing(cms, xmlTemplateDocument, elementName, parameters, templateSelector);
    }
    
    /**
     * Get a vector of CmsNewsObjects with one entry for every article.
     * @param cms A_CmsObject object for accessing system resources.
     * @param sortedFiles Vector with all files availables, sorted by date
     * @param showInactive Indicates, whether inactive articles should be added to result
     * @return List of articles available.
     */
    protected Vector getFileObjects(CmsObject cms, Vector sortedFiles, boolean showInactive) throws CmsException {
        
        // Loop through the sorted files and create a CmsNewsObject for each of them.
        Vector listFiles = new Vector();
        int numFiles = sortedFiles.size();
        for(int i = 0;i < numFiles;i++) {
            CmsNewsObject doc = new CmsNewsObject();
            CmsFile fileHeader = (CmsFile)sortedFiles.elementAt(i);
            CmsFile file = cms.readFile(fileHeader.getAbsolutePath());
            doc.init(cms, file);
            if(showInactive || doc.isActive()) {
                listFiles.addElement(doc);
            }
        }
        return listFiles;
    }
    
    /**
     * Get the folder with content files.
     * The value will be taken from the user parameters or from the constants
     * @param elementName Name of the current subtemplate
     * @param parameters User parameters
     * @return Name of the folder containing the content
     */
    protected String getFolder(String elementName, Hashtable parameters) {
        String folder = (String)parameters.get(elementName + ".folder");
        if(folder == null || "".equals(folder)) {
            folder = C_FOLDER_CONTENT;
        }
        return folder;
    }
    
    /**
     * Gets application-specific article list-data to display
     * on default clients.
     * @param template Template file to access
     * @param v Vector that contains all articles
     * @param servletPath Path to the OpenCms servlet zone
     * @return List of all articles.
     */
    protected String getListData(CmsXmlTemplateFile template, Vector v, String servletPath, Object userObj) throws CmsException {
        String result = "";
        for(int i = 0;i < v.size();i++) {
            Object o = v.elementAt(i);
            CmsNewsObject doc2 = (CmsNewsObject)o;
            template.setData("date", Utils.getNiceShortDate(doc2.getDate()));
            template.setData("headline", Encoder.escapeXml(doc2.getHeadline()));
            template.setData("author", Encoder.escapeXml(doc2.getAuthor()));
            template.setData("shorttext", Encoder.escapeXml(doc2.getShortText()));
            template.setData("link", servletPath + C_FOLDER_PAGE + doc2.getFilename() + "/index.html");
            template.setData("servletPath", servletPath + "/");
            template.setData("index", "" + i);
            result = result + template.getProcessedDataValue(C_TAG_LISTENTRY, this, userObj);
        }
        return result;
    }
    
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
    public String getTagContent(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObj) throws CmsException {
        CmsRequestContext reqCont = cms.getRequestContext();
        HttpServletRequest orgReq = (HttpServletRequest)reqCont.getRequest().getOriginalRequest();
        String servletPath = orgReq.getServletPath();
        Hashtable parameters = (Hashtable)userObj;
        CmsXmlTemplateFile templateFile = (CmsXmlTemplateFile)doc;
        CmsNewsObject article = (CmsNewsObject)parameters.get("_ARTICLE_");
        String elementName = (String)parameters.get("_ELEMENT_");
        String result = "";
        if(article != null) {
            if(tagcontent.toLowerCase().equals("externallink")) {
                result = article.getExternalLink();
            }
            else {
                if(tagcontent.toLowerCase().equals("date")) {
                    long date = article.getDate();
                    if(date != 0) {
                        result = Utils.getNiceShortDate(article.getDate());
                    }
                }
                else {
                    if(tagcontent.toLowerCase().equals("author")) {
                        result = Encoder.escapeXml(article.getAuthor());
                    }
                    else {
                        if(tagcontent.toLowerCase().equals("headline")) {
                            result = Encoder.escapeXml(article.getHeadline());
                        }
                        else {
                            if(tagcontent.toLowerCase().equals("shorttext")) {
                                result = Encoder.escapeXml(article.getShortText());
                            }
                            else {
                                if(tagcontent.toLowerCase().equals("text")) {
                                    result = article.getText(templateFile.getDataValue(C_TAG_PARAGRAPHSEP), true);
                                }
                                else {
                                    if(tagcontent.toLowerCase().equals("file")) {
                                        result = servletPath + C_FOLDER_PAGE + article.getFilename() + "/index.html";
                                    }
                                    else {
                                        if(tagcontent.toLowerCase().equals("linktext")) {
                                            result = "Artikel lesen";
                                        }
                                        else {
                                            if(tagcontent.toLowerCase().equals("servletPath")) {
                                                result = servletPath + "/";
                                            }
                                            else {
                                                if(tagcontent.toLowerCase().equals("index")) {
                                                    
                                                    // gets the index of currently selected article
                                                    String folder = getFolder(elementName, parameters);
                                                    Vector v = getAllArticles(cms, folder);
                                                    for(int i = 0;i < v.size();i++) {
                                                        Object o = v.elementAt(i);
                                                        CmsNewsObject doc2 = (CmsNewsObject)o;
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
     * Gets a valid URI especially for use in XSL-templates to produce PDF files.
     * <P>
     * Called by the template file using 
     * <code>&lt;METHOD name="getValidURI"/&gt;</code>.
     * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object the initiating XML document.  
     * @param userObj Hashtable with parameters.
     * @return Valid URI.
     */
    public String getValidURI(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObj) throws CmsException {
        Hashtable parameters = (Hashtable)userObj;
        CmsNewsObject article = (CmsNewsObject)parameters.get("_ARTICLE_");
        String result = "";
        if(article != null) {
            result = article.getExternalLink();
            if((result == null) || ("".equals(result))) {
                result = "#";
            }
        }
        return result;
    }
    
    /**
     * Gets application-specific article-list data to display 
     * on wap-enabled clients.
     * @param template Template file to access
     * @param v Vector that contains all articles
     * @param selection Index of the currently selected article 
     * @param isNavDeck Value to decide between navigation or article requests
     * @return List of all articles.
     */
    protected String getWapData(CmsXmlTemplateFile template, Vector v, int selection, boolean isNavDeck) throws CmsException {
        String result = "";
        Object o = v.elementAt(selection);
        CmsNewsObject doc2 = (CmsNewsObject)o;
        if(isNavDeck) { //generate output for requested navigation deck
            template.setData("selection", "?body.article=" + selection);
            template.setData("headline", Encoder.escapeXml(doc2.getHeadline()));
            result = template.getProcessedDataValue(C_TAG_WMLNAV);
        }
        else { //generate output for requested article deck
            template.setData("date", Utils.getNiceShortDate(doc2.getDate()));
            template.setData("headline", Encoder.escapeXml(doc2.getHeadline()));
            template.setData("shorttext", Encoder.escapeXml(doc2.getShortText()));
            template.setData("author", Encoder.escapeXml(doc2.getAuthor()));
            result = template.getProcessedDataValue(C_TAG_WMLARTICLE);
        }
        return result;
    }
}
