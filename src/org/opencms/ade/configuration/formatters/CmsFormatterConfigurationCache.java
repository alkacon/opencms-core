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

import org.opencms.ade.configuration.CmsConfigurationReader;
import org.opencms.ade.configuration.I_CmsGlobalConfigurationCache;
import org.opencms.db.CmsPublishedResource;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeFunctionConfig;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.loader.CmsResourceManager;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.util.CmsWaitHandle;
import org.opencms.xml.containerpage.I_CmsFormatterBean;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.CmsXmlContentProperty;
import org.opencms.xml.content.CmsXmlContentRootLocation;
import org.opencms.xml.content.I_CmsXmlContentValueLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
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

    /** Node name for the FormatterKey node. */
    public static final String N_FORMATTER_KEY = "FormatterKey";

    /** A UUID which is used to mark the configuration cache for complete reloading. */
    public static final CmsUUID RELOAD_MARKER = CmsUUID.getNullUUID();

    /** The resource type for macro formatters. */
    public static final String TYPE_FLEX_FORMATTER = "flex_formatter";

    /** The resource type for formatter configurations. */
    public static final String TYPE_FORMATTER_CONFIG = "formatter_config";

    /** The resource type for macro formatters. */
    public static final String TYPE_MACRO_FORMATTER = "macro_formatter";

    /** Type name for setting configurations. */
    public static final String TYPE_SETTINGS_CONFIG = "settings_config";

    /** The delay to use for updating the formatter cache, in seconds. */
    protected static int UPDATE_DELAY_MILLIS = 500;

    /** The logger for this class. */
    private static final Log LOG = CmsLog.getLog(CmsFormatterConfigurationCache.class);

    /** The CMS context used by this cache. */
    private CmsObject m_cms;

    /** The cache name. */
    private String m_name;

    /** Additional setting configurations. */
    private volatile Map<CmsUUID, Map<CmsSharedSettingKey, CmsXmlContentProperty>> m_settingConfigs;

    /** The current data contained in the formatter cache.<p> This field is reassigned when formatters are changed, but the objects pointed to by this  field are immutable.<p> **/
    private volatile CmsFormatterConfigurationCacheState m_state = new CmsFormatterConfigurationCacheState(
        Collections.<CmsUUID, I_CmsFormatterBean> emptyMap());

    /** The future for the scheduled task. */
    private volatile ScheduledFuture<?> m_taskFuture;

    /** The work queue to keep track of what needs to be done during the next cache update. */
    private LinkedBlockingQueue<Object> m_workQueue = new LinkedBlockingQueue<>();

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
     * Adds a wait handle to the list of wait handles.<p>
     *
     * @param handle the handle to add
     */
    public void addWaitHandle(CmsWaitHandle handle) {

        m_workQueue.add(handle);
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
     * Initializes the cache and installs the update task.<p>
     */
    public void initialize() {

        if (m_taskFuture != null) {
            m_taskFuture.cancel(false);
            m_taskFuture = null;
        }
        reload();
        m_taskFuture = OpenCms.getExecutor().scheduleWithFixedDelay(
            this::performUpdate,
            UPDATE_DELAY_MILLIS,
            UPDATE_DELAY_MILLIS,
            TimeUnit.MILLISECONDS);

    }

    /**
     * The method called by the scheduled update action to update the cache.<p>
     */
    public void performUpdate() {

        // Wrap everything in try-catch because we don't want to leak an exception out of a scheduled task
        try {
            ArrayList<Object> work = new ArrayList<>();
            m_workQueue.drainTo(work);
            Set<CmsUUID> copiedIds = new HashSet<CmsUUID>();
            List<CmsWaitHandle> waitHandles = new ArrayList<>();
            for (Object o : work) {
                if (o instanceof CmsUUID) {
                    copiedIds.add((CmsUUID)o);
                } else if (o instanceof CmsWaitHandle) {
                    waitHandles.add((CmsWaitHandle)o);
                }
            }
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
            if (copiedIds.size() > 0) {
                OpenCms.getADEManager().getCache().flushContainerPages(
                    m_cms.getRequestContext().getCurrentProject().isOnlineProject());
            }
            for (CmsWaitHandle handle : waitHandles) {
                handle.release();
            }
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Reloads the formatter cache.<p>
     */
    public void reload() {

        List<CmsResource> settingConfigResources = new ArrayList<>();
        try {
            I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(TYPE_SETTINGS_CONFIG);
            CmsResourceFilter filter = CmsResourceFilter.ONLY_VISIBLE_NO_DELETED.addRequireType(type);
            settingConfigResources.addAll(m_cms.readResources("/", filter));
        } catch (CmsException e) {
            LOG.warn(e.getLocalizedMessage(), e);
        }

        Map<CmsUUID, Map<CmsSharedSettingKey, CmsXmlContentProperty>> sharedSettingsByStructureId = new HashMap<>();
        for (CmsResource resource : settingConfigResources) {
            Map<CmsSharedSettingKey, CmsXmlContentProperty> sharedSettings = parseSettingsConfig(resource);
            if (sharedSettings != null) {
                sharedSettingsByStructureId.put(resource.getStructureId(), sharedSettings);
            }
        }
        m_settingConfigs = sharedSettingsByStructureId;

        List<CmsResource> formatterResources = new ArrayList<CmsResource>();
        try {
            I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(TYPE_FORMATTER_CONFIG);
            CmsResourceFilter filter = CmsResourceFilter.ONLY_VISIBLE_NO_DELETED.addRequireType(type);
            formatterResources.addAll(m_cms.readResources("/", filter));
        } catch (CmsException e) {
            LOG.warn(e.getLocalizedMessage(), e);
        }
        try {
            I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(TYPE_MACRO_FORMATTER);
            CmsResourceFilter filter = CmsResourceFilter.ONLY_VISIBLE_NO_DELETED.addRequireType(type);
            formatterResources.addAll(m_cms.readResources("/", filter));
            I_CmsResourceType typeFlex = OpenCms.getResourceManager().getResourceType(TYPE_FLEX_FORMATTER);
            CmsResourceFilter filterFlex = CmsResourceFilter.ONLY_VISIBLE_NO_DELETED.addRequireType(typeFlex);
            formatterResources.addAll(m_cms.readResources("/", filterFlex));
            I_CmsResourceType typeFunction = OpenCms.getResourceManager().getResourceType(
                CmsResourceTypeFunctionConfig.TYPE_NAME);
            CmsResourceFilter filterFunction = CmsResourceFilter.ONLY_VISIBLE_NO_DELETED.addRequireType(typeFunction);
            formatterResources.addAll(m_cms.readResources("/", filterFunction));
        } catch (CmsException e) {
            LOG.warn(e.getLocalizedMessage(), e);
        }
        Map<CmsUUID, I_CmsFormatterBean> newFormatters = Maps.newHashMap();
        for (CmsResource formatterResource : formatterResources) {
            I_CmsFormatterBean formatterBean = readFormatter(formatterResource.getStructureId());
            if (formatterBean != null) {
                newFormatters.put(formatterResource.getStructureId(), formatterBean);
            }
        }
        m_state = new CmsFormatterConfigurationCacheState(newFormatters);

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
    public void waitForUpdate() {

        CmsWaitHandle handle = new CmsWaitHandle(true);
        addWaitHandle(handle);
        handle.enter(Long.MAX_VALUE);
    }

    /**
     * Reads a formatter given its structure id and returns it, or null if the formatter couldn't be read.<p>
     *
     * @param structureId the structure id of the formatter configuration
     *
     * @return the formatter bean, or null if no formatter could be read for some reason
     */
    protected I_CmsFormatterBean readFormatter(CmsUUID structureId) {

        I_CmsFormatterBean formatterBean = null;
        CmsResource formatterRes = null;
        try {
            formatterRes = m_cms.readResource(structureId);
            CmsFile formatterFile = m_cms.readFile(formatterRes);
            CmsFormatterBeanParser parser = new CmsFormatterBeanParser(m_cms, m_settingConfigs);
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
        CmsResourceManager manager = OpenCms.getResourceManager();

        if (manager.matchResourceType(TYPE_SETTINGS_CONFIG, resourceType)) {
            // for each formatter configuration, only the combined settings are stored, not
            // the reference to the settings config. So we need to reload everything when a setting configuration
            // changes.
            markForUpdate(RELOAD_MARKER);
            return;
        }

        if (manager.matchResourceType(TYPE_FORMATTER_CONFIG, resourceType)
            || manager.matchResourceType(TYPE_MACRO_FORMATTER, resourceType)
            || manager.matchResourceType(TYPE_FLEX_FORMATTER, resourceType)
            || manager.matchResourceType(CmsResourceTypeFunctionConfig.TYPE_NAME, resourceType)) {
            markForUpdate(structureId);
        }
    }

    /**
     * Adds a formatter structure id to the update set, and schedule an update task unless one is already scheduled.<p>
     *
     * @param structureId the structure id of the formatter configuration
     */
    private void markForUpdate(CmsUUID structureId) {

        m_workQueue.add(structureId);
    }

    /**
     * Helper method for parsing a settings configuration file.
     *
     * <p> If a setting definition contains formatter keys, then one entry for each formatter key will be added to the result
     * map, otherwise just one general map entry with formatterKey = null will be generated for that setting.
     *
     * @param resource the resource to parse
     * @return the parsed setting definitions
     */
    private Map<CmsSharedSettingKey, CmsXmlContentProperty> parseSettingsConfig(CmsResource resource) {

        Map<CmsSharedSettingKey, CmsXmlContentProperty> result = new HashMap<>();
        try {
            CmsFile settingFile = m_cms.readFile(resource);
            CmsXmlContent settingContent = CmsXmlContentFactory.unmarshal(m_cms, settingFile);
            CmsXmlContentRootLocation location = new CmsXmlContentRootLocation(settingContent, Locale.ENGLISH);
            for (I_CmsXmlContentValueLocation settingLoc : location.getSubValues(CmsFormatterBeanParser.N_SETTING)) {
                CmsXmlContentProperty setting = CmsConfigurationReader.parseProperty(
                    m_cms,
                    settingLoc).getPropertyData();
                String includeName = setting.getIncludeName(setting.getName());
                if (includeName == null) {
                    LOG.warn(
                        "No include name defined for setting in "
                            + resource.getRootPath()
                            + ", setting = "
                            + ReflectionToStringBuilder.toString(setting, ToStringStyle.SHORT_PREFIX_STYLE));
                    continue;
                }
                Set<String> formatterKeys = new HashSet<>();
                for (I_CmsXmlContentValueLocation formatterKeyLoc : settingLoc.getSubValues(N_FORMATTER_KEY)) {
                    String formatterKey = formatterKeyLoc.getValue().getStringValue(m_cms);
                    if (!CmsStringUtil.isEmptyOrWhitespaceOnly(formatterKey)) {
                        formatterKeys.add(formatterKey.trim());
                    }
                }
                if (formatterKeys.size() == 0) {
                    result.put(new CmsSharedSettingKey(includeName, null), setting);
                } else {
                    for (String formatterKey : formatterKeys) {
                        result.put(new CmsSharedSettingKey(includeName, formatterKey), setting);

                    }
                }
            }
            return result;
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage());
            return null;
        }
    }
}
