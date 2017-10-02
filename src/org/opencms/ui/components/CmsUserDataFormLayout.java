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

package org.opencms.ui.components;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsMessages;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.util.CmsNullToEmptyConverter;
import org.opencms.workplace.CmsAccountInfo;
import org.opencms.workplace.CmsAccountInfo.Field;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.logging.Log;

import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.data.validator.AbstractStringValidator;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;

/**
 * Form Layout for user data.<p>
 */
public class CmsUserDataFormLayout extends FormLayout {

    /**
     * Validator employing the configured OpenCms validation handler.<p>
     */
    private static class FieldValidator extends AbstractStringValidator {

        /** The serial version id. */
        private static final long serialVersionUID = 4432834072807177046L;

        /** The field to validate. */
        private Field m_field;

        /**
         * Constructor.<p>
         *
         * @param field the field to validate
         */
        public FieldValidator(Field field) {
            super(null);
            m_field = field;
        }

        /**
         * @see com.vaadin.data.validator.AbstractValidator#isValidValue(java.lang.Object)
         */
        @SuppressWarnings("incomplete-switch")
        @Override
        protected boolean isValidValue(String value) {

            boolean result = true;

            try {
                switch (m_field) {
                    case email:
                        OpenCms.getValidationHandler().checkEmail(value);
                        break;
                    case firstname:
                        OpenCms.getValidationHandler().checkFirstname(value);
                        break;
                    case lastname:
                        OpenCms.getValidationHandler().checkLastname(value);
                        break;
                    case zipcode:
                        OpenCms.getValidationHandler().checkZipCode(value);
                        break;

                }
            } catch (CmsIllegalArgumentException e) {
                result = false;
                setErrorMessage(e.getLocalizedMessage(UI.getCurrent().getLocale()));
            }

            return result;
        }
    }

    /**Vaadin serial id.*/
    private static final long serialVersionUID = 4893705558720239863L;

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsUserDataFormLayout.class);

    /** The property item. */
    private PropertysetItem m_infos;

    /** The field binder. */
    private FieldGroup m_binder;

    /**
     * empty constructor.<p>
     */
    public CmsUserDataFormLayout() {

    }

    /**
     * Initializes available fields for given user.<p>
     *
     * @param user CmsUser to display fields for
     */
    public void initFields(CmsUser user) {

        m_infos = new PropertysetItem();
        for (CmsAccountInfo info : OpenCms.getWorkplaceManager().getAccountInfos()) {
            String value = info.getValue(user);
            if (value == null) {
                value = "";
            }
            m_infos.addItemProperty(info, new ObjectProperty<String>(value));
        }

        m_binder = new FieldGroup(m_infos);
        for (CmsAccountInfo info : OpenCms.getWorkplaceManager().getAccountInfos()) {
            addComponent(buildField(getLabel(info), info));
        }
    }

    /**
     * Store fields to given user.<p>
     *
     * @param user User to write information to
     * @param cms CmsObject
     * @param afterWrite runnable which gets called after writing user
     */
    public void submit(CmsUser user, CmsObject cms, Runnable afterWrite) {

        try {
            if (isValid()) {
                m_binder.commit();
                PropertyUtilsBean propUtils = new PropertyUtilsBean();
                for (CmsAccountInfo info : OpenCms.getWorkplaceManager().getAccountInfos()) {
                    if (info.isEditable()) {
                        if (info.isAdditionalInfo()) {
                            user.setAdditionalInfo(info.getAddInfoKey(), m_infos.getItemProperty(info).getValue());
                        } else {
                            propUtils.setProperty(
                                user,
                                info.getField().name(),
                                m_infos.getItemProperty(info).getValue());
                        }
                    }
                }
                cms.writeUser(user);
                afterWrite.run();
            }
        } catch (CmsException | CommitException | IllegalAccessException | InvocationTargetException
        | NoSuchMethodException e) {
            LOG.error("Unable to commit changes to user..", e);
        }
    }

    /**
     * Builds the text field for the given property.<p>
     *
     * @param label the field label
     * @param info the property name
     *
     * @return the field
     */
    private TextField buildField(String label, CmsAccountInfo info) {

        TextField field = (TextField)m_binder.buildAndBind(label, info);
        field.setConverter(new CmsNullToEmptyConverter());
        field.setWidth("100%");
        field.setEnabled(info.isEditable());
        if (info.isEditable()) {
            field.addValidator(new FieldValidator(info.getField()));
        }
        field.setImmediate(true);
        return field;
    }

    /**
     * Returns the field label.<p>
     *
     * @param info the info
     *
     * @return the label
     */
    private String getLabel(CmsAccountInfo info) {

        if (info.isAdditionalInfo()) {
            String label = CmsVaadinUtils.getMessageText("GUI_USER_DATA_" + info.getAddInfoKey().toUpperCase() + "_0");
            if (CmsMessages.isUnknownKey(label)) {
                return info.getAddInfoKey();
            } else {
                return label;
            }
        } else {
            return CmsVaadinUtils.getMessageText("GUI_USER_DATA_" + info.getField().name().toUpperCase() + "_0");
        }
    }

    /**
     * Returns whether the form fields are valid.<p>
     *
     * @return <code>true</code> if the form fields are valid
     */
    private boolean isValid() {

        boolean valid = true;
        for (Component comp : this) {
            if (comp instanceof TextField) {
                valid = valid && ((TextField)comp).isValid();
            }
        }
        return valid;
    }
}
