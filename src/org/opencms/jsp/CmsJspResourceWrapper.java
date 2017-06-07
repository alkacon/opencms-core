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

package org.opencms.jsp;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsLocaleGroup;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.google.common.collect.Maps;

/**
 * Wrapper subclass of CmsResource with some convenience methods.<p>
 */
public class CmsJspResourceWrapper extends CmsResource {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** Logger instance for this class. */
    @SuppressWarnings("unused")
    private static final Log LOG = CmsLog.getLog(CmsJspResourceWrapper.class);

    /** The CMS context. */
    private CmsObject m_cms;

    /** The set of locale variants. */
    private Map<String, CmsJspResourceWrapper> m_localeResources;

    /** The main locale. */
    private Locale m_mainLocale;

    /**
     * Creates a new instance.<p>
     *
     * @param cms the current CMS context
     * @param res the resource to wrap
     */
    public CmsJspResourceWrapper(CmsObject cms, CmsResource res) {
        super(
            res.getStructureId(),
            res.getResourceId(),
            res.getRootPath(),
            res.getTypeId(),
            res.isFolder(),
            res.getFlags(),
            res.getProjectLastModified(),
            res.getState(),
            res.getDateCreated(),
            res.getUserCreated(),
            res.getDateLastModified(),
            res.getUserLastModified(),
            res.getDateReleased(),
            res.getDateExpired(),
            res.getSiblingCount(),
            res.getLength(),
            res.getDateContent(),
            res.getVersion());
        m_cms = cms;
    }

    /**
     * Gets a link to the resource.<p>
     *
     * @return the link
     */
    public String getLink() {

        return OpenCms.getLinkManager().substituteLinkForUnknownTarget(
            m_cms,
            m_cms.getRequestContext().getSitePath(this));
    }

    /**
     * Gets a map of the locale group for the current resource, with locale strings as keys.<p>
     *
     * @return a map with locale strings as keys and resource wrappers for the corresponding locale variants
     */
    public Map<String, CmsJspResourceWrapper> getLocaleResource() {

        if (m_localeResources != null) {
            return m_localeResources;
        }
        try {
            CmsLocaleGroup localeGroup = m_cms.getLocaleGroupService().readLocaleGroup(this);
            Map<Locale, CmsResource> resourcesByLocale = localeGroup.getResourcesByLocale();
            Map<String, CmsJspResourceWrapper> result = Maps.newHashMap();
            for (Map.Entry<Locale, CmsResource> entry : resourcesByLocale.entrySet()) {
                result.put(entry.getKey().toString(), new CmsJspResourceWrapper(m_cms, entry.getValue()));
            }
            m_localeResources = result;
            return result;
        } catch (CmsException e) {
            return new HashMap<String, CmsJspResourceWrapper>();
        }
    }

    /**
     * Gets the main locale for this resource.<p>
     *
     * @return the main locale for this resource
     */
    public Locale getMainLocale() {

        if (m_mainLocale != null) {
            return m_mainLocale;
        }
        try {
            CmsLocaleGroup localeGroup = m_cms.getLocaleGroupService().readLocaleGroup(this);
            m_mainLocale = localeGroup.getMainLocale();
            return m_mainLocale;
        } catch (CmsException e) {
            return null;
        }
    }
}
