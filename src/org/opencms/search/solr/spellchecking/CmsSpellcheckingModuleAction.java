/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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

package org.opencms.search.solr.spellchecking;

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.db.CmsPublishList;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.module.I_CmsModuleAction;
import org.opencms.report.I_CmsReport;

import org.apache.commons.logging.Log;

/**
 * ModuleAction implementation for the spellchecking component that waits with indexing
 * operations until runlevel 4 has been reached.
 */
public final class CmsSpellcheckingModuleAction implements I_CmsModuleAction {

    /** The log object for this class. */
    static final Log LOG = CmsLog.getLog(CmsSpellcheckingModuleAction.class);

    /** The indexing thread. */
    private Thread m_indexingThread;

    /**
     * @see org.opencms.main.I_CmsEventListener#cmsEvent(org.opencms.main.CmsEvent)
     */
    public void cmsEvent(CmsEvent event) {

        // Ignore
    }

    /**
     * @see org.opencms.module.I_CmsModuleAction#initialize(org.opencms.file.CmsObject, org.opencms.configuration.CmsConfigurationManager, org.opencms.module.CmsModule)
     */
    public void initialize(
        final CmsObject adminCms,
        final CmsConfigurationManager configurationManager,
        final CmsModule module) {

        final Runnable r = new Runnable() {

            public void run() {

                // Although discouraged, use polling to make sure the indexing process does not start
                // before OpenCms has reached runlevel 4
                while (OpenCms.getRunLevel() == OpenCms.RUNLEVEL_2_INITIALIZING) {
                    try {
                        // Repeat check every five seconds
                        Thread.sleep(5 * 1000);
                    } catch (InterruptedException e) {
                        LOG.warn(e.getLocalizedMessage(), e);
                        // maybe OpenCms is being shutdown, just return
                        return;
                    }
                }

                // Check whether indexing is needed if not running the shell
                if ((OpenCms.getRunLevel() == OpenCms.RUNLEVEL_4_SERVLET_ACCESS)
                    && CmsSpellcheckDictionaryIndexer.updatingIndexNecessesary(adminCms)) {
                    CmsSolrSpellchecker spellchecker = OpenCms.getSearchManager().getSolrDictionary(adminCms);
                    if (spellchecker != null) {
                        spellchecker.parseAndAddDictionaries(adminCms);
                    }
                }
            }

        };

        m_indexingThread = new Thread(r, "CmsSpellcheckingModuleIndexingThread");
        m_indexingThread.start();
    }

    /**
     * @see org.opencms.module.I_CmsModuleAction#moduleUninstall(org.opencms.module.CmsModule)
     */
    public void moduleUninstall(CmsModule module) {

        shutDown(module);
    }

    /**
     * @see org.opencms.module.I_CmsModuleAction#moduleUpdate(org.opencms.module.CmsModule)
     */
    public void moduleUpdate(CmsModule module) {

        // Ignore
    }

    /**
     * @see org.opencms.module.I_CmsModuleAction#publishProject(org.opencms.file.CmsObject, org.opencms.db.CmsPublishList, int, org.opencms.report.I_CmsReport)
     */
    public void publishProject(CmsObject cms, CmsPublishList publishList, int publishTag, I_CmsReport report) {

        // Ignore
    }

    /**
     * @see org.opencms.module.I_CmsModuleAction#shutDown(org.opencms.module.CmsModule)
     */
    public void shutDown(CmsModule module) {

        if ((m_indexingThread != null) && m_indexingThread.isAlive()) {
            try {
                m_indexingThread.interrupt();
                m_indexingThread.join(10 * 1000);
            } catch (Exception e) {
                LOG.warn("Exception while shutting down spellcheck indexing thread.", e);
            }
        }
        m_indexingThread = null;
    }

}
