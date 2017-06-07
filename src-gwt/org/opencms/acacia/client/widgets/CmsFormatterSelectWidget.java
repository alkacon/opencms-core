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

package org.opencms.acacia.client.widgets;

import org.opencms.acacia.client.css.I_CmsWidgetsLayoutBundle;
import org.opencms.acacia.shared.CmsEntity;
import org.opencms.ade.contenteditor.client.CmsContentEditor;
import org.opencms.ade.contenteditor.client.I_CmsEntityChangeListener;
import org.opencms.ade.contenteditor.client.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.input.CmsSelectBox;
import org.opencms.gwt.client.util.CmsDebugLog;

import java.util.Map.Entry;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;

/**
 * Select widget which uses other values from the content as select options.<p>
 *
 * This works as follows: The widget is given a configuration consisting of three pipe-separated OpenCms content value paths.
 * The first path is used to select a set of nested content values. The second and third paths are relative to the first path
 * and are used to select a select option and a select option display text from the nested contents matching the first path.
 * Note that if you omit indexes on a component of the first path, all indexes will be matched.
 *
 * The widget attaches event listeners to the editor so it can dynamically update the list of select options when the content changes.
 */
public class CmsFormatterSelectWidget extends Composite implements I_CmsEditWidget {

    /** Path components of the path used to select the option value. */
    private String[] m_valuePath;

    /** The global select box. */
    protected CmsSelectBox m_selectBox = new CmsSelectBox();

    /** Value of the activation. */
    private boolean m_active = true;

    private CmsSelectConfigurationParser m_optionsDefault;

    private CmsSelectConfigurationParser m_optionsAllRemoved;

    /**
     * Creates a new widget instance.<p>
     *
     * @param configuration the widget configuration
     */
    public CmsFormatterSelectWidget(String configuration) {

        CmsDebugLog.consoleLog("Configuration of formatter-select-widget: " + configuration);
        String[] configParts = configuration.split("\\|\\|");
        if (configParts.length != 3) {
            //TODO: Throw exception
            CmsDebugLog.consoleLog("There were " + configParts.length + " configuration parts. 3 were expected.");
        }
        m_valuePath = splitPath(configParts[0]);
        CmsDebugLog.consoleLog("The first config part (xpath): " + configParts[0]);
        m_optionsDefault = new CmsSelectConfigurationParser(configParts[1]);
        CmsDebugLog.consoleLog("The second config part (default opts): " + configParts[1]);
        CmsDebugLog.consoleLog("Read " + m_optionsDefault.getOptions().size() + " Default options.");
        m_optionsAllRemoved = new CmsSelectConfigurationParser(configParts[2]);
        CmsDebugLog.consoleLog("The third config part (all-removed opts): " + configParts[2]);
        CmsDebugLog.consoleLog("Read " + m_optionsAllRemoved.getOptions().size() + " 'All removed' options.");

        // Place the check above the box using a vertical panel.
        m_selectBox.addStyleName(I_CmsWidgetsLayoutBundle.INSTANCE.widgetCss().selectBoxPanel());
        m_selectBox.setPopupResize(false);
        // add some styles to parts of the selectbox.
        m_selectBox.getOpener().addStyleName(I_CmsWidgetsLayoutBundle.INSTANCE.widgetCss().selectBoxSelected());
        m_selectBox.getSelectorPopup().addStyleName(I_CmsLayoutBundle.INSTANCE.globalWidgetCss().selectBoxPopup());
        m_selectBox.addValueChangeHandler(new ValueChangeHandler<String>() {

            public void onValueChange(ValueChangeEvent<String> event) {

                fireChangeEvent();

            }

        });

        update(CmsContentEditor.getEntity());
        initWidget(m_selectBox);
    }

    /**
     * @see com.google.gwt.event.dom.client.HasFocusHandlers#addFocusHandler(com.google.gwt.event.dom.client.FocusHandler)
     */
    public HandlerRegistration addFocusHandler(FocusHandler handler) {

        return addDomHandler(handler, FocusEvent.getType());
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {

        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * Represents a value change event.<p>
     * Please edit the blog entry text.
     */
    public void fireChangeEvent() {

        ValueChangeEvent.fire(this, m_selectBox.getFormValueAsString());

    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    public String getValue() {

        return m_selectBox.getFormValueAsString();
    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#isActive()
     */
    public boolean isActive() {

        return m_active;
    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#onAttachWidget()
     */
    public void onAttachWidget() {

        super.onAttach();
    }

    /**
     * @see com.google.gwt.user.client.ui.Widget#onLoad()
     */
    @Override
    public void onLoad() {

        update(CmsContentEditor.getEntity());

        CmsContentEditor.addEntityChangeListener(new I_CmsEntityChangeListener() {

            public void onEntityChange(CmsEntity entity) {

                update(CmsContentEditor.getEntity());
            }
        }, null);
    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#owns(com.google.gwt.dom.client.Element)
     */
    public boolean owns(Element element) {

        // TODO implement this in case we want the delete behavior for optional fields
        return false;

    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#setActive(boolean)
     */
    public void setActive(boolean active) {

        // check if value change. If not do nothing.
        if (m_active == active) {
            return;
        }
        // set new value.
        m_active = active;
        // set the new value to the selectbox.
        m_selectBox.setEnabled(active);
        // fire change event if necessary.
        if (active) {
            fireChangeEvent();
        }

    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#setName(java.lang.String)
     */
    public void setName(String name) {

        // no input field so nothing to do

    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    public void setValue(String value) {

        setValue(value, false);

    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
     */
    public void setValue(String value, boolean fireEvents) {

        m_selectBox.setFormValueAsString(value);
        if (fireEvents) {
            fireChangeEvent();
        }

    }

    /**
     * Updates the select options from the given entity.<p>
     *
     * @param entity a top-level content entity
     */
    public void update(CmsEntity entity) {

        String removeAllFlag = CmsEntity.getValueForPath(entity, m_valuePath);

        boolean allRemoved = Boolean.valueOf(removeAllFlag).booleanValue();

        replaceOptions(allRemoved ? m_optionsAllRemoved : m_optionsDefault);
    }

    /**
     * Replaces the select options with the given options.<p>
     *
     * @param options the map of select options (keys are option values, values are option descriptions)
     */
    private void replaceOptions(CmsSelectConfigurationParser parser) {

        CmsDebugLog.consoleLog("Replacing options. Should have " + parser.getOptions().size() + " new options.");
        String oldValue = m_selectBox.getFormValueAsString();
        // set the help info first!!
        for (Entry<String, String> helpEntry : parser.getHelpTexts().entrySet()) {
            m_selectBox.setTitle(helpEntry.getKey(), helpEntry.getValue());
        }
        //set value and option to the combo box.
        m_selectBox.setItems(parser.getOptions());
        //if one entrance is declared for default.
        if (parser.getDefaultValue() != null) {
            //set the declared value selected.
            m_selectBox.selectValue(parser.getDefaultValue());
            //TODO?: m_defaultValue = parser.getDefaultValue();
        }
        CmsDebugLog.consoleLog("Finished replacing ... set selection");

        m_selectBox.setFormValueAsString(oldValue);
        CmsDebugLog.consoleLog("Selection set.");

    }

    /**
     * Splits a path into components.<p>
     *
     * @param path the path to split
     * @return the path components
     */
    private String[] splitPath(String path) {

        path = path.replaceAll("^/", "").replaceAll("/$", "");
        return path.split("/");
    }

}
