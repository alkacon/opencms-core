/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/oracle/CmsProjectDriver.java,v $
 * Date   : $Date: 2004/08/12 11:01:30 $
 * Version: $Revision: 1.30 $
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

package org.opencms.db.oracle;

import org.opencms.db.CmsDriverManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;

import org.apache.commons.collections.ExtendedProperties;

/** 
 * Oracle/OCI implementation of the project driver methods.<p>
 *
 * @version $Revision: 1.30 $ $Date: 2004/08/12 11:01:30 $
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @since 5.1
 */
public class CmsProjectDriver extends org.opencms.db.generic.CmsProjectDriver {    

    /**
     * @see org.opencms.db.I_CmsDriver#init(org.apache.commons.collections.ExtendedProperties, java.util.List, org.opencms.db.CmsDriverManager)
     */
    public void init(ExtendedProperties configuration, List successiveDrivers, CmsDriverManager driverManager) {

        super.init(configuration, successiveDrivers, driverManager);
    }
    
    /**
     * @see org.opencms.db.I_CmsProjectDriver#initQueries()
     */
    public org.opencms.db.generic.CmsSqlManager initQueries() {
        return new org.opencms.db.oracle.CmsSqlManager();
    }

    /**
     * Serialize object data to write it as byte array in the database.<p>
     * 
     * @param object the object
     * @return byte[] the byte array with object data
     * @throws IOException if something goes wrong
     */
    protected final byte[] internalSerializeObject (Serializable object) throws IOException {
        // this method is final to allow the java compiler to inline this code!

        // serialize the object
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(bout);
        oout.writeObject(object);
        oout.close();

        return bout.toByteArray();
    }
}