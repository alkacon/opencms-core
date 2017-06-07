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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.xml.content;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsMacroResolver;
import org.opencms.xml.types.I_CmsXmlContentValue;

import org.apache.commons.logging.Log;

/**
 * Visitor implementation that provides macro resolving for all visited values.<p>
 *
 * This class is used when a new XML content is generated using a default content as model file.<p>
 *
 * @since 6.5.5
 */
public class CmsXmlContentMacroVisitor implements I_CmsXmlContentValueVisitor {

    /** Static reference to the log. */
    private static final Log LOG = CmsLog.getLog(CmsXmlContentMacroVisitor.class);

    /** The initialized OpenCms user context (required for VFS access). */
    CmsObject m_cms;

    /** The macro resolver to use for resolving macros. */
    CmsMacroResolver m_resolver;

    /**
     * Creates a new validation node visitor.<p>
     *
     * @param cms the initialized OpenCms user context (required for VFS access)
     * @param resolver the macro resolver to use for resolving macros
     */
    public CmsXmlContentMacroVisitor(CmsObject cms, CmsMacroResolver resolver) {

        // store reference to the provided CmsObject
        m_cms = cms;
        m_resolver = resolver;
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentValueVisitor#visit(org.opencms.xml.types.I_CmsXmlContentValue)
     */
    public void visit(I_CmsXmlContentValue value) {

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_XMLCONTENT_VISIT_1, value.getPath()));
        }

        // get original value (for simple types only)
        if (value.isSimpleType()) {
            String original = value.getStringValue(m_cms);
            // resolve the value
            String resolved = m_resolver.resolveMacros(original);
            if (!resolved.equals(original)) {
                // something has been changed, set new value
                value.setStringValue(m_cms, resolved);
            }
        }
    }
}