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

package org.opencms.xml.content;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsMessages;
import org.opencms.main.CmsException;
import org.opencms.relations.CmsRelationType;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.util.CmsDefaultSet;
import org.opencms.widgets.I_CmsComplexWidget;
import org.opencms.widgets.I_CmsWidget;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.containerpage.CmsFormatterConfiguration;
import org.opencms.xml.types.I_CmsXmlContentValue;
import org.opencms.xml.types.I_CmsXmlSchemaType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.dom4j.Element;

/**
 * Handles special XML content livetime events, and also provides XML content editor rendering hints.<p>
 * 
 * @since 6.0.0 
 */
public interface I_CmsXmlContentHandler {

    /** List of all allowed attribute mapping names, for fast lookup. */
    List<String> ATTRIBUTES = Collections.unmodifiableList(Arrays.asList(new String[] {"datereleased", "dateexpired"}));

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
     * Gets the list of allowed template context names.<p> 
     * 
     * @return the list of allowed template context names 
     */
    CmsDefaultSet<String> getAllowedTemplates();

    /** 
     * Gets the complex widget for the given schema type.<p>
     * 
     * @param value the schema type for which we want the complex widget
     * 
     * @return the complex widget instance for the schema type 
     */
    I_CmsComplexWidget getComplexWidget(I_CmsXmlSchemaType value);

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
     * Returns the container page element formatter configuration for a given resource.<p>
     * 
     * @param cms the current users OpenCms context, used for selecting the right project
     * @param res the resource for which the formatter configuration should be retrieved
     * 
     * @return the container page element formatter configuration for this handler
     */
    CmsFormatterConfiguration getFormatterConfiguration(CmsObject cms, CmsResource res);

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
     * Returns the all mappings defined for the given element xpath.<p>
     * 
     * @param elementName the element xpath to look up the mapping for
     * 
     * @return the mapping defined for the given element xpath
     */
    List<String> getMappings(String elementName);

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
     * Returns all configured Search fields for this XML content.<p>
     * 
     * @return the Search fields for this XMl content
     */
    Set<CmsSearchField> getSearchFields();

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
    I_CmsWidget getWidget(I_CmsXmlSchemaType value) throws CmsXmlException;

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
     */
    boolean isSearchable(I_CmsXmlContentValue value);

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
