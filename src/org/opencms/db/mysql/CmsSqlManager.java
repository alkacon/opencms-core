/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/mysql/CmsSqlManager.java,v $
 * Date   : $Date: 2003/06/13 10:03:10 $
 * Version: $Revision: 1.1 $
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

import com.opencms.core.A_OpenCms;
import com.opencms.util.Encoder;

import java.util.Properties;

/**
 * Handles SQL queries from query.properties of the MySQL driver package.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.1 $ $Date: 2003/06/13 10:03:10 $ 
 * @since 5.1
 */
public class CmsSqlManager extends org.opencms.db.generic.CmsSqlManager {
    
    private static final String C_PROPERTY_FILENAME = "org/opencms/db/mysql/query.properties";
    private static Properties c_queries = null; 
    private static Boolean c_escapeStrings = null;   
    
    /**
     * CmsSqlManager constructor.
     * 
     * @param dbPoolUrl the URL to access the correct connection pool
     */
    public CmsSqlManager(String dbPoolUrl) {
        super(dbPoolUrl);
        
        if (c_queries == null) {
            c_queries = loadProperties(C_PROPERTY_FILENAME);
        }
    }

    /**
     * Get the value for the query name
     *
     * @param queryName the name of the property
     * @return The value of the property
     */
    public String get(String queryName) {
        if (c_queries == null) {
            c_queries = loadProperties(C_PROPERTY_FILENAME);
        }
        
        String value = c_queries.getProperty(queryName);
        if (value == null || "".equals(value)) {
            value = super.get(queryName);
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
        
        return Encoder.encode(value);
    }


    /**
     * Returns <code>true</code> if Strings must be escaped before they are stored in the DB, 
     * this is required because MySQL does not support multi byte unicode strings.<p>
     * 
     * @return boolean <code>true</code> if Strings must be escaped before they are stored in the DB
     */
    public static boolean singleByteEncoding() {
        if (c_escapeStrings == null) {
            String encoding = A_OpenCms.getDefaultEncoding();
            c_escapeStrings = new Boolean("ISO-8859-1".equalsIgnoreCase(encoding) || "ISO-8859-15".equalsIgnoreCase(encoding) || "US-ASCII".equalsIgnoreCase(encoding) || "Cp1252".equalsIgnoreCase(encoding));
        }
        return c_escapeStrings.booleanValue();
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
        
        return Encoder.decode(value);
    }
        
}
