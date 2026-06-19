/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.configuration;

import org.opencms.jsp.I_CmsElementMarker;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.containerpage.I_CmsFormatterBean;

import javax.servlet.jsp.PageContext;

/**
 * Just a dummy implementation of the I_CmsElementMarker interface that keeps its parameters.
 */
public class CmsTestDummyElementMarker implements I_CmsElementMarker {

    private CmsParameterConfiguration m_config = new CmsParameterConfiguration();

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    public void addConfigurationParameter(String paramName, String paramValue) {

        m_config.add(paramName, paramValue);
    }

    /**
     * @see org.opencms.jsp.I_CmsElementMarker#addEndMarker(javax.servlet.jsp.PageContext, org.opencms.xml.containerpage.CmsContainerElementBean)
     */
    public void addEndMarker(PageContext context, CmsContainerElementBean element) {

        // just a dummy implementation
    }

    /**
     * @see org.opencms.jsp.I_CmsElementMarker#addStartMarker(javax.servlet.jsp.PageContext, org.opencms.xml.containerpage.CmsContainerElementBean, org.opencms.xml.containerpage.I_CmsFormatterBean)
     */
    public void addStartMarker(PageContext context, CmsContainerElementBean element, I_CmsFormatterBean formatter) {

        // just a dummy implementation
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#getConfiguration()
     */
    public CmsParameterConfiguration getConfiguration() {

        return m_config;
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#initConfiguration()
     */
    public void initConfiguration() throws CmsConfigurationException {

        // no-op
    }
}
