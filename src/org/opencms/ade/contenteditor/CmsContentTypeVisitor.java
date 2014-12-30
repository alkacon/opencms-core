/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.contenteditor;

import org.opencms.acacia.shared.CmsAttributeConfiguration;
import org.opencms.acacia.shared.CmsTabInfo;
import org.opencms.acacia.shared.CmsType;
import org.opencms.ade.contenteditor.shared.CmsComplexWidgetData;
import org.opencms.ade.contenteditor.shared.CmsExternalWidgetConfiguration;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsMessages;
import org.opencms.i18n.CmsMultiMessages;
import org.opencms.main.OpenCms;
import org.opencms.widgets.A_CmsWidget;
import org.opencms.widgets.I_CmsADEWidget;
import org.opencms.widgets.I_CmsComplexWidget;
import org.opencms.widgets.I_CmsWidget;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.content.CmsXmlContentTab;
import org.opencms.xml.content.I_CmsXmlContentHandler;
import org.opencms.xml.content.I_CmsXmlContentHandler.DisplayType;
import org.opencms.xml.types.A_CmsXmlContentValue;
import org.opencms.xml.types.CmsXmlNestedContentDefinition;
import org.opencms.xml.types.I_CmsXmlSchemaType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Visitor to read all types and attribute configurations within a content definition.<p>
 */
public class CmsContentTypeVisitor {

    /**
     * Helper class to evaluate the widget display type.<p>
     */
    protected class DisplayTypeEvaluator {

        /** The attribute name. */
        private String m_attributeName;

        /** The attribute type configuration. */
        private CmsAttributeConfiguration m_config;

        /** The configured display type. */
        private DisplayType m_configuredType;

        /** The default display type. */
        private DisplayType m_default;

        /** The applied rule. */
        private EvaluationRule m_rule;

        /**
         * Constructor.<p>
         * 
         * @param config the attribute type configuration
         * @param configuredType the configured display type
         * @param defaultType the default display type
         * @param rule the applied rule
         */
        protected DisplayTypeEvaluator(
            CmsAttributeConfiguration config,
            DisplayType configuredType,
            DisplayType defaultType,
            EvaluationRule rule) {

            m_config = config;

            m_configuredType = configuredType;
            m_default = defaultType;
            m_rule = rule;
        }

        /**
         * Returns the attribute name.<p>
         * 
         * @return the attribute name
         */
        protected String getAttributeName() {

            return m_attributeName;
        }

        /**
         * Returns the attribute configuration with the evaluated display type.<p>
         * 
         * @param predecessor the proposed predecessor display type
         * @param successor the proposed successor display type
         * 
         * @return the attribute configuration 
         */
        protected CmsAttributeConfiguration getEvaluatedConfiguration(DisplayType predecessor, DisplayType successor) {

            DisplayType resultingType = m_configuredType;

            if (resultingType.equals(DisplayType.none)) {
                if (m_rule.equals(EvaluationRule.rootLevel)) {
                    resultingType = DisplayType.wide;
                } else {
                    resultingType = getProposedType();
                    if ((predecessor != null) && predecessor.equals(DisplayType.none)) {
                        predecessor = null;
                    }
                    if ((successor != null) && successor.equals(DisplayType.none)) {
                        successor = null;
                    }
                    if ((predecessor != null) && predecessor.equals(DisplayType.column)) {
                        predecessor = DisplayType.singleline;
                    }
                    if ((successor != null) && successor.equals(DisplayType.column)) {
                        successor = DisplayType.singleline;
                    }
                    boolean strong = (m_rule.equals(EvaluationRule.none) || (m_rule.equals(EvaluationRule.optional) && m_default.equals(DisplayType.singleline)))
                        || (m_rule.equals(EvaluationRule.optional) && m_default.equals(DisplayType.singleline));
                    if (((predecessor == null) || (successor == null)) && strong) {
                        resultingType = m_default;
                    } else if ((predecessor != null) || (successor != null)) {

                        // check if the proposed type matches neither the type of the predecessor nor the type of the successor 
                        if (!(((predecessor != null) && resultingType.equals(predecessor)) || ((successor != null) && resultingType.equals(successor)))) {
                            DisplayType match = (predecessor != null)
                                && (predecessor.equals(DisplayType.wide) || predecessor.equals(DisplayType.singleline))
                            ? predecessor
                            : ((successor != null)
                                && (successor.equals(DisplayType.wide) || successor.equals(DisplayType.singleline))
                            ? successor
                            : null);
                            resultingType = match != null ? match : resultingType;
                        }
                    }
                }
            }
            m_config.setDisplayType(resultingType.name());
            return m_config;
        }

        /**
         * Returns the proposed display type.<p>
         * 
         * @return the proposed display type
         */
        protected DisplayType getProposedType() {

            DisplayType resultingType = m_configuredType;
            if (resultingType.equals(DisplayType.none)) {
                switch (m_rule) {
                    case rootLevel:
                    case labelLength:
                        resultingType = DisplayType.wide;
                        break;
                    case optional:
                        resultingType = DisplayType.singleline;
                        break;
                    default:
                        resultingType = m_default;

                }
            }
            return resultingType;
        }

        /**
         * Sets the attribute name.<p>
         * 
         * @param attributeName the attribute name
         */
        protected void setAttributeName(String attributeName) {

            m_attributeName = attributeName;
        }
    }

    /** Widget display type evaluation rules. */
    protected enum EvaluationRule {
        /** Label length rule. */
        labelLength,
        /** No rule applied. */
        none,
        /** Optional field rule. */
        optional,
        /** Root level rule. */
        rootLevel
    }

    /** The attribute configurations. */
    private Map<String, CmsAttributeConfiguration> m_attributeConfigurations;

    /** The CMS context used for this visitor. */
    private CmsObject m_cms;

    /** Map from attribute names to complex widget configurations. */
    private Map<String, CmsComplexWidgetData> m_complexWidgets = new HashMap<String, CmsComplexWidgetData>();

    /** The content handler. */
    private I_CmsXmlContentHandler m_contentHandler;

    /** The content resource. */
    private CmsFile m_file;

    /** Indicates the visited content has fields that are configured to be invisible to the current user. */
    private boolean m_hasInvisible;

    /** The content locale. */
    private Locale m_locale;

    /** The locale synchronized attribute names. */
    private List<String> m_localeSynchronizations;

    /** The messages. */
    private CmsMultiMessages m_messages;

    /** The registered types. */
    private Map<String, CmsType> m_registeredTypes;

    /** The tab informations. */
    private List<CmsTabInfo> m_tabInfos;

    /** The widget configurations. */
    private Map<String, CmsExternalWidgetConfiguration> m_widgetConfigurations;

    /** The widgets encountered by this visitor. */
    private List<I_CmsWidget> m_widgets = new ArrayList<I_CmsWidget>();

    /**
     * Constructor.<p>
     * 
     * @param cms the CMS context 
     * @param file the content file
     * @param locale the content locale
     */
    public CmsContentTypeVisitor(CmsObject cms, CmsFile file, Locale locale) {

        m_file = file;
        m_cms = cms;
        m_locale = locale;
    }

    /**
     * Gets the CMS context.<p>
     * 
     * @return the CMS context 
     */
    public CmsObject getCmsObject() {

        return m_cms;
    }

    /**
     * Gets the list of widgets which have been processed by this visitor.<p>
     * 
     * @return the list of widget 
     */
    public List<I_CmsWidget> getCollectedWidgets() {

        return Collections.unmodifiableList(m_widgets);
    }

    /** 
     * Gets the map of complex widget configurations.<p>
     * 
     * @return a map from attribute names to complex widget configurations 
     */
    public Map<String, CmsComplexWidgetData> getComplexWidgetData() {

        return m_complexWidgets;
    }

    /**
     * Returns the tabInfos.<p>
     *
     * @return the tabInfos
     */
    public List<CmsTabInfo> getTabInfos() {

        return m_tabInfos;
    }

    /**
     * Returns if the visited content has invisible fields.<p>
     * 
     * @return <code>true</code>  if the visited content has invisible fields
     */
    public boolean hasInvisibleFields() {

        return m_hasInvisible;
    }

    /**
     * Checks if the content type widgets are compatible with the new content editor.<p>
     * 
     * @param xmlContentDefinition the content definition
     * 
     * @return <code>true</code> if the content type widgets are compatible with the new content editor
     * 
     * @throws CmsXmlException if something goes wrong reading the type widget
     */
    public boolean isEditorCompatible(CmsXmlContentDefinition xmlContentDefinition) throws CmsXmlException {

        boolean result = true;
        for (I_CmsXmlSchemaType subType : xmlContentDefinition.getTypeSequence()) {
            if (subType.isSimpleType()) {
                result = isEditorCompatible((A_CmsXmlContentValue)subType);
            } else {
                CmsXmlContentDefinition subTypeDefinition = ((CmsXmlNestedContentDefinition)subType).getNestedContentDefinition();
                result = isEditorCompatible(subTypeDefinition);
            }
            if (!result) {
                break;
            }
        }
        return result;
    }

    /**
     * Visits all types within the XML content definition.<p>
     * 
     * @param xmlContentDefinition the content definition
     * @param messageLocale the locale
     */
    /**
     * Visits all types within the XML content definition.<p>
     * 
     * @param xmlContentDefinition the content definition
     * @param messageLocale the locale
     */
    public void visitTypes(CmsXmlContentDefinition xmlContentDefinition, Locale messageLocale) {

        m_contentHandler = xmlContentDefinition.getContentHandler();
        CmsMessages messages = null;
        m_messages = new CmsMultiMessages(messageLocale);
        try {
            messages = OpenCms.getWorkplaceManager().getMessages(messageLocale);
            if (messages != null) {
                m_messages.addMessages(messages);
            }
            messages = m_contentHandler.getMessages(messageLocale);
            if (messages != null) {
                m_messages.addMessages(messages);
            }
        } catch (Exception e) {
            //ignore
        }
        // generate a new multi messages object and add the messages from the workplace

        m_attributeConfigurations = new HashMap<String, CmsAttributeConfiguration>();
        m_widgetConfigurations = new HashMap<String, CmsExternalWidgetConfiguration>();
        m_registeredTypes = new HashMap<String, CmsType>();
        m_localeSynchronizations = new ArrayList<String>();
        m_tabInfos = collectTabInfos(xmlContentDefinition);
        readTypes(xmlContentDefinition, "");
    }

    /**
     * Returns the attribute configurations.<p>
     * 
     * @return the attribute configurations
     */
    protected Map<String, CmsAttributeConfiguration> getAttributeConfigurations() {

        return m_attributeConfigurations;
    }

    /**
     * Returns the locale synchronized attribute names.<p>
     * 
     * @return the locale synchronized attribute names
     */
    protected List<String> getLocaleSynchronizations() {

        return m_localeSynchronizations;
    }

    /**
     * Returns the types of the visited content definition.<p>
     * 
     * @return the types
     */
    protected Map<String, CmsType> getTypes() {

        return m_registeredTypes;
    }

    /**
     * Returns the external widget configurations.<p>
     * 
     * @return the external widget configurations
     */
    protected Collection<CmsExternalWidgetConfiguration> getWidgetConfigurations() {

        return m_widgetConfigurations.values();
    }

    /**
     * Returns the tab informations for the given content definition.<p>
     * 
     * @param definition the content definition
     * 
     * @return the tab informations
     */
    private List<CmsTabInfo> collectTabInfos(CmsXmlContentDefinition definition) {

        List<CmsTabInfo> result = new ArrayList<CmsTabInfo>();
        if (definition.getContentHandler().getTabs() != null) {
            for (CmsXmlContentTab xmlTab : definition.getContentHandler().getTabs()) {
                String tabName = m_messages.keyDefault(A_CmsWidget.LABEL_PREFIX
                    + definition.getInnerName()
                    + "."
                    + xmlTab.getTabName(), xmlTab.getTabName());
                result.add(new CmsTabInfo(tabName, xmlTab.getIdName(), xmlTab.getStartName(), xmlTab.isCollapsed()));
            }
        }
        return result;
    }

    /**
     * Returns the help information for this value.<p>
     * 
     * @param value the value
     * 
     * @return the help information
     */
    private String getHelp(I_CmsXmlSchemaType value) {

        StringBuffer result = new StringBuffer(64);
        result.append(A_CmsWidget.LABEL_PREFIX);
        result.append(getTypeKey(value));
        result.append(A_CmsWidget.HELP_POSTFIX);
        return m_messages.keyDefault(result.toString(), null);
    }

    /**
     * Returns the label for this value.<p>
     * 
     * @param value the value
     * 
     * @return the label
     */
    private String getLabel(I_CmsXmlSchemaType value) {

        StringBuffer result = new StringBuffer(64);
        result.append(A_CmsWidget.LABEL_PREFIX);
        result.append(getTypeKey(value));
        return m_messages.keyDefault(result.toString(), value.getName());
    }

    /**
     * Returns the schema type message key.<p>
     * 
     * @param value the schema type
     * 
     * @return the schema type message key
     */
    private String getTypeKey(I_CmsXmlSchemaType value) {

        StringBuffer result = new StringBuffer(64);
        result.append(value.getContentDefinition().getInnerName());
        result.append('.');
        result.append(value.getName());
        return result.toString();
    }

    /**
     * Checks if the content value widget is compatible with the new content editor.<p>
     * 
     * @param schemaType the content value type
     * 
     * @return <code>true</code> if the content value widget is compatible with the new content editor
     * 
     * @throws CmsXmlException if something goes wrong reading the type widget
     */
    private boolean isEditorCompatible(A_CmsXmlContentValue schemaType) throws CmsXmlException {

        boolean result = false;
        I_CmsXmlContentHandler contentHandler = schemaType.getContentDefinition().getContentHandler();
        I_CmsWidget widget = contentHandler.getWidget(schemaType);
        result = (widget == null) || (widget instanceof I_CmsADEWidget);
        return result;
    }

    /**
     * Returns if an element with the given path will be displayed at root level of a content editor tab.<p>
     * 
     * @param path the element path
     * 
     * @return <code>true</code> if an element with the given path will be displayed at root level of a content editor tab
     */
    private boolean isTabRootLevel(String path) {

        path = path.substring(1);
        if (!path.contains("/")) {
            return true;
        }
        if (m_tabInfos != null) {
            for (CmsTabInfo info : m_tabInfos) {
                if (info.isCollapsed()
                    && path.startsWith(info.getStartName())
                    && !path.substring(info.getStartName().length() + 1).contains("/")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Reads the attribute configuration for the given schema type. May return <code>null</code> if no special configuration was set.<p>
     * 
     * @param schemaType the schema type
     * @param path the attribute path
     * 
     * @return the attribute configuration
     */
    private DisplayTypeEvaluator readConfiguration(A_CmsXmlContentValue schemaType, String path) {

        String widgetName = null;
        String widgetConfig = null;
        CmsObject cms = getCmsObject();
        String label = getLabel(schemaType);
        // set the default display type
        DisplayType configuredType = DisplayType.none;
        DisplayType defaultType = DisplayType.none;
        EvaluationRule rule = EvaluationRule.none;
        try {
            I_CmsXmlContentHandler contentHandler = schemaType.getContentDefinition().getContentHandler();
            I_CmsWidget widget = contentHandler.getWidget(schemaType);
            configuredType = contentHandler.getDisplayType(schemaType);
            if (configuredType.equals(DisplayType.none) && schemaType.isSimpleType()) {
                // check the type is on the root level of the document, those will be displayed 'wide'
                // the path will always have a leading '/'
                // also in case the label has more than 15 characters, we display 'wide'
                if (isTabRootLevel(path)) {
                    rule = EvaluationRule.rootLevel;
                } else if (label.length() > 15) {
                    rule = EvaluationRule.labelLength;
                } else if ((schemaType.getMinOccurs() == 0)) {
                    rule = EvaluationRule.optional;
                }
            }
            if (widget != null) {
                widgetName = widget.getClass().getName();
                if ((configuredType == DisplayType.column)
                    && !(schemaType.isSimpleType() && (schemaType.getMaxOccurs() == 1) && widget.isCompactViewEnabled())) {
                    // column view is not allowed for this widget
                    configuredType = DisplayType.singleline;
                }
                long timer = 0;
                if (widget instanceof I_CmsADEWidget) {
                    if (CmsContentService.LOG.isDebugEnabled()) {
                        timer = System.currentTimeMillis();
                    }
                    I_CmsADEWidget adeWidget = (I_CmsADEWidget)widget;
                    defaultType = adeWidget.getDefaultDisplayType();
                    widgetName = adeWidget.getWidgetName();
                    widgetConfig = adeWidget.getConfiguration(cms, schemaType, m_messages, m_file, m_locale);
                    if (!adeWidget.isInternal() && !m_widgetConfigurations.containsKey(widgetName)) {
                        CmsExternalWidgetConfiguration externalConfiguration = new CmsExternalWidgetConfiguration(
                            widgetName,
                            adeWidget.getInitCall(),
                            adeWidget.getJavaScriptResourceLinks(cms),
                            adeWidget.getCssResourceLinks(cms));
                        m_widgetConfigurations.put(widgetName, externalConfiguration);
                    }
                    if (CmsContentService.LOG.isDebugEnabled()) {
                        CmsContentService.LOG.debug(Messages.get().getBundle().key(
                            Messages.LOG_TAKE_READING_WIDGET_CONFIGURATION_TIME_2,
                            widgetName,
                            "" + (System.currentTimeMillis() - timer)));
                    }

                }
                m_widgets.add(widget);
            } else if (contentHandler.getComplexWidget(schemaType) != null) {
                I_CmsComplexWidget complexWidget = contentHandler.getComplexWidget(schemaType);
                CmsComplexWidgetData widgetData = complexWidget.getWidgetData(m_cms);
                CmsExternalWidgetConfiguration externalConfig = widgetData.getExternalWidgetConfiguration();
                if (externalConfig != null) {
                    m_widgetConfigurations.put(complexWidget.getName(), externalConfig);
                }
                m_complexWidgets.put(CmsContentService.getAttributeName(schemaType), widgetData);
            }
        } catch (Exception e) {
            // may happen if no widget was set for the value
            CmsContentService.LOG.debug(e.getMessage(), e);
        }
        // remove the leading slash from element path to check visibility
        boolean visible = !m_contentHandler.hasVisibilityHandlers()
            || m_contentHandler.isVisible(cms, schemaType, path.substring(1), m_file, m_locale);
        if (!visible) {
            // set the has invisible flag
            m_hasInvisible = true;
        }
        boolean localeSynchronized = m_contentHandler.hasSynchronizedElements()
            && m_contentHandler.getSynchronizations().contains(path.substring(1));
        CmsAttributeConfiguration result = new CmsAttributeConfiguration(
            label,
            getHelp(schemaType),
            widgetName,
            widgetConfig,
            readDefaultValue(schemaType, path),
            configuredType.name(),
            visible,
            localeSynchronized);
        return new DisplayTypeEvaluator(result, configuredType, defaultType, rule);
    }

    /**
     * Reads the default value for the given type.<p>
     * 
     * @param schemaType the schema type
     * @param path the element path
     * 
     * @return the default value
     */
    private String readDefaultValue(I_CmsXmlSchemaType schemaType, String path) {

        return m_contentHandler.getDefault(getCmsObject(), m_file, schemaType, path, m_locale);
    }

    /**
     * Reads the types from the given content definition and adds the to the map of already registered
     * types if necessary.<p>
     * 
     * @param xmlContentDefinition the XML content definition
     * @param path the element path
     * 
     * @return the type 
     */
    private CmsType readTypes(CmsXmlContentDefinition xmlContentDefinition, String path) {

        String typeName = CmsContentService.getTypeUri(xmlContentDefinition);
        if (m_registeredTypes.containsKey(typeName)) {
            return m_registeredTypes.get(typeName);
        }
        CmsType type = new CmsType(typeName);
        type.setChoiceMaxOccurrence(xmlContentDefinition.getChoiceMaxOccurs());
        m_registeredTypes.put(typeName, type);
        CmsType choiceType = null;
        if (type.isChoice()) {
            choiceType = new CmsType(typeName + "/" + CmsType.CHOICE_ATTRIBUTE_NAME);
            m_registeredTypes.put(choiceType.getId(), choiceType);
            type.addAttribute(CmsType.CHOICE_ATTRIBUTE_NAME, choiceType, 1, xmlContentDefinition.getChoiceMaxOccurs());
        }
        ArrayList<DisplayTypeEvaluator> evaluators = new ArrayList<DisplayTypeEvaluator>();
        for (I_CmsXmlSchemaType subType : xmlContentDefinition.getTypeSequence()) {

            String subTypeName = null;
            String childPath = path + "/" + subType.getName();
            String subAttributeName = CmsContentService.getAttributeName(subType.getName(), typeName);
            DisplayTypeEvaluator ev = readConfiguration((A_CmsXmlContentValue)subType, childPath);
            ev.setAttributeName(subAttributeName);
            evaluators.add(ev);
            CmsType subEntityType;
            if (subType.isSimpleType()) {
                subTypeName = CmsContentService.TYPE_NAME_PREFIX + subType.getTypeName();
                if (!m_registeredTypes.containsKey(subTypeName)) {
                    subEntityType = new CmsType(subTypeName);
                    m_registeredTypes.put(subTypeName, subEntityType);
                } else {
                    subEntityType = m_registeredTypes.get(subTypeName);
                }
            } else {
                CmsXmlContentDefinition subTypeDefinition = ((CmsXmlNestedContentDefinition)subType).getNestedContentDefinition();
                subTypeName = CmsContentService.getTypeUri(subTypeDefinition);
                subEntityType = readTypes(subTypeDefinition, childPath);
            }
            if (choiceType != null) {
                choiceType.addAttribute(subAttributeName, subEntityType, subType.getMinOccurs(), subType.getMaxOccurs());
            } else {
                type.addAttribute(subAttributeName, subEntityType, subType.getMinOccurs(), subType.getMaxOccurs());
            }
        }
        DisplayType predecessor = null;
        for (int i = 0; i < evaluators.size(); i++) {
            DisplayTypeEvaluator ev = evaluators.get(i);
            DisplayType successor = ((i + 1) < evaluators.size()) ? evaluators.get(i + 1).getProposedType() : null;
            CmsAttributeConfiguration evaluated = ev.getEvaluatedConfiguration(predecessor, successor);
            m_attributeConfigurations.put(ev.getAttributeName(), evaluated);
            if (evaluated.isLocaleSynchronized()) {
                m_localeSynchronizations.add(ev.getAttributeName());
            }
            predecessor = DisplayType.valueOf(evaluated.getDisplayType());
        }
        return type;
    }
}
