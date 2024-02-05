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
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsDefaultValidationHandler;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.editablegroup.CmsEditableGroup;
import org.opencms.ui.components.editablegroup.I_CmsEditableGroupRow;
import org.opencms.ui.components.fileselect.CmsPathSelectField;
import org.opencms.util.CmsFileUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;

import com.google.common.base.Supplier;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Window;
import com.vaadin.v7.data.Property.ValueChangeEvent;
import com.vaadin.v7.data.Property.ValueChangeListener;
import com.vaadin.v7.data.Validator;
import com.vaadin.v7.ui.AbstractField;
import com.vaadin.v7.ui.CheckBox;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.TextArea;
import com.vaadin.v7.ui.TextField;

/**
 * Class for the ou edit and new dialog.<p>
 */
public class CmsOUEditDialog extends CmsBasicDialog {

    /**
     * Validator for ou name.<p>
     */
    protected class NameValidator implements Validator {

        /**vaadin serial id. */
        private static final long serialVersionUID = 6830449175508751039L;

        /**
         * @see com.vaadin.data.Validator#validate(java.lang.Object)
         */
        public void validate(Object value) throws InvalidValueException {

            try {
                CmsDefaultValidationHandler handler = new CmsDefaultValidationHandler();
                handler.checkUserName((String)value);
            } catch (CmsIllegalArgumentException e) {
                throw new InvalidValueException(
                    CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_USER_VALIDATION_LOGINNAME_INVALID_0));
            }

        }

    }

    /**
     * Validator for resource names.<p>
     */
    protected class ResourceValidator implements Validator {

        /**vaadin serial id. */
        private static final long serialVersionUID = -2325058988240648143L;

        /**
         * @see com.vaadin.data.Validator#validate(java.lang.Object)
         */
        public void validate(Object value) throws InvalidValueException {

            if (isInvalidResourceName((String)value)) {
                throw new InvalidValueException(
                    CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_OU_RESOURCE_NOT_VALID_0));
            }

            if (isOutOfOu((String)value)) {
                throw new InvalidValueException(
                    CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_OU_RESOURCE_OUT_OF_OU_0));
            }

        }
    }

    /**vaadim serial id. */
    private static final long serialVersionUID = -1462196157732607548L;

    /** The logger for this class. */
    static Log LOG = CmsLog.getLog(CmsOUEditDialog.class.getName());

    /**CmsObject. */
    protected CmsObject m_cms;

    /**OU to be edited, null if new one should be created. */
    private CmsOrganizationalUnit m_ou;

    /**vaadin component.*/
    Button m_ok;

    /**vaadin component.*/
    private TextField m_name;

    /**vaadin component.*/
    private Button m_cancel;

    /**vaadin component.*/
    private TextArea m_description;

    /**vaadin component.*/
    Label m_parentOu;

    /**vaadin component.*/
    private CheckBox m_hideLogin;

    /**vaadin component.*/
    private CheckBox m_webuser;

    /**vaadin component.*/
    private FormLayout m_resLayout;

    /** The group for the module resource fields. */
    private CmsEditableGroup m_ouResources;

    /**
     * public constructor.<p>
     *
     * @param cms CmsObject
     * @param ou id of group edit, null if ou should be created
     * @param window window holding the dialog
     * @param app
     */
    public CmsOUEditDialog(CmsObject cms, String ou, final Window window, final CmsAccountsApp app) {

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);

        if (ou != null) {
            try {
                displayResourceInfoDirectly(
                    Collections.singletonList(
                        CmsAccountsApp.getOUInfo(
                            OpenCms.getOrgUnitManager().readOrganizationalUnit(A_CmsUI.getCmsObject(), ou))));
            } catch (CmsException e1) {
                LOG.error("Unable to read OU", e1);
            }
        }

        m_cms = cms;
        Supplier<Component> fieldFactory = new Supplier<Component>() {

            public Component get() {

                CmsPathSelectField field = new CmsPathSelectField();
                field.setUseRootPaths(true);
                field.setCmsObject(m_cms);
                try {
                    field.setValue(
                        OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(m_cms, m_parentOu.getValue()).get(
                            0).getRootPath());
                } catch (CmsException e) {
                    //
                }
                return field;
            }
        };
        m_ouResources = new CmsEditableGroup(
            m_resLayout,
            fieldFactory,
            CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_OU_ADD_RESOURCE_0));
        m_ouResources.init();

        try {
            if (ou != null) {
                m_ou = OpenCms.getOrgUnitManager().readOrganizationalUnit(m_cms, ou);
                m_name.setEnabled(false);
                m_name.setValue(m_ou.getName());
                m_description.setValue(m_ou.getDescription());
                m_parentOu.setValue(m_ou.getParentFqn().equals("") ? "/" : m_ou.getParentFqn());
                m_hideLogin.setValue(Boolean.valueOf(m_ou.hasFlagHideLogin()));
                m_webuser.setValue(Boolean.valueOf(m_ou.hasFlagWebuser()));
                m_webuser.setEnabled(false);
                for (CmsResource resource : OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(
                    m_cms,
                    m_ou.getName())) {
                    CmsPathSelectField field = new CmsPathSelectField();
                    field.setValue(resource.getRootPath());
                    field.setUseRootPaths(true);
                    field.setCmsObject(m_cms);
                    m_ouResources.addRow(field);
                }
            }

        } catch (CmsException e) {
            LOG.error("unable to read group", e);
        }
        m_ok.setEnabled(m_ou != null);
        m_ok.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 2337532424806798793L;

            public void buttonClick(ClickEvent event) {

                validate();
                if (isValid()) {
                    saveOU();
                    window.close();
                    app.reload();
                }
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

        m_description.addValueChangeListener(listener);
        m_name.addValueChangeListener(listener);
    }

    /**
     * public constructor.<p>
     *
     * @param cms CmsObject
     * @param window window holding dialog
     * @param ou to create group in
     */
    public CmsOUEditDialog(CmsObject cms, Window window, String ou, CmsAccountsApp app) {

        this(cms, null, window, app);
        m_parentOu.setValue(ou.equals("") ? "/" : ou);
        try {
            displayResourceInfoDirectly(
                Collections.singletonList(
                    CmsAccountsApp.getOUInfo(
                        OpenCms.getOrgUnitManager().readOrganizationalUnit(
                            A_CmsUI.getCmsObject(),
                            m_parentOu.getValue()))));
        } catch (CmsException e1) {
            LOG.error("Unable to read OU", e1);
        }

        CmsPathSelectField field = new CmsPathSelectField();
        field.setUseRootPaths(true);
        field.setCmsObject(m_cms);
        try {
            field.setValue(OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(cms, ou).get(0).getRootPath());
        } catch (Throwable e) {
            // We simply do not set the field's value if something goes wrong.
        }
        m_ouResources.addRow(field);
    }

    /**
     * Checks if given resource is invalid.<p>
     *
     * @param resourceName name of resource
     * @return true if resourceName is invalid
     */
    protected boolean isInvalidResourceName(String resourceName) {

        if (resourceName == null) {
            return true;
        }
        try {

            m_cms.readResource(resourceName);
            return false;
        } catch (CmsException e) {
            //Ok, resource not valid..
        }
        return true;
    }

    /**
     * Check if resource is in parent OU.<p>
     *
     * @param resourceName to check
     * @return boolean
     */
    protected boolean isOutOfOu(String resourceName) {

        if (resourceName == null) {
            return true;
        }
        try {
            boolean notOk = true;
            for (CmsResource res : OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(
                m_cms,
                m_parentOu.getValue())) {
                if (resourceName.startsWith(res.getRootPath())) {
                    notOk = false;
                }
            }
            return notOk;
        } catch (CmsException e) {
            //
        }
        return true;
    }

    /**
     * Checks if all fields are valid.<p>
     *
     * @return true, if all data are ok.
     */
    @SuppressWarnings("unchecked")
    protected boolean isValid() {

        boolean res = true;

        res = res & m_description.isValid();
        res = res & m_name.isValid();
        if (!res) {
            return res;
        }
        for (I_CmsEditableGroupRow row : m_ouResources.getRows()) {
            if (!((AbstractField<String>)row.getComponent()).isValid()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Adds validators to fields.<p>
     */
    @SuppressWarnings("unchecked")
    protected void validate() {

        if (m_ou == null) {
            m_name.removeAllValidators();
            m_name.addValidator(new NameValidator());
        }
        m_description.setRequired(true);
        m_description.setRequiredError("Required");

        if (m_ouResources.getRows().isEmpty() & !m_webuser.getValue().booleanValue()) {
            CmsPathSelectField field = new CmsPathSelectField();
            field.setUseRootPaths(true);
            field.setCmsObject(m_cms);
            m_ouResources.addRow(field);
        }
        for (I_CmsEditableGroupRow row : m_ouResources.getRows()) {
            ((AbstractField<String>)row.getComponent()).removeAllValidators();
            ((AbstractField<String>)row.getComponent()).addValidator(new ResourceValidator());
        }
    }

    /**
     * Saves the OU.<p>
     */
    void saveOU() {

        try {
            List<String> resources = new ArrayList<String>();
            for (I_CmsEditableGroupRow row : m_ouResources.getRows()) {
                resources.add(((CmsPathSelectField)row.getComponent()).getValue());
            }
            if (m_ou == null) {
                String parentOu = m_parentOu.getValue();
                if (!parentOu.endsWith("/")) {
                    parentOu += "/";
                }
                if (resources.contains("null")) {
                    resources.remove("null");
                }
                List<String> resourceNames = CmsFileUtil.removeRedundancies(resources);
                m_ou = OpenCms.getOrgUnitManager().createOrganizationalUnit(
                    m_cms,
                    parentOu + m_name.getValue(),
                    m_description.getValue(),
                    getFlags(),
                    resourceNames.isEmpty() ? null : resourceNames.get(0));

                if (!resourceNames.isEmpty()) {
                    resourceNames.remove(0);
                    Iterator<String> itResourceNames = CmsFileUtil.removeRedundancies(resourceNames).iterator();
                    while (itResourceNames.hasNext()) {
                        OpenCms.getOrgUnitManager().addResourceToOrgUnit(m_cms, m_ou.getName(), itResourceNames.next());
                    }
                }
            } else {
                m_ou.setDescription(m_description.getValue());
                m_ou.setFlags(getFlags());
                List<String> resourceNamesNew = CmsFileUtil.removeRedundancies(resources);
                List<CmsResource> resourcesOld = OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(
                    m_cms,
                    m_ou.getName());
                List<String> resourceNamesOld = new ArrayList<String>();
                Iterator<CmsResource> itResourcesOld = resourcesOld.iterator();
                while (itResourcesOld.hasNext()) {
                    CmsResource resourceOld = itResourcesOld.next();
                    resourceNamesOld.add(m_cms.getSitePath(resourceOld));
                }
                Iterator<String> itResourceNamesNew = resourceNamesNew.iterator();
                // add new resources to ou
                while (itResourceNamesNew.hasNext()) {
                    String resourceNameNew = itResourceNamesNew.next();
                    if (!resourceNamesOld.contains(resourceNameNew)) {
                        OpenCms.getOrgUnitManager().addResourceToOrgUnit(m_cms, m_ou.getName(), resourceNameNew);
                    }
                }
                Iterator<String> itResourceNamesOld = resourceNamesOld.iterator();
                // delete old resources from ou
                while (itResourceNamesOld.hasNext()) {
                    String resourceNameOld = itResourceNamesOld.next();
                    if (!resourceNamesNew.contains(resourceNameOld)) {
                        OpenCms.getOrgUnitManager().removeResourceFromOrgUnit(m_cms, m_ou.getName(), resourceNameOld);
                    }
                }
                // write the edited organizational unit
                OpenCms.getOrgUnitManager().writeOrganizationalUnit(m_cms, m_ou);
            }
        } catch (CmsException e) {
            LOG.error("Unable to save OU", e);
        }
    }

    /**
     * Get OU-flags.<p>
     *
     * @return the flag int-value
     */
    private int getFlags() {

        int flags = 0;
        if (m_hideLogin.getValue().booleanValue()) {
            flags += CmsOrganizationalUnit.FLAG_HIDE_LOGIN;
        }
        if (m_webuser.getValue().booleanValue()) {
            flags += CmsOrganizationalUnit.FLAG_WEBUSERS;
        }
        return flags;
    }
}
