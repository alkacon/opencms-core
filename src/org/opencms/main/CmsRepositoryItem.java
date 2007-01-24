/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/main/Attic/CmsRepositoryItem.java,v $
 * Date   : $Date: 2007/01/24 10:04:26 $
 * Version: $Revision: 1.1.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2006 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.main;

import org.opencms.repository.I_CmsRepositoryItem;

import java.io.InputStream;

/**
 *
 */
public class CmsRepositoryItem implements I_CmsRepositoryItem {

    /**
     * 
     */
    public CmsRepositoryItem() {

        // TODO Auto-generated constructor stub

    }

    /**
     * @see org.opencms.repository.I_CmsRepositoryItem#getContent()
     */
    public byte[] getContent() {

        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see org.opencms.repository.I_CmsRepositoryItem#getContentLength()
     */
    public long getContentLength() {

        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * @see org.opencms.repository.I_CmsRepositoryItem#getCreationDate()
     */
    public long getCreationDate() {

        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * @see org.opencms.repository.I_CmsRepositoryItem#getLastModifiedDate()
     */
    public long getLastModifiedDate() {

        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * @see org.opencms.repository.I_CmsRepositoryItem#getMimeType()
     */
    public String getMimeType() {

        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see org.opencms.repository.I_CmsRepositoryItem#getName()
     */
    public String getName() {

        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see org.opencms.repository.I_CmsRepositoryItem#getStreamContent()
     */
    public InputStream getStreamContent() {

        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see org.opencms.repository.I_CmsRepositoryItem#isCollection()
     */
    public boolean isCollection() {

        // TODO Auto-generated method stub
        return false;
    }

}
