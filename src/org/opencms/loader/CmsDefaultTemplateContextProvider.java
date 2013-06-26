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

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.util.CmsJspDeviceSelector;
import org.opencms.jsp.util.I_CmsJspDeviceSelector;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import java.io.ByteArrayInputStream;
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

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDefaultTemplateContextProvider.class);

    /** The map of template contexts. */
    private Map<String, CmsTemplateContext> m_map = new HashMap<String, CmsTemplateContext>();

    /** The device selector used internally for detecting mobile devices. */
    private CmsJspDeviceSelector m_selector = new CmsJspDeviceSelector();

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
     * Returns the absolute VFS path, where the configuration property file is stored.<p>
     * 
     * The configuration property file must have the following format:
     * <ul>
     * <li>template.mobile=/absolute/path/to/mobile/template.jsp
     * <li>template.desktop=/absolute/path/to/mobile/desktop.jsp
     * </ul>
     * 
     * By default this method returns <code>null</code> what will trigger the default behavior:<br/>
     * looking for a java property file named 'templatecontext.properties' in the class path.<p>
     * 
     * Extends this class, override this method and return the absolute VFS path where OpenCms
     * should lookup the property file, in order to configure the template JSP inside OpenCms.<p> 
     * 
     * @return the absolute VFS path, where the configuration property file is stored
     */
    public String getConfigurationPropertyPath() {

        return null;
    }

    /**
     * Returns the message container.<p>
     * 
     * @return the message container
     */
    public CmsMessageContainer getMessageContainerDesktop() {

        return null;
    }

    /**
     * Returns the message container.<p>
     * 
     * @return the message container
     */
    public CmsMessageContainer getMessageContainerMobile() {

        return null;
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

        I_CmsJspDeviceSelector selector = CmsFlexController.getController(request).getCmsCache().getDeviceSelector();
        if (selector == null) {
            selector = m_selector;
        }
        String deviceType = selector.getDeviceType(request);
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
            InputStream stream = null;
            if (getConfigurationPropertyPath() != null) {
                try {
                    CmsObject clone = OpenCms.initCmsObject(cms);
                    clone.getRequestContext().setSiteRoot("");
                    CmsResource res = clone.readResource(getConfigurationPropertyPath());
                    if (res != null) {
                        CmsFile file = cms.readFile(res);
                        stream = new ByteArrayInputStream(file.getContents());
                    }
                } catch (Exception e) {
                    LOG.error("Could not cerate input stream for given configuration path: "
                        + getConfigurationPropertyPath(), e);
                }
            }
            if (stream == null) {
                stream = getClass().getClassLoader().getResourceAsStream("templatecontext.properties");
            }
            Properties properties = new Properties();
            properties.load(stream);
            stream.close();
            String templateMobile = properties.getProperty("template.mobile");
            String templateDesktop = properties.getProperty("template.desktop");
            CmsTemplateContext mobile = new CmsTemplateContext(
                "mobile",
                templateMobile,
                getMessageContainerMobile(),
                this);
            m_map.put(mobile.getKey(), mobile);
            CmsTemplateContext desktop = new CmsTemplateContext(
                "desktop",
                templateDesktop,
                getMessageContainerDesktop(),
                this);
            m_map.put(desktop.getKey(), desktop);
        } catch (Throwable t) {
            LOG.error(t.getLocalizedMessage(), t);
        }
    }

}
