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

package org.opencms.gwt;

import org.opencms.configuration.CmsDefaultUserSettings;
import org.opencms.configuration.preferences.I_CmsPreference;
import org.opencms.file.CmsObject;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.gwt.shared.CmsUserSettingsBean;
import org.opencms.i18n.CmsMultiMessages;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsMacroResolver;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.xml.content.CmsXmlContentProperty;
import org.opencms.xml.content.CmsXmlContentPropertyHelper;

import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

/**
 * Helper class to deal with loading and saving user preferences from the ADE user interface.<p>
 */
public class CmsClientUserSettingConverter {

    /**
     * Subclass of the normal action element which can be created even if we are not being called from a JSP.<p>
     */
    class NoJspActionElement extends CmsJspActionElement {

        /** The CMS object to use. */
        private CmsObject m_setCms;

        /**
         * Creates a new instance.<p>
         *
         * @param cms the CMS context to use
         * @param req the current request
         * @param res the current response
         */
        public NoJspActionElement(CmsObject cms, HttpServletRequest req, HttpServletResponse res) {

            super(null, req, res);
            m_setCms = cms;

        }

        /**
         * @see org.opencms.jsp.CmsJspBean#getCmsObject()
         */
        @Override
        public CmsObject getCmsObject() {

            return m_setCms;
        }

        /**
         * @see org.opencms.jsp.CmsJspBean#handleMissingFlexController()
         */
        @Override
        protected void handleMissingFlexController() {

            // ignore
        }

    }

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsClientUserSettingConverter.class);

    /** The CMS context to use. */
    private CmsObject m_cms;

    /** The current preferences. */
    private CmsDefaultUserSettings m_currentPreferences;

    /** The macro resolver used for macros in preference property definitions. */
    private CmsMacroResolver m_macroResolver = new CmsMacroResolver();

    /** The current request. */
    private HttpServletRequest m_request;

    /**
     * Creates a new instance.<p>
     *
     * @param cms the current CMS context
     * @param request the current request
     * @param response the current response
     */
    public CmsClientUserSettingConverter(CmsObject cms, HttpServletRequest request, HttpServletResponse response) {

        m_cms = cms;
        m_request = request;
        m_currentPreferences = new CmsDefaultUserSettings();
        m_currentPreferences.init(cms.getRequestContext().getCurrentUser());
        Locale locale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
        CmsMultiMessages messages = new CmsMultiMessages(locale);
        messages.addMessages(OpenCms.getWorkplaceManager().getMessages(locale));
        messages.addMessages(org.opencms.workplace.commons.Messages.get().getBundle(locale));
        m_macroResolver.setMessages(messages);

    }

    /**
     * Loads the current user's preferences into a CmsUserSettingsBean.<p>
     *
     * @return the bean representing the current user's preferences
     */
    public CmsUserSettingsBean loadSettings() {

        CmsUserSettingsBean result = new CmsUserSettingsBean();
        CmsDefaultUserSettings currentSettings = new CmsDefaultUserSettings();
        currentSettings.init(m_currentPreferences.getUser());
        for (I_CmsPreference pref : OpenCms.getWorkplaceManager().getDefaultUserSettings().getPreferences().values()) {
            String tab = pref.getTab();
            if (CmsGwtConstants.TAB_HIDDEN.equals(tab) || pref.isDisabled(m_cms)) {
                continue;
            }
            CmsXmlContentProperty prop2 = pref.getPropertyDefinition(m_cms);
            String value = pref.getValue(currentSettings);
            CmsXmlContentProperty resolvedProp = CmsXmlContentPropertyHelper.resolveMacrosInProperty(
                prop2.withDefaultWidget("string"),
                m_macroResolver);
            result.addSetting(value, resolvedProp, CmsGwtConstants.TAB_BASIC.equals(tab));
        }
        return result;
    }

    /**
     * Saves the given user preference values.<p>
     *
     * @param settings the user preference values to save
     *
     * @throws Exception if something goes wrong
     */
    public void saveSettings(Map<String, String> settings) throws Exception {

        for (Map.Entry<String, String> entry : settings.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            saveSetting(key, value);
        }
        m_currentPreferences.save(m_cms);
        CmsWorkplace.updateUserPreferences(m_cms, m_request);
    }

    /**
     * Saves an individual user preference value.<p>
     *
     * @param key the key of the user preference
     * @param value the value of the user preference
     *
     * @throws Exception if something goes wrong
     */
    private void saveSetting(String key, String value) throws Exception {

        Map<String, I_CmsPreference> prefs = OpenCms.getWorkplaceManager().getDefaultUserSettings().getPreferences();
        if (prefs.containsKey(key)) {
            prefs.get(key).setValue(m_currentPreferences, value);
        } else {
            LOG.error("Can't save user setting '" + key + "'");
        }
    }

}
