/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/util/CmsPropertyUtils.java,v $
 * Date   : $Date: 2004/07/18 16:34:26 $
 * Version: $Revision: 1.3 $
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

package org.opencms.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;

import org.apache.commons.collections.ExtendedProperties;

/**
 * Utilities to access property files.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.3 $
 * @since 5.3
 */
public final class CmsPropertyUtils {
    
    /**
     * Hides the public constructor.<p>
     */
    private CmsPropertyUtils() {
        // noop
    }
    
    /**
     * Loads an extended property file and performs escaping/unescaping of "," and "=" entries.<p> 
     * 
     * @param file the file tp read the properties from
     * @return the initialized extended properties
     * @throws IOException in case of IO errors 
     */
    public static ExtendedProperties loadProperties(String file) throws IOException {

        FileInputStream input = null;
        ExtendedProperties properties = null;

        try {
            input = new FileInputStream(new File(file));
            properties = new ExtendedProperties();
            properties.load(input);
            input.close();
        } catch (IOException e) {
            try {
                input.close();
            } catch (Exception ex) {
                // nothing we can do
            }
            throw e;
        }

        for (Iterator i = properties.keySet().iterator(); i.hasNext();) {
            String key = (String)i.next();
            Object obj = properties.get(key);
            String value[] = {};

            if (obj instanceof Vector) {
                value = (String[]) ((Vector)obj).toArray(value);
            } else {
                String v[] = {(String)obj };
                value = v;
            }

            for (int j = 0; j < value.length; j++) {
                value[j] = CmsStringUtil.substitute(value[j], "\\,", ",");
                value[j] = CmsStringUtil.substitute(value[j], "\\=", "=");
            }

            if (value.length > 1) {
                properties.put(key, new Vector(Arrays.asList(value)));
            } else {
                properties.put(key, value[0]);
            }
        }

        return properties;
    }
}
