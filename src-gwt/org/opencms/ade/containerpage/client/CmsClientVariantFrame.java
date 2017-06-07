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

package org.opencms.ade.containerpage.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Container widget for the iframe in which the template variant is shown.<p>
 */
public class CmsClientVariantFrame extends Composite {

    /** UiBinder interface for this class. */
    interface I_CmsClientVariantFrameUiBinder extends UiBinder<Widget, CmsClientVariantFrame> {
        // empty, for uibinder
    }

    /** UiBinder for this class. */
    private static I_CmsClientVariantFrameUiBinder uiBinder = GWT.create(I_CmsClientVariantFrameUiBinder.class);

    /** The iframe for the template variant. */
    protected Frame m_iframe;

    /** The iframe container. */
    @UiField
    protected Panel m_iframeContainer;

    /** Placeholder widget which is displayed until the iframe is loaded. */
    @UiField
    protected Widget m_iframePlaceholder;

    /**
     * Creates a new instance.<p>
     *
     * @param url the URL for the client variant
     * @param width the width
     * @param height the height
     * @param containerpageHandler the container page handler
     */
    public CmsClientVariantFrame(
        String url,
        int width,
        int height,
        final CmsContainerpageHandler containerpageHandler) {

        initWidget(uiBinder.createAndBindUi(this));
        m_iframe = new Frame();
        m_iframe.getElement().getStyle().setVisibility(com.google.gwt.dom.client.Style.Visibility.HIDDEN);
        m_iframe.setUrl(url);
        m_iframeContainer.setWidth(width + "px");
        m_iframeContainer.setHeight(height + "px");
        m_iframe.setWidth("100%");
        m_iframe.setHeight("100%");
        m_iframe.addLoadHandler(new LoadHandler() {

            public void onLoad(LoadEvent event) {

                m_iframePlaceholder.removeFromParent();
                m_iframe.getElement().getStyle().setVisibility(com.google.gwt.dom.client.Style.Visibility.VISIBLE);
                containerpageHandler.deactivateSelection();

            }
        });
        m_iframeContainer.add(m_iframe);
    }

    /**
     * Gets the iframe container.<p>
     *
     * @return the iframe container
     */
    public Panel getIframeContainer() {

        return m_iframeContainer;
    }

}
