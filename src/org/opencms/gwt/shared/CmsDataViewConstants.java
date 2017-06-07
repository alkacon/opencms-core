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

package org.opencms.gwt.shared;

/**
 * Constants used by the data view widget code (client/server side).<p>
 */
public final class CmsDataViewConstants {

    /** Key used for the callback. */
    public static final String PARAM_CALLBACK = "cb";

    /** Key used for the callback argument. */
    public static final String PARAM_CALLBACK_ARG = "cbp";

    /** JSON Key used for the view class. */
    public static final String CONFIG_VIEW_CLASS = "class";

    /** JSON key used for the view configuration. */
    public static final String CONFIG_VIEW_ARG = "config";

    /** JSON key for enabling / disabling multiselect. */
    public static final String CONFIG_MULTI_SELECT = "multiselect";

    /** Name of the request parameter used to send the widget configuration from the client to the embedded Vaadin dialog. */
    public static final String PARAM_CONFIG = "config";

    /** The renderer id. */
    public static final String RENDERER_ID = "dataview";

    /** JSON key for the result. */
    public static final String KEY_RESULT = "result";

    /** JSON key for a result's id. */
    public static final String FIELD_ID = "id";

    /** JSON key for a results's title. */
    public static final String FIELD_TITLE = "title";

    /** JSON key for a result's description. */
    public static final String FIELD_DESCRIPTION = "description";

    /** JSON key for a result's additional data. */
    public static final String FIELD_DATA = "data";

    /** JSON key for the dialog name. */
    public static final String DATAVIEW_DIALOG = "org.opencms.ui.dialogs.embedded.CmsDataViewAction";

    /** JSON key for the preview option. */
    public static final String CONFIG_PREVIEW = "preview";

    /** The accessor. */
    public static final String ACCESSOR = "acc";

    /** JSON key for the icon option. */
    public static final String CONFIG_ICON = "icon";

    /** Title content field name. */
    public static final String VALUE_TITLE = "Title";

    /** Description content field name. */
    public static final String VALUE_DESCRIPTION = "Description";

    /** Id content field name. */
    public static final String VALUE_ID = "Id";

    /** Data content field name. */
    public static final String VALUE_DATA = "Data";

    /** Default constructor. */
    private CmsDataViewConstants() {
        // do nothing
    }

}
