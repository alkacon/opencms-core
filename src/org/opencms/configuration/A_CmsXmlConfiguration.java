/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/configuration/A_CmsXmlConfiguration.java,v $
 * Date   : $Date: 2005/06/23 11:11:38 $
 * Version: $Revision: 1.16 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.configuration;

import org.opencms.main.CmsLog;

import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Abstract base implementation for xml configurations.<p>
 * 
 * @author Alexander Kandzior
 * 
 * @version $Revision: 1.16 $
 * 
 * @since 6.0.0
 */
public abstract class A_CmsXmlConfiguration implements I_CmsXmlConfiguration {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(A_CmsXmlConfiguration.class);

    /** The name of the XML file used for this configuration. */
    private String m_xmlFileName;

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    public void addConfigurationParameter(String paramName, String paramValue) {

        // simple default configuration does not support parameters 
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_ADD_CONFIG_PARAM_3, paramName, paramValue, this));
        }
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#getConfiguration()
     */
    public Map getConfiguration() {

        // simple default configuration does not support parameters
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_GET_CONFIGURATION_1, this));
        }
        return null;
    }

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#getDtdSystemLocation()
     */
    public String getDtdSystemLocation() {

        return CmsConfigurationManager.C_DEFAULT_DTD_LOCATION;
    }

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#getDtdUrlPrefix()
     */
    public String getDtdUrlPrefix() {

        return CmsConfigurationManager.C_DEFAULT_DTD_PREFIX;
    }

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#getXmlFileName()
     */
    public String getXmlFileName() {

        return m_xmlFileName;
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#initConfiguration()
     */
    public void initConfiguration() {

        // simple default configuration does not need to be initialized
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_INIT_CONFIGURATION_1, this));
        }
    }

    /**
     * Sets the file name of this XML configuration.<p>
     * 
     * @param fileName the file name to set
     */
    protected void setXmlFileName(String fileName) {

        m_xmlFileName = fileName;
    }
}
