/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/examples/Attic/CmsExampleNav.java,v $
 * Date   : $Date: 2000/03/16 13:42:59 $
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
 * @version $Revision: 1.2 $ $Date: 2000/03/16 13:42:59 $
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
        Hashtable sortedFolders = new Hashtable();
        int numFolders = allFolders.size();
        int maxfolder = 0;

        
        // First scan all subfolders of the root folder
        // for any navigation metainformations and store
        // the maximum position found
        for(int i=0; i<numFolders; i++) {
            A_CmsResource currFolder = (A_CmsResource)allFolders.elementAt(i);
            String filename = currFolder.getAbsolutePath();
            String navpos = cms.readMetainformation(filename, C_METAINFO_NAVPOS);
            String navtext = cms.readMetainformation(filename, C_METAINFO_NAVTITLE);     
            if(currFolder.getState() != C_STATE_DELETED) { 
                // Only list files in the nav bar if they are not deleted!
                if(navpos != null && navtext != null && (!"".equals(navpos)) && (!"".equals(navtext))
                     && ((!currFolder.getName().startsWith(C_TEMP_PREFIX)) || filename.equals(requestedUri))) {
                    Integer npValue = new Integer(navpos);
                    int npIntValue = npValue.intValue();
                    if(maxfolder < npIntValue) {
                        maxfolder = npIntValue;
                    }
                    sortedFolders.put(npValue, filename);
                }
            }        
        }
        
        // The Hashtable sortedFolders now contains all folders
        // that should appear in the nav.
        
        for(int i=1; i<=maxfolder; i++) {
            String currFolder = (String)sortedFolders.get(new Integer(i));
            if(currFolder != null && !"".equals(currFolder)) {
                String navtext = cms.readMetainformation(currFolder, C_METAINFO_NAVTITLE);                 
                //if(! currFolder.equals("/" + requestedFolder + "/")) {
                if(! requestedUri.startsWith(currFolder)) {
                    result.append(xmlTemplateDocument.getOtherSectionNavEntry(servletPath + currFolder + "index.html", navtext));                
                } else {
                    result.append(xmlTemplateDocument.getCurrentSectionNavEntry(servletPath + currFolder + "index.html", navtext));                
                
                    // This is the currently requested folder.
                    // Only for this folder we should display the list of files
                    Vector allFiles = cms.getFilesInFolder(currFolder);
                    Hashtable sortedNav = new Hashtable();
                    int numFiles = 0;
                    if(allFiles != null) {
                        numFiles = allFiles.size();
                    }
                    int maxindex = 0;
               
                    // First scan all files in the given folder
                    // for any navigation metainformations and store
                    // the maximum position found
                    for(int j=0; j<numFiles; j++) {
                        A_CmsResource currFile = (A_CmsResource)allFiles.elementAt(j);
                        String filename = currFile.getAbsolutePath();
                        String navpos = cms.readMetainformation(filename, C_METAINFO_NAVPOS);
                        navtext = cms.readMetainformation(filename, C_METAINFO_NAVTITLE);     
                        if(currFile.getState() != C_STATE_DELETED) { 
                            // Only list files in the nav bar if they are not deleted!
                            if(navpos != null && navtext != null && (!"".equals(navpos)) && (!"".equals(navtext))
                                 && ((!currFile.getName().startsWith(C_TEMP_PREFIX)) || filename.equals(requestedUri))) {
                                Integer npValue = new Integer(navpos);
                                int npIntValue = npValue.intValue();
                                if(maxindex < npIntValue) {
                                    maxindex = npIntValue;
                                }
                                sortedNav.put(npValue, filename);
                            }
                        }        
                    }

                    // The Hashtable sortedNav now contains all navigation
                    // elements with its positions as key.
                    // So we can loop through all possible positions
                    // and print out the nav elements
                    for(int j=1; j<=maxindex; j++) {
                        String filename = (String)sortedNav.get(new Integer(j));
                        if(filename != null && !"".equals(filename)) {
                            navtext = cms.readMetainformation(filename, C_METAINFO_NAVTITLE);                 
                            if(filename.equals(requestedUri)) {
                                result.append(xmlTemplateDocument.getCurrentNavEntry(navtext));                    
                            } else {
                                result.append(xmlTemplateDocument.getOtherNavEntry(servletPath + filename, navtext));
                            }
                        }                
                    }            
                }
            }                        
        }         
        return result.toString().getBytes();            
    }        
}
