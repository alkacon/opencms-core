/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/Attic/CmsCoreService.java,v $
 * Date   : $Date: 2010/04/06 12:21:52 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.gwt;

import org.opencms.gwt.shared.rpc.CmsRpcException;
import org.opencms.gwt.shared.rpc.I_CmsCoreService;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;

import org.apache.commons.logging.Log;

/**
 * Provides general core services.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 8.0.0
 * 
 * @see org.opencms.gwt.CmsCoreService
 * @see org.opencms.gwt.shared.rpc.I_CmsCoreService
 * @see org.opencms.gwt.shared.rpc.I_CmsCoreServiceAsync
 */
public class CmsCoreService extends CmsGwtService implements I_CmsCoreService {

    /** Serialization uid. */
    private static final long serialVersionUID = 5915848952948986278L;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsCoreService.class);

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsCoreService#lock(java.lang.String)
     */
    public void lock(String uri) throws CmsRpcException {

        try {
            getCmsObject().lockResource(uri);
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new CmsRpcException(e.getLocalizedMessage());
        }
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsCoreService#unlock(java.lang.String)
     */
    public void unlock(String uri) throws CmsRpcException {

        try {
            getCmsObject().unlockResource(uri);
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new CmsRpcException(e.getLocalizedMessage());
        }
    }
}
