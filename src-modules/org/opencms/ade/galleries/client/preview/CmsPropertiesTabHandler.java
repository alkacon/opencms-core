/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/preview/Attic/CmsPropertiesTabHandler.java,v $
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

package org.opencms.ade.galleries.client.preview;

import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryMode;

/**
 * The properties tab handler of the preview dialog.<p>
 * 
 * This class receives event information from the properties tab and 
 * delegates it to the preview controller.
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 8.0.0
 */
public class CmsPropertiesTabHandler {

    /** The preview controller. */
    protected I_CmsPreviewController m_controller;

    /**
     * Constructor.<p>
     * 
     * @param controller the controller
     */
    public CmsPropertiesTabHandler(I_CmsPreviewController controller) {

        m_controller = controller;
    }

    /**
     * Will be triggered when the ok button is clicked.<p>
     * 
     * @param dialogMode the mode of the gallery
     */
    public void onSelect(GalleryMode dialogMode) {

        m_controller.select(dialogMode);
    }
}