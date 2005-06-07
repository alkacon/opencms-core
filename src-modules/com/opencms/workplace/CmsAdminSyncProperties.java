/*
* File   : $Source: /alkacon/cvs/opencms/src-modules/com/opencms/workplace/Attic/CmsAdminSyncProperties.java,v $
* Date   : $Date: 2005/06/07 15:03:46 $
* Version: $Revision: 1.3 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
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

import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSet;
import org.opencms.synchronize.CmsSynchronizeSettings;

import com.opencms.core.I_CmsSession;
import com.opencms.legacy.CmsXmlTemplateLoader;
import com.opencms.template.CmsXmlTemplateFile;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Template class for displaying OpenCms workplace administration synchronisation properties.
 *
 * Creation date: ()
 * @author Edna Falkenhan
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */
public class CmsAdminSyncProperties extends CmsWorkplaceDefault {

    private final String C_STEP = "step";
    private final String C_SYNCPROJECT = "syncproject";
    private final String C_SYNCPATH = "syncpath";
    private final String C_SYNCRESOURCES = "syncresources";
    private final String C_ADDFOLDER = "addfolder";

    /**
     * Gets the content of a defined section in a given template file and its subtemplates
     * with the given parameters.
     *
     * @see #getContent(CmsObject, String, String, Hashtable, String)
     * @param cms CmsObject Object for accessing system resources.
     * @param templateFile Filename of the template file.
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     */
    public byte[] getContent(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException {
        if(CmsLog.getLog(this).isDebugEnabled() && C_DEBUG) {
            CmsLog.getLog(this).debug("Getting content of element " + ((elementName==null)?"<root>":elementName));
            CmsLog.getLog(this).debug("Template file is: " + templateFile);
            CmsLog.getLog(this).debug("Selected template section is: " + ((templateSelector==null)?"<default>":templateSelector));
        }

        CmsXmlTemplateFile templateDocument = getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);
        CmsRequestContext reqCont = cms.getRequestContext();
        I_CmsSession session = CmsXmlTemplateLoader.getSession(reqCont, true);
        CmsXmlLanguageFile lang = new CmsXmlLanguageFile(cms);

        String syncPath = new String();
        String allResources = new String();

        // clear session values on first load
        String step = (String)parameters.get(C_STEP);
        syncPath = (String)parameters.get(C_SYNCPATH);        
        allResources = (String)parameters.get(C_SYNCRESOURCES);

        if(step == null) {
            // if the dialog was opened the first time remove the session values
            // and get the id of the current project
            if(session.getValue(C_STEP) == null){
                // remove all session values
                session.removeValue(C_SYNCPATH);
                session.removeValue(C_SYNCRESOURCES);
                session.removeValue(C_ADDFOLDER);
                session.removeValue("lasturl");
                session.putValue(C_STEP, "nextstep");
            }
        } else {
            if("OK".equalsIgnoreCase(step)) {
                syncPath = (String)parameters.get(C_SYNCPATH);
                allResources = (String)parameters.get(C_SYNCRESOURCES);
                // the form has just been submitted, store the data in the session
                if(((syncPath == null) || syncPath.equals("")) ||
                    ((allResources == null) || allResources.equals(""))) {
                    templateSelector = "datamissing";
                } else {
                    // all the required data has been entered
                    session.putValue(C_SYNCPATH, syncPath);
                    session.putValue(C_SYNCRESOURCES, allResources);
                    // 'allResources' has the "form res1;res2;...resk;"
                    // this is because the simpler 'getParameterValues' method doesn't work with Silverstream
                    Vector folders = parseResources(allResources);
                    if (folders.size() >= 1) {
                        allResources = (String)folders.elementAt(0);
                    }
                    
                    // modify the foldername if nescessary (the root folder is always given
                    // as a nice name)
                    if(lang.getLanguageValue("title.rootfolder").equals(allResources)) {
                        allResources = "/";
                    }

                    // numRes = folders.size(); // could have been changed
                    // check if all the resources are writeable
                    // if not, return a message
                    Vector notWriteable = new Vector();
                    if(!isReadable(cms, allResources)) {
                        notWriteable.addElement(allResources);
                        templateSelector = "errorsyncproperties";
                    }

                    if("errorsyncproperties".equals(templateSelector)){
                        // at least one of the choosen folders was not writeable
                        templateDocument.setData("details", "The following folders were not writeable:"
                                + notWriteable.toString());
                    }
                }
                if(templateSelector == null || "".equals(templateSelector)){

                    // now update the settings
                    CmsSynchronizeSettings settings = new CmsSynchronizeSettings();
                    settings.setEnabled(true);
                    settings.setDestinationPathInRfs(syncPath);
                    ArrayList list = new ArrayList();
                    list.add(allResources);
                    settings.setSourcePathInVfs(list);
                    
                    CmsUserSettings userSettings = new CmsUserSettings(cms.getRequestContext().currentUser());
                    userSettings.setSynchronizeSettings(settings);
                    userSettings.save(cms);

                    templateSelector = "done";
                    
                    // remove the values from the session
                    session.removeValue(C_STEP);
                }
            } else if("fromerrorpage".equals(step)) {
                // after an error fill in the data from the session into the template
                templateDocument.setData(C_SYNCPROJECT, "" + cms.getRequestContext().currentProject().getId());
                templateDocument.setData(C_SYNCPATH, (String)session.getValue(C_SYNCPATH));
                templateDocument.setData(C_ADDFOLDER, "");
                templateDocument.setData(C_SYNCRESOURCES, (String)session.getValue(C_SYNCRESOURCES));
                templateSelector = "";
            } else if("cancel".equals(step)){
                // remove the values from the session
                session.removeValue(C_STEP);
                templateSelector = "done";
            }
        }
        // if there are still values in the session (like after an error), use them
        if((syncPath == null) || ("".equals(syncPath))) {
            syncPath = (String)session.getValue(C_SYNCPATH);
        }
        if((allResources == null) || ("".equals(allResources))) {
            allResources = (String)session.getValue(C_SYNCRESOURCES);
        }

        CmsUserSettings userSettings = new CmsUserSettings(cms.getRequestContext().currentUser());
        CmsSynchronizeSettings settings = userSettings.getSynchronizeSettings();
        if (settings == null) {
            settings = new CmsSynchronizeSettings();
        }
        
        if((syncPath == null) || ("".equals(syncPath))) {
            syncPath = settings.getDestinationPathInRfs();
            if(syncPath == null){
                syncPath = "";
            }
        }
        if((allResources == null) || ("".equals(allResources))) {
            allResources = (String)settings.getSourcePathInVfs().get(0);
            if (allResources == null) {
                allResources = "";
            }
            // remove the last semikolon
            if (allResources.endsWith(";")){
                allResources = allResources.substring(0,allResources.lastIndexOf(";"));
            }
        }

        templateDocument.setData(C_SYNCPROJECT, "" + cms.getRequestContext().currentProject().getId());
        templateDocument.setData(C_SYNCRESOURCES, allResources);
        templateDocument.setData(C_SYNCPATH, syncPath);
        session.putValue(C_SYNCPATH, syncPath);
        session.putValue(C_SYNCRESOURCES, allResources);

        // Now load the template file and start the processing
        return startProcessing(cms, templateDocument, elementName, parameters, templateSelector);
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
    public boolean isCacheable(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
        return false;
    }

    /**
     * Gets the resources.
     * <P>
     * The given vectors <code>names</code> and <code>values</code> will
     * be filled with the appropriate information to be used for building
     * a select box.
     *
     * @param cms CmsObject Object for accessing system resources.
     * @param names Vector to be filled with the appropriate values in this method.
     * @param values Vector to be filled with the appropriate values in this method.
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @return Index representing the current value in the vectors.
     * @throws CmsException
     */

    public Integer getResources(CmsObject cms, CmsXmlLanguageFile lang, Vector names,
            Vector values, Hashtable parameters) throws CmsException {

        I_CmsSession session = CmsXmlTemplateLoader.getSession(cms.getRequestContext(), true);
        String enteredResources = (String)session.getValue(C_SYNCRESOURCES);
        Vector resources = parseResources(enteredResources);
        // fill the names and values
        for(int z = 0;z < resources.size();z++) {
            String resourceName = (String)resources.elementAt(z);
            names.addElement(resourceName);
            values.addElement(resourceName);
        }
        return new Integer(-1);
    }

    /** Parse the string which holds all resources
     *
     * @param resources containts the full pathnames of all the resources, separated by semicolons
     * @return A vector with the same resources
     */
    private Vector parseResources(String resources) {
        Vector ret = new Vector();
        if(resources != null) {
            StringTokenizer resTokenizer = new StringTokenizer(resources, ";");
            while(resTokenizer.hasMoreElements()) {
                String path = (String)resTokenizer.nextElement();
                ret.addElement(path);
            }
        }
        return ret;
    }

    /** Check whether some of the resources are redundant because a superfolder has also
     *  been selected.
     *
     * @param resources containts the full pathnames of all the resources
     * @return A vector with the same resources, but the paths in the return value are disjoint
     */
    private void checkRedundancies(Vector resources) {
        int i, j;
        if(resources == null) {
            return ;
        }
        Vector redundant = new Vector();
        int n = resources.size();
        if(n < 2) {
            // no check needed, because there is only one resource or
            // no resources selected, return empty Vector
            return ;
        }
        for(i = 0;i < n;i++) {
            redundant.addElement(new Boolean(false));
        }
        for(i = 0;i < n - 1;i++) {
            for(j = i + 1;j < n;j++) {
                if(((String)resources.elementAt(i)).length() <
                        ((String)resources.elementAt(j)).length()) {
                    if(((String)resources.elementAt(j)).startsWith((String)resources.elementAt(i))) {
                        redundant.setElementAt(new Boolean(true), j);
                    }
                }
                else {
                    if(((String)resources.elementAt(i)).startsWith((String)resources.elementAt(j))) {
                        redundant.setElementAt(new Boolean(true), i);
                    }
                }
            }
        }
        for(i = n - 1;i >= 0;i--) {
            if(((Boolean)redundant.elementAt(i)).booleanValue()) {
                resources.removeElementAt(i);
            }
        }
    }

    /**
     * Check if this resource should is writeable.
     * @param cms The CmsObject
     * @param res The resource to be checked.
     * @return True or false.
     * @throws CmsException if something goes wrong.
     */
    private boolean isReadable(CmsObject cms, String resPath)  throws CmsException {
        CmsResource res = cms.readResource(resPath, CmsResourceFilter.IGNORE_EXPIRATION);
        return cms.hasPermissions(res, CmsPermissionSet.ACCESS_READ);
    }
}