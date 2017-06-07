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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.editors;

import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.CmsXmlUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.logging.Log;

import org.dom4j.Document;
import org.dom4j.Element;

/**
 * Single editor configuration object.<p>
 *
 * Holds all necessary information about an OpenCms editor which is stored in the
 * "editor_configuration.xml" file in each editor folder.<p>
 *
 * Provides methods to get the editor information for the editor manager.<p>
 *
 * @since 6.0.0
 */
public class CmsWorkplaceEditorConfiguration {

    /** Name of the root document node. */
    public static final String DOCUMENT_NODE = "editor";

    /** Name of the name attribute. */
    protected static final String A_NAME = "name";

    /** Name of the single user agent node. */
    protected static final String N_AGENT = "agent";

    /** Name of the resource type class node. */
    protected static final String N_CLASS = "class";

    /** Name of the editor label node. */
    protected static final String N_LABEL = "label";

    /** Name of the resource type subnode mapto. */
    protected static final String N_MAPTO = "mapto";

    /** Name of the resource type subnode name. */
    protected static final String N_NAME = "name";

    /** Name of the param node. */
    protected static final String N_PARAM = "param";

    /** Name of the params node. */
    protected static final String N_PARAMS = "params";

    /** Name of the resource type subnode ranking. */
    protected static final String N_RANKING = "ranking";

    /** Name of the resourcetypes node. */
    protected static final String N_RESOURCETYPES = "resourcetypes";

    /** Name of the resource type node. */
    protected static final String N_TYPE = "type";

    /** Name of the useragents node. */
    protected static final String N_USERAGENTS = "useragents";

    /** Name of the widgeteditor node. */
    protected static final String N_WIDGETEDITOR = "widgeteditor";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsWorkplaceEditorConfiguration.class);

    /** The browser patterns. */
    private List<Pattern> m_browserPattern;

    /** The editor label. */
    private String m_editorLabel;

    /** The editor URI. */
    private String m_editorUri;

    /** The name of the configuration (usually the name of the folder under /system/workplace/editors). */
    private String m_name;

    /** Additional parameters for the editor. */
    private Map<String, String> m_parameters = new HashMap<String, String>();

    /** The resource types. */
    private Map<String, String[]> m_resTypes;

    /** The user agents. */
    private List<String> m_userAgentsRegEx;

    /** The valid configuration flag. */
    private boolean m_validConfiguration;

    /** The widget editor. */
    private String m_widgetEditor;

    /**
     * Constructor with xml data String.<p>
     *
     * @param xmlData the XML data String containing the information about the editor
     * @param editorUri the editor workplace URI
     * @param name the editor configuration name
     */
    public CmsWorkplaceEditorConfiguration(byte[] xmlData, String editorUri, String name) {

        setValidConfiguration(true);
        try {
            m_name = name;
            initialize(CmsXmlUtils.unmarshalHelper(xmlData, null), editorUri);

        } catch (CmsXmlException e) {
            // xml String could not be parsed
            logConfigurationError(Messages.get().getBundle().key(Messages.ERR_XML_PARSE_0), e);
        }
    }

    /**
     * Returns the list of compiled browser patterns.<p>
     *
     * @return the list of compiled browser patterns
     */
    public List<Pattern> getBrowserPattern() {

        return m_browserPattern;
    }

    /**
     * Returns the editor label key used for the localized nice name.<p>
     *
     * @return the editor label key used for the localized nice name
     */
    public String getEditorLabel() {

        return m_editorLabel;
    }

    /**
     * Returns the editor workplace URI.<p>
     *
     * @return the editor workplace URI
     */
    public String getEditorUri() {

        return m_editorUri;
    }

    /**
     * Returns the mapping for the given resource type.<p>
     *
     * @param resourceType the resource type name to check
     * @return the mapping or null, if no mapping is specified
     */
    public String getMappingForResourceType(String resourceType) {

        String[] resourceTypeParams = getResourceTypes().get(resourceType);
        if (resourceTypeParams == null) {
            return null;
        } else {
            return resourceTypeParams[1];
        }
    }

    /**
     * Gets the name of the editor configuration (usually the folder name under /system/workplace/editors).<p>
     *
     * @return the name of the editor configuration
     */
    public String getName() {

        return m_name;
    }

    /**
     * Gets the map of additional editor parameters.<p>
     *
     * @return the editor parameter map
     */
    public Map<String, String> getParameters() {

        return m_parameters;
    }

    /**
     * Returns the ranking value for the given resource type.<p>
     *
     * @param resourceType the current resource type
     * @return the ranking (the higher the better)
     */
    public float getRankingForResourceType(String resourceType) {

        String[] resourceTypeParams = getResourceTypes().get(resourceType);
        if (resourceTypeParams == null) {
            return -1.0f;
        } else {
            return Float.parseFloat(resourceTypeParams[0]);
        }
    }

    /**
     * Returns the valid resource types of the editor.<p>
     *
     * A single map item has the resource type name as key,
     * the value is a String array with two entries:
     * <ul>
     * <li>Entry 0: the ranking for the resource type</li>
     * <li>Entry 1: the mapping to another resource type or null</li>
     * </ul><p>
     *
     * @return the valid resource types of the editor
     */
    public Map<String, String[]> getResourceTypes() {

        return m_resTypes;
    }

    /**
     * Returns the valid user agents regular expressions of the editor.<p>
     *
     * @return the valid user agents regular expressions of the editor
     */
    public List<String> getUserAgentsRegEx() {

        return m_userAgentsRegEx;
    }

    /**
     * Returns the widget editor class for rich text editing.<p>
     *
     * @return the widget editor class for rich text editing
     */
    public String getWidgetEditor() {

        return m_widgetEditor;
    }

    /**
     * Returns if the current configuration is valid.<p>
     *
     * @return true if no configuration errors were found, otherwise false
     */
    public boolean isValidConfiguration() {

        return m_validConfiguration;
    }

    /**
     * Returns if the editor is usable as a widget editor for rich text editing.<p>
     *
     * @return true if the editor is usable as a widget editor for rich text editing, otherwise false
     */
    public boolean isWidgetEditor() {

        return CmsStringUtil.isNotEmpty(m_widgetEditor);
    }

    /**
     * Tests if the current browser is matching the configuration.<p>
     *
     * @param currentBrowser the users browser String to test
     * @return true if the browser matches the configuration, otherwise false
     */
    public boolean matchesBrowser(String currentBrowser) {

        if (currentBrowser == null) {
            return false;
        }
        for (int i = 0; i < getBrowserPattern().size(); i++) {
            boolean matches = getBrowserPattern().get(i).matcher(currentBrowser.trim()).matches();
            if (matches) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().getBundle().key(Messages.LOG_BROWSER_MATCHES_CONFIG_1, currentBrowser));
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Returns if the configuration is suitable for the given resource type.<p>
     *
     * @param resourceType the resource type to check
     * @return true if the configuration matches the resource type
     */
    public boolean matchesResourceType(String resourceType) {

        return m_resTypes.containsKey(resourceType);
    }

    /**
     * Initializes all member variables.<p>
     *
     * @param document the XML configuration document
     * @param editorUri the editor workplace URI
     */
    @SuppressWarnings("unchecked")
    private void initialize(Document document, String editorUri) {

        m_parameters.clear();

        // get the root element of the configuration
        Element rootElement = document.getRootElement();

        // set the label of the editor
        setEditorLabel(rootElement.elementText(N_LABEL));

        // set the widget editor class if available
        String widgetClass = rootElement.elementText(N_WIDGETEDITOR);
        if (CmsStringUtil.isNotEmpty(widgetClass)) {
            setWidgetEditor(widgetClass);
        }

        // set the URI of the editor
        setEditorUri(editorUri);

        // create the map of valid resource types
        Iterator<Element> i = rootElement.element(N_RESOURCETYPES).elementIterator(N_TYPE);
        Map<String, String[]> resTypes = new HashMap<String, String[]>();
        while (i.hasNext()) {
            Element currentType = i.next();
            float ranking;
            String name = currentType.elementText(N_NAME);
            if (CmsStringUtil.isEmpty(name)) {
                logConfigurationError(Messages.get().getBundle().key(Messages.ERR_INVALID_RESTYPE_NAME_0), null);
                continue;
            }
            try {
                ranking = Float.parseFloat(currentType.elementText(N_RANKING));
            } catch (Throwable t) {
                logConfigurationError(Messages.get().getBundle().key(Messages.ERR_INVALID_RESTYPE_RANKING_1, name), t);
                continue;
            }
            String mapTo = currentType.elementText(N_MAPTO);
            if (CmsStringUtil.isEmpty(mapTo)) {
                mapTo = null;
            }
            resTypes.put(name, new String[] {"" + ranking, mapTo});
        }
        // add the additional resource types
        i = rootElement.element(N_RESOURCETYPES).elementIterator(N_CLASS);
        while (i.hasNext()) {
            Element currentClass = i.next();
            String name = currentClass.elementText(N_NAME);
            List<String> assignedTypes = new ArrayList<String>();
            try {
                // get the editor type matcher class
                I_CmsEditorTypeMatcher matcher = (I_CmsEditorTypeMatcher)Class.forName(name).newInstance();
                assignedTypes = matcher.getAdditionalResourceTypes();
            } catch (Throwable t) {
                logConfigurationError(Messages.get().getBundle().key(Messages.ERR_INVALID_RESTYPE_CLASS_1, name), t);
                continue;
            }
            float ranking;
            try {
                ranking = Float.parseFloat(currentClass.elementText(N_RANKING));
            } catch (Throwable t) {
                logConfigurationError(Messages.get().getBundle().key(Messages.ERR_INVALID_RESTYPE_RANKING_1, name), t);
                continue;
            }
            String mapTo = currentClass.elementText(N_MAPTO);
            if ("".equals(mapTo)) {
                mapTo = null;
            }
            // now loop through all types found and add them
            Iterator<String> j = assignedTypes.iterator();
            while (j.hasNext()) {
                String typeName = j.next();
                resTypes.put(typeName, new String[] {"" + ranking, mapTo});
            }
        }

        setResourceTypes(resTypes);

        // create the list of user agents & compiled patterns for editor
        i = document.getRootElement().element(N_USERAGENTS).elementIterator(N_AGENT);
        List<Pattern> pattern = new ArrayList<Pattern>();
        List<String> userAgents = new ArrayList<String>();
        while (i.hasNext()) {
            Element currentAgent = i.next();
            String agentName = currentAgent.getText();
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(agentName)) {
                userAgents.add(agentName);
                try {
                    pattern.add(Pattern.compile(agentName));
                } catch (PatternSyntaxException e) {
                    logConfigurationError(
                        Messages.get().getBundle().key(Messages.ERR_COMPILE_EDITOR_REGEX_1, agentName),
                        e);
                }
            } else {
                logConfigurationError(Messages.get().getBundle().key(Messages.ERR_INVALID_USERAGENT_DEF_0), null);
            }
        }
        setBrowserPattern(pattern);
        setUserAgentsRegEx(userAgents);

        Element paramsElement = (Element)(document.getRootElement().selectSingleNode(N_PARAMS));
        if (paramsElement != null) {
            List<?> params = paramsElement.selectNodes(N_PARAM);
            for (Object paramObj : params) {
                Element paramElement = (Element)paramObj;
                String name = paramElement.attributeValue(A_NAME);
                String value = paramElement.getText();
                m_parameters.put(name, value);
            }
        }
    }

    /**
     * Logs configuration errors and invalidates the current configuration.<p>
     *
     * @param message the message specifying the configuration error
     * @param t the Throwable object or null
     */
    private void logConfigurationError(String message, Throwable t) {

        setValidConfiguration(false);
        if (LOG.isErrorEnabled()) {
            if (t == null) {
                LOG.error(Messages.get().getBundle().key(Messages.LOG_EDITOR_CONFIG_ERROR_1, message));
            } else {
                LOG.error(Messages.get().getBundle().key(Messages.LOG_EDITOR_CONFIG_ERROR_1, message), t);
            }
        }
    }

    /**
     * Sets the list of compiled browser patterns.<p>
     *
     * @param pattern the list of compiled browser patterns
     */
    private void setBrowserPattern(List<Pattern> pattern) {

        if ((pattern == null) || (pattern.size() == 0)) {
            setValidConfiguration(false);
            LOG.error(Messages.get().getBundle().key(Messages.LOG_EDITOR_CONFIG_NO_PATTERN_0));
        }
        m_browserPattern = pattern;
    }

    /**
     * Sets the editor label key used for the localized nice name.<p>
     *
     * @param label the editor label key used for the localized nice name
     */
    private void setEditorLabel(String label) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(label)) {
            setValidConfiguration(false);
            LOG.error(Messages.get().getBundle().key(Messages.LOG_EDITOR_CONFIG_NO_LABEL_0));
        }
        m_editorLabel = label;
    }

    /**
     * Sets the editor workplace URI.<p>
     * @param uri the editor workplace URI
     */
    private void setEditorUri(String uri) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(uri)) {
            setValidConfiguration(false);
            LOG.error(Messages.get().getBundle().key(Messages.LOG_EDITOR_CONFIG_NO_URI_0));
        }
        m_editorUri = uri;
    }

    /**
     * Sets the valid resource types of the editor.<p>
     *
     * @param types the valid resource types of the editor
     */
    private void setResourceTypes(Map<String, String[]> types) {

        if ((types == null) || (types.size() == 0)) {
            setValidConfiguration(false);
            LOG.error(Messages.get().getBundle().key(Messages.LOG_NO_RESOURCE_TYPES_0));
        }
        m_resTypes = types;
    }

    /**
     * Sets the valid user agents regular expressions of the editor.<p>
     *
     * @param agents the valid user agents regular expressions of the editor
     */
    private void setUserAgentsRegEx(List<String> agents) {

        if ((agents == null) || (agents.size() == 0)) {
            setValidConfiguration(false);
            LOG.error(Messages.get().getBundle().key(Messages.LOG_NO_USER_AGENTS_0));
        }
        m_userAgentsRegEx = agents;
    }

    /**
     * Sets if the current configuration is valid.<p>
     *
     * @param isValid true if no configuration errors were found, otherwise false
     */
    private void setValidConfiguration(boolean isValid) {

        m_validConfiguration = isValid;
    }

    /**
     * Sets the widget editor class for rich text editing.<p>
     *
     * @param widgetEditor the widget editor class for rich text editing
     */
    private void setWidgetEditor(String widgetEditor) {

        m_widgetEditor = widgetEditor;
    }

}
