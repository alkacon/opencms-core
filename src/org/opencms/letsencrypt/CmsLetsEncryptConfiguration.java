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

package org.opencms.letsencrypt;

import org.opencms.configuration.CmsElementWithAttrsParamConfigHelper;
import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.configuration.I_CmsConfigurationParameterHandler;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;

import org.apache.commons.logging.Log;

/**
 * Configuration class containing the LetsEncrypt configuration settings OpenCms needed by OpenCms.
 */
public class CmsLetsEncryptConfiguration implements I_CmsConfigurationParameterHandler {

    /** Enum which represents different modes that control which domains OpenCms puts into the certificate configuration. */
    public static enum Mode {

        /** Use both site domains and workplace domains in certificate configuration. */
        all,

        /** Do not generate the certificate configuration. */
        disabled,

        /** Only use site domains in certificate configuration. */
        sites,

        /** Only use workplace domains in certificate configuration. */
        workplace
    }

    /**
     * Enum which represents the different types of events that LetsEncrypt updates should be triggered by.
     */
    public static enum Trigger {
        /** Triggered when webserver config is updated. */
        siteConfig,

        /** Triggered when the webserver thread is run. */
        webserverThread
    }

    /** Attribute name for the certificate configuration path. */
    public static final String ATTR_CERTCONFIG = "certconfig";

    /** Attribute name for the host. */
    public static final String ATTR_HOST = "host";

    /** Attribute name for the mode. */
    public static final String ATTR_MODE = "mode";

    /** Attribute name for the port. */
    public static final String ATTR_PORT = "port";

    /** Attribute name for the trigger mode. */
    public static final String ATTR_TRIGGER = "trigger";

    /** Node name. */
    public static final String N_LETSENCRYPT = "letsencrypt";

    /**
     * Helper for parsing / generating the configuration.<p>
     **/
    public static final CmsElementWithAttrsParamConfigHelper CONFIG_HELPER = new CmsElementWithAttrsParamConfigHelper(
        "*/system",
        N_LETSENCRYPT,
        CmsLetsEncryptConfiguration.class,
        ATTR_HOST,
        ATTR_PORT,
        ATTR_MODE,
        ATTR_CERTCONFIG,
        ATTR_TRIGGER);

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsLetsEncryptConfiguration.class);

    /** The default trigger mode. */
    public static final Trigger DEFAULT_TRIGGER = Trigger.siteConfig;

    /** The internal configuration object. */
    private CmsParameterConfiguration m_config = new CmsParameterConfiguration();

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    public void addConfigurationParameter(String paramName, String paramValue) {

        m_config.put(paramName, paramValue);
    }

    /**
     * Gets the path where the certificate configuration should be written to.<p>
     *
     * @return the certificate configuration target path
     */
    public String getCertConfigPath() {

        return m_config.get(ATTR_CERTCONFIG);
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#getConfiguration()
     */
    public CmsParameterConfiguration getConfiguration() {

        return CmsParameterConfiguration.unmodifiableVersion(m_config);

    }

    /**
     * Gets the host name for the LetsEncrypt docker container.<p>
     *
     * The host name is used to signal to the LetsEncrypt container that the configuration has been updated.
     *
     * @return the host name of the LetsEncrypt container
     */
    public String getHost() {

        return m_config.get(ATTR_HOST);
    }

    /**
     * Gets the configured mode, or null if no mode or an invalid mode have been configured.<p>
     *
     * @return the mode
     */
    public Mode getMode() {

        try {
            return Mode.valueOf(m_config.get(ATTR_MODE));
        } catch (Exception e) {
            LOG.error("Error getting letsencrypt mode: " + e.getLocalizedMessage(), e);
            return null;
        }
    }

    /**
     * Gets the configured port, or -1 if the port is not set or has an invalid value.<p>
     *
     * The port is used to signal to the LetsEncrypt docker container that the certificate configuration has changed.
     *
     * @return the configured port
     */
    public int getPort() {

        try {
            String portStr = m_config.get(ATTR_PORT);
            return Integer.valueOf(portStr).intValue();
        } catch (Exception e) {
            LOG.error("Error getting letsencrypt port: " + e.getLocalizedMessage(), e);
            return -1;
        }
    }

    /**
     * Gets the trigger mode.<p>
     *
     * @return the trigger mode
     */
    public Trigger getTrigger() {

        try {
            String triggerStr = m_config.get(ATTR_TRIGGER);
            if (triggerStr == null) {
                return DEFAULT_TRIGGER; // trigger is optional, don't log an error
            }
            Trigger trigger = Trigger.valueOf(triggerStr);
            return trigger;
        } catch (Exception e) {
            LOG.error("Error getting configured letsencrypt trigger: " + e.getLocalizedMessage(), e);
            return DEFAULT_TRIGGER;
        }

    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#initConfiguration()
     */
    public void initConfiguration() {
        // do nothing
    }

    /**
     * Checks if the configuration is enabled and does not have missing settings.<p>
     *
     * @return true if the configuration is enabled and does not have missing settings
     */
    public boolean isValidAndEnabled() {

        return (getMode() != null)
            && (getMode() != Mode.disabled)
            && (getPort() > -1)
            && !CmsStringUtil.isEmptyOrWhitespaceOnly(getCertConfigPath())
            && !CmsStringUtil.isEmptyOrWhitespaceOnly(getHost());
    }

}
