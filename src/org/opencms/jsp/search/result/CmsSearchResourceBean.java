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

package org.opencms.jsp.search.result;

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.jsp.util.CmsJspContentAccessBean;
import org.opencms.search.CmsSearchResource;
import org.opencms.util.CmsCollectionsGenericWrapper;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.Transformer;

/** JSP EL friendly wrapper class for a single search result (document). */
public class CmsSearchResourceBean implements I_CmsSearchResourceBean {

    /** The result as returned normally. */
    final CmsSearchResource m_searchResource;
    /** Lazy map to access the String fields of the document. */
    private Map<String, String> m_stringfields;
    /** Lazy map to access the Date fields of the document. */
    private Map<String, Date> m_datefields;
    /** Lazy map to access the multi-valued String fields of the document. */
    private Map<String, List<String>> m_multivaluedfields;
    /** Lazy map to access content in different locales. */
    private Map<String, CmsJspContentAccessBean> m_localizedContent;
    /** Cms object. */
    final CmsObject m_cmsObject;

    /** Constructor taking the search resource to wrap.
     * @param searchResource The search resource to wrap.
     * @param cms The Cms object, used to read resources.
     */
    public CmsSearchResourceBean(final CmsSearchResource searchResource, final CmsObject cms) {

        m_searchResource = searchResource;
        m_cmsObject = cms;
    }

    /**
     * @see org.opencms.jsp.search.result.I_CmsSearchResourceBean#getDateFields()
     */
    @Override
    public Map<String, Date> getDateFields() {

        if (m_datefields == null) {
            m_datefields = CmsCollectionsGenericWrapper.createLazyMap(new Transformer() {

                @Override
                public Object transform(final Object fieldName) {

                    return getSearchResource().getDateField(fieldName.toString());
                }
            });
        }
        return m_datefields;
    }

    /**
     * @see org.opencms.jsp.search.result.I_CmsSearchResourceBean#getFields()
     */
    @Override
    public Map<String, String> getFields() {

        if (m_stringfields == null) {
            m_stringfields = CmsCollectionsGenericWrapper.createLazyMap(new Transformer() {

                @Override
                public Object transform(final Object fieldName) {

                    return getSearchResource().getField(fieldName.toString());
                }
            });
        }
        return m_stringfields;
    }

    /**
     * @see org.opencms.jsp.search.result.I_CmsSearchResourceBean#getMultiValuedFields()
     */
    @Override
    public Map<String, List<String>> getMultiValuedFields() {

        if (m_multivaluedfields == null) {
            m_multivaluedfields = CmsCollectionsGenericWrapper.createLazyMap(new Transformer() {

                @Override
                public Object transform(final Object fieldName) {

                    return getSearchResource().getMultivaluedField(fieldName.toString());
                }
            });

        }
        return m_multivaluedfields;
    }

    /**
     * @see org.opencms.jsp.search.result.I_CmsSearchResourceBean#getSearchResource()
     */
    @Override
    public CmsSearchResource getSearchResource() {

        return m_searchResource;
    }

    /**
     * @see org.opencms.jsp.search.result.I_CmsSearchResourceBean#getXmlContent()
     */
    @Override
    public CmsJspContentAccessBean getXmlContent() {

        CmsJspContentAccessBean accessBean = null;
        try {
            accessBean = new CmsJspContentAccessBean(m_cmsObject, m_searchResource);
        } catch (@SuppressWarnings("unused") Exception e) {
            // do nothing - simply could not read content;
        }
        return accessBean;
    }

    /**
     * @see org.opencms.jsp.search.result.I_CmsSearchResourceBean#getXmlContentInLocale()
     */
    public Map<String, CmsJspContentAccessBean> getXmlContentInLocale() {

        if (m_localizedContent == null) {
            m_localizedContent = CmsCollectionsGenericWrapper.createLazyMap(new Transformer() {

                @Override
                public Object transform(final Object locale) {

                    CmsJspContentAccessBean accessBean = null;
                    try {
                        accessBean = new CmsJspContentAccessBean(
                            m_cmsObject,
                            CmsLocaleManager.getLocale((String)locale),
                            m_searchResource);
                    } catch (@SuppressWarnings("unused") Exception e) {
                        // simply return null
                    }
                    return accessBean;
                }
            });

        }
        return m_localizedContent;
    }
}
