/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/web/Attic/CmsSimpleNav.java,v $
 * Date   : $Date: 2000/02/17 18:41:16 $
 * Version: $Revision: 1.1 $
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

package com.opencms.web;

import java.util.*;
import java.io.*;
import com.opencms.launcher.*;
import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.template.*;
import com.opencms.workplace.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import javax.servlet.http.*;

/**
 * Template class for displaying a simple navigation
 * used for the CeBIT online application form.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.1 $ $Date: 2000/02/17 18:41:16 $
 */
public class CmsSimpleNav extends CmsXmlTemplate implements I_CmsConstants {
    
    /** Describes the folder whose navigation should be built */
    static final String C_NAVFOLDER = "/cebitlive/";
    
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
     * Reads in the template file and starts the XML parser for the expected
     * content type <class>CmsSimpleNavFile</code>
     * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param templateFile Filename of the template file.
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     */
    public CmsXmlTemplateFile getOwnTemplateFile(A_CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException {
        CmsSimpleNavFile xmlTemplateDocument = new CmsSimpleNavFile(cms, templateFile);       
        return xmlTemplateDocument;
    }        

    /**
     * Handles any occurence of an <code>&lt;ELEMENT&gt;</code> tag.
     * <P>
     * Every XML template class should use CmsXmlTemplateFile as
     * the interface to the XML file. Since CmsXmlTemplateFile is
     * an extension of A_CmsXmlContent by the additional tag
     * <code>&lt;ELEMENT&gt;</code> this user method ist mandatory.
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

        // First create a copy of the parameter hashtable
        Hashtable parameterHashtable = (Hashtable)((Hashtable)userObject).clone();

        // Reference to our own document.
        CmsSimpleNavFile xmlTemplateDocument = (CmsSimpleNavFile)doc;     
        
        StringBuffer result = new StringBuffer();        
        Vector allFiles = cms.getFilesInFolder(C_NAVFOLDER);
        Hashtable sortedNav = new Hashtable();
        int numFiles = allFiles.size();
        int maxindex = 0;
        
        
        // First scan all files in the given folder
        // for any navigation metainformations and store
        // the maximum position found
        for(int i=0; i<numFiles; i++) {
            A_CmsResource currFile = (A_CmsResource)allFiles.elementAt(i);
            String filename = currFile.getAbsolutePath();
            String navpos = cms.readMetainformation(filename, C_METAINFO_NAVPOS);
            String navtext = cms.readMetainformation(filename, C_METAINFO_NAVTITLE);     
            if(navpos != null && navtext != null && (!"".equals(navpos)) && (!"".equals(navtext))) {
                Integer npValue = new Integer(navpos);
                int npIntValue = npValue.intValue();
                if(maxindex < npIntValue) {
                    maxindex = npIntValue;
                }
                sortedNav.put(npValue, filename);
            }        
        }
        
        // The Hashtable sortedNav now contains all navigation
        // elements with its positions as key.
        // So we can loop through all possible positions
        // and print out the nav elements
        for(int i=1; i<=maxindex; i++) {
            String filename = (String)sortedNav.get(new Integer(i));
            if(filename != null && !"".equals(filename)) {
                String navtext = cms.readMetainformation(filename, C_METAINFO_NAVTITLE);                 
                if(filename.equals("/cebitlive/cebitTest.html")) {
                    result.append(xmlTemplateDocument.getCurrentNavEntry(navtext));                    
                } else {
                    result.append(xmlTemplateDocument.getOtherNavEntry(filename, navtext));
                }
                
            }            
        }                        
        
        result.append(xmlTemplateDocument.getCurrentNavEntry("Titel alleine"));
        result.append(xmlTemplateDocument.getOtherNavEntry("newlink", "Title mit Link"));
                
        return result.toString().getBytes();            
    }        
}
