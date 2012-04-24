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

package org.opencms.cmis;

import org.opencms.file.CmsObject;

import java.math.BigInteger;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.impl.server.AbstractServiceFactory;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.apache.chemistry.opencmis.server.support.CmisServiceWrapper;

public class CmsCmisServiceFactory extends AbstractServiceFactory {

    private static final BigInteger DEFAULT_MAX_ITEMS_TYPES = BigInteger.valueOf(50);
    private static final BigInteger DEFAULT_DEPTH_TYPES = BigInteger.valueOf(-1);
    private static final BigInteger DEFAULT_MAX_ITEMS_OBJECTS = BigInteger.valueOf(200);
    private static final BigInteger DEFAULT_DEPTH_OBJECTS = BigInteger.valueOf(10);

    private static CmsObject m_adminCms;

    private static CmsCmisRepositoryManager m_repositoryManager;

    public static void initializeStatic(CmsObject adminCms) {

        m_repositoryManager = new CmsCmisRepositoryManager();
        m_repositoryManager.initialize(adminCms);
    }

    @Override
    public void init(Map<String, String> parameters) {

    }

    @Override
    public void destroy() {

    }

    @Override
    public CmisService getService(CallContext context) {

        CmisServiceWrapper<CmsCmisService> wrapperService = null;
        wrapperService = new CmisServiceWrapper<CmsCmisService>(
            new CmsCmisService(m_repositoryManager),
            DEFAULT_MAX_ITEMS_TYPES,
            DEFAULT_DEPTH_TYPES,
            DEFAULT_MAX_ITEMS_OBJECTS,
            DEFAULT_DEPTH_OBJECTS);

        wrapperService.getWrappedService().setCallContext(context);

        return wrapperService;
    }
}
