/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/util/Attic/CmsJspContentUtilBean.java,v $
 * Date   : $Date: 2007/08/14 13:12:37 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2007 Alkacon Software GmbH (http://www.alkacon.com)
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
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsException;
import org.opencms.util.CmsUUID;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.map.LazyMap;

/**
 * Utility methods that allow convenient access to the OpenCms VFS from the JSP EL with the JSTL.<p>
 * 
 * @author Alexander Kandzior
 * 
 * @version $Revision: 1.2 $ 
 * 
 * @since 7.0.2
 * 
 * @see CmsJspContentAccessBean
 */
public class CmsJspContentUtilBean {

    /**
     * Provides a Map which contains long values mapped to Dates, 
     * the input is assumed to be a String that represents long time stamp.<p>
     * 
     * This is a helper method as it is often required to create a date object from a long 
     * value stored in a {@link CmsResource}.<p>
     */
    public class CmsConvertDateTransformer implements Transformer {

        /**
         * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
         */
        public Object transform(Object input) {

            return convertDate(input);
        }
    }

    /**
     * Transformer that loads all properties of a resource from the OpenCms VFS, 
     * the input is used as String for the resource name to read.<p>
     */
    public class CmsPropertyLoaderTransformer implements Transformer {

        /** Indicates if properties should be searchen when loaded. */
        private boolean m_search;

        /**
         * Creates a new property loading Transformer.<p>
         * 
         * @param search indicates if properties should be searchen when loaded
         */
        public CmsPropertyLoaderTransformer(boolean search) {

            m_search = search;
        }

        /**
         * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
         */
        public Object transform(Object input) {

            Map result = null;
            // first read the resource using the lazy map 
            CmsResource resource = (CmsResource)getReadResource().get(input);
            if (resource != null) {
                try {
                    // read the properties of the requested resource
                    result = CmsProperty.toMap(m_cms.readPropertyObjects(resource, m_search));
                } catch (CmsException e) {
                    // unable to read resource, return empty map
                }
            }
            // result may still be null
            return (result == null) ? Collections.EMPTY_MAP : result;
        }
    }

    /**
     * Transformer that loads a resource from the OpenCms VFS, the input 
     * is used as String for the resource name to read.<p>
     */
    public class CmsResourceLoaderTransformer implements Transformer {

        /**
         * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
         */
        public Object transform(Object input) {

            CmsResource result;
            try {
                // read the requested resource
                result = convertResource(m_cms, input);
            } catch (CmsException e) {
                // unable to read resource, return null
                result = null;
            }
            return result;
        }
    }

    /** The OpenCms context of the current user. */
    protected CmsObject m_cms;

    /** Contains the converted dates. */
    private Map m_convertDate;

    /** Properties loaded from the OpenCms VFS. */
    private Map m_properties;

    /** Properties loaded from the OpenCms VFS with search. */
    private Map m_propertiesSearch;

    /** Resources loaded from the OpenCms VFS. */
    private Map m_resources;

    /**
     * Creates a new context bean using the OpenCms context of the current user.<p>
     * 
     * @param cms the OpenCms context of the current user
     */
    public CmsJspContentUtilBean(CmsObject cms) {

        m_cms = cms;
    }

    /**
     * Returns a Date created from an Object.<p>
     * 
     * <ul>
     * <li>The Object is first checked if it is a {@link Date} already, if so it is casted and returned unchanged.
     * <li>If not, the input is checked if it is a {@link Long}, and if so the Date is created from the Long value.
     * <li>If it's not a Date and not a Long, the Object is transformed to a String and then it's tried 
     * to parse a Long out of the String. 
     * <li>If this fails, it is tried to parse as a Date using the
     * default date formatting. 
     * <li>If this also fails, a new Date is returned that has been initialized with 0.<p>
     * </ul>
     * 
     * @param input the Object to create a Date from
     * 
     * @return a Date created from the given Object
     */
    protected static Date convertDate(Object input) {

        Date result;
        if (input instanceof Date) {
            result = (Date)input;
        } else if (input instanceof Long) {
            result = new Date(((Long)input).longValue());
        } else {
            String str = String.valueOf(input);
            try {
                // treat the input as a String
                long l = Long.parseLong(str);
                result = new Date(l);
            } catch (NumberFormatException e) {
                try {
                    // try to parse String as a Date
                    result = DateFormat.getDateInstance().parse(str);
                } catch (ParseException e1) {
                    result = null;
                }
                if (result == null) {
                    // use default date if parsing fails
                    result = new Date(0);
                }
            }
        }
        return result;
    }

    /**
     * Returns a Locale created from an Object.<p>
     * 
     * <ul>
     * <li>The Object is first checked if it is a {@link Locale} already, if so it is casted and returned. 
     * <li>If not, the input is transformed to a String and then a Locale lookup with this String is done.
     * <li>If the locale lookup fails, the OpenCms default locale is returned.
     * </ul>
     * 
     * @param input the Object to create a Locale from 
     * 
     * @return a Locale created from the given Object
     */
    protected static Locale convertLocale(Object input) {

        Locale locale;
        if (input instanceof Locale) {
            locale = (Locale)input;
        } else {
            locale = CmsLocaleManager.getLocale(String.valueOf(input));
        }
        return locale;
    }

    /**
     * Returns a resource created from an Object.<p> 
     * 
     * <ul>
     * <li>If the input is already a {@link CmsResource}, it is casted to the resource and returned unchanged.
     * <li>If the input is a String, the given OpenCms context is used to read a resource with this name from the VFS.
     * <li>If the input is a {@link CmsUUID}, the given OpenCms context is used to read a resource with 
     * this UUID from the VFS.
     * <li>Otherwise the input is converted to a String, and then the given OpenCms context is used to read 
     * a resource with this name from the VFS.
     * </ul>
     * 
     * @param cms the current OpenCms user context
     * @param input the input to create a resource from
     * 
     * @return a resource created from the given Object
     * 
     * @throws CmsException in case of errors accessing the OpenCms VFS for reading the resource
     */
    protected static CmsResource convertResource(CmsObject cms, Object input) throws CmsException {

        CmsResource result;
        if (input instanceof String) {
            // input is a String
            result = cms.readResource((String)input);
        } else if (input instanceof CmsResource) {
            // input is already a resource
            result = (CmsResource)input;
        } else if (input instanceof CmsUUID) {
            // input is a UUID
            result = cms.readResource((CmsUUID)input);
        } else {
            // input seems not really to make sense, try to use it like a String
            result = cms.readResource(String.valueOf(input));
        }
        return result;
    }

    /**
     * Returns a lazy initialized Map that provides a {@link Date} for each long value used as a key in the Map.<p> 
     *  
     * This is a utility method which allows easy conversion of the date values found as long in the 
     * {@link CmsJspContentAccessBean#getFile()} raw file object.<p>
     * 
     * Usage example on a JSP with the JSTL:<pre>
     * &lt;cms:contentload ... &gt;
     *     &lt;cms:contentaccess var="content" /&gt; 
     *     Date the resource was created: &lt;fmt:formatDate value="${content.util.convertDate[content.file.dateCreated]}" /&gt;
     * &lt;/cms:contentload&gt;</pre>
     *  
     * @return a lazy initialized Map that provides a {@link Date} for each long value used as a key in the Map
     */
    public Map getConvertDate() {

        if (m_convertDate == null) {
            m_convertDate = LazyMap.decorate(new HashMap(), new CmsConvertDateTransformer());
        }
        return m_convertDate;
    }

    /**
     * Returns a map the lazily reads all resource properties from the OpenCms VFS, without search.<p>
     * 
     * @return a map the lazily reads all resource properties from the OpenCms VFS, without search
     */
    public Map getReadProperties() {

        if (m_properties == null) {
            // create a new lazy loading map that read the requested resource properties
            m_properties = LazyMap.decorate(new HashMap(), new CmsPropertyLoaderTransformer(false));
        }
        return m_properties;
    }

    /**
     * Returns a map the lazily reads all resource properties from the OpenCms VFS, with search.<p>
     * 
     * @return a map the lazily reads all resource properties from the OpenCms VFS, with search
     */
    public Map getReadPropertiesSearch() {

        if (m_propertiesSearch == null) {
            // create a new lazy loading map that read the requested resource properties
            m_propertiesSearch = LazyMap.decorate(new HashMap(), new CmsPropertyLoaderTransformer(true));
        }
        return m_propertiesSearch;
    }

    /**
     * Returns a map the lazily reads resources from the OpenCms VFS.<p>
     * 
     * @return a map the lazily reads resources from the OpenCms VFS
     */
    public Map getReadResource() {

        if (m_resources == null) {
            // create a new lazy loading map that read the requested resources
            m_resources = LazyMap.decorate(new HashMap(), new CmsResourceLoaderTransformer());
        }
        return m_resources;
    }
}