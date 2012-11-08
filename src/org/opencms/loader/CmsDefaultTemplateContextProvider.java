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
import org.opencms.jsp.util.CmsJspDeviceSelector;
import org.opencms.main.CmsLog;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;

/**
 * Example implementation of a template context provider for deciding between a desktop template and a mobile template.<p>
 * 
 * The template JSP paths are read from a file "templatecontext.properties" in the classpath.
 * 
 */
public class CmsDefaultTemplateContextProvider implements I_CmsTemplateContextProvider {

    /** The map of template contexts. */
    private Map<String, CmsTemplateContext> m_map = new HashMap<String, CmsTemplateContext>();

    /** The device selector used internally for detecting mobile devices. */
    CmsJspDeviceSelector m_selector = new CmsJspDeviceSelector();

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDefaultTemplateContextProvider.class);

    /** Default constructor. */
    public CmsDefaultTemplateContextProvider() {

    }

    /**
     * @see org.opencms.loader.I_CmsTemplateContextProvider#getAllContexts()
     */
    public synchronized Map<String, CmsTemplateContext> getAllContexts() {

        return Collections.unmodifiableMap(m_map);
    }

    /**
     * @see org.opencms.loader.I_CmsTemplateContextProvider#getOverrideCookieName()
     */
    public String getOverrideCookieName() {

        return "templatecontext";
    }

    /**
     * @see org.opencms.loader.I_CmsTemplateContextProvider#getTemplateContext(org.opencms.file.CmsObject, javax.servlet.http.HttpServletRequest, org.opencms.file.CmsResource)
     */
    public synchronized CmsTemplateContext getTemplateContext(
        CmsObject cms,
        HttpServletRequest request,
        CmsResource resource) {

        String deviceType = m_selector.getDeviceType(request);
        if (deviceType.equals(CmsJspDeviceSelector.C_MOBILE)) {
            return m_map.get("mobile");
        } else {
            return m_map.get("desktop");
        }
    }

    /**
     * @see org.opencms.loader.I_CmsTemplateContextProvider#initialize(org.opencms.file.CmsObject)
     */
    public void initialize(CmsObject cms) {

        try {
            Properties properties = new Properties();
            InputStream stream = getClass().getClassLoader().getResourceAsStream("templatecontext.properties");
            properties.load(stream);
            stream.close();
            String templateMobile = properties.getProperty("template.mobile");
            String templateDesktop = properties.getProperty("template.desktop");
            CmsTemplateContext mobile = new CmsTemplateContext("mobile", templateMobile, null, this);
            m_map.put(mobile.getKey(), mobile);
            CmsTemplateContext desktop = new CmsTemplateContext("desktop", templateDesktop, null, this);
            m_map.put(desktop.getKey(), desktop);
        } catch (Throwable t) {
            LOG.error(t.getLocalizedMessage(), t);
        }
    }
}
