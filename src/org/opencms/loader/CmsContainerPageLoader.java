/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/loader/Attic/CmsContainerPageLoader.java,v $
 * Date   : $Date: 2009/09/14 13:59:36 $
 * Version: $Revision: 1.1.2.6 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2009 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.loader;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.workplace.editors.ade.CmsContainerPageCache;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import javax.servlet.ServletRequest;

/**
 * OpenCms loader for resources of type <code>{@link org.opencms.file.types.CmsResourceTypeContainerPage}</code>.<p>
 *
 * It is just a xml-content loader with special object caching.<p>
 *
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.1.2.6 $ 
 * 
 * @since 7.6
 */
public class CmsContainerPageLoader extends A_CmsXmlDocumentLoader {

    /** Xml content node constant name. */
    public static final String N_CONTAINER = "Containers";

    /** Xml content node constant element. */
    public static final String N_ELEMENT = "Elements";

    /** Xml content node constant formatter. */
    public static final String N_FORMATTER = "Formatter";

    /** Xml content node constant name. */
    public static final String N_NAME = "Name";

    /** Xml content node constant type. */
    public static final String N_TYPE = "Type";

    /** Xml content node constant uri. */
    public static final String N_URI = "Uri";

    /** The id of this loader. */
    public static final int RESOURCE_LOADER_ID = 11;

    /**
     * Default constructor.<p>
     */
    public CmsContainerPageLoader() {

        // empty
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#getLoaderId()
     */
    public int getLoaderId() {

        return RESOURCE_LOADER_ID;
    }

    /**
     * Returns a String describing this resource loader, which is (localized to the system default locale)
     * <code>"The OpenCms default resource loader for container page"</code>.<p>
     * 
     * @return a describing String for the ResourceLoader 
     */
    public String getResourceLoaderInfo() {

        return Messages.get().getBundle().key(Messages.GUI_LOADER_CONTAINERPAGE_DEFAULT_DESC_0);
    }

    /**
     * @see org.opencms.loader.A_CmsXmlDocumentLoader#isUsableForTemplates()
     */
    @Override
    public boolean isUsableForTemplates() {

        return true;
    }

    /**
     * @see org.opencms.loader.A_CmsXmlDocumentLoader#getTemplatePropertyDefinition()
     */
    @Override
    protected String getTemplatePropertyDefinition() {

        return CmsPropertyDefinition.PROPERTY_TEMPLATE_ELEMENTS;
    }

    /**
     * @see org.opencms.loader.A_CmsXmlDocumentLoader#unmarshalXmlDocument(org.opencms.file.CmsObject, org.opencms.file.CmsResource, javax.servlet.ServletRequest)
     */
    @Override
    protected I_CmsXmlDocument unmarshalXmlDocument(CmsObject cms, CmsResource resource, ServletRequest req)
    throws CmsException {

        CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, resource, req);
        CmsContainerPageCache.getInstance().setCache(cms, resource, content);
        return content;
    }
}
