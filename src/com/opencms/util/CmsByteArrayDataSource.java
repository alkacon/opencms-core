/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/util/Attic/CmsByteArrayDataSource.java,v $
* Date   : $Date: 2004/01/06 09:46:26 $
* Version: $Revision: 1.7 $
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

package com.opencms.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import javax.activation.DataSource;

/** 
 * This class implements a DataSource from an InputStream, a byte array or a 
 * String and is most often used to create a mail message with the CmsMail object.<p>
 * 
 * See also
 * <a href="http://java.sun.com/products/javamail/index.html">http://java.sun.com/products/javamail/index.html</a>.<p>
 * 
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.7 $ $Date: 2004/01/06 09:46:26 $
 * @see com.opencms.defaults.CmsMail
 */
public class CmsByteArrayDataSource implements DataSource {
    private byte[] m_data; // data
    private String m_type; // content-type
    

    /**
     * Constructor to create a DataSource from a byte array.<p>
     * 
     * @param data the data
     * @param type the type of the data
     */
    public CmsByteArrayDataSource(byte[] data, String type) {
        m_data = data;
        m_type = type;
    }
    
    /**
     * Constructor to create a DataSource from an input stream.<p>
     * 
     * @param is the data as input stream
     * @param type the type of the data
     */
    public CmsByteArrayDataSource(InputStream is, String type) {
        m_type = type;
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            int ch;
            while ((ch = is.read()) != -1) {
                
                // XXX - must be made more efficient by                
                // doing buffered reads, rather than one byte reads
                os.write(ch);
            }
            m_data = os.toByteArray();
        } catch (IOException ioex) {
            // ignore
        }
    }

    /**
     * Constructor to create a DataSource from a String.<p>
     * 
     * @param data the data as string
     * @param type the type of the data
     * @param encoding the encoding
     */
    public CmsByteArrayDataSource(String data, String type, String encoding) {
        try {
            
            // Assumption that the string contains only ASCII            
            // characters!  Otherwise just pass a charset into this            
            // constructor and use it in getBytes()
            m_data = data.getBytes(encoding);
        } catch (UnsupportedEncodingException uex) {
            m_data = data.getBytes();
        }
        m_type = type;
    }
  
    /**
     * @see javax.activation.DataSource#getContentType()
     */
    public String getContentType() {
        return m_type;
    }

    /**
     * @see javax.activation.DataSource#getInputStream()
     */
    public InputStream getInputStream() throws IOException {
        if (m_data == null) {
            throw new IOException("no data");
        }
        return new ByteArrayInputStream(m_data);
    }
    
    /**
     * @see javax.activation.DataSource#getName()
     */
    public String getName() {
        return "dummy";
    }
    
    /**
     * @see javax.activation.DataSource#getOutputStream()
     */
    public OutputStream getOutputStream() throws IOException {
        throw new IOException("cannot do this");
    }
}