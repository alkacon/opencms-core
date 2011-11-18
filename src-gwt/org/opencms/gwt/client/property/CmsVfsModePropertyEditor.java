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

package org.opencms.gwt.client.property;

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.ui.input.CmsSelectBox;
import org.opencms.gwt.client.ui.input.I_CmsHasGhostValue;
import org.opencms.gwt.client.ui.input.I_CmsStringModel;
import org.opencms.gwt.client.ui.input.form.CmsBasicFormField;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsDomUtil.Style;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.gwt.shared.property.CmsClientProperty;
import org.opencms.gwt.shared.property.CmsClientProperty.Mode;
import org.opencms.gwt.shared.property.CmsPathValue;
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
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
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
public class CmsVfsModePropertyEditor extends A_CmsPropertyEditor {

    /** The interval used for updating the height. */
    public static final int UPDATE_HEIGHT_INTERVAL = 200;

    /** The map of tab names. */
    private static BiMap<CmsClientProperty.Mode, String> tabs;

    /** The map of models of the fields. */
    Map<String, I_CmsStringModel> m_models = new HashMap<String, I_CmsStringModel>();

    /** The previous tab index. */
    private int m_oldTabIndex = -1;

    /** The panel used for editing the properties. */
    private CmsPropertyPanel m_panel;

    /** The properties of the entry. */
    private Map<String, CmsClientProperty> m_properties;

    /** Flag which indicates whether the resource properties should be editable. */
    private boolean m_showResourceProperties;

    /**
     * Creates a new sitemap entry editor instance for the VFS mode.<p>
     * 
     * @param propConfig the property configuration 
     * @param handler the sitemap entry editor handler 
     */
    public CmsVfsModePropertyEditor(Map<String, CmsXmlContentProperty> propConfig, I_CmsPropertyEditorHandler handler) {

        super(propConfig, handler);
        m_dialog.setCaption(null);
        m_dialog.removePadding();
        m_properties = CmsClientProperty.makeLazyCopy(handler.getOwnProperties());
    }

    static {
        tabs = HashBiMap.create();
        tabs.put(Mode.effective, CmsPropertyPanel.TAB_SIMPLE);
        tabs.put(Mode.structure, CmsPropertyPanel.TAB_INDIVIDUAL);
        tabs.put(Mode.resource, CmsPropertyPanel.TAB_SHARED);
    }

    /**
     * @see org.opencms.gwt.client.property.A_CmsPropertyEditor#buildFields()
     */
    @Override
    public void buildFields() {

        internalBuildConfiguredFields();
        internalBuildOtherFields();
    }

    /**
     * Sets the "show resource properties" flag which controls whether the resource value fields should be built.<p>
     * 
     * @param showResourceProperties if true, the resource value fields will be build 
     */
    public void setShowResourceProperties(boolean showResourceProperties) {

        m_showResourceProperties = showResourceProperties;
    }

    /**
     * @see org.opencms.gwt.client.property.A_CmsPropertyEditor#start()
     */
    @Override
    public void start() {

        super.start();
        Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {

            public boolean execute() {

                if (!getPropertyPanel().getTabPanel().isAttached() || !getPropertyPanel().getTabPanel().isVisible()) {
                    return false;
                }
                updateHeight();
                return true;
            }
        }, UPDATE_HEIGHT_INTERVAL);
    }

    /**
     * Returns the property panel.<p>
     * 
     * @return the property panel
     */
    protected CmsPropertyPanel getPropertyPanel() {

        return m_panel;
    }

    /**
     * Method which is called when the tab is switched.<p>
     * 
     * @param toTab the tab to which the user is switching 
     */
    protected void handleSwitchTab(int toTab) {

        switch (toTab) {
            case 0:
                rebuildSimpleTab();
                break;
            case 1:
                rebuildIndividualTab();
                break;
            case 2:
                rebuildSharedTab();
                break;
            default:
                break;
        }
        if (m_disabledReason != null) {
            disableInput(m_disabledReason);
        } else {
            m_form.validateAllFields();
        }
    }

    /**
     * @see org.opencms.gwt.client.property.A_CmsPropertyEditor#setupFieldContainer()
     */
    @Override
    protected void setupFieldContainer() {

        CmsListInfoBean info = m_handler.getPageInfo();
        m_panel = new CmsPropertyPanel(m_showResourceProperties, info);
        String modeClass = m_handler.getModeClass();
        if (modeClass != null) {
            m_panel.addStyleName(modeClass);
        }
        m_panel.addBeforeSelectionHandler(new BeforeSelectionHandler<Integer>() {

            public void onBeforeSelection(BeforeSelectionEvent<Integer> event) {

                int target = event.getItem().intValue();
                handleSwitchTab(target);
            }
        });

        m_form.setWidget(m_panel);
    }

    /**
     * Updates the panel height depending on the content of the current tab.<p>
     */
    protected void updateHeight() {

        int tabIndex = m_panel.getTabPanel().getSelectedIndex();
        boolean changedTab = tabIndex != m_oldTabIndex;
        m_oldTabIndex = tabIndex;
        Widget tabWidget = m_panel.getTabPanel().getWidget(tabIndex);
        Element tabElement = tabWidget.getElement();
        Element innerElement = tabElement.getFirstChildElement();
        int contentHeight = CmsDomUtil.getCurrentStyleInt(innerElement, Style.height);
        int spaceLeft = m_dialog.getAvailableHeight(0);
        int newHeight = Math.min(spaceLeft, contentHeight) + 50;
        if ((m_panel.getTabPanel().getOffsetHeight() != newHeight) || changedTab) {
            m_panel.getTabPanel().setHeight(newHeight + "px");
            if (CmsCoreProvider.get().isIe7()) {
                int selectedIndex = m_panel.getTabPanel().getSelectedIndex();
                Widget widget = m_panel.getTabPanel().getWidget(selectedIndex);
                widget.setHeight((newHeight - 45) + "px");
                m_dialog.center();
            }
        }

    }

    /**
     * Builds a single form field.<p>
     * 
     * @param ownProps the entry's own properties 
     * @param propName the property name
     * @param mode the mode which controls which kind of field will be built  
     */
    private void buildField(Map<String, CmsClientProperty> ownProps, final String propName, CmsClientProperty.Mode mode) {

        String entryId = m_handler.getId().toString();
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

        CmsClientProperty ownProp = m_properties.get(propName);
        CmsPathValue pathValue = CmsClientProperty.getPathValue(ownProp, mode).prepend(entryId + "/" + propName + "/");

        //CHECK: should fields other than NavText be really automatically allowed to be empty in navigation mode?
        String tab = tabs.get(mode);
        CmsBasicFormField field = CmsBasicFormField.createField(
            propDef,
            pathValue.getPath() + "#" + tab,
            this,
            Collections.singletonMap(
                CmsSelectBox.NO_SELECTION_TEXT,
                Messages.get().key(Messages.GUI_SELECTBOX_UNSELECTED_1)),
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
        m_form.addField(tab, field, initialValue);
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
     * Builds the fields for the configured properties in the first tab.<p>
     */
    private void internalBuildConfiguredFields() {

        Map<String, CmsClientProperty> ownProps = m_handler.getOwnProperties();
        List<String> keys = new ArrayList<String>(m_propertyConfig.keySet());
        moveToTop(keys, CmsClientProperty.PROPERTY_NAVTEXT);
        moveToTop(keys, CmsClientProperty.PROPERTY_DESCRIPTION);
        moveToTop(keys, CmsClientProperty.PROPERTY_TITLE);
        for (String propName : keys) {
            buildField(ownProps, propName, Mode.effective);
        }
    }

    /**
     * 
     * Builds the fields for a given mode.<p>
     * 
     * @param mode the mode 
     */
    private void internalBuildFields(Mode mode) {

        Map<String, CmsClientProperty> ownProps = m_handler.getOwnProperties();
        for (String propName : m_handler.getAllPropertyNames()) {
            buildField(ownProps, propName, mode);
        }
    }

    /**
     * Builds the fields for the "structure" and "resource" tabs.<p>
     */
    private void internalBuildOtherFields() {

        internalBuildFields(Mode.structure);
        if (m_showResourceProperties) {
            internalBuildFields(Mode.resource);
        }
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
     * Rebuilds the "individual" tab.<p>
     */
    private void rebuildIndividualTab() {

        m_form.removeGroup(CmsPropertyPanel.TAB_INDIVIDUAL);
        CmsPropertyPanel panel = ((CmsPropertyPanel)m_form.getWidget());
        panel.clearTab(CmsPropertyPanel.TAB_INDIVIDUAL);
        internalBuildFields(Mode.structure);
        m_form.renderGroup(CmsPropertyPanel.TAB_INDIVIDUAL);
    }

    /**
     * Rebuilds the "shared" tab.<p>
     */
    private void rebuildSharedTab() {

        m_form.removeGroup(CmsPropertyPanel.TAB_SHARED);
        CmsPropertyPanel panel = ((CmsPropertyPanel)m_form.getWidget());
        panel.clearTab(CmsPropertyPanel.TAB_SHARED);
        internalBuildFields(Mode.resource);
        m_form.renderGroup(CmsPropertyPanel.TAB_SHARED);
    }

    /**
     * Rebuilds the simple tab.<p>
     */
    private void rebuildSimpleTab() {

        m_form.removeGroup(CmsPropertyPanel.TAB_SIMPLE);
        CmsPropertyPanel panel = ((CmsPropertyPanel)m_form.getWidget());
        panel.clearTab(CmsPropertyPanel.TAB_SIMPLE);
        if (m_handler.hasEditableName()) {
            m_form.addField(CmsPropertyPanel.TAB_SIMPLE, createUrlNameField());
        }
        internalBuildConfiguredFields();
        m_form.renderGroup(CmsPropertyPanel.TAB_SIMPLE);
    }
}
