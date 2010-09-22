/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/types/Attic/CmsResourceTypeJspRenderer.java,v $
 * Date   : $Date: 2010/09/22 14:27:47 $
 * Version: $Revision: 1.5 $
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

package org.opencms.file.types;

import org.opencms.db.CmsSecurityManager;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.CmsDefaultXmlContentHandler;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;

/**
 * Resource type descriptor for the type "jsprenderer".<p>
 *
 * @author Tobias Herrmann 
 * @author Andreas Zahner
 * 
 * @version $Revision: 1.5 $ 
 * 
 * @since 7.9.0 
 */
public class CmsResourceTypeJspRenderer extends CmsResourceTypeXmlContent {

    /** Module parameter prefix for the container types. */
    public static final String MODULE_PARAM_SUFFIX_CONTAINERTYPES = ".ade.containertypes";

    /** Module parameter prefix for the formatter. */
    public static final String MODULE_PARAM_SUFFIX_FORMATTER = ".ade.formatter";

    /** Property name for container types property. */
    protected static final String PROPERTY_CONTAINERTYPES = "ade.containertypes";

    /** Property name for formatter property. */
    protected static final String PROPERTY_FORMATTER = "ade.formatter";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsResourceTypeJspRenderer.class);

    /** Macro key to resolve the container type in the formatter VFS path. */
    private static final String MACRO_CONTAINERTYPE = "ade.containertype";

    /**
     * @see org.opencms.file.types.CmsResourceTypeXmlContent#createResource(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, java.lang.String, byte[], java.util.List)
     */
    @Override
    public CmsResource createResource(
        CmsObject cms,
        CmsSecurityManager securityManager,
        String resourcename,
        byte[] content,
        List<CmsProperty> properties) throws CmsException {

        boolean addPropertyFormatter = true;
        boolean addPropertyContainerTypes = true;
        for (Iterator<CmsProperty> i = properties.iterator(); i.hasNext();) {
            CmsProperty property = i.next();
            // check if there are already formatter or container types properties defined to be set on creation
            if (property.getName().equals(PROPERTY_CONTAINERTYPES)) {
                addPropertyContainerTypes = false;
            } else if (property.getName().equals(PROPERTY_FORMATTER)) {
                addPropertyFormatter = false;
            }
        }

        if (addPropertyContainerTypes || addPropertyFormatter) {
            // at least one property has to be set additionally
            I_CmsResourceType resType = OpenCms.getResourceManager().getResourceType(getTypeId());
            if (resType.isAdditionalModuleResourceType()) {
                String moduleName = resType.getModuleName();
                CmsModule module = OpenCms.getModuleManager().getModule(moduleName);
                if (addPropertyContainerTypes) {
                    // add the container types property
                    String types = module.getParameter(resType.getTypeName() + MODULE_PARAM_SUFFIX_CONTAINERTYPES);
                    if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(types)) {
                        properties.add(new CmsProperty(PROPERTY_CONTAINERTYPES, null, types));
                    }
                }
                if (addPropertyFormatter) {
                    // add the formatter property
                    String formatter = module.getParameter(resType.getTypeName() + MODULE_PARAM_SUFFIX_FORMATTER);
                    if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(formatter)) {
                        properties.add(new CmsProperty(PROPERTY_FORMATTER, null, formatter));
                    }
                }
            }
        }
        // call super implementation with eventually extended property list
        return super.createResource(cms, securityManager, resourcename, content, properties);
    }

    /**
     * @see org.opencms.file.types.CmsResourceTypeXmlContent#getFormatterForContainerTypeAndWidth(org.opencms.file.CmsObject, org.opencms.file.CmsResource, java.lang.String, int)
     */
    @Override
    public String getFormatterForContainerTypeAndWidth(
        CmsObject cms,
        CmsResource resource,
        String containerType,
        int width) {

        if (CmsDefaultXmlContentHandler.DEFAULT_FORMATTER_TYPE.equals(containerType)) {
            return CmsDefaultXmlContentHandler.DEFAULT_FORMATTER;
        }
        try {
            String formatter = cms.readPropertyObject(resource, PROPERTY_FORMATTER, true).getValue();
            List<String> types = Collections.emptyList();
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(formatter)) {
                // try to read the formatter from module parameters
                I_CmsResourceType resType = OpenCms.getResourceManager().getResourceType(resource);
                if (resType.isAdditionalModuleResourceType()) {
                    String moduleName = resType.getModuleName();
                    CmsModule module = OpenCms.getModuleManager().getModule(moduleName);
                    // read formatter and container types from module parameters
                    formatter = module.getParameter(resType.getTypeName() + MODULE_PARAM_SUFFIX_FORMATTER);
                    String typesStr = module.getParameter(resType.getTypeName() + MODULE_PARAM_SUFFIX_CONTAINERTYPES);
                    if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(typesStr)) {
                        types = CmsStringUtil.splitAsList(typesStr, CmsProperty.VALUE_LIST_DELIMITER);
                    }
                }
            }
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(formatter)) {
                // found a formatter, now resolve container type macro in formatter path (if present)
                CmsMacroResolver resolver = CmsMacroResolver.newInstance().setKeepEmptyMacros(true);
                resolver.addMacro(MACRO_CONTAINERTYPE, containerType);
                formatter = resolver.resolveMacros(formatter);
                if (types.isEmpty()) {
                    // still not found types, read them from the property
                    types = cms.readPropertyObject(resource, PROPERTY_CONTAINERTYPES, true).getValueList();
                }
                if ((types == null) || types.isEmpty() || types.contains(containerType)) {
                    return formatter;
                }
                // container type not part of the specified types, formatter is not valid
                return null;
            }
        } catch (CmsException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(
                    Messages.ERR_READING_FORMATTER_CONFIGURATION_1,
                    cms.getSitePath(resource)), e);
            }
        }
        // try to get formatter out of XSD as fall back
        return super.getFormatterForContainerTypeAndWidth(cms, resource, containerType, width);
    }
}
