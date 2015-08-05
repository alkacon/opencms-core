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

package org.opencms.ui.dialogs;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.lock.CmsLockActionRecord;
import org.opencms.lock.CmsLockActionRecord.LockChange;
import org.opencms.lock.CmsLockUtil;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.workplace.CmsWorkplaceMessages;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;

/**
 * Dialog used to change resource modification times.<p>
 */
public class CmsSecureExportDialog extends CssLayout {

    /** The log instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSecureExportDialog.class);

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The Cancel button. */
    protected Button m_cancelButton;

    /** The dialog context. */
    protected I_CmsDialogContext m_context;

    /** Field for the export setting. */
    protected ComboBox m_exportField;

    /** Field for the export name. */
    protected TextField m_exportNameField;

    /** Field for the 'internal' option. */
    protected CheckBox m_internalField;

    /** The OK  button. */
    protected Button m_okButton;

    /** The current resource. */
    protected CmsResource m_resource;

    /** Field for the secure setting. */
    protected ComboBox m_secureField;

    /** The label to display the online link. */
    private Label m_linkField;

    /**
     * Creates a new instance.<p>
     *
     * @param context the dialog context
     */
    public CmsSecureExportDialog(I_CmsDialogContext context) {
        m_context = context;
        CmsObject cms = context.getCms();

        CmsVaadinUtils.readAndLocalizeDesign(
            this,
            OpenCms.getWorkplaceManager().getMessages(A_CmsUI.get().getLocale()),
            null);
        m_resource = m_context.getResources().get(0);

        initComboBox(m_exportField);
        initComboBox(m_secureField);
        m_linkField.setValue(OpenCms.getLinkManager().getOnlineLink(cms, cms.getSitePath(m_resource)));

        loadData();
        CmsSite site = OpenCms.getSiteManager().getCurrentSite(context.getCms());
        if ((site != null) && !site.hasSecureServer()) {
            m_secureField.setEnabled(false);
        }

        m_internalField.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = 1L;

            public void valueChange(ValueChangeEvent event) {

                Boolean valueObj = (Boolean)(event.getProperty().getValue());
                if (valueObj.booleanValue()) {
                    m_secureField.setEnabled(false);
                    m_exportField.setEnabled(false);
                } else {
                    m_secureField.setEnabled(true);
                    m_exportField.setEnabled(true);
                }

            }
        });

        m_cancelButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                m_context.onFinish(null);
            }

        });

        m_okButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                try {
                    saveData();
                    m_context.onFinish(null);
                } catch (Exception e) {
                    m_context.onError(e);
                }
            }
        });
    }

    /**
     * Loads the dialog data.<p>
     */
    protected void loadData() {

        try {
            List<CmsProperty> propList = m_context.getCms().readPropertyObjects(m_resource, false);
            Map<String, CmsProperty> propMap = CmsProperty.toObjectMap(propList);
            String secureValue = convertPropertyToFieldValue(propMap.get(CmsPropertyDefinition.PROPERTY_SECURE));
            String exportValue = convertPropertyToFieldValue(propMap.get(CmsPropertyDefinition.PROPERTY_EXPORT));
            CmsProperty exportnameProp = propMap.get(CmsPropertyDefinition.PROPERTY_EXPORTNAME);
            String exportnameValue = "";
            if (exportnameProp != null) {
                exportnameValue = exportnameProp.getValue();
            }
            m_exportField.setValue(exportValue);
            m_secureField.setValue(secureValue);
            m_exportNameField.setValue(exportnameValue);
            m_internalField.setValue(Boolean.valueOf(m_resource.isInternal()));
        } catch (CmsException e) {
            m_context.onError(e);

        }
    }

    /**
     * Touches the selected files.<p>
     *
     * @throws CmsException if something goes wrong
     */
    protected void saveData() throws CmsException {

        CmsObject cms = m_context.getCms();
        for (CmsResource resource : m_context.getResources()) {
            CmsLockActionRecord actionRecord = null;
            try {
                actionRecord = CmsLockUtil.ensureLock(m_context.getCms(), resource);

                String secureValue = (String)m_secureField.getValue();
                String exportValue = (String)m_exportField.getValue();
                String exportname = m_exportNameField.getValue();
                CmsProperty secureProp = new CmsProperty(CmsPropertyDefinition.PROPERTY_SECURE, secureValue, null);
                CmsProperty exportProp = new CmsProperty(CmsPropertyDefinition.PROPERTY_EXPORT, exportValue, null);
                CmsProperty exportNameProp = new CmsProperty(
                    CmsPropertyDefinition.PROPERTY_EXPORTNAME,
                    exportname,
                    null);
                boolean internal = m_internalField.getValue().booleanValue();

                cms.writePropertyObjects(resource, Arrays.asList(secureProp, exportProp, exportNameProp));
                resource.setInternal(internal);
                cms.writeResource(resource);
            } finally {
                if ((actionRecord != null) && (actionRecord.getChange() == LockChange.locked)) {
                    m_context.getCms().unlockResource(resource);
                }
            }
        }
    }

    /**
     * Converts a property object to a field value for one of the boolean selection widgets.<p>
     *
     * @param prop the property to convert
     * @return the field value
     */
    private String convertPropertyToFieldValue(CmsProperty prop) {

        if (prop == null) {
            return "";
        }
        return "" + Boolean.valueOf(prop.getValue());
    }

    /**
     * Fills the combo box with the options 'True', 'False' and 'Not set'.<p>
     *
     * @param combo the combo box to fill
     */
    private void initComboBox(ComboBox combo) {

        combo.setNullSelectionAllowed(false);
        combo.setTextInputAllowed(false);
        combo.addItem("true");
        combo.addItem("false");
        combo.addItem("");
        CmsWorkplaceMessages wpMessages = OpenCms.getWorkplaceManager().getMessages(A_CmsUI.get().getLocale());
        combo.setItemCaption("true", wpMessages.key(org.opencms.workplace.commons.Messages.GUI_LABEL_TRUE_0));
        combo.setItemCaption("false", wpMessages.key(org.opencms.workplace.commons.Messages.GUI_LABEL_FALSE_0));
        combo.setItemCaption("", wpMessages.key(org.opencms.workplace.commons.Messages.GUI_SECURE_NOT_SET_0));
    }

    /**
     * Returns true if the given string value is the representation of a boolean value.<p<
     *
     * @param value the string to check
     * @return true if the string is the representation of a boolean value
     */
    private boolean isBoolean(String value) {

        return "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value);
    }

}
