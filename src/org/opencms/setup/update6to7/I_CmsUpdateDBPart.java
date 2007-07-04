/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/setup/update6to7/Attic/I_CmsUpdateDBPart.java,v $
 * Date   : $Date: 2007/07/04 16:57:44 $
 * Version: $Revision: 1.2 $
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

package org.opencms.setup.update6to7;

import java.util.Map;

/**
 * Represent a part of the database update process.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.2 $ 
 * 
 * @since 6.9.2 
 */
public interface I_CmsUpdateDBPart {

    /**
     * Executes the update part.<p>
     */
    void execute();

    /**
     * Returns the right instance based on the database name.<p>
     * 
     * @param dbName the database name
     * @param dbPoolData the database pool data
     * 
     * @return the right instance
     */
    I_CmsUpdateDBPart getDbInstance(String dbName, Map dbPoolData);

    /**
     * Sets the database pool Data.<p>
     *
     * @param poolData the database pool Data to set
     */
    void setPoolData(Map poolData);
}
