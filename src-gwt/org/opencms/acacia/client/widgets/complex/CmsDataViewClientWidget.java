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

package org.opencms.acacia.client.widgets.complex;

import org.opencms.acacia.client.css.I_CmsLayoutBundle;
import org.opencms.acacia.client.widgets.complex.CmsDataViewPreviewWidget.I_ImageProvider;
import org.opencms.acacia.shared.CmsEntity;
import org.opencms.gwt.client.util.CmsEmbeddedDialogHandler;
import org.opencms.gwt.shared.CmsDataViewConstants;
import org.opencms.gwt.shared.CmsDataViewParamEncoder;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * The client-side widget for data view items.<p>
 *
 * This widget by itself doesn't allow you to edit the information directly,
 * instead it opens a popup window when clicked, in which the user can then select items
 * from the configured data view.<p>
 *
 */
public class CmsDataViewClientWidget extends Composite {

    /** The name of the callback. */
    public static final String CALLBACK = "cmsDataViewCallback";

    /** Map of accessors, with their IDs as keys. */
    private static Map<String, CmsDataViewValueAccessor> accessors = Maps.newHashMap();

    /** The entity for which the widget was instantiated. */
    private CmsEntity m_entity;

    /** The configuration string. */
    private String m_config;

    /** The JSON configuration. */
    private JSONObject m_jsonConfig;

    /** Object used to read values from  or write them back to the editor. */
    private CmsDataViewValueAccessor m_valueAccessor;

    /**
     * Creates a new instance.<p>
     *
     * @param valueAccessor object used to read/write values from and to the editor
     * @param configString the configuration string
     */
    public CmsDataViewClientWidget(CmsDataViewValueAccessor valueAccessor, String configString) {
        m_valueAccessor = valueAccessor;
        m_valueAccessor.setWidget(this);

        accessors.put(valueAccessor.getId(), valueAccessor);
        m_config = configString;

        m_jsonConfig = JSONParser.parseLenient(configString).isObject();

        Widget widget = createWidget();
        initWidget(widget);
        addStyleName(I_CmsLayoutBundle.INSTANCE.form().widget());
        widget.getElement().getStyle().setMarginLeft(-20, Unit.PX);

        ensureCallback(CALLBACK);
        addDomHandler(new ClickHandler() {

            @SuppressWarnings("synthetic-access")
            public void onClick(ClickEvent event) {

                m_jsonConfig.put(CmsDataViewConstants.PARAM_CALLBACK, new JSONString(CALLBACK));
                m_jsonConfig.put(
                    CmsDataViewConstants.PARAM_CALLBACK_ARG,
                    new JSONString("{" + CmsDataViewConstants.ACCESSOR + ": '" + m_valueAccessor.getId() + "'}"));

                CmsEmbeddedDialogHandler handler = new CmsEmbeddedDialogHandler();
                Map<String, String> additionalParams = new HashMap<String, String>();
                additionalParams.put(
                    CmsDataViewConstants.PARAM_CONFIG,
                    CmsDataViewParamEncoder.encodeString(m_jsonConfig.toString()));
                handler.openDialog(
                    CmsDataViewConstants.DATAVIEW_DIALOG,
                    null,
                    new ArrayList<CmsUUID>(),
                    additionalParams);
            }
        }, ClickEvent.getType());
    }

    /**
     * Ensures that the javascript callback is installed.<p>
     *
     * @param name the name of the callback function
     */
    public static native void ensureCallback(String name) /*-{
        if (!($wnd[name])) {
            $wnd[name] = function(x) {
                var json = JSON.stringify(x);
                @org.opencms.acacia.client.widgets.complex.CmsDataViewClientWidget::handleResult(Ljava/lang/String;)(json);
            };
        }
    }-*/;

    /**
     * Handles the JSON results returned by the embedded Vaadin dialog.<p>
     *
     * @param json the JSON results
     */
    public static void handleResult(String json) {

        JSONObject jsonObj = JSONParser.parseLenient(json).isObject();
        JSONArray results = jsonObj.get(CmsDataViewConstants.KEY_RESULT).isArray();
        String accessorId = jsonObj.get(CmsDataViewConstants.ACCESSOR).isString().stringValue();
        CmsDataViewValueAccessor accessor = accessors.get(accessorId);
        List<CmsDataViewValue> values = Lists.newArrayList();
        for (int i = 0; i < results.size(); i++) {
            JSONObject singleResult = results.get(i).isObject();
            String id = singleResult.get(CmsDataViewConstants.FIELD_ID).isString().stringValue();
            String title = singleResult.get(CmsDataViewConstants.FIELD_TITLE).isString().stringValue();
            String description = singleResult.get(CmsDataViewConstants.FIELD_DESCRIPTION).isString().stringValue();
            String data = singleResult.get(CmsDataViewConstants.FIELD_DATA).isString().stringValue();
            CmsDataViewValue value = new CmsDataViewValue(id, title, description, data);
            values.add(value);
        }
        accessor.replaceValue(values);
    }

    /**
     * Creates the correct widget based on the configuration.<p>
     *
     * @return the new widget
     */
    Widget createWidget() {

        if (isTrue(m_jsonConfig, CmsDataViewConstants.CONFIG_PREVIEW)) {
            return new CmsDataViewPreviewWidget(
                m_config,
                m_valueAccessor,
                new CmsDataViewPreviewWidget.ContentImageLoader());
        } else {
            I_ImageProvider provider = null;
            CmsDataViewValue val = m_valueAccessor.getValue();
            JSONValue iconVal = m_jsonConfig.get(CmsDataViewConstants.CONFIG_ICON);
            if ((iconVal != null) && (iconVal.isString() != null)) {
                provider = new CmsDataViewPreviewWidget.SimpleImageLoader(iconVal.isString().stringValue());
            }
            return new CmsDataViewPreviewWidget(m_config, m_valueAccessor, provider);
        }

    }

    /**
     * Checks if a property in a JSON object is either the boolean value 'true' or a string representation of that value.<p>
     *
     * @param obj the JSON object
     * @param property the property name
     * @return true if the value represents the boolean 'true'
     */
    private boolean isTrue(JSONObject obj, String property) {

        JSONValue val = obj.get(property);
        if (val == null) {
            return false;
        }
        boolean stringTrue = ((val.isString() != null) && Boolean.parseBoolean(val.isString().stringValue()));
        boolean boolTrue = ((val.isBoolean() != null) && val.isBoolean().booleanValue());
        return stringTrue || boolTrue;
    }
}
