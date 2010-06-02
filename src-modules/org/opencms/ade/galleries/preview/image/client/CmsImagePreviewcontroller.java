/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/preview/image/client/Attic/CmsImagePreviewcontroller.java,v $
 * Date   : $Date: 2010/06/02 14:46:36 $
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

package org.opencms.ade.galleries.preview.image.client;

import org.opencms.ade.galleries.client.preview.CmsPreviewUtil;
import org.opencms.ade.galleries.client.preview.I_CmsPreviewController;
import org.opencms.ade.galleries.shared.CmsPreviewInfoBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryMode;
import org.opencms.gwt.client.CmsCoreProvider;

/**
 * Image preview dialog controller.<p>
 * 
 * This class handles the communication between preview dialog and the server.  
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 8.0.0
 */
public class CmsImagePreviewcontroller implements I_CmsPreviewController {

    /** The image preview dialog. */
    protected CmsImagePreviewControllerHandler m_handler;

    /** The field id of the input field in the xmlcontent. */
    private String m_fieldId;

    /** The info bean of the preview dialog. */
    private CmsPreviewInfoBean m_infoBean;

    /**
     * Constructor.<p>
     * 
     * @param handler the controller handler 
     */
    public CmsImagePreviewcontroller(CmsImagePreviewControllerHandler handler) {

        m_handler = handler;
    }

    /**
     * Saves the changed properties.<p>
     */
    public void saveProperties() {

        // TODO: rps call to save properties
        m_handler.onSaveProperties();
    }

    /**
     * Selects the resource.<p>
     * 
     * @param dialogMode the gallery mode 
     */
    public void select(GalleryMode dialogMode) {

        switch (dialogMode) {
            case widget:
                // write the link to the selected resource back to the fck editor
                String linkPath = CmsCoreProvider.get().link(m_infoBean.getResourcePath());
                CmsPreviewUtil.setResourcePath(m_fieldId, linkPath);
                break;
            case sitemap:
            case ade:
            case view:
            default:
                break;

        }
        m_handler.onSelect(dialogMode);
    }
}