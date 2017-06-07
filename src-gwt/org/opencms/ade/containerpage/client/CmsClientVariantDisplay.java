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

import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.shared.CmsClientVariantInfo;
import org.opencms.gwt.shared.CmsGwtConstants;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * Class used to display a client variant of a template context.<p>
 */
public class CmsClientVariantDisplay {

    /**
     * Popup subclass which exposes the getGlassElement method.<p>
     */
    class VariantPopup extends PopupPanel {

        /**
         * Creates a new instance.<p>
         */
        public VariantPopup() {

            super(true, true);
            addStyleName(I_CmsLayoutBundle.INSTANCE.dialogCss().popup());
        }

        /**
         * @see com.google.gwt.user.client.ui.PopupPanel#getGlassElement()
         */
        @Override
        public Element getGlassElement() {

            return super.getGlassElement();
        }
    }

    /** the container page handler. */
    CmsContainerpageHandler m_containerpageHandler;

    /** The popup. */
    private VariantPopup m_popup;

    /**
     * Creates a new instance.<p>
     *
     * @param handler the container page handler
     */
    public CmsClientVariantDisplay(CmsContainerpageHandler handler) {

        m_containerpageHandler = handler;
    }

    /**
     * Clears a currently displayed popup.<p>
     *
     */
    public void clear() {

        if (m_popup != null) {
            m_popup.hide();
        }
        m_popup = null;

    }

    /**
     * Shows the given context/variant combination.<p>
     *
     * @param context the template context
     * @param info the client variant
     */
    public void show(String context, CmsClientVariantInfo info) {

        clear();
        String url = buildClientVariantUrl(context, info);
        CmsClientVariantFrame container = new CmsClientVariantFrame(
            url,
            info.getScreenWidth(),
            info.getScreenHeight(),
            m_containerpageHandler);
        VariantPopup popup = new VariantPopup();
        popup.setGlassEnabled(true);
        popup.add(container);
        m_popup = popup;
        m_popup.getGlassElement().addClassName(I_CmsLayoutBundle.INSTANCE.dialogCss().popupOverlay());
        m_popup.getGlassElement().getStyle().setBackgroundColor("white");
        m_popup.getGlassElement().getStyle().setOpacity(0.8);
        m_popup.getElement().getStyle().setZIndex(200000);
        popup.center();

        popup.addCloseHandler(new CloseHandler<PopupPanel>() {

            public void onClose(CloseEvent<PopupPanel> event) {

                m_containerpageHandler.activateSelection();
            }
        });
    }

    /**
     * Builds the URL for the client variant.<p>
     *
     * @param context the template context name
     * @param info the client variant info
     *
     * @return the URL for the variant
     */
    private String buildClientVariantUrl(String context, CmsClientVariantInfo info) {

        String currentUrl = Window.Location.getHref();
        // remove fragment
        currentUrl = currentUrl.replaceFirst("#.*$", "");
        String connector = "?";
        if (currentUrl.indexOf('?') >= 0) {
            connector = "&";
        }
        String targetUrl = currentUrl
            + connector
            + CmsGwtConstants.PARAM_DISABLE_DIRECT_EDIT
            + "=true"
            + "&"
            + CmsGwtConstants.PARAM_TEMPLATE_CONTEXT
            + "="
            + context;
        return targetUrl;
    }
}
