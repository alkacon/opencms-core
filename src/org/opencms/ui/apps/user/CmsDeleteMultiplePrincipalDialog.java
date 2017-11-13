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
import org.opencms.security.CmsPrincipal;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsResourceInfo;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;

/**
 * Dialog for delete multiple principal.<p>
 */
public class CmsDeleteMultiplePrincipalDialog extends CmsBasicDialog {

    /**vaadin serial id. */
    private static final long serialVersionUID = -1191281655158071555L;

    /**The icon. */
    private Label m_icon;

    /**Vaadin component. */
    Button m_okButton;

    /**Vaadin component. */
    CmsObject m_cms;

    /**Vaadin component. */
    Button m_cancelButton;

    /**Vaadin component. */
    FormLayout m_force;

    /**Vaadin component. */
    CheckBox m_forceCheck;

    /**The ids to delete. */
    private Set<String> m_ids;

    /**
     * Public constructor.<p>
     *
     * @param cms CmsObeject
     * @param context ids of principal to delete
     * @param window window
     */
    public CmsDeleteMultiplePrincipalDialog(CmsObject cms, Set<String> context, Window window) {
        init(cms, window);
        m_ids = context;
        try {
            List<CmsResourceInfo> infos = new ArrayList<CmsResourceInfo>();
            for (String id : context) {
                infos.add(CmsAccountsApp.getPrincipalInfo(CmsPrincipal.readPrincipal(cms, new CmsUUID(id))));
            }
            displayResourceInfoDirectly(infos);
            boolean simple = true;
            for (String id : context) {
                simple = simple & (m_cms.getResourcesForPrincipal(new CmsUUID(id), null, false).size() == 0);
            }
            m_force.setVisible(!simple);
            m_okButton.setEnabled(simple);
            m_forceCheck.addValueChangeListener(new ValueChangeListener() {

                /**vaadin serial id. */
                private static final long serialVersionUID = 3604602066297066360L;

                public void valueChange(ValueChangeEvent event) {

                    m_okButton.setEnabled(m_forceCheck.getValue().booleanValue());

                }
            });
        } catch (CmsException e) {
            //
        }
    }

    /**
     * Deletes the given user.<p>
     */
    protected void deletePrincipal() {

        try {
            for (String id : m_ids) {
                m_cms.deleteUser(new CmsUUID(id));
            }
        } catch (CmsException e) {
            //
        }
    }

    /**
     * Initialized the dialog.<p>
     *
     * @param cms CmsObject
     * @param window window
     */
    private void init(CmsObject cms, final Window window) {

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        m_icon.setContentMode(ContentMode.HTML);
        m_icon.setValue(FontOpenCms.WARNING.getHtml());
        m_cms = cms;
        m_okButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = -7845894751587879028L;

            public void buttonClick(ClickEvent event) {

                deletePrincipal();
                window.close();
                A_CmsUI.get().reload();

            }

        });

        m_cancelButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 6649262870116199591L;

            public void buttonClick(ClickEvent event) {

                window.close();

            }

        });
    }

}
