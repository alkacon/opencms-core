/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/legacy/Attic/CmsLegacyModuleAction.java,v $
 * Date   : $Date: 2005/03/15 18:05:55 $
 * Version: $Revision: 1.12 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002  Alkacon Software (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.opencms.legacy;

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.db.CmsDbPool;
import org.opencms.db.CmsPublishList;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsRequestContext;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.module.A_CmsModuleAction;
import org.opencms.module.CmsModule;
import org.opencms.report.I_CmsReport;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import com.opencms.defaults.master.genericsql.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Provided backward compatiblity for legacy (5.0) module "publishClass" 
 * and "maintainanceClass" methods.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */
public class CmsLegacyModuleAction extends A_CmsModuleAction {

    /** Name of the "publishclasses" parameter. */
    public static final String C_PARAM_PUBLISH_CLASSES = "publishclasses";

    /** Name of the "maintananceclasses" parameter. */
    public static final String C_PARAM_MAINTANANCE_CLASSES = "maintananceclasses";
    
    /** Name of the legacy compatibility module. */
    public static final String C_LEGACY_MODULE_NAME = "org.opencms.legacy.compatibility";
    
    /**
     * Splitsa a list of class names with a ";" delimiter.<p>
     * 
     * @param value the value to split
     * @return a list of class names
     */
    private static List splitClassNames(String value) {

        ArrayList result = new ArrayList();
        
        if (value == null) {
            return result;
        }
        
        String[] names = CmsStringUtil.splitAsArray(value, ';');
        for (int i=0; i<names.length; i++) {
            String name = names[i].trim();
            if (CmsStringUtil.isNotEmpty(name)) {
                result.add(name);
            }
        }
        
        return result;
    }
    
    /**
     * Returns the list of configured legacy module publish classes.<p>
     * 
     * @return the list of configured legacy module publish classes
     */
    public static List getLegacyModulePublishClasses() {
        
        CmsModule module = OpenCms.getModuleManager().getModule(C_LEGACY_MODULE_NAME);
        if (module == null) {
            // module is not (correctly) configured
            return Collections.EMPTY_LIST;
        }
        
        return splitClassNames(module.getParameter(C_PARAM_PUBLISH_CLASSES));        
    }       

    /**
     * @see org.opencms.module.I_CmsModuleAction#initialize(org.opencms.file.CmsObject, CmsConfigurationManager, CmsModule)
     */
    public void initialize(CmsObject adminCms, CmsConfigurationManager configurationManager, CmsModule module) {
        
        Map config = adminCms.getConfigurations();
        String dbName = config.get(CmsDbPool.C_KEY_DATABASE_NAME).toString().toLowerCase();
        String poolUrl = config.get("db.cos.pool").toString();
        
        CmsDbAccess dbAccess = new CmsDbAccess(poolUrl);
        boolean masterAvailable = dbAccess.checkTables();

        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Checking master module tables for " + dbName
                + " - " + ((masterAvailable) ? "found" : "not found"));
        }
        
        if (!masterAvailable) {
            if (OpenCms.getLog(this).isDebugEnabled()) {
                OpenCms.getLog(this).debug("Calling master module table setup script for " + dbName);
            }
            
            String modulePath[] = (String[])module.getResources().toArray(new String[1]);
            
            String scriptPath;
            if (dbName.toLowerCase().startsWith("mysql")) {
                // use the mysql setup script for all MySQL variations (Innodb, MySQL 4.1 etc. pp.)
                scriptPath = modulePath[0] + "setup/mysql.sql";
            } else {
                scriptPath = modulePath[0] + "setup/" + dbName + ".sql";
            }
            
            try {
                String updateScript = new String(adminCms.readFile(scriptPath).getContents());
                
                HashMap replacers = new HashMap();
                for (Iterator i = module.getParameters().keySet().iterator(); i.hasNext();) {
                    String param = (String)i.next(), value = null;
                    if (param.startsWith("$")) {
                        value = (String)module.getParameters().get(param);
                        replacers.put(param, value);
                    }
                }
                dbAccess.updateDatabase(updateScript, replacers);
            } catch (CmsException exc) {
                if (OpenCms.getLog(this).isErrorEnabled()) {
                    OpenCms.getLog(this).error(
                        "Error while creating master module tables",
                        exc);
                }
            }
            
            if (OpenCms.getLog(this).isInfoEnabled()) {
                OpenCms.getLog(this).info(
                        ". Legacy initialization: Created master module tables");
            }
        }
    }
    
    /**
     * @see org.opencms.module.I_CmsModuleAction#publishProject(org.opencms.file.CmsObject, org.opencms.db.CmsPublishList, int, org.opencms.report.I_CmsReport)
     */
    public void publishProject(CmsObject cms, CmsPublishList publishList, int backupTagId, I_CmsReport report) {

        CmsRequestContext context = cms.getRequestContext();

        if (!publishList.isDirectPublish()
            && context.currentProject().getType() != I_CmsConstants.C_PROJECT_TYPE_TEMPORARY) {
            
            List legacyPublishClasses = getLegacyModulePublishClasses();
            if (legacyPublishClasses.isEmpty()) {
                // no legacy publish classes configured in module
                return;
            }

            long publishDate = System.currentTimeMillis();
            boolean backupEnabled = OpenCms.getSystemInfo().isVersionHistoryEnabled();

            if (backupEnabled) {
                try {
                    publishDate = cms.readBackupProject(backupTagId).getPublishingDate();
                } catch (CmsException e) {
                    // nothing to do
                }

                if (publishDate == 0) {
                    publishDate = System.currentTimeMillis();
                }
            }

            Vector changedResources = new Vector();
            Vector changedModuleMasters = new Vector();

            for (int i = 0; i < legacyPublishClasses.size(); i++) {
                // call the publishProject method of the class with parameters:
                // cms, m_enableHistory, project_id, version_id, publishDate, subId,
                // the vector changedResources and the vector changedModuleMasters
                try {
                    // The changed masters are added to the vector changedModuleMasters, so after the last module
                    // was published the vector contains the changed masters of all published modules
                    Class.forName((String)legacyPublishClasses.get(i)).getMethod(
                        "publishProject",
                        new Class[] {
                            CmsObject.class,
                            CmsUUID.class,
                            Boolean.class,
                            Integer.class,
                            Integer.class,
                            Long.class,
                            Vector.class,
                            Vector.class}).invoke(
                        null,
                        new Object[] {
                            cms,
                            publishList.getPublishHistoryId(),
                            new Boolean(OpenCms.getSystemInfo().isVersionHistoryEnabled()),
                            new Integer(context.currentProject().getId()),
                            new Integer(backupTagId),
                            new Long(publishDate),
                            changedResources,
                            changedModuleMasters});
                } catch (ClassNotFoundException ec) {
                    report.println(report.key("report.publish_class_for_module_does_not_exist_1")
                        + (String)legacyPublishClasses.get(i)
                        + report.key("report.publish_class_for_module_does_not_exist_2"), I_CmsReport.C_FORMAT_WARNING);
                    if (OpenCms.getLog(this).isErrorEnabled()) {
                        OpenCms.getLog(this).error(
                            "Error calling publish class of module " + (String)legacyPublishClasses.get(i),
                            ec);
                    }
                } catch (Throwable t) {
                    report.println(t);
                    if (OpenCms.getLog(this).isErrorEnabled()) {
                        OpenCms.getLog(this).error(
                            "Error while publishing data of module " + (String)legacyPublishClasses.get(i),
                            t);
                    }
                }
            }
        }
    }
}