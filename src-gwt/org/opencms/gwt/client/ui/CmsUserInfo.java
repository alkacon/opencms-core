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

package org.opencms.gwt.client.ui;

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.util.CmsEmbeddedDialogHandler;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.util.CmsUUID;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;

/**
 * The user info toolbar button.<p>
 */
public class CmsUserInfo extends A_CmsToolbarButton<I_CmsToolbarHandler> {

    /** The embedded dialog handler. */
    private CmsEmbeddedDialogHandler m_dialogHandler;

    /**
     * Constructor.<p>
     */
    public CmsUserInfo() {
        super(null, null);
        addStyleName(I_CmsLayoutBundle.INSTANCE.toolbarCss().userInfo());
        getUpFace().setHTML("<img src=\"" + CmsCoreProvider.get().getUserInfo().getUserIcon() + "\" />");
        setTitle(Messages.get().key(Messages.GUI_USER_INFO_0));
        m_dialogHandler = new CmsEmbeddedDialogHandler();
        addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            @Override
            public void onClick(ClickEvent event) {

                onToolbarClick();
            }
        });
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsToolbarButton#isActive()
     */
    @Override
    public boolean isActive() {

        return m_dialogHandler.hasDialogFrame();
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsToolbarButton#onToolbarActivate()
     */
    @Override
    public void onToolbarActivate() {

        Map<String, String> params = new HashMap<String, String>();
        params.put("left", String.valueOf(getElement().getAbsoluteLeft()));
        m_dialogHandler.setOnCloseCommand(new Command() {

            public void execute() {

                onClose();
            }
        });
        m_dialogHandler.openDialog(
            "org.opencms.ui.actions.CmsUserInfoDialogAction",
            CmsGwtConstants.CONTEXT_TYPE_APP_TOOLBAR,
            Collections.<CmsUUID> emptyList(),
            params);
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsToolbarButton#onToolbarClick()
     */
    @Override
    public void onToolbarClick() {

        boolean active = isActive();

        setActive(!active);

    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsToolbarButton#onToolbarDeactivate()
     */
    @Override
    public void onToolbarDeactivate() {

        m_dialogHandler.setOnCloseCommand(null);
        m_dialogHandler.finish(null);
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsToolbarButton#setActive(boolean)
     */
    @Override
    public void setActive(boolean active) {

        if (active) {
            if (m_handler != null) {
                m_handler.deactivateCurrentButton();
                m_handler.setActiveButton(this);
            }
            onToolbarActivate();

        } else {
            onToolbarDeactivate();

            if (m_handler != null) {
                m_handler.setActiveButton(null);
                m_handler.activateSelection();
            }
        }
    }

    /**
     * Sets the button handler.<p>
     *
     * @param handler the button handler
     */
    public void setHandler(I_CmsToolbarHandler handler) {

        m_handler = handler;
    }

    /**
     * Returns the container-page handler.<p>
     *
     * @return the container-page handler
     */
    @Override
    protected I_CmsToolbarHandler getHandler() {

        return m_handler;
    }

    /**
     * Executed on window close.<p>
     */
    protected void onClose() {

        onToolbarDeactivate();
        if (m_handler != null) {
            m_handler.setActiveButton(null);
            m_handler.activateSelection();
        }
    }
}
