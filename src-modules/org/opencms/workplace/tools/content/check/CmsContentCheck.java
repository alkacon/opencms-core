/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/content/check/CmsContentCheck.java,v $
 * Date   : $Date: 2005/10/25 15:14:32 $
 * Version: $Revision: 1.1.2.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.tools.content.check;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.report.I_CmsReport;
import org.opencms.util.CmsFileUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.tools.CmsToolManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.logging.Log;

/**
 * Main implementation of the content check. It maintains all configured content check
 * plugins and handles the check process.<p>
 * 
 *
 * @author  Michael Emmerich
 * 
 * @version $Revision: 1.1.2.2 $ 
 * 
 * @since 6.1.2 
 */
public class CmsContentCheck {

    /**  Path to the plugin folder. */
    public static final String VFS_PATH_PLUGIN_FOLDER = CmsWorkplace.VFS_PATH_WORKPLACE
        + "admin/contenttools/check/plugin/";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsContentCheck.class);

    /** The CmsObject. */
    private CmsObject m_cms;

    /** The list of paths to be processed. */
    private List m_paths;

    /** The list of all instantiated plugins. */
    private List m_plugins;

    /** The report for the output. */
    private I_CmsReport m_report;

    /** The map of resources to check. */
    private SortedMap m_resources;

    /** The content check result. */
    private CmsContentCheckResult m_result;

    /**
     * Constructor, creates a new CmsContentCheck. <p>
     * 
     * @param cms the CmsObject
     */
    public CmsContentCheck(CmsObject cms) {

        m_cms = cms;
        m_plugins = new ArrayList();
        m_paths = new ArrayList();
        m_result = new CmsContentCheckResult();

        try {
            init();
        } catch (CmsException e) {
            // TODO: do some errorhandling
        }
    }

    /**
     * Gets a list of all paths to be processed by the content check plugins.<p>
     * 
     * @return list of vfs paths
     */
    public List getPaths() {

        return m_paths;
    }

    /**
     * Gets a list of instances of all available content check plugins.<p>
     * 
     * @return list of plugin instances.
     */
    public List getPlugins() {

        return m_plugins;
    }

    /**
     * Gets the number of installed plugins.<p>
     * 
     * @return number of installed plugins
     */
    public int getPluginsCount() {

        return m_plugins.size();
    }

    /**
     * Gets the results of the content check.<p>
     * 
     * @return CmsContentCheckResult object containing all resources that collected an error or warning
     */
    public CmsContentCheckResult getResults() {

        return m_result;
    }

    /**
     * Sets the list of all paths to be processed by the content check plugins.<p>
     * @param paths list of vfs paths
     */
    public void setPaths(List paths) {

        m_paths = paths;
    }

    /**
     * Starts the content check.<p>
     * 
     * @param cms the CmsObject
     * @param report StringBuffer for reporting
     * @throws Exception if something goes wrong
     */
    public void startContentCheck(CmsObject cms, I_CmsReport report) throws Exception {

        m_report = report;
        m_report.println(Messages.get().container(Messages.RPT_CONTENT_CHECK_BEGIN_0), I_CmsReport.FORMAT_HEADLINE);

        // collect all resources
        m_report.print(Messages.get().container(Messages.RPT_CONTENT_COLLECT_BEGIN_0), I_CmsReport.FORMAT_HEADLINE);
        m_report.println(
            org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0),
            I_CmsReport.FORMAT_HEADLINE);
        m_resources = collectResources(cms);
        m_report.print(
            org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0),
            I_CmsReport.FORMAT_HEADLINE);
        m_report.println(
            Messages.get().container(Messages.RPT_CONTENT_COLLECT_END_1, new Integer(m_resources.size())),
            I_CmsReport.FORMAT_HEADLINE);

        // now process all resources
        m_report.print(Messages.get().container(Messages.RPT_CONTENT_PROCESS_BEGIN_0), I_CmsReport.FORMAT_HEADLINE);
        m_report.println(
            org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0),
            I_CmsReport.FORMAT_HEADLINE);
        int count = 1;
        Iterator i = m_resources.keySet().iterator();
        while (i.hasNext()) {
            String resourceName = (String)i.next();
            CmsContentCheckResource res = (CmsContentCheckResource)m_resources.get(resourceName);
            boolean errorWarning = false;
            m_report.print(Messages.get().container(
                Messages.RPT_CONTENT_PROCESS_2,
                new Integer(count++),
                new Integer(m_resources.size())), I_CmsReport.FORMAT_NOTE);
            m_report.print(
                Messages.get().container(Messages.RPT_CONTENT_PROCESS_RESOURCE_1, res.getResourceName()),
                I_CmsReport.FORMAT_DEFAULT);
            m_report.print(
                org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0),
                I_CmsReport.FORMAT_NOTE);

            // loop through all plugins and perform each content check
            Iterator j = getPlugins().iterator();
            while (j.hasNext()) {
                I_CmsContentCheck plugin = (I_CmsContentCheck)j.next();
                if (plugin.isActive()) {
                    try {
                        // process the content check
                        res = plugin.executeContentCheck(m_cms, res);

                    } catch (CmsException e) {
                        errorWarning = true;
                        m_report.println(Messages.get().container(Messages.RPT_EMPTY_0), I_CmsReport.FORMAT_DEFAULT);
                        m_report.print(Messages.get().container(
                            Messages.RPT_CONTENT_PROCESS_ERROR_2,
                            plugin.getName(),
                            e), I_CmsReport.FORMAT_ERROR);
                    }
                }
            }

            // check if there are some errors
            List errors = res.getErrors();
            if (errors != null && errors.size() > 0) {
                errorWarning = true;
                m_report.println(Messages.get().container(Messages.RPT_EMPTY_0), I_CmsReport.FORMAT_DEFAULT);
                m_report.println(
                    Messages.get().container(Messages.RPT_CONTENT_PROCESS_ERROR_0),
                    I_CmsReport.FORMAT_ERROR);
                Iterator k = errors.iterator();
                while (k.hasNext()) {
                    String error = (String)k.next();
                    m_report.println(
                        Messages.get().container(Messages.RPT_CONTENT_PROCESS_ERROR_1, error),
                        I_CmsReport.FORMAT_ERROR);

                }
            }

            // check if there are some warnings
            List warnings = res.getWarnings();
            if (warnings != null && warnings.size() > 0) {
                errorWarning = true;
                m_report.println(Messages.get().container(Messages.RPT_EMPTY_0), I_CmsReport.FORMAT_DEFAULT);
                m_report.println(
                    Messages.get().container(Messages.RPT_CONTENT_PROCESS_WARNING_0),
                    I_CmsReport.FORMAT_WARNING);
                Iterator k = warnings.iterator();
                while (k.hasNext()) {
                    String warning = (String)k.next();
                    m_report.println(
                        Messages.get().container(Messages.RPT_CONTENT_PROCESS_WARNING_1, warning),
                        I_CmsReport.FORMAT_WARNING);

                }
            }

            // store the updated CmsContentCheckResource
            m_resources.put(resourceName, res);

            if (!errorWarning) {
                m_report.println(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                    I_CmsReport.FORMAT_OK);
            } else {
                // there was an error or warning, so store the CmsContentCheckResource
                // in the results
                m_result.addResult(res);
            }
        }
        m_report.print(
            org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0),
            I_CmsReport.FORMAT_HEADLINE);
        m_report.println(Messages.get().container(Messages.RPT_CONTENT_PROCESS_END_0), I_CmsReport.FORMAT_HEADLINE);

        m_report.println(Messages.get().container(Messages.RPT_CONTENT_CHECK_END_0), I_CmsReport.FORMAT_HEADLINE);
    }

    /**
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {

        StringBuffer buf = new StringBuffer();
        buf.append("CmsContentCheck Paths=[");
        for (int i = 0; i < m_paths.size(); i++) {
            String path = (String)m_paths.get(i);
            buf.append(path);
            if (i < m_paths.size() - 1) {
                buf.append(",");
            }
        }
        buf.append("] Plugins=[");
        for (int i = 0; i < m_plugins.size(); i++) {
            I_CmsContentCheck plugin = (I_CmsContentCheck)m_plugins.get(i);
            buf.append(plugin.getName());
            buf.append(" (");
            buf.append(plugin.isActive());
            buf.append(")");
            if (i < m_plugins.size() - 1) {
                buf.append(",");
            }
        }
        buf.append("]");
        return buf.toString();
    }

    /** 
     * Collects all resources required for the content checks and stores them in a Set.<p>
     * 
     * The collection of resources is build based on the vfs paths stored in this object. 
     * To prevent that resources are collected multiple times (in case of overlapping vfs paths),
     * the results will be stored as a map.
     * 
     * @param cms the CmsObject
     * @return map of CmsContentCheckResources
     */
    private SortedMap collectResources(CmsObject cms) {

        SortedMap collectedResources = new TreeMap();

        // get all vfs paths and extract the resources from there
        Iterator i = CmsFileUtil.removeRedundancies(m_paths).iterator();
        while (i.hasNext()) {
            String path = (String)i.next();
            m_report.print(
                Messages.get().container(Messages.RPT_EXTRACT_FROM_PATH_BEGIN_1, path),
                I_CmsReport.FORMAT_HEADLINE);
            m_report.println(
                org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0),
                I_CmsReport.FORMAT_HEADLINE);
            try {
                List resources = cms.readResources(path, CmsResourceFilter.IGNORE_EXPIRATION, true);
                // get the single resources and store them
                Iterator j = resources.iterator();
                while (j.hasNext()) {
                    CmsResource res = (CmsResource)j.next();
                    //                    m_report.print(
                    //                        Messages.get().container(Messages.RPT_EXTRACT_FROM_PATH_1, cms.getSitePath(res)),
                    //                        I_CmsReport.FORMAT_DEFAULT);
                    //                    m_report.print(
                    //                        org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0),
                    //                        I_CmsReport.FORMAT_DEFAULT);
                    // create a CmsContentCheckResource for each resource
                    CmsContentCheckResource contentCheckRes = new CmsContentCheckResource(res);
                    collectedResources.put(contentCheckRes.getResourceName(), contentCheckRes);
                    //                    m_report.println(
                    //                        org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                    //                        I_CmsReport.FORMAT_OK);
                }

            } catch (CmsException e) {
                m_report.println(
                    Messages.get().container(Messages.RPT_EXTRACT_FROM_PATH_ERROR_2, path, e),
                    I_CmsReport.FORMAT_ERROR);
            }

            m_report.print(
                org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0),
                I_CmsReport.FORMAT_HEADLINE);
            m_report.println(
                Messages.get().container(Messages.RPT_EXTRACT_FROM_PATH_END_0),
                I_CmsReport.FORMAT_HEADLINE);

        }

        return collectedResources;
    }

    /**
     * Initializes the CmsContent check and reads all available plugins.<p>
     */
    private void init() throws CmsException {

        // first get all installed plugins
        // plugins are subfolders of the "/plugin/" folder with a template
        // property holdding the name of the plugin class
        List resources = m_cms.readResourcesWithProperty(VFS_PATH_PLUGIN_FOLDER, CmsToolManager.HANDLERCLASS_PROPERTY);
        Iterator i = resources.iterator();
        while (i.hasNext()) {
            CmsResource res = (CmsResource)i.next();
            // ony check folders
            if (res.isFolder()) {
                String classname = m_cms.readPropertyObject(
                    res.getRootPath(),
                    CmsToolManager.HANDLERCLASS_PROPERTY,
                    false).getValue();
                try {
                    Object objectInstance = Class.forName(classname).newInstance();
                    if (objectInstance instanceof I_CmsContentCheck) {
                        I_CmsContentCheck plugin = (I_CmsContentCheck)objectInstance;
                        plugin.init(m_cms);
                        // store the plugin instance
                        m_plugins.add(plugin);
                        LOG.info(Messages.get().key(Messages.LOG_CREATE_PLUGIN_1, classname));
                    } else {
                        LOG.warn(Messages.get().key(Messages.LOG_CANNOT_CREATE_PLUGIN_1, classname));
                    }
                } catch (Throwable t) {
                    LOG.error(Messages.get().key(Messages.LOG_CANNOT_CREATE_PLUGIN_2, classname, t));
                }
            }
        }
    }

}
