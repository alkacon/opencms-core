/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2011 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.config;

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.ade.configuration.CmsModelPageConfig;
import org.opencms.ade.configuration.CmsPropertyConfig;
import org.opencms.ade.configuration.CmsResourceTypeConfig;
import org.opencms.ade.detailpage.CmsDetailPageInfo;

import java.util.List;

/**
 * fasfasdf.<p>
 */
public class CmsTestConfigData extends CmsADEConfigData {

    public CmsADEConfigData m_parent;

    public CmsTestConfigData(
        String basePath,
        List<CmsResourceTypeConfig> resourceTypeConfig,
        List<CmsPropertyConfig> propertyConfig,
        List<CmsDetailPageInfo> detailPageInfos,
        List<CmsModelPageConfig> modelPages) {

        super(basePath, resourceTypeConfig, propertyConfig, detailPageInfos, modelPages);
    }

    public CmsADEConfigData parent() {

        return m_parent;
    }

    public void setParent(CmsADEConfigData parent) {

        m_parent = parent;
    }

}
