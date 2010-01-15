/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/types/Attic/CmsResourceTypeJspRenderer.java,v $
 * Date   : $Date: 2010/01/15 14:55:48 $
 * Version: $Revision: 1.1 $
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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.CmsDefaultXmlContentHandler;

import java.util.List;

import org.apache.commons.logging.Log;

/**
 * Resource type descriptor for the type "jsprenderer".<p>
 *
 * @author Tobias Herrmann 
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 7.9.0 
 */
public class CmsResourceTypeJspRenderer extends CmsResourceTypeXmlContent {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsResourceTypeJspRenderer.class);

    /** Property name for formatter property. */
    protected static final String FORMATTER_PROPERTY = "ade.formatter";

    /** Property name for container types property. */
    protected static final String CONTAINERTYPES_PROPERTY = "ade.containertypes";

    /**
     * @see org.opencms.file.types.I_CmsResourceType#getFormatterForContainerType(CmsObject, CmsResource, String)
     */
    @Override
    public String getFormatterForContainerType(CmsObject cms, CmsResource resource, String containerType) {

        if (containerType.equals(CmsDefaultXmlContentHandler.DEFAULT_FORMATTER_TYPE)) {
            return CmsDefaultXmlContentHandler.DEFAULT_FORMATTER;
        }
        try {
            CmsProperty formatterProp = cms.readPropertyObject(resource, FORMATTER_PROPERTY, true);
            String formatter = formatterProp.getValue();
            if (!CmsStringUtil.isEmptyOrWhitespaceOnly(formatter)) {
                CmsProperty typesProp = cms.readPropertyObject(resource, CONTAINERTYPES_PROPERTY, true);
                List<String> types = typesProp.getValueList();
                if (types.isEmpty() || types.contains(containerType)) {
                    return formatter;
                }
            }
        } catch (CmsException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(
                    Messages.ERR_READING_FORMATTER_CONFIGURATION_1,
                    cms.getSitePath(resource)), e);
            }
        }
        return null;
    }
}
