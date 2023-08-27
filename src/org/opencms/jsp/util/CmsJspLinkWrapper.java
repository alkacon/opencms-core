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

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;

/**
 * Wrapper for handling links in template/formatter JSP EL.
 */
public class CmsJspLinkWrapper extends AbstractCollection<String> {

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
     * Returns <code>true</code> if the wrapped link has been somehow initialized.<p>
     *
     * @return <code>true</code> if the wrapped link has been somehow initialized
     */
    public boolean getExists() {

        return m_link != null;
    }

    /**
     * Returns <code>true</code> in case the wrapped link is empty, that is either <code>null</code> or an empty String.<p>
     *
     * @return <code>true</code> in case the wrapped link is empty
     */
    public boolean getIsEmpty() {

        return CmsStringUtil.isEmpty(m_link);
    }

    /**
     * Returns <code>true</code> in case the wrapped link is empty or whitespace only,
     * that is either <code>null</code> or a String that contains only whitespace chars.<p>
     *
     * @return <code>true</code> in case the wrapped link is empty or whitespace only
     */
    public boolean getIsEmptyOrWhitespaceOnly() {

        return CmsStringUtil.isEmptyOrWhitespaceOnly(m_link);
    }

    /**
     * Returns <code>true</code> if the link is internal.
     *
     * @return <code>true</code> if the link is internal
     */
    public boolean getIsInternal() {

        if (m_internal == null) {
            String serverLink = getServerLink();
            m_internal = Boolean.valueOf(null != CmsXmlVarLinkValue.getInternalPathAndQuery(m_cms, serverLink));
        }
        return m_internal.booleanValue();
    }

    /**
     * Returns <code>true</code> in case the wrapped link exists and is not empty or whitespace only.<p>
     *
     * @return <code>true</code> in case the wrapped link exists and is not empty or whitespace only
     */
    public boolean getIsSet() {

        return !getIsEmptyOrWhitespaceOnly();
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
     * Returns the wrapped link as a String as in {@link #toString()}.<p>
     *
     * @return the wrapped link as a String
     */
    public String getToString() {

        return toString();
    }

    /**
     * @see org.opencms.jsp.util.A_CmsJspValueWrapper#hashCode()
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(m_link);
    }

    /**
     * Supports the use of the <code>empty</code> operator in the JSP EL by implementing the Collection interface.<p>
     *
     * @return the value from {@link #getIsEmptyOrWhitespaceOnly()} which is the inverse of {@link #getIsSet()}.<p>
     *
     * @see java.util.AbstractCollection#isEmpty()
     * @see #getIsEmptyOrWhitespaceOnly()
     * @see #getIsSet()
     */
    @Override
    public boolean isEmpty() {

        return getIsEmptyOrWhitespaceOnly();
    }

    /**
     * Supports the use of the <code>empty</code> operator in the JSP EL by implementing the Collection interface.<p>
     *
     * @return an empty Iterator in case {@link #isEmpty()} is <code>true</code>,
     * otherwise an Iterator that will return the String value of this wrapper exactly once.<p>
     *
     * @see java.util.AbstractCollection#size()
     */
    @Override
    public Iterator<String> iterator() {

        Iterator<String> it = new Iterator<String>() {

            private boolean isFirst = true;

            @Override
            public boolean hasNext() {

                return isFirst && !isEmpty();
            }

            @Override
            public String next() {

                isFirst = false;
                return toString();
            }

            @Override
            public void remove() {

                throw new UnsupportedOperationException();
            }
        };
        return it;
    }

    /**
     * Supports the use of the <code>empty</code> operator in the JSP EL by implementing the Collection interface.<p>
     *
     * @return returns 0 in case thiss link is empty, or 1 otherwise.<p>
     *
     * @see java.util.AbstractCollection#size()
     */
    @Override
    public int size() {

        return isEmpty() ? 0 : 1;
    }

    /**
     * Returns the wrapped link as a String as in {@link #getLink()}.<p>
     *
     * @return the wrapped link as a String
     */
    @Override
    public String toString() {

        return getLink();
    }

}
