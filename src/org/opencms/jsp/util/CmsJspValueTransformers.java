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
import org.opencms.main.CmsException;
import org.opencms.util.CmsCollectionsGenericWrapper;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.collections.Transformer;
import org.apache.commons.lang3.LocaleUtils;

/** Utility class for providing {@link Transformer} implementations for various purposes. */
public final class CmsJspValueTransformers {

    /** Transformer that yields locale specific properties for a resource. */
    public static final class CmsLocalePropertyLoaderTransformer implements Transformer {

        /** The {@link CmsObject} used for reading properties. */
        private CmsObject m_cms;
        /** The resource where the properties are read from. */
        private CmsResource m_res;

        /** The map from locale to properties. */
        private Map<Locale, Map<String, String>> m_localeProperties;

        /** Search for the property or not. */
        private boolean m_search;

        /**
         * Default constructor.
         * @param cms the {@link CmsObject} used to read properties.
         * @param resource the resource for which properties should be read.
         * @param search flag, indicating if property should be searched or not.
         */
        public CmsLocalePropertyLoaderTransformer(CmsObject cms, CmsResource resource, boolean search) {

            m_cms = cms;
            m_res = resource;
            m_search = search;
            m_localeProperties = new HashMap<Locale, Map<String, String>>();
        }

        /**
         * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
         */
        public Object transform(Object inputLocale) {

            Locale locale = null;
            if (null != inputLocale) {
                if (inputLocale instanceof Locale) {
                    locale = (Locale)inputLocale;
                } else if (inputLocale instanceof String) {
                    try {
                        locale = LocaleUtils.toLocale((String)inputLocale);
                    } catch (@SuppressWarnings("unused") IllegalArgumentException | NullPointerException e) {
                        // do nothing, just go on without locale
                    }
                }
            }
            if (m_localeProperties.get(locale) == null) {
                Map<String, String> lazyMap = CmsCollectionsGenericWrapper.createLazyMap(
                    new CmsJspValueTransformers.CmsPropertyLoaderTransformer(m_cms, m_res, m_search, locale));
                m_localeProperties.put(locale, lazyMap);
            }
            return m_localeProperties.get(locale);
        }

    }

    /**
     * Transformer that reads a resource property,
     * the input is used as String for the property name to read.<p>
     */
    public static final class CmsPropertyLoaderTransformer implements Transformer {

        /** The {@link CmsObject} used for reading properties. */
        private CmsObject m_cms;
        /** The resource where the properties are read from. */
        private CmsResource m_res;
        /** The locale for which properties should be read. */
        private Locale m_locale;
        /** Search for the property or not. */
        private boolean m_search;

        /**
         * Creates a new property loading Transformer.<p>
         *
         * @param resource the resource where the properties are read from
         * @param cms the {@link CmsObject} used for reading properties.
         * @param search flag, indicating if property should be searched or not.
         */
        public CmsPropertyLoaderTransformer(CmsObject cms, CmsResource resource, boolean search) {

            m_cms = cms;
            m_res = resource;
            m_search = search;

        }

        /**
         * Creates a new property loading Transformer.<p>
         *
         * @param resource the resource where the properties are read from
         * @param locale the locale for which properties should be accessed
         * @param cms the {@link CmsObject} used for reading properties.
         * @param search flag, indicating if property should be searched or not.
         */
        public CmsPropertyLoaderTransformer(CmsObject cms, CmsResource resource, boolean search, Locale locale) {

            m_cms = cms;
            m_res = resource;
            m_locale = locale;
            m_search = search;

        }

        /**
         * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
         */
        public Object transform(Object input) {

            String result;
            try {
                // read the requested property
                result = m_cms.readPropertyObject(m_res, String.valueOf(input), m_search, m_locale).getValue();
            } catch (@SuppressWarnings("unused") CmsException e) {
                // unable to read property, return null
                result = null;
            }
            return result;
        }
    }

    /** Hide the default constructor. */
    private CmsJspValueTransformers() {
        // just hide the default constructor
    }

}
