/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ade.postupload.client.ui;

import org.opencms.ade.postupload.client.Messages;
import org.opencms.gwt.client.CmsJsFunctions;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

import jsinterop.base.Js;

/**
 * Widget to display an image preview in the upload property dialog.
 */
public class CmsImagePreview extends Composite {

    /**
     * The UI binder interface.<p>
     */
    interface I_UiBinder extends UiBinder<FlowPanel, CmsImagePreview> {
        // nothing to do
    }

    private static I_UiBinder uiBinder = GWT.create(I_UiBinder.class);

    /** The preview image. */
    @UiField
    protected Image m_image;

    /** The first info label. */
    @UiField
    protected Label m_info1;

    /** The second info label. */
    @UiField
    protected Label m_info2;

    @UiField
    protected Label m_label;

    /** Link which opens the preview in a separate window. */
    @UiField
    protected CmsPreviewLink m_link;

    /**
     * Creates a new instance.
     */
    public CmsImagePreview() {

        initWidget(uiBinder.createAndBindUi(this));
        m_label.setText(Messages.get().key(Messages.GUI_DIALOG_PREVIEW_LABEL_0));
        m_image.addErrorHandler(event -> CmsJsFunctions.INSTANCE.handleBrokenImage(Js.cast(m_image.getElement())));
    }

    /**
     * Hides the widget using the CSS 'visibility' property (so it will still occupy space).
     */
    public void hide() {

        getElement().getStyle().setVisibility(Visibility.HIDDEN);
    }

    /**
     * Sets the full preview URL.
     *
     * @param fullPreviewUrl the full preview URL
     */
    public void setFullPreview(String fullPreviewUrl) {

        m_link.setHref(fullPreviewUrl);
    }

    /**
     * Sets the high resolution preview URL.
     *
     * @param highResPreviewLink the high resolution preview URL
     */
    public void setHighResImageUrl(String highResPreviewLink) {

        if (highResPreviewLink != null) {
            m_image.getElement().setAttribute("srcset", highResPreviewLink + " 2x");
        } else {
            m_image.getElement().removeAttribute("srcset");
        }

    }

    /**
     * Sets the preview image URL.
     *
     * @param imageUrl the preview image URL
     */
    public void setImageUrl(String imageUrl) {

        if (imageUrl == null) {
            imageUrl = "";
        }
        m_image.setUrl(imageUrl);
    }

    /**
     * Sets the first info label text.
     *
     * @param text the text
     */
    public void setInfo1(String text) {

        m_info1.setText(text);
    }

    /**
     * Sets the second info label text.
     *
     * @param text the text
     */
    public void setInfo2(String text) {

        m_info2.setText(text);
    }

}
