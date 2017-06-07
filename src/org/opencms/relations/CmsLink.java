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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.relations;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.wrapper.CmsObjectWrapper;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsStaticResourceHandler;
import org.opencms.main.OpenCms;
import org.opencms.staticexport.CmsLinkProcessor;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.util.CmsUriSplitter;

import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import org.dom4j.Attribute;
import org.dom4j.Element;

/**
 * A single link entry in the link table.<p>
 *
 * @since 6.0.0
 */
public class CmsLink {

    /** Name of the internal attribute of the link node. */
    public static final String ATTRIBUTE_INTERNAL = "internal";

    /** Name of the name attribute of the elements node. */
    public static final String ATTRIBUTE_NAME = "name";

    /** Name of the type attribute of the elements node. */
    public static final String ATTRIBUTE_TYPE = "type";

    /** Default link name. */
    public static final String DEFAULT_NAME = "ref";

    /** Default link type. */
    public static final CmsRelationType DEFAULT_TYPE = CmsRelationType.XML_WEAK;

    /** A dummy uri. */
    public static final String DUMMY_URI = "@@@";

    /** Name of the anchor node. */
    public static final String NODE_ANCHOR = "anchor";

    /** Name of the query node. */
    public static final String NODE_QUERY = "query";

    /** Name of the target node. */
    public static final String NODE_TARGET = "target";

    /** Name of the UUID node. */
    public static final String NODE_UUID = "uuid";

    /** Constant for the NULL link. */
    public static final CmsLink NULL_LINK = new CmsLink();

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsLink.class);

    /** The anchor of the URI, if any. */
    private String m_anchor;

    /** The XML element reference. */
    private Element m_element;

    /** Indicates if the link is an internal link within the OpenCms VFS. */
    private boolean m_internal;

    /** The internal name of the link. */
    private String m_name;

    /** The parameters of the query, if any. */
    private Map<String, String[]> m_parameters;

    /** The query, if any. */
    private String m_query;

    /** The site root of the (internal) link. */
    private String m_siteRoot;

    /** The structure id of the linked resource. */
    private CmsUUID m_structureId;

    /** The link target (destination). */
    private String m_target;

    /** The type of the link. */
    private CmsRelationType m_type;

    /** The raw uri. */
    private String m_uri;

    /**
     * Reconstructs a link object from the given XML node.<p>
     *
     * @param element the XML node containing the link information
     */
    public CmsLink(Element element) {

        m_element = element;
        Attribute attrName = element.attribute(ATTRIBUTE_NAME);
        if (attrName != null) {
            m_name = attrName.getValue();
        } else {
            m_name = DEFAULT_NAME;
        }
        Attribute attrType = element.attribute(ATTRIBUTE_TYPE);
        if (attrType != null) {
            m_type = CmsRelationType.valueOfXml(attrType.getValue());
        } else {
            m_type = DEFAULT_TYPE;
        }
        Attribute attrInternal = element.attribute(ATTRIBUTE_INTERNAL);
        if (attrInternal != null) {
            m_internal = Boolean.valueOf(attrInternal.getValue()).booleanValue();
        } else {
            m_internal = true;
        }

        Element uuid = element.element(NODE_UUID);
        Element target = element.element(NODE_TARGET);
        Element anchor = element.element(NODE_ANCHOR);
        Element query = element.element(NODE_QUERY);

        m_structureId = (uuid != null) ? new CmsUUID(uuid.getText()) : null;
        m_target = (target != null) ? target.getText() : null;
        m_anchor = (anchor != null) ? anchor.getText() : null;
        setQuery((query != null) ? query.getText() : null);

        // update the uri from the components
        setUri();
    }

    /**
     * Creates a new link object without a reference to the xml page link element.<p>
     *
     * @param name the internal name of this link
     * @param type the type of this link
     * @param structureId the structure id of the link
     * @param uri the link uri
     * @param internal indicates if the link is internal within OpenCms
     */
    public CmsLink(String name, CmsRelationType type, CmsUUID structureId, String uri, boolean internal) {

        m_element = null;
        m_name = name;
        m_type = type;
        m_internal = internal;
        m_structureId = structureId;
        m_uri = uri;
        // update component members from the uri
        setComponents();
    }

    /**
     * Creates a new link object without a reference to the xml page link element.<p>
     *
     * @param name the internal name of this link
     * @param type the type of this link
     * @param uri the link uri
     * @param internal indicates if the link is internal within OpenCms
     */
    public CmsLink(String name, CmsRelationType type, String uri, boolean internal) {

        this(name, type, null, uri, internal);
    }

    /**
     *  Empty constructor for NULL constant.<p>
     */
    private CmsLink() {

        // empty constructor for NULL constant
    }

    /**
     * Checks and updates the structure id or the path of the target.<p>
     *
     * @param cms the cms context
     */
    public void checkConsistency(CmsObject cms) {

        if (!m_internal || (cms == null)) {
            return;
        }

        // in case of static resource links use the null UUID
        if (CmsStaticResourceHandler.isStaticResourceUri(m_target)) {
            m_structureId = CmsUUID.getNullUUID();
            return;
        }

        try {
            if (m_structureId == null) {
                // try by path
                throw new CmsException(Messages.get().container(Messages.LOG_BROKEN_LINK_NO_ID_0));
            }
            // first look for the resource with the given structure id
            String rootPath = null;
            CmsResource res;
            try {
                res = cms.readResource(m_structureId, CmsResourceFilter.ALL);
                rootPath = res.getRootPath();
                if (!res.getRootPath().equals(m_target)) {
                    // update path if needed
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(
                            Messages.get().getBundle().key(
                                Messages.LOG_BROKEN_LINK_UPDATED_BY_ID_3,
                                m_structureId,
                                m_target,
                                res.getRootPath()));
                    }

                }
            } catch (CmsException e) {
                // not found
                throw new CmsVfsResourceNotFoundException(
                    org.opencms.db.generic.Messages.get().container(
                        org.opencms.db.generic.Messages.ERR_READ_RESOURCE_1,
                        m_target),
                    e);
            }
            if ((rootPath != null) && !rootPath.equals(m_target)) {
                // set the new target
                m_target = res.getRootPath();
                setUri();
                // update xml node
                CmsLinkUpdateUtil.updateXml(this, m_element, true);
            }
        } catch (CmsException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_BROKEN_LINK_BY_ID_2, m_target, m_structureId), e);
            }
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_target)) {
                // no correction is possible
                return;
            }
            // go on with the resource with the given path
            String siteRoot = cms.getRequestContext().getSiteRoot();
            try {
                cms.getRequestContext().setSiteRoot("");
                // now look for the resource with the given path
                CmsResource res = cms.readResource(m_target, CmsResourceFilter.ALL);
                if (!res.getStructureId().equals(m_structureId)) {
                    // update structure id if needed
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(
                            Messages.get().getBundle().key(
                                Messages.LOG_BROKEN_LINK_UPDATED_BY_NAME_3,
                                m_target,
                                m_structureId,
                                res.getStructureId()));
                    }
                    m_target = res.getRootPath(); // could change by a translation rule
                    m_structureId = res.getStructureId();
                    CmsLinkUpdateUtil.updateXml(this, m_element, true);
                }
            } catch (CmsException e1) {
                // no correction was possible
                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().getBundle().key(Messages.LOG_BROKEN_LINK_BY_NAME_1, m_target), e1);
                }
                m_structureId = null;
            } finally {
                cms.getRequestContext().setSiteRoot(siteRoot);
            }
        }
    }

    /**
     * A link is considered equal if the link target and the link type is equal.<p>
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof CmsLink) {
            CmsLink other = (CmsLink)obj;
            return (m_type == other.m_type) && CmsStringUtil.isEqual(m_target, other.m_target);
        }
        return false;
    }

    /**
     * Returns the anchor of this link.<p>
     *
     * @return the anchor or null if undefined
     */
    public String getAnchor() {

        return m_anchor;
    }

    /**
     * Returns the xml node element representing this link object.<p>
     *
     * @return the xml node element representing this link object
     */
    public Element getElement() {

        return m_element;
    }

    /**
     * Returns the processed link.<p>
     *
     * @param cms the current OpenCms user context, can be <code>null</code>
     *
     * @return the processed link
     */
    public String getLink(CmsObject cms) {

        if (m_internal) {
            // if we have a local link, leave it unchanged
            // cms may be null for unit tests
            if ((cms == null) || (m_uri.length() == 0) || (m_uri.charAt(0) == '#')) {
                return m_uri;
            }

            checkConsistency(cms);
            //String target = replaceTargetWithDetailPageIfNecessary(cms, m_resource, m_target);
            String target = m_target;
            String uri = computeUri(target, m_query, m_anchor);

            CmsObjectWrapper wrapper = (CmsObjectWrapper)cms.getRequestContext().getAttribute(
                CmsObjectWrapper.ATTRIBUTE_NAME);
            if (wrapper != null) {
                // if an object wrapper is used, rewrite the URI
                m_uri = wrapper.rewriteLink(m_uri);
                uri = wrapper.rewriteLink(uri);
            }

            if ((cms.getRequestContext().getSiteRoot().length() == 0)
                && (cms.getRequestContext().getAttribute(CmsRequestContext.ATTRIBUTE_EDITOR) == null)) {
                // Explanation why this check is required:
                // If the site root name length is 0, this means that a user has switched
                // the site root to the root site "/" in the Workplace.
                // In this case the workplace site must also be the active site.
                // If the editor is opened in the root site, because of this code the links are
                // always generated _with_ server name / port so that the source code looks identical to code
                // that would normally be created when running in a regular site.
                // If normal link processing would be used, the site information in the link
                // would be lost.
                return OpenCms.getLinkManager().substituteLink(cms, uri);
            }

            // get the site root for this URI / link
            // if there is no site root, we either have a /system link, or the site was deleted,
            // return the full URI prefixed with the opencms context
            String siteRoot = getSiteRoot();
            if (siteRoot == null) {
                return OpenCms.getLinkManager().substituteLink(cms, uri);
            }

            if (cms.getRequestContext().getAttribute(CmsRequestContext.ATTRIBUTE_FULLLINKS) != null) {
                // full links should be generated even if we are in the same site
                return OpenCms.getLinkManager().getServerLink(cms, uri);
            }

            // return the link with the server prefix, if necessary
            return OpenCms.getLinkManager().substituteLink(cms, getSitePath(uri), siteRoot);
        } else {

            // don't touch external links
            return m_uri;
        }
    }

    /**
     * Returns the processed link.<p>
     *
     * @param cms the current OpenCms user context, can be <code>null</code>
     * @param processEditorLinks this parameter is not longer used
     *
     * @return the processed link
     *
     * @deprecated use {@link #getLink(CmsObject)} instead,
     *      the process editor option is set using the OpenCms request context attributes
     */
    @Deprecated
    public String getLink(CmsObject cms, boolean processEditorLinks) {

        return getLink(cms);
    }

    /**
     * Returns the macro name of this link.<p>
     *
     * @return the macro name name of this link
     */
    public String getName() {

        return m_name;
    }

    /**
     * Returns the first parameter value for the given parameter name.<p>
     *
     * @param name the name of the parameter
     * @return the first value for this name or <code>null</code>
     */
    public String getParameter(String name) {

        String[] p = getParameterMap().get(name);
        if (p != null) {
            return p[0];
        }

        return null;
    }

    /**
     * Returns the map of parameters of this link.<p>
     *
     * @return the map of parameters
     */
    public Map<String, String[]> getParameterMap() {

        if (m_parameters == null) {
            m_parameters = CmsRequestUtil.createParameterMap(m_query);
        }
        return m_parameters;
    }

    /**
     * Returns the set of available parameter names for this link.<p>
     *
     * @return the parameter names
     */
    public Set<String> getParameterNames() {

        return getParameterMap().keySet();
    }

    /**
     * Returns all parameter values for the given name.<p>
     *
     * @param name the name of the parameter
     *
     * @return all parameter values or <code>null</code>
     */
    public String[] getParameterValues(String name) {

        return getParameterMap().get(name);
    }

    /**
     * Returns the query of this link.<p>
     *
     * @return the query or null if undefined
     */
    public String getQuery() {

        return m_query;
    }

    /**
     * Returns the vfs link of the target if it is internal.<p>
     *
     * @return the full link destination or null if the link is not internal
     */
    public String getSitePath() {

        return getSitePath(m_uri);
    }

    /**
     * Return the site root if the target of this link is internal, or <code>null</code> otherwise.<p>
     *
     * @return the site root if the target of this link is internal, or <code>null</code> otherwise
     */
    public String getSiteRoot() {

        if (m_internal && (m_siteRoot == null)) {
            m_siteRoot = OpenCms.getSiteManager().getSiteRoot(m_target);
            if (m_siteRoot == null) {
                m_siteRoot = "";
            }
        }
        return m_siteRoot;
    }

    /**
     * The structure id of the linked resource.<p>
     *
     * @return structure id of the linked resource
     */
    public CmsUUID getStructureId() {

        return m_structureId;
    }

    /**
     * Returns the target (destination) of this link.<p>
     *
     * @return the target the target (destination) of this link
     */
    public String getTarget() {

        return m_target;
    }

    /**
     * Returns the type of this link.<p>
     *
     * @return the type of this link
     */
    public CmsRelationType getType() {

        return m_type;
    }

    /**
     * Returns the raw uri of this link.<p>
     *
     * @return the uri
     */
    public String getUri() {

        return m_uri;
    }

    /**
     * Returns the vfs link of the target if it is internal.<p>
     *
     * @return the full link destination or null if the link is not internal
     *
     * @deprecated Use {@link #getSitePath()} instead
     */
    @Deprecated
    public String getVfsUri() {

        return getSitePath();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        int result = m_type.hashCode();
        if (m_target != null) {
            result += m_target.hashCode();
        }
        return result;
    }

    /**
     * Returns if the link is internal.<p>
     *
     * @return true if the link is a local link
     */
    public boolean isInternal() {

        return m_internal;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return m_uri;
    }

    /**
     * Updates the uri of this link with a new value.<p>
     *
     * Also updates the structure of the underlying XML page document this link belongs to.<p>
     *
     * Note that you can <b>not</b> update the "internal" or "type" values of the link,
     * so the new link must be of same type (A, IMG) and also remain either an internal or external link.<p>
     *
     * @param uri the uri to update this link with <code>scheme://authority/path#anchor?query</code>
     */
    public void updateLink(String uri) {

        // set the uri
        m_uri = uri;

        // update the components
        setComponents();

        // update the xml
        CmsLinkUpdateUtil.updateXml(this, m_element, true);
    }

    /**
     * Updates the uri of this link with a new target, anchor and query.<p>
     *
     * If anchor and/or query are <code>null</code>, this features are not used.<p>
     *
     * Note that you can <b>not</b> update the "internal" or "type" values of the link,
     * so the new link must be of same type (A, IMG) and also remain either an internal or external link.<p>
     *
     * Also updates the structure of the underlying XML page document this link belongs to.<p>
     *
     * @param target the target (destination) of this link
     * @param anchor the anchor or null if undefined
     * @param query the query or null if undefined
     */
    public void updateLink(String target, String anchor, String query) {

        // set the components
        m_target = target;
        m_anchor = anchor;
        setQuery(query);

        // create the uri from the components
        setUri();

        // update the xml
        CmsLinkUpdateUtil.updateXml(this, m_element, true);
    }

    /**
     * Helper method for getting the site path for a uri.<p>
     *
     * @param uri a VFS uri
     * @return the site path
     */
    protected String getSitePath(String uri) {

        if (m_internal) {
            String siteRoot = getSiteRoot();
            if (siteRoot != null) {
                return uri.substring(siteRoot.length());
            } else {
                return uri;
            }
        }
        return null;
    }

    /**
     * Helper method for creating a uri from its components.<p>
     *
     * @param target the uri target
     * @param query the uri query component
     * @param anchor the uri anchor component
     *
     * @return the uri
     */
    private String computeUri(String target, String query, String anchor) {

        StringBuffer uri = new StringBuffer(64);
        uri.append(target);
        if (query != null) {
            uri.append('?');
            uri.append(query);
        }
        if (anchor != null) {
            uri.append('#');
            uri.append(anchor);
        }
        return uri.toString();

    }

    /**
     * Sets the component member variables (target, anchor, query)
     * by splitting the uri <code>scheme://authority/path#anchor?query</code>.<p>
     */
    private void setComponents() {

        CmsUriSplitter splitter = new CmsUriSplitter(m_uri, true);
        m_target = splitter.getPrefix();
        m_anchor = CmsLinkProcessor.unescapeLink(splitter.getAnchor());
        setQuery(splitter.getQuery());
    }

    /**
     * Sets the query of the link.<p>
     *
     * @param query the query to set.
     */
    private void setQuery(String query) {

        m_query = CmsLinkProcessor.unescapeLink(query);
        m_parameters = null;
    }

    /**
     * Joins the internal target, anchor and query components
     * to one uri string, setting the internal uri and parameters fields.<p>
     */
    private void setUri() {

        m_uri = computeUri(m_target, m_query, m_anchor);
    }
}