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

package org.opencms.ui.apps.resourcetypes;

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.file.CmsObject;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsResourceInfo;
import org.opencms.ui.components.fileselect.CmsPathSelectField;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.workplace.explorer.CmsResourceUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;
import com.vaadin.ui.Button;
import com.vaadin.ui.Window;
import com.vaadin.v7.data.Property.ValueChangeEvent;
import com.vaadin.v7.data.Property.ValueChangeListener;
import com.vaadin.v7.data.Validator;
import com.vaadin.v7.shared.ui.label.ContentMode;
import com.vaadin.v7.ui.CheckBox;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.OptionGroup;
import com.vaadin.v7.ui.TextField;

/**
 * Class for the edit resource type dialog.<p>
 */
@SuppressWarnings("deprecation")
public class CmsEditResourceTypeDialog extends CmsBasicDialog {

    /**
     * Validator for the title field.<p>
     */

    class IDValidator implements Validator {

        /**vaadin serial id.*/
        private static final long serialVersionUID = 7878441125879949490L;

        /**
         * @see com.vaadin.v7.data.Validator#validate(java.lang.Object)
         */
        public void validate(Object value) throws InvalidValueException {

            if (((String)value).isEmpty()) {
                throw new InvalidValueException(
                    CmsVaadinUtils.getMessageText(Messages.GUI_RESOURCETYPE_EDIT_INVALID_ID_0));
            }
            try {
                int id = Integer.parseInt((String)value);

                if (id == m_type.getTypeId()) {
                    return;
                }

                if (!CmsResourceTypeApp.isResourceTypeIdFree(id)) {
                    throw new InvalidValueException(
                        CmsVaadinUtils.getMessageText(Messages.GUI_RESOURCETYPE_EDIT_INVALID_ID_0));
                }
            } catch (NumberFormatException e) {
                throw new InvalidValueException(
                    CmsVaadinUtils.getMessageText(Messages.GUI_RESOURCETYPE_EDIT_INVALID_ID_0));
            }

        }

    }

    /**
     * Validator for the title field.<p>
     */
    class NameValidator implements Validator {

        /**vaadin serial id.*/
        private static final long serialVersionUID = 7878441125879949490L;

        /**
         * @see com.vaadin.data.Validator#validate(java.lang.Object)
         */
        public void validate(Object value) throws InvalidValueException {

            if (((String)value).isEmpty()) {
                throw new InvalidValueException(
                    CmsVaadinUtils.getMessageText(Messages.GUI_RESOURCETYPE_EDIT_INVALID_NAME_0));
            }
            if (m_type.getTypeName().equals(value)) {
                return;
            }
            if (!CmsResourceTypeApp.isResourceTypeNameFree((String)value)) {
                throw new InvalidValueException(
                    CmsVaadinUtils.getMessageText(Messages.GUI_RESOURCETYPE_EDIT_INVALID_NAME_0));
            }
        }
    }

    /**
     * Validator for the title field.<p>
     */
    class ResourceValidator implements Validator {

        /**vaadin serial id.*/
        private static final long serialVersionUID = 7878441125879949490L;

        /**
         * @see com.vaadin.v7.data.Validator#validate(java.lang.Object)
         */
        public void validate(Object value) throws InvalidValueException {

            if ((value == null) || ((String)value).isEmpty()) {
                throw new InvalidValueException(
                    CmsVaadinUtils.getMessageText(Messages.GUI_RESOURCETYPE_EDIT_INVALID_RESORUCE_0));
            }

            String resource = (String)value;
            if (!m_cms.existsResource(resource)) {
                throw new InvalidValueException(
                    CmsVaadinUtils.getMessageText(Messages.GUI_RESOURCETYPE_EDIT_INVALID_RESORUCE_0));
            }

        }

    }

    /** Vaadin serial id.*/
    private static final long serialVersionUID = -5966214615311043554L;

    /**constant. */
    private static final String ICON_MODE_CSS = "css";

    /**constant. */
    private static final String ICON_MODE_FILE = "file";

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsEditResourceTypeDialog.class);

    /** Vaadin vomponent.*/
    private OptionGroup m_iconMode;

    /** Vaadin vomponent.*/
    private TextField m_smallIconCSS;

    /** Vaadin vomponent.*/
    private TextField m_bigIconCSS;

    /** Vaadin vomponent.*/
    private TextField m_smallIconFile;

    /** Vaadin vomponent.*/
    private TextField m_bigIconFile;

    /** Resource type.*/
    protected I_CmsResourceType m_type;

    /** Vaadin vomponent.*/
    private Label m_warningIcon;

    /** Vaadin vomponent.*/
    private Button m_ok;

    /** Vaadin vomponent.*/
    private CheckBox m_confirm;

    /** Vaadin vomponent.*/
    private Button m_cancel;

    /** Vaadin vomponent.*/
    private TextField m_typeShortName;

    /** Vaadin vomponent.*/
    private TextField m_typeName;

    /** Vaadin vomponent.*/
    private TextField m_typeDescription;

    /** Vaadin vomponent.*/
    private TextField m_typeTitle;

    /** Vaadin vomponent.*/
    private TextField m_typeID;

    /** Vaadin vomponent.*/
    private CmsPathSelectField m_schema;

    /** A root CmsObject.*/
    private CmsObject m_cms;

    /**
     * Public constructor.<p>
     *
     * @param window Window
     * @param app app
     * @param resourceType type to be edited.
     */
    public CmsEditResourceTypeDialog(Window window, CmsResourceTypeApp app, I_CmsResourceType resourceType) {

        m_type = resourceType;
        try {
            m_cms = OpenCms.initCmsObject(A_CmsUI.getCmsObject());
        } catch (CmsException e1) {
            m_cms = A_CmsUI.getCmsObject();
        }
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        m_warningIcon.setContentMode(ContentMode.HTML);
        m_warningIcon.setValue(FontOpenCms.WARNING.getHtml());
        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(m_type.getModuleName())) {
            CmsModule module = OpenCms.getModuleManager().getModule(m_type.getModuleName());
            CmsExplorerTypeSettings dummy = OpenCms.getWorkplaceManager().getExplorerTypeSetting(m_type.getTypeName());
            for (CmsExplorerTypeSettings settings : module.getExplorerTypes()) {
                if (dummy.equals(settings)) {
                    CmsResourceInfo resInfo = new CmsResourceInfo(
                        CmsVaadinUtils.getMessageText(settings.getKey()),
                        m_type.getTypeName(),
                        CmsResourceUtil.getBigIconResource(settings, null));
                    displayResourceInfoDirectly(Arrays.asList(resInfo));
                }
            }
        } else {
            m_confirm.setEnabled(false);
        }

        m_ok.addClickListener(e -> submit(window, app));
        m_cancel.addClickListener(e -> window.close());
        setFields();
        setVisibilityOfIconFields();

        m_typeShortName.addValidator(new NameValidator());
        m_typeID.addValidator(new IDValidator());

        m_ok.setEnabled(false);
        m_confirm.addValueChangeListener(e -> m_ok.setEnabled(m_confirm.getValue().booleanValue()));
        m_typeShortName.setValue(m_type.getTypeName());
        m_typeID.setValue(String.valueOf(m_type.getTypeId()));
        if (m_type instanceof CmsResourceTypeXmlContent) {
            CmsResourceTypeXmlContent typeXML = (CmsResourceTypeXmlContent)m_type;
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(typeXML.getSchema())) {
                m_schema.setVisible(false);
            } else {
                m_schema.setValue(typeXML.getSchema());
                m_schema.addValidator(new ResourceValidator());
            }
        } else {
            m_schema.setVisible(false);
        }

        m_iconMode.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = -4165180367864561412L;

            public void valueChange(ValueChangeEvent event) {

                setVisibilityOfIconFields();
            }

        });

    }

    /**
     * Sets the visibility of the icon fields.<p>
     */
    protected void setVisibilityOfIconFields() {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_type.getModuleName())) {
            m_iconMode.setVisible(false);
            m_smallIconCSS.setVisible(false);
            m_smallIconFile.setVisible(false);
            m_bigIconCSS.setVisible(false);
            m_bigIconFile.setVisible(false);
            return;
        }

        if (ICON_MODE_CSS.equals(m_iconMode.getValue())) {
            m_smallIconCSS.setVisible(true);
            m_bigIconCSS.setVisible(true);
            m_smallIconFile.setVisible(false);
            m_bigIconFile.setVisible(false);
            return;
        }
        if (ICON_MODE_FILE.equals(m_iconMode.getValue())) {
            m_smallIconCSS.setVisible(false);
            m_bigIconCSS.setVisible(false);
            m_smallIconFile.setVisible(true);
            m_bigIconFile.setVisible(true);
            return;
        }
        m_smallIconCSS.setVisible(false);
        m_bigIconCSS.setVisible(false);
        m_smallIconFile.setVisible(false);
        m_bigIconFile.setVisible(false);
    }

    /**
     * Submit changes.<p>
     *
     * @param window Window
     * @param app app
     */
    protected void submit(Window window, CmsResourceTypeApp app) {

        if (isValid()) {
            CmsModule module = OpenCms.getModuleManager().getModule(m_type.getModuleName()).clone();
            if (isKeepTypeCase()) {
                saveResourceType(module);
            } else {
                try {
                    changeIdNameOrSchema(module);
                } catch (CmsConfigurationException e) {
                    LOG.error("Unable to change resource type.", e);
                }
            }

            try {
                OpenCms.getModuleManager().updateModule(m_cms, module);
                OpenCms.getResourceManager().initialize(m_cms);
                OpenCms.getWorkplaceManager().addExplorerTypeSettings(module);
                // re-initialize the workplace
                OpenCms.getWorkplaceManager().initialize(m_cms);
            } catch (CmsException e) {
                LOG.error("Unable to save resource type", e);
            }

            window.close();
            app.reload();
        }

    }

    /**
     * Change Id, name or schema (all require to delete old type and create new one).<p>
     *
     * @param module Module
     * @throws CmsConfigurationException exception
     */
    private void changeIdNameOrSchema(CmsModule module) throws CmsConfigurationException {

        //Remove m_type from current module (= delete type)
        List<CmsExplorerTypeSettings> typeSettings = Lists.newArrayList(module.getExplorerTypes());
        List<CmsExplorerTypeSettings> newTypeSettings = new ArrayList<CmsExplorerTypeSettings>();
        for (CmsExplorerTypeSettings setting : typeSettings) {
            if (!setting.getName().equals(m_type.getTypeName())) {
                newTypeSettings.add(setting);
            }
        }
        OpenCms.getWorkplaceManager().removeExplorerTypeSettings(module);

        List<I_CmsResourceType> types = new ArrayList<I_CmsResourceType>(module.getResourceTypes());

        types.remove(m_type);

        //Create new type
        CmsResourceTypeXmlContent type = new CmsResourceTypeXmlContent();
        type.addConfigurationParameter(CmsResourceTypeXmlContent.CONFIGURATION_SCHEMA, m_schema.getValue());
        type.setAdditionalModuleResourceType(true);
        type.setModuleName(module.getName());
        type.initConfiguration(
            m_typeShortName.getValue(),
            m_typeID.getValue(),
            CmsResourceTypeXmlContent.class.getName());
        types.add(type);

        module.setResourceTypes(types);

        // create the matching explorer type
        CmsExplorerTypeSettings setting = new CmsExplorerTypeSettings();
        setting.setTypeAttributes(
            m_typeShortName.getValue(),
            m_typeName.getValue(), //ToDo nicename
            null,
            null,
            CmsNewResourceTypeDialog.ICON_SMALL_DEFAULT,
            CmsNewResourceTypeDialog.ICON_BIG_DEFAULT,
            CmsResourceTypeXmlContent.getStaticTypeName(),
            null,
            "false",
            null,
            null);
        setting.setAutoSetNavigation("false");
        setting.setAutoSetTitle("false");
        setting.setNewResourceOrder("10");
        setting.setAddititionalModuleExplorerType(true);

        setting.setBigIcon(
            ICON_MODE_CSS.equals(m_iconMode.getValue())
            ? null
            : CmsStringUtil.isEmptyOrWhitespaceOnly(m_bigIconFile.getValue()) ? null : m_bigIconFile.getValue());
        setting.setIcon(
            ICON_MODE_CSS.equals(m_iconMode.getValue())
            ? null
            : CmsStringUtil.isEmptyOrWhitespaceOnly(m_smallIconFile.getValue()) ? null : m_smallIconFile.getValue());
        setting.setBigIconStyle(
            ICON_MODE_CSS.equals(m_iconMode.getValue())
            ? CmsStringUtil.isEmptyOrWhitespaceOnly(m_bigIconCSS.getValue()) ? null : m_bigIconCSS.getValue()
            : null);
        setting.setSmallIconStyle(
            ICON_MODE_CSS.equals(m_iconMode.getValue())
            ? CmsStringUtil.isEmptyOrWhitespaceOnly(m_smallIconCSS.getValue()) ? null : m_smallIconCSS.getValue()
            : null);
        setting.setKey(m_typeName.getValue());
        setting.setTitleKey(m_typeTitle.getValue());
        setting.setInfo(m_typeDescription.getValue());

        newTypeSettings.add(setting);
        module.setExplorerTypes(newTypeSettings);

        OpenCms.getWorkplaceManager().addExplorerTypeSettings(module);

    }

    private boolean isKeepTypeCase() {

        boolean res = ((Integer.parseInt(m_typeID.getValue())) == m_type.getTypeId()); //same id

        res = res && m_typeShortName.getValue().equals(m_type.getTypeName()); //same name

        if (m_type instanceof CmsResourceTypeXmlContent) {

            res = res && ((CmsResourceTypeXmlContent)m_type).getSchema().equals(m_schema.getValue()); //same schema
        }

        return res;

    }

    private boolean isValid() {

        return m_schema.isValid() && m_typeID.isValid() && m_typeShortName.isValid();
    }

    private void saveResourceType(CmsModule module) {

        List<CmsExplorerTypeSettings> typeSettings = Lists.newArrayList(
            OpenCms.getModuleManager().getModule(m_type.getModuleName()).getExplorerTypes());
        for (CmsExplorerTypeSettings setting : typeSettings) {
            if (!setting.getName().equals(m_type.getTypeName())) {
                continue;
            }
            setting.setBigIcon(
                ICON_MODE_CSS.equals(m_iconMode.getValue())
                ? null
                : CmsStringUtil.isEmptyOrWhitespaceOnly(m_bigIconFile.getValue()) ? null : m_bigIconFile.getValue());
            setting.setIcon(
                ICON_MODE_CSS.equals(m_iconMode.getValue())
                ? null
                : CmsStringUtil.isEmptyOrWhitespaceOnly(m_smallIconFile.getValue())
                ? null
                : m_smallIconFile.getValue());
            setting.setBigIconStyle(
                ICON_MODE_CSS.equals(m_iconMode.getValue())
                ? CmsStringUtil.isEmptyOrWhitespaceOnly(m_bigIconCSS.getValue()) ? null : m_bigIconCSS.getValue()
                : null);
            setting.setSmallIconStyle(
                ICON_MODE_CSS.equals(m_iconMode.getValue())
                ? CmsStringUtil.isEmptyOrWhitespaceOnly(m_smallIconCSS.getValue()) ? null : m_smallIconCSS.getValue()
                : null);
            setting.setKey(m_typeName.getValue());
            setting.setTitleKey(m_typeTitle.getValue());
            setting.setInfo(m_typeDescription.getValue());
            break;
        }
        module.setExplorerTypes(typeSettings);
        OpenCms.getWorkplaceManager().addExplorerTypeSettings(module);
    }

    private void setFields() {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_type.getModuleName())) {
            return;
        }

        List<CmsExplorerTypeSettings> typeSettings = Lists.newArrayList(
            OpenCms.getModuleManager().getModule(m_type.getModuleName()).getExplorerTypes());
        for (CmsExplorerTypeSettings setting : typeSettings) {
            if (!setting.getName().equals(m_type.getTypeName())) {
                continue;
            }
            boolean cssIconStyle = setting.getBigIcon() == null;

            if (cssIconStyle) {
                m_iconMode.setValue(ICON_MODE_CSS);
                if (setting.getBigIconStyle() == null) {
                    m_smallIconCSS.setValue("");
                    m_bigIconCSS.setValue("");
                } else {
                    m_smallIconCSS.setValue(setting.getSmallIconStyle());
                    m_bigIconCSS.setValue(setting.getBigIconStyle());
                }
            } else {
                m_iconMode.setValue(ICON_MODE_FILE);
                m_smallIconFile.setValue(setting.getIcon());
                m_bigIconFile.setValue(setting.getBigIcon());
            }
            m_typeName.setValue(setting.getKey());
            m_typeTitle.setValue(setting.getTitleKey());
            m_typeDescription.setValue(setting.getInfo());
            break;
        }
    }

}
