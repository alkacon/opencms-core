/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/configuration/CmsDigesterResourceTypeCreationFactory.java,v $
 * Date   : $Date: 2005/10/12 14:38:21 $
 * Version: $Revision: 1.1.2.2 $
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

package org.opencms.configuration;

import org.opencms.file.types.CmsResourceTypeUnknown;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.CmsLog;

import org.apache.commons.digester.AbstractObjectCreationFactory;
import org.apache.commons.digester.ObjectCreationFactory;
import org.apache.commons.logging.Log;

import org.xml.sax.Attributes;

/**
 * Factory to create resource type instances from the XML configuration.<p>
 * 
 * This is required because the default digester implementation will cause an exception in case 
 * a resource type class is missing. However, a missing class is common if a module with a new resource type
 * class is imported. In this case, the resource type class is changes to <code>{@link org.opencms.file.types.CmsResourceTypeUnknown}</code>,
 * so that the import of the resources can proceed.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.1.2.2 $
 * 
 * @since 6.0.2
 */
public class CmsDigesterResourceTypeCreationFactory extends AbstractObjectCreationFactory
implements ObjectCreationFactory {

    /** The log object of this class. */
    private static final Log LOG = CmsLog.getLog(CmsDigesterResourceTypeCreationFactory.class);
    
    /**
     * Default constructor for the resource type configuration factory.<p> 
     */
    public CmsDigesterResourceTypeCreationFactory() {

        super();
    }

    /**
     * @see org.apache.commons.digester.ObjectCreationFactory#createObject(org.xml.sax.Attributes)
     */
    public Object createObject(Attributes attributes) throws Exception {

        // get the class name attribute
        String className = attributes.getValue(I_CmsXmlConfiguration.A_CLASS);
        // create the class instance
        I_CmsResourceType type;
        try {
            if (className != null) {
                className = className.trim();
            }
            type = (I_CmsResourceType)Class.forName(className).newInstance();
        } catch (Exception e) {
            // resource type is unknown, use dummy class to import the module resources
            type = new CmsResourceTypeUnknown();
            // write an error to the log
            LOG.error(Messages.get().key(
                Messages.ERR_UNKNOWN_RESTYPE_CLASS_2,
                className,
                type.getClass().getName()), e);
        }
        return type;
    }
}