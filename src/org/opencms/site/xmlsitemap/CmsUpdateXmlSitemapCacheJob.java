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

package org.opencms.site.xmlsitemap;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.scheduler.I_CmsScheduledJob;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Scheduled job for updating the XML sitemap cache.<p>
 */
public class CmsUpdateXmlSitemapCacheJob implements I_CmsScheduledJob {

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsUpdateXmlSitemapCacheJob.class);

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
        LOG.info("Starting to process individual XML sitemap files...");
        int i = 0;
        for (CmsResource res : resources) {
            i += 1;
            try {
                LOG.info("Processing file " + res.getRootPath() + " [" + i + "/" + resources.size() + "]");
                String name = res.getName();
                if (name.contains("robots") || name.contains("test")) {
                    LOG.info("Ignoring file " + res.getRootPath());
                    continue;
                }
                CmsXmlSeoConfiguration config = new CmsXmlSeoConfiguration();
                config.load(cms, res);
                CmsXmlSitemapGenerator generator = null;
                if (config.usesCache()) {
                    generator = CmsXmlSitemapActionElement.prepareSitemapGenerator(res, config);
                }
                if (generator != null) {
                    String xml = generator.renderSitemap();
                    CmsXmlSitemapCache.INSTANCE.put(res.getRootPath(), xml);
                } else {
                    LOG.info("Ignoring file " + res.getRootPath());
                }
            } catch (Exception e) {
                LOG.error("Error processing file " + res.getRootPath() + ": " + e.getLocalizedMessage(), e);
            }
        }
        long end = System.currentTimeMillis();
        LOG.info("Finished processing XML sitemap files. Elapsed time: " + ((end - start) / 1000) + " seconds");
        return "";
    }
}
