/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/Attic/CmsGalleryFactory.java,v $
 * Date   : $Date: 2010/09/08 08:21:20 $
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

package org.opencms.ade.galleries.client;

import org.opencms.ade.galleries.client.ui.CmsGalleryDialog;
import org.opencms.gwt.client.draganddrop.I_CmsDragHandler;
import org.opencms.gwt.client.ui.dnd.CmsDnDManager;

/**
 * Factory class to create gallery dialog with or without paramter.<p>
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.4 $
 * 
 * @since 8.0.
 */
public final class CmsGalleryFactory {

    /**
     * Prevent instantiation.<p>
     */
    private CmsGalleryFactory() {

        // empty
    }

    /**
     * Returns a gallery dialog object.<p>
     * 
     * @return gallery dialog
     */
    public static CmsGalleryDialog createDialog() {

        CmsGalleryDialog galleryDialog = new CmsGalleryDialog();
        new CmsGalleryController(new CmsGalleryControllerHandler(galleryDialog));
        return galleryDialog;
    }

    /**
     * Returns a gallery dialog object with dnd manager.<p>
     * 
     * @param dndManager the reference to the dnd manager
     * @return gallery dialog
     */
    public static CmsGalleryDialog createDialog(CmsDnDManager dndManager) {

        CmsGalleryDialog galleryDialog = new CmsGalleryDialog(dndManager);
        new CmsGalleryController(new CmsGalleryControllerHandler(galleryDialog));
        return galleryDialog;
    }

    /**
     * Returns a gallery dialog object with drag handler.<p>
     * 
     * @param handler the reference to the drag handler
     * @return gallery dialog
     */
    public static CmsGalleryDialog createDialog(I_CmsDragHandler<?, ?> handler) {

        CmsGalleryDialog galleryDialog = new CmsGalleryDialog(handler);
        new CmsGalleryController(new CmsGalleryControllerHandler(galleryDialog));
        return galleryDialog;
    }
}