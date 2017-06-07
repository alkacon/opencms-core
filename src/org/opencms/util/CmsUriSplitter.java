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

package org.opencms.util;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Splits an URI String into separate components.<p>
 *
 * An URI is splitted into a <code>prefix</code>, a <code>anchor</code> and a <code>query</code> part.
 */
public class CmsUriSplitter {

    /** Empty (non null) StringBuffer constant. */
    private static final StringBuffer EMPTY_BUFFER = new StringBuffer(0);

    /** The anchor part of the URI, for example <code>someanchor</code>. */
    private String m_anchor;

    /** Indicates if 'strict' URI parsing did produce an error. */
    private boolean m_errorFree;

    /** Indicates if 'strict' URI parsing was used. */
    private boolean m_isStrict;

    /** The URI protocol, for example <code>http</code> or <code>https</code>. */
    private String m_protocol;

    /** The prefix part of the URI, for example <code>http://www.opencms.org/some/path/</code>. */
    private String m_prefix;

    /** The query part of the URI, for example <code>a=b&c=d</code>. */
    private String m_query;

    /** The suffix part of the uri. */
    private String m_suffix;

    /** The original URI String that was split. */
    private String m_uri;

    /**
     * Creates a splitted URI using the default (not strict) parsing mode.<p>
     *
     * @param uri the URI to split
     */
    public CmsUriSplitter(String uri) {

        this(uri, false);
    }

    /**
     * Creates a splitted URI using the given parsing mode.<p>
     *
     * Using 'strict' parsing mode, all requirements for an URI are checked.
     * If 'strict' is set to <code>false</code>, then only some simple parsing rules are applied,
     * in which case the result may not be 100% valid (but still usable).
     * If 'strict' parsing generates an error, then simple parsing is used as a fallback.<p>
     *
     * @param uri the URI to split
     * @param strict if <code>true</code>, then 'strict' parsing mode is used, otherwise a relaxed URI parsing is done
     */
    public CmsUriSplitter(String uri, boolean strict) {

        m_uri = uri;
        m_errorFree = true;
        m_isStrict = strict;

        if (strict) {

            // use strict parsing
            try {
                URI u = new URI(uri);
                m_protocol = u.getScheme();
                m_prefix = ((m_protocol != null) ? m_protocol + ":" : "") + u.getRawSchemeSpecificPart();
                m_anchor = u.getRawFragment();
                m_query = u.getRawQuery();
                if (m_prefix != null) {
                    int i = m_prefix.indexOf('?');
                    if (i != -1) {
                        m_query = m_prefix.substring(i + 1);
                        m_prefix = m_prefix.substring(0, i);
                    }
                }
                if (m_anchor != null) {
                    int i = m_anchor.indexOf('?');
                    if (i != -1) {
                        m_query = m_anchor.substring(i + 1);
                        m_anchor = m_anchor.substring(0, i);
                    }
                }
            } catch (Exception exc) {
                // may be thrown by URI constructor if URI is invalid
                strict = false;
                m_errorFree = false;
            }
        }

        if ((!strict) && (uri != null)) {

            // use simple parsing
            StringBuffer prefix = new StringBuffer(uri.length());
            StringBuffer anchor = EMPTY_BUFFER;
            StringBuffer query = EMPTY_BUFFER;

            int len = uri.length();
            int cur = 0;

            for (int i = 0; i < len; i++) {
                char c = uri.charAt(i);
                if ((cur == 0) && (c == ':')) {
                    m_protocol = prefix.toString();
                }
                if (c == '#') {
                    // start of anchor
                    cur = 1;
                    anchor = new StringBuffer(uri.length());
                    continue;
                }
                if (c == '?') {
                    // start of query
                    cur = 2;
                    // ensure a duplicate query part is 'flushed' (same behavior as strict parser)
                    query = new StringBuffer(uri.length());
                    continue;
                }
                switch (cur) {
                    case 1:
                        // append to anchor
                        anchor.append(c);
                        break;
                    case 2:
                        // append to query
                        query.append(c);
                        break;
                    default:
                        // append to prefix
                        prefix.append(c);
                        break;
                }
            }

            if (prefix.length() > 0) {
                m_prefix = prefix.toString();
            }
            if (anchor.length() > 0) {
                m_anchor = anchor.toString();
            }
            if (query.length() > 0) {
                m_query = query.toString();
            }
        }
    }

    /**
     * Checks if the given URI is well formed.<p>
     *
     * @param uri the URI to check
     *
     * @return <code>true</code> if the given URI is well formed
     */
    @SuppressWarnings("unused")
    public static boolean isValidUri(String uri) {

        boolean result = false;
        try {
            new URI(uri);
            result = true;
        } catch (Exception e) {
            // nothing to do
        }
        return result;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof CmsUriSplitter) {
            CmsUriSplitter other = (CmsUriSplitter)obj;
            if (!((m_protocol == other.m_protocol) || ((m_protocol != null) && m_protocol.equals(other.m_protocol)))) {
                return false;
            }
            if (!((m_prefix == other.m_prefix) || ((m_prefix != null) && m_prefix.equals(other.m_prefix)))) {
                return false;
            }
            if (!((m_anchor == other.m_anchor) || ((m_anchor != null) && m_anchor.equals(other.m_anchor)))) {
                return false;
            }
            if (!((m_query == other.m_query) || ((m_query != null) && m_query.equals(other.m_query)))) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Returns the anchor part of the uri, for example <code>someanchor</code>,
     * or <code>null</code> if no anchor is available.<p>
     *
     * @return the anchor part of the uri
     */
    public String getAnchor() {

        return m_anchor;
    }

    /**
     * Returns the prefix part of the uri, for example <code>http://www.opencms.org/some/path/</code>,
     * or <code>null</code> if no prefix is available.<p>
     *
     * @return the prefix part of the uri
     */
    public String getPrefix() {

        return m_prefix;
    }

    /**
     * Returns the URI protocol, for example <code>http</code> or <code>https</code>.<p>
     *
     * @return the URI protocol
     */
    public String getProtocol() {

        return m_protocol;
    }

    /**
     * Returns the query part of the uri, for example <code>a=b&c=d</code>,
     * or <code>null</code> if no query is available.<p>
     *
     * @return the query part of the uri
     */
    public String getQuery() {

        return m_query;
    }

    /**
     * Returns the suffix part of the uri, a combination of query and anchor,
     * for example <code>?a=b&c=d#someanchor</code>,
     * or the empty String if no suffix is available.<p>
     *
     * @return the suffix part of the uri
     */
    public String getSuffix() {

        if (m_suffix == null) {
            StringBuffer result = new StringBuffer();
            if (m_query != null) {
                result.append('?');
                result.append(m_query);
            }
            if (m_anchor != null) {
                result.append('#');
                result.append(m_anchor);
            }
            m_suffix = result.toString();
        }
        return m_suffix;
    }

    /**
     * Returns the URI String passed to this URI splitter.<p>
     *
     * @return the URI String passed to this URI splitter
     */
    public String getUri() {

        return m_uri;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        int hashCode = 0;
        if (m_prefix != null) {
            hashCode += m_prefix.hashCode();
        }
        if (m_anchor != null) {
            hashCode += m_anchor.hashCode();
        }
        if (m_query != null) {
            hashCode += m_query.hashCode();
        }
        return hashCode;
    }

    /**
     * Returns <code>true</code> if the URI was parsed error free in 'strict' mode,
     * or if the simple mode was used.<p>
     *
     * @return <code>true</code> if the URI was parsed error free in 'strict' mode,
     *      or if the simple mode was used
     */
    public boolean isErrorFree() {

        return m_errorFree;
    }

    /**
     * Returns an URI object created from the original input String.<p>
     *
     * This method will do a "best effort" to convert the original input String to a legal URI.
     * Most notably, it will be able to handle original input Strings that contain a space " "
     * and other usually illegal characters.<p>
     *
     * @return an URI object created from the original input String
     *
     * @throws URISyntaxException in case no URI object can be created from the original input String
     */
    public URI toURI() throws URISyntaxException {

        if (m_isStrict && m_errorFree) {
            // we have already verified that the URI contains no errors
            return new URI(m_uri);
        }
        // create a new URI from the components
        // using this constructor the input will be escaped if required
        return new URI(null, m_prefix + (m_query != null ? "?" + m_query : ""), m_anchor);
    }
}