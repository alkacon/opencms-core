/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/examples/Attic/CmsExampleNav.java,v $
 * Date   : $Date: 2000/03/22 10:29:36 $
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

package com.opencms.examples;

import java.util.*;
import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.template.*;

import javax.servlet.http.*;

/**
 * This class demonstrates how a customized subtemplate can be 
 * included in any other template. The subtemplate contains
 * a simple navigation for the web site.
 * <P>
 * Normally, subtemplates are includued by writing a special
 * XML tag <code>&lt;ELEMENT name="subname1"/>&gt;</code>
 * at the place in the parent template, where the subtemplate
 * should be inserted. Outside the template definition
 * (i. e. outside the <code>&lt;TEMPLATE&gt;</code> tag) 
 * there must be defined, how the content of the subtemplate
 * should be generated. This can be done by adding 
 * <blockquote>
 *     <code>&lt;ELEMENTDEF name="subname1"/&gt;><BR>
 *     &lt;CLASS&gt;com.opencms.template.CmsXmlTemplate&lt;/CLASS&gt;
 *     &lt;CLASS&gt;</code>name of the subtemplate file<code>&lt;/CLASS&gt; 
 *     &lt;/ELEMENTDEF&gt; </code>
 * </blockquote>
 * 
 * This class, however, is a customized class for
 * generating dynamic output (e.g. a dynamic navigation like in this
 * example), since such a special content can not be created by the
 * standard class <code>CmsXmlTemplateClass</code>.
 * In this special case, the value of the element definition's <code>&lt;CLASS&gt;</code> 
 * tag must be replaced by the name of the customized class.
 * <P>
 * Every template may include as many subtemplates as needed.
 * Subtemplates itself can include other subtemplates using
 * the same technique, too.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.3 $ $Date: 2000/03/22 10:29:36 $
 */
public class CmsExampleNav extends CmsXmlTemplate implements I_CmsConstants {
        
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
     * Reads in the template file and starts the XML parser for the expected
     * content type <class>CmsExampleNavFile</code>
     * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param templateFile Filename of the template file.
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     */
    public CmsXmlTemplateFile getOwnTemplateFile(A_CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException {
        CmsExampleNavFile xmlTemplateDocument = new CmsExampleNavFile(cms, templateFile);       
        return xmlTemplateDocument;
    }        

    /**
     * Prints out the navigation.
     * <P>
     * The cms object will be asked for all subfolders of the root
     * folder. Subfolders will be scanned for their files. 
     * Subfolders and files having all metainformations required
     * for the navigation (navigation text and position) will be used to generate the output.
     * Any other folders/files will be ignored.
     * <p>
     * The layout of the navigation will be taken from
     * special XML data tags, defined in the template file for
     * the navigation. These tags can be accessed by 
     * a special content definition class that 
     * parses the XML file and provides special methods for
     * accessing these tags (<code>CmsExampleNavFile</code>).
     * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param userObj Hashtable with parameters.
     * @return String or byte[] with the content of this subelement.
     * @exception CmsException
     */
    public Object getNav(A_CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) 
            throws CmsException {
        // Reference to our own document.
        CmsExampleNavFile xmlTemplateDocument = (CmsExampleNavFile)doc;     
        
        String requestedUri = cms.getRequestContext().getUri();        
        String servletPath = ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getServletPath();
        StringBuffer result = new StringBuffer();        
        
        Vector allFolders = cms.getSubFolders("/");
        int numFolders = allFolders.size();
        int maxfolder = 0;

        String folderNames[] = new String[numFolders];
        String folderTitles[] = new String[numFolders];
        String folderPos[] = new String[numFolders];
                    
        // First scan all subfolders of the root folder
        // for any navigation metainformations and store
        // the maximum position found
        for(int i=0; i<numFolders; i++) {
            A_CmsResource currFolder = (A_CmsResource)allFolders.elementAt(i);
            String filename = currFolder.getAbsolutePath();
            String navpos = cms.readMetainformation(filename, C_METAINFO_NAVPOS);
            String navtext = cms.readMetainformation(filename, C_METAINFO_NAVTITLE);     
            if(currFolder.getState() != C_STATE_DELETED) { 
                // Only list folders in the nav bar if they are not deleted!
                if(navpos != null && navtext != null && (!"".equals(navpos)) && (!"".equals(navtext))
                     && ((!currFolder.getName().startsWith(C_TEMP_PREFIX)) || filename.equals(requestedUri))) {
                    folderNames[maxfolder] = filename;
                    folderTitles[maxfolder] = navtext;
                    folderPos[maxfolder] = navpos;
                    maxfolder++;
                }
            }        
        }

        // Sort all selected folders by position
        sort(cms, folderNames, folderTitles, folderPos, maxfolder);
        
        // The arrays folderNames and folderTitles now contain all folders
        // that should appear in the nav.
        // Loop through all folders and generate output        
        for(int i=0; i<maxfolder; i++) {
            String currFolder = folderNames[i];
            String navtext = folderTitles[i];                
            if(! requestedUri.startsWith(currFolder)) {
                result.append(xmlTemplateDocument.getOtherSectionNavEntry(servletPath + currFolder + "index.html", navtext));                
            } else {
                result.append(xmlTemplateDocument.getCurrentSectionNavEntry(servletPath + currFolder + "index.html", navtext));                
                
                // This is the currently requested folder.
                // Only for this folder we should display the list of files
                result.append(filesNav(cms, currFolder, xmlTemplateDocument));
            }                        
        }         
        return result.toString().getBytes();            
    }        

    /**
     * Generates the navigation entries of all files in a given subfolder.
     * Used by getNav().
     * @param cms Cms object for accessing system resources.
     * @param folderName Name of the folder the navigation should be generated for.
     * @param xmlTemplateDocument Navigation definition document.
     * @return String containing generated output.
     * @exception CmsException if file or folder access failed.
     */    
    protected String filesNav(A_CmsObject cms, String folderName, CmsExampleNavFile xmlTemplateDocument) throws CmsException {

        String requestedUri = cms.getRequestContext().getUri();        
        String servletPath = ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getServletPath();

        StringBuffer result = new StringBuffer();
        
        Vector allFiles = cms.getFilesInFolder(folderName);
        int numFiles = 0;
        if(allFiles != null) {
            numFiles = allFiles.size();
        }
        String fileNames[] = new String[numFiles];
        String fileTitles[] = new String[numFiles];
        String filePos[] = new String[numFiles];
        int maxindex = 0;
               
        // First scan all files in the given folder
        // for any navigation metainformations and store
        // the maximum position found
        for(int j=0; j<numFiles; j++) {
            A_CmsResource currFile = (A_CmsResource)allFiles.elementAt(j);
            String filename = currFile.getAbsolutePath();
            String navpos = cms.readMetainformation(filename, C_METAINFO_NAVPOS);
            String navtext = cms.readMetainformation(filename, C_METAINFO_NAVTITLE);     
            if(currFile.getState() != C_STATE_DELETED) { 
                // Only list files in the nav bar if they are not deleted!
                if(navpos != null && navtext != null && (!"".equals(navpos)) && (!"".equals(navtext))
                     && ((!currFile.getName().startsWith(C_TEMP_PREFIX)) || filename.equals(requestedUri))) {
                    fileNames[maxindex] = filename;
                    fileTitles[maxindex] = navtext;
                    filePos[maxindex] = navpos;
                    maxindex++;                                
                }
            }        
        }
                
        // Sort all selected files
        sort(cms, fileNames, fileTitles, filePos, maxindex);

        // The arrays fileNames and fileTitles now contain all navigation
        // elements with its positions as key.
        // So we can loop through the arrays and print out the nav elements
        for(int j=0; j<maxindex; j++) {
            String filename = fileNames[j];
            String navtext = fileTitles[j];
            if(filename.equals(requestedUri)) {
                result.append(xmlTemplateDocument.getCurrentNavEntry(navtext));                    
            } else {
                result.append(xmlTemplateDocument.getOtherNavEntry(servletPath + filename, navtext));
            }
        }            
        
        return new String(result);
    }
    
   /**
    * Sorts a set of three String arrays containing navigation information depending on 
    * their navigation positions.
    * @param cms Cms Object for accessign files.
    * @param filenames Array of filenames
    * @param nicenames Array of well formed navigation names
    * @param positions Array of navpostions
    */
    private void sort(A_CmsObject cms, String[] filenames, String[] nicenames,
                                 String[] positions, int max){
        // Sorting algorithm
        // This method uses an bubble sort, so replace this with something more
        // efficient
     
        for (int i=max-1;i>0;i--) {
            for (int j=0;j<i;j++) {
              
                float a=new Float(positions[j]).floatValue();
                float b=new Float(positions[j+1]).floatValue();
                if (a > b) {
                    String tempfilename= filenames[j];
                    String tempnicename = nicenames[j];
                    String tempposition = positions[j];
                    filenames[j]=filenames[j+1];
                    nicenames[j]=nicenames[j+1];
                    positions[j]=positions[j+1];
                    filenames[j+1]=tempfilename;
                    nicenames[j+1]=tempnicename;
                    positions[j+1]=tempposition;                    
                }
            }
        }
    }    
}
