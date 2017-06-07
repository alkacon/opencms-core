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

package org.opencms.ui.sitemap;

import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.i18n.CmsLocaleGroup;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsUUID;

import java.util.Collection;
import java.util.Locale;

import org.apache.commons.logging.Log;

/**
 * Represents the data of a sitemap tree node.
 */
public class CmsSitemapTreeNodeData {

    /** The log instance for this class. */
    @SuppressWarnings("unused")
    private static final Log LOG = CmsLog.getLog(CmsSitemapTreeNodeData.class);

    /** The client sitemap entry. */
    private CmsClientSitemapEntry m_entry;

    /** Indicates whether we have definitely no children. */
    private boolean m_hasNoChildren;

    /** Sitmap entry is copyable (ie has the 'Copy page' option). */
    private boolean m_isCopyable;

    /** Flag indicating whether the linked resource is directly linked. */
    private boolean m_isDirectLink;

    /** The linked resource. */
    private CmsResource m_linkedResource;

    /** String containing locales for which no translations should be provided, read from locale.notranslation property. */
    private String m_noTranslation;

    /** The other locale. */
    private Locale m_otherLocale;

    /** The entry resource. */
    private CmsResource m_resource;

    /**
     * Creates a new instance.<p>
     *
     * @param mainLocale the main locale
     * @param otherLocale the other locale
     */
    public CmsSitemapTreeNodeData(Locale mainLocale, Locale otherLocale) {
        m_otherLocale = otherLocale;
    }

    /**
     * Gets the client sitemap entry.<p>
     *
     * @return the client sitemap entry
     */
    public CmsClientSitemapEntry getClientEntry() {

        return m_entry;
    }

    /**
     * Gets the linked resource.<p>
     *
     * @return the linked resource
     */
    public CmsResource getLinkedResource() {

        return m_linkedResource;
    }

    /**
     * Gets the sitemap entry resource.<p>
     *
     * @return the resource
     */
    public CmsResource getResource() {

        return m_resource;

    }

    /**
     * Returns true if the node definitely has no children to load.<p>
     *
     * @return true if there are definitely no children to load
     */
    public boolean hasNoChildren() {

        return m_hasNoChildren;
    }

    /**
     * Initializes the bean.<p>
     *
     * @param cms the CMS context to use
     *
     * @throws CmsException if something goes wrong
     */
    public void initialize(CmsObject cms) throws CmsException {

        CmsUUID id = m_entry.getId();
        CmsResource resource = cms.readResource(id, CmsResourceFilter.IGNORE_EXPIRATION);
        m_resource = resource;
        CmsResource defaultFile = resource;
        if (resource.isFolder()) {
            defaultFile = cms.readDefaultFile(resource, CmsResourceFilter.IGNORE_EXPIRATION);
        }
        CmsLocaleGroup localeGroup = cms.getLocaleGroupService().readLocaleGroup(defaultFile);
        CmsResource primary = localeGroup.getPrimaryResource();
        CmsProperty noTranslationProp = cms.readPropertyObject(
            primary,
            CmsPropertyDefinition.PROPERTY_LOCALE_NOTRANSLATION,
            false);
        m_noTranslation = noTranslationProp.getValue();
        CmsUUID defaultFileId = (defaultFile != null) ? defaultFile.getStructureId() : null;
        m_isCopyable = (defaultFile != null) && CmsResourceTypeXmlContainerPage.isContainerPage(defaultFile);
        Collection<CmsResource> resourcesForTargetLocale = localeGroup.getResourcesForLocale(m_otherLocale);
        if (!resourcesForTargetLocale.isEmpty()) {
            m_linkedResource = resourcesForTargetLocale.iterator().next();
            if (primary.getStructureId().equals(m_resource.getStructureId())
                || primary.getStructureId().equals(defaultFileId)
                || primary.getStructureId().equals(m_linkedResource.getStructureId())) {
                m_isDirectLink = true;
            }
        }
    }

    /**
     * Returns true if the 'Copy page' function should be offered for this entry.<p>
     *
     * @return true if the 'copy pgae' function should be available
     */
    public boolean isCopyable() {

        return m_isCopyable;
    }

    /**
     * Returns true if the linked resource is directly linked.<p>
     *
     * @return true if the linked resource is directly linked
     */
    public boolean isDirectLink() {

        return m_isDirectLink;

    }

    /**
     * Returns true if this sitemap entry has a linked resource.<p>
     *
     * @return true if there is a linked resource
     */
    public boolean isLinked() {

        return m_linkedResource != null;
    }

    /**
     * Checks if this entry is marked as 'do not translate' for the given locale .<p>
     *
     * @param locale the locale
     * @return true if the 'do not translate' mark for the given locale is set
     */
    public boolean isMarkedNoTranslation(Locale locale) {

        if (m_noTranslation != null) {
            return CmsLocaleManager.getLocales(m_noTranslation).contains(locale);
        }
        return false;
    }

    /**
     * Sets the client entry.<p>
     *
     * @param entry the client entry
     */
    public void setClientEntry(CmsClientSitemapEntry entry) {

        m_entry = entry;
    }

    /**
     * Sets the 'has no children' flag.<p>
     *
     * @param b the new value
     */
    public void setHasNoChildren(boolean b) {

        m_hasNoChildren = b;

    }

}
