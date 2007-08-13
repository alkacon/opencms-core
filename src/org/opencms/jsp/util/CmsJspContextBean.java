/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/util/Attic/CmsJspContextBean.java,v $
 * Date   : $Date: 2007/08/13 16:30:11 $
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
import org.opencms.main.CmsException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.map.LazyMap;

public class CmsJspContextBean {

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

            Map result;
            try {
                // read the properties of the requested resource
                result = CmsProperty.toMap(m_cms.readPropertyObjects((String)input, m_search));
            } catch (CmsException e) {
                // unable to read resource, return null
                result = Collections.EMPTY_MAP;
            }
            return result;
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
                result = m_cms.readResource((String)input);
            } catch (CmsException e) {
                // unable to read resource, return null
                result = null;
            }
            return result;
        }
    }

    /** The OpenCms context of the current user. */
    protected CmsObject m_cms;

    /** Properties loaded from the OpenCms VFS. */
    private Map m_readProperties;

    /** Properties loaded from the OpenCms VFS with search. */
    private Map m_readPropertiesSearch;

    /** Resources loaded from the OpenCms VFS. */
    private Map m_readResources;

    /**
     * Creates a new context bean using the OpenCms context of the current user.<p>
     * 
     * @param cms the OpenCms context of the current user
     */
    public CmsJspContextBean(CmsObject cms) {

        m_cms = cms;
    }

    /**
     * Returns a map the lazily reads all resource properties from the OpenCms VFS, without search.<p>
     * 
     * @return a map the lazily reads all resource properties from the OpenCms VFS, without search
     */
    public Map getReadProperties() {

        if (m_readProperties == null) {
            // create a new lazy loading map that read the requested resource properties
            m_readProperties = LazyMap.decorate(new HashMap(), new CmsPropertyLoaderTransformer(false));
        }
        return m_readProperties;
    }

    /**
     * Returns a map the lazily reads all resource properties from the OpenCms VFS, with search.<p>
     * 
     * @return a map the lazily reads all resource properties from the OpenCms VFS, with search
     */
    public Map getReadPropertiesSearch() {

        if (m_readPropertiesSearch == null) {
            // create a new lazy loading map that read the requested resource properties
            m_readPropertiesSearch = LazyMap.decorate(new HashMap(), new CmsPropertyLoaderTransformer(true));
        }
        return m_readPropertiesSearch;
    }

    /**
     * Returns a map the lazily reads resources from the OpenCms VFS.<p>
     * 
     * @return a map the lazily reads resources from the OpenCms VFS
     */
    public Map getReadResource() {

        if (m_readResources == null) {
            // create a new lazy loading map that read the requested resources
            m_readResources = LazyMap.decorate(new HashMap(), new CmsResourceLoaderTransformer());
        }
        return m_readResources;
    }
}