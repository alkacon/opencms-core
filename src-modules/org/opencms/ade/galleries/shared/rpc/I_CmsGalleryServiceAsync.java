/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/shared/rpc/Attic/I_CmsGalleryServiceAsync.java,v $
 * Date   : $Date: 2010/03/30 14:08:37 $
 * Version: $Revision: 1.2 $
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

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Handles all RPC services related to the gallery dialog.<p>
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.2 $ 
 * 
 * @since 8.0.0
 * 
 * @see org.opencms.ade.galleries.CmsGalleryService
 * @see org.opencms.ade.galleries.shared.rpc.I_CmsGalleryService
 * @see org.opencms.ade.galleries.shared.rpc.I_CmsGalleryServiceAsync
 */
public interface I_CmsGalleryServiceAsync {

    /**
     * Returns the gallery info bean containing the content of the configured criteria tabs.<p> 
     * 
     * @param tabs the configuration of the tabs
     * @param callback the async callback
     */
    void getCriteriaLists(ArrayList<String> tabs, AsyncCallback<CmsGalleryInfoBean> callback);

    /**
     * Returns the search results for initial search parameter.<p>
     * 
     * As search results content of selected galleries or categories or a specified resource can be returned.
     * 
     * @param searchObj the initial search bean
     * @param callback the async callback
     */
    void getInitialSearch(CmsGallerySearchObject searchObj, AsyncCallback<CmsGalleryInfoBean> callback);

    /**
     * Returns the search results for initial search parameter.<p>
     * 
     * As search results content of selected galleries or categories or a specified resource can be returned.
     * 
     * @param tabs the configuration of the tabs
     * @param searchObj the initial search object 
     * @param dialogMode the dialog mode of this gallery dialog   
     * @param callback the async callback
     */
    void getInitialSettings(
        ArrayList<String> tabs,
        CmsGallerySearchObject searchObj,
        String dialogMode,
        AsyncCallback<CmsGalleryInfoBean> callback);

    /**
     * Returns the gallery search object containing search results and the currant search parameter.<p>  
     * 
     * @param searchObj the current search object
     * @param callback the async callback
     */
    void getSearch(CmsGallerySearchObject searchObj, AsyncCallback<CmsGalleryInfoBean> callback);
}
