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

package org.opencms.ade.sitemap.client.edit;

import org.opencms.ade.sitemap.client.Messages;
import org.opencms.gwt.client.property.A_CmsPropertyEditor;
import org.opencms.gwt.client.property.I_CmsPropertyEditorHandler;
import org.opencms.gwt.client.ui.input.I_CmsFormWidget;
import org.opencms.gwt.client.ui.input.I_CmsHasGhostValue;
import org.opencms.gwt.client.ui.input.form.A_CmsFormFieldPanel;
import org.opencms.gwt.client.ui.input.form.CmsBasicFormField;
import org.opencms.gwt.client.ui.input.form.CmsInfoBoxFormFieldPanel;
import org.opencms.gwt.shared.CmsGwtLog;
import org.opencms.gwt.shared.property.CmsClientProperty;
import org.opencms.gwt.shared.property.CmsPathValue;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.Widget;

/**
 * Sitemap entry editor class for the navigation mode.<p>
 *
 * @since 8.0.0
 */
public class CmsNavModePropertyEditor extends A_CmsPropertyEditor {

    /** The 'split mode' to use for rendering fields. For some property fields, we may want to render two fields, one for the folder and one for the index.html, and this enum is used to tell the rendering function which we are currently rendering. */
    enum SplitMode {
        /** The property is not split. */
        none,

        /** The property is split, and we are rendering the field for the parent folder. */
        parent,

        /** The property is split, and we are rendering the field for the index.html. */
        child;
    }

    /**
    * Creates a new instance.<p>
    *
    * @param propertyConfig the property configuration
    * @param handler the entry editor handler
    */
    public CmsNavModePropertyEditor(
        Map<String, CmsXmlContentProperty> propertyConfig,
        I_CmsPropertyEditorHandler handler) {

        super(propertyConfig, handler);
    }

    /**
     * @see org.opencms.gwt.client.property.A_CmsPropertyEditor#buildFields()
     */
    @Override
    public void buildFields() {

        Map<String, CmsClientProperty> ownProps = m_handler.getOwnProperties();
        Map<String, CmsClientProperty> defaultFileProps = m_handler.getDefaultFileProperties();
        CmsGwtLog.log("DFP" + m_handler.getDefaultFileProperties());

        String entryId = m_handler.getId().toString();
        String defaultFileId = toStringOrNull(m_handler.getDefaultFileId());
        boolean splitTitle = false;
        String navText = "";
        String title = "";
        for (String key : Arrays.asList(CmsClientProperty.PROPERTY_NAVTEXT, CmsClientProperty.PROPERTY_TITLE)) {
            CmsClientProperty prop = ownProps.get(key);
            if (prop != null) {
                String value = prop.getEffectiveValue();
                if (value == null) {
                    value = "";
                }
                if (CmsClientProperty.PROPERTY_NAVTEXT.equals(key)) {
                    navText = value;
                } else {
                    title = value;
                }
            }
        }
        // if we have both a folder and a default file and either the navtext is different from the title,
        // or the title is empty, split title field into two fields, one for the folder and one for the default file
        splitTitle = (defaultFileId != null) && !(navText.equals(title) || "".equals(title));

        List<String> keys = new ArrayList<String>(m_propertyConfig.keySet());
        moveToTop(keys, CmsClientProperty.PROPERTY_NAVTEXT);
        moveToTop(keys, CmsClientProperty.PROPERTY_DESCRIPTION);
        moveToTop(keys, CmsClientProperty.PROPERTY_TITLE);
        for (String propName : keys) {
            if (splitTitle && CmsClientProperty.PROPERTY_TITLE.equals(propName)) {
                for (SplitMode mode : Arrays.asList(SplitMode.parent, SplitMode.child)) {
                    buildSimpleField(entryId, defaultFileId, ownProps, defaultFileProps, propName, mode);
                }
            } else {
                buildSimpleField(entryId, defaultFileId, ownProps, defaultFileProps, propName, SplitMode.none);
            }

        }
    }

    /**
     * @see org.opencms.gwt.client.property.A_CmsPropertyEditor#setupFieldContainer()
     */
    @Override
    protected void setupFieldContainer() {

        m_form.setWidget(new CmsInfoBoxFormFieldPanel(m_handler.getPageInfo()));
    }

    /**
     * Builds a single form field.<p>
     *

     * @param entryId the entry id
     * @param defaultFileId the default file id
     * @param ownProps the entry's own properties
     * @param defaultFileProps the default file properties
     * @param propName the property name
     */
    private void buildSimpleField(
        String entryId,
        String defaultFileId,
        Map<String, CmsClientProperty> ownProps,
        Map<String, CmsClientProperty> defaultFileProps,
        String propName,
        SplitMode splitMode) {

        CmsXmlContentProperty propDef = m_propertyConfig.get(propName);
        CmsClientProperty fileProp = defaultFileProps == null ? null : defaultFileProps.get(propName);
        CmsClientProperty ownProp = ownProps.get(propName);
        CmsPathValue pathValue = null;
        if (splitMode == SplitMode.parent) {
            if (ownProp != null) {
                pathValue = ownProp.getPathValue().prepend(entryId + "/" + propName);
            } else {
                pathValue = new CmsPathValue(
                    "",
                    entryId + "/" + propName + "/" + CmsClientProperty.PATH_STRUCTURE_VALUE);
            }
        } else if (splitMode == SplitMode.child) {
            if (fileProp != null) {
                pathValue = fileProp.getPathValue().prepend(defaultFileId + "/" + propName);
            } else {
                pathValue = new CmsPathValue(
                    "",
                    defaultFileId + "/" + propName + "/" + CmsClientProperty.PATH_STRUCTURE_VALUE);
            }
        } else {
            if (!CmsClientProperty.isPropertyEmpty(fileProp)) {
                pathValue = fileProp.getPathValue().prepend(defaultFileId + "/" + propName);
            } else if (!CmsClientProperty.isPropertyEmpty(ownProp)) {
                pathValue = ownProp.getPathValue().prepend(entryId + "/" + propName);
            } else {
                String targetId = null;
                if (propDef.isPreferFolder() || (m_handler.getDefaultFileId() == null)) {
                    targetId = entryId;
                } else {
                    targetId = m_handler.getDefaultFileId().toString();
                }
                pathValue = new CmsPathValue(
                    "",
                    targetId + "/" + propName + "/" + CmsClientProperty.PATH_STRUCTURE_VALUE);
            }
        }
        boolean alwaysAllowEmpty = !propName.equals(CmsClientProperty.PROPERTY_NAVTEXT);
        //CHECK: should we really generally allow empty fields other than NavText to be empty?

        CmsBasicFormField field = CmsBasicFormField.createField(
            propDef,
            pathValue.getPath(),
            this,
            Collections.<String, String> emptyMap(),
            alwaysAllowEmpty);
        CmsClientProperty inheritedProperty = m_handler.getInheritedProperty(propName);
        String inherited = (inheritedProperty == null) ? null : inheritedProperty.getEffectiveValue();
        if (inheritedProperty != null) {
            String message = Messages.get().key(
                Messages.GUI_PROPERTY_ORIGIN_2,
                inheritedProperty.getOrigin(),
                inherited);
            ((Widget)field.getWidget()).setTitle(message);
        }
        I_CmsFormWidget w = field.getWidget();
        // model binding not necessary here
        String initialValue = pathValue.getValue();

        boolean ghost = CmsStringUtil.isEmptyOrWhitespaceOnly(pathValue.getValue());
        if (w instanceof I_CmsHasGhostValue) {
            ((I_CmsHasGhostValue)w).setGhostValue(inherited, ghost);
            if (ghost) {
                initialValue = null;
            }
        }
        if (ghost && (inheritedProperty != null)) {
            String message = org.opencms.gwt.client.Messages.get().key(
                org.opencms.gwt.client.Messages.GUI_ORIGIN_INHERITED_1,
                inheritedProperty.getOrigin());
            field.getLayoutData().put("info", message);
        }
        if (splitMode == SplitMode.parent) {
            field.getLayoutData().put(A_CmsFormFieldPanel.LAYOUT_TAG, Messages.get().key(Messages.GUI_TAG_FOLDER_0));
        } else if (splitMode == SplitMode.child) {
            String subtitle = m_handler.getPageInfo().getSubTitle();
            int slashPos = subtitle.lastIndexOf("/");
            String name = slashPos > -1 ? subtitle.substring(slashPos + 1) : subtitle;
            if (!name.toLowerCase().endsWith(".html")) {
                name = "index.html";
            }
            field.getLayoutData().put(A_CmsFormFieldPanel.LAYOUT_TAG, name);
        }

        m_form.addField(m_form.getWidget().getDefaultGroup(), field, initialValue);
    }

    /**
     * Moves the given property name to the top of the keys if present.<p>
     *
     * @param keys the list of keys
     * @param propertyName the property name to move
     */
    private void moveToTop(List<String> keys, String propertyName) {

        if (keys.contains(propertyName)) {
            keys.remove(propertyName);
            keys.add(0, propertyName);
        }
    }

    /**
     * Helper method which converts an object to a string and returns null if it is null.<p>
     *
     * @param obj the object for which to return the string
     *
     * @return the string representation or null
     */
    private String toStringOrNull(Object obj) {

        return obj == null ? null : obj.toString();
    }

}
