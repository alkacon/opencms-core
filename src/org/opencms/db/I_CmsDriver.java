/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/I_CmsDriver.java,v $
 * Date   : $Date: 2005/06/23 10:47:10 $
 * Version: $Revision: 1.12 $
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

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.main.CmsException;

import java.util.List;

/**
 * 
 * @author Carsten Weinholz 
 * 
 * @version $Revision: 1.12 $
 * 
 * @since 6.0.0 
 * 
 * @param Leave a comment here. Why doesn't checkstyle complain about empty interface comments?
 */
public interface I_CmsDriver {

    /**
     * Initializes the driver.<p>
     * 
     * @param dbc the current database context
     * @param configurationManager the configuration manager
     * @param successiveDrivers a list of successive drivers to be initialized
     * @param driverManager the initialized OpenCms driver manager
     * 
     * @throws CmsException if something goes wrong
     */
    void init(
        CmsDbContext dbc,
        CmsConfigurationManager configurationManager,
        List successiveDrivers,
        CmsDriverManager driverManager) throws CmsException;

    /**
     * Returns information about the driver.<p>
     * 
     * @return an information string
     */
    String toString();

}