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
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.editablegroup.CmsEditableGroup;
import org.opencms.ui.components.editablegroup.I_CmsEditableGroupRow;
import org.opencms.util.CmsDataTypeUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.logging.Log;

import com.google.common.base.Supplier;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Window;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.TextField;

/**
 * Class for the Additional User info dialog.<p>
 */
public class CmsAdditionalInfosDialog extends CmsBasicDialog {

    /**vaadin serial id. */
    private static final long serialVersionUID = 9007206105761103873L;

    /** The logger of this class. */
    private static final Log LOG = CmsLog.getLog(CmsAdditionalInfosDialog.class);

    /**CmsUser to edit additional infos for. */
    private CmsUser m_user;

    /**vaadin component.*/
    private FormLayout m_layout;

    /**Layout for not editable info. */
    private FormLayout m_layoutnoedit;

    /**cancel button.*/
    private Button m_cancel;

    /**ok button.*/
    private Button m_ok;

    /** The map of editable additional info entries. */
    private SortedMap<String, Object> m_addInfoEditable;

    /** The map of non-editable additional info entries. */
    private SortedMap<String, Object> m_addInfoReadOnly;

    /** The group for the module resource fields. */
    private CmsEditableGroup m_userinfoGroup;

    /** The group for the module resource fields. */
    private CmsEditableGroup m_userinfoNoEditGroup;

    /** CmsObject.*/
    private CmsObject m_cms;

    /**
     * public constructor.<p>
     *
     * @param cms CmsObject
     * @param userID id of user
     * @param window window
     * @param app
     */
    public CmsAdditionalInfosDialog(CmsObject cms, CmsUUID userID, final Window window, CmsAccountsApp app) {

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);

        m_cms = cms;
        try {
            m_user = cms.readUser(userID);
            displayResourceInfoDirectly(Collections.singletonList(CmsAccountsApp.getPrincipalInfo(m_user)));
        } catch (CmsException e) {
            //
        }

        setAddInfoMaps();

        Supplier<Component> fieldFactory = new Supplier<Component>() {

            public Component get() {

                return addInfoLayout("", "", true);
            }
        };

        m_userinfoGroup = new CmsEditableGroup(m_layout, fieldFactory, "Add");
        m_userinfoGroup.init();

        m_userinfoNoEditGroup = new CmsEditableGroup(m_layoutnoedit, fieldFactory, "Add");
        m_userinfoNoEditGroup.setAddButtonVisible(false);
        m_userinfoNoEditGroup.init();

        for (String info : m_addInfoEditable.keySet()) {
            addAddInfoLayout(info, m_addInfoEditable.get(info), true);
        }

        for (String info : m_addInfoReadOnly.keySet()) {
            addAddInfoLayout(info, m_addInfoReadOnly.get(info), false);
        }

        window.setCaption(
            CmsVaadinUtils.getMessageText(
                org.opencms.ui.apps.Messages.GUI_USERMANAGEMENT_USER_ADDITIONAL_INFOS_WINDOW_CAPTION_1,
                m_user.getSimpleName()));

        m_cancel.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 4879585408645749L;

            public void buttonClick(ClickEvent event) {

                window.close();
            }

        });

        m_ok.addClickListener(new ClickListener() {

            /**vaadin serial id. */
            private static final long serialVersionUID = -5168099515616397665L;

            public void buttonClick(ClickEvent event) {

                submit();
                window.close();
                app.reload();
            }
        });

    }

    /**
     * Submit additional info to user.<p>
     */
    protected void submit() {

        if (m_userinfoNoEditGroup.getRows().size() != m_addInfoReadOnly.size()) {
            List<String> currentKeys = getKeyListFromGroup(m_userinfoNoEditGroup);
            for (String key : m_addInfoReadOnly.keySet()) {
                if (!currentKeys.contains(key)) {
                    saveAddInfo(key, null);
                }
            }
        }

        for (I_CmsEditableGroupRow row : m_userinfoGroup.getRows()) {
            String key = ((TextField)(((HorizontalLayout)row.getComponent()).getComponent(0))).getValue();
            String value = ((TextField)(((HorizontalLayout)row.getComponent()).getComponent(1))).getValue();
            saveAddInfo(key, value);
            m_addInfoEditable.remove(key);
        }

        //Remaining items in list seem to be deleted by the user..
        for (String key : m_addInfoEditable.keySet()) {
            saveAddInfo(key, null);
        }
        try {
            m_cms.writeUser(m_user);
        } catch (CmsException e) {
            LOG.error("Unable to write user.", e);
        }
    }

    /**
     * Get horizontal layout with key, value from additional info.<p>
     *
     * @param key key
     * @param value value
     * @param editable boolean
     * @return HorizontalLayout
     */
    HorizontalLayout addInfoLayout(String key, Object value, boolean editable) {

        HorizontalLayout res = new HorizontalLayout();
        res.setWidth("100%");
        res.setSpacing(true);

        TextField keyField = new TextField();
        keyField.setValue(key);
        keyField.setEnabled(editable);
        keyField.setWidth("100%");

        TextField valueField = new TextField();
        valueField.setValue(value.toString());
        valueField.setEnabled(editable);
        valueField.setWidth("100%");

        res.addComponent(keyField);
        res.addComponent(valueField);

        res.setExpandRatio(keyField, 1);
        res.setExpandRatio(valueField, 1);

        return res;
    }

    /**
     * Add key value pair as component to ui.<p>
     *
     * @param key string
     * @param value object
     * @param editable boolean
     */
    private void addAddInfoLayout(String key, Object value, boolean editable) {

        HorizontalLayout res = addInfoLayout(key, value, editable);
        if (editable) {
            m_userinfoGroup.addRow(res);
        } else {
            m_userinfoNoEditGroup.addRow(res);
        }
    }

    /**
     * Get List of keys from UI.<p>
     *
     * @param group editablegroup object
     * @return List of keys
     */
    private List<String> getKeyListFromGroup(CmsEditableGroup group) {

        List<String> res = new ArrayList<String>();
        for (I_CmsEditableGroupRow row : group.getRows()) {
            res.add(((TextField)(((HorizontalLayout)row.getComponent()).getComponent(0))).getValue());
        }
        return res;
    }

    /**
     * Adds additional info to user.<p>
     * (Doesn't write the user)<p>
     *
     * @param key string
     * @param value string
     */
    private void saveAddInfo(String key, String value) {

        int pos = key.indexOf("@");

        String className = "";
        if (pos > -1) {
            className = key.substring(pos + 1);
            key = key.substring(0, pos);
        }

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(value)) {
            m_user.deleteAdditionalInfo(key);
            return;
        }

        if (pos < 0) {
            m_user.setAdditionalInfo(key, value);
            return;
        }

        Class<?> clazz;
        try {
            // try the class name
            clazz = Class.forName(className);
        } catch (Throwable e) {
            try {
                // try the class in the java.lang package
                clazz = Class.forName(Integer.class.getPackage().getName() + "." + className);
            } catch (Throwable e1) {
                clazz = String.class;
            }
        }
        m_user.setAdditionalInfo(key, CmsDataTypeUtil.parse(value, clazz));
    }

    /**
     * Builds the additional info maps.<p>
     */
    private void setAddInfoMaps() {

        m_addInfoEditable = new TreeMap<String, Object>();
        m_addInfoReadOnly = new TreeMap<String, Object>();
        Iterator<Entry<String, Object>> itEntries = m_user.getAdditionalInfo().entrySet().iterator();
        while (itEntries.hasNext()) {
            Entry<String, Object> entry = itEntries.next();
            String key = entry.getKey().toString();
            if ((entry.getValue() == null) || CmsStringUtil.isEmptyOrWhitespaceOnly(entry.getValue().toString())) {
                // skip empty entries
                continue;
            }
            if (!entry.getValue().getClass().equals(String.class)) {
                // only show type different to string
                key += "@" + entry.getValue().getClass().getName();
            }
            if (CmsDataTypeUtil.isParseable(entry.getValue().getClass())) {
                m_addInfoEditable.put(key, entry.getValue());
            } else {
                String value = entry.getValue().toString();
                if (value.length() > (75 - key.length())) {
                    if ((75 - key.length()) > 5) {
                        value = value.substring(0, (75 - key.length())) + " ...";
                    } else {
                        value = "...";
                    }
                }
                m_addInfoReadOnly.put(key, value);
            }
        }
    }
}
