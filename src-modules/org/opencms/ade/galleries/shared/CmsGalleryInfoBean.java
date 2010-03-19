/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/shared/Attic/CmsGalleryInfoBean.java,v $
 * Date   : $Date: 2010/03/19 10:11:54 $
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

package org.opencms.ade.galleries.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * This bean contains the gallery information required for displaying the gallery information and the search.<p>
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 8.0.0
 */
public class CmsGalleryInfoBean implements IsSerializable {

    /** The search beans contains the currents search criteria. */
    private CmsGallerySearchObject m_searchObject;

    /** The search beans contains the currents search criteria. */
    private CmsGalleryDialogBean m_dialogInfo;

    /**
     * Returns the searchObject.<p>
     *
     * @return the searchObject
     */
    public CmsGallerySearchObject getSearchObject() {

        return m_searchObject;
    }

    /**
     * Returns the dialogInfo.<p>
     *
     * @return the dialogInfo
     */
    public CmsGalleryDialogBean getDialogInfo() {

        return m_dialogInfo;
    }

    /**
     * Sets the dialogInfo.<p>
     *
     * @param dialogInfo the dialogInfo to set
     */
    public void setDialogInfo(CmsGalleryDialogBean dialogInfo) {

        m_dialogInfo = dialogInfo;
    }

    /**
     * Sets the searchObject.<p>
     *
     * @param searchObject the searchObject to set
     */
    public void setSearchObject(CmsGallerySearchObject searchObject) {

        m_searchObject = searchObject;
    }
}