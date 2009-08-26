/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/ade/Attic/CmsADEElementCreator.java,v $
 * Date   : $Date: 2009/08/26 07:58:18 $
 * Version: $Revision: 1.1.2.5 $
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

package org.opencms.workplace.editors.ade;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.json.JSONObject;
import org.opencms.loader.CmsContainerPageLoader;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.PrintfFormat;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
 * @version $Revision: 1.1.2.5 $ 
 * 
 * @since 7.6 
 */
public class CmsADEElementCreator {

    /** The format used for the macro replacement. */
    public static final String FILE_NUMBER_FORMAT = "%0.5d";

    /** The macro name for new file name patterns. */
    public static final String MACRO_NUMBER = "number";

    /** The tag name of the configuration for a single type. */
    public static final String N_ADE_TYPE = "ADEType";

    /** The tag name of the destination in the type configuration. */
    public static final String N_DESTINATION = "Destination";

    /** The tag name of the source file in the type configuration. */
    public static final String N_SOURCE = "Source";

    /** Container page loader reference. */
    private static final CmsContainerPageLoader LOADER = (CmsContainerPageLoader)OpenCms.getResourceManager().getLoader(
        CmsContainerPageLoader.RESOURCE_LOADER_ID);

    /** Configuration data, read from xml content. */
    private Map<String, CmsADETypeConfigurationItem> m_configuration;

    /**
     * Constructs a new instance.<p>
     * 
     * @param cms the cms context used for reading the configuration
     * @param containerPage the container page
     *  
     * @throws CmsException if something goes wrong
     */
    public CmsADEElementCreator(CmsObject cms, CmsResource containerPage)
    throws CmsException {

        JSONObject localeData = LOADER.getCache(cms, containerPage, cms.getRequestContext().getLocale());
        String configPath = localeData.optString(CmsContainerPageLoader.N_NEW_CONFIG, "");
        // configPath the VFS path of the configuration file
        m_configuration = new HashMap<String, CmsADETypeConfigurationItem>();
        CmsFile configFile = cms.readFile(configPath);
        I_CmsXmlDocument content = CmsXmlContentFactory.unmarshal(cms, configFile);
        parseConfiguration(cms, content);
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
    public static synchronized String getNewFileName(CmsObject cms, String pattern) throws CmsException {

        // this method was adapted from A_CmsResourceCollector#getCreateInFolder
        pattern = cms.getRequestContext().removeSiteRoot(pattern);
        PrintfFormat format = new PrintfFormat(FILE_NUMBER_FORMAT);
        String folderName = CmsResource.getFolderPath(pattern);
        List resources = cms.readResources(folderName, CmsResourceFilter.ALL, false);
        // now create a list of all resources that just contains the file names
        Set<String> result = new HashSet<String>();
        for (int i = 0; i < resources.size(); i++) {
            CmsResource resource = (CmsResource)resources.get(i);
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
     * Helper method for retrieving the OpenCms type name for a given type id.<p>
     * 
     * @param typeId the id of the type
     * 
     * @return the name of the type
     * 
     * @throws CmsException if something goes wrong
     */
    private static String getTypeName(int typeId) throws CmsException {

        return OpenCms.getResourceManager().getResourceType(typeId).getTypeName();
    }

    /**
     * Creates a new element of a given type at the configured location.<p>
     * 
     * @param cms the CmsObject used for creating the new element
     * @param type the type of the element to be created
     * 
     * @return the CmsResource representing the newly created element
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsResource createElement(CmsObject cms, String type) throws CmsException {

        CmsADETypeConfigurationItem configItem = m_configuration.get(type);
        String destination = configItem.getDestination();
        String newFileName = getNewFileName(cms, destination);
        cms.copyResource(configItem.getSourceFile(), newFileName);
        return cms.readResource(newFileName);
    }

    /**
     * Returns the configuration as an unmodifiable map.<p>
     * 
     * @return the configuration as an unmodifiable map
     */
    public Map<String, CmsADETypeConfigurationItem> getConfiguration() {

        return Collections.unmodifiableMap(m_configuration);
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

        Locale currentLocale = cms.getRequestContext().getLocale();
        Locale defaultLocale = CmsLocaleManager.getDefaultLocale();
        Locale locale = null;
        if (content.hasLocale(currentLocale)) {
            locale = currentLocale;
        } else if (content.hasLocale(defaultLocale)) {
            locale = defaultLocale;
        } else {
            List locales = content.getLocales();
            if (locales.size() == 0) {

                throw new CmsException(Messages.get().container(
                    Messages.ERR_NO_TYPE_CONFIG_1,
                    content.getFile().getRootPath()));
            }
            locale = (Locale)locales.get(0);
        }

        Iterator itTypes = content.getValues(N_ADE_TYPE, locale).iterator();
        while (itTypes.hasNext()) {
            I_CmsXmlContentValue xmlType = (I_CmsXmlContentValue)itTypes.next();
            String typePath = xmlType.getPath();
            String source = content.getValue(CmsXmlUtils.concatXpath(typePath, N_SOURCE), locale).getStringValue(cms);
            String destination = content.getValue(CmsXmlUtils.concatXpath(typePath, N_DESTINATION), locale).getStringValue(
                cms);
            CmsADETypeConfigurationItem configItem = new CmsADETypeConfigurationItem(source, destination);
            CmsResource resource = cms.readResource(source);
            String type = getTypeName(resource.getTypeId());
            m_configuration.put(type, configItem);
        }
    }
}
