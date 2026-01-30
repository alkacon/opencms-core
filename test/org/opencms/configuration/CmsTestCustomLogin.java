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

package org.opencms.configuration;

import org.opencms.file.CmsObject;
import org.opencms.security.I_CmsCustomLogin;

import java.util.Locale;

public class CmsTestCustomLogin implements I_CmsCustomLogin {

    private CmsParameterConfiguration m_params = new CmsParameterConfiguration();

    @Override
    public void addConfigurationParameter(String paramName, String paramValue) {

        m_params.add(paramName, paramValue);

    }

    @Override
    public CmsParameterConfiguration getConfiguration() {

        return m_params;
    }

    @Override
    public String getLoginButtonCaption(Locale locale) {

        return "foo";
    }

    @Override
    public String getRedirect(String orgUnit) {

        return orgUnit;
    }

    @Override
    public void initConfiguration() throws CmsConfigurationException {

    }

    @Override
    public void initialize(CmsObject cms) {

    }

    public boolean isEnabled() {

        return false;
    }

    @Override
    public boolean needsOrgUnit() {

        return false;
    }

}
