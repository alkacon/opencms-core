/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/examples/news/Attic/CmsNewsAdmin.java,v $
 * Date   : $Date: 2000/03/31 12:30:39 $
 * Version: $Revision: 1.4 $
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
import com.opencms.util.*;
import com.opencms.template.*;
import com.opencms.workplace.*;

import java.util.*;
import javax.servlet.http.*;

/**
 * News administration template class
 * <p>
 * Used both for displaying news administration overviews and
 * editing news.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.4 $ $Date: 2000/03/31 12:30:39 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */
public class CmsNewsAdmin extends CmsWorkplaceDefault implements I_CmsConstants, I_CmsNewsConstants,
                                                                 I_CmsFileListUsers {

    /** Name of the file parameter in the URL */
    public static final String C_NEWS_PARAM_FILE = "file";

    /** Name of the date parameter in the HTTP get request */
    public static final String C_NEWS_PARAM_DATE = "date";

    /** Name of the headline parameter in the HTTP get request */
    public static final String C_NEWS_PARAM_HEADLINE = "headline";
    
    /** Name of the shorttext parameter in the HTTP get request */
    public static final String C_NEWS_PARAM_SHORTTEXT = "shorttext";

    /** Name of the text parameter in the HTTP get request */
    public static final String C_NEWS_PARAM_TEXT = "text";
    
    /** Name of the external link parameter in the HTTP get request */
    public static final String C_NEWS_PARAM_EXTLINK = "extlink";

    /** Name of the state parameter in the HTTP get request */
    public static final String C_NEWS_PARAM_STATE = "state";
        
    /** Template selector of the "done" page */
    public static final String C_NEWS_DONE = "done";

    /** Filelist datablock for news state value */
    private final static String C_NEWS_STATE_VALUE = "NEWS_STATE_VALUE";

    /** Filelist datablock for news author value */
    private final static String C_NEWS_AUTHOR_VALUE = "NEWS_AUTHOR_VALUE";
    
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
        return false;
    }    

    /**
     * Gets the content of a defined section in a given template file and its subtemplates
     * with the given parameters. 
     * 
     * @see getContent(A_CmsObject cms, String templateFile, String elementName, Hashtable parameters)
     * @param cms A_CmsObject Object for accessing system resources.
     * @param templateFile Filename of the template file.
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     */
    public byte[] getContent(A_CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException {
        
        HttpServletRequest orgReq = (HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest();    
        HttpSession session = orgReq.getSession(true);
        
		// read the parameters
        String file = (String)parameters.get(C_NEWS_PARAM_FILE);
        if(file == null) {
            file = (String)session.getValue(C_NEWS_PARAM_FILE);
        } else {
            session.putValue("file", file);
        }

        String action = (String)parameters.get("action");
        if(action == null) {
            action = (String)session.getValue("action");
        } else {
            session.putValue("action", action);
        }        
        
        String newDate = (String)parameters.get(C_NEWS_PARAM_DATE);
        String newHeadline = (String)parameters.get(C_NEWS_PARAM_HEADLINE);
        String newShorttext = (String)parameters.get(C_NEWS_PARAM_SHORTTEXT);
        String newText = (String)parameters.get(C_NEWS_PARAM_TEXT);
        String newExternalLink = (String)parameters.get(C_NEWS_PARAM_EXTLINK);
        String newState = (String)parameters.get(C_NEWS_PARAM_STATE);

        // load the template file of the news admin screen
        CmsXmlWpTemplateFile xmlTemplateDocument = (CmsXmlWpTemplateFile)getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);

        // Only go on, if the "edit" action was requested
        if("edit".equals(action)) {                
            // Calendar object used to get the actual date
            GregorianCalendar cal = new GregorianCalendar();
        
            if(newHeadline == null && newShorttext == null && newText == null && newExternalLink == null) {
                if(file != null && ! "".equals(file)) {
                    // the user wants to edit an old article
                    CmsNewsTemplateFile newsFile = getNewsContentFile(cms, cms.readFile(file));
                    
                    A_CmsResource newsContentFileObject = cms.readFileHeader(newsFile.getAbsoluteFilename());
                    if (!newsContentFileObject.isLocked()) {
                        cms.lockResource(newsFile.getAbsoluteFilename());
                    }
                                    
                    parameters.put(C_NEWS_PARAM_DATE, Utils.getNiceShortDate(newsFile.getNewsDate()));
                    parameters.put(C_NEWS_PARAM_HEADLINE, newsFile.getNewsHeadline());
                    parameters.put(C_NEWS_PARAM_SHORTTEXT, newsFile.getNewsShortText());
                    parameters.put(C_NEWS_PARAM_TEXT, newsFile.getNewsText());
                    parameters.put(C_NEWS_PARAM_EXTLINK, newsFile.getNewsExternalLink());
                    parameters.put(C_NEWS_PARAM_STATE, new Boolean(newsFile.isNewsActive()));
                    xmlTemplateDocument.setXmlData("author", newsFile.getNewsAuthor());
                    session.putValue("author", newsFile.getNewsAuthor());
                } else {
                    // the user requested a new article

                    // Get the currently logged in user
                    A_CmsUser author = cms.getRequestContext().currentUser();        
        
                    // Get the String for the author
                    String authorText = null;
                    String initials =  getInitials(author);                               
                    String firstName = author.getFirstname();
                    String lastName = author.getLastname();
                    if((firstName == null || "".equals(firstName)) && (lastName == null || "".equals(lastName))) {
                        authorText = initials;
                    } else {            
                        authorText = firstName + " " + lastName;
                        authorText = authorText.trim();
                        authorText = authorText + " (" + initials + ")";
                    }
                    session.putValue("author", authorText);
                    xmlTemplateDocument.setXmlData("author", authorText);
                    
                    // Get the Sting for the actual date
                    String dateText = Utils.getNiceShortDate(cal.getTime().getTime());
                    parameters.put(C_NEWS_PARAM_DATE, dateText);
                }
            } else {
                // this is the POST result of an user input
                          
                CmsNewsTemplateFile newsContentFile = null;                 
                
                if(file == null || "".equals(file)) {
                    // we have to create a new new file
                                    
                    // Get the currently logged in user
                    A_CmsUser author = cms.getRequestContext().currentUser();        
        
                    // Build the new article filename
                    String dateFileText = getDateFileText(cal);        
                    String newsNumber = this.getNewArticleNumber(cms, dateFileText);
                    String initials =  getInitials(author);               
                    String newsFileName = dateFileText + "-" + newsNumber + "-" + initials.toLowerCase();
                    parameters.put(C_NEWS_PARAM_FILE, newsFileName);
                    
                    newsContentFile = createNewsFile(cms, newsFileName); //, authorText, dateText, newHeadline, newShorttext, newText, newExternalLink);
                    createPageFile(cms, newsFileName);

                    // check the date parameter
                    if(newDate == null || "".equals(newDate)) {
                        newDate = Utils.getNiceShortDate(cal.getTime().getTime());
                    }
                    
                    // Create task
                    CmsXmlLanguageFile lang = new CmsXmlLanguageFile(cms);
                    HttpServletRequest req = (HttpServletRequest)(cms.getRequestContext().getRequest().getOriginalRequest());
                    String taskUrl = req.getScheme() + "://" + req.getHeader("HOST") + req.getServletPath() + C_NEWS_FOLDER_PAGE + newsFileName + "/index.html";
                    String taskcomment = "<A HREF=\"javascript:openwinfull('" + taskUrl + "', 'preview', 0, 0);\"> " + taskUrl + "</A>";
                    CmsTaskAction.create(cms, C_NEWS_USER, C_NEWS_ROLE, lang.getLanguageValue("task.label.news"), taskcomment, Utils.getNiceShortDate(new Date().getTime() + 345600000), "1", "", "", "", "");                
                } else {
                    newsContentFile = getNewsContentFile(cms, cms.readFile(file));                
                }
                
                setNewsFileContent(newsContentFile, (String)session.getValue("author"), newDate, newHeadline, newShorttext, newText, newExternalLink, newState);
                cms.unlockResource(newsContentFile.getAbsoluteFilename());
                
                                                             
                session.removeValue("file");
                templateSelector = C_NEWS_DONE;                
		    }
        }
        
        // Finally start the processing
		return startProcessing(cms, xmlTemplateDocument, elementName, parameters, templateSelector);
    }

    public String getDate(A_CmsObject cms, CmsXmlLanguageFile lang, Hashtable parameters) {
        String result = (String)parameters.get(C_NEWS_PARAM_DATE);
        if(result == null) {
            result = "";
        }
        return result;
    }

    public String getHeadline(A_CmsObject cms, CmsXmlLanguageFile lang, Hashtable parameters) {
        String result = (String)parameters.get(C_NEWS_PARAM_HEADLINE);
        if(result == null) {
            result = "";
        }
        return result;
    }

    public String getShorttext(A_CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) {
        Hashtable parameters = (Hashtable)userObject;
        String result = (String)parameters.get(C_NEWS_PARAM_SHORTTEXT);
        if(result == null) {
            result = "";
        }
        return result;
    }

    public String getText(A_CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) {
        Hashtable parameters = (Hashtable)userObject;
        String result = (String)parameters.get(C_NEWS_PARAM_TEXT);
        if(result == null) {
            result = "";
        }
        return result;
    }
        
    public String getExternalLink(A_CmsObject cms, CmsXmlLanguageFile lang, Hashtable parameters) {
        String result = (String)parameters.get(C_NEWS_PARAM_EXTLINK);
        if(result == null || "".equals(result)) {
            result = "http://";
        }
        return result;
    }
    
    /**
     * Gets the resources displayed in the Radiobutton group on the new resource dialog.
     * @param cms The CmsObject.
     * @param lang The langauge definitions.
     * @param names The names of the new rescources (used for optional images).
     * @param values The links that are connected with each resource.
     * @param descriptions Description that will be displayed for the new resource.
     * @param parameters Hashtable of parameters (not used yet).
     * @returns The vectors names and values are filled with the information found in the 
     * workplace.ini.
     * @exception Throws CmsException if something goes wrong.
     */
    public Integer getStates(A_CmsObject cms, CmsXmlLanguageFile lang, Vector names, Vector values, Vector descriptions, Hashtable parameters) 
           throws CmsException { 
        Boolean state = (Boolean)parameters.get(C_NEWS_PARAM_STATE);        
        names.addElement("");
        names.addElement("");
        values.addElement("active");
        values.addElement("inactive");
        descriptions.addElement(lang.getLanguageValue(C_LANG_LABEL + ".active"));
        descriptions.addElement(lang.getLanguageValue(C_LANG_LABEL + ".inactive")); 
        if(state != null && state.equals(Boolean.TRUE)) {
            return new Integer(0);
        } else {
            return new Integer(1);
        }
    }        

   /** 
    * Collects all folders and files that are displayed in the file list.
    * @param cms The CmsObject.
    * @return A vector of folder and file objects.
    * @exception Throws CmsException if something goes wrong.
    */
    public Vector getFiles(A_CmsObject cms) 
        throws CmsException {
        Vector newsFiles = new Vector();
        
        Vector newsFolders = cms.getSubFolders(C_NEWS_FOLDER_PAGE); 
        int numNewsFolders = newsFolders.size();
        for(int i=0; i<numNewsFolders; i++) {
            A_CmsResource currFolder = (A_CmsResource)newsFolders.elementAt(i);
            CmsFile newsPageFile = null;
            try {
                newsPageFile = cms.readFile(currFolder.getAbsolutePath(), "index.html");
            } catch(Exception e) {
                // Oh... we expected an index file here.
                // Do nothing instead
                continue;
            }
            newsFiles.addElement(newsPageFile);            
        }                                                            
        return newsFiles;
    }
       
    /**
     * Used to modify the bit pattern for hiding and showing columns in
     * the file list.
     * @param cms Cms object for accessing system resources.
     * @param prefs Old bit pattern.
     * @return New modified bit pattern.
     * @see I_CmsFileListUsers
     */
    public int modifyDisplayedColumns(A_CmsObject cms, int prefs) {
        prefs = ((prefs & C_FILELIST_TITLE) == 0) ? prefs : (prefs - C_FILELIST_TITLE);
        prefs = ((prefs & C_FILELIST_TYPE) == 0) ? prefs : (prefs - C_FILELIST_TYPE);
        return prefs;
    }
    
    /**
     * Fills all customized columns with the appropriate settings for the given file 
     * list entry. Any column filled by this method may be used in the customized template
     * for the file list.
     * @param cms Cms object for accessing system resources.
     * @param filelist Template file containing the definitions for the file list together with
     * the included customized defintions.
     * @param res A_CmsResource Object of the current file list entry.
     * @param lang Current language file.
     * @exception CmsException if access to system resources failed.
     * @see I_CmsFileListUsers
     */
    public void getCustomizedColumnValues(A_CmsObject cms, CmsXmlWpTemplateFile filelistTemplate, A_CmsResource res, CmsXmlLanguageFile lang) 
        throws CmsException {
        String state = lang.getLanguageValue(C_LANG_LABEL + ".notavailable");
        String author = state;
        String name = null;
        if(res instanceof CmsFile) {
            CmsNewsTemplateFile newsContentFile = getNewsContentFile(cms, res);
            state = newsContentFile.isNewsActive() ? lang.getLanguageValue(C_LANG_LABEL + ".active") : lang.getLanguageValue(C_LANG_LABEL + ".inactive");
            author = newsContentFile.getNewsAuthor();
            name = newsContentFile.getNewsHeadline();
        }
        filelistTemplate.setData(C_NEWS_STATE_VALUE, state);
        filelistTemplate.setData(C_NEWS_AUTHOR_VALUE, author);        
        if(name != null) {
             filelistTemplate.setData(C_FILELIST_NAME_VALUE, name);                    
        }
    }    

    protected CmsNewsTemplateFile getNewsContentFile(A_CmsObject cms, A_CmsResource file) throws CmsException {

        CmsFile newsContentFileObject = null;
        CmsNewsTemplateFile newsContentFile = null;

        // The given file object contains the news page file.
        // we have to read out the article
        CmsXmlControlFile newsPageFile = new CmsXmlControlFile(cms, (CmsFile)file);
        String readParam = newsPageFile.getElementParameter("body", "read");
        String newsfolderParam = newsPageFile.getElementParameter("body", "newsfolder");
        
        if(readParam != null && !"".equals(readParam)) {
            // there is a read parameter given.
            // so we know which news file should be read.
            if(newsfolderParam == null || "".equals(newsfolderParam)) {
                newsfolderParam = C_NEWS_FOLDER_CONTENT;
            }
            try {
                newsContentFileObject = cms.readFile(newsfolderParam, readParam);
            } catch(Exception e) {
                // Ooops. The news content file could not be read.
                newsContentFileObject = null;
            }
            if(newsContentFileObject != null) {
                newsContentFile = new CmsNewsTemplateFile(cms, newsContentFileObject);
            }
        }
        return newsContentFile;
    }
    
    
    /**
     * Get the new article number by scanning all existing articles
     * of the given date and inreasing the maximum number.
     * @param cms A_CmsObject for accessing system resources
     * @param dateFileText String containing the date used in news file names
     * @return new article number.
     * @exception CmsException
     */
    private String getNewArticleNumber(A_CmsObject cms, String dateFileText) throws CmsException {
        String numberText = null;
        
        // Get all files in the news folder
        Vector allNews = cms.getFilesInFolder(C_NEWS_FOLDER_CONTENT);
        int numNews = allNews.size();
        int max = -1;
        for(int i=0; i<numNews; i++) {
            // Scan all files in the news folder beginning with the
            // current date String.
            // The old maximum number will be stored in "max"
            CmsFile file = (CmsFile)allNews.elementAt(i);
            String filename = file.getName();
            if(filename.startsWith(dateFileText)) {                
                int index1 = filename.indexOf("-");
                int index2 = filename.indexOf("-", index1+1);
                numberText = filename.substring(index1+1, index2);
                int noOfDay = new Integer(numberText).intValue();
                if(noOfDay > max) {
                    max = noOfDay;
                }
            }
        }
        
        // Build a 3 digit String representation of the new number
        // (with leading 0)
        max++;        
        numberText = "00" + max;
        if(numberText.length() > 3) {
            numberText = numberText.substring(1,4);
        }
        return numberText;
    }
    
    /**
     * Get the initials of the current user.
     * If both firstname and lastname of the user are not set,
     * the login name will be returned instead.
     * @author A_CmsUser object of the currently logged in user.
     * @return initials of the user.
     */
    private String getInitials(A_CmsUser author) {
        String firstname = author.getFirstname();
        String lastname = author.getLastname();
        String initials = "";
        if(firstname.length() >= 1) {
            initials = initials + firstname.substring(0,1).toLowerCase();
        }
        if(lastname.length() >= 1) {
            initials = initials + lastname.substring(0,1).toLowerCase();
        }
        if("".equals(initials)) {           
            initials = author.getName(); 
        }
        return initials;
    }
    
    /**
     * Get a String representation of the date given by the <code>cal</code>
     * object, that can be used to build filenames for news files.
     * <P>
     * The date will be written like <code>YYMMDD</code>.
     * @param cal Calendar object representig the date
     * @return Date String
     */
    private String getDateFileText(GregorianCalendar cal) {        
        String day="0"+new Integer(cal.get(Calendar.DAY_OF_MONTH)).intValue();        
        String month="0"+new Integer(cal.get(Calendar.MONTH)+1).intValue(); 
        String year="0"+new Integer(cal.get(Calendar.YEAR) % 100).toString();

        if (day.length() > 2) {
            day=day.substring(1,3);
        }
        if (month.length() > 2) {
            month=month.substring(1,3);
        }
        if (year.length() > 2) {
            year=year.substring(1,3);
        }
       
        return year + month + day;
    }
    
    /**
     * Create a news content file.
     * 
     * @param cms A_CmsObject for accessing system resources.
     * @param newsFileName filename to be used
     * @param author Author
     * @param date Date
     * @param headline Headline
     * @param shorttext Short news text
     * @param test Complete news text
     * @param extlink External link
     * @exception CmsException
     */
    private CmsNewsTemplateFile createNewsFile(A_CmsObject cms, String newsFileName) throws CmsException { //, String author, String date, String headline, 
                                //String shorttext, String text, String extlink) throws CmsException {
        String fullFilename = C_NEWS_FOLDER_CONTENT + newsFileName;
        CmsNewsTemplateFile newsTempDoc = new CmsNewsTemplateFile();
        newsTempDoc.createNewFile(cms, fullFilename, "plain");
              
        cms.chmod(fullFilename, C_ACCESS_DEFAULT_FLAGS);
        return newsTempDoc;
    }
   
    private void setNewsFileContent(CmsNewsTemplateFile newsFile, String author, String date, String headline, 
                                String shorttext, String text, String extlink, String state) throws CmsException {
        newsFile.setNewsAuthor(author);
        if(date != null && !"".equals(date)) {
            // only set date if given
            newsFile.setNewsDate(date);
        }
        newsFile.setNewsHeadline(headline);
        newsFile.setNewsShortText(shorttext);
        newsFile.setNewsText(text);
        newsFile.setNewsExternalLink(extlink);               
        newsFile.setNewsActive("active".equals(state));
        newsFile.write();
    }            
        
    /**
     * Create a new page file for displayin a given news content file.
     * 
     * @param cms A_CmsObject for accessing system resources.
     * @param newsFileName filename to be used
     * @exception CmsException
     */
    private void createPageFile(A_CmsObject cms, String newsFileName) throws CmsException {
        
        // Create the news folder
        cms.createFolder(C_NEWS_FOLDER_PAGE, newsFileName);
               
        /*String fullFolderName = C_NEWS_FOLDER_PAGE + newsFileName + "/";
        cms.lockResource(fullFolderName);
        cms.chmod(fullFolderName, C_ACCESS_DEFAULT_FLAGS); 
        cms.unlockResource(fullFolderName);*/
    
        // Create an index file in this folder
        String fullFilename = C_NEWS_FOLDER_PAGE + newsFileName + "/index.html";        
        CmsXmlControlFile pageFile = new CmsXmlControlFile();
        pageFile.createNewFile(cms, fullFilename, "newspage");
        pageFile.setTemplateClass("com.opencms.template.CmsXmlTemplate");
        pageFile.setMasterTemplate("/content/templates/xDemoTemplate1");
        pageFile.setElementClass("body", "com.opencms.examples.news.CmsNewsTemplate");
        pageFile.setElementTemplate("body", C_PATH_INTERNAL_TEMPLATES + "newsTemplate");
        pageFile.setElementParameter("body", "newsfolder", C_NEWS_FOLDER_CONTENT);
        pageFile.setElementParameter("body", "read", newsFileName);
        pageFile.write();
        cms.chmod(fullFilename, C_ACCESS_DEFAULT_FLAGS);
        cms.unlockResource(fullFilename);
    }
}
