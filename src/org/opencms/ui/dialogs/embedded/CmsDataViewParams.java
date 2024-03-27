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

package org.opencms.ui.dialogs.embedded;

import org.opencms.file.CmsObject;
import org.opencms.gwt.shared.CmsDataViewConstants;
import org.opencms.gwt.shared.CmsDataViewParamEncoder;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.main.CmsLog;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.dataview.I_CmsDataView;
import org.opencms.widgets.dataview.I_CmsDataViewItem;

import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;

/**
 * Class representing the configuration passed to the Vaadin data view dialog by the client.<p>
 */
public class CmsDataViewParams {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDataViewParams.class);

    /** The callback to call with the selected items. */
    private String m_callback;

    /** Argument to pass back to the callback. */
    private String m_callbackArg;

    /** Value of multiselect option. */
    private String m_multiSelect;

    /** Value of class option. */
    private String m_viewClass;

    /** Configuration string for the data view class. */
    private String m_viewArg;

    /**
     * Creates a new instance by parsing the query string of the given URI.<p>
     *
     * @param context the dialog context
     */
    public CmsDataViewParams(I_CmsDialogContext context) {

        if (context.getParameters().get(CmsDataViewConstants.PARAM_CONFIG) != null) {
            try {
                JSONObject json = new JSONObject(CmsDataViewParamEncoder.decodeString(context.getParameters().get(CmsDataViewConstants.PARAM_CONFIG)));
                m_callback = json.optString(CmsDataViewConstants.PARAM_CALLBACK);
                m_callbackArg = json.optString(CmsDataViewConstants.PARAM_CALLBACK_ARG);
                m_viewClass = json.optString(CmsDataViewConstants.CONFIG_VIEW_CLASS);
                m_viewArg = json.optString(CmsDataViewConstants.CONFIG_VIEW_ARG);
                m_multiSelect = json.optString(CmsDataViewConstants.CONFIG_MULTI_SELECT);
            } catch (JSONException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * Creates the data view instance.<p>
     *
     * @param cms the CMS context
     * @param locale the locale
     * @return the new data view instance
     */
    public I_CmsDataView createViewInstance(CmsObject cms, Locale locale) {

        try {
            Class<?> cls = Class.forName(m_viewClass);
            Object viewObj = cls.newInstance();
            I_CmsDataView dataView = (I_CmsDataView)viewObj;
            dataView.initialize(cms, m_viewArg, locale);
            return dataView;
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            return null;
        }
    }

    /**
     * Return true if the list should have multi-selection enabled.<p>
     *
     * @return true if multi-selection should be enabled
     */
    public boolean isMultiSelect() {

        return Boolean.parseBoolean(m_multiSelect);
    }

    /**
     * Creates the script which calls the callback with the result.<p>
     *
     * @param result the list of result data items
     * @return the script to call the callback
     */
    public String prepareCallbackScript(List<I_CmsDataViewItem> result) {

        try {
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_callbackArg)) {
                m_callbackArg = "{}";
            }
            JSONObject obj = new JSONObject(m_callbackArg);
            JSONArray selection = new JSONArray();
            for (I_CmsDataViewItem item : result) {
                JSONObject singleResult = new JSONObject();
                singleResult.put(CmsDataViewConstants.FIELD_ID, item.getId());
                singleResult.put(CmsDataViewConstants.FIELD_TITLE, item.getTitle());
                singleResult.put(CmsDataViewConstants.FIELD_DESCRIPTION, item.getDescription());
                singleResult.put(CmsDataViewConstants.FIELD_DATA, item.getData());
                selection.put(singleResult);
            }
            obj.put(CmsDataViewConstants.KEY_RESULT, selection);
            String jsonString = obj.toString();
            return "parent." + m_callback + "(" + jsonString + ")";
        } catch (Exception e) {
            return null;
        }

    }

}
