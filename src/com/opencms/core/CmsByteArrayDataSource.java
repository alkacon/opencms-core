
/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/core/Attic/CmsByteArrayDataSource.java,v $
* Date   : $Date: 2001/01/24 09:41:40 $
* Version: $Revision: 1.3 $
*
* Copyright (C) 2000  The OpenCms Group 
* 
* This File is part of OpenCms -
* the Open Source Content Mananagement System
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version.
* 
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* 
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.com
* 
* You should have received a copy of the GNU General Public License
* long with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/

package com.opencms.core;

import java.io.*;
import javax.activation.*;

/** 
 * This class implements a DataSource from:	an InputStream, a byte array, a String
 * This class is used to send a html text or a Data source.
 * @author $Author: m.meurer $
 * @version $Revision: 1.3 $ $Date: 2001/01/24 09:41:40 $
 * @see http://java.sun.com/products/javamail/index.html
 *
 */
public class CmsByteArrayDataSource implements DataSource {
    private byte[] data; // data
    private String type; // content-type
    
    /* Create a DataSource from a byte array */
    public CmsByteArrayDataSource(byte[] data, String type) {
        this.data = data;
        this.type = type;
    }
    
    /* Create a DataSource from an input stream */
    public CmsByteArrayDataSource(InputStream is, String type) {
        this.type = type;
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            int ch;
            while((ch = is.read()) != -1) {
                
                // XXX - must be made more efficient by                
                // doing buffered reads, rather than one byte reads
                os.write(ch);
            }
            data = os.toByteArray();
        }
        catch(IOException ioex) {
            
        }
    }
    
    /* Create a DataSource from a String */
    public CmsByteArrayDataSource(String data, String type) {
        try {
            
            // Assumption that the string contains only ASCII            
            // characters!  Otherwise just pass a charset into this            
            // constructor and use it in getBytes()
            this.data = data.getBytes("iso-8859-1");
        }
        catch(UnsupportedEncodingException uex) {
            
        }
        this.type = type;
    }
    public String getContentType() {
        return type;
    }
    
    /**
     * Return an InputStream for the data.
     * Note - a new stream must be returned each time.
     */
    public InputStream getInputStream() throws IOException {
        if(data == null) {
            throw new IOException("no data");
        }
        return new ByteArrayInputStream(data);
    }
    public String getName() {
        return "dummy";
    }
    public OutputStream getOutputStream() throws IOException {
        throw new IOException("cannot do this");
    }
}
