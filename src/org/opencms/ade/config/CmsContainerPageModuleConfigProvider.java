/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/ade/config/CmsContainerPageModuleConfigProvider.java,v $
 * Date   : $Date: 2011/05/03 16:50:25 $
 * Version: $Revision: 1.3 $
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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;

/**
 * Class for reading module configurations for container pages.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.3 $
 * 
 * @since 8.0.0
 */
public class CmsContainerPageModuleConfigProvider extends A_CmsModuleConfigProvider<CmsContainerPageConfigurationData> {

    /** The module configuration key. */
    public static final String MODULE_KEY = "containerpage.config";

    /**
     * Creates a new instance.<p>
     * 
     * @param cms the CMS object to use 
     */
    public CmsContainerPageModuleConfigProvider(CmsObject cms) {

        super(cms, MODULE_KEY);
    }

    /**
     * @see org.opencms.ade.config.A_CmsModuleConfigProvider#createEmptyConfiguration()
     */
    @Override
    protected CmsContainerPageConfigurationData createEmptyConfiguration() {

        return new CmsContainerPageConfigurationData();
    }

    /**
     * @see org.opencms.ade.config.A_CmsModuleConfigProvider#readSingleConfiguration(org.opencms.file.CmsObject, org.opencms.file.CmsResource)
     */
    @Override
    protected CmsContainerPageConfigurationData readSingleConfiguration(CmsObject cms, CmsResource res)
    throws CmsException {

        CmsConfigurationParser configParser = new CmsConfigurationParser();
        configParser.processFile(cms, res);
        return configParser.getContainerPageConfigurationData(new CmsConfigurationSourceInfo(res, true));
    }
}
