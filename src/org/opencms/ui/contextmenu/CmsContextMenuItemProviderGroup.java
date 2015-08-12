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

package org.opencms.ui.contextmenu;

import org.opencms.main.CmsLog;
import org.opencms.util.CmsUUID;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.apache.commons.logging.Log;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.MutableClassToInstanceMap;

/**
 * Class used to manage multiple context menu item providers through a single instance.<p>
 *
 * Also keeps track of available menu entries by their global id.<p>
 */
public class CmsContextMenuItemProviderGroup implements I_CmsContextMenuItemProvider {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsContextMenuItemProviderGroup.class);

    /** Map of provider classes. */
    ClassToInstanceMap<I_CmsContextMenuItemProvider> m_providerMap = MutableClassToInstanceMap.create();

    /** Item cache. */
    private List<I_CmsContextMenuItem> m_itemCache = Lists.newArrayList();

    /** Cache of items by global id. */
    private Map<String, I_CmsContextMenuItem> m_itemsByGlobalId = Maps.newHashMap();

    /**
     * Creates a new instance.<p>
     */
    public CmsContextMenuItemProviderGroup() {
        ServiceLoader<I_CmsContextMenuItemProvider> providerLoader = ServiceLoader.load(
            I_CmsContextMenuItemProvider.class);
        for (I_CmsContextMenuItemProvider provider : providerLoader) {
            addProvider(provider);
        }

    }

    /**
     * Adds a new provider class.<p>
     *
     * @param providerClass the provider class
     */
    public void addProvider(Class<? extends I_CmsContextMenuItemProvider> providerClass) {

        try {
            m_providerMap.put(providerClass, providerClass.newInstance());
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Adds a provider.<p>
     *
     * Note that no two providers of the same exact class may be added.<p>
     *
     * @param instance the provider instance to add
     */
    public void addProvider(I_CmsContextMenuItemProvider instance) {

        m_providerMap.put(instance.getClass(), instance);

    }

    /**
     * Gets the context menu item by its global id.<p>
     *
     * @param globalId the global id
     *
     * @return the menu item with the global id, or null if none was found
     */
    public I_CmsContextMenuItem getItemByGlobalId(String globalId) {

        return m_itemsByGlobalId.get(globalId);
    }

    /**
     * @see org.opencms.ui.contextmenu.I_CmsContextMenuItemProvider#getMenuItems()
     */
    public List<I_CmsContextMenuItem> getMenuItems() {

        return Collections.unmodifiableList(m_itemCache);
    }

    /**
     * Initializes this instance.<p>
     *
     * This must be called after the provider classes have been added.
     */
    public synchronized void initialize() {

        List<I_CmsContextMenuItem> result = Lists.newArrayList();
        for (I_CmsContextMenuItemProvider provider : m_providerMap.values()) {
            result.addAll(provider.getMenuItems());
        }
        for (I_CmsContextMenuItem item : result) {
            String globalId = item.getGlobalId();
            if (globalId == null) {
                globalId = "" + new CmsUUID();
                item.setGlobalId(globalId);
            }
            // Note that we don't clear m_itemsByGlobalId. This is because
            // we use global ids to uniquely identify context menu items on the client,
            // and a call to initialize might happen while there is still an active client session
            // using 'old' context menu items (obtained before the call to initialize()). Since we can't really
            // guarantee that the 'same' menu item always has the same global id, we just leave the old items
            // in the map instead.
            m_itemsByGlobalId.put(item.getGlobalId(), item);
        }
        m_itemCache.clear();
        m_itemCache.addAll(result);
    }

}
