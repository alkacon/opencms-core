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

package org.opencms.i18n;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

/**
 * Represents a group of resources which are locale variants of each other.<p>
 */
public class CmsLocaleGroup {

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsLocaleGroup.class);

    /** The CMS context to use. */
    private CmsObject m_cms;

    /** The locale cache. */
    private Map<CmsResource, Locale> m_localeCache = Maps.newHashMap();

    /** The 'no translation' setting for this locale group. */
    private String m_noTranslation;

    /** The primary resource. */
    private CmsResource m_primaryResource;

    /** Map of resources by locale. */
    private Multimap<Locale, CmsResource> m_resourcesByLocale = ArrayListMultimap.create();

    /** The secondary resources. */
    private Set<CmsResource> m_secondaryResources;

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
     * Gets the list of all resources of this group (primary and secondary).<p>
     *
     * @return the list of all resources of this group
     */
    public List<CmsResource> getAllResources() {

        List<CmsResource> result = Lists.newArrayList();
        result.add(m_primaryResource);
        for (CmsResource res : getSecondaryResources()) {
            result.add(res);
        }
        return result;
    }

    /**
     * Gets the main locale (i.e. the locale of the primary resource of this group).<p>
     *
     * @return the main locale
     */
    public Locale getMainLocale() {

        return m_localeCache.get(m_primaryResource);
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
     * Gets a map which contains the resources of the locale group as keys, indexed by their locale.<p>
     *
     * If the locale group contains more than one resource from the same locale,, which one is used a map value is undefined.
     *
     * @return the map of resources by locale
     */
    public Map<Locale, CmsResource> getResourcesByLocale() {

        List<CmsResource> resources = Lists.newArrayList();
        resources.add(m_primaryResource);
        resources.addAll(m_secondaryResources);
        Collections.sort(resources, new Comparator<CmsResource>() {

            public int compare(CmsResource arg0, CmsResource arg1) {

                String path1 = arg0.getRootPath();
                String path2 = arg1.getRootPath();
                return path2.compareTo(path1);
            }

        });
        Map<Locale, CmsResource> result = new LinkedHashMap<Locale, CmsResource>();
        for (CmsResource resource : resources) {
            result.put(m_localeCache.get(resource), resource);
        }
        return result;

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
     */
    public boolean hasLocale(Locale locale) {

        return m_resourcesByLocale.containsKey(locale);
    }

    /**
     * Checks if the locale group is marked as not translatable for the given locale.<p>
     *
     * @param locale a locale
     *
     * @return true if the locale group is marked as not translatable for the given locale
     */
    public boolean isMarkedNoTranslation(Locale locale) {

        return (m_noTranslation != null) && CmsLocaleManager.getLocales(m_noTranslation).contains(locale);
    }

    /**
     * Checks if the locale group is marked as not translatable for any of the given locales.<p>
     *
     * @param locales a set of locales
     * @return true if the locale group is marked as not translatable for any of the  given resources
     */
    public boolean isMarkedNoTranslation(Set<Locale> locales) {

        if (m_noTranslation == null) {
            return false;
        }
        List<Locale> noTranslationLocales = CmsLocaleManager.getLocales(m_noTranslation);
        for (Locale locale : noTranslationLocales) {
            if (locales.contains(locale)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if this is a potential group head, i.e. the locale of the primary resource is the main translation locale configured for the site
     * in which it is located.<p>
     *
     * @return true if this is a potential group head
     */
    public boolean isPotentialGroupHead() {

        CmsSite site = OpenCms.getSiteManager().getSiteForRootPath(m_primaryResource.getRootPath());
        if (site == null) {
            return false;
        }
        Locale mainLocale = site.getMainTranslationLocale(null);
        if (mainLocale == null) {
            return false;
        }
        Locale primaryLocale = getMainLocale();
        return mainLocale.equals(primaryLocale);

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
     * Checks if this is either a real group or a potential group head (i.e. a potential primary resource).<p>
     *
     * @return true if this is a real group or a potential group head
     */
    public boolean isRealGroupOrPotentialGroupHead() {

        return isRealGroup() || isPotentialGroupHead();
    }

    /**
     * Gets the locales of the resources from  this locale group.<p>
     *
     * @return the locales of this locale group
     */
    Set<Locale> getLocales() {

        return Sets.newHashSet(getResourcesByLocale().keySet());
    }

    /**
     * Initializes the locales.<p>
     *
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
        try {
            CmsProperty noTranslationProp = m_cms.readPropertyObject(
                m_primaryResource,
                CmsPropertyDefinition.PROPERTY_LOCALE_NOTRANSLATION,
                false);
            m_noTranslation = noTranslationProp.getValue();
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);

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
