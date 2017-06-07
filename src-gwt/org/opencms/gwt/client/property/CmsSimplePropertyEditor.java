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

package org.opencms.gwt.client.property;

import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.ui.input.I_CmsHasGhostValue;
import org.opencms.gwt.client.ui.input.I_CmsStringModel;
import org.opencms.gwt.client.ui.input.form.CmsBasicFormField;
import org.opencms.gwt.client.ui.input.form.CmsSimpleFormFieldPanel;
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.shared.property.CmsClientProperty;
import org.opencms.gwt.shared.property.CmsClientProperty.Mode;
import org.opencms.gwt.shared.property.CmsPathValue;
import org.opencms.gwt.shared.property.CmsPropertyModification;
import org.opencms.util.CmsPair;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.Widget;

/**
 * The sitemap entry editor class for the VFS mode.<p>
 *
 * @since 8.0.0
 */
public class CmsSimplePropertyEditor extends A_CmsPropertyEditor {

    /** The map of models of the fields. */
    Map<String, I_CmsStringModel> m_models = new HashMap<String, I_CmsStringModel>();

    /** The properties of the entry. */
    private Map<String, CmsClientProperty> m_properties;

    /**
     * Creates a new sitemap entry editor instance for the VFS mode.<p>
     *
     * @param propConfig the property configuration
     * @param handler the sitemap entry editor handler
     */
    public CmsSimplePropertyEditor(Map<String, CmsXmlContentProperty> propConfig, I_CmsPropertyEditorHandler handler) {

        super(propConfig, handler);
        m_properties = CmsClientProperty.makeLazyCopy(handler.getOwnProperties());
    }

    /**
     * @see org.opencms.gwt.client.property.A_CmsPropertyEditor#buildFields()
     */
    @Override
    public void buildFields() {

        Map<String, CmsClientProperty> ownProps = m_handler.getOwnProperties();
        Map<String, CmsClientProperty> defaultFileProps = m_handler.getDefaultFileProperties();
        Map<String, CmsClientProperty> props;
        CmsUUID id = null;
        CmsDebugLog.consoleLog("buildFields -- isFolder == " + m_handler.isFolder());
        if (!m_handler.isFolder()) {
            props = ownProps;
            id = m_handler.getId();
        } else if (m_handler.getDefaultFileId() != null) {
            props = defaultFileProps;
            id = m_handler.getDefaultFileId();
        } else {
            props = ownProps;
            id = m_handler.getId();
        }
        props = CmsClientProperty.makeLazyCopy(props);
        List<String> keys = new ArrayList<String>(m_propertyConfig.keySet());
        moveToTop(keys, CmsClientProperty.PROPERTY_NAVTEXT);
        moveToTop(keys, CmsClientProperty.PROPERTY_DESCRIPTION);
        moveToTop(keys, CmsClientProperty.PROPERTY_TITLE);
        moveToTop(keys, CmsPropertyModification.FILE_NAME_PROPERTY);
        for (String propName : keys) {
            buildField(props, propName, Mode.effective, id);
        }
    }

    /**
     * @see org.opencms.gwt.client.property.A_CmsPropertyEditor#addSpecialFields()
     */
    @Override
    protected void addSpecialFields() {

        // we don't want any special fields
    }

    /**
     * @see org.opencms.gwt.client.property.A_CmsPropertyEditor#setupFieldContainer()
     */
    @Override
    protected void setupFieldContainer() {

        CmsSimpleFormFieldPanel panel = new CmsSimpleFormFieldPanel();
        m_form.setWidget(panel);
    }

    /**
     * Builds a single form field.<p>
     *
     * @param ownProps the entry's own properties
     * @param propName the property name
     * @param mode the mode which controls which kind of field will be built
     * @param id the id of the resource for which to build the field
     */
    private void buildField(
        Map<String, CmsClientProperty> ownProps,
        final String propName,
        CmsClientProperty.Mode mode,
        CmsUUID id) {

        CmsXmlContentProperty propDef = m_propertyConfig.get(propName);

        if (propDef == null) {
            String widget = CmsClientProperty.PROPERTY_TEMPLATE.equals(propName) ? "template" : "string";
            propDef = new CmsXmlContentProperty(
                propName,
                "string",
                widget,
                "",
                null,
                null,
                null,
                null,
                null,
                null,
                null);
        }

        if (mode != Mode.effective) {
            propDef = propDef.withNiceName(propName);
        }

        CmsClientProperty ownProp = ownProps.get(propName);
        CmsPathValue pathValue = CmsClientProperty.getPathValue(ownProp, mode).prepend(id + "/" + propName + "/");

        //CHECK: should fields other than NavText be really automatically allowed to be empty in navigation mode?
        CmsBasicFormField field = CmsBasicFormField.createField(
            propDef,
            pathValue.getPath(),
            this,
            Collections.<String, String> emptyMap(),
            true);

        CmsPair<String, String> defaultValueAndOrigin = getDefaultValueToDisplay(ownProp, mode);
        String defaultValue = "";
        String origin = "";
        if (defaultValueAndOrigin != null) {
            defaultValue = defaultValueAndOrigin.getFirst();
            origin = defaultValueAndOrigin.getSecond();
        }
        Widget w = (Widget)field.getWidget();
        I_CmsStringModel model = getStringModel(pathValue);
        field.bind(model);
        boolean ghost = CmsStringUtil.isEmptyOrWhitespaceOnly(pathValue.getValue());
        String initialValue = pathValue.getValue();
        if (w instanceof I_CmsHasGhostValue) {
            ((I_CmsHasGhostValue)w).setGhostValue(defaultValue, ghost);
            if (ghost) {
                initialValue = null;
            }
        }

        boolean isShowingGhost = ghost && !CmsStringUtil.isEmpty(defaultValue);

        if (isShowingGhost) {
            field.getLayoutData().put("info", origin);
        }
        if (!ghost || isShowingGhost) {
            field.getLayoutData().put(CmsPropertyPanel.LD_DISPLAY_VALUE, "true");
        }
        field.getLayoutData().put(CmsPropertyPanel.LD_PROPERTY, propName);
        m_form.addField(field, initialValue);
    }

    /**
     * Creates a string model which uses a field of a CmsClientProperty for storing its value.<p>
     *
     * @param id the structure id
     * @param propName the property id
     * @param isStructure if true, the structure value field should be used, else the resource value field
     *
     *
     * @return the new model object
     */
    private I_CmsStringModel createStringModel(final CmsUUID id, final String propName, final boolean isStructure) {

        final CmsClientProperty property = m_properties.get(propName);

        return new I_CmsStringModel() {

            private boolean m_active;

            private EventBus m_eventBus = new SimpleEventBus();

            public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {

                return m_eventBus.addHandler(ValueChangeEvent.getType(), handler);
            }

            /**
             * @see com.google.gwt.event.shared.HasHandlers#fireEvent(com.google.gwt.event.shared.GwtEvent)
             */
            public void fireEvent(GwtEvent<?> event) {

                m_eventBus.fireEvent(event);
            }

            public String getId() {

                return Joiner.on("/").join(id.toString(), propName, isStructure ? "S" : "R");
            }

            public String getValue() {

                if (isStructure) {
                    return property.getStructureValue();
                } else {
                    return property.getResourceValue();
                }
            }

            public void setValue(String value, boolean notify) {

                if (!m_active) {
                    m_active = true;
                    try {
                        String oldValue = getValue();
                        boolean changed = !Objects.equal(value, oldValue);
                        if (isStructure) {
                            property.setStructureValue(value);
                        } else {
                            property.setResourceValue(value);
                        }
                        if (notify && changed) {
                            ValueChangeEvent.fire(this, value);
                        }
                    } finally {
                        m_active = false;
                    }
                }
            }
        };
    }

    /**
     * Gets a pair of strings containing the default value to display for a given property and its source.<p>
     *
     * @param prop the property
     * @param mode the mode
     *
     * @return a pair of the form (defaultValue, origin)
     */
    private CmsPair<String, String> getDefaultValueToDisplay(CmsClientProperty prop, Mode mode) {

        if ((mode == Mode.structure) && !CmsStringUtil.isEmpty(prop.getResourceValue())) {
            String message = Messages.get().key(Messages.GUI_ORIGIN_SHARED_0);

            return CmsPair.create(prop.getResourceValue(), message);
        }
        CmsClientProperty inheritedProperty = m_handler.getInheritedProperty(prop.getName());
        if (CmsClientProperty.isPropertyEmpty(inheritedProperty)) {
            return null;
        }
        CmsPathValue pathValue = inheritedProperty.getPathValue(mode);
        String message = Messages.get().key(Messages.GUI_ORIGIN_INHERITED_1, inheritedProperty.getOrigin());
        return CmsPair.create(pathValue.getValue(), message);
    }

    /**
     * Creates a string model for a given property path value, and returns the same model if the same path value is passed in.<p>
     *
     * @param pathValue the path value
     *
     * @return the model for that path value
     */
    private I_CmsStringModel getStringModel(CmsPathValue pathValue) {

        String path = pathValue.getPath();
        I_CmsStringModel model = m_models.get(path);
        if (model == null) {
            String[] tokens = path.split("/");
            String id = tokens[0];
            String propName = tokens[1];
            boolean isStructure = tokens[2].equals("S");
            model = createStringModel(new CmsUUID(id), propName, isStructure);
            m_models.put(path, model);
        }
        return model;
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

}
