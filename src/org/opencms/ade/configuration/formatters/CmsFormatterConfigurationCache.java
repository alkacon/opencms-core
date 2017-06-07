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

package org.opencms.ade.configuration.formatters;

import org.opencms.ade.configuration.I_CmsGlobalConfigurationCache;
import org.opencms.db.CmsPublishedResource;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;
import org.opencms.xml.containerpage.CmsFormatterBean;
import org.opencms.xml.containerpage.I_CmsFormatterBean;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;

import com.google.common.collect.Maps;

/**
 * A cache object which holds a collection of formatter configuration beans read from the VFS.<p>
 *
 * This class does not immediately update the cached formatter collection when changes in the VFS occur, but instead
 * schedules an update action with a slight delay, so that if many formatters are changed in a short time, only one update
 * operation is needed.<p>
 *
 * Two instances of this cache are needed, one for the Online project and one for Offline projects.<p>
 **/
public class CmsFormatterConfigurationCache implements I_CmsGlobalConfigurationCache {

    /** A UUID which is used to mark the configuration cache for complete reloading. */
    public static final CmsUUID RELOAD_MARKER = CmsUUID.getNullUUID();

    /** The resource type for formatter configurations. */
    public static final String TYPE_FORMATTER_CONFIG = "formatter_config";

    /** The resource type for macro formatters. */
    public static final String TYPE_MACRO_FORMATTER = "macro_formatter";

    /** The delay to use for updating the formatter cache, in seconds. */
    protected static int UPDATE_DELAY_SECONDS = 7;

    /** The logger for this class. */
    private static final Log LOG = CmsLog.getLog(CmsFormatterConfigurationCache.class);

    /** The CMS context used by this cache. */
    private CmsObject m_cms;

    /** The set of ids to update. */
    private Set<CmsUUID> m_idsToUpdate = new HashSet<CmsUUID>();

    /** The cache name. */
    private String m_name;

    /** Flag which indicates whether an update operation has been scheduled. */
    private boolean m_scheduledUpdate;

    /** The current data contained in the formatter cache.<p> This field is reassigned when formatters are changed, but the objects pointed to by this  field are immutable.<p> **/
    private volatile CmsFormatterConfigurationCacheState m_state = new CmsFormatterConfigurationCacheState(
        Collections.<CmsUUID, I_CmsFormatterBean> emptyMap());

    /**
     * Creates a new formatter configuration cache instance.<p>
     *
     * @param cms the CMS context to use
     * @param name the cache name
     *
     * @throws CmsException if something goes wrong
     */
    public CmsFormatterConfigurationCache(CmsObject cms, String name)
    throws CmsException {

        m_cms = OpenCms.initCmsObject(cms);
        Map<CmsUUID, I_CmsFormatterBean> noFormatters = Collections.emptyMap();
        m_state = new CmsFormatterConfigurationCacheState(noFormatters);
        m_name = name;
    }

    /**
     * @see org.opencms.ade.configuration.I_CmsGlobalConfigurationCache#clear()
     */
    public void clear() {

        markForUpdate(RELOAD_MARKER);
    }

    /**
     * Gets the cache instance name.<p>
     *
     * @return the cache instance name
     */
    public String getName() {

        return m_name;
    }

    /**
     * Gets the collection of cached formatters.<p>
     *
     * @return the collection of cached formatters
     */
    public CmsFormatterConfigurationCacheState getState() {

        return m_state;
    }

    /**
     * The method called by the scheduled update action to update the cache.<p>
     */
    public synchronized void performUpdate() {

        m_scheduledUpdate = false;
        Set<CmsUUID> copiedIds = new HashSet<CmsUUID>(m_idsToUpdate);
        m_idsToUpdate.clear();
        if (copiedIds.contains(RELOAD_MARKER)) {
            // clear cache event, reload all formatter configurations
            reload();
        } else {
            // normal case: incremental update
            Map<CmsUUID, I_CmsFormatterBean> formattersToUpdate = Maps.newHashMap();
            for (CmsUUID structureId : copiedIds) {
                I_CmsFormatterBean formatterBean = readFormatter(structureId);
                // formatterBean may be null here
                formattersToUpdate.put(structureId, formatterBean);
            }
            m_state = m_state.createUpdatedCopy(formattersToUpdate);
        }
    }

    /**
     * Reloads the formatter cache.<p>
     */
    public synchronized void reload() {

        try {
            m_idsToUpdate.clear();
            I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(TYPE_FORMATTER_CONFIG);
            CmsResourceFilter filter = CmsResourceFilter.ONLY_VISIBLE_NO_DELETED.addRequireType(type);
            List<CmsResource> formatterResources = new ArrayList<CmsResource>(m_cms.readResources("/", filter));
            type = OpenCms.getResourceManager().getResourceType(TYPE_MACRO_FORMATTER);
            filter = CmsResourceFilter.ONLY_VISIBLE_NO_DELETED.addRequireType(type);
            formatterResources.addAll(m_cms.readResources("/", filter));
            Map<CmsUUID, I_CmsFormatterBean> newFormatters = Maps.newHashMap();
            for (CmsResource formatterResource : formatterResources) {
                I_CmsFormatterBean formatterBean = readFormatter(formatterResource.getStructureId());
                if (formatterBean != null) {
                    newFormatters.put(formatterResource.getStructureId(), formatterBean);
                }
            }
            m_state = new CmsFormatterConfigurationCacheState(newFormatters);
        } catch (CmsException e) {
            LOG.warn(e.getLocalizedMessage(), e);
        }
    }

    /**
     * @see org.opencms.ade.configuration.I_CmsGlobalConfigurationCache#remove(org.opencms.db.CmsPublishedResource)
     */
    public void remove(CmsPublishedResource pubRes) {

        checkIfUpdateIsNeeded(pubRes.getStructureId(), pubRes.getRootPath(), pubRes.getType());
    }

    /**
     * @see org.opencms.ade.configuration.I_CmsGlobalConfigurationCache#remove(org.opencms.file.CmsResource)
     */
    public void remove(CmsResource resource) {

        checkIfUpdateIsNeeded(resource.getStructureId(), resource.getRootPath(), resource.getTypeId());
    }

    /**
     * @see org.opencms.ade.configuration.I_CmsGlobalConfigurationCache#update(org.opencms.db.CmsPublishedResource)
     */
    public void update(CmsPublishedResource pubRes) {

        checkIfUpdateIsNeeded(pubRes.getStructureId(), pubRes.getRootPath(), pubRes.getType());
    }

    /**
     * @see org.opencms.ade.configuration.I_CmsGlobalConfigurationCache#update(org.opencms.file.CmsResource)
     */
    public void update(CmsResource resource) {

        checkIfUpdateIsNeeded(resource.getStructureId(), resource.getRootPath(), resource.getTypeId());
    }

    /**
     * Waits until no update action is scheduled.<p>
     *
     * Should only be used in tests.<p>
     */
    public synchronized void waitForUpdate() {

        while (m_scheduledUpdate) {
            try {
                // use wait, not Thread.sleep, so the object monitor is released
                wait(300);
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * Reads a formatter given its structure id and returns it, or null if the formatter couldn't be read.<p>
     *
     * @param structureId the structure id of the formatter configuration
     *
     * @return the formatter bean, or null if no formatter could be read for some reason
     */
    protected CmsFormatterBean readFormatter(CmsUUID structureId) {

        CmsFormatterBean formatterBean = null;
        CmsResource formatterRes = null;
        try {
            formatterRes = m_cms.readResource(structureId);
            CmsFile formatterFile = m_cms.readFile(formatterRes);
            CmsFormatterBeanParser parser = new CmsFormatterBeanParser(m_cms);
            CmsXmlContent content = CmsXmlContentFactory.unmarshal(m_cms, formatterFile);
            formatterBean = parser.parse(content, formatterRes.getRootPath(), "" + formatterRes.getStructureId());
        } catch (Exception e) {

            if (formatterRes == null) {
                // normal case if resources get deleted, should not be written to the error channel
                LOG.info("Could not read formatter with id " + structureId);
            } else {
                LOG.error(
                    "Error while trying to read formatter configuration "
                        + formatterRes.getRootPath()
                        + ":    "
                        + e.getLocalizedMessage(),
                    e);
            }
        }
        return formatterBean;
    }

    /**
     * Checks if an update of the formatter is needed and if so, adds its structure id to the update set.<p>
     *
     * @param structureId the structure id of the formatter
     * @param path the path of the formatter
     * @param resourceType the resource type
     */
    private void checkIfUpdateIsNeeded(CmsUUID structureId, String path, int resourceType) {

        if (CmsResource.isTemporaryFileName(path)) {
            return;
        }
        if (OpenCms.getResourceManager().matchResourceType(TYPE_FORMATTER_CONFIG, resourceType)
            || OpenCms.getResourceManager().matchResourceType(TYPE_MACRO_FORMATTER, resourceType)) {
            markForUpdate(structureId);
        }
    }

    /**
     * Adds a formatter structure id to the update set, and schedule an update task unless one is already scheduled.<p>
     *
     * @param structureId the structure id of the formatter configuration
     */
    private synchronized void markForUpdate(CmsUUID structureId) {

        m_idsToUpdate.add(structureId);
        if (!m_scheduledUpdate) {
            OpenCms.getExecutor().schedule(new Runnable() {

                public void run() {

                    performUpdate();
                }
            }, UPDATE_DELAY_SECONDS, TimeUnit.SECONDS);
            m_scheduledUpdate = true;

        }
    }
}
