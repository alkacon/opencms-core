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

package org.opencms.widgets;

import org.opencms.ade.contenteditor.shared.CmsComplexWidgetData;
import org.opencms.file.CmsObject;
import org.opencms.gwt.shared.CmsDataViewConstants;
import org.opencms.json.JSONObject;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.widgets.dataview.I_CmsDataView;

import org.apache.commons.logging.Log;

/**
 * Complex widget for opening selecting data from a data source implementing the I_CmsDataView interface.<p>
 *
 * This widget can only be used
 */
public class CmsDataViewWidget implements I_CmsComplexWidget {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDataViewWidget.class);

    /** The widget configuration. */
    private String m_config;

    /**
     * Default constructor.<p>
     */
    public CmsDataViewWidget() {
        this("");
    }

    /**
     * Creates a new instance.<p>
     *
     * @param config the widget configuration
     */
    public CmsDataViewWidget(String config) {
        m_config = config;
    }

    /**
     * @see org.opencms.widgets.I_CmsComplexWidget#configure(java.lang.String)
     */
    public I_CmsComplexWidget configure(String configuration) {

        validateConfiguration(configuration);
        return new CmsDataViewWidget(configuration);
    }

    /**
     * @see org.opencms.widgets.I_CmsComplexWidget#getName()
     */
    public String getName() {

        return "dataview";
    }

    /**
     * @see org.opencms.widgets.I_CmsComplexWidget#getWidgetData(org.opencms.file.CmsObject)
     */
    public CmsComplexWidgetData getWidgetData(CmsObject cms) {

        String configToUse = m_config;
        try {
            JSONObject json = new JSONObject(m_config);
            String icon = json.optString(CmsDataViewConstants.CONFIG_ICON);
            if (icon != null) {
                String iconLink = OpenCms.getLinkManager().substituteLinkForUnknownTarget(cms, icon);
                json.put(CmsDataViewConstants.CONFIG_ICON, iconLink);
                configToUse = json.toString();
            }
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }

        return new CmsComplexWidgetData(CmsDataViewConstants.RENDERER_ID, configToUse, null);
    }

    /**
     * Validates the configuration.<p>
     *
     * @param configuration the configuration
     */
    public void validateConfiguration(String configuration) {

        try {
            JSONObject json = new JSONObject(configuration);
            String className = json.optString(CmsDataViewConstants.CONFIG_VIEW_CLASS);

            Class<?> cls = Class.forName(className, false, getClass().getClassLoader());
            if (!I_CmsDataView.class.isAssignableFrom(cls)) {
                throw new IllegalArgumentException(
                    "Class " + cls.getName() + " does not implement " + I_CmsDataView.class.getName());
            }
        } catch (Exception e) {
            throw new CmsWidgetConfigurationException(e);
        }

    }

}
