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

import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.util.CmsUUID;

import java.util.Collections;

import org.apache.commons.logging.Log;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Window;
import com.vaadin.v7.data.Property.ValueChangeEvent;
import com.vaadin.v7.data.Property.ValueChangeListener;
import com.vaadin.v7.ui.CheckBox;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.TextArea;
import com.vaadin.v7.ui.TextField;

/**
 * Class for the dialog to edit or create a CmsGroup.<p>
 */
public class CmsGroupEditDialog extends CmsBasicDialog {

    /** The logger for this class. */
    static Log LOG = CmsLog.getLog(CmsGroupEditDialog.class.getName());

    /**vaadin serial id.*/
    private static final long serialVersionUID = 6633733627052633351L;

    /**vaadin component.*/
    Button m_ok;

    /** The app instance. */
    private CmsAccountsApp m_app;

    /**vaadin component.*/
    private Button m_cancel;

    /**CmsObject.*/
    private CmsObject m_cms;

    /**vaadin component.*/
    private TextArea m_description;

    /**vaadin component.*/
    private CheckBox m_enabled;

    /**CmsGroup.*/
    private CmsGroup m_group;

    /** The group edit parameters. */
    private CmsGroupEditParameters m_groupEditParameters = new CmsGroupEditParameters();

    /**vaadin component.*/
    private TextField m_name;

    /**vaadin component.*/
    private Label m_ou;

    /**
     * public constructor.<p>
     *
     * @param cms CmsObject
     * @param groupId id of group edit, null if groud should be created
     * @param window window holding the dialog
     * @param app the app instance
     */
    public CmsGroupEditDialog(CmsObject cms, CmsUUID groupId, final Window window, final CmsAccountsApp app) {

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        m_cms = cms;
        m_app = app;

        try {
            if (groupId != null) {
                m_group = m_cms.readGroup(groupId);
                m_groupEditParameters = m_app.getGroupEditParameters(m_group);
                displayResourceInfoDirectly(Collections.singletonList(CmsAccountsApp.getPrincipalInfo(m_group)));
                m_ou.setValue(m_group.getOuFqn());
                m_name.setValue(m_group.getSimpleName());
                m_name.setEnabled(false);

                m_description.setValue(m_group.getDescription());

                m_enabled.setValue(Boolean.valueOf(m_group.isEnabled()));

            }

        } catch (CmsException e) {
            LOG.error("unable to read group", e);
        }
        m_ok.setEnabled(false);
        m_ok.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 2337532424806798793L;

            public void buttonClick(ClickEvent event) {

                saveGroup();
                window.close();
                app.reload();

            }
        });

        m_cancel.addClickListener(new ClickListener() {

            private static final long serialVersionUID = -6389260624197980323L;

            public void buttonClick(ClickEvent event) {

                window.close();

            }
        });

        ValueChangeListener listener = new ValueChangeListener() {

            private static final long serialVersionUID = -7480617292190495288L;

            public void valueChange(ValueChangeEvent event) {

                m_ok.setEnabled(true);

            }

        };

        m_enabled.addValueChangeListener(listener);
        m_description.addValueChangeListener(listener);
        m_name.addValueChangeListener(listener);
        if (!m_groupEditParameters.isEditable()) {
            m_description.setEnabled(false);
            m_enabled.setEnabled(false);
        }

    }

    /**
     * Constructor for dialog for new groups.
     *
     * @param cms CmsObject
     * @param window window holding dialog
     * @param ou to create group in
     * @param app the app instance
     */
    public CmsGroupEditDialog(CmsObject cms, Window window, String ou, CmsAccountsApp app) {

        this(cms, null, window, app);
        m_ou.setValue(ou);
        m_enabled.setValue(Boolean.valueOf(true));
    }

    /**
     * Save group.<p>
     */
    protected void saveGroup() {

        if (m_group == null) {
            m_group = new CmsGroup();
            String ou = m_ou.getValue();
            if (!ou.endsWith("/")) {
                ou += "/";
            }
            m_group.setName(m_name.getValue());
            try {
                m_cms.createGroup(ou + m_name.getValue(), m_description.getValue(), 0, null);
            } catch (CmsException e) {
                //
            }
        }

        m_group.setDescription(m_description.getValue());
        m_group.setEnabled(m_enabled.getValue().booleanValue());

        try {
            m_cms.writeGroup(m_group);
        } catch (CmsException e) {
            //
        }
    }
}
