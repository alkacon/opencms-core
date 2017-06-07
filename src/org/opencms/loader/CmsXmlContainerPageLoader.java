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
import org.opencms.jsp.util.CmsJspStandardContextBean.TemplateBean;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.xml.containerpage.CmsXmlContainerPage;
import org.opencms.xml.containerpage.CmsXmlContainerPageFactory;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * OpenCms loader for resources of type <code>{@link org.opencms.file.types.CmsResourceTypeXmlContainerPage}</code>.<p>
 *
 * It is just a xml-content loader with template capabilities.<p>
 *
 * @since 7.6
 */
public class CmsXmlContainerPageLoader extends CmsXmlContentLoader {

    /** The id of this loader. */
    public static final int CONTAINER_PAGE_RESOURCE_LOADER_ID = 11;

    /**
     * Default constructor.<p>
     */
    public CmsXmlContainerPageLoader() {

        // empty
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#getLoaderId()
     */
    @Override
    public int getLoaderId() {

        return CONTAINER_PAGE_RESOURCE_LOADER_ID;
    }

    /**
     * Returns a String describing this resource loader, which is (localized to the system default locale)
     * <code>"The OpenCms default resource loader for container page"</code>.<p>
     *
     * @return a describing String for the ResourceLoader
     */
    @Override
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
     * @see org.opencms.loader.A_CmsXmlDocumentLoader#load(org.opencms.file.CmsObject, org.opencms.file.CmsResource, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public void load(CmsObject cms, CmsResource resource, HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException, CmsException {

        CmsTemplateLoaderFacade loaderFacade = OpenCms.getResourceManager().getTemplateLoaderFacade(
            cms,
            req,
            resource,
            getTemplatePropertyDefinition());
        CmsTemplateContext context = loaderFacade.getTemplateContext();
        req.setAttribute(CmsTemplateContextManager.ATTR_TEMPLATE_CONTEXT, context);
        TemplateBean templateBean = new TemplateBean(
            context != null ? context.getKey() : loaderFacade.getTemplateName(),
            loaderFacade.getTemplate());
        templateBean.setForced((context != null) && context.isForced());
        req.setAttribute(CmsTemplateContextManager.ATTR_TEMPLATE_BEAN, templateBean);
        loaderFacade.getLoader().load(cms, loaderFacade.getLoaderStartResource(), req, res);
    }

    /**
     * @see org.opencms.loader.A_CmsXmlDocumentLoader#getTemplatePropertyDefinition()
     */
    @Override
    protected String getTemplatePropertyDefinition() {

        return CmsPropertyDefinition.PROPERTY_TEMPLATE;
    }

    /**
     * @see org.opencms.loader.A_CmsXmlDocumentLoader#unmarshalXmlDocument(org.opencms.file.CmsObject, org.opencms.file.CmsResource, javax.servlet.ServletRequest)
     */
    @Override
    protected CmsXmlContainerPage unmarshalXmlDocument(CmsObject cms, CmsResource resource, ServletRequest req)
    throws CmsException {

        return CmsXmlContainerPageFactory.unmarshal(cms, resource, req);
    }
}
