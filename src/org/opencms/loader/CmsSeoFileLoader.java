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

package org.opencms.loader;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.content.CmsXmlContentFactory;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

/**
 * The SEO file loader.<p>
 */
public class CmsSeoFileLoader extends A_CmsXmlDocumentLoader {

    /** The loader id. */
    public static final int LOADER_ID = 14;

    /** The SEO file template JSP path. */
    public static final String TEMPLATE_PATH = "/system/modules/org.opencms.ade.config/pages/render-seo-file.jsp";

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#getLoaderId()
     */
    public int getLoaderId() {

        return LOADER_ID;
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#getResourceLoaderInfo()
     */
    public String getResourceLoaderInfo() {

        return Messages.get().getBundle().key(Messages.GUI_LOADER_SEOFILE_DEFAULT_DESC_0);
    }

    /**
     * @see org.opencms.loader.A_CmsXmlDocumentLoader#getTemplateLoaderFacade(org.opencms.file.CmsObject, org.opencms.file.CmsResource, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected CmsTemplateLoaderFacade getTemplateLoaderFacade(
        CmsObject cms,
        CmsResource resource,
        HttpServletRequest req) throws CmsException {

        CmsResource template = cms.readFile(TEMPLATE_PATH, CmsResourceFilter.IGNORE_EXPIRATION);
        return new CmsTemplateLoaderFacade(OpenCms.getResourceManager().getLoader(template), resource, template);
    }

    /**
     * @see org.opencms.loader.A_CmsXmlDocumentLoader#getTemplatePropertyDefinition()
     */
    @Override
    protected String getTemplatePropertyDefinition() {

        throw new CmsRuntimeException(
            Messages.get().container(Messages.ERR_TEMPLATE_PROTERTY_UNSUPPORTED_1, getClass().getName()));
    }

    /**
     * @see org.opencms.loader.A_CmsXmlDocumentLoader#unmarshalXmlDocument(org.opencms.file.CmsObject, org.opencms.file.CmsResource, javax.servlet.ServletRequest)
     */
    @Override
    protected I_CmsXmlDocument unmarshalXmlDocument(CmsObject cms, CmsResource resource, ServletRequest req)
    throws CmsException {

        return CmsXmlContentFactory.unmarshal(cms, resource, req);
    }

}
