/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/I_CmsDriver.java,v $
 * Date   : $Date: 2004/02/13 13:41:44 $
 * Version: $Revision: 1.5 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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
 
package org.opencms.db;

import org.opencms.main.CmsException;

import java.util.List;

import org.apache.commons.collections.ExtendedProperties;

/**
 * @version $Revision: 1.5 $ $Date: 2004/02/13 13:41:44 $
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 */
public interface I_CmsDriver {

    /**
     * Initializes the driver.<p>
     * 
     * @param configuration hte configuration to read
     * @param successiveDrivers a list of successive drivers to initialize
     * @param driverManager backlink to the driver manager
     * @throws CmsException if something goes wrong
     */
    void init(ExtendedProperties configuration, List successiveDrivers, CmsDriverManager driverManager) throws CmsException;
    
    /**
     * Returns information about the driver.<p>
     * 
     * @return an information string
     */
    String toString();
}