/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/CmsDbContextFactory.java,v $
 * Date   : $Date: 2004/11/22 18:03:05 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.file.CmsRequestContext;



/**
 * A default implementation of {@link I_CmsDbContextFactory}.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.1 $
 * @since 5.5.2
 */
public class CmsDbContextFactory implements I_CmsDbContextFactory {
       
    /**
     * @see org.opencms.db.I_CmsDbContextFactory#getDbContext(org.opencms.file.CmsRequestContext)
     */
    public CmsDbContext getDbContext(CmsRequestContext context) {

        return new CmsDbContext(context);
    }

    /**
     * @see org.opencms.db.I_CmsDbContextFactory#getDbContext()
     */
    public CmsDbContext getDbContext() {

        return new CmsDbContext();
    }
    
    /**
     * @see org.opencms.db.I_CmsDbContextFactory#initialize(org.opencms.db.CmsDriverManager)
     */
    public void initialize(CmsDriverManager driverManager) {

        // noop
    }
}
