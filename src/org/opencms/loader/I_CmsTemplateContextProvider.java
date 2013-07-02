/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * Interface for template context providers.<p>
 * 
 * Implementations of this class are used to dynamically determine a template used for rendering 
 * a page at runtime, e.g. there could be one template for desktop browsers and another for mobile 
 * browsers. 
 * 
 * It is possible to override the template using a cookie containing the name of the template context
 * which should be used as a value. The cookie name is determined by the template context provider.<p>
 */
public interface I_CmsTemplateContextProvider {

    /** 
     * Gets a map of all template contexts, with the template context names as keys.<p>
     * 
     * @return the map of template context providers 
     */
    Map<String, CmsTemplateContext> getAllContexts();

    /** 
     * Returns the style sheet to be used for the editor.<p>
     * 
     * @param cms the current CMS context
     * @param editedResourcePath the path of the edited resource 
     * 
     * @return the path of the style sheet to be used for the resource 
     */
    String getEditorStyleSheet(CmsObject cms, String editedResourcePath);

    /**
     * Gets the name of the cookie which should be used for overriding the template context.<p>
     * 
     * @return the name of the cookie used for overriding the template context 
     */
    String getOverrideCookieName();

    /**
     * Determines the template context from the current CMS context, request, and resource.<p>
     * 
     * @param cms the CMS context
     * @param request the current request 
     * @param resource the resource being rendered 
     *  
     * @return the current template context 
     */
    CmsTemplateContext getTemplateContext(CmsObject cms, HttpServletRequest request, CmsResource resource);

    /**
     * Initializes the context provider using a CMS object.<p>
     * 
     * @param cms the current CMS context
     */
    void initialize(CmsObject cms);

}
