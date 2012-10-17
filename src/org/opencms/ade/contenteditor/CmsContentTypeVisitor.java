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

import com.alkacon.acacia.shared.AttributeConfiguration;
import com.alkacon.acacia.shared.TabInfo;
import com.alkacon.acacia.shared.Type;
import com.alkacon.vie.shared.I_Type;

import org.opencms.ade.contenteditor.shared.CmsExternalWidgetConfiguration;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsMessages;
import org.opencms.i18n.CmsMultiMessages;
import org.opencms.main.OpenCms;
import org.opencms.widgets.A_CmsWidget;
import org.opencms.widgets.I_CmsADEWidget;
import org.opencms.widgets.I_CmsWidget;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.content.CmsXmlContentTab;
import org.opencms.xml.content.I_CmsXmlContentHandler;
import org.opencms.xml.types.A_CmsXmlContentValue;
import org.opencms.xml.types.CmsXmlNestedContentDefinition;
import org.opencms.xml.types.I_CmsXmlSchemaType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Visitor to read all types and attribute configurations within a content definition.<p>
 */
public class CmsContentTypeVisitor {

    /** The attribute configurations. */
    private Map<String, AttributeConfiguration> m_attributeConfigurations;

    /** The content handler. */
    private I_CmsXmlContentHandler m_contentHandler;

    /** The content resource. */
    private CmsFile m_file;

    /** The content locale. */
    private Locale m_locale;

    /** The messages. */
    private CmsMultiMessages m_messages;

    /** The registered types. */
    private Map<String, I_Type> m_registeredTypes;

    /** The tab informations. */
    private List<TabInfo> m_tabInfos;

    /** The widget configurations. */
    private Map<String, CmsExternalWidgetConfiguration> m_widgetConfigurations;

    /** The CMS context used for this visitor. */
    private CmsObject m_cms;

    /** Set of class names of non-ADE widgets which have been discovered. */
    private Set<String> m_nonAdeWidgets = new HashSet<String>();

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
     * Gets the set of class names of non-ADE widgets that have been discovered.<p>
     * 
     * @return the set of non-ADE widget classes 
     */
    public Set<String> getNonAdeWidgets() {

        return m_nonAdeWidgets;
    }

    /**
     * Returns the tabInfos.<p>
     *
     * @return the tabInfos
     */
    public List<TabInfo> getTabInfos() {

        return m_tabInfos;
    }

    /** 
     * Returns true if non-ADE compatible widgets were found.<p>
     * 
     * @return true if non-ADE compatible widgets were found 
     */
    public boolean hasNonAdeWidgets() {

        return !m_nonAdeWidgets.isEmpty();
    }

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
            m_messages.addMessages(messages);
            m_messages.addMessages(m_contentHandler.getMessages(messageLocale));
        } catch (Exception e) {
            //ignore
        }
        // generate a new multi messages object and add the messages from the workplace

        m_attributeConfigurations = new HashMap<String, AttributeConfiguration>();
        m_widgetConfigurations = new HashMap<String, CmsExternalWidgetConfiguration>();
        m_registeredTypes = new HashMap<String, I_Type>();
        readTypes(xmlContentDefinition, "");
        m_tabInfos = collectTabInfos(xmlContentDefinition);
    }

    /**
     * Returns the attribute configurations.<p>
     * 
     * @return the attribute configurations
     */
    protected Map<String, AttributeConfiguration> getAttributeConfigurations() {

        return m_attributeConfigurations;
    }

    /**
     * Returns the types of the visited content definition.<p>
     * 
     * @return the types
     */
    protected Map<String, I_Type> getTypes() {

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
    private List<TabInfo> collectTabInfos(CmsXmlContentDefinition definition) {

        List<TabInfo> result = new ArrayList<TabInfo>();
        if (definition.getContentHandler().getTabs() != null) {
            for (CmsXmlContentTab xmlTab : definition.getContentHandler().getTabs()) {
                String tabName = m_messages.keyDefault(A_CmsWidget.LABEL_PREFIX
                    + definition.getInnerName()
                    + "."
                    + xmlTab.getTabName(), xmlTab.getTabName());
                result.add(new TabInfo(tabName, xmlTab.getIdName(), xmlTab.getStartName(), xmlTab.isCollapsed()));
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
        return m_messages.keyDefault(result.toString(), value.getName());
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
     * Reads the attribute configuration for the given schema type. May return <code>null</code> if no special configuration was set.<p>
     * 
     * @param schemaType the schema type
     * @param path the attribute path
     * 
     * @return the attribute configuration
     */
    private AttributeConfiguration readConfiguration(A_CmsXmlContentValue schemaType, String path) {

        AttributeConfiguration result = null;
        String widgetName = null;
        String widgetConfig = null;
        CmsObject cms = getCmsObject();
        try {

            I_CmsWidget widget = schemaType.getContentDefinition().getContentHandler().getWidget(schemaType);
            widgetName = widget.getClass().getName();
            widgetConfig = widget.getConfiguration();
            if (widget instanceof I_CmsADEWidget) {
                I_CmsADEWidget adeWidget = (I_CmsADEWidget)widget;
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
            } else {
                m_nonAdeWidgets.add(widget.getClass().getName());
            }
        } catch (Exception e) {
            // may happen if no widget was set for the value
            CmsContentService.LOG.debug(e.getMessage(), e);
        }
        result = new AttributeConfiguration(
            getLabel(schemaType),
            getHelp(schemaType),
            widgetName,
            widgetConfig,
            readDefaultValue(schemaType, path));
        return result;
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
     */
    private void readTypes(CmsXmlContentDefinition xmlContentDefinition, String path) {

        String typeName = CmsContentService.getTypeUri(xmlContentDefinition);
        if (m_registeredTypes.containsKey(typeName)) {
            return;
        }
        Type type = new Type(typeName);
        type.setChoiceMaxOccurrence(xmlContentDefinition.getChoiceMaxOccurs());
        m_registeredTypes.put(typeName, type);
        if (type.isChoice()) {
            Type choiceType = new Type(typeName + "/" + Type.CHOICE_ATTRIBUTE_NAME);
            m_registeredTypes.put(choiceType.getId(), choiceType);
            type.addAttribute(
                Type.CHOICE_ATTRIBUTE_NAME,
                choiceType.getId(),
                1,
                xmlContentDefinition.getChoiceMaxOccurs());
            type = choiceType;
        }
        for (I_CmsXmlSchemaType subType : xmlContentDefinition.getTypeSequence()) {

            String subTypeName = null;
            String childPath = path + "/" + subType.getName();
            String subAttributeName = CmsContentService.getAttributeName(subType.getName(), typeName);
            AttributeConfiguration config = readConfiguration((A_CmsXmlContentValue)subType, childPath);
            if (config != null) {
                m_attributeConfigurations.put(subAttributeName, config);
            }
            if (subType.isSimpleType()) {
                subTypeName = CmsContentService.TYPE_NAME_PREFIX + subType.getTypeName();
                if (!m_registeredTypes.containsKey(subTypeName)) {
                    m_registeredTypes.put(subTypeName, new Type(subTypeName));
                }
            } else {
                CmsXmlContentDefinition subTypeDefinition = ((CmsXmlNestedContentDefinition)subType).getNestedContentDefinition();
                subTypeName = CmsContentService.getTypeUri(subTypeDefinition);
                readTypes(subTypeDefinition, childPath);
            }
            type.addAttribute(subAttributeName, subTypeName, subType.getMinOccurs(), subType.getMaxOccurs());
        }
    }
}