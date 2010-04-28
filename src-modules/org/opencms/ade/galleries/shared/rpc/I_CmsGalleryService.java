/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/shared/rpc/Attic/I_CmsGalleryService.java,v $
 * Date   : $Date: 2010/04/28 10:25:47 $
 * Version: $Revision: 1.4 $
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

package org.opencms.ade.galleries.shared.rpc;

import org.opencms.ade.galleries.shared.CmsGalleryInfoBean;
import org.opencms.ade.galleries.shared.CmsGallerySearchObject;
import org.opencms.gwt.CmsRpcException;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * Handles all RPC services related to the gallery dialog.<p>
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.4 $ 
 * 
 * @since 8.0.0
 * 
 * @see org.opencms.ade.galleries.CmsGalleryService
 * @see org.opencms.ade.galleries.shared.rpc.I_CmsGalleryService
 * @see org.opencms.ade.galleries.shared.rpc.I_CmsGalleryServiceAsync
 */
@RemoteServiceRelativePath("org.opencms.ade.galleries.CmsGalleryService.gwt")
public interface I_CmsGalleryService extends RemoteService {

    /**
     * Returns the gallery info bean containing the content of the configured criteria tabs.<p> 
     * 
     * @param tabs the configuration of the tabs
     * 
     * @return the gallery info bean with the tabs' content
     * 
     * @throws CmsRpcException if something goes wrong
     */
    CmsGalleryInfoBean getCriteriaLists(ArrayList<String> tabs) throws CmsRpcException;

    /**
     * Returns the search results for initial search parameter.<p>
     * 
     * As search results content of selected galleries or categories or a specified resource can be returned.
     * 
     * @param searchObj the initial search object
     * 
     * @return the gallery info bean including search results 
     * 
     * @throws CmsRpcException if something goes wrong
     */
    CmsGalleryInfoBean getInitialSearch(CmsGallerySearchObject searchObj) throws CmsRpcException;

    /**
     * Returns the initial setting for the gallery.<p>
     * 
     * This includes the content of the criteria tabs and if provided the search results for the search object.
     *  
     * @param tabs the configuration of the tabs
     * @param searchObj the initial search object
     * @param dialogMode the dialog mode of this gallery dialog
     * 
     * @return the gallery info bean including the tabs' content and search results 
     * @throws CmsRpcException if something goes wrong
     */
    CmsGalleryInfoBean getInitialSettings(ArrayList<String> tabs, CmsGallerySearchObject searchObj, String dialogMode)
    throws CmsRpcException;

    /**
     * Returns the gallery search object containing search results and the currant search parameter.<p>  
     * 
     * @param searchObj the current search object
     * @return the search object containing search results
     * @throws CmsRpcException is something goes wrong
     */
    CmsGallerySearchObject getSearch(CmsGallerySearchObject searchObj) throws CmsRpcException;

}
