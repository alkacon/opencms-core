/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.galleries.shared;

import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryTabId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A bean containing a configuration for the gallery dialog's available tabs,
 * consisting of a list of tabs and a default tab to display first.
 */
public class CmsGalleryTabConfiguration implements IsSerializable {

    /** The map containing the predefined tab configurations. */
    public static final Map<String, CmsGalleryTabConfiguration> DEFAULT_CONFIGURATIONS;

    /** Gallery configuration id. */
    public static final String TC_ADE_ADD = "adeAdd";

    /** Gallery configuration id. */
    public static final String TC_FOLDERS = "folders";

    /** Gallery configuration id. */
    public static final String TC_GALLERIES = "galleries";

    /** Gallery configuration id. */
    public static final String TC_SELECT_ALL = "selectAll";

    /** Gallery configuration id. */
    public static final String TC_SELECT_DOC = "selectDoc";

    /** Gallery confiugration id. */
    public static final String TC_SELECT_ALL_NO_SITEMAP = "selectAllNoSitemap";

    static {
        Map<String, CmsGalleryTabConfiguration> defaultConfigs = new HashMap<String, CmsGalleryTabConfiguration>();
        defaultConfigs.put(TC_SELECT_ALL, parse("*sitemap,types,galleries,categories,vfstree,search,results"));
        defaultConfigs.put(TC_SELECT_ALL_NO_SITEMAP, parse("*types,galleries,categories,vfstree,search,results"));
        defaultConfigs.put(TC_SELECT_DOC, parse("types,*galleries,categories,vfstree,search,results"));
        defaultConfigs.put(TC_ADE_ADD, parse("*types,galleries,categories,vfstree,search,results"));
        defaultConfigs.put(TC_FOLDERS, parse("*vfstree"));
        defaultConfigs.put(TC_GALLERIES, parse("*galleries,vfstree,results"));
        DEFAULT_CONFIGURATIONS = Collections.unmodifiableMap(defaultConfigs);
    }

    /** The id of the default tab. */
    protected GalleryTabId m_defaultTab;

    /** The list of tab ids. */
    private List<GalleryTabId> m_tabs;

    /**
     * Creates  a new gallery tab configuration based on a list of tabs.<p>
     *
     * @param tabsList the list of tabs
     */
    public CmsGalleryTabConfiguration(List<GalleryTabId> tabsList) {

        m_tabs = tabsList;
    }

    /**
     * Default constructor for serialization.<p>
     */
    protected CmsGalleryTabConfiguration() {

        // only used for serialization
    }

    /**
     * Gets the default tab configuration.<p>
     *
     * @return the default tab configuration
     */
    public static CmsGalleryTabConfiguration getDefault() {

        return resolve(TC_SELECT_ALL);
    }

    /**
     * Creates a gallery tab configuration from a configuration string.<p>
     *
     * The string should consist of a comma-separated list of tab ids, omitting the cms_tab_ prefix of the corresponding enum values.
     * The tab which should be used as a default tab should be prefixed with an asterisk '*'.
     *
     * @param configStr the configuration string
     *
     * @return the parsed tab configuration
     */
    public static CmsGalleryTabConfiguration parse(String configStr) {

        String[] tokens = configStr.split(" *, *");
        GalleryTabId defaultTabId = null;
        // use LinkedHashMap to preserve both order and uniqueness of tabs
        LinkedHashMap<GalleryTabId, Object> tabs = new LinkedHashMap<GalleryTabId, Object>();
        for (String token : tokens) {
            token = token.trim();
            boolean isDefault = false;
            if (token.startsWith("*")) {
                token = token.substring(1).trim();
                isDefault = true;
            }
            GalleryTabId tab = parseTabId(token);
            if (tab != null) {
                tabs.put(tab, null);
                if (isDefault && (defaultTabId == null)) {
                    defaultTabId = tab;
                }
            }
        }
        List<GalleryTabId> tabsList = new ArrayList<GalleryTabId>();
        for (GalleryTabId tab : tabs.keySet()) {
            tabsList.add(tab);
        }
        if (defaultTabId == null) {
            defaultTabId = tabsList.get(0);
        }
        return new CmsGalleryTabConfiguration(tabsList).withDefault(defaultTabId);

    }

    /**
     * Parses a tab id from a gallery configuration string.<p>
     *
     * @param tabId the tab id to parse
     *
     * @return the gallery tab id enum value
     */
    public static GalleryTabId parseTabId(String tabId) {

        try {
            return GalleryTabId.valueOf("cms_tab_" + tabId);
        } catch (Throwable e) {
            return null;
        }

    }

    /**
     * Given a string which is either the name of a predefined tab configuration or a configuration string, returns
     * the corresponding tab configuration.
     *
     * @param configStr a configuration string or predefined configuration name
     *
     * @return the gallery tab configuration
     */
    public static CmsGalleryTabConfiguration resolve(String configStr) {

        CmsGalleryTabConfiguration tabConfig;
        if (DEFAULT_CONFIGURATIONS != null) {
            tabConfig = DEFAULT_CONFIGURATIONS.get(configStr);
            if (tabConfig != null) {
                return tabConfig;
            }
        }
        return parse(configStr);
    }

    /**
     * Gets the default tab.<p>
     *
     * @return the default tab
     */
    public GalleryTabId getDefaultTab() {

        return m_defaultTab;
    }

    /**
     * Gets the list of tabs.<p>
     *
     * @return the list of tabs
     */
    public List<GalleryTabId> getTabs() {

        return Collections.unmodifiableList(m_tabs);

    }

    /**
     * Creates a new tab configuration based on this one, but changes its default tab.<P>
     *
     * @param defaultTab the new default tab
     * @return the copy with the changed default tab
     */
    public CmsGalleryTabConfiguration withDefault(GalleryTabId defaultTab) {

        CmsGalleryTabConfiguration result = new CmsGalleryTabConfiguration(m_tabs);
        result.m_defaultTab = defaultTab;
        return result;
    }

}
