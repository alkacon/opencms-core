/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/containerpage/Attic/CmsConfigurationParser.java,v $
 * Date   : $Date: 2010/09/03 13:27:35 $
 * Version: $Revision: 1.7 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.xml.containerpage;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalStateException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.PrintfFormat;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.CmsXmlContentProperty;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Class for managing the creation of new content elements in ADE.<p>
 * 
 * XML files in the VFS can be used to configure which files are used as
 * prototypes for new elements, and which file names are used for the new
 * elements.<p> 
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.7 $ 
 * 
 * @since 7.6 
 */
public class CmsConfigurationParser {

    /** The format used for the macro replacement. */
    public static final String FILE_NUMBER_FORMAT = "%0.4d";

    /** The macro name for new file name patterns. */
    public static final String MACRO_NUMBER = "number";

    /** The tag name of the configuration for a single type. */
    public static final String N_ADE_TYPE = "ADEType";

    /** The tag name of the destination in the type configuration. */
    public static final String N_DESTINATION = "Destination";

    /** The tag name of the source file in the type configuration. */
    public static final String N_FOLDER = "Folder";

    /** The tag name of the source file in the type configuration. */
    public static final String N_PATTERN = "Pattern";

    /** The tag name of the source file in the type configuration. */
    public static final String N_SOURCE = "Source";

    /** The tag name for elements containing field configurations. */
    private static final String N_ADE_FIELD = "ADEField";

    /** Configuration data, read from xml content. */
    private Map<String, CmsConfigurationItem> m_configuration;

    /** New elements. */
    private List<CmsResource> m_newElements;

    /** The list of properties read from the configuration file. */
    private List<CmsXmlContentProperty> m_props = new ArrayList<CmsXmlContentProperty>();

    /**
     * Constructs a new instance.<p>
     * 
     * @param cms the cms context used for reading the configuration
     * @param config the configuration file
     *  
     * @throws CmsException if something goes wrong
     */
    public CmsConfigurationParser(CmsObject cms, CmsResource config)
    throws CmsException {

        if (config.getTypeId() != CmsResourceTypeXmlContainerPage.CONFIGURATION_TYPE_ID) {
            throw new CmsIllegalStateException(Messages.get().container(
                Messages.ERR_CONFIG_WRONG_TYPE_2,
                CmsPropertyDefinition.PROPERTY_ADE_CNTPAGE_CONFIG,
                cms.getSitePath(config)));
        }

        m_configuration = new HashMap<String, CmsConfigurationItem>();
        m_newElements = new ArrayList<CmsResource>();

        CmsFile configFile = cms.readFile(config);
        I_CmsXmlDocument content = CmsXmlContentFactory.unmarshal(cms, configFile);
        parseConfiguration(cms, content);
    }

    /**
     * Returns the configuration as an unmodifiable map.<p>
     * 
     * @return the configuration as an unmodifiable map
     */
    public Map<String, CmsConfigurationItem> getConfiguration() {

        return Collections.unmodifiableMap(m_configuration);
    }

    /**
     * Returns an unmodifiable list of properties defined in the configuration file.<p>
     *  
     * @return the list of properties defined in the configuration file 
     */
    public List<CmsXmlContentProperty> getDefinedProperties() {

        return Collections.unmodifiableList(m_props);
    }

    /**
     * Returns the new elements.<p>
     * 
     * @return the new elements
     */
    public List<CmsResource> getNewElements() {

        return Collections.unmodifiableList(m_newElements);
    }

    /**
     * Returns a new file name for an element to be created based on a pattern.<p>
     * 
     * The pattern consists of a path which may contain the macro %(number), which 
     * will be replaced by the first 5-digit sequence for which the resulting file name is not already
     * used.<p>
     * 
     * Although this method is synchronized, it may still return a used file name in the unlikely
     * case that it is called after a previous call to this method, but before the resulting file name
     * was used to create a file.<p>  
     * 
     * This method was adapted from the method {@link org.opencms.file.collectors.A_CmsResourceCollector}<code>#getCreateInFolder</code>.<p>
     *
     * @param cms the CmsObject used for checking the existence of file names
     * @param pattern the pattern for new files
     * 
     * @return the new file name
     * 
     * @throws CmsException if something goes wrong
     */
    public String getNewFileName(CmsObject cms, String pattern) throws CmsException {

        // this method was adapted from A_CmsResourceCollector#getCreateInFolder
        pattern = cms.getRequestContext().removeSiteRoot(pattern);
        PrintfFormat format = new PrintfFormat(FILE_NUMBER_FORMAT);
        String folderName = CmsResource.getFolderPath(pattern);
        List<CmsResource> resources = cms.readResources(folderName, CmsResourceFilter.ALL, false);
        // now create a list of all resources that just contains the file names
        Set<String> result = new HashSet<String>();
        for (int i = 0; i < resources.size(); i++) {
            CmsResource resource = resources.get(i);
            result.add(cms.getSitePath(resource));
        }

        String checkFileName, checkTempFileName, number;
        CmsMacroResolver resolver = CmsMacroResolver.newInstance();
        int j = 0;
        do {
            number = format.sprintf(++j);
            resolver.addMacro(MACRO_NUMBER, number);
            // resolve macros in file name
            checkFileName = resolver.resolveMacros(pattern);
            // get name of the resolved temp file
            checkTempFileName = CmsWorkplace.getTemporaryFileName(checkFileName);
        } while (result.contains(checkFileName) || result.contains(checkTempFileName));
        return checkFileName;
    }

    /**
     * Parses a type configuration contained in an XML content.<p>
     * 
     * This method uses the first locale from the following list which has a corresponding
     * element in the XML content:
     * <ul>
     *  <li>the request context's locale</li>
     *  <li>the default locale</li>
     *  <li>the first locale available in the XML content</li>
     * </ul><p>
     *
     * @param cms the CmsObject to use for VFS operations
     * @param content the XML content with the type configuration
     * 
     * @throws CmsException if something goes wrong
     */
    public void parseConfiguration(CmsObject cms, I_CmsXmlDocument content) throws CmsException {

        Locale locale = getLocale(cms, content);
        List<I_CmsXmlContentValue> typeValues = content.getValues(N_ADE_TYPE, locale);
        for (I_CmsXmlContentValue xmlType : typeValues) {
            parseType(cms, xmlType, locale);
        }

        List<I_CmsXmlContentValue> fieldValues = content.getValues(N_ADE_FIELD, locale);
        for (I_CmsXmlContentValue xmlField : fieldValues) {
            parseField(cms, xmlField, locale);
        }

    }

    /**
     * Helper method for finding the locale for accessing the XML content.<p>
     * 
     * @param cms the CMS context 
     * @param content the XML content 
     * 
     * @return the locale
     * 
     * @throws CmsException if something goes wrong 
     */
    protected Locale getLocale(CmsObject cms, I_CmsXmlDocument content) throws CmsException {

        Locale currentLocale = cms.getRequestContext().getLocale();
        Locale defaultLocale = CmsLocaleManager.getDefaultLocale();
        Locale locale = null;
        if (content.hasLocale(currentLocale)) {
            locale = currentLocale;
        } else if (content.hasLocale(defaultLocale)) {
            locale = defaultLocale;
        } else {
            List<Locale> locales = content.getLocales();
            if (locales.size() == 0) {
                throw new CmsException(Messages.get().container(
                    Messages.ERR_NO_TYPE_CONFIG_1,
                    content.getFile().getRootPath()));
            }
            locale = locales.get(0);
        }
        return locale;
    }

    /**
     * Returns a child content value of a given content value as a string.<p>
     *  
     * @param cms the CMS context 
     * @param value the parent content value
     * @param locale the locale 
     * @param subPath the name of the child content value, relative to the parent's path 
     * 
     * @return the child content value as a string 
     */
    protected String getSubValueAsString(CmsObject cms, I_CmsXmlContentValue value, Locale locale, String subPath) {

        I_CmsXmlContentValue subValue = value.getDocument().getValue(
            CmsXmlUtils.concatXpath(value.getPath(), subPath),
            locale);
        return subValue != null ? subValue.getStringValue(cms) : null;
    }

    /**
     * Helper method for retrieving the OpenCms type name for a given type id.<p>
     * 
     * @param typeId the id of the type
     * 
     * @return the name of the type
     * 
     * @throws CmsException if something goes wrong
     */
    protected String getTypeName(int typeId) throws CmsException {

        return OpenCms.getResourceManager().getResourceType(typeId).getTypeName();
    }

    /**
     * Parses a single field definition from a content value.<p>
     * 
     * @param cms the CMS context 
     * @param xmlField the content value to parse the field from 
     * @param locale the locale to use 
     */
    private void parseField(CmsObject cms, I_CmsXmlContentValue xmlField, Locale locale) {

        String name = getSubValueAsString(cms, xmlField, locale, "Name");
        String type = getSubValueAsString(cms, xmlField, locale, "Type");
        String widget = getSubValueAsString(cms, xmlField, locale, "Widget");
        String widgetConfig = getSubValueAsString(cms, xmlField, locale, "WidgetConfig");
        String ruleRegex = getSubValueAsString(cms, xmlField, locale, "RuleRegex");
        String ruleType = getSubValueAsString(cms, xmlField, locale, "RuleType");
        String default1 = getSubValueAsString(cms, xmlField, locale, "Default");
        String error = getSubValueAsString(cms, xmlField, locale, "Error");
        String niceName = getSubValueAsString(cms, xmlField, locale, "NiceName");
        String description = getSubValueAsString(cms, xmlField, locale, "Description");
        String advanced = getSubValueAsString(cms, xmlField, locale, "Advanced");
        CmsXmlContentProperty prop = new CmsXmlContentProperty(
            name,
            type,
            widget,
            widgetConfig,
            ruleRegex,
            ruleType,
            default1,
            niceName,
            description,
            error,
            advanced);
        m_props.add(prop);
    }

    /**
     * Internal method for parsing the element types in the configuration file.<p>
     * 
     * @param cms the CMS context 
     * @param xmlType a content value representing an element type 
     * @param locale the locale to use 
     * 
     * @throws CmsException if something goes wrong 
     */
    private void parseType(CmsObject cms, I_CmsXmlContentValue xmlType, Locale locale) throws CmsException {

        String source = getSubValueAsString(cms, xmlType, locale, N_SOURCE);
        String folder = getSubValueAsString(cms, xmlType, locale, CmsXmlUtils.concatXpath(N_DESTINATION, N_FOLDER));
        String pattern = getSubValueAsString(cms, xmlType, locale, CmsXmlUtils.concatXpath(N_DESTINATION, N_PATTERN));

        CmsConfigurationItem configItem = new CmsConfigurationItem(source, folder, pattern);
        CmsResource resource = cms.readResource(source);
        String type = getTypeName(resource.getTypeId());
        m_configuration.put(type, configItem);

        // checking access entries for the explorer-type
        CmsResource folderRes = cms.readResource(folder);
        CmsExplorerTypeSettings settings = OpenCms.getWorkplaceManager().getExplorerTypeSetting(
            OpenCms.getResourceManager().getResourceType(resource).getTypeName());
        boolean editable = settings.isEditable(cms, folderRes);
        //TODO: fix wrong permission test for explorer-types
        boolean controlPermission = settings.getAccess().getPermissions(cms, folderRes).requiresControlPermission();
        if (editable && controlPermission) {
            m_newElements.add(resource);
        }
    }
}
