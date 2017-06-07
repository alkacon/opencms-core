/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Data view widget which fetches a preview image for the selected item from the server.<p>
 */
public class CmsDataViewPreviewWidget extends Composite {

    /**
     * Loads image from data source.<p>
     */
    public static class ContentImageLoader implements I_ImageProvider {

        /**
         * @see org.opencms.acacia.client.widgets.complex.CmsDataViewPreviewWidget.I_ImageProvider#loadImage(java.lang.String, java.lang.String, com.google.gwt.user.client.rpc.AsyncCallback)
         */
        public void loadImage(String config, String id, AsyncCallback<String> callback) {

            CmsCoreProvider.getVfsService().getDataViewThumbnail(config, id, callback);
        }
    }

    /**
     * Interface to load the thumbnail (potentially asynchronously).
     */
    public static interface I_ImageProvider {

        /**
         * Loads the thumbnail.<p>
         *
         * @param config the widget configuration
         * @param id the data id
         * @param callback the callback to call with the result (the image URL)
         */
        void loadImage(String config, String id, AsyncCallback<String> callback);
    }

    /**
     * Loads image from a fixed URL.<p>
     */
    public static class SimpleImageLoader implements I_ImageProvider {

        /** The thumbnail url. */
        private String m_url;

        /**
         * Creates a new URL.<p>
         *
         * @param url the URL of the image to load
         */
        public SimpleImageLoader(String url) {
            m_url = url;
        }

        /**
         * @see org.opencms.acacia.client.widgets.complex.CmsDataViewPreviewWidget.I_ImageProvider#loadImage(java.lang.String, java.lang.String, com.google.gwt.user.client.rpc.AsyncCallback)
         */
        public void loadImage(String config, String id, AsyncCallback<String> callback) {

            callback.onSuccess(m_url);
        }

    }

    /** The ui-binder interface for this widget. */
    interface I_CmsPreviewUiBinder extends UiBinder<Widget, CmsDataViewPreviewWidget> {
        // GWT interface, nothing to do here
    }

    /** The widget configuration. */
    private String m_config;

    /** The object used to access the editor value. */
    private CmsDataViewValueAccessor m_accessor;

    /** The container for the image. */
    @UiField
    protected FlowPanel m_imageContainer;

    /** The preview image. */
    @UiField
    protected Image m_image;

    /** The label to the right of the preview image. */
    @UiField
    protected Label m_label;

    /** The label with the description text. */
    @UiField
    protected HTML m_descriptionLabel;

    /** The image provider. */
    private I_ImageProvider m_imageProvider;

    /**
     * Creates a new instance.<p>
     *
     * @param config the widget configuration
     * @param accessor the accessor for the editor values
     * @param provider the image provider
     */
    public CmsDataViewPreviewWidget(String config, CmsDataViewValueAccessor accessor, I_ImageProvider provider) {
        I_CmsPreviewUiBinder binder = GWT.create(I_CmsPreviewUiBinder.class);
        initWidget(binder.createAndBindUi(this));
        m_config = config;
        m_accessor = accessor;
        m_label.setText(m_accessor.getValue().getTitle());
        m_imageProvider = provider;
        if (provider == null) {
            m_imageContainer.setVisible(false);
        }
        m_descriptionLabel.setHTML(m_accessor.getValue().getDescription());
    }

    /**
     * @see com.google.gwt.user.client.ui.Widget#onLoad()
     */
    @Override
    protected void onLoad() {

        super.onLoad();
        if (m_imageProvider != null) {
            m_imageProvider.loadImage(m_config, m_accessor.getValue().getId(), new AsyncCallback<String>() {

                public void onFailure(Throwable caught) {
                    // do nothing

                }

                public void onSuccess(String imageUrl) {

                    if (imageUrl == null) {
                        imageUrl = "";
                    }
                    m_image.setUrl(imageUrl);
                }
            });
        }
    }

}
