/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/oraclesql/Attic/CmsResourceBroker.java,v $
* Date   : $Date: 2002/09/16 12:52:41 $
* Version: $Revision: 1.3 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.opencms.file.oraclesql;

import source.org.apache.java.util.*;
import com.opencms.core.*;


/**
 * This is THE resource broker. It merges all resource broker
 * into one public class. The interface is local to package. <B>All</B> methods
 * get additional parameters (callingUser and currentproject) to check the security-
 * police.
 *
 * @author Andreas Schouten
 * @author Michaela Schleich
 * @author Michael Emmerich
 * @author Anders Fugmann
 * @version $Revision: 1.3 $ $Date: 2002/09/16 12:52:41 $
 */
public class CmsResourceBroker extends com.opencms.file.genericSql.CmsResourceBroker {

    /**
     * return the correct DbAccess class.
     * This method should be overloaded by all other Database Drivers
     * Creation date: (09/15/00 %r)
     * @return com.opencms.file.genericSql.CmsDbAccess
     * @param configurations source.org.apache.java.util.Configurations
     * @exception com.opencms.core.CmsException Thrown if CmsDbAccess class could not be instantiated.
     */
    public com.opencms.file.genericSql.CmsDbAccess createDbAccess(Configurations configurations) throws CmsException{
        return new com.opencms.file.oraclesql.CmsDbAccess(configurations);
    }

    /**
     * Initializes the resource broker and sets up all required modules and connections.
     * @param config The OpenCms configuration.
     * @exception CmsException Throws CmsException if something goes wrong.
     */
    public void init(Configurations config) throws CmsException {
        super.init(config);
    }

}
