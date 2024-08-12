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

package org.opencms.ade.containerpage.shared;

import org.opencms.gwt.shared.CmsAdditionalInfoBean;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.gwt.shared.CmsTemplateContextInfo;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Bean holding all element information including it's formatted contents.<p>
 *
 * @since 8.0.0
 */
public class CmsContainerElementData extends CmsContainerElement {

    /** The contents by container type. */
    private Map<String, String> m_contents;

    private CmsListInfoBean m_listInfo;

    /** The group-container description. */
    private String m_description;

    /** Dragging an element may require changing its settings, but this changes the id since it is computed from the settings. In the DND case this field contains the client id of the element with the changed settings. */
    private String m_dndId;

    /** The formatter configurations by container. */
    private Map<String, CmsFormatterConfigCollection> m_formatters;

    /** The inheritance infos off all sub-items. */
    private List<CmsInheritanceInfo> m_inheritanceInfos = new ArrayList<CmsInheritanceInfo>();

    /** The inheritance name. */
    private String m_inheritanceName;

    /** Indicates whether this element is used as a group. */
    private boolean m_isGroup;

    /** The last user modifying the element. */
    private String m_lastModifiedByUser;

    /** The date of last modification. */
    private long m_lastModifiedDate;

    /** The time this element was loaded. */
    private long m_loadTime;

    /** The element navText property. */
    private String m_navText;

    /** The settings for this container entry. */
    private Map<String, String> m_settings;

    /** The contained sub-item id's. */
    private List<String> m_subItems = new ArrayList<String>();

    /** The title. */
    private String m_title;

    /** True if the element's type is disabled in the sitemap configuration. */
    private boolean m_addDisabled;

    /** The supported container types of a group-container. */
    private Set<String> m_types;

    private boolean m_copyDisabled;

    /**
     * Creates a new instance.
     */
    public CmsContainerElementData() {

        // do nothing
    }

    /**
     * Returns the contents.<p>
     *
     * @return the contents
     */
    public Map<String, String> getContents() {

        return m_contents;
    }

    /**
     * Returns the required css resources.<p>
     *
     * @param containerName the current container name
     *
     * @return the required css resources
     */
    public Set<String> getCssResources(String containerName) {

        CmsFormatterConfig formatterConfig = getFormatterConfig(containerName);
        return formatterConfig != null ? formatterConfig.getCssResources() : Collections.<String> emptySet();
    }

    /**
     * Returns the description.<p>
     *
     * @return the description
     */
    public String getDescription() {

        return m_description;
    }

    /**
     * Dragging an element may require changing its settings, but this changes the id since it is computed from the settings. In the DND case this method returns the client id of the element with the changed settings.<p>
     *
     * @return the drag and drop element id, or null if it isn't available or needed
     */
    public String getDndId() {

        return m_dndId;
    }

    /**
     * Returns the individual element settings formated with nice-names to be used as additional-info.<p>
     *
     * @param containerId the container id
     *
     * @return the settings list
     */
    public List<CmsAdditionalInfoBean> getFormatedIndividualSettings(String containerId) {

        List<CmsAdditionalInfoBean> result = new ArrayList<CmsAdditionalInfoBean>();
        CmsFormatterConfig config = getFormatterConfig(containerId);
        if ((m_settings != null) && (config != null)) {
            for (Entry<String, String> settingEntry : m_settings.entrySet()) {
                String settingKey = settingEntry.getKey();
                if (config.getSettingConfig().containsKey(settingEntry.getKey())) {
                    String niceName = config.getSettingConfig().get(settingEntry.getKey()).getNiceName();
                    if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(
                        config.getSettingConfig().get(settingEntry.getKey()).getNiceName())) {
                        settingKey = niceName;
                    }
                }
                result.add(new CmsAdditionalInfoBean(settingKey, settingEntry.getValue(), null));
            }
        }
        return result;
    }

    /**
     * Returns the current formatter configuration.<p>
     *
     * @param containerName the current container name
     *
     * @return the current formatter configuration
     */
    public CmsFormatterConfig getFormatterConfig(String containerName) {

        CmsFormatterConfig formatterConfig = null;
        if (m_formatters != null) {
            String keyOrId = getSettings().get(CmsFormatterConfig.getSettingsKeyForContainer(containerName));
            if (keyOrId == null) {
                keyOrId = getSettings().get(CmsFormatterConfig.getSettingsKeyForContainer(""));
            }
            CmsFormatterConfigCollection formattersForContainer = getFormatters().get(containerName);
            if ((keyOrId != null) && (formattersForContainer != null)) {
                formatterConfig = formattersForContainer.get(keyOrId);
                if (formatterConfig == null) {
                    int separatorPos = keyOrId.lastIndexOf(CmsGwtConstants.FORMATTER_SUBKEY_SEPARATOR);
                    if (separatorPos != -1) {
                        String parentKey = keyOrId.substring(0, separatorPos);
                        formatterConfig = formattersForContainer.get(parentKey);
                    }
                }
            }
            if (formatterConfig == null) {
                if (getFormatters().containsKey(containerName)) {
                    try {
                        formatterConfig = getFormatters().get(containerName).getFirstFormatter();
                    } catch (NoSuchElementException e) {
                        // formatterConfig remains null
                    }
                }
            }
        }
        return formatterConfig;
    }

    /**
     * Gets the formatter collections for the different containers.
     *
     * @return the formatter collections for the containers
     */
    public Map<String, CmsFormatterConfigCollection> getFormatters() {

        return m_formatters;
    }

    /**
     * Returns the inheritance infos off all sub-items.<p>
     *
     * @return the inheritance infos off all sub-items.
     */
    public List<CmsInheritanceInfo> getInheritanceInfos() {

        if (isInheritContainer()) {
            return m_inheritanceInfos;
        } else {
            throw new UnsupportedOperationException("Only inherit containers have inheritance infos");
        }
    }

    /**
     * Returns the inheritance name.<p>
     *
     * @return the inheritance name
     */
    public String getInheritanceName() {

        return m_inheritanceName;
    }

    /**
     * Returns the last modifying user.<p>
     *
     * @return the last modifying user
     */
    public String getLastModifiedByUser() {

        return m_lastModifiedByUser;
    }

    /**
     * Returns the last modification date.<p>
     *
     * @return the last modification date
     */
    public long getLastModifiedDate() {

        return m_lastModifiedDate;
    }

    /**
     * Gets the list info bean.
     *
     * @return the list info bean
     */
    public CmsListInfoBean getListInfo() {

        return m_listInfo;
    }

    /**
     * Returns the load time.<p>
     *
     * @return the load time
     */
    public long getLoadTime() {

        return m_loadTime;
    }

    /**
     * Returns the navText.<p>
     *
     * @return the navText
     */
    public String getNavText() {

        return m_navText;
    }

    /**
     * Gets the setting configuration for this container element.<p>
     *
     * @param containerName the current container name
     *
     * @return the setting configuration map
     */
    public Map<String, CmsXmlContentProperty> getSettingConfig(String containerName) {

        CmsFormatterConfig formatterConfig = getFormatterConfig(containerName);
        return formatterConfig != null
        ? formatterConfig.getSettingConfig()
        : Collections.<String, CmsXmlContentProperty> emptyMap();
    }

    /**
     * Returns the settings for this container element.<p>
     *
     * @return a map of settings
     */
    public Map<String, String> getSettings() {

        return m_settings;
    }

    /**
     * Returns the sub-items.<p>
     *
     * @return the sub-items
     */
    public List<String> getSubItems() {

        if (isGroupContainer()) {
            return m_subItems;
        } else if (isInheritContainer()) {
            List<String> result = new ArrayList<String>();
            for (CmsInheritanceInfo info : m_inheritanceInfos) {
                if (info.isVisible()) {
                    result.add(info.getClientId());
                }
            }
            return result;
        } else {
            throw new UnsupportedOperationException("Only group or inherit containers have sub-items");
        }
    }

    /**
     * Returns the supported container types.<p>
     *
     * @return the supported container types
     */
    @Override
    public String getTitle() {

        return m_title;
    }

    /**
     * If this element represents an element group, this method will return the supported container type.<p>
     *
     * @return the supported container types
     */
    public Set<String> getTypes() {

        return m_types;
    }

    /**
     * Returns if there are alternative formatters available for the given container.<p>
     *
     * @param containerName the container name
     *
     * @return <code>true</code> if there are alternative formatters available for the given container
     */
    public boolean hasAlternativeFormatters(String containerName) {

        return (m_formatters.get(containerName) != null) && (m_formatters.get(containerName).size() > 1);
    }

    /**
     * @see org.opencms.ade.containerpage.shared.CmsContainerElement#hasSettings(java.lang.String)
     */
    @Override
    public boolean hasSettings(String containerId) {

        // in case formatter info is not available, do the simple check
        if ((m_formatters == null) || m_formatters.isEmpty()) {
            return super.hasSettings(containerId);
        } else {
            CmsFormatterConfig config = getFormatterConfig(containerId);
            return (config != null) && (!config.getSettingConfig().isEmpty() || hasAlternativeFormatters(containerId));
        }
    }

    /**
     * Returns true if the element's type is disabled in the sitemap configuration.
     *
     * @return true if the type is disabled
     */
    public boolean isAddDisabled() {

        return m_addDisabled;
    }

    /**
     * Returns true if copying of the element should be disabled.
     *
     * @return true if copying of the element should be disabled
     */
    public boolean isCopyDisabled() {

        return m_copyDisabled;
    }

    /**
     * Returns if this element is used as a group.<p>
     *
     * @return if this element is used as a group
     */
    public boolean isGroup() {

        return m_isGroup;
    }

    /**
     * Returns true if the element should be shown in the current template context.<p>
     *
     * @param currentContext the current template context
     *
     * @return true if the element should be shown
     */
    public boolean isShowInContext(String currentContext) {

        if ((m_settings == null)
            || !m_settings.containsKey(CmsTemplateContextInfo.SETTING)
            || CmsStringUtil.isEmptyOrWhitespaceOnly(m_settings.get(CmsTemplateContextInfo.SETTING))) {
            return true;
        }
        return CmsStringUtil.splitAsList(m_settings.get(CmsTemplateContextInfo.SETTING), "|").contains(currentContext);
    }

    /**
     * Sets the 'is type disabled' flag (type is disabled in the sitemap configuration).
     *
     * @param typeDisabled the new value
     */
    public void setAddDisabled(boolean typeDisabled) {

        m_addDisabled = typeDisabled;
    }

    /**
     * Sets the contents.<p>
     *
     * @param contents the contents to set
     */
    public void setContents(Map<String, String> contents) {

        m_contents = contents;
    }

    /**
     * Sets the 'copy disabled' status.
     *
     * @param copyDisabled true if copying should be disabled
     */
    public void setCopyDisabled(boolean copyDisabled) {

        m_copyDisabled = copyDisabled;
    }

    /**
     * Sets the description.<p>
     *
     * @param description the description to set
     */
    public void setDescription(String description) {

        m_description = description;
    }

    /**
     * During dragging and dropping in the container page editor, it may be required to substitute a different element for the element being dragged. This sets the id of the element to substitute.<p>
     *
     * @param dndId the drag and drop replacement element's client id
     */
    public void setDndId(String dndId) {

        m_dndId = dndId;
    }

    /**
     * Sets the formatter configurations.<p>
     *
     * @param formatters the formatter configurations to set
     */
    public void setFormatters(Map<String, CmsFormatterConfigCollection> formatters) {

        m_formatters = formatters;
    }

    /**
     * Sets if this element is used as a group.<p>
     *
     * @param isGroup if this element is used as a group
     */
    public void setGroup(boolean isGroup) {

        m_isGroup = isGroup;
    }

    /**
     * Sets the inheritance infos.<p>
     *
     * @param inheritanceInfos the inheritance infos to set
     */
    public void setInheritanceInfos(List<CmsInheritanceInfo> inheritanceInfos) {

        m_inheritanceInfos = inheritanceInfos;
    }

    /**
     * Sets the inheritance name.<p>
     *
     * @param inheritanceName the inheritance name to set
     */
    public void setInheritanceName(String inheritanceName) {

        m_inheritanceName = inheritanceName;
    }

    /**
     * Sets the modifying userdByUser.<p>
     *
     * @param lastModifiedByUser the last modifying user to set
     */
    public void setLastModifiedByUser(String lastModifiedByUser) {

        m_lastModifiedByUser = lastModifiedByUser;
    }

    /**
     * Sets the last modification date.<p>
     *
     * @param lastModifiedDate the last modification date to set
     */
    public void setLastModifiedDate(long lastModifiedDate) {

        m_lastModifiedDate = lastModifiedDate;
    }

    /**
     * Sets the list info bean.
     *
     * @param listInfo the list info bean
     */
    public void setListInfo(CmsListInfoBean listInfo) {

        m_listInfo = listInfo;
    }

    /**
     * Sets the load time.<p>
     *
     * @param loadTime the load time to set
     */
    public void setLoadTime(long loadTime) {

        m_loadTime = loadTime;
    }

    /**
     * Sets the navText.<p>
     *
     * @param navText the navText to set
     */
    public void setNavText(String navText) {

        m_navText = navText;
    }

    /**
     * Sets the settings for this container element.<p>
     *
     * @param settings the new settings
     */
    public void setSettings(Map<String, String> settings) {

        m_settings = settings;
    }

    /**
     * Sets the sub-items.<p>
     *
     * @param subItems the sub-items to set
     */
    public void setSubItems(List<String> subItems) {

        m_subItems = subItems;
    }

    /**
     * Sets the title.<p>
     *
     * @param title the title to set
     */
    @Override
    public void setTitle(String title) {

        m_title = title;
    }

    /**
     * Sets the supported container types.<p>
     *
     * @param types the supported container types to set
     */
    public void setTypes(Set<String> types) {

        m_types = types;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        StringBuffer result = new StringBuffer();
        result.append("Title: ").append(getTitle()).append("  File: ").append(getSitePath()).append(
            "  ClientId: ").append(getClientId());
        return result.toString();
    }

}
