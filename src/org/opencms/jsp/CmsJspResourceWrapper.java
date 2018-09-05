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

import org.opencms.file.CmsFile;
import org.opencms.file.CmsFolder;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsLocaleGroup;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;

import java.util.HashMap;
import java.util.List;
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

    /** The file object for this resource. */
    private CmsFile m_file;

    /** The resource / file content as a String. */
    private String m_content;

    /** The calculated site path of the resource. */
    private String m_sitePath;

    /** Properties of this resource. */
    private Map<String, String> m_properties;

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
        m_file = null;
        m_content = "";
    }

    /**
     * Two resources are considered equal in case their structure id is equal.<p>
     *
     * @see CmsResource#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }
        if (obj instanceof CmsResource) {
            return ((CmsResource)obj).getStructureId().equals(getStructureId());
        }
        return false;
    }

    /**
     * Returns the content of the file as a String.<p>
     *
     * @return the content of the file as a String
     */
    public String getContent() {

        if ((m_content.length() == 0) && (getFile() != null)) {
            m_content = new String(getFile().getContents());
        }
        return m_content;
    }

    /**
     * Returns this resources name extension (if present).<p>
     *
     * The extension will always be lower case.<p>
     *
     * @return the extension or <code>null</code> if not available
     *
     * @see CmsResource#getExtension(String)
     * @see org.opencms.jsp.util.CmsJspVfsAccessBean#getResourceExtension(Object)
     */
    public String getExtension() {

        return getExtension(getRootPath());
    }

    /**
     * Returns the full file object for this resource.<p>
     *
     * @return the full file object for this resource
     */
    public CmsFile getFile() {

        if ((m_file == null) && !isFolder()) {
            try {
                m_file = m_cms.readFile(this);
            } catch (CmsException e) {
                // this should not happen since we are updating from a resource object
            }
        }
        return m_file;
    }

    /**
     * Returns the folder of this resource.<p>
     *
     * In case this resource already is a {@link CmsFolder}, it is returned without modification.
     * In case it is a {@link CmsFile}, the parent folder of the file is returned.<p>
     *
     * @return the folder of this resource
     *
     * @see #getSitePathFolder()
     */
    public CmsJspResourceWrapper getFolder() {

        CmsJspResourceWrapper result;
        if (isFolder()) {
            result = this;
        } else {
            result = readResource(getSitePathFolder());
        }
        return result;
    }

    /**
     * Returns a substituted link to this resource.<p>
     *
     * @return the link
     */
    public String getLink() {

        return OpenCms.getLinkManager().substituteLinkForUnknownTarget(
            m_cms,
            m_cms.getRequestContext().getSitePath(this));
    }

    /**
     * Returns a map of the locale group for the current resource, with locale strings as keys.<p>
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
     * Returns the main locale for this resource.<p>
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

    /**
     * Returns the navigation info element for this resource.
     *
     * @return the navigation info element for this resource
     */
    public CmsJspNavElement getNavInfo() {

        return new CmsJspNavElement(getSitePath(), this, getProperties(), getSitePathLevel() - 1);
    }

    /**
     * Returns the parent folder of this resource.<p>
     *
     * @return the parent folder of this resource
     *
     * @see #getSitePathParentFolder()
     * @see CmsResource#getParentFolder(String)
     * @see org.opencms.jsp.util.CmsJspVfsAccessBean#getParentFolder(Object)
     */
    public CmsJspResourceWrapper getParentFolder() {

        return readResource(getSitePathParentFolder());
    }

    /**
     * Returns the properties of this resource in a map.<p>
     *
     * @return the properties of this resource in a map
     */
    public Map<String, String> getProperties() {

        if (m_properties == null) {
            try {
                List<CmsProperty> properties = m_cms.readPropertyObjects(this, false);
                m_properties = CmsProperty.toMap(properties);
            } catch (CmsException e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(e.getMessage(), e);
                }
            }
        }
        return m_properties;
    }

    /**
     * Returns this resources name extension (if present).<p>
     *
     * The extension will always be lower case.<p>
     *
     * @return the extension or <code>null</code> if not available
     *
     * @see CmsResource#getExtension(String)
     * @see org.opencms.jsp.util.CmsJspVfsAccessBean#getResourceExtension(Object)
     */
    public String getResourceExtension() {

        return getExtension();
    }

    /**
     * Returns the name of this resource without the path information.<p>
     *
     * The resource name of a file is the name of the file.
     * The resource name of a folder is the folder name with trailing "/".
     * The resource name of the root folder is <code>/</code>.<p>
     *
     * @return the name of this resource without the path information
     *
     * @see CmsResource#getName()
     * @see org.opencms.jsp.util.CmsJspVfsAccessBean#getResourceName(Object)
     */
    public String getResourceName() {

        return getName();
    }

    /**
     * Returns the folder name of this resource from the root site.<p>
     *
     * In case this resource already is a {@link CmsFolder}, the folder path is returned without modification.
     * In case it is a {@link CmsFile}, the parent folder name of the file is returned.<p>
     *
     * @return  the folder name of this resource from the root site
     */
    public String getRootPathFolder() {

        String result;
        if (isFile()) {
            result = getRootPathParentFolder();
        } else {
            result = getRootPath();
        }
        return result;
    }

    /**
     * Returns the directory level of a resource from the root site.<p>
     *
     * The root folder "/" has level 0,
     * a folder "/foo/" would have level 1,
     * a folder "/foo/bar/" level 2 etc.<p>
     *
     * @return the directory level of a resource from the root site
     *
     * @see CmsResource#getPathLevel(String)
     */
    public int getRootPathLevel() {

        return getPathLevel(getRootPath());
    }

    /**
     * Returns the parent folder of this resource from the root site.<p>
     *
     * @return the parent folder of this resource from the root site
     *
     * @see CmsResource#getParentFolder(String)
     */
    public String getRootPathParentFolder() {

        return getParentFolder(getRootPath());
    }

    /**
     * Returns the current site path to this resource.<p>
     *
     * @return the current site path to this resource
     *
     * @see org.opencms.file.CmsRequestContext#getSitePath(CmsResource)
     */
    public String getSitePath() {

        if (m_sitePath == null) {
            m_sitePath = m_cms.getRequestContext().getSitePath(this);
        }

        return m_sitePath;
    }

    /**
     * Returns the folder name of this resource in the current site.<p>
     *
     * In case this resource already is a {@link CmsFolder}, the folder path is returned without modification.
     * In case it is a {@link CmsFile}, the parent folder name of the file is returned.<p>
     *
     * @return  the folder name of this resource in the current site
     */
    public String getSitePathFolder() {

        String result;
        if (isFile()) {
            result = getSitePathParentFolder();
        } else {
            result = getSitePath();
        }
        return result;
    }

    /**
     * Returns the directory level of a resource in the current site.<p>
     *
     * The root folder "/" has level 0,
     * a folder "/foo/" would have level 1,
     * a folder "/foo/bar/" level 2 etc.<p>
     *
     * @return the directory level of a resource in the current site
     *
     * @see CmsResource#getPathLevel(String)
     * @see org.opencms.jsp.util.CmsJspVfsAccessBean#getPathLevel(Object)
     */
    public int getSitePathLevel() {

        return getPathLevel(getSitePath());
    }

    /**
     * Returns the parent folder of this resource in the current site.<p>
     *
     * @return the parent folder of this resource in the current site
     *
     * @see CmsResource#getParentFolder(String)
     * @see org.opencms.jsp.util.CmsJspVfsAccessBean#getParentFolder(Object)
     */
    public String getSitePathParentFolder() {

        return getParentFolder(getSitePath());
    }

    /**
     * @see CmsResource#hashCode()
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        if (getStructureId() != null) {
            return getStructureId().hashCode();
        }

        return CmsUUID.getNullUUID().hashCode();
    }

    /**
     * Reads a resource, suppressing possible exceptions.<p>
     *
     * @param sitePath the site path of the resource to read.
     *
     * @return the resource of <code>null</code> on case an exception occurred while reading
     */
    private CmsJspResourceWrapper readResource(String sitePath) {

        CmsJspResourceWrapper result = null;
        try {
            result = new CmsJspResourceWrapper(m_cms, m_cms.readResource(sitePath));
        } catch (CmsException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(e.getMessage(), e);
            }
        }
        return result;
    }
}
