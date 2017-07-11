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

package org.opencms.ui.apps.sessions;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsSessionInfo;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.CmsFileExplorerConfiguration;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsUserInfo;

import java.util.Iterator;
import java.util.List;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.VerticalLayout;

/**
 * Dialog to show user information and to switch to user session.<p>
 */
public class CmsUserInfoDialog extends CmsBasicDialog {

    /**vaadin serial id.*/
    private static final long serialVersionUID = -8358238253459658269L;

    /**vaadin component.*/
    private VerticalLayout m_layout;

    /**vaadin component. */
    private Button m_okButton;

    /**vaadin component. */
    private Button m_cancelButton;

    /**CmsObject.*/
    protected CmsObject m_cms;

    /**
     * public constructor.<p>
     *
     * @param id id to session
     * @param furtherInfo lines to be shown
     * @param closeRunnable runnable called by closing window
     */
    public CmsUserInfoDialog(final String id, List<String> furtherInfo, final Runnable closeRunnable) {
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        CmsUserInfo info = new CmsUserInfo(OpenCms.getSessionManager().getSessionInfo(id).getUserId());
        Iterator<String> iterator = furtherInfo.iterator();

        info.addUserStatus(iterator.next());

        while (iterator.hasNext()) {
            info.addDetailLine(iterator.next());
        }
        m_layout.addComponent(info);

        m_cms = A_CmsUI.getCmsObject();

        m_okButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 3096577957489665752L;

            public void buttonClick(ClickEvent event) {

                try {
                    CmsSessionInfo session = OpenCms.getSessionManager().getSessionInfo(id);
                    A_CmsUI.get().changeProject(m_cms.readProject(session.getProject()));
                    A_CmsUI.get().changeSite(session.getSiteRoot());

                    String path = OpenCms.getSessionManager().switchUserFromSession(
                        m_cms,
                        CmsVaadinUtils.getRequest(),
                        m_cms.readUser(session.getUserId()),
                        session);

                    if (path == null) {
                        A_CmsUI.get().getPage().setLocation(
                            CmsVaadinUtils.getWorkplaceLink()
                                + "#!"
                                + CmsFileExplorerConfiguration.APP_ID
                                + "/"
                                + session.getProject().getStringValue()
                                + "!!"
                                + session.getSiteRoot()
                                + "!!!!");
                        A_CmsUI.get().getPage().reload();
                    }
                    A_CmsUI.get().getPage().setLocation(path);
                } catch (CmsException e) {
                    //
                }
                closeRunnable.run();
            }

        });

        m_cancelButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = -1033076596404978498L;

            public void buttonClick(ClickEvent event) {

                closeRunnable.run();

            }
        });
    }

    /**
     * Hides the switch user button.<p>
     *
     * @param hide true-> button not displayed
     */
    public void setHideSwitchButton(boolean hide) {

        m_okButton.setVisible(!hide);

    }
}
