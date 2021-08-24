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

package org.opencms.xml.xml2json.renderer;

import org.opencms.configuration.I_CmsConfigurationParameterHandler;
import org.opencms.file.CmsObject;
import org.opencms.json.JSONException;
import org.opencms.main.CmsException;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.xml2json.handler.CmsJsonHandlerContext;

import java.util.Locale;

/**
 * Renders a single-locale content tree as JSON.
 */
public interface I_CmsJsonRendererXmlContent extends I_CmsConfigurationParameterHandler {

    /**
     * Initializes the context for the renderer.
     *
     * @param context the context to set
     * @throws CmsException if something goes wrong
     */
    void initialize(CmsJsonHandlerContext context) throws CmsException;

    /**
     * Initializes the context for the renderer.
     *
     * @param cms the CMS object to set
     * @throws CmsException if something goes wrong
     */
    void initialize(CmsObject cms) throws CmsException;

    /**
     * Converts the XML content for a single locale to a JSON object
     *
     * @param content the content
     * @param locale the locale
     * @return the JSON object
     *
     * @throws JSONException if something goes wrong
     */
    Object render(CmsXmlContent content, Locale locale) throws JSONException;

}
