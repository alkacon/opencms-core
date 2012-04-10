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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.jsp.decorator;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsException;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The CmsDecoratorConfiguration initalizes and stores the text decorations.<p>
 * 
 * It uses uses the information of one or more <code>{@link CmsDecorationDefintion}</code> to create the
 * pre- and postfixs for text decorations.
 * 
 * @since 6.1.3 
 */

public class CmsDecoratorConfiguration implements I_CmsDecoratorConfiguration {

    /** The xpath for the decoration configuration. */
    public static final String XPATH_DECORATION = "decoration";

    /** The xpath for the exclude configuration. */
    public static final String XPATH_EXCLUDE = "exclude";

    /** The xpath for the uselocale configuration. */
    public static final String XPATH_USELOCALE = "uselocale";

    /** The xpath for the filename configuration. */
    private static final String XPATH_FILENAME = "filename";

    /** The xpath for the markfirst configuration. */
    private static final String XPATH_MARKFIRST = "markfirst";

    /** The xpath for the name configuration. */
    private static final String XPATH_NAME = "name";

    /** The xpath for the posttext configuration. */
    private static final String XPATH_POSTTEXT = "posttext";

    /** The xpath for the posttextfirst configuration. */
    private static final String XPATH_POSTTEXTFIRST = "posttextfirst";

    /** The xpath for the pretext configuration. */
    private static final String XPATH_PRETEXT = "pretext";

    /** The xpath for the pretextfirst configuration. */
    private static final String XPATH_PRETEXTFIRST = "pretextfirst";

    /** The CmsObject. */
    private CmsObject m_cms;

    /** The config file. */
    private String m_configFile;

    /** The locale for extracting the configuration data. */
    private Locale m_configurationLocale = CmsLocaleManager.getLocale("en");

    /** Map of configured decorations. */
    private CmsDecorationBundle m_decorations;

    /** The list of excluded tags. */
    private List m_excludes;

    /** The locale for to build the configuration for. */
    private Locale m_locale;

    /** The list of already used  decorations. */
    private List m_usedDecorations;

    /** The list with all <code>{@link CmsDecorationDefintion}</code> instances parsed from the config file. */
    private List m_decorationDefinitions;

    /**
     * Constructor, creates a new, empty CmsDecoratorConfiguration.<p>
     *
     */
    public CmsDecoratorConfiguration() {

        m_decorations = new CmsDecorationBundle();
        m_configFile = null;
        m_cms = null;
        m_locale = null;
        m_usedDecorations = new ArrayList();
        m_excludes = new ArrayList();
        m_decorationDefinitions = new ArrayList();
    }

    /**
     * Constructor, creates a new, empty CmsDecoratorConfiguration.<p>
     * 
     * @param cms the CmsObject
     * @throws CmsException if something goes wrong
     *
     */
    public CmsDecoratorConfiguration(CmsObject cms)
    throws CmsException {

        m_decorations = new CmsDecorationBundle();
        m_configFile = null;
        m_cms = cms;
        m_locale = m_cms.getRequestContext().getLocale();
        m_usedDecorations = new ArrayList();
        m_excludes = new ArrayList();
        m_decorationDefinitions = new ArrayList();
        init(cms, null, null);
    }

    /**
     * Constructor, creates a new, CmsDecoratorConfiguration with a given config file.<p>
     * 
     * @param cms the CmsObject
     * @param configFile the configuration file
     * @throws CmsException if something goes wrong
     */
    public CmsDecoratorConfiguration(CmsObject cms, String configFile)
    throws CmsException {

        m_decorations = new CmsDecorationBundle();
        m_configFile = null;
        m_cms = cms;
        m_locale = m_cms.getRequestContext().getLocale();
        m_usedDecorations = new ArrayList();
        m_excludes = new ArrayList();
        m_decorationDefinitions = new ArrayList();
        init(cms, configFile, null);
    }

    /**
     * Constructor, creates a new, CmsDecoratorConfiguration with a given config file and locale.<p>
     * 
     * @param cms the CmsObject
     * @param configFile the configuration file
     * @param locale to locale to build this configuration for
     * @throws CmsException if something goes wrong
     */
    public CmsDecoratorConfiguration(CmsObject cms, String configFile, Locale locale)
    throws CmsException {

        m_decorations = new CmsDecorationBundle();
        m_configFile = null;
        m_cms = cms;
        m_locale = m_cms.getRequestContext().getLocale();
        m_usedDecorations = new ArrayList();
        m_excludes = new ArrayList();
        m_decorationDefinitions = new ArrayList();
        init(cms, configFile, locale);
    }

    /**
     * Adds decorations defined in a <code>{@link CmsDecorationDefintion}</code> object to the map of all decorations.<p>
     * @param decorationDefinition the <code>{@link CmsDecorationDefintion}</code> the decorations to be added
     * @throws CmsException if something goes wrong
     */
    public void addDecorations(CmsDecorationDefintion decorationDefinition) throws CmsException {

        m_decorations.putAll(decorationDefinition.createDecorationBundle(m_cms, m_configurationLocale).getAll());
    }

    /**
     * Returns the cms.<p>
     *
     * @return the cms
     */
    public CmsObject getCms() {

        return m_cms;
    }

    /**
     * Returns the configFile.<p>
     *
     * @return the configFile
     */
    public String getConfigFile() {

        return m_configFile;
    }

    /**
     * Returns the configurationLocale.<p>
     *
     * @return the configurationLocale
     */
    public Locale getConfigurationLocale() {

        return m_configurationLocale;
    }

    /**
     * Builds a CmsDecorationDefintion from a given configuration file.<p>
     * 
     * @param configuration the configuration file
     * @param i the number of the decoration definition to create
     * @return CmsDecorationDefintion created form configuration file
     */
    public CmsDecorationDefintion getDecorationDefinition(CmsXmlContent configuration, int i) {

        CmsDecorationDefintion decDef = new CmsDecorationDefintion();
        String name = configuration.getValue(XPATH_DECORATION + "[" + i + "]/" + XPATH_NAME, m_configurationLocale).getStringValue(
            m_cms);
        String markfirst = configuration.getValue(
            XPATH_DECORATION + "[" + i + "]/" + XPATH_MARKFIRST,
            m_configurationLocale).getStringValue(m_cms);
        String pretext = configuration.getValue(
            XPATH_DECORATION + "[" + i + "]/" + XPATH_PRETEXT,
            m_configurationLocale).getStringValue(m_cms);
        String posttext = configuration.getValue(
            XPATH_DECORATION + "[" + i + "]/" + XPATH_POSTTEXT,
            m_configurationLocale).getStringValue(m_cms);
        String pretextfirst = configuration.getValue(
            XPATH_DECORATION + "[" + i + "]/" + XPATH_PRETEXTFIRST,
            m_configurationLocale).getStringValue(m_cms);
        String posttextfirst = configuration.getValue(
            XPATH_DECORATION + "[" + i + "]/" + XPATH_POSTTEXTFIRST,
            m_configurationLocale).getStringValue(m_cms);
        String filenname = configuration.getValue(
            XPATH_DECORATION + "[" + i + "]/" + XPATH_FILENAME,
            m_configurationLocale).getStringValue(m_cms);

        decDef.setName(name);
        decDef.setMarkFirst(markfirst.equals("true"));
        decDef.setPreText(pretext);
        decDef.setPostText(posttext);
        decDef.setPreTextFirst(pretextfirst);
        decDef.setPostTextFirst(posttextfirst);
        decDef.setConfigurationFile(filenname);

        return decDef;
    }

    /**
     * Returns the list with all <code>{@link CmsDecorationDefintion}</code> 
     * instances parsed from the config file.<p>
     * 
     * @return The list with all <code>{@link CmsDecorationDefintion}</code> instances 
     *      parsed from the config file
     */
    public List getDecorationDefinitions() {

        return m_decorationDefinitions;
    }

    /**
     * Gets the decoration bundle.<p>
     *@return the decoration bundle to be used
     */
    public CmsDecorationBundle getDecorations() {

        return m_decorations;
    }

    /**
     * Returns the excludes.<p>
     *
     * @return the excludes
     */
    public List getExcludes() {

        return m_excludes;
    }

    /**
     * Returns the locale.<p>
     *
     * @return the locale
     */
    public Locale getLocale() {

        return m_locale;
    }

    /**
     * Returns the usedDecorations.<p>
     *
     * @return the usedDecorations
     */
    public List getUsedDecorations() {

        return m_usedDecorations;
    }

    /**
     * Tests if a decoration key was used before in this configuration.<p>
     * @param key the key to look for
     * @return true if this key was already used
     */
    public boolean hasUsed(String key) {

        return m_usedDecorations.contains(key);
    }

    /**
     * @see org.opencms.jsp.decorator.I_CmsDecoratorConfiguration#init(org.opencms.file.CmsObject, java.lang.String, java.util.Locale)
     */
    public void init(CmsObject cms, String configFile, Locale locale) throws CmsException {

        m_cms = cms;
        m_locale = cms.getRequestContext().getLocale();
        if (configFile != null) {
            m_configFile = configFile;
        }
        if (locale != null) {
            m_decorations = new CmsDecorationBundle(locale);
            m_locale = locale;
        }

        if (m_configFile != null) {

            // get the configuration file
            CmsResource res = m_cms.readResource(m_configFile);
            CmsFile file = m_cms.readFile(res);
            CmsXmlContent configuration = CmsXmlContentFactory.unmarshal(m_cms, file);

            // get the uselocale flag
            // if this flag is not set to true, we must build locale independent decoration bundles
            String uselocale = configuration.getValue(XPATH_USELOCALE, m_configurationLocale).getStringValue(m_cms);
            if (!uselocale.equals("true")) {
                m_locale = null;
            }
            // get the number of decoration definitions
            int decorationDefCount = configuration.getIndexCount(XPATH_DECORATION, m_configurationLocale);
            // get all the decoration definitions
            for (int i = 1; i <= decorationDefCount; i++) {
                CmsDecorationDefintion decDef = getDecorationDefinition(configuration, i);
                m_decorationDefinitions.add(decDef);
                CmsDecorationBundle decBundle = decDef.createDecorationBundle(m_cms, m_locale);
                // merge it to the already existing decorations
                m_decorations.putAll(decBundle.getAll());
            }

            // now read the exclude values
            int excludeValuesCount = configuration.getIndexCount(XPATH_EXCLUDE, m_configurationLocale);
            // get all the exclude definitions
            for (int i = 1; i <= excludeValuesCount; i++) {
                String excludeValue = configuration.getStringValue(
                    m_cms,
                    XPATH_EXCLUDE + "[" + i + "]",
                    m_configurationLocale);
                m_excludes.add(excludeValue.toLowerCase());
            }
        }
    }

    /**
     * Tests if a tag is contained in the exclude list of the decorator.<p>
     * 
     * @param tag the tag to test
     * @return true if the tag is in the exclode list, false othwerwise.
     */
    public boolean isExcluded(String tag) {

        return m_excludes.contains(tag.toLowerCase());
    }

    /**
     * Mark a decoration key as already used.<p>
     * @param key the key to mark
     */
    public void markAsUsed(String key) {

        m_usedDecorations.add(key);
    }

    /**
     * Resets the used decoration keys.<p>
     */
    public void resetMarkedDecorations() {

        m_usedDecorations = new ArrayList();
    }

    /**
     * Sets the cms.<p>
     *
     * @param cms the cms to set
     */
    public void setCms(CmsObject cms) {

        m_cms = cms;
    }

    /**
     * Sets the configFile.<p>
     *
     * @param configFile the configFile to set
     */
    public void setConfigFile(String configFile) {

        m_configFile = configFile;
    }

    /**
     * Sets the configurationLocale.<p>
     *
     * @param configurationLocale the configurationLocale to set
     */
    public void setConfigurationLocale(Locale configurationLocale) {

        m_configurationLocale = configurationLocale;
    }

    /**
     * Sets the decorationDefinitions.<p>
     *
     * @param decorationDefinitions the decorationDefinitions to set
     */
    public void setDecorationDefinitions(List decorationDefinitions) {

        m_decorationDefinitions = decorationDefinitions;
    }

    /**
     * Sets the decoration bundle, overwriting an exiting one.<p> 
     * 
     * @param decorations new decoration bundle
     */
    public void setDecorations(CmsDecorationBundle decorations) {

        m_decorations = decorations;
    }

    /**
     * Sets the excludes.<p>
     *
     * @param excludes the excludes to set
     */
    public void setExcludes(List excludes) {

        m_excludes = excludes;
    }

    /**
     * Sets the locale.<p>
     *
     * @param locale the locale to set
     */
    public void setLocale(Locale locale) {

        m_locale = locale;
    }

    /**
     * Sets the usedDecorations.<p>
     *
     * @param usedDecorations the usedDecorations to set
     */
    public void setUsedDecorations(List usedDecorations) {

        m_usedDecorations = usedDecorations;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {

        StringBuffer buf = new StringBuffer();
        buf.append(this.getClass().getName());
        buf.append(" [configFile = '");
        buf.append(m_configFile);
        buf.append("', decorations = '");
        buf.append(m_decorations);
        buf.append("', locale = '");
        buf.append(m_locale);
        buf.append("']");
        return buf.toString();
    }

}
