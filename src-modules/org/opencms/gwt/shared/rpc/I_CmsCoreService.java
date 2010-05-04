/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/shared/rpc/Attic/I_CmsCoreService.java,v $
 * Date   : $Date: 2010/05/04 09:40:41 $
 * Version: $Revision: 1.5 $
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

package org.opencms.gwt.shared.rpc;

import org.opencms.gwt.CmsRpcException;
import org.opencms.gwt.shared.CmsCategoryTreeEntry;
import org.opencms.gwt.shared.CmsCoreData;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * Provides general core services.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.5 $ 
 * 
 * @since 8.0.0
 * 
 * @see org.opencms.gwt.CmsCoreService
 * @see org.opencms.gwt.shared.rpc.I_CmsCoreService
 * @see org.opencms.gwt.shared.rpc.I_CmsCoreServiceAsync
 */
@RemoteServiceRelativePath("org.opencms.gwt.CmsCoreService.gwt")
public interface I_CmsCoreService extends RemoteService {

    /**
     * Returns the categories for the given search parameters.<p>
     * 
     * @param fromCatPath the category path to start with, can be <code>null</code> or empty to use the root
     * @param includeSubCats if to include all categories, or first level child categories only
     * @param refVfsPaths the reference paths, can be <code>null</code> to only use the system repository
     * 
     * @return the resource categories
     * 
     * @throws CmsRpcException if something goes wrong 
     */
    CmsCategoryTreeEntry getCategories(String fromCatPath, boolean includeSubCats, List<String> refVfsPaths)
    throws CmsRpcException;

    /**
     * Locks the given resource.<p>
     * 
     * @param uri the resource URI 
     * 
     * @return <code>null</code> if successful, an error message if not 
     * 
     * @throws CmsRpcException if something goes wrong 
     */
    String lock(String uri) throws CmsRpcException;

    /**
     * Locks the given resource with a temporary lock.<p>
     * 
     * @param uri the resource URI 
     * 
     * @return <code>null</code> if successful, an error message if not 
     * 
     * @throws CmsRpcException if something goes wrong 
     */
    String lockTemp(String uri) throws CmsRpcException;

    /**
     * Generates core data for prefetching in the host page.<p>
     * 
     * @return the core data
     * 
     * @throws CmsRpcException if something goes wrong 
     */
    CmsCoreData prefetch() throws CmsRpcException;

    /**
     * Unlocks the given resource.<p>
     * 
     * @param uri the resource URI 
     * 
     * @return <code>null</code> if successful, an error message if not 
     * 
     * @throws CmsRpcException if something goes wrong 
     */
    String unlock(String uri) throws CmsRpcException;
}
