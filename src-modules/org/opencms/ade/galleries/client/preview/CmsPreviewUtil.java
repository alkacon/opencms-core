/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/preview/Attic/CmsPreviewUtil.java,v $
 * Date   : $Date: 2010/05/27 10:28:29 $
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

/**
 * Utility class for resource preview.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public final class CmsPreviewUtil {

    /**
     * Constructor.<p>
     */
    private CmsPreviewUtil() {

        // hiding the constructor
    }

    /**
     * Exports the functions of {@link org.opencms.ade.galleries.client.preview.I_CmsResourcePreview}
     * to the window object for use via JSNI.<p> 
     * 
     * @param previewName the name of the preview
     * @param preview the preview
     */
    public static native void exportFunctions(String previewName, I_CmsResourcePreview preview) /*-{
        var listKey=@org.opencms.ade.galleries.client.preview.I_CmsResourcePreview::KEY_PREVIEW_PROVIDER_LIST;
        if (!$wnd[listKey]){
        $wnd[listKey]={};
        }
        $wnd[listKey][previewName]={};
        $wnd[listKey][previewName][@org.opencms.ade.galleries.client.preview.I_CmsResourcePreview::KEY_OPEN_PREVIEW_FUNCTION]=function(mode, path, parentElementId){
        preview.@org.opencms.ade.galleries.client.preview.I_CmsResourcePreview::openPreview(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(mode, path, parentElementId);
        };
        $wnd[listKey][previewName][@org.opencms.ade.galleries.client.preview.I_CmsResourcePreview::KEY_SELECT_RESOURCE_FUNCTION]=function(mode, path){
        preview.@org.opencms.ade.galleries.client.preview.I_CmsResourcePreview::selectResource(Ljava/lang/String;Ljava/lang/String;)(mode, path);
        };
    }-*/;
}
