/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/util/Attic/CmsLinkTable.java,v $
 * Date   : $Date: 2003/12/05 11:02:07 $
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
package org.opencms.util;

import java.util.HashMap;
import java.util.Iterator;

/**
 * @version $Revision: 1.1 $ $Date: 2003/12/05 11:02:07 $
 * @author 	Carsten Weinholz (c.weinholz@alkacon.com)
 */
public class CmsLinkTable {

    private static final String C_LINK_PREFIX = "link";
    
    private static final String C_LOCAL_PREFIX = "/";
    
    private HashMap m_linkTable;
    
    public class CmsLink {
        
        protected String m_name;
       
        protected String m_type;
        
        protected String m_target;
        
        protected boolean m_internal;
        
        public String getName() {
            
            return m_name;
        }
        
        public String getType() {
            
            return m_type;
        }
        
        public String getTarget() {
            
            return m_target;
        }
        
        public boolean isInternal() {
            
            return m_internal;
        }
    }
    
    public CmsLinkTable () {
        
        m_linkTable = new HashMap();
    }
    
    public String addLink (String type, String target) {
        
        CmsLink link = new CmsLink();
        link.m_name = C_LINK_PREFIX + m_linkTable.size();
        link.m_type = type;
        link.m_target = target;
        link.m_internal = isInternal(target);
        
        m_linkTable.put(link.m_name, link);
        return link.m_name;
    }

    public void addLink (String name, String type, String target, boolean internal) {

        CmsLink link = new CmsLink();
        link.m_name = name;
        link.m_type = type;
        link.m_target = target;
        link.m_internal = internal;
        
        m_linkTable.put(link.m_name, link);
    }
    
    public CmsLink getLink (String name) {
        
        return (CmsLink)m_linkTable.get(name);
    }

    public boolean isEmpty() {
        
        return m_linkTable.isEmpty();
    }
    
    public Iterator iterator() {
    
        return m_linkTable.keySet().iterator();
    }
    
    private boolean isInternal(String link) {
        
        return link.startsWith(C_LOCAL_PREFIX);
    }
}
