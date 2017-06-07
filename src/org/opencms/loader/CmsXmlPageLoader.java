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

package org.opencms.loader;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.page.CmsXmlPageFactory;

import javax.servlet.ServletRequest;

/**
 * OpenCms loader for resources of type <code>{@link org.opencms.file.types.CmsResourceTypeXmlPage}</code>.<p>
 *
 * @since 6.0.0
 */
public class CmsXmlPageLoader extends A_CmsXmlDocumentLoader {

    /** The id of this loader. */
    public static final int RESOURCE_LOADER_ID = 9;

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#getLoaderId()
     */
    public int getLoaderId() {

        return RESOURCE_LOADER_ID;
    }

    /**
     * Returns a String describing this resource loader, which is (localized to the system default locale)
     * <code>"The OpenCms default resource loader for xml pages"</code>.<p>
     *
     * @return a describing String for the ResourceLoader
     */
    public String getResourceLoaderInfo() {

        return Messages.get().getBundle().key(Messages.GUI_LOADER_XMLPAGE_DEFAULT_DESC_0);
    }

    /**
     * @see org.opencms.loader.A_CmsXmlDocumentLoader#unmarshalXmlDocument(org.opencms.file.CmsObject, org.opencms.file.CmsResource, javax.servlet.ServletRequest)
     */
    @Override
    protected I_CmsXmlDocument unmarshalXmlDocument(CmsObject cms, CmsResource resource, ServletRequest req)
    throws CmsException {

        return CmsXmlPageFactory.unmarshal(cms, resource, req);
    }

    /**
     * @see org.opencms.loader.A_CmsXmlDocumentLoader#getTemplatePropertyDefinition()
     */
    @Override
    protected String getTemplatePropertyDefinition() {

        return CmsPropertyDefinition.PROPERTY_TEMPLATE;
    }
}