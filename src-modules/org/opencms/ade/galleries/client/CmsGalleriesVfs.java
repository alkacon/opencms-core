/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/Attic/CmsGalleriesVfs.java,v $
 * Date   : $Date: 2010/04/21 15:43:31 $
 * Version: $Revision: 1.6 $
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
import org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.A_CmsEntryPoint;
import org.opencms.gwt.client.ui.CmsFlowPanel;
import org.opencms.gwt.client.util.CmsDomUtil;

import com.google.gwt.user.client.ui.RootPanel;

/**
 * Gallery Dialog entry class to be open from the vfs tree.<p>
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.6 $ 
 * 
 * @since 8.0.0
 */
public class CmsGalleriesVfs extends A_CmsEntryPoint {

    /**
     * Ensures all style sheets are loaded.<p>
     */
    public static void initCss() {

        I_CmsLayoutBundle.INSTANCE.galleryDialogCss().ensureInjected();
        I_CmsLayoutBundle.INSTANCE.listTreeCss().ensureInjected();
        org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle.INSTANCE.galleryDialogCss().ensureInjected();
    }

    /**
     * @see org.opencms.gwt.client.A_CmsEntryPoint#onModuleLoad()
     */
    @Override
    public void onModuleLoad() {

        super.onModuleLoad();
        initCss();
        final CmsFlowPanel html = new CmsFlowPanel(CmsDomUtil.Tag.div.name());
        html.addStyleName(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().galleryDialogSize());
        RootPanel.getBodyElement().addClassName(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().galleriesDialog());
        RootPanel.get().add(html);

        // add a gallery dialog to html
        CmsGalleryDialog galleryDialog = new CmsGalleryDialog();
        html.add(galleryDialog);
        galleryDialog.init();
    }
}
