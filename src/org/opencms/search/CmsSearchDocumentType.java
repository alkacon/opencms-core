/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/CmsSearchDocumentType.java,v $
 * Date   : $Date: 2005/06/23 11:11:28 $
 * Version: $Revision: 1.9 $
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

package org.opencms.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A document type specifies which document factory class is used to pull the 
 * content of an OpenCms document into a Lucene index document.<p> 
 * 
 * The appropriate document factory class gets triggerd while the search index is built
 * for OpenCms documents matching the specified resource type and/or mimetype combination 
 * in a document factory class instance.<p>
 * 
 * @author Thomas Weckert  
 * 
 * @version $Revision: 1.9 $ 
 * 
 * @since 6.0.0 
 */
public class CmsSearchDocumentType implements Serializable, Cloneable {

    /** The name of the document factory class. */
    private String m_className;

    /** The mimetype to trigger the document factory class. */
    private List m_mimeTypes;

    /** The logical key/name of this document type. */
    private String m_name;

    /** A list of Cms resource types to trigger the document factory. */
    private List m_resourceTypes;

    /**
     * Default constructor.<p>
     */
    public CmsSearchDocumentType() {

        m_resourceTypes = new ArrayList();
        m_mimeTypes = new ArrayList();
    }

    /**
     * Returns the name of the document factory class.<p>
     *
     * @return the name of the document factory class
     */
    public String getClassName() {

        return m_className;
    }

    /**
     * Returns the mimetypes to trigger the document factory class.<p>
     *
     * @return the mimetypes to trigger the document factory class
     */
    public List getMimeTypes() {

        return m_mimeTypes;
    }

    /**
     * Returns the logical key/name of this document type.<p>
     *
     * @return the logical key/name of this document type
     */
    public String getName() {

        return m_name;
    }

    /**
     * Returns the list of Cms resource types to trigger the document factory.<p>
     *
     * @return the list of Cms resource types to trigger the document factory
     */
    public List getResourceTypes() {

        return m_resourceTypes;
    }

    /**
     * Sets the name of the document factory class.<p>
     *
     * @param className the name of the document factory class
     */
    public void setClassName(String className) {

        m_className = className;
    }

    /**
     * Sets the mimetypes to trigger the document factory class.<p>
     *
     * @param mimetypes the mimetypes to trigger the document factory class
     */
    public void setMimeTypes(List mimetypes) {

        m_mimeTypes = mimetypes;
    }

    /**
     * Sets the logical key/name of this document type.<p>
     *
     * @param name the logical key/name of this document type
     */
    public void setName(String name) {

        m_name = name;
    }

    /**
     * Sets the list of Cms resource types to trigger the document factory.<p>
     *
     * @param resourceTypes the list of Cms resource types to trigger the document factory
     */
    public void setResourceTypes(List resourceTypes) {

        m_resourceTypes = resourceTypes;
    }

    /**
     * Adds the class name of a resource type.<p>
     * 
     * @param resourceType the class name of a resource type
     */
    public void addResourceType(String resourceType) {

        m_resourceTypes.add(resourceType);
    }

    /**
     * Adds a mimetype.<p>
     * 
     * @param mimeType a mimetype
     */
    public void addMimeType(String mimeType) {

        m_mimeTypes.add(mimeType);
    }

}