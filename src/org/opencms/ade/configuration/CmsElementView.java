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

package org.opencms.ade.configuration;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.galleries.CmsGallerySearch;
import org.opencms.search.galleries.CmsGallerySearchResult;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.CmsRole;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.Locale;

import org.apache.commons.logging.Log;

/**
 * Represents a element view for the container page editor.<p>
 */
public class CmsElementView {

    /** The default element view. */
    public static final CmsElementView DEFAULT_ELEMENT_VIEW = new CmsElementView(null);

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsElementView.class);

    /** The group resource. */
    private CmsResource m_resource;

    /**
     * Constructor.<p>
     * 
     * @param resource the group resource
     */
    public CmsElementView(CmsResource resource) {

        m_resource = resource;
    }

    /**
     * Returns the element view description.<p>
     * 
     * @param cms the cms context
     * @param locale the locale
     * 
     * @return the description
     */
    public String getDescription(CmsObject cms, Locale locale) {

        String result = "";
        if (m_resource != null) {
            try {
                CmsGallerySearchResult search = CmsGallerySearch.searchById(cms, m_resource.getStructureId(), locale);
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(search.getTitle())) {
                    result = search.getDescription();
                } else {
                    CmsProperty prop = cms.readPropertyObject(
                        m_resource,
                        CmsPropertyDefinition.PROPERTY_DESCRIPTION,
                        false);
                    result = prop.getValue();
                }
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        } else {
            result = Messages.get().getBundle(locale).key(Messages.GUI_ELEMENT_VIEW_DEFAULT_DESCRIPTION_0);
        }
        return result;
    }

    /**
     * Returns the element view id.<p>
     * 
     * @return the group id
     */
    public CmsUUID getId() {

        if (m_resource != null) {
            return m_resource.getStructureId();
        } else {
            // only in case of the default element view
            return CmsUUID.getNullUUID();
        }
    }

    /**
     * Returns the element view resource.<p>
     * 
     * @return the element view resource
     */
    public CmsResource getResource() {

        return m_resource;
    }

    /**
     * Returns the element view title.<p>
     * 
     * @param cms the cms context
     * @param locale the locale
     * 
     * @return the title
     */
    public String getTitle(CmsObject cms, Locale locale) {

        String result = "";
        if (m_resource != null) {
            try {
                CmsGallerySearchResult search = CmsGallerySearch.searchById(cms, m_resource.getStructureId(), locale);
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(search.getTitle())) {
                    result = search.getTitle();
                } else {
                    CmsProperty prop = cms.readPropertyObject(m_resource, CmsPropertyDefinition.PROPERTY_TITLE, false);
                    result = prop.getValue();
                }
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        } else {
            result = Messages.get().getBundle(locale).key(Messages.GUI_ELEMENT_VIEW_DEFAULT_TITLE_0);
        }
        return result;
    }

    /**
     * Checks whether the current user has permissions to use the element view.<p>
     * 
     * @param cms the cms context
     * 
     * @return <code>true</code> if the current user has permissions to use the element view
     **/
    public boolean hasPermission(CmsObject cms) {

        try {
            if (m_resource != null) {
                return cms.hasPermissions(m_resource, CmsPermissionSet.ACCESS_READ);
            } else {
                return OpenCms.getRoleManager().hasRole(cms, CmsRole.ELEMENT_AUTHOR);
            }
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        return false;
    }
}
