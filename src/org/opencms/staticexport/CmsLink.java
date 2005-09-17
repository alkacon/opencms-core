/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/staticexport/Attic/CmsLink.java,v $
 * Date   : $Date: 2005/09/17 16:38:43 $
 * Version: $Revision: 1.24.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.staticexport;

import org.opencms.site.CmsSiteManager;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsUriSplitter;
import org.opencms.xml.page.CmsXmlPage;

import java.util.Map;
import java.util.Set;

import org.dom4j.Element;

/**
 * A single link entry in the link table.<p>
 * 
 * @author Carsten Weinholz 
 * 
 * @version $Revision: 1.24.2.1 $ 
 * 
 * @since 6.0.0 
 */
public class CmsLink {

    /** The anchor of the uri, if any. */
    private String m_anchor;

    /** The xml element reference. */
    private Element m_element;

    /** Indicates if the link is an internal link within the OpenCms VFS. */
    private boolean m_internal;

    /** The internal name of the link. */
    private String m_name;

    /** The parameters of the query , if any. */
    private Map m_parameters;

    /** The query, if any. */
    private String m_query;

    /** The site root of the (internal) link. */
    private String m_siteRoot;

    /** The link target (destination). */
    private String m_target;

    /** The type of the link. */
    private String m_type;

    /** The raw uri. */
    private String m_uri;

    /**
     * Creates a new link object with a reference to the xml page link element.<p>
     * 
     * @param element the xml link element reference
     * @param name the internal name of this link
     * @param type the type of this link
     * @param uri the link uri
     * @param internal indicates if the link is internal within OpenCms 
     */
    public CmsLink(Element element, String name, String type, String uri, boolean internal) {

        m_element = element;
        m_name = name;
        m_type = type;
        m_internal = internal;

        m_uri = uri;

        // update component members from the uri
        setComponents(m_uri);
    }

    /**
     * Creates a new link object with a reference to the xml page link element.<p>
     * 
     * @param element the xml link element reference
     * @param name the internal name of this link
     * @param type the type of this link
     * @param target the link target (without anchor/query)
     * @param anchor the anchor or null 
     * @param query the query or null
     * @param internal indicates if the link is internal within OpenCms 
     */
    public CmsLink(
        Element element,
        String name,
        String type,
        String target,
        String anchor,
        String query,
        boolean internal) {

        m_element = element;
        m_name = name;
        m_type = type;
        m_internal = internal;

        m_target = target;
        m_anchor = anchor;
        setQuery(query);

        // update the uri from the components
        m_uri = setUri(m_target, m_anchor, m_query);
    }

    /**
     * Creates a new link object without a reference to the xml page link element.<p>
     * 
     * @param name the internal name of this link
     * @param type the type of this link
     * @param uri the link uri
     * @param internal indicates if the link is internal within OpenCms 
     */
    public CmsLink(String name, String type, String uri, boolean internal) {

        this(null, name, type, uri, internal);
    }

    /**
     * Creates a new link object without a reference to the xml page link element.<p>
     * 
     * @param name the internal name of this link
     * @param type the type of this link
     * @param target the link target (without anchor/query)
     * @param anchor the anchor or null 
     * @param query the query or null
     * @param internal indicates if the link is internal within OpenCms 
     */
    public CmsLink(String name, String type, String target, String anchor, String query, boolean internal) {

        this(null, name, type, target, anchor, query, internal);
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

        String[] p = (String[])m_parameters.get(name);
        if (p != null) {
            return p[0];
        }

        return null;
    }

    /**
     * Returns the map of parameters of this link.<p>
     * 
     * @return the map of parameters (<code>Map(String[])</code>)
     */
    public Map getParameterMap() {

        return m_parameters;
    }

    /**
     * Returns the set of available parameter names for this link.<p>
     * 
     * @return a <code>Set</code> of parameter names
     */
    public Set getParameterNames() {

        return m_parameters.keySet();
    }

    /**
     * Returns all parameter values for the given name.<p>
     * 
     * @param name the name of the parameter
     * @return a <code>String[]</code> of all parameter values or <code>null</code>
     */
    public String[] getParameterValues(String name) {

        return (String[])m_parameters.get(name);
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
     * Return the site root of the target if it is internal.<p>
     * 
     * @return the site root or null
     */
    public String getSiteRoot() {

        if (m_siteRoot != null) {
            return m_siteRoot;
        }
        if (m_internal) {
            m_siteRoot = CmsSiteManager.getSiteRoot(m_target);
            return m_siteRoot;
        }
        return null;
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
    public String getType() {

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
     * @return the full link destination or null if the link is not internal.
     */
    public String getVfsUri() {

        if (m_internal) {
            String siteRoot = getSiteRoot();
            return m_uri.substring(siteRoot.length());
        }

        return null;
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
        setComponents(m_uri);

        // update the xml
        updateXml();
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
        m_uri = setUri(m_target, m_anchor, m_query);

        // update the xml
        updateXml();
    }

    /**
     * Sets the component member variables (target, anchor, query) 
     * by splitting the uri <code>scheme://authority/path#anchor?query</code>.<p>
     * 
     * @param uri the uri to update the component members with
     */
    private void setComponents(String uri) {

        CmsUriSplitter splitter = new CmsUriSplitter(uri, true);
        m_target = splitter.getPrefix();
        m_anchor = splitter.getAnchor();
        setQuery(splitter.getQuery());

        // initialize the parameter map
        m_parameters = CmsRequestUtil.createParameterMap(m_query);
    }

    /**
     * Sets the query of the link.<p>
     * 
     * @param query the query to set.
     */
    private void setQuery(String query) {

        m_query = CmsLinkProcessor.unescapeLink(query);
    }

    /**
     * Joins the given components to one uri string.<p>
     * 
     * @param target the link target (without anchor/query)
     * @param anchor the anchor or null 
     * @param query the query or null
     * 
     * @return the composed uri
     */
    private String setUri(String target, String anchor, String query) {

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

        // initialize the parameter map
        m_parameters = CmsRequestUtil.createParameterMap(query);

        return uri.toString();
    }

    /**
     * Update the link node in the underlying XML page document.<p>
     */
    private void updateXml() {

        // check if this link node has a reference to the XML document
        if (m_element != null) {

            // handle <target> node in XML document
            Element targetElement = m_element.element(CmsXmlPage.NODE_TARGET);
            targetElement.clearContent();
            targetElement.addCDATA(m_target);

            // handle <anchor> node in XML document
            Element anchorElement = m_element.element(CmsXmlPage.NODE_ANCHOR);
            if (m_anchor != null) {
                if (anchorElement == null) {
                    // element wasn't there before, add element and set value
                    m_element.addElement(CmsXmlPage.NODE_ANCHOR).addCDATA(m_anchor);
                } else {
                    // element is there, update element value
                    anchorElement.clearContent();
                    anchorElement.addCDATA(m_anchor);
                }
            } else {
                // use remove method only when element exists
                if (anchorElement != null) {
                    // remove element
                    m_element.remove(anchorElement);
                }
            }

            // handle <query> node in XML document
            Element queryElement = m_element.element(CmsXmlPage.NODE_QUERY);
            if (m_query != null) {
                if (queryElement == null) {
                    // element wasn't there before, add element and set value 
                    m_element.addElement(CmsXmlPage.NODE_QUERY).addCDATA(m_query);
                } else {
                    // element is there, update element value
                    queryElement.clearContent();
                    queryElement.addCDATA(m_query);
                }
            } else {
                // use remove method only when element exists
                if (queryElement != null) {
                    // remove element
                    m_element.remove(queryElement);
                }
            }
        }
    }
}