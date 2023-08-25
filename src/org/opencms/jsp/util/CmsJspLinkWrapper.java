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

package org.opencms.jsp.util;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.jsp.CmsJspResourceWrapper;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsLink;
import org.opencms.relations.CmsRelationType;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.types.CmsXmlVarLinkValue;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;

/**
 * Wrapper for handling links in template/formatter JSP EL.
 */
public class CmsJspLinkWrapper {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJspLinkWrapper.class);

    /** Stored CMS context. */
    protected CmsObject m_cms;

    /** The link literal from which this wrapper was created. */
    protected String m_link;

    /** Cached internal/external state. */
    protected Boolean m_internal;

    /** Cached link target resource. */
    protected Optional<CmsResource> m_resource;

    /** Cached links (online, perma, server). */
    protected Map<String, String> m_stringCache = new ConcurrentHashMap<>();

    /**
     * Creates a new link wrapper.
     *
     * <p>The link parameter should be in the same format that you enter in an XML content of field of type OpenCmsVarLink, i.e.
     * either a full external URL or a site path with a query string attached.
     *
     * @param cms the CMS context
     * @param link the link to wrap
     */
    public CmsJspLinkWrapper(CmsObject cms, String link) {

        m_cms = cms;
        m_link = link;
    }

    /**
     * Checks whether the link is not null.
     *
     * @return true if the link is not null
     */
    public boolean getExists() {

        return m_link != null;
    }

    /**
     * Checks whether the link is empty.
     *
     * @return true if the link is empty
     */
    public boolean getIsEmpty() {

        return CmsStringUtil.isEmpty(m_link);
    }

    /**
     * Checks if the link is empty or consists only of whitespace.
     *
     * @return true if the link is empty or consists of whitespace
     */
    public boolean getIsEmptyOrWhitespaceOnly() {

        return CmsStringUtil.isEmptyOrWhitespaceOnly(m_link);
    }

    /**
     * Returns true if the link is internal.
     *
     * @return true if the link is internal
     */
    public boolean getIsInternal() {

        if (m_internal == null) {
            String serverLink = getServerLink();
            m_internal = Boolean.valueOf(null != CmsXmlVarLinkValue.getInternalPathAndQuery(m_cms, serverLink));
        }
        return m_internal.booleanValue();
    }

    /**
     * Performs normal link substitution.
     *
     * @return the substituted link
     */
    public String getLink() {

        return m_stringCache.computeIfAbsent("link", k -> A_CmsJspValueWrapper.substituteLink(m_cms, m_link));
    }

    /**
     * Gets the literal from which this wrapper was constructed.
     *
     * @return the original link literal
     */
    public String getLiteral() {

        return m_link;
    }

    /**
     * Performs online link substitution.
     *
     * @return the online link
     */
    public String getOnlineLink() {

        return m_stringCache.computeIfAbsent("online", k -> OpenCms.getLinkManager().getOnlineLink(m_cms, m_link));
    }

    /**
     * Performs permalink substitution.
     *
     * @return the permalink
     */
    public String getPermaLink() {

        return m_stringCache.computeIfAbsent("perma", k -> OpenCms.getLinkManager().getPermalink(m_cms, m_link));
    }

    /**
     * Gets the resource wrapper for the link target.
     *
     * @return the resource wrapper for the target
     */
    public CmsJspResourceWrapper getResource() {

        if (m_resource == null) {
            try {
                String link = CmsXmlVarLinkValue.getInternalPathAndQuery(m_cms, getServerLink());
                if (link == null) {
                    m_resource = Optional.empty();
                } else {
                    CmsLink linkObj = new CmsLink(/*name=*/null, CmsRelationType.HYPERLINK, link, true);
                    linkObj.checkConsistency(m_cms);
                    m_resource = Optional.ofNullable(linkObj.getResource());
                }
            } catch (Exception e) {
                LOG.warn(e.getLocalizedMessage(), e);
                m_resource = Optional.empty();
            }
        }
        if (m_resource.isPresent()) {
            return CmsJspResourceWrapper.wrap(m_cms, m_resource.get());
        } else {
            return null;
        }

    }

    /**
     * Performs server link substitution.
     *
     * @return the server link
     */
    public String getServerLink() {

        return m_stringCache.computeIfAbsent("server", k -> OpenCms.getLinkManager().getServerLink(m_cms, m_link));
    }

    /**
     * @see org.opencms.jsp.util.A_CmsJspValueWrapper#hashCode()
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(m_link);
    }

    /**
     * @see java.util.AbstractCollection#toString()
     */
    @Override
    public String toString() {

        return getLink();
    }

}
