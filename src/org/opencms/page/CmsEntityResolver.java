/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/page/Attic/CmsEntityResolver.java,v $
 * Date   : $Date: 2004/02/13 13:41:45 $
 * Version: $Revision: 1.6 $
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

import org.opencms.file.CmsObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * Resolves XML entities (e.g. external DTDs) in the OpenCms VFS.<p>
 * 
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @version $Revision: 1.6 $ 
 */
public class CmsEntityResolver implements EntityResolver {

    /** The cms object */
    private CmsObject m_cms = null;
        
    /**
     * Creates a new entity resolver to read the dtd.
     * 
     * @param cms the cms object
     */
    public CmsEntityResolver(CmsObject cms) {
        m_cms = cms;
    }
    
    /**
     * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String, java.lang.String)
     */
    public InputSource resolveEntity(String publicId, String systemId) {        
        if (publicId == null) {            
            try {
                URI dtdURI = new URI(systemId);                
                // TODO: assumes that the system id is an opencms internal path 
                InputStream in = new ByteArrayInputStream(m_cms.readFile(dtdURI.getPath()).getContents());
                return new InputSource(in);
            } catch (Exception exc) {
                // noop
            }
        }        
        return null;
    }
}
