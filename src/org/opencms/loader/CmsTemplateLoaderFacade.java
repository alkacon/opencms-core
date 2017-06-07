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

import org.opencms.file.CmsResource;

/**
 * Facade object that provides access to the template loader for a resource.<p>
 *
 * Some resource types are actually not loadable themself but require a template
 * to be processed. This template is attached using the <code>template</code> property.
 * Depending on the resource type of the template itself, a loader is then selected that
 * processed the requested resource. The processing itself might start on the template,
 * or on the requested resource (this will depend on the loader and the resource type implementation).<p>
 *
 * @since 6.0.0
 */
public class CmsTemplateLoaderFacade {

    /** The selected template context. */
    private CmsTemplateContext m_context;

    /** The resource loader. */
    private I_CmsResourceLoader m_loader;

    /** The resource requested by the user. */
    private CmsResource m_resource;

    /** The template file attached to the resource. */
    private CmsResource m_template;

    /** The template name. */
    private String m_templateName = "";

    /**
     * Creates a new template loader facade.<p>
     *
     * Some resource types are actually not loadable themself but require a template
     * to be processed. This template is attached using the <code>template</code> property.
     * Depending on the resource type of the template itself, a loader is then selected that
     * processed the requested resource. The processing itself might start on the template,
     * or on the requested resource (this will depend on the loader and the resource type implementation).<p>
     *
     * @param loader the loader to use
     * @param resource the file to use
     * @param template the template to use (ignored if null)
     * @throws CmsLoaderException in case the template file does not use a loader that actually supports templates
     */
    public CmsTemplateLoaderFacade(I_CmsResourceLoader loader, CmsResource resource, CmsResource template)
    throws CmsLoaderException {

        if (!loader.isUsableForTemplates()) {
            throw new CmsLoaderException(Messages.get().container(Messages.ERR_LOADER_NOT_TEMPLATE_ENABLED_0));
        }
        m_loader = loader;
        m_resource = resource;
        m_template = template;
    }

    /**
     * Returns the loader.<p>
     *
     * @return the loader
     */
    public I_CmsResourceLoader getLoader() {

        return m_loader;
    }

    /**
     * Returns the loaders start resource.<p>
     *
     * @return the loaders start resource
     */
    public CmsResource getLoaderStartResource() {

        if (m_loader.isUsingUriWhenLoadingTemplate()) {
            return m_resource;
        } else {
            return m_template;
        }
    }

    /**
     * Returns the resource.<p>
     *
     * @return the resource
     */
    public CmsResource getResource() {

        return m_resource;
    }

    /**
     * Returns the template.<p>
     *
     * @return the template
     */
    public CmsResource getTemplate() {

        return m_template;
    }

    /**
     * Gets the template context.<p>
     *
     * @return the template context
     */
    public CmsTemplateContext getTemplateContext() {

        return m_context;
    }

    /**
     * Gets the template name.<p>
     *
     * @return the template name
     */
    public String getTemplateName() {

        return m_templateName;
    }

    /**
     * Sets the template context.<p>
     *
     * @param context the template context
     */
    public void setTemplateContext(CmsTemplateContext context) {

        m_context = context;
    }

    /**
     * Sets the template name.<p>
     *
     * @param templateName the new template name
     */
    public void setTemplateName(String templateName) {

        m_templateName = templateName;
    }

}