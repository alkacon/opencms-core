/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/types/CmsResourceTypeUnknown.java,v $
 * Date   : $Date: 2005/10/12 14:38:21 $
 * Version: $Revision: 1.1.2.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.file.types;

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.loader.CmsDumpLoader;
import org.opencms.main.CmsLog;

import org.apache.commons.logging.Log;

/**
 * Resource type descriptor used in case the given resource type class in the XML configuration could 
 * not be instanciated.<p>
 * 
 * Using this class usually indicates that the class name given in the XML configuration is unavailable.
 * This can be the case if a module with a new resource type is imported, where the resource type class 
 * comes as part of the module and OpenCms must be restarted after the module import.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.1.2.2 $ 
 * 
 * @since 6.0.0 
 */
public class CmsResourceTypeUnknown extends A_CmsResourceType {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsResourceTypeUnknown.class);

    /**
     * Default constructor, used to initialize member variables.<p>
     */
    public CmsResourceTypeUnknown() {

        super();
    }

    /**
     * Unknown resource types are always loaded with the <code>{@link CmsDumpLoader}</code>.<p>
     * 
     * @see org.opencms.file.types.I_CmsResourceType#getLoaderId()
     */
    public int getLoaderId() {

        return CmsDumpLoader.RESOURCE_LOADER_ID;
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#initConfiguration(java.lang.String, java.lang.String, String)
     */
    public void initConfiguration(String name, String id, String className) throws CmsConfigurationException {

        // if this class is used this is usually a configuration error
        LOG.error(Messages.get().key(
            Messages.ERR_UNKNOWN_RESTYPE_CLASS_4,
            new Object[] {className, name, id, this.getClass().getName()}));

        // use super initilizer to configure the unknown resource type
        super.initConfiguration(name, id, className);
    }
}