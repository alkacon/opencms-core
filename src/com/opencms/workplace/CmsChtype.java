/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsChtype.java,v $
 * Date   : $Date: 2004/06/25 16:32:34 $
 * Version: $Revision: 1.38 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2001  The OpenCms Group
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about OpenCms, please see the
 * OpenCms Website: http://www.opencms.org 
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.opencms.workplace;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsRequestContext;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.workplace.CmsWorkplaceAction;

import com.opencms.core.I_CmsSession;
import com.opencms.legacy.CmsXmlTemplateLoader;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

/**
 * Template class for displaying the type screen of the OpenCms workplace.<p>
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.38 $ $Date: 2004/06/25 16:32:34 $
 */
public class CmsChtype extends CmsWorkplaceDefault {

    /** Definition of the Datablock RADIOSIZE */
    private final static String C_RADIOSIZE = "RADIOSIZE";

    /** Vector containing all names of the radiobuttons */
    private Vector m_names = null;

    /** Vector containing all links attached to the radiobuttons */
    private Vector m_values = null;

    /**
	 * Gets the content of the chtype template and processes the data input.<p>
     * 
     * @param cms The CmsObject.
     * @param templateFile The chtype template file
     * @param elementName not used
     * @param parameters Parameters of the request and the template.
     * @param templateSelector Selector of the template tag to be displayed.
	 * @return Bytearray containing the processed data of the template.
	 * @throws CmsException if something goes wrong.
     */
    public byte[] getContent(CmsObject cms, String templateFile, String elementName,
            Hashtable parameters, String templateSelector) 
    throws CmsException {
        CmsRequestContext requestContext = cms.getRequestContext();
        I_CmsSession session = CmsXmlTemplateLoader.getSession(requestContext, true);

        // the template to be displayed
        String template = null;

        // clear session values on first load
        String initial = (String)parameters.get(C_PARA_INITIAL);
        if (initial != null) {

            // remove all session values
            session.removeValue(C_PARA_RESOURCE);
            session.removeValue("lasturl");
        }

        // get the lasturl parameter
        String lasturl = getLastUrl(cms, parameters);
        String newtype = (String)parameters.get(C_PARA_NEWTYPE);

        // get the filename
        String filename = (String)parameters.get(C_PARA_RESOURCE);
        if (filename != null) {
            session.putValue(C_PARA_RESOURCE, filename);
        }
        filename = (String)session.getValue(C_PARA_RESOURCE);
        CmsFile file = (CmsFile)cms.readFileHeader(filename);

        // check if the newtype parameter is available. This parameter is set when
        // the new file type is selected
        if (newtype != null) {

            // get the new resource type
            
            int type = OpenCms.getResourceManager().getResourceType(newtype).getTypeId(); 
            
            // check the autolock resource setting and lock the resource if necessary
            if (OpenCms.getWorkplaceManager().autoLockResources()) {
                if (cms.getLock(filename).isNullLock()) {
                    // resource is not locked, lock it automatically
                    cms.lockResource(filename);
                }       
            }
            
            // change the file type
            cms.chtype(cms.readAbsolutePath(file), type);
            session.removeValue(C_PARA_RESOURCE);

            // return to filelist
            try {
                if (lasturl == null || "".equals(lasturl)) {
                    CmsXmlTemplateLoader.getResponse(requestContext).sendCmsRedirect(getConfigFile(cms).getWorkplaceActionPath()
                    + CmsWorkplaceAction.getExplorerFileUri(CmsXmlTemplateLoader.getRequest(cms.getRequestContext()).getOriginalRequest()));
                } else {
                    CmsXmlTemplateLoader.getResponse(requestContext).sendRedirect(lasturl);
                }
            } catch (Exception e) {
                throw new CmsException("Redirect fails :" + getConfigFile(cms).getWorkplaceActionPath()
                        + CmsWorkplaceAction.getExplorerFileUri(CmsXmlTemplateLoader.getRequest(cms.getRequestContext()).getOriginalRequest()), CmsException.C_UNKNOWN_EXCEPTION, e);
            }
            return null;
        }
        CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms, templateFile);

        // set all required datablocks
        xmlTemplateDocument.setData("OWNER", "" /* Utils.getFullName(cms.readOwner(file)) */);
        xmlTemplateDocument.setData("GROUP", "" /* cms.readGroup(file).getName() */);
        xmlTemplateDocument.setData("FILENAME", file.getName());
        getResources(cms, null, null, null, null, null);
        if (m_names != null) {
            xmlTemplateDocument.setData(C_RADIOSIZE, new Integer(m_names.size()).toString());
        }

        // process the selected template
        return startProcessing(cms, xmlTemplateDocument, "", parameters, template);
    }

    /**
     * Gets the resources displayed in the Radiobutton group on the chtype dialog.<p>
     * 
     * @param cms The CmsObject.
     * @param lang The langauge definitions.
     * @param names The names of the new rescources (used for optional images).
     * @param values The links that are connected with each resource.
     * @param descriptions Description that will be displayed for the new resource.
     * @param parameters Hashtable of parameters (not used yet).
	 * @return The vectors names and values filled with the information found in the
     * workplace.ini.
	 * @throws CmsException if something goes wrong.
     */
    public void getResources(CmsObject cms, CmsXmlLanguageFile lang, Vector names,
            Vector values, Vector descriptions, Hashtable parameters) 
    throws CmsException {

        // check if the list of available resources is not yet loaded from the workplace.ini
        if(m_names == null || m_values == null) {
            m_names = new Vector();
            m_values = new Vector();
            CmsXmlWpConfigFile configFile = new CmsXmlWpConfigFile(cms);
            configFile.getWorkplaceIniData(m_names, m_values, "RESOURCETYPES", "RESOURCE");
        }

        // check if the temportary name and value vectors are not initialized, 
        // create them if nescessary.
        if(names == null) names = new Vector();
        if(values == null) values = new Vector();
        if(descriptions == null) descriptions = new Vector();

        // now m_names and m_values contain all available resource information
        // Loop through the vectors and fill the result vectors
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
        }
    }

    /**
     * Indicates if the results of this class are cacheable,
     * which is not the case for this class.<p>
     *
     * @param cms CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return <code>false</code>
     */
    public boolean isCacheable(CmsObject cms, String templateFile, String elementName,
        Hashtable parameters, String templateSelector) {
        return false;
    }
}
