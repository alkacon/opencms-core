/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/ade/Attic/CmsADEElementCreator.java,v $
 * Date   : $Date: 2009/08/24 15:30:35 $
 * Version: $Revision: 1.1.2.1 $
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
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.PrintfFormat;
import org.opencms.workplace.CmsWorkplace;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Class for managing the creation of new content elements in ADE.<p>
 * 
 * XML files in the VFS can be used to configure which files are used as
 * prototypes for new elements, and which file names are used for the new
 * elements. 
 * 
 */
public class CmsADEElementCreator {

    /**
     * The tag name for a configuration.
     */
    public static final String TAG_CONFIG = "ADETypeConfiguration";

    /**
     * The tag name for the configuration of a single type.
    */
    public static final String TAG_TYPE = "ADEType";
    /**
     * The macro name for new file name patterns. 
     */
    public static final String MACRO_NUMBER = "number";

    /**
     * The format used for the macro replacement.
     */
    public static final String FILE_NUMBER_FORMAT = "%0.5d";

    /**
     * The "."-separated XML path for the tag which stores the source file of a type.
     */
    public static final String XML_PATH_SOURCE = "Source";

    /**
     * The "."-separated XML path for the tag which stores the creation destination of a type.
     */
    public static final String XML_PATH_DESTINATION = "Destination";

    private Map<String, CmsADETypeConfigurationItem> m_configuration;

    /**
     * Constructs a new instance.<p>
     * 
     *  @param cms the CmsObject used for reading the configuration
     *  @param configPath the VFS path of the configuration file. 
     *  @throws Exception if something goes wrong
     */
    public CmsADEElementCreator(CmsObject cms, String configPath)
    throws Exception {

        m_configuration = new HashMap<String, CmsADETypeConfigurationItem>();
        parseConfiguration(cms, readConfiguration(cms, configPath));
    }

    /**
     * Helper method for getting a nested sub-element of an XML element.<p>
     * 
     * @param elem the element from which a sub-element should be retrieved 
     * @param path the path of the sub-element, consisting of a "."-separated list of tag names.
     * @return the sub-element at the given path
     */
    private Element getSubElement(Element elem, String path) {

        String[] tags = path.split("\\.");
        for (String tag : tags) {
            elem = (Element)elem.getElementsByTagName(tag).item(0);
        }
        return elem;

    }

    /**
     * Helper method for retrieving the OpenCms type name for a given type id.<p>
     * @param typeId the id of the type
     * @return the name of the type
     * @throws CmsException
     */
    private static String getTypeName(int typeId) throws CmsException {

        return OpenCms.getResourceManager().getResourceType(typeId).getTypeName();
    }

    /**
     * Reads the configuration from an XML file in the VFS.<p>
     * 
     * @param cms the CmsObject used for reading the configuration
     * @param configPath the path of the configuration file
     * @return the XML DOM element of the configuration
     * @throws Exception if the reading or parsing goes wrong. 
     */
    protected Element readConfiguration(CmsObject cms, String configPath) throws Exception {

        CmsFile file = cms.readFile(configPath);
        byte[] contents = file.getContents();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new ByteArrayInputStream(contents));
        Element element = document.getDocumentElement();
        NodeList configurations = element.getElementsByTagName(TAG_CONFIG);
        return (Element)configurations.item(0);
    }

    /**
     * Configures this element based on the configuration in an XML DOM tree.<p> 
     * @param cms the CmsObject used for determining the types of files in the configuration. 
     * @param config the XML DOM tree containing the configuration
     * @throws CmsException if something goes wrong.
     */
    protected void parseConfiguration(CmsObject cms, Element config) throws CmsException {

        NodeList nodes = config.getElementsByTagName(TAG_TYPE);
        for (int i = 0; i < nodes.getLength(); i++) {
            Element configElem = (Element)nodes.item(i);
            String sourceFileName = getSubElement(configElem, XML_PATH_SOURCE).getTextContent();
            String destination = getSubElement(configElem, XML_PATH_DESTINATION).getTextContent();
            CmsFile sourceFile = cms.readFile(sourceFileName);
            CmsADETypeConfigurationItem item = new CmsADETypeConfigurationItem(sourceFileName, destination);
            m_configuration.put(getTypeName(sourceFile.getTypeId()), item);
        }
    }

    /**
     * Returns the configuration as an unmodifiable map.<p>
     * 
     * @return the configuration as an unmodifiable map.
     */
    public Map<String, CmsADETypeConfigurationItem> getConfiguration() {

        return Collections.unmodifiableMap(m_configuration);
    }

    /**
     * Creates a new element of a given type at the configured location.<p>
     * @param cms the CmsObject used for creating the new element.
     * @param type the type of the element to be created.
     * @return the CmsResource representing the newly created element.
     * @throws CmsException if something goes wrong.
     */
    public CmsResource createElement(CmsObject cms, String type) throws CmsException {

        CmsADETypeConfigurationItem configItem = m_configuration.get(type);
        CmsResource resource = cms.readResource(configItem.getSourceFile());
        String destination = configItem.getDestination();
        String newFileName = getNewFileName(cms, destination);
        cms.copyResource(cms.getSitePath(resource), newFileName);
        return cms.readResource(newFileName);
    }

    /**
     * Returns a new file name for an element to be created based on a pattern.<p>
     * 
     * The pattern consists of a path which may contain the macro %(number), which 
     * will be replaced by the first 5-digit sequence for which the resulting file name is not already
     * used.
     * 
     * Although this method is synchronized, it may still return a used file name in the unlikely
     * case that it is called after a previous call to this method, but before the resulting file name
     * was used to create a file.  
     * 
     * This method was adapted from the method A_CmsResourceCollector#getCreateInFolder
     *
     * @param cms the CmsObject used for checking the existence of file names.
     * @param pattern the pattern for new files. 
     * @return the new file name. 
     * @throws CmsException if something goes wrong.
     */
    public static synchronized String getNewFileName(CmsObject cms, String pattern) throws CmsException {

        // this method was adapted from A_CmsResourceCollector#getCreateInFolder

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

}
