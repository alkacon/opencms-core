/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/examples/news/Attic/CmsNewsAdmin.java,v $
 * Date   : $Date: 2000/05/18 13:53:39 $
 * Version: $Revision: 1.13 $
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
import java.io.*;
import javax.servlet.http.*;

import org.apache.xml.serialize.*;

/**
 * News administration template class
 * <p>
 * Used both for displaying news administration overviews and
 * editing news.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.13 $ $Date: 2000/05/18 13:53:39 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */
public class CmsNewsAdmin extends CmsWorkplaceDefault implements I_CmsConstants, I_CmsNewsConstants, I_CmsFileListUsers {
        
    /** Template selector of the "done" page */
    public static final String C_NEWS_DONE = "done";

    /** Filelist datablock for news state value */
    private final static String C_NEWS_STATE_VALUE = "NEWS_STATE_VALUE";

    /** Filelist datablock for news author value */
    private final static String C_NEWS_AUTHOR_VALUE = "NEWS_AUTHOR_VALUE";

    /** Definition of the Datablock NEW */   
    private final static String C_NEW="NEW";
     
    /** Definition of the Datablock NEW_ENABLED */   
    private final static String C_NEW_ENABLED="NEW_ENABLED";    

    /** Definition of the Datablock NEW_DISABLED */   
    private final static String C_NEW_DISABLED="NEW_DISABLED"; 
    
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
        
		// read all parameters
        String file = (String)parameters.get(C_NEWS_PARAM_FILE);
        if(file == null) {
            file = (String)session.getValue(C_NEWS_PARAM_FILE);
        } else {
            session.putValue(C_NEWS_PARAM_FILE, file);
        }

        String action = (String)parameters.get(C_NEWS_PARAM_ACTION);
        if(action == null) {
            action = (String)session.getValue(C_NEWS_PARAM_ACTION);
        } else {
            session.putValue(C_NEWS_PARAM_ACTION, action);
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
                    CmsNewsContentFile newsFile = getNewsContentFile(cms, cms.readFile(file));
                    
                    A_CmsResource newsContentFileObject = cms.readFileHeader(newsFile.getAbsoluteFilename());
                    if (!newsContentFileObject.isLocked()) {
                        cms.lockResource(newsFile.getAbsoluteFilename());
                    }
                                    
                    parameters.put(C_NEWS_PARAM_DATE, Utils.getNiceShortDate(newsFile.getNewsDate()));
                    parameters.put(C_NEWS_PARAM_HEADLINE, newsFile.getNewsHeadline());
                    parameters.put(C_NEWS_PARAM_SHORTTEXT, newsFile.getNewsShortText());
                    parameters.put(C_NEWS_PARAM_TEXT, newsFile.getNewsText("\n\n"));
                    parameters.put(C_NEWS_PARAM_EXTLINK, newsFile.getNewsExternalLink());
                    parameters.put(C_NEWS_PARAM_STATE, new Boolean(newsFile.isNewsActive()));
                    xmlTemplateDocument.setData(C_NEWS_PARAM_AUTHOR, newsFile.getNewsAuthor());
                    session.putValue(C_NEWS_PARAM_AUTHOR, newsFile.getNewsAuthor());
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
                    session.putValue(C_NEWS_PARAM_AUTHOR, authorText);
                    xmlTemplateDocument.setData(C_NEWS_PARAM_AUTHOR, authorText);
                    
                    // Get the Sting for the actual date
                    String dateText = Utils.getNiceShortDate(cal.getTime().getTime());
                    parameters.put(C_NEWS_PARAM_DATE, dateText);
                }
            } else {
                // this is the POST result of an user input
                
                CmsXmlTemplateFile newsIni = new CmsXmlTemplateFile(cms, C_NEWS_INI);                
                CmsNewsContentFile newsContentFile = null;                 
                
                if(file == null || "".equals(file)) {
                    // we have to create a new new file
                                    
                    // Get the currently logged in user
                    A_CmsUser author = cms.getRequestContext().currentUser();        
        
                    // Build the new article filename
                    String dateFileText = getDateFileText(cal);        
                    String newsNumber = getNewArticleNumber(cms, dateFileText);
                    String initials =  getInitials(author);               
                    String newsFileName = dateFileText + "-" + newsNumber + "-" + initials.toLowerCase();
                    parameters.put(C_NEWS_PARAM_FILE, newsFileName);
                    
                    // create files
                    newsContentFile = createNewsFile(cms, newsFileName);                    
                    createPageFile(cms, newsFileName, newsIni.getDataValue("mastertemplate"));

                    // check the date parameter
                    if(newDate == null || "".equals(newDate)) {
                        newDate = Utils.getNiceShortDate(cal.getTime().getTime());
                    }
                    
                    // Try creating the task
                    try {
                        makeTask(cms, newsFileName, newsIni.getDataValue("newstask.agent"), newsIni.getDataValue("newstask.role"));
                    } catch(Exception e) {
                        if(A_OpenCms.isLogging()) {
                            A_OpenCms.log(C_OPENCMS_CRITICAL, getClassName() + "Cannot create news task for news article " + newsFileName + ". ");
                            A_OpenCms.log(C_OPENCMS_CRITICAL, getClassName() + e.getMessage());
                        }
                    }                    
                } else {
                    newsContentFile = getNewsContentFile(cms, cms.readFile(file));                
                    // Touch the page file. This will mark it a "changed".
                    cms.writeFileHeader((CmsFile)cms.readFileHeader(C_NEWS_FOLDER_PAGE + newsContentFile.getFilename() + "/index.html"));
                }
                
                // Set news content and unlock resource
                setNewsFileContent(newsContentFile, (String)session.getValue(C_NEWS_PARAM_AUTHOR), newDate, newHeadline, newShorttext, newText, newExternalLink, newState);
                cms.unlockResource(newsContentFile.getAbsoluteFilename());                                                                             
                
                // Session parameters are not needed any more...
                session.removeValue(C_NEWS_PARAM_FILE);
                session.removeValue(C_NEWS_PARAM_ACTION);
                session.removeValue(C_NEWS_PARAM_AUTHOR);
                templateSelector = C_NEWS_DONE;                
		    }
        }
        
        
        // check if the new resource button must be enabeld.
        // this is only done if the project is not the online project.
        if(xmlTemplateDocument.hasData(C_NEW_DISABLED) && xmlTemplateDocument.hasData(C_NEW_ENABLED)) {
            if (cms.getRequestContext().currentProject().equals(cms.onlineProject()) || !checkWriteAccess(cms)) {
                xmlTemplateDocument.setData(C_NEW,xmlTemplateDocument.getProcessedDataValue(C_NEW_DISABLED,this));                
            } else {
                xmlTemplateDocument.setData(C_NEW,xmlTemplateDocument.getProcessedDataValue(C_NEW_ENABLED,this));       
            }
        }
        
        // Finally start the processing
		return startProcessing(cms, xmlTemplateDocument, elementName, parameters, templateSelector);
    }

    /**
     * Used for filling the input field <em>Date</em> in the news editor.
     * @param cms Cms object for accessing system resources.
     * @param lang Current language file.
     * @param parameters User parameters.
     * @return String containing the date.
     */
    public String getDate(A_CmsObject cms, CmsXmlLanguageFile lang, Hashtable parameters) {
        String result = (String)parameters.get(C_NEWS_PARAM_DATE);
        if(result == null) {
            result = "";
        }
        return result;
    }

    /**
     * Used for filling the input field <em>Headline</em> in the news editor.
     * @param cms Cms object for accessing system resources.
     * @param lang Current language file.
     * @param parameters User parameters.
     * @return String containing the headline of the article.
     */
    public String getHeadline(A_CmsObject cms, CmsXmlLanguageFile lang, Hashtable parameters) {
        String result = (String)parameters.get(C_NEWS_PARAM_HEADLINE);
        if(result == null) {
            result = "";
        }
        return result;
    }

    /**
     * Used for filling the input field <em>Short Text</em> in the news editor.
     * @param cms Cms object for accessing system resources.
     * @param lang Current language file.
     * @param parameters User parameters.
     * @return String containing the short text of the article.
     */
    public String getShorttext(A_CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) {
        Hashtable parameters = (Hashtable)userObject;
        String result = (String)parameters.get(C_NEWS_PARAM_SHORTTEXT);
        if(result == null) {
            result = "";
        }
        return result;
    }

    /**
     * Used for filling the input field <em>Text</em> in the news editor.
     * @param cms Cms object for accessing system resources.
     * @param lang Current language file.
     * @param parameters User parameters.
     * @return String containing the article text.
     */
    public String getText(A_CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) {
        Hashtable parameters = (Hashtable)userObject;
        String result = (String)parameters.get(C_NEWS_PARAM_TEXT);
        if(result == null) {
            result = "";
        }
        return result;
    }
        
    /**
     * Used for filling the input field <em>External Link</em> in the news editor.
     * @param cms Cms object for accessing system resources.
     * @param lang Current language file.
     * @param parameters User parameters.
     * @return String containing the external link.
     */
    public String getExternalLink(A_CmsObject cms, CmsXmlLanguageFile lang, Hashtable parameters) {
        String result = (String)parameters.get(C_NEWS_PARAM_EXTLINK);
        if(result == null || "".equals(result)) {
            result = "";
        }
        return result;
    }
    
    /**
     * Used for filling the values of a radio button.
     * <P>
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
        values.addElement(C_NEWS_STATE_ACTIVE);     
        values.addElement(C_NEWS_STATE_INACTIVE);
        descriptions.addElement(lang.getLanguageValue(C_LANG_LABEL + "." + C_NEWS_STATE_ACTIVE));
        descriptions.addElement(lang.getLanguageValue(C_LANG_LABEL + "." + C_NEWS_STATE_INACTIVE)); 
        if(state != null && state.equals(Boolean.TRUE)) {
            return new Integer(0);
        } else {
            return new Integer(1);
        }
    }        

   /** 
    * From interface <code>I_CmsFileListUsers</code>.
    * <P>    
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
     * From interface <code>I_CmsFileListUsers</code>.
     * <P>    
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
     * From interface <code>I_CmsFileListUsers</code>.
     * <P>    
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
            CmsNewsContentFile newsContentFile = getNewsContentFile(cms, res);
            state = newsContentFile.isNewsActive() ? lang.getLanguageValue(C_LANG_LABEL + "." + C_NEWS_STATE_ACTIVE) : lang.getLanguageValue(C_LANG_LABEL + "." + C_NEWS_STATE_INACTIVE);
            author = newsContentFile.getNewsAuthor();
            name = newsContentFile.getNewsHeadline();
        } 
        filelistTemplate.setData(C_NEWS_STATE_VALUE, state);
        filelistTemplate.setData(C_NEWS_AUTHOR_VALUE, author);   
        if(name != null) {
             filelistTemplate.setData(C_FILELIST_NAME_VALUE, name);                    
        } 
    }    

    /**
     * Get the corresponding news content file for a given newspage file.
     * @param file File object of the newspage file.
     * @param cms A_CmsObject for accessing system resources.
     * @return CmsNewsContentFile object of the corresponding news content file.
     * @exception CmsException if file access failed.
     */
    private CmsNewsContentFile getNewsContentFile(A_CmsObject cms, A_CmsResource file) throws CmsException {

        CmsFile newsContentFileObject = null;
        CmsNewsContentFile newsContentFile = null;

        // The given file object contains the news page file.
        // we have to read out the article
        CmsXmlControlFile newsPageFile = new CmsXmlControlFile(cms, (CmsFile)file); 
        String readParam = newsPageFile.getElementParameter(C_BODY_ELEMENT, C_NEWS_PARAM_READ);
        String newsfolderParam = newsPageFile.getElementParameter(C_BODY_ELEMENT, C_NEWS_PARAM_NEWSFOLDER);
        
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
                newsContentFile = new CmsNewsContentFile(cms, newsContentFileObject);
            }
        }
        return newsContentFile;
    }
    
    /** Create the task for the new news article.
     * @param cms A_CmsObject for accessing system resources.
     * @param newsFileName File name of the news article, used to generate a link.
     * @param taskuser User of the new task
     * @param taskgroup Group of the new task. 
     * @exception CmsException
     */
    private void makeTask(A_CmsObject cms, String newsFileName, String taskuser, String taskgroup) throws CmsException {
        CmsXmlLanguageFile lang = new CmsXmlLanguageFile(cms);
        HttpServletRequest req = (HttpServletRequest)(cms.getRequestContext().getRequest().getOriginalRequest());
        String taskUrl = req.getScheme() + "://" + req.getHeader("HOST") + req.getServletPath() + C_NEWS_FOLDER_PAGE + newsFileName + "/index.html";
        String taskcomment = "<A HREF=\"javascript:openwinfull('" + taskUrl + "', 'preview', 0, 0);\"> " + taskUrl + "</A>";
        CmsTaskAction.create(cms, taskuser, taskgroup, lang.getLanguageValue("task.label.news"), taskcomment, Utils.getNiceShortDate(new Date().getTime() + 345600000), "1", "", "", "", "");                
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
     * Create a news content file. File flags setted in the user preferences
     * will be overridden to system default flags.
     * 
     * @param cms A_CmsObject for accessing system resources.
     * @param newsFileName filename to be used
     * @exception CmsException
     */
    private CmsNewsContentFile createNewsFile(A_CmsObject cms, String newsFileName) throws CmsException { //, String author, String date, String headline, 
        String fullFilename = C_NEWS_FOLDER_CONTENT + newsFileName;
        CmsNewsContentFile newsTempDoc = new CmsNewsContentFile();
        newsTempDoc.createNewFile(cms, fullFilename, C_TYPE_PLAIN_NAME);              
        cms.chmod(fullFilename, C_ACCESS_DEFAULT_FLAGS);
        return newsTempDoc;
    }
   
    /**
     * Set the content of the news XML file to the given values.
     * <p>
     * The <code>text</code> will be separated into paragraphs, if more than one
     * following line feeds are found.
     * @param newsFile CmsNewsContentFile object of the news content file.
     * @param author Author
     * @param date Date
     * @param headline Headline
     * @param shorttext Short news text
     * @param test Complete news text
     * @param extlink External link
     * @param state State of the article. Should be <code>active</code> or <code>inactive</code>
     * @exception CmsExecption
     */
    private void setNewsFileContent(CmsNewsContentFile newsFile, String author, String date, String headline, 
                                String shorttext, String text, String extlink, String state) throws CmsException {
        newsFile.setNewsAuthor(author);
        if(date != null && !"".equals(date)) {
            // only set date if given
            newsFile.setNewsDate(date);
        }
		
        // Divide the text into separate lines.
        BufferedReader br = new BufferedReader(new StringReader(text));
        String lineStr = null;
        StringBuffer sb = new StringBuffer();
        Vector paragraphs = new Vector();
        try { 
            while ((lineStr = br.readLine()) != null) {
                lineStr = lineStr.trim();
                if("".equals(lineStr)) {
                    // If two following line feeds were found, the begin of a new
                    // paragraph was detected.
                    paragraphs.addElement(sb.toString());
                    sb = new StringBuffer();
                } else {
                    sb.append(lineStr);
                    sb.append(" ");
                }                                
            }
            paragraphs.addElement(sb.toString().trim());
        } catch(Exception e) {
            throwException("Could not set content of news file " + newsFile.getAbsoluteFilename() + ". " + e);            
        }                
                
        // set all other values
        newsFile.setNewsHeadline(headline);
        newsFile.setNewsShortText(shorttext);
        newsFile.setNewsText(paragraphs);
		newsFile.setNewsExternalLink(extlink);               
        newsFile.setNewsActive(C_NEWS_STATE_ACTIVE.equals(state));
        newsFile.write();
    }            
        
    /**
     * Create a new page file for displayin a given news content file.
     * 
     * @param cms A_CmsObject for accessing system resources.
     * @param newsFileName filename to be used
     * @param mastertemplate filename of the master template that should be used for displaying news.
     * @exception CmsException
     */
    private void createPageFile(A_CmsObject cms, String newsFileName, String mastertemplate) throws CmsException {
        
        // Create the news folder
        cms.createFolder(C_NEWS_FOLDER_PAGE, newsFileName);
                   
        // Create an index file in this folder
        String fullFilename = C_NEWS_FOLDER_PAGE + newsFileName + "/index.html";        
        CmsXmlControlFile pageFile = new CmsXmlControlFile();
        pageFile.createNewFile(cms, fullFilename, C_TYPE_NEWSPAGE_NAME);
        pageFile.setTemplateClass("com.opencms.template.CmsXmlTemplate");
        pageFile.setMasterTemplate(mastertemplate);
        pageFile.setElementClass(C_BODY_ELEMENT, "com.opencms.examples.news.CmsNewsTemplate");
        pageFile.setElementTemplate(C_BODY_ELEMENT, C_PATH_INTERNAL_TEMPLATES + "newsTemplate");
        pageFile.setElementParameter(C_BODY_ELEMENT, C_NEWS_PARAM_NEWSFOLDER, C_NEWS_FOLDER_CONTENT);
        pageFile.setElementParameter(C_BODY_ELEMENT, C_NEWS_PARAM_READ, newsFileName);
        pageFile.write();
        cms.chmod(fullFilename, C_ACCESS_DEFAULT_FLAGS);
        cms.unlockResource(fullFilename);
    }

    /**
     * Check if the current user has write access to the news folders
     * in the current project.
     * Used to decide if the "new article" should be shown.
     * @param cms Cms Object for accessing system resources.
     * @return <code>true</code> if the user has write access, <code>false</code> otherwise.  
     * @exception CmsException if check access failed.
     */
    private boolean checkWriteAccess(A_CmsObject cms) throws CmsException {
        CmsFolder pageFolder = null;
        CmsFolder contentFolder = null;
        try {
            pageFolder = cms.readFolder(C_NEWS_FOLDER_PAGE);
            contentFolder = cms.readFolder(C_NEWS_FOLDER_CONTENT);
        } catch(Exception e) {
            return false;
        }
        return cms.accessCreate(pageFolder) && cms.accessCreate(contentFolder);        
    }
}
