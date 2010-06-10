/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/preview/image/client/Attic/CmsImagePreviewController.java,v $
 * Date   : $Date: 2010/06/10 08:45:04 $
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

import org.opencms.ade.galleries.client.preview.A_CmsPreviewController;
import org.opencms.ade.galleries.preview.image.shared.CmsImageInfoBean;
import org.opencms.ade.galleries.shared.CmsResourceInfoBean;

import java.util.Map;

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
public class CmsImagePreviewController extends A_CmsPreviewController<CmsImageInfoBean> {

    /** The field id of the input field in the xmlcontent. */
    private String m_fieldId;

    /** The info bean of the preview dialog. */
    private CmsResourceInfoBean m_infoBean;

    /**
     * Constructor.<p>
     * 
     * @param handler the controller handler 
     */
    public CmsImagePreviewController(CmsImagePreviewHandler handler) {

        super(handler);
        handler.init(this);
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.I_CmsPreviewController#loadResourceInfo(java.lang.String)
     */
    public void loadResourceInfo(String resourcePath) {

        // TODO: Auto-generated method stub

    }

    /**
     * @see org.opencms.ade.galleries.client.preview.I_CmsPreviewController#saveProperties(java.util.Map)
     */
    public void saveProperties(Map<String, String> properties) {

        // TODO: Auto-generated method stub

    }
}