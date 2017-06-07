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

package org.opencms.ade.publish;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.collectors.I_CmsResourceCollector;
import org.opencms.gwt.shared.I_CmsContentLoadCollectorInfo;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.logging.Log;

import com.google.common.collect.Sets;

/**
 * Helper class used to determine which resources from a collector list should be included in a publish list.<p>
 */
public class CmsCollectorPublishListHelper {

    /** The CMS context being used by this class. */
    private CmsObject m_cms;

    /** The collector information. */
    private I_CmsContentLoadCollectorInfo m_info;

    /** Boolean constant. */
    public static final boolean OFFLINE = false;

    /** Boolean constant. */
    public static final boolean ONLINE = true;

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsCollectorPublishListHelper.class);

    /** The number of resources which should be fetched via the collector. */
    private int m_collectorLimit;

    /**
     * Creates a new instance.<p>
     *
     * @param cms the CMS context to use
     * @param collectorInfo the collector information
     * @param collectorLimit the number of resources which should be fetched via the collector
     */
    public CmsCollectorPublishListHelper(
        CmsObject cms,
        I_CmsContentLoadCollectorInfo collectorInfo,
        int collectorLimit) {

        if (cms.getRequestContext().getCurrentProject().isOnlineProject()) {
            throw new IllegalArgumentException("CmsObject must not be set to the Online project!");
        }

        m_cms = cms;
        m_info = collectorInfo;
        m_collectorLimit = collectorLimit;
    }

    /**
     * Initializes a CmsObject.<p>
     *
     * @param online true if a CmsObject for the Online project should be returned
     * @return the initialized CmsObject
     *
     * @throws CmsException if something goes wrong
     */
    public CmsObject getCmsObject(boolean online) throws CmsException {

        return CmsPublishListHelper.adjustCmsObject(m_cms, online);
    }

    /**
     * Gets the collector to use.<p>
     *
     * @return the collector to use
     */
    public I_CmsResourceCollector getCollector() {

        return OpenCms.getResourceManager().getContentCollector(m_info.getCollectorName());
    }

    /**
     * Gets the list to add to the publish list for the collector list.<p>
     *
     * @return the resources to add to the publish list
     * @throws CmsException if something goes wrong
     */
    public Set<CmsResource> getPublishListFiles() throws CmsException {

        String context = "[" + RandomStringUtils.randomAlphabetic(8) + "] ";

        List<CmsResource> offlineResults = computeCollectorResults(OFFLINE);
        if (LOG.isDebugEnabled()) {
            LOG.debug(context + "Offline collector results for " + m_info + ": " + resourcesToString(offlineResults));
        }
        List<CmsResource> onlineResults = computeCollectorResults(ONLINE);
        if (LOG.isDebugEnabled()) {
            LOG.debug(context + "Online collector results for " + m_info + ": " + resourcesToString(onlineResults));
        }

        Set<CmsResource> result = Sets.newHashSet();
        for (CmsResource offlineRes : offlineResults) {

            if (!(offlineRes.getState().isUnchanged())) {
                result.add(offlineRes);
            }
        }
        Set<CmsResource> onlineAndNotOffline = Sets.newHashSet(onlineResults);
        onlineAndNotOffline.removeAll(offlineResults);
        for (CmsResource res : onlineAndNotOffline) {
            try {
                // Because the resources have state 'unchanged' in the Online project, we need to read them again in the Offline project
                res = getCmsObject(OFFLINE).readResource(res.getStructureId(), CmsResourceFilter.ALL);
                result.add(res);
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        result.addAll(onlineAndNotOffline);
        if (LOG.isDebugEnabled()) {
            LOG.debug(context + "Publish list contributions for " + m_info + ": " + resourcesToString(result));
        }
        return result;
    }

    /**
     * Computes the collector results.<p>
     *
     * @param online true if the collector results for the Online project should be returned
     * @return the collector results
     *
     * @throws CmsException if something goes wrong
     */
    protected List<CmsResource> computeCollectorResults(boolean online) throws CmsException {

        CmsObject cms = getCmsObject(online);
        I_CmsResourceCollector collector = getCollector();
        List<CmsResource> collectorResult = collector.getResults(
            cms,
            m_info.getCollectorName(),
            m_info.getCollectorParams(),
            m_collectorLimit);
        return collectorResult;
    }

    /**
     * Helper method to generate a string representation for a collection of resources for debug purposes.<p>
     *
     * @param resources the resources
     * @return a string representing the list of resources
     */
    private String resourcesToString(Iterable<CmsResource> resources) {

        StringBuffer buffer = new StringBuffer();
        boolean first = true;
        buffer.append("[");
        for (CmsResource res : resources) {
            if (!first) {
                buffer.append(", ");
            }
            first = false;
            buffer.append(res.getRootPath());
            buffer.append("!");
            buffer.append(res.getState().getAbbreviation());
        }
        buffer.append("]");
        return buffer.toString();
    }
}
