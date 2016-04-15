/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.acacia.client.widgets.complex;

import org.opencms.gwt.client.CmsCoreProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Data view widget which fetches a preview image for the selected item from the server.<p>
 */
public class CmsDataViewPreviewWidget extends Composite {

    /** The ui-binder interface for this widget. */
    interface I_CmsPreviewUiBinder extends UiBinder<Widget, CmsDataViewPreviewWidget> {
        // GWT interface, nothing to do here
    }

    /** The widget configuration. */
    private String m_config;

    /** The object used to access the editor value. */
    private CmsDataViewValueAccessor m_accessor;

    /** The preview image. */
    @UiField
    protected Image m_image;

    /** The label to the right of the preview image. */
    @UiField
    protected Label m_label;

    /** The label with the description text. */
    @UiField
    protected HTML m_descriptionLabel;

    /**
     * Creates a new instance.<p>
     *
     * @param config the widget configuration
     * @param accessor the accessor for the editor values
     */
    public CmsDataViewPreviewWidget(String config, CmsDataViewValueAccessor accessor) {
        I_CmsPreviewUiBinder binder = GWT.create(I_CmsPreviewUiBinder.class);
        initWidget(binder.createAndBindUi(this));
        m_config = config;
        m_accessor = accessor;
        m_label.setText(m_accessor.getValue().getTitle());
        m_descriptionLabel.setHTML(m_accessor.getValue().getDescription());
    }

    /**
     * @see com.google.gwt.user.client.ui.Widget#onLoad()
     */
    @Override
    protected void onLoad() {

        super.onLoad();
        CmsCoreProvider.getVfsService().getDataViewThumbnail(
            m_config,
            m_accessor.getValue().getId(),
            new AsyncCallback<String>() {

                public void onFailure(Throwable caught) {

                    // TODO Auto-generated method stub

                }

                public void onSuccess(String imageUrl) {

                    m_image.setUrl(imageUrl);
                }
            });
    }

}
