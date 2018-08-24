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

package org.opencms.ui.apps.user;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsCssIcon;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsResourceInfo;
import org.opencms.ui.components.OpenCmsTheme;

import java.util.Collections;

import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Window;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * Dialog to create new element. (User, Group or OU).
 */
public class CmsNewElementDialog extends CmsBasicDialog {

    /**ID for user.*/
    private static String ID_USER = "user";

    /**ID for group. */
    private static String ID_GROUP = "group";

    /**ID for OU. */
    private static String ID_OU = "ou";

    /**vaadin serial id.*/
    private static final long serialVersionUID = 2351253053915340926L;

    /**vaadin component.*/
    private VerticalLayout m_container;

    /**vaadin component.*/
    private Button m_cancelButton;

    /**window. */
    private Window m_window;

    /**The cms object. */
    private CmsObject m_cms;

    /**vaadin component. */
    private Label m_ouLabel;

    /**The ou. */
    private String m_ou;

    /**Accounts app. */
    private CmsAccountsApp m_app;

    /**
     * public constructor.<p>
     * @param cms CmsObject
     * @param ou ou
     *
     * @param window window holding the dialog
     */
    public CmsNewElementDialog(CmsObject cms, String ou, final Window window, CmsAccountsApp app) {

        m_app = app;
        m_window = window;
        m_cms = cms;
        m_ou = ou;
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);

        try {
            displayResourceInfoDirectly(
                Collections.singletonList(
                    CmsAccountsApp.getOUInfo(
                        OpenCms.getOrgUnitManager().readOrganizationalUnit(A_CmsUI.getCmsObject(), ou))));
            m_ouLabel.setValue(
                OpenCms.getOrgUnitManager().readOrganizationalUnit(m_cms, ou).getDisplayName(
                    m_cms.getRequestContext().getLocale()));
        } catch (CmsException e) {
            //
        }
        m_cancelButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = -7494631798452339165L;

            public void buttonClick(ClickEvent event) {

                window.close();

            }
        });
        CmsResourceInfo newUser = new CmsResourceInfo(
            CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_ADD_USER_0),
            CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_ADD_USER_HELP_0),
            new CmsCssIcon(OpenCmsTheme.ICON_USER));
        newUser.setData(ID_USER);
        CmsResourceInfo newGroup = new CmsResourceInfo(
            CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_ADD_GROUP_0),
            CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_ADD_GROUP_HELP_0),
            new CmsCssIcon(OpenCmsTheme.ICON_GROUP));
        newGroup.setData(ID_GROUP);
        if (OpenCms.getRoleManager().hasRole(m_cms, CmsRole.ADMINISTRATOR.forOrgUnit(ou))) {
            CmsResourceInfo newOU = new CmsResourceInfo(
                CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_ADD_OU_0),
                CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_ADD_OU_HELP_0),
                new CmsCssIcon(OpenCmsTheme.ICON_OU));
            newOU.setData(ID_OU);
            m_container.addComponent(newOU);
        }
        m_container.addComponent(newUser);
        m_container.addComponent(newGroup);

        m_container.addLayoutClickListener(new LayoutClickListener() {

            private static final long serialVersionUID = 5189868437695349511L;

            public void layoutClick(LayoutClickEvent event) {

                AbstractComponent component = (AbstractComponent)event.getChildComponent();
                if (component != null) {
                    if (component.getData() instanceof String) {
                        openNewDialog((String)component.getData());
                    }
                }
            }
        });
    }

    /**
     * Opens the dialog.<p>
     *
     * @param id of selected item
     */
    protected void openNewDialog(String id) {

        CmsBasicDialog dialog = null;
        String caption = "";

        if (id.equals(ID_GROUP)) {
            dialog = new CmsGroupEditDialog(m_cms, m_window, m_ou, m_app);
            caption = CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_ADD_GROUP_0);
        }
        if (id.equals(ID_OU)) {
            dialog = new CmsOUEditDialog(m_cms, m_window, m_ou, m_app);
            caption = CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_ADD_OU_0);

        }
        if (id.equals(ID_USER)) {
            dialog = new CmsUserEditDialog(m_cms, m_window, m_ou, m_app);
            caption = CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_ADD_USER_0);
        }

        if (dialog != null) {
            m_window.setContent(dialog);
            m_window.setCaption(caption);
            m_window.center();
        }
    }
}
