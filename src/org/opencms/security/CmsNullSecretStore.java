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
 * For further information about Alkacon Software, please see the
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.security;

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.file.CmsObject;

/**
 * Secret store that stores absolutely no secrets.
 *
 * <p>This is used when no class attribute is specified in the secret store configuration.
 */
public class CmsNullSecretStore implements I_CmsSecretStore {

    /** The internal configuration map. */
    private CmsParameterConfiguration m_config = new CmsParameterConfiguration();

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    @Override
    public void addConfigurationParameter(String paramName, String paramValue) {

        // ignore
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#getConfiguration()
     */
    @Override
    public CmsParameterConfiguration getConfiguration() {

        return m_config;
    }

    /**
     * @see org.opencms.security.I_CmsSecretStore#getSecret(java.lang.String)
     */
    @Override
    public String getSecret(String key) {

        return null;
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#initConfiguration()
     */
    @Override
    public void initConfiguration() throws CmsConfigurationException {

    }

    /**
     * @see org.opencms.security.I_CmsSecretStore#initialize(org.opencms.file.CmsObject)
     */
    @Override
    public void initialize(CmsObject cmsObject) {

        // TODO Auto-generated method stub

    }

}
