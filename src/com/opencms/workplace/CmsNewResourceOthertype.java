
/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsNewResourceOthertype.java,v $
* Date   : $Date: 2001/03/14 14:55:06 $
* Version: $Revision: 1.22 $
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

package com.opencms.workplace;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;
import javax.servlet.http.*;
import java.util.*;

/**
 * Template class for displaying the new resource screen for a new simple document
 * of the OpenCms workplace.<P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 *
 * @author Michael Emmerich
 * @version $Revision: 1.22 $ $Date: 2001/03/14 14:55:06 $
 */

public class CmsNewResourceOthertype extends CmsWorkplaceDefault implements I_CmsWpConstants,I_CmsConstants {


    /** Definition of the Datablock RADIOSIZE */
    private final static String C_RADIOSIZE = "RADIOSIZE";


    /** Vector containing all names of the radiobuttons */
    private Vector m_names = null;


    /** Vector containing all links attached to the radiobuttons */
    private Vector m_values = null;

    /**
     * Overwrites the getContent method of the CmsWorkplaceDefault.<br>
     * Gets the content of the new resource othertype template and processed the data input.
     * @param cms The CmsObject.
     * @param templateFile The lock template file
     * @param elementName not used
     * @param parameters Parameters of the request and the template.
     * @param templateSelector Selector of the template tag to be displayed.
     * @return Bytearry containing the processed data of the template.
     * @exception Throws CmsException if something goes wrong.
     */

    public byte[] getContent(CmsObject cms, String templateFile, String elementName,
            Hashtable parameters, String templateSelector) throws CmsException {
        System.err.println("******");
	Enumeration enu = parameters.keys();
	while (enu.hasMoreElements()) {
		String key = (String) enu.nextElement();
		String values = (String) parameters.get(key);
		System.err.println(key + " : " + values);
	}
	System.err.println("-------");
        // the template to be displayed
        String template = null;
        String filename = null;
        String title = null;
        String keywords = null;
        String description = null;
        String foldername = null;
        String type = null;
        I_CmsSession session = cms.getRequestContext().getSession(true);

        // clear session values on first load
        String initial = (String)parameters.get(C_PARA_INITIAL);
        if(initial != null) {

            // remove all session values
            session.removeValue(C_PARA_FILE);
            session.removeValue(C_PARA_TITLE);
            session.removeValue(C_PARA_KEYWORDS);
            session.removeValue(C_PARA_DESCRIPTION);
            session.removeValue("lasturl");
        }

        // get the current phase of this wizard
        String step = cms.getRequestContext().getRequest().getParameter("step");
        if(step != null) {

            // step 1 - show the final selection screen
            if(step.equals("1")) {
                template = "step1";
                filename = cms.getRequestContext().getRequest().getParameter(C_PARA_FILE);
                session.putValue(C_PARA_FILE, filename);
                title = cms.getRequestContext().getRequest().getParameter(C_PARA_TITLE);
                session.putValue(C_PARA_TITLE, title);
                keywords = (String)parameters.get(C_PARA_KEYWORDS);
                session.putValue(C_PARA_KEYWORDS, keywords);
                description = (String)parameters.get(C_PARA_DESCRIPTION);
                session.putValue(C_PARA_DESCRIPTION, description);
            }
            else {
                if(step.equals("2")) {

                    // step 2 - create the file
                    // get folder- and filename
                    foldername = (String)session.getValue(C_PARA_FILELIST);
                    title = (String)session.getValue(C_PARA_TITLE);
                    if(foldername == null) {
                        foldername = cms.rootFolder().getAbsolutePath();
                    }
                    filename = (String)session.getValue(C_PARA_FILE);
                    type = (String)cms.getRequestContext().getRequest().getParameter("type");
                    keywords = (String)session.getValue(C_PARA_KEYWORDS);
                    description = (String)session.getValue(C_PARA_DESCRIPTION);
                    // create the new file
                    cms.createFile(foldername, filename, new byte[0], type);

                    // lock the new file
                    cms.lockResource(foldername + filename);
                    cms.writeProperty(foldername + filename, C_PROPERTY_TITLE, title);
                    cms.writeProperty(foldername + filename, C_PROPERTY_KEYWORDS, keywords);
                    cms.writeProperty(foldername + filename, C_PROPERTY_DESCRIPTION, description);

                    // remove values from session
                    session.removeValue(C_PARA_FILE);
                    session.removeValue(C_PARA_TITLE);
                    session.removeValue(C_PARA_KEYWORDS);
                    session.removeValue(C_PARA_DESCRIPTION);

                    // TODO: ErrorHandling
                    // now return to filelist
                    try {
                        cms.getRequestContext().getResponse().sendCmsRedirect(getConfigFile(cms).getWorkplaceActionPath() + C_WP_EXPLORER_FILELIST);
                    }
                    catch(Exception e) {
                        throw new CmsException("Redirect fails :" + getConfigFile(cms).getWorkplaceActionPath() + C_WP_EXPLORER_FILELIST, CmsException.C_UNKNOWN_EXCEPTION, e);
                    }
                    return null;
                }
            }
        }
        else {
            session.removeValue(C_PARA_FILE);
        }

        // get the document to display
        CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms, templateFile);

        // set the size of the radiobox entrys
        getResources(cms, null, null, null, null, null);
        if(m_names != null) {
            xmlTemplateDocument.setData(C_RADIOSIZE, new Integer(m_names.size()).toString());
        }

        // process the selected template
        return startProcessing(cms, xmlTemplateDocument, "", parameters, template);
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

    public int getResources(CmsObject cms, CmsXmlLanguageFile lang, Vector names,
            Vector values, Vector descriptions, Hashtable parameters) throws CmsException {
        I_CmsSession session = cms.getRequestContext().getSession(true);
        String filename = (String)session.getValue(C_PARA_FILE);
        String resType = new String();
        if(filename != null) {
            String suffix = filename.substring(filename.lastIndexOf('.') + 1);
            suffix = suffix.toLowerCase(); // file extension of filename

            // read the known file extensions from the database
            Hashtable extensions = cms.readFileExtensions();
            if(extensions != null) {
                resType = (String)extensions.get(suffix);
            }
            if(resType == null) {
                resType = "";
            }
        }
        else {
            resType = "";
        }
        int ret = 0;

        // Check if the list of available resources is not yet loaded from the workplace.ini
        if(m_names == null || m_values == null) {
            m_names = new Vector();
            m_values = new Vector();
            CmsXmlWpConfigFile configFile = new CmsXmlWpConfigFile(cms);
            configFile.getWorkplaceIniData(m_names, m_values, "OTHERRESOURCES", "RESOURCE");
        }

        // Check if the temportary name and value vectors are not initialized, create

        // them if nescessary.
        if(names == null) {
            names = new Vector();
        }
        if(values == null) {
            values = new Vector();
        }
        if(descriptions == null) {
            descriptions = new Vector();
        }

        // OK. Now m_names and m_values contain all available
        // resource information.
        // Loop through the vectors and fill the result vectors.
        int numViews = m_names.size();
        for(int i = 0;i < numViews;i++) {
            String loopValue = (String)m_values.elementAt(i);
            String loopName = (String)m_names.elementAt(i);
            values.addElement(loopValue);
            names.addElement("file_" + loopName);
            String descr;
            if(lang != null) {
                descr = lang.getLanguageValue("fileicon." + loopName);
            }
            else {
                descr = loopName;
            }
            descriptions.addElement(descr);
            if(resType.equals(loopName)) {

                // known file extension
                ret = i;
            }
        }
        return ret;
    }

    /**
     * Indicates if the results of this class are cacheable.
     *
     * @param cms CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return <EM>true</EM> if cacheable, <EM>false</EM> otherwise.
     */

    public boolean isCacheable(CmsObject cms, String templateFile, String elementName,
            Hashtable parameters, String templateSelector) {
        return false;
    }

    /**
     * Sets the value of the new file input field of dialog.
     * This method is directly called by the content definiton.
     * @param Cms The CmsObject.
     * @param lang The language file.
     * @param parameters User parameters.
     * @return Value that is set into the new file dialod.
     * @exception CmsExeption if something goes wrong.
     */

    public String setValue(CmsObject cms, CmsXmlLanguageFile lang, Hashtable parameters) throws CmsException {
        I_CmsSession session = cms.getRequestContext().getSession(true);

        // get a previous value from the session
        String filename = (String)session.getValue(C_PARA_FILE);
        if(filename == null) {
            filename = "";
        }
        return filename;
    }
}
