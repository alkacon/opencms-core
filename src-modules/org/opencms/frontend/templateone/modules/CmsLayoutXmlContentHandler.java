/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/frontend/templateone/modules/CmsLayoutXmlContentHandler.java,v $
 * Date   : $Date: 2011/03/23 14:53:03 $
 * Version: $Revision: 1.9 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.frontend.templateone.modules;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.frontend.templateone.CmsTemplateContentListItem;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.content.CmsDefaultXmlContentHandler;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.Messages;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A special XML content handler, will be used by all XML contents that create layout files.<p>
 * 
 * Layout files with properties attached to them are used to create
 * structured content lists in the center or on the right side of template one.<p>
 * 
 * @author Andreas Zahner 
 * 
 * @version $Revision: 1.9 $ 
 * 
 * @since 6.1.7
 */
public class CmsLayoutXmlContentHandler extends CmsDefaultXmlContentHandler {

    /** The resource type name of the configuration files using this handler. */
    public static final String CONFIG_RESTYPE_NAME = "layout";

    /** The element name for the layout item collector name. */
    public static final String ELEMENT_COLLECTOR = "Collector";

    /** The element name for the layout item list count. */
    public static final String ELEMENT_COUNT = "Count";

    /** The element name for the layout. */
    public static final String ELEMENT_LAYOUT = "Layout";

    /** The element name for the layout item resource type. */
    public static final String ELEMENT_RESOURCETYPE = "Type";

    /** The element name for the layout item VFS folder. */
    public static final String ELEMENT_VFSFOLDER = "Folder";

    /** The name of the leyout column property. */
    public static final String PROPERTY_LAYOUT_COLUMN = "layout.column";

    /**
     * Creates a new instance of the default XML content handler.<p>  
     */
    public CmsLayoutXmlContentHandler() {

        super();
    }

    /**
     * This overwrites the super implementation to revolve additional fixed property mappings.<p>
     * 
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#prepareForWrite(org.opencms.file.CmsObject, org.opencms.xml.content.CmsXmlContent, org.opencms.file.CmsFile)
     */
    @Override
    public CmsFile prepareForWrite(CmsObject cms, CmsXmlContent content, CmsFile file) throws CmsException {

        super.prepareForWrite(cms, content, file);
        resolveLayoutMappings(cms, content);
        removeEmptyLayoutMappings(cms, content);
        return file;
    }

    /**
     * Removes the layout property values on resources for non-existing, optional elements.<p>
     * 
     * @param cms the current users OpenCms context
     * @param content the XML content to remove the property values for
     * 
     * @throws CmsException in case of read/write errors accessing the OpenCms VFS
     */
    protected void removeEmptyLayoutMappings(CmsObject cms, CmsXmlContent content) throws CmsException {

        super.removeEmptyMappings(cms, content);

        // get root path of the file
        String rootPath = content.getFile().getRootPath();
        List siblings = null;
        String storedSiteRoot = cms.getRequestContext().getSiteRoot();
        
        try {
            // try / catch to ensure site root is always restored
            cms.getRequestContext().setSiteRoot("/");

            // read all siblings of the file
            siblings = cms.readSiblings(rootPath, CmsResourceFilter.IGNORE_EXPIRATION);

            for (int i = 0; i < siblings.size(); i++) {

                // get sibline filename and locale
                String filename = ((CmsResource)siblings.get(i)).getRootPath();
                Locale locale = OpenCms.getLocaleManager().getDefaultLocale(cms, filename);

                if (!content.hasLocale(locale)) {
                    // only remove property if the locale fits
                    continue;
                }

                List nestedItems = content.getValues(ELEMENT_LAYOUT, locale);

                // delete properties for an eventually deleted nested layout item
                int deleteIndex = nestedItems.size() + 1;

                // create resolver to resolve property definition names
                CmsMacroResolver resolver = CmsMacroResolver.newInstance();
                resolver.addMacro(CmsTemplateContentListItem.MACRO_LISTINDEX, "" + deleteIndex);

                List properties = new ArrayList(4);

                // delete the resource type property
                CmsProperty p = new CmsProperty(
                    resolver.resolveMacros(CmsTemplateContentListItem.PROPERTY_LAYOUT_TYPE),
                    CmsProperty.DELETE_VALUE,
                    null);
                properties.add(p);

                // delete the vfs folder property
                p = new CmsProperty(
                    resolver.resolveMacros(CmsTemplateContentListItem.PROPERTY_LAYOUT_FOLDER),
                    CmsProperty.DELETE_VALUE,
                    null);
                properties.add(p);

                // delete the list count property
                p = new CmsProperty(
                    resolver.resolveMacros(CmsTemplateContentListItem.PROPERTY_LAYOUT_COUNT),
                    CmsProperty.DELETE_VALUE,
                    null);
                properties.add(p);

                // delete the collector property
                p = new CmsProperty(
                    resolver.resolveMacros(CmsTemplateContentListItem.PROPERTY_LAYOUT_COLLECTOR),
                    CmsProperty.DELETE_VALUE,
                    null);
                properties.add(p);

                // write the deleted property values
                cms.writePropertyObjects(filename, properties);
            }

        } finally {
            // restore the saved site root
            cms.getRequestContext().setSiteRoot(storedSiteRoot);
        }
    }

    /**
     * Resolves the special layout property mappings for the resource.<p>
     * 
     * These mappings are not defined in the XSD, they are fixed for the layout configuration XML contents.<p>
     * 
     * @param cms the current users OpenCms context
     * @param content the XML content to map the layout property values for
     * @throws CmsException in case of read/write errors accessing the OpenCms VFS
     */
    protected void resolveLayoutMappings(CmsObject cms, CmsXmlContent content) throws CmsException {

        // get the original VFS file from the content
        CmsFile file = content.getFile();
        if (file == null) {
            throw new CmsXmlException(Messages.get().container(Messages.ERR_XMLCONTENT_RESOLVE_FILE_NOT_FOUND_0));
        }

        // get root path of the file 
        String rootPath = content.getFile().getRootPath();
        String storedSiteRoot = cms.getRequestContext().getSiteRoot();

        try {
            // try / catch to ensure site root is always restored
            cms.getRequestContext().setSiteRoot("/");

            // read all siblings of the file
            List siblings = cms.readSiblings(rootPath, CmsResourceFilter.IGNORE_EXPIRATION);

            // for multilanguage mappings, we need to ensure 
            // a) all siblings are handled
            // b) only the "right" locale is mapped to a sibling

            for (int i = (siblings.size() - 1); i >= 0; i--) {
                // get filename
                String filename = ((CmsResource)siblings.get(i)).getRootPath();
                Locale locale = OpenCms.getLocaleManager().getDefaultLocale(cms, filename);

                if (!content.hasLocale(locale)) {
                    // only write properties if the locale fits
                    continue;
                }

                // iterate the found layout definitions to set the properties
                List nestedItems = content.getValues(ELEMENT_LAYOUT, locale);
                List properties = new ArrayList(nestedItems.size() * 4);
                for (int k = 0; k < nestedItems.size(); k++) {
                    I_CmsXmlContentValue layoutConfig = (I_CmsXmlContentValue)nestedItems.get(k);
                    String layoutConfigPath = layoutConfig.getPath() + "/";

                    // create resolver to resolve property definition names
                    CmsMacroResolver resolver = CmsMacroResolver.newInstance();
                    resolver.addMacro(CmsTemplateContentListItem.MACRO_LISTINDEX, "" + (layoutConfig.getIndex() + 1));

                    // set the resource type property
                    String resType = content.getStringValue(cms, layoutConfigPath + ELEMENT_RESOURCETYPE, locale);
                    CmsProperty p = new CmsProperty(
                        resolver.resolveMacros(CmsTemplateContentListItem.PROPERTY_LAYOUT_TYPE),
                        resType,
                        null);
                    properties.add(p);

                    // set the vfs folder property
                    String folder = content.getStringValue(cms, layoutConfigPath + ELEMENT_VFSFOLDER, locale);
                    if (CmsStringUtil.isNotEmpty(folder)) {
                        // folder is set, eventually remove site root from path
                        if ((folder.length() > 1) && folder.startsWith(storedSiteRoot)) {
                            folder = folder.substring(storedSiteRoot.length());
                        }
                    } else {
                        // folder not set, remove property value
                        folder = CmsProperty.DELETE_VALUE;
                    }
                    p = new CmsProperty(
                        resolver.resolveMacros(CmsTemplateContentListItem.PROPERTY_LAYOUT_FOLDER),
                        folder,
                        null);
                    properties.add(p);

                    // set the list count property
                    String count = content.getStringValue(cms, layoutConfigPath + ELEMENT_COUNT, locale);
                    p = new CmsProperty(
                        resolver.resolveMacros(CmsTemplateContentListItem.PROPERTY_LAYOUT_COUNT),
                        count,
                        null);
                    properties.add(p);

                    // set the collector property
                    String collector = content.getStringValue(cms, layoutConfigPath + ELEMENT_COLLECTOR, locale);
                    p = new CmsProperty(
                        resolver.resolveMacros(CmsTemplateContentListItem.PROPERTY_LAYOUT_COLLECTOR),
                        collector,
                        null);
                    properties.add(p);
                }

                // write all property objects
                cms.writePropertyObjects(filename, properties);
            }

        } finally {
            // restore the saved site root
            cms.getRequestContext().setSiteRoot(storedSiteRoot);
        }
    }

}