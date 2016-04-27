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

package org.opencms.gwt.client.ui;

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.util.CmsEmbeddedDialogHandler;
import org.opencms.gwt.shared.CmsCoreData.UserInfo;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.util.CmsUUID;

import java.util.Collections;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;

/**
 * The user info toolbar button.<p>
 */
public class CmsUserInfo extends CmsMenuButton {

    /** The user info HTML. */
    HTML m_infoHtml;

    /**
     * Constructor.<p>
     */
    public CmsUserInfo() {
        super();
        getPopup().addStyleName(I_CmsLayoutBundle.INSTANCE.dialogCss().contextMenu());
        getPopup().setWidth(0);

        addStyleName(I_CmsLayoutBundle.INSTANCE.toolbarCss().userInfo());
        m_button.getUpFace().setHTML("<img src=\"" + CmsCoreProvider.get().getUserInfo().getUserIcon() + "\" />");
        setToolbarMode(true);

        FlowPanel panel = new FlowPanel();
        panel.setStyleName(I_CmsLayoutBundle.INSTANCE.toolbarCss().userInfoDialog());
        m_infoHtml = new HTML(CmsCoreProvider.get().getUserInfo().getInfoHtml());
        panel.add(m_infoHtml);
        FlowPanel buttonBar = new FlowPanel();
        buttonBar.setStyleName(I_CmsLayoutBundle.INSTANCE.toolbarCss().userInfoButtons());
        if (!CmsCoreProvider.get().getUserInfo().isManaged()) {
            CmsPushButton editUser = new CmsPushButton();
            editUser.setText(Messages.get().key(Messages.GUI_EDIT_USER_0));
            buttonBar.add(editUser);
            editUser.addClickHandler(new ClickHandler() {

                public void onClick(ClickEvent event) {

                    new CmsEmbeddedDialogHandler().openDialog(
                        "org.opencms.ui.actions.CmsEditUserDialogAction",
                        CmsGwtConstants.CONTEXT_TYPE_APP_TOOLBAR,
                        Collections.<CmsUUID> emptyList());
                    closeMenu();
                }
            });
        }
        CmsPushButton logout = new CmsPushButton();
        logout.setText(Messages.get().key(Messages.GUI_LOGOUT_0));
        buttonBar.add(logout);
        logout.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                String logoutTarget = CmsCoreProvider.get().link(CmsCoreProvider.get().getLoginURL()) + "?logout=true";
                Window.Location.replace(logoutTarget);
            }
        });
        panel.add(buttonBar);
        setMenuWidget(panel);
        addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                toggleUserInfo();
            }
        });
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsMenuButton#openMenu()
     */
    @Override
    public void openMenu() {

        CmsRpcAction<UserInfo> action = new CmsRpcAction<UserInfo>() {

            @Override
            public void execute() {

                CmsCoreProvider.getService().getUserInfo(this);
            }

            @Override
            protected void onResponse(UserInfo result) {

                m_infoHtml.setHTML(result.getInfoHtml());
            }
        };
        action.execute();
        super.openMenu();
    }

    /**
     * Toggles the user info visibility.<p>
     */
    void toggleUserInfo() {

        if (isOpen()) {
            closeMenu();

        } else {
            openMenu();
        }
    }
}
