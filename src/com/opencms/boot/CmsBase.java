/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/boot/Attic/CmsBase.java,v $
* Date   : $Date: 2003/09/16 19:12:39 $
* Version: $Revision: 1.14 $
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


package com.opencms.boot;

import org.opencms.main.OpenCms;

import java.io.File;

/**
 * OpenCms Base class for static access to system wide properties
 * and helper functions, e.g. OpenCms logging oder OpenCms base path.
 *
 * @author Alexander Lucas
 * @version $Revision: 1.14 $ $Date: 2003/09/16 19:12:39 $
 */
public final class CmsBase extends Object {
    
    /**
     * Default constructor. Nobody is allowed to create an instance of this class!
     */
    private CmsBase() {
        super();
    }    

    /**
     * Get the OpenCms web-base path.<p> 
     * 
     * @return the current web base path
     */
    public static String getWebBasePath() {
        File basePath = new File(OpenCms.getBasePath());
        String webBasePath = basePath.getParent();
        if (!webBasePath.endsWith(File.separatorChar+"")) {
            webBasePath += File.separatorChar;
        }
        return webBasePath;
    }

    /** 
     * Get the OpenCms WebApplicationName.<p> 
     * 
     * @return the web application name
     */
    public static String getWebAppName() {
        File basePath = new File(OpenCms.getBasePath());
        String webAppName = basePath.getParentFile().getName();
        return webAppName;
    }

    /**
     * Get the absolute web path for a given path.<p>
     * 
     * @param s the path
     * @return the absolute path
     */
    public static String getAbsoluteWebPath(String s) {
        if (s == null) {
            return null;
        }

        File f = new File(s);
        if (! f.isAbsolute()) {
            if (OpenCms.getBasePath() == null) {
                return null;
            } else {
                return getWebBasePath() + s;
            }
        } else {
            return s;
        }
    }

    private static final char m_replaceSep = (File.separatorChar == '/')?'\\':'/';

    /**
     * Gets the absolute path for a given path.<p>
     * 
     * @param s the path
     * @return the absolute path
     */
    public static String getAbsolutePath(String s) {
        if (s == null) {
            return null;
        }         

        File f = new File(s);
        if (! f.isAbsolute()) {
            if (OpenCms.getBasePath() == null) {
                return null;
            } else {
                return (OpenCms.getBasePath() + s).replace(m_replaceSep, File.separatorChar);
            }
        } else {
            return s.replace(m_replaceSep, File.separatorChar);
        }
    }

    /**
     * Gets the path to the properties file.<p>
     * 
     * @param absolute flag to indicate if absolute path is wanted
     * @return the relative or absolute path to opencms.properties
     */
    public static String getPropertiesPath(boolean absolute) {
        String result = "config/opencms.properties".replace('/', File.separatorChar);
        if (absolute) {
            if (OpenCms.getBasePath() == null) {
                result = null;
            } else {
                result = OpenCms.getBasePath() + result;
            }
        }
        return result;
    }
}
