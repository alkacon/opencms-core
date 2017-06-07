/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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

package org.opencms.ade.contenteditor;

import org.opencms.file.CmsResource;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.I_CmsXmlContentEditorChangeHandler;

/**
 * Abstract implementation of the content editor change handler.<p>
 */
public abstract class A_CmsXmlContentEditorChangeHandler implements I_CmsXmlContentEditorChangeHandler {

    /** The configuration string. */
    protected String m_configuration;

    /** The content field to watch for changes. */
    protected String m_scope;

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentEditorChangeHandler#getConfiguration()
     */
    public String getConfiguration() {

        return m_configuration;
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentEditorChangeHandler#getScope()
     */
    public String getScope() {

        return m_scope;
    }

    /**
     * Resolves a relative content value path to an absolute one.<p>
     *
     * @param source the source path
     * @param target the target path
     *
     * @return the resolved path
     */
    public String resolveRelativePath(String source, String target) {

        String result = null;
        if (target.startsWith(".")) {
            if (target.startsWith("./")) {
                target = target.substring(2);
            }
            while (target.startsWith("../")) {
                source = CmsResource.getParentFolder(source);
                target = target.substring(3);
            }
            result = CmsStringUtil.joinPaths(source, target);
        } else {
            result = target;
        }
        return result;
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentEditorChangeHandler#setConfiguration(java.lang.String)
     */
    public void setConfiguration(String configuration) {

        m_configuration = configuration;
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentEditorChangeHandler#setScope(java.lang.String)
     */
    public void setScope(String scope) {

        m_scope = scope;
    }

}
