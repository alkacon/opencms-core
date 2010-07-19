/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/preview/Attic/A_CmsPreviewController.java,v $
 * Date   : $Date: 2010/07/19 07:45:28 $
 * Version: $Revision: 1.3 $
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

package org.opencms.ade.galleries.client.preview;

import org.opencms.ade.galleries.shared.CmsResourceInfoBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryMode;

/**
 * Preview dialog controller.<p>
 * 
 * This class handles the communication between preview dialog and the server.  
 * 
 * @param <T> the resource info bean type
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.3 $ 
 * 
 * @since 8.0.0
 */
public abstract class A_CmsPreviewController<T extends CmsResourceInfoBean> implements I_CmsPreviewController<T> {

    /** The info bean of the binary preview dialog. */
    protected T m_infoBean;

    /**
     * Returns the preview handler.<p>
     * 
     * @return the preview handler
     */
    public abstract I_CmsPreviewHandler<T> getHandler();

    /**
     * Selects the resource.<p>
     * 
     * @param galleryMode the gallery mode 
     * @param resourcePath the path of the selected resource
     * @param title the resource title
     */
    public static void select(GalleryMode galleryMode, String resourcePath, String title) {

        switch (galleryMode) {
            case widget:
                CmsPreviewUtil.setResourcePath(resourcePath);
                break;
            case editor:
                CmsPreviewUtil.setLink(resourcePath, title, null);
                break;
            case sitemap:
            case ade:
            case view:
            default:
                //nothing to do here, should not be called
                break;
        }
    }

    /**
     * Returns if the dialog may be closed.<p>
     * 
     * @return <code>true</code> if the dialog may be closed
     */
    public boolean closeGalleryDialog() {

        return getHandler().setDataInEditor();
    }

    /**
     * Returns the resource path of the current resource.<p>
     * 
     * @return the resource path
     */
    public String getResourcePath() {

        return m_infoBean.getResourcePath();
    }

    /**
     * Sets the current resource within the editor or xml-content.<p>
     * 
     * @param galleryMode the gallery mode
     */
    public void setResource(GalleryMode galleryMode) {

        select(galleryMode, m_infoBean.getResourcePath(), m_infoBean.getTitle());
    }

    /**
     * Calls the preview handler to display the given data.<p>
     * 
     * @param resourceInfo the resource info data
     */
    public void showData(T resourceInfo) {

        m_infoBean = resourceInfo;
        getHandler().showData(resourceInfo);

    }
}
