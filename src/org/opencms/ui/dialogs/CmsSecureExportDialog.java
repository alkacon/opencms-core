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

package org.opencms.ui.dialogs;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.lock.CmsLockActionRecord;
import org.opencms.lock.CmsLockActionRecord.LockChange;
import org.opencms.lock.CmsLockException;
import org.opencms.lock.CmsLockUtil;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsOkCancelActionHandler;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplaceMessages;

import java.util.ArrayList;
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
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Dialog used to change resource modification times.<p>
 */
public class CmsSecureExportDialog extends CmsBasicDialog {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSecureExportDialog.class);

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The Cancel button. */
    protected Button m_cancelButton;

    /** The dialog context. */
    protected I_CmsDialogContext m_context;

    /** Field for the export setting. */
    protected OptionGroup m_exportField;

    /** Field for the export name. */
    protected TextField m_exportNameField;

    /** Field for the 'internal' option. */
    protected CheckBox m_internalField;

    /** The OK  button. */
    protected Button m_okButton;

    /** The current resource. */
    protected CmsResource m_resource;

    /** Field for the secure setting. */
    protected OptionGroup m_secureField;

    /** Label to inform user that server has no secure server. */
    protected Label m_noSecureServerLabel;

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
        initOptionGroup(m_secureField);
        initOptionGroup(m_exportField);
        m_linkField.setValue(OpenCms.getLinkManager().getOnlineLink(cms, cms.getSitePath(m_resource)));

        loadData();
        CmsSite site = OpenCms.getSiteManager().getCurrentSite(context.getCms());
        m_noSecureServerLabel.setVisible(false);
        if ((site != null) && !site.hasSecureServer()) {
            m_secureField.setEnabled(false);
            m_secureField.setVisible(false);
            m_noSecureServerLabel.setVisible(true);
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

                cancel();
            }

        });

        m_okButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                submit();
            }
        });
        displayResourceInfo(m_context.getResources());

        setActionHandler(new CmsOkCancelActionHandler() {

            private static final long serialVersionUID = 1L;

            @Override
            protected void cancel() {

                CmsSecureExportDialog.this.cancel();
            }

            @Override
            protected void ok() {

                submit();
            }
        });
    }

    /**
     * Loads the dialog data.<p>
     */
    protected void loadData() {

        try {
            List<CmsProperty> propList = m_context.getCms().readPropertyObjects(m_resource, false);
            List<CmsProperty> inheritedPropList = m_context.getCms().readPropertyObjects(m_resource, true);
            Map<String, CmsProperty> propMap = CmsProperty.toObjectMap(propList);
            Map<String, CmsProperty> inheritedPropMap = CmsProperty.toObjectMap(inheritedPropList);
            String secureValue = convertPropertyToFieldValue(propMap.get(CmsPropertyDefinition.PROPERTY_SECURE));
            String inheritedSecureValue = convertPropertyToFieldValue(
                inheritedPropMap.get(CmsPropertyDefinition.PROPERTY_SECURE));

            String exportValue = convertPropertyToFieldValue(propMap.get(CmsPropertyDefinition.PROPERTY_EXPORT));

            CmsProperty exportnameProp = propMap.get(CmsPropertyDefinition.PROPERTY_EXPORTNAME);
            String exportnameValue = "";
            if (exportnameProp != null) {
                exportnameValue = exportnameProp.getValue();
            }
            m_exportField.setValue(exportValue);
            m_secureField.setValue(secureValue);
            if ("".equals(secureValue) && !"".equals(inheritedSecureValue)) {
                String origin = m_context.getCms().getRequestContext().removeSiteRoot(
                    inheritedPropMap.get(CmsPropertyDefinition.PROPERTY_SECURE).getOrigin());
                String inheritedValueCaption = CmsVaadinUtils.getMessageText(
                    org.opencms.workplace.commons.Messages.GUI_SECURE_INHERIT_FROM_2,
                    inheritedSecureValue,
                    origin);
                m_secureField.setItemCaption("", inheritedValueCaption);
            }

            m_exportNameField.setValue(exportnameValue);
            m_internalField.setValue(Boolean.valueOf(m_resource.isInternal()));
        } catch (CmsException e) {
            m_context.error(e);

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
                    try {
                        cms.unlockResource(resource);
                    } catch (CmsLockException e) {
                        LOG.warn(e.getLocalizedMessage(), e);
                    }
                }
            }
        }
    }

    /**
     * Cancels the dialog.<p>
     */
    void cancel() {

        m_context.finish(new ArrayList<CmsUUID>());
    }

    /**
     * Submits the dialog.<p>
     */
    void submit() {

        try {
            saveData();
            m_context.finish(null);
        } catch (Exception e) {
            m_context.error(e);
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
     * Fills the selection widget with the options 'True', 'False' and 'Not set'.<p>
     *
     * @param optGroup the option group to initialize
     */
    private void initOptionGroup(OptionGroup optGroup) {

        optGroup.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
        optGroup.setNullSelectionAllowed(false);
        optGroup.addItem("true");
        optGroup.addItem("false");
        optGroup.addItem("");
        CmsWorkplaceMessages wpMessages = OpenCms.getWorkplaceManager().getMessages(A_CmsUI.get().getLocale());
        optGroup.setItemCaption("true", wpMessages.key(org.opencms.workplace.commons.Messages.GUI_LABEL_TRUE_0));
        optGroup.setItemCaption("false", wpMessages.key(org.opencms.workplace.commons.Messages.GUI_LABEL_FALSE_0));
        optGroup.setItemCaption("", wpMessages.key(org.opencms.workplace.commons.Messages.GUI_SECURE_NOT_SET_0));
    }

}
