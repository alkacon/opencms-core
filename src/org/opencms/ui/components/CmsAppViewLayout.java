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

package org.opencms.ui.components;

import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.I_CmsUpdateListener;
import org.opencms.ui.apps.CmsAppWorkplaceUi;
import org.opencms.ui.apps.I_CmsAppUIContext;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.extensions.CmsGwtDialogExtension;

import com.vaadin.server.Page.BrowserWindowResizeEvent;
import com.vaadin.server.Page.BrowserWindowResizeListener;
import com.vaadin.server.Responsive;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.declarative.Design;

/**
 * The layout used within the app view.<p>
 */
public class CmsAppViewLayout extends CssLayout implements I_CmsAppUIContext, BrowserWindowResizeListener {

    /** The serial version id. */
    private static final long serialVersionUID = -290796815149968830L;

    /** The app area. */
    private CssLayout m_appArea;

    /** The app id. */
    private String m_appId;

    /** The info area grid. */
    private CssLayout m_infoArea;

    /** The toolbar. */
    private CmsToolBar m_toolbar;

    /**
     * Constructor.<p>
     *
     * @param appId the app id
     */
    public CmsAppViewLayout(String appId) {

        m_appId = appId;
        Design.read("CmsAppView.html", this);
        Responsive.makeResponsive(this);
        // setting the width to 100% within the java code is required by the responsive resize listeners
        setWidth("100%");
        m_toolbar.init(m_appId);
    }

    /**
     * @see org.opencms.ui.apps.I_CmsAppUIContext#addPublishButton(org.opencms.ui.I_CmsUpdateListener)
     */
    public Button addPublishButton(final I_CmsUpdateListener<String> updateListener) {

        Button publishButton = CmsToolBar.createButton(
            FontOpenCms.PUBLISH,
            CmsVaadinUtils.getMessageText(Messages.GUI_PUBLISH_BUTTON_TITLE_0));
        if (CmsAppWorkplaceUi.isOnlineProject()) {
            // disable publishing in online project
            publishButton.setEnabled(false);
            publishButton.setDescription(CmsVaadinUtils.getMessageText(Messages.GUI_TOOLBAR_NOT_AVAILABLE_ONLINE_0));
        }
        publishButton.addClickListener(new ClickListener() {

            /** Serial version id. */
            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                CmsAppWorkplaceUi.get().disableGlobalShortcuts();
                CmsGwtDialogExtension extension = new CmsGwtDialogExtension(A_CmsUI.get(), updateListener);
                extension.openPublishDialog(A_CmsUI.getCmsObject().getRequestContext().getCurrentProject());
            }
        });

        addToolbarButton(publishButton);
        return publishButton;
    }

    /**
     * @see org.opencms.ui.apps.I_CmsAppUIContext#addToolbarButton(com.vaadin.ui.Component)
     */
    public void addToolbarButton(Component button) {

        m_toolbar.addButtonLeft(button);
    }

    /**
     * @see org.opencms.ui.apps.I_CmsAppUIContext#addToolbarButtonRight(com.vaadin.ui.Component)
     */
    public void addToolbarButtonRight(Component button) {

        m_toolbar.addButtonRight(button);
    }

    /**
     * @see com.vaadin.server.Page.BrowserWindowResizeListener#browserWindowResized(com.vaadin.server.Page.BrowserWindowResizeEvent)
     */
    public void browserWindowResized(BrowserWindowResizeEvent event) {

        m_toolbar.browserWindowResized(event);
    }

    /**
     * @see org.opencms.ui.apps.I_CmsAppUIContext#clearToolbarButtons()
     */
    public void clearToolbarButtons() {

        m_toolbar.clearButtonsLeft();
    }

    /**
     * Closes the toolbar popup views.<p>
     */
    public void closePopupViews() {

        m_toolbar.closePopupViews();
    }

    /**
     * @see org.opencms.ui.apps.I_CmsAppUIContext#enableDefaultToolbarButtons(boolean)
     */
    public void enableDefaultToolbarButtons(boolean enabled) {

        m_toolbar.enableDefaultButtons(enabled);
    }

    /**
     * @see org.opencms.ui.apps.I_CmsAppUIContext#getAppId()
     */
    public String getAppId() {

        return m_appId;
    }

    /**
     * @see org.opencms.ui.apps.I_CmsAppUIContext#hideToolbar()
     */
    public void hideToolbar() {

        addStyleName(OpenCmsTheme.HIDDEN_TOOLBAR);
        m_toolbar.setVisible(false);
    }

    /**
     * @see org.opencms.ui.apps.I_CmsAppUIContext#removeToolbarButton(com.vaadin.ui.Component)
     */
    public void removeToolbarButton(Component button) {

        m_toolbar.removeButton(button);
    }

    /**
     * Sets the app content component.<p>
     *
     * @param appContent the app content
     */
    public void setAppContent(Component appContent) {

        m_appArea.removeAllComponents();
        if (appContent != null) {
            m_appArea.addComponent(appContent);
        }
    }

    /**
     * @see org.opencms.ui.apps.I_CmsAppUIContext#setAppInfo(com.vaadin.ui.Component)
     */
    public void setAppInfo(Component infoContent) {

        m_infoArea.removeAllComponents();
        m_infoArea.addComponent(infoContent);
    }

    /**
     * @see org.opencms.ui.apps.I_CmsAppUIContext#setAppTitle(java.lang.String)
     */
    public void setAppTitle(String title) {

        CmsAppWorkplaceUi.setWindowTitle(title);
        m_toolbar.setAppTitle(title);
    }

    /**
     * @see org.opencms.ui.apps.I_CmsAppUIContext#setMenuDialogContext(org.opencms.ui.I_CmsDialogContext)
     */
    public void setMenuDialogContext(I_CmsDialogContext context) {

        m_toolbar.setDialogContext(context);
    }

    /**
     * @see org.opencms.ui.apps.I_CmsAppUIContext#showInfoArea(boolean)
     */
    public void showInfoArea(boolean show) {

        m_infoArea.setVisible(show);
    }

    /**
     * @see org.opencms.ui.apps.I_CmsAppUIContext#showToolbar()
     */
    public void showToolbar() {

        removeStyleName(OpenCmsTheme.HIDDEN_TOOLBAR);
        m_toolbar.setVisible(false);
    }

    /**
     * @see org.opencms.ui.apps.I_CmsAppUIContext#updateOnChange()
     */
    public void updateOnChange() {

        m_toolbar.updateAppIndicator();
    }

    /**
     * @see org.opencms.ui.apps.I_CmsAppUIContext#updateUserInfo()
     */
    public void updateUserInfo() {

        m_toolbar.refreshUserInfoDropDown();
    }
}
