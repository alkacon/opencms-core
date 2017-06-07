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

package org.opencms.ui.contextmenu;

import org.opencms.main.CmsLog;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import org.apache.commons.logging.Log;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.Lists;
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

    /**
     * Creates a new instance.<p>
     */
    public CmsContextMenuItemProviderGroup() {
        Iterator<I_CmsContextMenuItemProvider> providersIt = ServiceLoader.load(
            I_CmsContextMenuItemProvider.class).iterator();
        while (providersIt.hasNext()) {
            try {
                addProvider(providersIt.next());
            } catch (Throwable t) {
                LOG.error("Error loading context menu provider from classpath.", t);
            }
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
        m_itemCache = result;
    }

}
