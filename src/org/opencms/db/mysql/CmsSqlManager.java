/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/mysql/CmsSqlManager.java,v $
 * Date   : $Date: 2004/03/29 10:39:53 $
 * Version: $Revision: 1.13 $
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
 
package org.opencms.db.mysql;

import org.opencms.i18n.CmsEncoder;
import org.opencms.main.OpenCms;


import java.util.Properties;

/**
 * Handles SQL queries from query.properties of the MySQL driver package.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.13 $ $Date: 2004/03/29 10:39:53 $ 
 * @since 5.1
 */
public class CmsSqlManager extends org.opencms.db.generic.CmsSqlManager {
    
    private static final String C_PROPERTY_FILENAME = "org/opencms/db/mysql/query.properties";
    private static Properties c_queries = null; 
    private static Boolean c_singleByteEncoding = null;   
    
    /**
     * Initializes the SQL manager.<p>
     * 
     * @see org.opencms.db.generic.CmsSqlManager#CmsSqlManager()
     */  
    public CmsSqlManager() {
        super();
        
        if (c_queries == null) {
            c_queries = loadProperties(C_PROPERTY_FILENAME);
            precalculateQueries(c_queries);
        }
    }
    
    /**
     * @see java.lang.Object#finalize()
     */
    protected void finalize() throws Throwable {
        try {
            if (c_queries != null) {
                c_queries.clear();
            }
            c_queries = null;
        } catch (Throwable t) {
            // ignore
        }
        super.finalize();
    }    

    /**
     * @see org.opencms.db.generic.CmsSqlManager#readQuery(java.lang.String)
     */
    public String readQuery(String queryName) {
        if (c_queries == null) {
            c_queries = loadProperties(C_PROPERTY_FILENAME);
            precalculateQueries(c_queries);
        }
        
        String value = c_queries.getProperty(queryName);
        if (value == null || "".equals(value)) {
            value = super.readQuery(queryName);
        }
        
        return value;
    }
    
    /**
     * Escapes a String to prevent issues with UTF-8 encoding, same style as
     * http uses for form data since MySQL doesn't support Unicode/UTF-8 strings.<p>
     * 
     * @param value String to be escaped
     * @return the escaped String
     */
    public static String escape(String value) { 
        if (singleByteEncoding()) {
            return value;
        }
        
        return CmsEncoder.encode(value);
    }


    /**
     * Returns <code>false</code> if Strings must be escaped before they are stored in the DB, 
     * this is required because MySQL does not support multi byte unicode strings.<p>
     * 
     * @return boolean <code>true</code> if Strings must be escaped before they are stored in the DB
     */
    public static boolean singleByteEncoding() {
        if (c_singleByteEncoding == null) {
            String encoding = OpenCms.getSystemInfo().getDefaultEncoding();
            c_singleByteEncoding = new Boolean("ISO-8859-1".equalsIgnoreCase(encoding) 
                || "ISO-8859-15".equalsIgnoreCase(encoding) 
                || "US-ASCII".equalsIgnoreCase(encoding) 
                || "Cp1252".equalsIgnoreCase(encoding));
//          FIXME: Encoding for mySQL - use unicoded support in mySQL 4 and remove this
            c_singleByteEncoding = new Boolean(false);
        }
        return c_singleByteEncoding.booleanValue();
    }

    /**
     * Unescapes a String to prevent issues with UTF-8 encoding, same style as
     * http uses for form data since MySQL doesn't support Unicode/UTF-8 strings.<p>
     * 
     * @param value String to be unescaped
     * @return the unescaped String
     */
    public static String unescape(String value) {
        if (singleByteEncoding()) {
            return value;
        }
        
        return CmsEncoder.decode(value);
    }
        
}
