/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.site.xmlsitemap;

import org.opencms.db.CmsPublishList;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.scheduler.I_CmsScheduledJob;
import org.opencms.site.xmlsitemap.CmsLlmsGenerator.CmsLlmsFileContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Scheduled job for updating the llms.txt summary files.<p>
 */
public class CmsUpdateLlmsTextJob implements I_CmsScheduledJob {

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsUpdateLlmsTextJob.class);

    /**
     * @see org.opencms.scheduler.I_CmsScheduledJob#launch(org.opencms.file.CmsObject, java.util.Map)
     */
    public String launch(CmsObject cms, Map<String, String> parameters) throws Exception {

        long start = System.currentTimeMillis();
        LOG.info("Starting job " + getClass().getName());

        String parentFolder = parameters.get("folder");
        if (parentFolder == null) {
            parentFolder = "/";
        }
        I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(CmsXmlSeoConfiguration.SEO_FILE_TYPE);
        List<CmsResource> resources = cms.readResources(
            parentFolder,
            CmsResourceFilter.DEFAULT_FILES.addRequireType(type));
        List<CmsResource> publishList = new ArrayList<CmsResource>();
        LOG.info("Starting to process individual llms.txt configuration files...");
        int i = 0;
        for (CmsResource res : resources) {
            i += 1;
            try {
                LOG.info("Processing file " + res.getRootPath() + " [" + i + "/" + resources.size() + "]");
                String name = res.getName();
                if (name.contains("sitemap") || name.contains("robots") || name.contains("test")) {
                    LOG.info("Ignoring file " + res.getRootPath());
                    continue;
                }
                CmsXmlSeoConfiguration config = new CmsXmlSeoConfiguration();
                if (res.getState().isChanged()) {
                    // use online configuration to generate result
                    CmsProject currentProject = cms.getRequestContext().getCurrentProject();
                    cms.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_NAME));
                    config.load(cms, cms.readResource(res.getStructureId()));
                    cms.getRequestContext().setCurrentProject(currentProject);
                } else {
                    config.load(cms, res);
                }
                if (config.getMode().equals(CmsXmlSeoConfiguration.MODE_LLMS_TXT)) {
                    // get llms.txt generator
                    CmsLlmsGenerator llmsGenerator = new CmsLlmsGenerator(config, res, cms);
                    CmsLlmsFileContainer updateResult = llmsGenerator.updateLlmsFile();
                    if (updateResult.updated() && !res.getState().isNew()) {
                        // add result file to publish list
                        publishList.add(updateResult.llmsFile());
                    }
                } else {
                    LOG.info("Ignoring file " + res.getRootPath());
                }
            } catch (Exception e) {
                LOG.error("Error processing file " + res.getRootPath() + ": " + e.getLocalizedMessage(), e);
            }
        }
        if (!publishList.isEmpty()) {
            LOG.info("Publishing updated files...");
            CmsPublishList pubList = OpenCms.getPublishManager().getPublishList(cms, publishList, false);
            OpenCms.getPublishManager().publishProject(cms, null, pubList);
        }

        long end = System.currentTimeMillis();
        LOG.info(
            "Finished processing llms.txt configuration files. Elapsed time: " + ((end - start) / 1000) + " seconds");
        return "";
    }
}
