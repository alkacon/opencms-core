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

import org.opencms.acacia.client.I_CmsWidgetFactory;
import org.opencms.ade.contenteditor.widgetregistry.client.WidgetRegistry;
import org.opencms.gwt.client.I_CmsHasInit;
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.shared.CmsGwtConstants;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import com.google.gwt.dom.client.Element;

import elemental2.core.Global;
import jsinterop.base.Any;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

/**
 * Factory to generate basic input widget.<p>
 */
public class CmsTextareaWidgetFactory implements I_CmsWidgetFactory, I_CmsHasInit {

    /** The widget name. */
    private static final String WIDGET_NAME = "org.opencms.widgets.CmsTextareaWidget";

    /** Configuration for enabling typography button in inline mode only. */
    public static final String CONF_TYPOGRAPHY = "typography";

    /** Array of both possible typography options. */
    public static final List<String> TYPOGRAPHY_OPTIONS = Arrays.asList(
        CONF_TYPOGRAPHY,
        CmsTextareaWidget.CONF_AUTO_TYPOGRAPHY);

    /**
     * Initializes this class.<p>
     */
    public static void initClass() {

        WidgetRegistry.getInstance().registerWidgetFactory(WIDGET_NAME, new CmsTextareaWidgetFactory());
    }

    /**
     * @see org.opencms.acacia.client.I_CmsWidgetFactory#createFormWidget(java.lang.String)
     */
    public I_CmsFormEditWidget createFormWidget(String configuration) {

        return new CmsFormWidgetWrapper(new CmsTextareaWidget(configuration));
    }

    /**
     * @see org.opencms.acacia.client.I_CmsWidgetFactory#createInlineWidget(java.lang.String, com.google.gwt.dom.client.Element)
     */
    public I_CmsEditWidget createInlineWidget(String configuration, Element element) {

        String typografLocale = null;
        try {
            JsPropertyMap<Object> configObj = Js.cast(Global.JSON.parse(configuration));

            Any config = configObj.getAsAny(CmsGwtConstants.JSON_TEXTAREA_CONFIG);
            Any locale = configObj.getAsAny(CmsGwtConstants.JSON_TEXTAREA_LOCALE);
            if ((config != null) && hasTypography(config.asString())) {
                if ((locale != null) && CmsTypografUtil.Typograf.hasLocale(locale.asString())) {
                    typografLocale = locale.asString();
                }
            }
        } catch (Exception e) {
            CmsDebugLog.consoleLog(e.getMessage());
        }
        CmsTinyMCEWidget result = new CmsTinyMCEWidget(element, CmsTinyMCEWidget.NO_HTML_EDIT);
        result.setTypografLocale(typografLocale);
        return result;
    }

    /**
     * Checks if a typography option is set.
     *
     * @param config the config string
     * @return true if a typography option is set
     */
    private boolean hasTypography(String config) {

        boolean result = Stream.of(config.split("\\|")).anyMatch(token -> TYPOGRAPHY_OPTIONS.contains(token.trim()));
        return result;
    }

}
