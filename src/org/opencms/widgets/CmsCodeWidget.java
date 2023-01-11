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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.gwt.shared.I_CmsAutoBeanFactory;
import org.opencms.gwt.shared.I_CmsCodeMirrorClientConfiguration;
import org.opencms.i18n.CmsMessages;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.I_CmsXmlContentHandler.DisplayType;
import org.opencms.xml.types.A_CmsXmlContentValue;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;

import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;

/**
 * Widget for editing source code.
 *
 * <p>The current implementation uses the CodeMirror  editor on the client side.
 *
 * <p>The configuration string is a JSON string (in the form parseable by org.opencms.json.JSONObject) with the following possible optional attributes:
 * <ul>
 * <li>mode: the initially selected editor mode (text|javascript|jsp|java|html|xml)
 * <li>height: the height in pixels (if 'grow' is set to true, this is used as minimum height instead).
 * </ul>
 */
public class CmsCodeWidget extends A_CmsWidget implements I_CmsADEWidget {

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsCodeWidget.class);

    /** The factory for the client-side configuration. */
    private static I_CmsAutoBeanFactory m_configFactory = AutoBeanFactorySource.create(I_CmsAutoBeanFactory.class);

    /**
     * Creates a new instance.
     */
    public CmsCodeWidget() {

        this("{}");

    }

    /**
     * Creates a new instance.
     *
     * @param configuration the configuration
     */
    public CmsCodeWidget(String configuration) {

        setConfiguration(configuration);
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getConfiguration(org.opencms.file.CmsObject, org.opencms.xml.types.A_CmsXmlContentValue, org.opencms.i18n.CmsMessages, org.opencms.file.CmsResource, java.util.Locale)
     */
    public String getConfiguration(
        CmsObject cms,
        A_CmsXmlContentValue contentValue,
        CmsMessages messages,
        CmsResource resource,
        Locale contentLocale) {

        try {
            JSONObject serverConfig = new JSONObject();
            if (!CmsStringUtil.isEmptyOrWhitespaceOnly(getConfiguration())) {
                try {
                    serverConfig = new JSONObject(getConfiguration());
                } catch (JSONException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
            String mode = serverConfig.optString("mode", "html");
            String heightStr = serverConfig.optString("height");
            Integer height = null;
            if ((heightStr != null) && !("none".equals(heightStr))) {
                try {
                    height = Integer.valueOf(heightStr);
                } catch (NumberFormatException e) {
                    LOG.warn(e.getLocalizedMessage(), e);
                }
            }
            AutoBean<I_CmsCodeMirrorClientConfiguration> clientConfig = m_configFactory.createConfiguration();
            Locale userLocale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
            clientConfig.as().setPhrasesJSON(getPhrases(userLocale));
            clientConfig.as().setHeight(height);
            clientConfig.as().setStartMode(mode);
            String clientConfigJsonString = AutoBeanCodex.encode(clientConfig).getPayload();
            return clientConfigJsonString;
        } catch (Exception e) {
            LOG.error(e);
            return "{}";
        }
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getCssResourceLinks(org.opencms.file.CmsObject)
     */
    public List<String> getCssResourceLinks(CmsObject cms) {

        return null;
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getDefaultDisplayType()
     */
    public DisplayType getDefaultDisplayType() {

        return DisplayType.wide;
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogWidget(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    public String getDialogWidget(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        return null;
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getInitCall()
     */
    public String getInitCall() {

        return null;
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getJavaScriptResourceLinks(org.opencms.file.CmsObject)
     */
    public List<String> getJavaScriptResourceLinks(CmsObject cms) {

        return null;
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getWidgetName()
     */
    public String getWidgetName() {

        return CmsCodeWidget.class.getName();
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#isInternal()
     */
    public boolean isInternal() {

        return true;
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#newInstance()
     */
    public I_CmsWidget newInstance() {

        return new CmsCodeWidget(getConfiguration());
    }

    /**
     * Gets the i18n phrases for CodeMirror.
     *
     * @param userLocale the current locale
     * @return the phrases as JSON
     */
    private String getPhrases(Locale userLocale) {

        try {
            InputStream stream = null;
            stream = getClass().getResourceAsStream("codemirror_phrases_" + userLocale.getLanguage() + ".json");
            if (stream != null) {
                try {
                    byte[] data = CmsFileUtil.readFully(stream, false);
                    String result = new String(data, StandardCharsets.UTF_8);
                    @SuppressWarnings("unused")
                    JSONObject dummy = new JSONObject(result); // throw and log exception if invalid JSON
                    return result;
                } finally {
                    stream.close();
                }
            } else {
                return "{}";
            }
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            return "{}";
        }
    }

}
