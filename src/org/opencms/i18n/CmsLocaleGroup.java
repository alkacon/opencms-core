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

package org.opencms.i18n;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

/**
 * Represents a group of resources which are locale variants of each other.<p>
 */
public class CmsLocaleGroup {

    /** The CMS context to use. */
    private CmsObject m_cms;

    /** The primary resource. */
    private CmsResource m_primaryResource;

    /** The secondary resources. */
    private Set<CmsResource> m_secondaryResources;

    /** The locale cache. */
    private Map<CmsResource, Locale> m_localeCache = Maps.newHashMap();

    /** Map of resources by locale. */
    private Multimap<Locale, CmsResource> m_resourcesByLocale = ArrayListMultimap.create();

    /**
     * Creates a new instance.<p>
     *
     * @param cms the CMS context to use
     * @param primaryResource the primary resource
     * @param secondaryResources the secondary resources
     */
    public CmsLocaleGroup(CmsObject cms, CmsResource primaryResource, List<CmsResource> secondaryResources) {
        m_primaryResource = primaryResource;
        m_secondaryResources = Sets.newHashSet(secondaryResources);
        m_cms = cms;
        initLocales();
    }

    /**
     * Gets the primary resource.<p>
     *
     * @return the primary resource
     */
    public CmsResource getPrimaryResource() {

        return m_primaryResource;
    }

    /**
     * Gets the resources of this group which have the given locale.<p>
     *
     * @param locale a locale
     * @return the collection of resources with the given locale
     */
    public Collection<CmsResource> getResourcesForLocale(Locale locale) {

        return Lists.newArrayList(m_resourcesByLocale.get(locale));
    }

    /**
     * Gets the secondary resources of this group.<p>
     *
     * @return the collection of secondary resources
     */
    public Set<CmsResource> getSecondaryResources() {

        return Collections.unmodifiableSet(m_secondaryResources);
    }

    /**
     * Checks if this group has a resource with the given locale.<p>
     *
     * @param locale the locale
     * @return true  if the group has a resource with the locale
     * @throws CmsException if something goes wrong
     */
    public boolean hasLocale(Locale locale) throws CmsException {

        return m_resourcesByLocale.containsKey(locale);
    }

    /**
     * Checks if this group is a real group, i.e. consists of more than one resource.<p>
     *
     * @return true if this is a real group
     */
    public boolean isRealGroup() {

        return m_secondaryResources.size() > 0;
    }

    /**
     * Initializes the locales.<p>
     *
     * @throws CmsException if something goes wrong
     */
    private void initLocales() {

        if (!m_localeCache.isEmpty()) {
            return;
        }
        readLocale(m_primaryResource);
        for (CmsResource resource : m_secondaryResources) {
            readLocale(resource);
        }
        for (Map.Entry<CmsResource, Locale> entry : m_localeCache.entrySet()) {
            CmsResource key = entry.getKey();
            Locale value = entry.getValue();
            m_resourcesByLocale.put(value, key);
        }
    }

    /**
     * Reads the locale for the given resource.<p>
     *
     * @param res the locale for the resource
     */
    private void readLocale(CmsResource res) {

        Locale locale = OpenCms.getLocaleManager().getDefaultLocale(m_cms, res);
        m_localeCache.put(res, locale);
    }

}
