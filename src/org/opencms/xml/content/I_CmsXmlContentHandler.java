/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/content/I_CmsXmlContentHandler.java,v $
 * Date   : $Date: 2005/05/19 16:35:47 $
 * Version: $Revision: 1.16 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.xml.content;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsMessages;
import org.opencms.main.CmsException;
import org.opencms.widgets.I_CmsWidget;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.types.I_CmsXmlContentValue;
import org.opencms.xml.types.I_CmsXmlSchemaType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.dom4j.Element;

/**
 * Handles special XML content livetime events, and also provides XML content editor rendering hints.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.16 $
 * @since 5.5.4
 */
public interface I_CmsXmlContentHandler {

    /** Array of all allowed attribute mapping names. */
    String[] C_ATTRIBUTES = {"datereleased", "dateexpired"};

    /** List of all allowed attribute mapping names, for fast lookup. */
    List C_ATTRIBUTES_LIST = Collections.unmodifiableList(Arrays.asList(C_ATTRIBUTES));

    /** Prefix for attribute mappings. */
    String C_MAPTO_ATTRIBUTE = "attribute:";

    /** Prefix for property mappings. */
    String C_MAPTO_PROPERTY = "property:";

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
     * Returns the default String value for the given XML content schema type object in the given XML content.<p> 
     * 
     * If a schema type does not have a default value, this method must return <code>null</code>.
     * 
     * @param cms the current users OpenCms context
     * @param type the value to get the default for
     * @param locale the currently selected locale for the value
     * 
     * @return the default String value for the given XML content value object
     * 
     * @see org.opencms.xml.types.I_CmsXmlSchemaType#getDefault(Locale)
     */
    String getDefault(CmsObject cms, I_CmsXmlSchemaType type, Locale locale);

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
    I_CmsWidget getWidget(I_CmsXmlContentValue value) throws CmsXmlException;

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
     * Prepares the given XML content to be written to the OpenCms VFS.<p>
     * 
     * This method is alway called before any content gets written.
     * It can be used to perform XML validation, pretty - printing 
     * or customized actions on the given XML content.<p>  
     * 
     * @param cms the current OpenCms user context
     * @param content the XML content to be written
     * @param file the resource the XML content in it's current state was unmarshalled from 
     * 
     * @return the file to write to the OpenCms VFS, this will be an updated vresion of the parameter file
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