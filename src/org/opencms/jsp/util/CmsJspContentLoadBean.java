/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/util/CmsJspContentLoadBean.java,v $
 * Date   : $Date: 2007/08/13 16:30:11 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2007 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.jsp.util;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Allows JSP access to the results of a &lt;cms:contentload ... &gt; operation using the JSTL and EL.<p>
 * 
 * @author Alexander Kandzior
 * 
 * @version $Revision: 1.2 $ 
 * 
 * @since 7.0.2 
 * 
 * @see org.opencms.jsp.CmsJspTagContentLoad
 */
public class CmsJspContentLoadBean {

    /** The OpenCms context of the current user. */
    protected CmsObject m_cms;

    /** The List of results form the content loader. */
    protected List m_content;

    /** The selected locale for accessing entries from the XML content. */
    protected Locale m_locale;

    /**
     * Creates a new context bean using the OpenCms context of the current user.<p>
     * 
     * @param cms the OpenCms context of the current user
     * @param locale the Locale to use when accessing the content
     * @param content the content to access, must contain Object of type {@link CmsResource}
     */
    public CmsJspContentLoadBean(CmsObject cms, Locale locale, List content) {

        m_cms = cms;
        m_locale = locale;

        m_content = new ArrayList(content.size());
        int size = content.size();
        for (int i = 0; i < size; i++) {
            CmsResource res = (CmsResource)content.get(i);
            CmsJspContentAccessBean bean = new CmsJspContentAccessBean(m_cms, m_locale, res);
            m_content.add(bean);
        }
    }

    /**
     * Returns a List of {@link CmsJspContentAccessBean} instances, which have been wrapped around
     * the original {@link CmsResource} instances of the collector result.<p>
     * 
     * @return a List of {@link CmsJspContentAccessBean} instances, which have been wrapped around
     * the original {@link CmsResource} instances of the collector result.<p>
     */
    public List getContent() {

        return m_content;
    }
}