/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/mail/CmsVfsDataSource.java,v $
 * Date   : $Date: 2005/10/21 09:13:56 $
 * Version: $Revision: 1.1.2.1 $
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

package org.opencms.mail;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

/**
 * DataSource wrapper for VFS resources, allows easy sending of VFS resources as email attachments.<p>
 * 
 * @author Alexander Kandzior
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 6.2.0 
 */
public class CmsVfsDataSource implements DataSource {

    /** The content type to use for the data source. */
    private String m_contentType;

    /** The file that accessed by this data source. */
    private CmsFile m_file;

    /**
     * Creates a new data source for the given VFS resource.<p>
     * 
     * @param cms the current users OpenCms context
     * @param resource the resource to use
     * @param contentType the content type to use
     * 
     * @throws CmsException in case of errors accessing the resource in the VFS
     */
    public CmsVfsDataSource(CmsObject cms, CmsResource resource, String contentType)
    throws CmsException {

        m_file = CmsFile.upgrade(resource, cms);
        m_contentType = contentType;
    }

    /**
     * @see javax.activation.DataSource#getContentType()
     */
    public String getContentType() {

        return m_contentType;
    }

    /**
     * Returns an input stream baded on the file contents.<p>
     * 
     * @see javax.activation.DataSource#getInputStream()
     */
    public InputStream getInputStream() {

        return new ByteArrayInputStream(m_file.getContents());
    }

    /**
     * Returns the root path of the given resource.<p>
     * 
     * @see javax.activation.DataSource#getName()
     */
    public String getName() {

        return m_file.getRootPath();
    }

    /**
     * Don't use this method, VFS resources can't be written using this datasource class.<p>
     * 
     * This method will just return a new <code>{@link ByteArrayOutputStream}</code>.<p>
     * 
     * @see javax.activation.DataSource#getOutputStream()
     */
    public OutputStream getOutputStream() {

        // maybe throw an Exception here to avoid errors
        return new ByteArrayOutputStream();
    }
}