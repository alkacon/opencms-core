/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/preview/image/client/Attic/CmsResourcePreview.java,v $
 * Date   : $Date: 2010/06/02 14:46:36 $
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

package org.opencms.ade.galleries.preview.image.client;

import org.opencms.ade.galleries.client.preview.A_CmsResourcePreview;
import org.opencms.ade.galleries.preview.image.shared.I_CmsImagePreviewProvider;
import org.opencms.ade.galleries.shared.CmsPreviewInfoBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryMode;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gwt.user.client.ui.RootPanel;

/**
 * The image resource preview.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public class CmsResourcePreview extends A_CmsResourcePreview {

    /**
     * Debug function.<p>
     * 
     * @param text text to display
     */
    //TODO: remove
    private static native void alert(String text) /*-{
        $wnd.alert(text);
    }-*/;

    /**
     * @see org.opencms.ade.galleries.client.preview.I_CmsResourcePreview#getPreviewName()
     */
    public String getPreviewName() {

        return I_CmsImagePreviewProvider.PREVIEW_NAME;
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.I_CmsResourcePreview#openPreview(String, String, String)
     */
    public void openPreview(String galleryMode, String resourcePath, String parentElementId) {

        RootPanel parentPanel = RootPanel.get(parentElementId);

        // inserting the preview into the DOM
        GalleryMode mode = I_CmsGalleryProviderConstants.GalleryMode.valueOf(galleryMode);

        CmsImagePreview preview = new CmsImagePreview(mode, parentPanel.getOffsetHeight(), parentPanel.getOffsetWidth());

        // initialize the controller and controller handler
        CmsImagePreviewcontroller controller = new CmsImagePreviewcontroller(new CmsImagePreviewControllerHandler(
            preview));

        // TODO: remove dummy data
        CmsPreviewInfoBean dummyBean = new CmsPreviewInfoBean();
        dummyBean.setPreviewHtml(resourcePath);
        Map<String, String> dummyProps = new LinkedHashMap<String, String>();
        dummyProps.put("Title", "Mein Title");
        dummyProps.put("Description", "Mein Title");
        dummyProps.put("Groesse", "Mein Title");
        dummyProps.put("Groesse und sehr lang und so", "Mein Title");
        dummyProps.put("Groesse", "Mein Title und hier auch etwas länger");
        dummyProps.put("Groesse", "Mein Title");
        dummyProps.put("Groesse und alles durcheinander", "Mein Title und auch enen langen Text");
        dummyProps.put("Groesse", "Mein Title");
        dummyProps.put("Groesse", "Mein Title");
        dummyProps.put("Groesse", "Mein Title udn am Ende auch");
        dummyBean.setPropeties(dummyProps);

        Map<String, String> dummyInfos = new LinkedHashMap<String, String>();
        dummyInfos.put(I_CmsGalleryProviderConstants.ImageParams.title.name(), "");
        dummyInfos.put(I_CmsGalleryProviderConstants.ImageParams.file_name.name(), "");
        dummyInfos.put(I_CmsGalleryProviderConstants.ImageParams.file_size.name(), "");
        dummyInfos.put(I_CmsGalleryProviderConstants.ImageParams.file_type.name(), "Mein Title");
        dummyInfos.put(I_CmsGalleryProviderConstants.ImageParams.height.name(), "");
        dummyInfos.put(I_CmsGalleryProviderConstants.ImageParams.width.name(), "");
        dummyInfos.put(I_CmsGalleryProviderConstants.ImageParams.path.name(), "");
        dummyBean.setImageInfos(dummyInfos);

        // fill the content of the preview
        preview.fillPreviewPanel(
            parentPanel.getOffsetHeight(),
            parentPanel.getOffsetWidth(),
            dummyBean.getPreviewHtml());
        preview.fillTabs(parentPanel.getOffsetHeight(), parentPanel.getOffsetWidth(), dummyBean, controller);

        parentPanel.add(preview);
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.I_CmsResourcePreview#selectResource(java.lang.String, java.lang.String)
     */
    public void selectResource(String galleryMode, String resourcePath) {

        alert("select resource " + resourcePath);
    }
}