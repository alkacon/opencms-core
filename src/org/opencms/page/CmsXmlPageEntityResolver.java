/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/page/Attic/CmsXmlPageEntityResolver.java,v $
 * Date   : $Date: 2004/04/30 10:09:34 $
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

package org.opencms.page;

import org.opencms.main.OpenCms;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * Resolves XML entities (e.g. external DTDs) in the OpenCms VFS.<p>
 * 
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @version $Revision: 1.1 $ 
 */
public class CmsXmlPageEntityResolver implements EntityResolver {

    /** The location of the xmlpage DTD */
    private static final String C_XMLPAGE_DTD_LOCATION = "org/opencms/page/xmlpage.dtd";

    /** The (old) DTD address of the OpenCms xmlpage (used until 5.3.5) */
    protected static final String C_XMLPAGE_DTD_OLD_SYSTEM_ID = "/system/shared/page.dtd";

    /**
     * Creates a new entity resolver to read the xmlpage dtd.
     */
    public CmsXmlPageEntityResolver() {
        // noop
    }

    /**
     * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String, java.lang.String)
     */
    public InputSource resolveEntity(String publicId, String systemId) {

        if (systemId.equals(CmsXmlPage.C_XMLPAGE_DTD_SYSTEM_ID) || systemId.endsWith(C_XMLPAGE_DTD_OLD_SYSTEM_ID)) {
            try {
                return new InputSource(getClass().getClassLoader().getResourceAsStream(C_XMLPAGE_DTD_LOCATION));
            } catch (Throwable t) {
                OpenCms.getLog(this).error("Did not find xmlpage DTD at " + C_XMLPAGE_DTD_LOCATION);
            }
        }
        // use the default behaviour (i.e. resolve through external URL)
        return null;
    }
    
    //    /**
    //     * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String, java.lang.String)
    //     */
    //    public InputSource resolveEntity(String publicId, String systemId) {        
    //        if (publicId == null) {            
    //            try {
    //                URI dtdURI = new URI(systemId);                
    //                InputStream in = new ByteArrayInputStream(m_cms.readFile(dtdURI.getPath()).getContents());
    //                return new InputSource(in);
    //            } catch (Exception exc) {
    //                // noop
    //            }
    //        }        
    //        return null;
    //    }    
}