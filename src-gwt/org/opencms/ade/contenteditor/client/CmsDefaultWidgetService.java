/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.contenteditor.client;

import com.alkacon.acacia.client.WidgetService;
import com.alkacon.acacia.client.widgets.FormWidgetWrapper;
import com.alkacon.acacia.client.widgets.I_EditWidget;
import com.alkacon.acacia.client.widgets.TinyMCEWidget;

import org.opencms.ade.contenteditor.client.widgets.CmsFileWidget;
import org.opencms.ade.contenteditor.client.widgets.CmsGalleryWidget;
import org.opencms.ade.contenteditor.client.widgets.CmsImageGalleryWidget;
import org.opencms.ade.contenteditor.client.widgets.CmsTextareaWidget;
import org.opencms.ade.contenteditor.client.widgets.CmsTextboxWidget;
import org.opencms.util.CmsStringUtil;

/**
 * Default OpenCms widget service implementation.<p>
 */
public class CmsDefaultWidgetService extends WidgetService {

    /**
     * @see com.alkacon.acacia.client.WidgetService#shouldRemoveLastValueAfterUnfocus(com.alkacon.acacia.client.widgets.I_EditWidget)
     */
    @Override
    public boolean shouldRemoveLastValueAfterUnfocus(I_EditWidget widget) {

        if (widget instanceof FormWidgetWrapper) {
            widget = ((FormWidgetWrapper)widget).getEditWidget();
        }
        if ((widget instanceof CmsTextareaWidget)
            || (widget instanceof CmsTextboxWidget)
            || (widget instanceof TinyMCEWidget)) {
            String value = widget.getValue();
            if (" ".equals(value)) {
                return false;
            }
            return CmsStringUtil.isEmptyOrWhitespaceOnly(value);
        } else if ((widget instanceof CmsFileWidget)
            || (widget instanceof CmsImageGalleryWidget)
            || (widget instanceof CmsGalleryWidget)) {
            return CmsStringUtil.isEmptyOrWhitespaceOnly(widget.getValue());
        }
        return false;
    }

}
