/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/examples/news/Attic/CmsAdminArticleNew.java,v $
 * Date   : $Date: 2000/03/16 21:26:51 $
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
package com.opencms.examples.news;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;
import com.opencms.workplace.*;

import java.util.*;
import javax.servlet.http.*;

/**
 * Template class for displaying OpenCms workplace admin project screens.
 * <P>
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.3 $ $Date: 2000/03/16 21:26:51 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */
public class CmsAdminArticleNew extends CmsWorkplaceDefault implements I_CmsNewsConstants, I_CmsConstants {

    /** Name of the headline parameter in the HTTP get request */
    public static final String C_ARTICLENEW_HEADLINE = "headline";
    
    /** Name of the shorttext parameter in the HTTP get request */
    public static final String C_ARTICLENEW_SHORTTEXT = "shorttext";

    /** Name of the text parameter in the HTTP get request */
    public static final String C_ARTICLENEW_TEXT = "text";
    
    /** Name of the external link parameter in the HTTP get request */
    public static final String C_ARTICLENEW_EXTLINK = "extlink";
    
    /** Template selector of the "done" page */
    public static final String C_ARTICLENEW_DONE = "done";
    
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
        
		// read the parameters
        String newHeadline = (String)parameters.get(C_ARTICLENEW_HEADLINE);
        String newShorttext = (String)parameters.get(C_ARTICLENEW_SHORTTEXT);
        String newText = (String)parameters.get(C_ARTICLENEW_TEXT);
        String newExternalLink = (String)parameters.get(C_ARTICLENEW_EXTLINK);
		                
        // Calendar object used to get the actual date
        GregorianCalendar cal = new GregorianCalendar();

        // Get the currently logged in user
        A_CmsUser author = cms.getRequestContext().currentUser();        

        // Build the new article filename
        String dateFileText = getDateFileText(cal);        
        String newsNumber = this.getNewArticleNumber(cms, dateFileText);
        String initials =  getInitials(author);               
        String newsFileName = dateFileText + "-" + newsNumber + "-" + initials.toLowerCase();

        // Get the String for the author
        String authorText = null;
        String firstName = author.getFirstname();
        String lastName = author.getLastname();
        if((firstName == null || "".equals(firstName)) && (lastName == null || "".equals(lastName))) {
            authorText = initials;
        } else {            
            authorText = firstName + " " + lastName;
            authorText = authorText.trim();
            authorText = authorText + " (" + initials + ")";
        }
        
        // Get the Sting for the actual date
        String dateText = Utils.getNiceShortDate(cal.getTime().getTime());
        
		// is there any data? 
		if( (newHeadline != null) && (newShorttext != null) &&  (newText != null) && 
    			(newExternalLink != null)) {
            // yes! there really exist new data!
            
            createNewsFile(cms, newsFileName, authorText, dateText, newHeadline, newShorttext, newText, newExternalLink);
            createPageFile(cms, newsFileName);

            CmsXmlLanguageFile lang = new CmsXmlLanguageFile(cms);
            
            // Create task
            HttpServletRequest req = (HttpServletRequest)(cms.getRequestContext().getRequest().getOriginalRequest());
            String taskUrl = req.getScheme() + "://" + req.getHeader("HOST") + req.getServletPath() + C_NEWS_FOLDER_PAGE + newsFileName + "/index.html";
            String taskcomment = "<A HREF=\"javascript:openwinfull('" + taskUrl + "', 'preview', 0, 0);\"> " + taskUrl + "</A>";
            CmsTaskAction.create(cms, C_NEWS_USER, C_NEWS_ROLE, lang.getLanguageValue("task.label.news"), taskcomment, Utils.getNiceShortDate(new Date().getTime() + 345600000), "1", "", "", "", "");
                                             
            templateSelector = C_ARTICLENEW_DONE;                
		}
        
        // load the template file of the news admin screen
        CmsXmlWpTemplateFile xmlTemplateDocument = (CmsXmlWpTemplateFile)getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);
        xmlTemplateDocument.setXmlData("author", authorText);
        xmlTemplateDocument.setXmlData("date", dateText);
        
        // Now load the template file and start the processing
		return startProcessing(cms, xmlTemplateDocument, elementName, parameters, templateSelector);
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
    private void createNewsFile(A_CmsObject cms, String newsFileName, String author, String date, String headline, 
                                String shorttext, String text, String extlink) throws CmsException {
        String fullFilename = C_NEWS_FOLDER_CONTENT + newsFileName;
        CmsNewsTemplateFile newsTempDoc = new CmsNewsTemplateFile();
        newsTempDoc.createNewFile(cms, fullFilename, "plain");
              
        newsTempDoc.setNewsAuthor(author);
        newsTempDoc.setNewsDate(date);
        newsTempDoc.setNewsHeadline(headline);
        newsTempDoc.setNewsShortText(shorttext);
        newsTempDoc.setNewsText(text);
        newsTempDoc.setNewsExternalLink(extlink);               
        newsTempDoc.write();
        cms.chmod(fullFilename, C_ACCESS_DEFAULT_FLAGS);
        cms.unlockResource(fullFilename);
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
        pageFile.createNewFile(cms, fullFilename, "page");
        pageFile.setTemplateClass("com.opencms.template.CmsXmlTemplate");
        pageFile.setMasterTemplate("/content/templates/mfNewsTeaser");
        pageFile.setElementClass("body", "com.opencms.examples.news.CmsNewsTemplate");
        pageFile.setElementTemplate("body", C_PATH_INTERNAL_TEMPLATES + "newsTemplate");
        pageFile.setParameter("body", "newsfolder", C_NEWS_FOLDER_CONTENT);
        pageFile.setParameter("body", "read", newsFileName);
        pageFile.write();
        cms.chmod(fullFilename, C_ACCESS_DEFAULT_FLAGS);
        cms.unlockResource(fullFilename);
    }
}
