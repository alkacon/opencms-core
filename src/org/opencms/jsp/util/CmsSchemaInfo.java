/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.jsp.util;

import org.opencms.acacia.shared.CmsTabInfo;
import org.opencms.ade.contenteditor.CmsContentTypeVisitor;
import org.opencms.ade.contenteditor.CmsWidgetUtil;
import org.opencms.ade.contenteditor.CmsWidgetUtil.WidgetInfo;
import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsMessages;
import org.opencms.i18n.CmsMultiMessages;
import org.opencms.jsp.util.I_CmsFormatterInfo.ResolveMode;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.A_CmsWidget;
import org.opencms.widgets.CmsSelectWidgetOption;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.content.CmsDefaultXmlContentHandler;
import org.opencms.xml.content.I_CmsXmlContentHandler;
import org.opencms.xml.types.CmsXmlNestedContentDefinition;
import org.opencms.xml.types.I_CmsXmlSchemaType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;

/**
 * Bean for accessing XML content schema information from JSPs.
 */
public class CmsSchemaInfo {

    /**
     * Represents information about a single field in a content schema.
     */
    public class Field implements I_CmsInfoWrapper {

        /** The nested fields. */
        private LinkedHashMap<String, Field> m_children = new LinkedHashMap<>();

        /** The content definition (may be null). */
        private CmsXmlContentDefinition m_contentDefinition;

        /** The field. */
        private I_CmsXmlSchemaType m_field;

        /** The path of the field. */
        private String m_path = "";

        /** The widget information. */
        private WidgetInfo m_widgetInfo;

        /**
         * Creates a new instance for the root level of a schema.
         *
         * @param contentDef the content definition
         */
        public Field(CmsXmlContentDefinition contentDef) {

            m_contentDefinition = contentDef;
            m_path = "";
            processContentDefinition(m_contentDefinition);
        }

        /**
         * Creates a new instance.
         *
         * @param field the schema type for the field
         * @param path the path of the field
         */
        public Field(I_CmsXmlSchemaType field, String path) {

            m_path = path;
            m_field = field;
            if (field instanceof CmsXmlNestedContentDefinition) {
                CmsXmlNestedContentDefinition nestedDef = (CmsXmlNestedContentDefinition)field;
                m_contentDefinition = nestedDef.getNestedContentDefinition();
                processContentDefinition(m_contentDefinition);
            }
        }

        /**
         * Gets the nested fields, if any.
         *
         *
         * @return the nested fields
         */
        public Collection<Field> getChildren() {

            return m_children.values();
        }

        /**
         * Gets the content definition.
         *
         * @return the content definition
         */
        public CmsXmlContentDefinition getContentDefinition() {

            return m_contentDefinition;
        }

        /**
         * Gets the default value.
         *
         * @return the default value
         */
        @SuppressWarnings("synthetic-access")
        public String getDefaultValue() {

            if (m_field == null) {
                // root node
                return null;
            }
            return m_root.getContentDefinition().getContentHandler().getDefault(m_cms, null, m_field, m_path, m_locale);
        }

        /**
         * Gets the description.
         *
         * @return the description
         */
        public String getDescription() {

            return getDescription(ResolveMode.text, m_cms.getRequestContext().getLocale());
        }

        /**
         * Gets the localized description
         * 
         * @param locale the locale
         * @return the description for the given locale
         */
        public String getDescription(Locale locale) {

            return getDescription(ResolveMode.text, locale);

        }

        /**
         * Gets the description key.
         *
         * @return the description key
         */
        public String getDescriptionKey() {

            return getDescription(ResolveMode.key, m_cms.getRequestContext().getLocale());
        }

        /**
         * Gets the raw configured description.
         *
         * @return the raw configured description
         */
        public String getDescriptionRaw() {

            return getDescription(ResolveMode.raw, m_cms.getRequestContext().getLocale());
        }

        /**
         * Gets the display name.
         *
         * @return the display name
         */
        public String getDisplayName() {

            return getDisplayName(ResolveMode.text);
        }

        /**
         * Gets the display name key.
         *
         * @return the display name key
         */
        public String getDisplayNameKey() {

            return getDisplayName(ResolveMode.key);
        }

        /**
         * Gets the raw configured string for the display name.
         *
         * @return the raw display name
         */
        public String getDisplayNameRaw() {

            return getDisplayName(ResolveMode.raw);

        }

        /**
         * Checks if this is a choice type.
         *
         * @return true if this is a choice type
         */
        public boolean getIsChoice() {

            return m_field.isChoiceType();
        }

        /**
         * Checks if this is a nested content type.
         *
         * @return true if this is a nested content type
         */
        public boolean getIsNestedContent() {

            return m_contentDefinition != null;
        }

        /**
         * Gets the maximum number of occurrences.
         *
         * @return the maximum number of occurrences
         */
        public int getMaxOccurs() {

            return m_field.getMaxOccurs();
        }

        /**
         * Gets the minimum number of occurrences.
         *
         * @return the minimum number of occurrences
         */
        public int getMinOccurs() {

            return m_field.getMinOccurs();
        }

        /**
         * Gets the field name.
         *
         * @return the name
         */
        public String getName() {

            if (m_field == null) {
                return null;
            }
            return m_field.getName();
        }

        /**
         * Tries to interpret the widget configuration as a select option configuration and returns the list of select options if this succeeds, and null otherwise.
         *
         * @return the list of parsed select options, or null if the widget configuration couldn't be interpreted that way
         */
        @SuppressWarnings({"synthetic-access"})
        public List<CmsSelectWidgetOption> getParsedSelectOptions() {

            String widgetConfig = getWidgetConfig();
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(widgetConfig)) {
                // passing an empty/null configuration to parseOptions would result in an empty list, not null, and we want null here
                return null;
            }
            try {
                List<CmsSelectWidgetOption> options = org.opencms.widgets.CmsSelectWidgetOption.parseOptions(
                    widgetConfig);
                List<CmsSelectWidgetOption> result = new ArrayList<>();
                Set<String> values = options.stream().map(option -> option.getValue()).collect(Collectors.toSet());
                String defaultValue = getDefaultValue();
                Locale locale = m_cms.getRequestContext().getLocale();
                if (CmsStringUtil.isEmptyOrWhitespaceOnly(defaultValue) || !values.contains(defaultValue)) {
                    CmsSelectWidgetOption noValue = new CmsSelectWidgetOption(
                        "",
                        true,
                        org.opencms.gwt.Messages.get().getBundle(locale).key(
                            org.opencms.gwt.Messages.GUI_SELECTBOX_EMPTY_SELECTION_0));
                    result.add(noValue);
                }

                result.addAll(options);
                return result;
            } catch (Exception e) {
                LOG.info(e.getLocalizedMessage(), e);
                return null;
            }
        }

        /**
         * Gets the field type.
         *
         * @return the type
         */
        public String getType() {

            return m_field.getTypeName();
        }

        /**
         * Gets the validation error message.
         *
         * @return the validation error message
         */
        @SuppressWarnings("synthetic-access")
        public String getValidationError() {

            if (m_field == null) {
                return null;
            }
            I_CmsXmlContentHandler handler = m_field.getContentDefinition().getContentHandler();
            if (!(handler instanceof CmsDefaultXmlContentHandler)) {
                return null;
            }
            CmsDefaultXmlContentHandler defaultHandler = (CmsDefaultXmlContentHandler)handler;
            return defaultHandler.getValidationWarningOrErrorMessage(m_cms, m_locale, m_field.getName(), false, false);
        }

        /**
         * Gets the validation error localization key.
         *
         * @return the validation error localization key
         */
        @SuppressWarnings("synthetic-access")
        public String getValidationErrorKey() {

            if (m_field == null) {
                return null;
            }
            I_CmsXmlContentHandler handler = m_field.getContentDefinition().getContentHandler();
            if (!(handler instanceof CmsDefaultXmlContentHandler)) {
                return null;
            }
            CmsDefaultXmlContentHandler defaultHandler = (CmsDefaultXmlContentHandler)handler;
            return defaultHandler.getValidationWarningOrErrorMessage(m_cms, m_locale, m_field.getName(), false, true);
        }

        /**
         * Gets the validation warning message.
         *
         * @return the validation warning message
         */
        @SuppressWarnings({"synthetic-access"})
        public String getValidationWarning() {

            if (m_field == null) {
                return null;
            }
            I_CmsXmlContentHandler handler = m_field.getContentDefinition().getContentHandler();
            if (!(handler instanceof CmsDefaultXmlContentHandler)) {
                return null;
            }
            CmsDefaultXmlContentHandler defaultHandler = (CmsDefaultXmlContentHandler)handler;
            return defaultHandler.getValidationWarningOrErrorMessage(m_cms, m_locale, m_field.getName(), true, false);
        }

        /**
         * Gets the validation warning message key.
         *
         * @return the validation warning message key
         */
        @SuppressWarnings("synthetic-access")
        public String getValidationWarningKey() {

            if (m_field == null) {
                return null;
            }
            I_CmsXmlContentHandler handler = m_field.getContentDefinition().getContentHandler();
            if (!(handler instanceof CmsDefaultXmlContentHandler)) {
                return null;
            }
            CmsDefaultXmlContentHandler defaultHandler = (CmsDefaultXmlContentHandler)handler;
            return defaultHandler.getValidationWarningOrErrorMessage(m_cms, m_locale, m_field.getName(), true, true);
        }

        /**
         * Gets the visibility configuration string for the field.
         *
         * @return the visibility configuration string
         */
        public String getVisibility() {

            I_CmsXmlContentHandler handler = m_root.getContentDefinition().getContentHandler();
            if (handler instanceof CmsDefaultXmlContentHandler) {
                return ((CmsDefaultXmlContentHandler)handler).getVisibilityConfigString(m_path);
            }
            return null;
        }

        /**
         * Gets the widget.
         *
         * @return the widget
         */
        @SuppressWarnings("synthetic-access")
        public String getWidget() {

            if (m_contentDefinition != null) {
                return null;
            }
            WidgetInfo widgetInfo = getWidgetInfo();
            if (widgetInfo != null) {
                return widgetInfo.getWidget().getClass().getName();
            } else {
                LOG.error("Widget info not defined for " + m_path + " in " + m_root.getContentDefinition());
                return null;
            }
        }

        /**
         * Gets the widget configuration.
         *
         * @return the widget configuration
         */
        @SuppressWarnings("synthetic-access")
        public String getWidgetConfig() {

            if (m_contentDefinition != null) {
                return null;
            }
            WidgetInfo widgetInfo = getWidgetInfo();
            if (widgetInfo != null) {
                return widgetInfo.getWidget().getConfiguration();
            } else {
                LOG.error("Widget info not defined for " + m_path + " in " + m_root.getContentDefinition());
                return null;
            }

        }

        /**
         * Gets the description or description key.
         *
         * @param resolveMode the resolve mode
         * @param locale the locale to use
         * @return the description or localization key
         */
        @SuppressWarnings("synthetic-access")
        private String getDescription(ResolveMode resolveMode, Locale locale) {

            if (m_field == null) {
                return null;
            }
            StringBuffer result = new StringBuffer(64);
            I_CmsXmlContentHandler handler = m_field.getContentDefinition().getContentHandler();
            if (handler instanceof CmsDefaultXmlContentHandler) {
                CmsDefaultXmlContentHandler defaultHandler = (CmsDefaultXmlContentHandler)handler;
                String help = defaultHandler.getFieldHelp().get(m_field.getName());
                if (help != null) {
                    CmsMacroResolver resolver = new CmsMacroResolver();
                    if (resolveMode == ResolveMode.raw) {
                        return help;
                    } else {
                        if (resolveMode == ResolveMode.key) {
                            resolver = new CmsKeyDummyMacroResolver(resolver);
                        }
                        resolver.setCmsObject(m_cms);
                        resolver.setKeepEmptyMacros(true);
                        resolver.setMessages(getMessages(locale));

                        String val = resolver.resolveMacros(help);
                        if (resolveMode == ResolveMode.key) {
                            return CmsKeyDummyMacroResolver.getKey(val);
                        } else {
                            return val;
                        }
                    }
                }
            }
            result.append(A_CmsWidget.LABEL_PREFIX);
            result.append(getTypeKey(m_field));
            result.append(A_CmsWidget.HELP_POSTFIX);
            switch (resolveMode) {
                case key:
                case raw:
                    return result.toString();
                case text:
                default:
                    return getMessages(locale).keyDefault(result.toString(), null);
            }
        }

        /**
         * Gets the display name or localization key.
         *
         * @param keyOnly true if only the localization key should be returned rather than the localized display name
         * @return the display name or localization key
         */
        @SuppressWarnings("synthetic-access")
        private String getDisplayName(ResolveMode resolveMode) {

            if (m_field == null) {
                return null;
            }
            I_CmsXmlContentHandler handler = m_field.getContentDefinition().getContentHandler();
            if (handler instanceof CmsDefaultXmlContentHandler) {
                CmsDefaultXmlContentHandler defaultHandler = (CmsDefaultXmlContentHandler)handler;
                String label = defaultHandler.getFieldLabels().get(m_field.getName());
                if (label != null) {
                    if (resolveMode == ResolveMode.raw) {
                        return label;
                    } else {
                        CmsMacroResolver resolver = new CmsMacroResolver();
                        if (resolveMode == ResolveMode.key) {
                            resolver = new CmsKeyDummyMacroResolver(resolver);
                        }
                        resolver.setCmsObject(m_cms);
                        resolver.setKeepEmptyMacros(true);
                        resolver.setMessages(m_messages);
                        String val = resolver.resolveMacros(label);
                        if (resolveMode == ResolveMode.key) {
                            return CmsKeyDummyMacroResolver.getKey(val);
                        } else {
                            return val;
                        }
                    }
                }
            }
            StringBuffer result = new StringBuffer(64);
            result.append(A_CmsWidget.LABEL_PREFIX);
            result.append(getTypeKey(m_field));
            switch (resolveMode) {
                case raw:
                case key:
                    return result.toString();
                case text:
                default:
                    return m_messages.keyDefault(result.toString(), m_field.getName());
            }
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
         * Gets the widget info.
         *
         * @return the widget info
         */
        @SuppressWarnings("synthetic-access")
        private WidgetInfo getWidgetInfo() {

            if (m_widgetInfo == null) {
                m_widgetInfo = CmsWidgetUtil.collectWidgetInfo(
                    m_cms,
                    m_root.getContentDefinition(),
                    m_path,
                    null,
                    m_cms.getRequestContext().getLocale());
            }
            return m_widgetInfo;

        }

        /**
         * Process content definition.
         *
         * @param contentDef the content definition
         */
        private void processContentDefinition(CmsXmlContentDefinition contentDef) {

            List<I_CmsXmlSchemaType> fields = contentDef.getTypeSequence();
            for (I_CmsXmlSchemaType field : fields) {
                String name = field.getName();
                m_children.put(name, new Field(field, combinePaths(m_path, name)));
            }
        }

    }

    /**
     * Represents the a single editor tab and its fields.
     */
    public class Tab implements I_CmsInfoWrapper {

        private Locale m_defaultLocale;

        /** The fields. */
        private List<Field> m_fields = new ArrayList<>();

        private int m_tabIndex = -1;

        private Function<Locale, List<CmsTabInfo>> m_tabInfoProvider;

        /**
         * Default constructor - doesn't initialize anything.
         *
         * <p>Used for 'dummy' tab that is generated when no actual tabs are configured.
         *
         */
        public Tab() {

        }

        /**
         * Constructor for tab that is actually configured in the schema.
         *
         * @param defaultLocale the default locale
         * @param tabIndex the current tab index
         * @param tabInfoProvider the function that provides the list of localized tab info objects
         */
        public Tab(Locale defaultLocale, int tabIndex, Function<Locale, List<CmsTabInfo>> tabInfoProvider) {

            m_tabInfoProvider = tabInfoProvider;
            m_tabIndex = tabIndex;
            m_defaultLocale = defaultLocale;
        }

        /**
         * Adds a field.
         *
         * @param field the field to add
         */
        public void add(Field field) {

            m_fields.add(field);
        }

        /**
         * Gets the description.
         *
         * @return the description
         */
        public String getDescription() {

            return localizedTabInfo(null, null, tabInfo -> tabInfo.getDescription());
        }

        /**
         * Gets the localized description.
         * 
         * @param locale the locale 
         * @return the description for the locale
         */
        @Override
        public String getDescription(Locale locale) {

            return localizedTabInfo(locale, null, tab -> tab.getDescription());
        }

        /**
         * Gets the description key.
         *
         * @return the description key
         */
        public String getDescriptionKey() {

            return localizedTabInfo(null, null, tabInfo -> tabInfo.getDescriptionKey());
        }

        /**
         * Gets the raw description string.
         *
         * @return the raw description
         */
        public String getDescriptionRaw() {

            return localizedTabInfo(null, null, tab -> tab.getDescriptionRaw());
        }

        /**
         * Gets the display name.
         *
         * @return the display name
         */
        public String getDisplayName() {

            return localizedTabInfo(null, null, tab -> tab.getTabName());
        }

        /**
         * Gets the display name key.
         *
         * @return the display name key
         */
        public String getDisplayNameKey() {

            return localizedTabInfo(null, null, tab -> tab.getTabNameKey());
        }

        /**
         * Gets the raw display name string.
         *
         * @return the raw display name string
         */
        public String getDisplayNameRaw() {

            return localizedTabInfo(null, null, tab -> tab.getTabNameRaw());

        }

        /**
         * Gets the fields.
         *
         * @return the fields
         */
        public List<Field> getFields() {

            return m_fields;
        }

        /**
         * Helper method for reading localized tab information.
         *
         * @param locale the locale (null for current locale)
         * @param defaultValue the default value
         * @param function a function to extract the relevant information from a localized CmsTabInfo instance
         *
         * @return the result
         */
        private String localizedTabInfo(Locale locale, String defaultValue, Function<CmsTabInfo, String> function) {

            if (locale == null) {
                locale = m_defaultLocale;
            }
            if ((m_tabIndex != -1) && (m_tabInfoProvider != null)) {
                CmsTabInfo tabInfo = m_tabInfoProvider.apply(locale).get(m_tabIndex);
                return function.apply(tabInfo);
            }
            return defaultValue;
        }

    }

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSchemaInfo.class);

    /** The CMS context. */
    private CmsObject m_cms;

    private CmsXmlContentDefinition m_contentDefinition;

    /** The locale. */
    private Locale m_locale;

    /** The messages. */
    private CmsMultiMessages m_messages;

    /** Cache of message objects. */
    private Map<Locale, CmsMultiMessages> m_messagesByLocale = new HashMap<>();

    /** The root field instanec representing the whole schema. */
    private Field m_root;

    private Map<Locale, List<CmsTabInfo>> m_tabInfoCache = new HashMap<>();

    /** The tabs. */
    private List<Tab> m_tabs;

    /**
     * Creates a new instance.
     *
     * @param cms the CMS context
     * @param contentDef the content definition
     */
    public CmsSchemaInfo(CmsObject cms, CmsXmlContentDefinition contentDef) {

        m_cms = cms;
        m_locale = cms.getRequestContext().getLocale();
        m_root = new Field(contentDef);
        m_contentDefinition = contentDef;

        Locale locale = cms.getRequestContext().getLocale();
        m_messages = getMessages(locale);
        initTabs();

    }

    /**
     * Combine schema paths.
     *
     * @param a the first path
     * @param b the second path
     * @return the combined paths
     */
    static String combinePaths(String a, String b) {

        if ("".equals(a)) {
            return b;
        }
        return a + "/" + b;
    }

    /**
     * Gets the root node.
     *
     * @return the root node
     */
    public Field getRoot() {

        return m_root;
    }

    /**
     * Gets the tabs.
     *
     * @return the tabs
     */
    public List<Tab> getTabs() {

        return m_tabs;
    }

    /**
     * Checks for tabs.
     *
     * @return true, if there are tabs
     */
    public boolean hasTabs() {

        return m_tabs.get(0).getDisplayName() == null;
    }

    /**
     * Creates the message object for the given locale.
     * 
     * @param locale the locale 
     * @return the messages for the given locale 
     */
    private CmsMultiMessages createMessages(Locale locale) {

        CmsMessages messages;
        CmsMultiMessages resultMessages = new CmsMultiMessages(locale);
        resultMessages.setFallbackHandler(m_contentDefinition.getContentHandler().getMessageKeyHandler());
        try {
            messages = OpenCms.getWorkplaceManager().getMessages(locale);
            if (messages != null) {
                resultMessages.addMessages(messages);
            }
            messages = m_contentDefinition.getContentHandler().getMessages(locale);
            if (messages != null) {
                resultMessages.addMessages(messages);
            }
        } catch (Exception e) {
            LOG.debug(e.getMessage(), e);
        }
        return resultMessages;
    }

    /**
     * Gets or creates the messages object for the given locale.
     *
     * @param locale the locale to use
     * @return the messages for the locale
     */
    private CmsMultiMessages getMessages(Locale locale) {

        return m_messagesByLocale.computeIfAbsent(locale, l -> {
            return createMessages(l);
        });
    }

    /**
     * Gets or creates the localized tab information for the given locale.
     * 
     * @param locale the locale 
     * @return the localized tab information
     */
    private List<CmsTabInfo> getTabInfos(Locale locale) {

        return m_tabInfoCache.computeIfAbsent(
            locale,
            l -> CmsContentTypeVisitor.collectTabInfos(m_cms, m_root.getContentDefinition(), getMessages(l)));
    }

    /**
     * Initializes the tabs.
     */
    private void initTabs() {

        List<CmsTabInfo> tabs = CmsContentTypeVisitor.collectTabInfos(m_cms, m_root.getContentDefinition(), m_messages);

        List<Tab> result = new ArrayList<>();
        if ((tabs == null) || (tabs.size() == 0)) {
            Tab defaultTab = new Tab();
            result.add(defaultTab);
            defaultTab.getFields().addAll(m_root.getChildren());
        } else {
            int index = 0;
            for (Field node : m_root.getChildren()) {
                if ((index < tabs.size()) && node.getName().equals(tabs.get(index).getStartName())) {
                    Tab tab = new Tab(m_cms.getRequestContext().getLocale(), index, locale -> getTabInfos(locale));
                    result.add(tab);
                    index += 1;
                }
                if (result.size() > 0) {
                    result.get(result.size() - 1).add(node);
                }
            }
        }
        m_tabs = result;
    }
}
