/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/shared/Attic/CmsGalleryInfoBean.java,v $
 * Date   : $Date: 2010/03/30 14:08:36 $
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

package org.opencms.ade.galleries.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * The general bean holding the gallery information required for displaying the gallery information and the search.<p>
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.2 $ 
 * 
 * @since 8.0.0
 */
public class CmsGalleryInfoBean implements IsSerializable {

    /** The search beans contains the currents search criteria. */
    private CmsGalleryDialogBean m_dialogInfo;

    /** The dialogmode of the current gallery (view, widget, editor, properties, ade).*/
    private String m_dialogMode;

    /** The search beans contains the currents search criteria. */
    private CmsGallerySearchObject m_searchObject;

    /**
     * Returns the dialog info.<p>
     *
     * @return the dialog info
     */
    public CmsGalleryDialogBean getDialogInfo() {

        return m_dialogInfo;
    }

    /**
     * Returns the dialog mode.<p>
     *
     * @return the dialog mode
     */
    public String getDialogMode() {

        if (m_dialogMode == null) {
            return "";
        }
        return m_dialogMode;
    }

    /**
     * Returns the search object.<p>
     *
     * @return the search object
     */
    public CmsGallerySearchObject getSearchObject() {

        return m_searchObject;
    }

    /**
     * Sets the dialog info.<p>
     *
     * @param dialogInfo the dialog infos to set
     */
    public void setDialogInfo(CmsGalleryDialogBean dialogInfo) {

        m_dialogInfo = dialogInfo;
    }

    /**
     * Sets the dialog mode.<p>
     *
     * @param dialogMode the dialog mode to set
     */
    public void setDialogMode(String dialogMode) {

        m_dialogMode = dialogMode;
    }

    /**
     * Sets the search object.<p>
     *
     * @param searchObject the search object to set
     */
    public void setSearchObject(CmsGallerySearchObject searchObject) {

        m_searchObject = searchObject;
    }
}