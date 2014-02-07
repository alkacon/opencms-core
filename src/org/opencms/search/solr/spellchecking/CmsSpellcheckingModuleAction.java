/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.module.I_CmsModuleAction;
import org.opencms.report.I_CmsReport;

/**
 * ModuleAction implementation for the spellchecking component that waits with indexing
 * operations until runlevel 4 has been reached. 
 */
public final class CmsSpellcheckingModuleAction implements I_CmsModuleAction {

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
                // before OpenCms has reached runlevel 4 (Also, ignore if running on shell)
                while ((OpenCms.getRunLevel() != OpenCms.RUNLEVEL_4_SERVLET_ACCESS)
                    || (OpenCms.getRunLevel() != OpenCms.RUNLEVEL_3_SHELL_ACCESS)) {
                    try {
                        // Repeat check every ten seconds
                        Thread.sleep(10 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                // Check whether indexing is needed
                if (CmsSpellcheckDictionaryIndexer.updatingIndexNecessesary(adminCms)) {
                    final CmsSolrSpellchecker spellchecker = OpenCms.getSearchManager().getSolrDictionary(adminCms);
                    spellchecker.parseAndAddDictionaries(adminCms);
                }
            }
        };

        if (OpenCms.getRunLevel() != OpenCms.RUNLEVEL_3_SHELL_ACCESS) {
            new Thread(r, "CmsSpellcheckingModuleIndexingThread").start();
        }

    }

    /**
     * @see org.opencms.module.I_CmsModuleAction#moduleUninstall(org.opencms.module.CmsModule)
     */
    public void moduleUninstall(CmsModule module) {

        // Ignore
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

        // Ignore
    }

}
