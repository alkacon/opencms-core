/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/xmlmodules/Attic/A_CmsModuleAdmin.java,v $
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
import com.opencms.util.*;
import com.opencms.template.*;
import com.opencms.workplace.*;

import java.util.*;
import java.io.*;
import javax.servlet.http.*;

import org.apache.xml.serialize.*;

/**
 * Template class to handle the basics of administration for
 *  XML-based modules. 
 * <p>
 * Provides methods for both displaying and editing articles.
 * 
 * @author Matthias Schreiber
 * @version $Revision: 1.2 $ $Date: 2000/07/11 15:01:18 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */
public abstract class A_CmsModuleAdmin extends CmsWorkplaceDefault implements I_CmsConstants, I_CmsModuleConstants, I_CmsFileListUsers {
        
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
        return false;
    }    

    
	/**
     * Gets the content of a defined section in a given template file and its subtemplates
     * with the given parameters. 
     * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param templateFile Filename of the template file.
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     */
    public abstract byte[] getContent(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) 
			throws CmsException;

    
	/**
     * Used for filling the input field <em>Date</em> in the news editor.
     * @param cms Cms object for accessing system resources.
     * @param lang Current language file.
     * @param parameters User parameters.
     * @return String containing the date.
     */
    public String getDate(CmsObject cms, CmsXmlLanguageFile lang, Hashtable parameters) {
        String result = (String)parameters.get(C_XML_DATE);
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
    public String getHeadline(CmsObject cms, CmsXmlLanguageFile lang, Hashtable parameters) {
        String result = (String)parameters.get(C_XML_HEADLINE);
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
    public String getText(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) {
        Hashtable parameters = (Hashtable)userObject;
        String result = (String)parameters.get(C_XML_TEXT);
        if(result == null) {
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
    public Integer getStates(CmsObject cms, CmsXmlLanguageFile lang, Vector names, Vector values, Vector descriptions, Hashtable parameters) 
           throws CmsException { 
        Boolean state = (Boolean)parameters.get(C_PARAM_STATE);        
        names.addElement("");
        names.addElement("");
        values.addElement(C_STATE_ACTIVE);     
        values.addElement(C_STATE_INACTIVE);
        descriptions.addElement(lang.getLanguageValue(C_LANG_LABEL + "." + C_STATE_ACTIVE));
        descriptions.addElement(lang.getLanguageValue(C_LANG_LABEL + "." + C_STATE_INACTIVE)); 
        if(state == null || state.equals(Boolean.TRUE)) {
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
 
	public abstract Vector getFiles(CmsObject cms) throws CmsException;
       
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
    public int modifyDisplayedColumns(CmsObject cms, int prefs) {
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
    public void getCustomizedColumnValues(CmsObject cms, CmsXmlWpTemplateFile filelistTemplate, CmsResource res, CmsXmlLanguageFile lang) 
        throws CmsException { 
        String state = lang.getLanguageValue(C_LANG_LABEL + ".notavailable");
        String author = state;
        String name = null;
        if(res instanceof CmsFile) { 
            A_CmsModuleObject contentFile = getContentFile(cms, res);
            state = contentFile.isActive() ? lang.getLanguageValue(C_LANG_LABEL + "." + C_STATE_ACTIVE) : lang.getLanguageValue(C_LANG_LABEL + "." + C_STATE_INACTIVE);
            author = Encoder.escapeXml(contentFile.getAuthor());
            name = Encoder.escapeXml(contentFile.getHeadline());
        } 
        filelistTemplate.setData(C_STATE_VALUE, state);
        filelistTemplate.setData(C_AUTHOR_VALUE, author);   
        if(name != null) {
             filelistTemplate.setData(C_FILELIST_NAME_VALUE, name);                    
        } 
    }    

    
    /** Create the task for the new article.
     * @param cms A_CmsObject for accessing system resources.
     * @param newsFileName File name of the news article, used to generate a link.
     * @param taskuser User of the new task
     * @param taskgroup Group of the new task. 
     * @exception CmsException
     */
    protected void makeTask(CmsObject cms, String fileName, String folderPage, String taskuser, String taskgroup, String tasklabel) throws CmsException {
        CmsXmlLanguageFile lang = new CmsXmlLanguageFile(cms);
        HttpServletRequest req = (HttpServletRequest)(cms.getRequestContext().getRequest().getOriginalRequest());
        String taskUrl = req.getScheme() + "://" + req.getHeader("HOST") + req.getServletPath() + folderPage + fileName + "/index.html";
        String taskcomment = "<A HREF=\"javascript:openwinfull('" + taskUrl + "', 'preview', 0, 0);\"> " + taskUrl + "</A>";
        CmsTaskAction.create(cms, taskuser, taskgroup, lang.getLanguageValue(tasklabel), taskcomment, Utils.getNiceShortDate(new Date().getTime() + 345600000), "1", "", "", "", "");                
    }
    
    /**
     * Get the new article number by scanning all existing articles
     * of the given date and inreasing the maximum number.
     * @param cms A_CmsObject for accessing system resources
     * @param dateFileText String containing the date used in news file names
     * @return new article number.
     * @exception CmsException
     */
    protected String getNewArticleNumber(CmsObject cms, String dateFileText, String folderContent) throws CmsException {
        String numberText = null;
        
        // Get all files in the news folder
        Vector allNews = cms.getFilesInFolder(folderContent);
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
    protected String getInitials(CmsUser author) {
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
    protected String getDateFileText(GregorianCalendar cal) {        
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
     * Get the corresponding news content file for a given newspage file.
     * @param file File object of the newspage file.
     * @param cms A_CmsObject for accessing system resources.
     * @return CmsNewsContentFile object of the corresponding news content file.
     * @exception CmsException if file access failed.
     */
    protected abstract A_CmsModuleObject getContentFile(CmsObject cms, CmsResource file)
			throws CmsException;
	
    
    /**
     * Create a content file. File flags setted in the user preferences
     * will be overridden to system default flags.
     * 
     * @param cms A_CmsObject for accessing system resources.
     * @param fileName filename to be used
     * @return Instance of a content file.
     * @exception CmsException
     */
    protected abstract A_CmsModuleObject createFile(CmsObject cms, String fileName)
			throws CmsException;
        
   /**
     * Create a new page file for displaying a given content file.
     * 
     * @param cms A_CmsObject for accessing system resources.
     * @param fileName filename to be used
     * @param mastertemplate filename of the master template that should be used for displaying the content.
     * @exception CmsException
     */
    protected abstract void createPageFile(CmsObject cms, String fileName, String mastertemplate)
			throws CmsException;

    /**
     * Check if the current user has write access to the module folder
     * and its subfolders in the current project.
     * Used to decide if the "new article" should be displayed.
     * @param cms Cms Object for accessing system resources.
     * @return <code>true</code> if the user has write access, <code>false</code> otherwise.  
     * @exception CmsException if check access failed.
     */
    protected boolean checkWriteAccess(CmsObject cms, String folderPage, String folderContent) throws CmsException {
        CmsFolder pageFolder = null;
        CmsFolder contentFolder = null;
        try {
            pageFolder = cms.readFolder(folderPage);
            contentFolder = cms.readFolder(folderContent);
        } catch(Exception e) {
            return false;
        }
        return cms.accessCreate(pageFolder) && cms.accessCreate(contentFolder);        
    }
	
	/**
     * Gets a vector with all paragraphs of the <code>text</code>.
     * <p>
     * A paragraph is detected when the <code>text</code> contains 2 or
     * more following line feeds. 
     * @param obj CmsNewsObject for a certain content file.
     * @param text The text input string
     * @return Vector with text paragraphs.
     * @exception CmsExecption
     */
	protected Vector getTextParagraphs (A_CmsModuleObject obj, String text) throws CmsException {
        BufferedReader br = new BufferedReader(new StringReader(text));
        String lineStr = null;
        StringBuffer sb = new StringBuffer();
        Vector paragraphs = new Vector();
		// Divide the text into separate lines.
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
            throwException("Could not set content of news file " + obj.getAbsoluteFilename() + ". " + e);            
        }  
		return paragraphs; 
	}
	
}
