/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/staging/Attic/CmsUri.java,v $
* Date   : $Date: 2001/04/26 16:14:52 $
* Version: $Revision: 1.2 $
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
package com.opencms.staging;

import java.util.*;
import java.io.*;
import com.opencms.file.*;

/**
 * An instance of CmsUri represents an requestable ressource in the OpenCms
 * staging-area. It points to the starting element and handles the access-
 * checks to this ressource in a simple way.
 *
 * If access is granted for the current user it starts the startingElement to
 * process the content of this ressource.
 *
 * @author: Andreas Schouten
 */
public class CmsUri {

    /**
     * the id of the Group that can read the resource
     * -1 if any Group can read this.
     */
    private int m_readAccessGroup;

    /**
     * The launcher type for this resource.
     */
    private int m_launcherType;

    /**
     * The Name of the templateclass that should used as root-Template.
     */
    private String m_launcherClassname;

    /**
     * The Key to the Element used to start the contentgeneration for
     * this Uri.
     */
    private CmsElementDescriptor m_startingElement;

    /**
     * Constructor.
     *
     * @param startingElement The Element to start the contentgenerating for this uri.
     * @param readAccessGroup the Group that can read the uri.
     * @param launcherType The launcher type of this resource.
     * @param launcherClassname The Name of the templateclass that should used as root-Template.
     */
    public CmsUri(CmsElementDescriptor startingElement, int readAccessGroup,
                    int launcherType, String launcherClassname){
        m_startingElement = startingElement;
        m_readAccessGroup = readAccessGroup;
        m_launcherType = launcherType;
        m_launcherClassname = launcherClassname;
    }
}