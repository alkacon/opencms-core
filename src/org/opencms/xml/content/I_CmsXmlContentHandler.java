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

package org.opencms.xml.content;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsMessages;
import org.opencms.i18n.CmsMultiMessages.I_KeyFallbackHandler;
import org.opencms.main.CmsException;
import org.opencms.relations.CmsRelationType;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.util.CmsDefaultSet;
import org.opencms.widgets.I_CmsComplexWidget;
import org.opencms.widgets.I_CmsWidget;
import org.opencms.workplace.editors.directedit.I_CmsEditHandler;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.containerpage.CmsFormatterConfiguration;
import org.opencms.xml.content.CmsDefaultXmlContentHandler.InvalidRelationAction;
import org.opencms.xml.types.I_CmsXmlContentValue;
import org.opencms.xml.types.I_CmsXmlContentValue.SearchContentType;
import org.opencms.xml.types.I_CmsXmlSchemaType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.servlet.ServletRequest;

import org.dom4j.Element;

/**
 * Handles special XML content livetime events, and also provides XML content editor rendering hints.<p>
 *
 * @since 6.0.0
 */
public interface I_CmsXmlContentHandler {

    /**
     * The available display types for element widgets.
     */
    public static enum DisplayType {
        /** The two column type. */
        column,

        /** The default display type. */
        none,

        /** The single line type. */
        singleline,

        /** The default wide display type. */
        wide
    }

    /**
     * Settings for a JSON renderer.
     */
    public static class JsonRendererSettings {

        /** The renderer class name. */
        private String m_className;

        /** The parameters for the renderer. */
        private Map<String, String> m_params;

        /**
         * Creates a new instance.
         *
         * @param className the class name
         * @param params the parameters
         */
        public JsonRendererSettings(String className, Map<String, String> params) {

            m_className = className;
            m_params = params;
        }

        /**
         * Gets the class name.
         *
         * @return the class name
         */
        public String getClassName() {

            return m_className;
        }

        /**
         * Gets the parameters.
         *
         * @return the parameters
         */
        public Map<String, String> getParameters() {

            if (m_params == null) {
                return Collections.emptyMap();
            }
            return Collections.unmodifiableMap(m_params);
        }

    }

    /**
     * The available mapping types. Currently only available for searchsettings (using the "addto" attribute in the node &lt;solrfield&gt;).
     */
    public static enum MappingType {
        /** Map for the content's resource. */
        ELEMENT,

        /** Map for all container pages the content is placed on. */
        PAGE
    }

    /** Represents how values should be handled with regard to synchronization across locales in the editor. */
    public static enum SynchronizationMode {
        /** No synchronization. */
        none,
        /** Normal synchronization. */
        standard,
        /** Strong synchronization - a value will be synchronized even if the parent nodes are missing in the target locale. */
        strong;
    }

    /** Mapping name for the 'date expired' mapping. */
    String ATTRIBUTE_DATEEXPIRED = "dateexpired";

    /** Mapping name for the 'date released' mapping. */
    String ATTRIBUTE_DATERELEASED = "datereleased";

    /** List of all allowed attribute mapping names, for fast lookup. */
    List<String> ATTRIBUTES = Collections.unmodifiableList(
        Arrays.asList(new String[] {ATTRIBUTE_DATERELEASED, ATTRIBUTE_DATEEXPIRED}));

    /** Prefix for attribute mappings. */
    String MAPTO_ATTRIBUTE = "attribute:";

    /** Prefix for permission mappings. */
    String MAPTO_PERMISSION = "permission:";

    /** Prefix for property mappings. */
    String MAPTO_PROPERTY = "property:";

    /** Prefix for property mappings. */
    String MAPTO_PROPERTY_INDIVIDUAL = MAPTO_PROPERTY + CmsProperty.TYPE_INDIVIDUAL + ":";

    /** Prefix for property list mappings. */
    String MAPTO_PROPERTY_LIST = "propertyList:";

    /** Prefix for property list mappings. */
    String MAPTO_PROPERTY_LIST_INDIVIDUAL = MAPTO_PROPERTY_LIST + CmsProperty.TYPE_INDIVIDUAL + ":";

    /** Prefix for property list mappings. */
    String MAPTO_PROPERTY_LIST_SHARED = MAPTO_PROPERTY_LIST + CmsProperty.TYPE_SHARED + ":";

    /** Prefix for property mappings. */
    String MAPTO_PROPERTY_SHARED = MAPTO_PROPERTY + CmsProperty.TYPE_SHARED + ":";

    /** Prefix for URL name mappings. */
    String MAPTO_URLNAME = "urlName";

    /**
     * Writes an availability date back to the content, if a mapping is defined for it.
     *
     * @param cms the CMS context
     * @param content the content to write to
     * @param attr the attribute to write
     * @param locales the locales of the resource
     * @param value the value to write
     *
     * @throws CmsException if something goes wrong
     */
    boolean applyReverseAvailabilityMapping(
        CmsObject cms,
        CmsXmlContent content,
        CmsMappingResolutionContext.AttributeType attr,
        List<Locale> locales,
        long value)
    throws CmsException;

    /**
     * Checks if an availability attribute should be written back to the content if the availability is changed through the availability dialog.
     *
     * @param attr the attribute to check
     * @return true if the attribute should be written to the content
     */
    boolean canUseReverseAvailabilityMapping(CmsMappingResolutionContext.AttributeType attr);

    /**
     * Gets the list of allowed template context names.<p>
     *
     * @return the list of allowed template context names
     */
    CmsDefaultSet<String> getAllowedTemplates();

    /**
     * Gets the list of change handler configurations.
     *
     * @return the list of change handler configurations
     */
    List<CmsChangeHandlerConfig> getChangeHandlerConfigs();

    /**
     * Gets the unconfigured complex widget defined for the given path.<p>
     *
     * @param cms the CMS context
     * @param path the value path
     * @return the complex widget
     */
    I_CmsComplexWidget getComplexWidget(CmsObject cms, String path);

    /**
     * Returns the configuration String value for the widget used to edit the given XML content schema type.<p>
     *
     * If no configuration value is available, this method must return <code>null</code>.
     *
     * @param type the value to get the widget configuration for
     *
     * @return the configuration String value for the widget used to edit the given XML content schema type
     */
    String getConfiguration(I_CmsXmlSchemaType type);

    /**
     * Gets the widget configuration for the given sub-path.<p>
     *
     * @param remainingPath a sub-path
     * @return the widget configuration for the given sub-path
     */
    String getConfiguration(String remainingPath);

    /**
     * Gets the configured display type for a given path.<p>
     *
     * @param path the path
     * @param defaultVal the value to return if no configured display type is found
     *
     * @return the configured display type (or the default value)
     */
    DisplayType getConfiguredDisplayType(String path, DisplayType defaultVal);

    /**
     * Returns the resource-independent CSS resources to include into the html-page head.<p>
     *
     * @return the CSS resources to include into the html-page head
     */
    Set<String> getCSSHeadIncludes();

    /**
     * Returns all the CSS resources to include into the html-page head.<p>
     *
     * @param cms the current CMS context
     * @param resource the resource from which to get the head includes
     *
     * @throws CmsException if something goes wrong
     *
     * @return the CSS resources to include into the html-page head
     */
    Set<String> getCSSHeadIncludes(CmsObject cms, CmsResource resource) throws CmsException;

    /**
     * Returns the default String value for the given XML content schema type object in the given XML content.<p>
     *
     * If a schema type does not have a default value, this method must return <code>null</code>.
     *
     * @param cms the current users OpenCms context
     * @param resource the content resource
     * @param type the type to get the default for
     * @param path the element path
     * @param locale the currently selected locale for the value
     *
     * @return the default String value for the given XML content value object
     *
     * @see org.opencms.xml.types.I_CmsXmlSchemaType#getDefault(Locale)
     */
    String getDefault(CmsObject cms, CmsResource resource, I_CmsXmlSchemaType type, String path, Locale locale);

    /**
     * Returns the default String value for the given XML content schema type object in the given XML content.<p>
     *
     * If a schema type does not have a default value, this method must return <code>null</code>.
     *
     * @param cms the current users OpenCms context
     * @param value the value to get the default for
     * @param locale the currently selected locale for the value
     *
     * @return the default String value for the given XML content value object
     *
     * @see org.opencms.xml.types.I_CmsXmlSchemaType#getDefault(Locale)
     */
    String getDefault(CmsObject cms, I_CmsXmlContentValue value, Locale locale);

    /**
     * Gets the default complex widget to be used for this type.<p>
     *
     * @return the default complex widget for this type
     */
    I_CmsComplexWidget getDefaultComplexWidget();

    /**
     * Gets the default complex widget class name configured for this type.<p>
     *
     * @return the default complex widget class name
     */
    String getDefaultComplexWidgetClass();

    /**
     * Gets the default complex widget configuration string configured for this type.<p>
     *
     * @return the default complex widget configuration string
     */
    String getDefaultComplexWidgetConfiguration();

    /**
     * Returns if the widget for this type should be displayed in compact view.<p>
     *
     * @param type the value to check the view mode for
     *
     * @return the widgets display type
     */
    DisplayType getDisplayType(I_CmsXmlSchemaType type);

    /**
     * Returns the edit handler if configured.<p>
     *
     * @return the edit handler
     */
    I_CmsEditHandler getEditHandler();

    /**
     * Returns the editor change handlers.<p>
     *
     * @param selfOnly if true, only return editor change handlers configured directly for this content handler
     * @return the editor change handlers
     */
    List<I_CmsXmlContentEditorChangeHandler> getEditorChangeHandlers(boolean selfOnly);

    /**
     * Returns the container page element formatter configuration for a given resource.<p>
     *
     * @param cms the current users OpenCms context, used for selecting the right project
     * @param res the resource for which the formatter configuration should be retrieved
     *
     * @return the container page element formatter configuration for this handler
     */
    CmsFormatterConfiguration getFormatterConfiguration(CmsObject cms, CmsResource res);

    /**
     * Gets the geo-coordinate mapping configuration.
     *
     * @return the geo-coordinate mapping configuration
     */
    CmsGeoMappingConfiguration getGeoMappingConfiguration();

    /**
     * Gets the action to perform if the given name refers to a link field which refers to a VFS file that no longer exists.
     *
     * @param name the field name
     * @return the action
     */
    default InvalidRelationAction getInvalidRelationAction(String name) {

        return null;
    }

    /**
     * Returns the resource-independent javascript resources to include into the html-page head.<p>
     *
     * @return the javascript resources to include into the html-page head
     */
    Set<String> getJSHeadIncludes();

    /**
     * Returns all the javascript resources to include into the html-page head.<p>
     *
     * @param cms the current CMS context
     * @param resource the resource for which the head includes should be retrieved
     *
     * @return the javascript resources to include into the html-page head
     *
     * @throws CmsException if something goes wrong
     */
    Set<String> getJSHeadIncludes(CmsObject cms, CmsResource resource) throws CmsException;

    /**
     * Gets the JSON renderer settings.
     *
     * @return the JSON renderer settings
     */
    JsonRendererSettings getJsonRendererSettings();

    /**
     * Gets the mappings defined in the schema.
     *
     * @return the mappings
     */
    Map<String, List<String>> getMappings();

    /**
     * Returns the all mappings defined for the given element xpath.<p>
     *
     * @param elementName the element xpath to look up the mapping for
     *
     * @return the mapping defined for the given element xpath
     */
    List<String> getMappings(String elementName);

    /**
     * Gets the message key fallback handler.<p>
     *
     * This is used to automatically provide fallbacks for missing message keys in the editor.<p>
     *
     * @return the message key fallback handler
     */
    I_KeyFallbackHandler getMessageKeyHandler();

    /**
     * Returns the {@link CmsMessages} that are used to resolve localized keys
     * for the given locale in this content handler.<p>
     *
     * If no localized messages are configured for this content handler,
     * this method returns <code>null</code>.<p>
     *
     * @param locale the locale to get the messages for
     *
     * @return the {@link CmsMessages} that are used to resolve localized keys
     * for the given locale in this content handler
     */
    CmsMessages getMessages(Locale locale);

    /**
     * Returns the folder name that contains eventual XML content model files to use for this resource type.<p>
     *
     * @return the folder name containing eventual XML content master files
     */
    String getModelFolder();

    /**
     * Returns the nested formatters for the given resource.<p>
     *
     * @param cms the cms context
     * @param res the resource
     * @param locale the content locale
     * @param req the request if available
     *
     * @return the nested formatter ids
     */
    List<String> getNestedFormatters(CmsObject cms, CmsResource res, Locale locale, ServletRequest req);

    /**
    * Gets the parameter with the given name..<p>
    *
    * @param param the parameter name
    * @return the parameter value
    */
    String getParameter(String param);

    /**
     * Returns the preview URI for the given XML content value object to be displayed in the editor.<p>
     *
     * If <code>null</code> is returned, no preview is possible for contents using this handler.<p>
     *
     * @param cms the current OpenCms user context
     * @param content the XML content to display the preview URI for
     * @param resourcename the name in the VFS of the resource that is currently edited
     *
     * @return the preview URI for the given XML content value object to be displayed in the editor
     */
    String getPreview(CmsObject cms, CmsXmlContent content, String resourcename);

    /**
     * Returns the relation type for the given value.<p>
     *
     * @param value the value to get the relation type for
     *
     * @return the relation type for the given value
     *
     * @deprecated use {@link #getRelationType(String)} with {@link I_CmsXmlContentValue#getPath()} instead
     */
    @Deprecated
    CmsRelationType getRelationType(I_CmsXmlContentValue value);

    /**
     * Returns the relation type for the given path.<p>
     *
     * @param path the path to get the relation type for
     *
     * @return the relation type for the given path
     */
    CmsRelationType getRelationType(String path);

    /**
     * Returns the relation type for the given path.<p>
     *
     * @param xpath the path to get the relation type for
     * @param defaultType the default type if none is set
     *
     * @return the relation type for the given path
     */
    CmsRelationType getRelationType(String xpath, CmsRelationType defaultType);

    /**
     * Returns the search content type, ie., the way how to integrate the value into full text search.<p>
     *
     * For the full text search, the value of all elements in one locale of the XML content are combined
     * to one big text, which is referred to as the "content" in the context of the full text search.
     * With this option, it is possible to hide certain elements from this "content" that does not make sense
     * to include in the full text search.<p>
     *
     * Moreover, if the value contains a link to another resource, the content of that other resource can be added.
     *
     * @param value the XML content value to check
     *
     * @return the search content type, indicating how the element should be added to the content for the full text search
     */
    SearchContentType getSearchContentType(I_CmsXmlContentValue value);

    /**
     * Returns all configured Search fields for this XML content.<p>
     *
     * @return the Search fields for this XMl content
     */
    Set<CmsSearchField> getSearchFields();

    /**
     * Returns all configured Search fields for this XML content that should be attached to container pages the content is placed on.<p>
     *
     * @return the Search fields for this XMl content
     */
    Set<CmsSearchField> getSearchFieldsForPage();

    /**
     * Returns the search content settings defined in the annotation node of this XML content.<p>
     *
     * A search setting defined within the xsd:annotaion node of an XML schema definition can look like:<p>
     * <code>&lt;searchsetting element="Image/Align" searchContent="false"/&gt;</code><p>
     *
     * The returned map contains the 'element' attribute value as keys and the 'searchContent'
     * attribute value as values.<p>
     *
     * @return the search field settings for this XML content schema
     */
    Map<String, SearchContentType> getSearchSettings();

    /**
     * Returns the element settings defined for the container page formatters.<p>
     *
     * @param cms the current CMS context
     * @param resource the resource for which to get the setting definitions
     *
     * @return the element settings defined for the container page formatters
     */
    Map<String, CmsXmlContentProperty> getSettings(CmsObject cms, CmsResource resource);

    /**
     * Returns the configuration for elements that require a synchronization across all locales.<p>
     *
     * @param recursive if true, recursively combines all synchronization information from nested schemas, otherwise only returns the synchronizations for this schema
     * @return the synchronization information
     */
    CmsSynchronizationSpec getSynchronizations(boolean recursive);

    /**
     * Returns the tabs to be displayed in the editor.<p>
     *
     * @return the tabs to be displayed in the editor
     */
    List<CmsXmlContentTab> getTabs();

    /**
     * Returns the "Title" mapping set for the given XML content document in the given Locale.<p>
     *
     * @param cms the current OpenCms user context
     * @param document the XML content to get the title mapping for (this must be of a type that uses this handler)
     * @param locale the locale to get the title mapping for
     *
     * @return the "Title" mapping set for the given XML content document in the given Locale
     */
    String getTitleMapping(CmsObject cms, CmsXmlContent document, Locale locale);

    /**
     * Gets the version transformation VFS path.
     *
     * <p>If schema versions are used, the the XSLT transformation read from this VFS path is used to transform contents
     * of older versions into the current version.
     *
     * @return the VFS path to read an XSLT file for version transformation from
     */
    String getVersionTransformation();

    /**
     * Gets the widget for the given path and CMS context.
     *
     * @param cms the current CMS context
     * @param path the XML value path
     * @return the widget for the path
     */
    I_CmsWidget getWidget(CmsObject cms, String path);

    /**
     * Returns the editor widget that should be used for the given XML content value.<p>
     *
     * The handler implementations should use the "appinfo" node of the XML content definition
     * schema to define the mappings of elements to widgets.<p>
     *
     * @param value the XML content value to get the widget for
     *
     * @return the editor widget that should be used for the given XML content value
     *
     * @throws CmsXmlException if something goes wrong
     */
    @Deprecated
    I_CmsWidget getWidget(I_CmsXmlSchemaType value) throws CmsXmlException;

    /**
     * Returns true if the contents for this content handler have schema-based formatters which can be disabled or enabled.<p>
     *
     * @return true if the contents for this content handler have schema-based formatters which can be disabled or enabled
     */
    boolean hasModifiableFormatters();

    /**
     * Returns whether there are nested formatters configured for this content type.<p>
     *
     * @return <code>true</code> if there are nested formatters configured for this content type
     */
    boolean hasNestedFormatters();

    /**
     * Returns if there are locale synchronized elements configured.<p>
     *
     * @return <code>true</code> if there are locale synchronized elements configured
     */
    boolean hasSynchronizedElements();

    /**
     * Returns if there are visibility handlers configured for this content type.<p>
     *
     * @return <code>true</code> if there are visibility handlers configured for this content type
     */
    boolean hasVisibilityHandlers();

    /**
     * Initializes this content handler for the given XML content definition by
     * analyzing the "appinfo" node.<p>
     *
     * @param appInfoElement the "appinfo" element root node to analyze
     * @param contentDefinition the XML content definition that XML content handler belongs to
     *
     * @throws CmsXmlException if something goes wrong
     */
    void initialize(Element appInfoElement, CmsXmlContentDefinition contentDefinition) throws CmsXmlException;

    /**
     * Performs a check of the given XML document.<p>
     *
     * The main difference to the {@link #resolveValidation(CmsObject, I_CmsXmlContentValue, CmsXmlContentErrorHandler)}
     * method is that this method may silently remove some values, for instance, for broken links.<p>
     *
     * @param cms the current OpenCms user context
     * @param document the document to resolve the check rules for
     */
    void invalidateBrokenLinks(CmsObject cms, CmsXmlContent document);

    /**
     * Checks whether the Acacia editor is disabled for this type.<p>
     *
     * @return true if the Acacia editor is disabled
     */
    boolean isAcaciaEditorDisabled();

    /**
     * Returns <code>true</code> if the XML content should be indexed when it is dropped in a container page,
     * and returns <code>false</code> if this XML content should be indexed as 'stand-alone' document.<p>
     *
     * This flag is intended by excluding XML contents from the search index that are not used as detail pages,
     * but to index those extraction result when they are part of a container page.<p>
     *
     * In order to set this falg add an attribute <code>containerpageOnly="true"</code> to the
     * <code>'&lt;searchsettings&gt;-node'</code> of the XSD of the resource type you want to be indexed only
     * when it is part of a container page.<p>
     *
     * @return the container page only flag
     */
    boolean isContainerPageOnly();

    /**
     * Returns <code>true</code> in case the given value should be searchable with
     * the integrated full text search.<p>
     *
     * For the full text search, the value of all elements in one locale of the XML content are combined
     * to one big text, which is referred to as the "content" in the context of the full text search.
     * With this option, it is possible to hide certain elements from this "content" that does not make sense
     * to include in the full text search.<p>
     *
     * @param value the XML content value to check
     *
     * @return <code>true</code> in case the given value should be searchable
     *
     * @deprecated use {@link #getSearchContentType(I_CmsXmlContentValue)} instead. Will be removed if plain lucene search is removed.
     */
    @Deprecated
    default boolean isSearchable(I_CmsXmlContentValue value) {

        return Objects.equals(getSearchContentType(value), SearchContentType.TRUE);
    }

    /**
     * Returns if the given content field should be visible to the current user.<p>
     *
     * @param cms the cms context
     * @param schemaType the content value type
     * @param valuePath the value path
     * @param resource the edited resource
     * @param contentLocale the content locale
     *
     * @return <code>true</code> if the given content field should be visible to the current user
     */
    boolean isVisible(
        CmsObject cms,
        I_CmsXmlSchemaType schemaType,
        String valuePath,
        CmsResource resource,
        Locale contentLocale);

    /**
     * Prepares the given XML content to be used after it was read from the OpenCms VFS.<p>
     *
     * This method is always called after any content is unmarshalled.
     * It can be used to perform customized actions on the given XML content.<p>
     *
     * @param cms the current OpenCms user context
     * @param content the XML content to be used as read from the VFS
     *
     * @return the prepared content to be used
     */
    CmsXmlContent prepareForUse(CmsObject cms, CmsXmlContent content);

    /**
     * Prepares the given XML content to be written to the OpenCms VFS.<p>
     *
     * This method is always called before any content gets written.
     * It can be used to perform XML validation, pretty - printing
     * or customized actions on the given XML content.<p>
     *
     * @param cms the current OpenCms user context
     * @param content the XML content to be written
     * @param file the resource the XML content in it's current state was unmarshalled from
     *
     * @return the file to write to the OpenCms VFS, this will be an updated version of the parameter file
     *
     * @throws CmsException in case something goes wrong
     */
    CmsFile prepareForWrite(CmsObject cms, CmsXmlContent content, CmsFile file) throws CmsException;

    /**
     * Resolves the value mappings of the given XML content value, according
     * to the rules of this XML content handler.<p>
     *
     * @param cms the current OpenCms user context
     * @param content the XML content to resolve the mappings for
     * @param value the value to resolve the mappings for
     *
     * @throws CmsException if something goes wrong
     */
    void resolveMapping(CmsObject cms, CmsXmlContent content, I_CmsXmlContentValue value) throws CmsException;

    /**
     * Performs a validation of the given XML content value, and saves all errors or warnings found in
     * the provided XML content error handler.<p>
     *
     * The errorHandler parameter is optional, if <code>null</code> is given a new error handler
     * instance must be created.<p>
     *
     * @param cms the current OpenCms user context
     * @param value the value to resolve the validation rules for
     * @param errorHandler (optional) an error handler instance that contains previous error or warnings
     *
     * @return an error handler that contains all errors and warnings currently found
     */
    CmsXmlContentErrorHandler resolveValidation(
        CmsObject cms,
        I_CmsXmlContentValue value,
        CmsXmlContentErrorHandler errorHandler);

}
