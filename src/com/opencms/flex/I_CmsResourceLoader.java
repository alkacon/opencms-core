/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/flex/Attic/I_CmsResourceLoader.java,v $
* Date   : $Date: 2002/08/21 11:29:32 $
* Version: $Revision: 1.2 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2002  The OpenCms Group
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

package com.opencms.flex;
/**
 * This interface describes a ResourceLoader for OpenCms.
 * A resource loader is a modern implementation of a CmsLauncher.
 * It is able to load a resource from the VFS.<p>
 *
 * The ResourceLoader operates with Request and Response in 
 * much the same way as a standard Java web application.<p>
 *
 * The ResourceLoader is much closer related to the standard 
 * Java Servlet API then the CmsLauncher, which makes it easier to 
 * understand for the novice OpenCms programmer. That way, a programmer
 * will hopefully need less time to get productive with OpenCms.<p>
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.2 $
 */
public interface I_CmsResourceLoader {
    
    /** The name of the VFS property that steers the caching */
    public static final String C_LOADER_CACHEPROPERTY = "cache";
    
    /** The name of the VFS property that steers the caching */
    public static final String C_LOADER_STREAMPROPERTY = "stream";
    
    /** Name of FlexCache runtime property */
    public static final String C_LOADER_CACHENAME = "flexcache";
      
    /** Initialize the ResourceLoader */
    public void init(com.opencms.core.A_OpenCms openCms);
    
    /** Basic processing method with CmsFile */
    public void load(com.opencms.file.CmsObject cms, com.opencms.file.CmsFile file, javax.servlet.http.HttpServletRequest req, javax.servlet.http.HttpServletResponse res) throws com.opencms.core.CmsException;
    
    /** Basic processing method with CmsFile */
    public void service(com.opencms.file.CmsObject cms, com.opencms.file.CmsResource file, com.opencms.flex.cache.CmsFlexRequest req, com.opencms.flex.cache.CmsFlexResponse res) throws com.opencms.core.CmsException;

    /** Return a String describing the ResourceLoader */
    public String getResourceLoaderInfo();
    
    /** Destroy this ResourceLoder */
    public void destroy();
}