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
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.xml.types.I_CmsXmlContentValue;

import org.apache.commons.logging.Log;

/**
 * Visitor implementation that resolves the content mappings for all the visited values.<p>
 *
 * This class is used when {@link org.opencms.xml.content.CmsXmlContent#validate(CmsObject)}
 * is called to resolve the mappings of a XML content object.<p>
 *
 * @since 6.0.0
 */
class CmsXmlContentMappingVisitor implements I_CmsXmlContentValueVisitor {

    /** Static reference to the log. */
    private static final Log LOG = CmsLog.getLog(CmsXmlContentMappingVisitor.class);

    /** The initialized OpenCms user context (required for VFS access). */
    CmsObject m_cms;

    /** The XML content the mappings are resolved for. */
    CmsXmlContent m_content;

    /** The "main" content definition, used for all mappings. */
    I_CmsXmlContentHandler m_handler;

    /**
     * Creates a new error handler node visitor.<p>
     *
     * @param cms the initialized OpenCms user context (required for VFS access)
     * @param content the XML content to resolve the mappings for
     */
    public CmsXmlContentMappingVisitor(CmsObject cms, CmsXmlContent content) {

        // store references
        m_cms = cms;
        m_content = content;
        m_handler = content.getHandler();
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentValueVisitor#visit(org.opencms.xml.types.I_CmsXmlContentValue)
     */
    public void visit(I_CmsXmlContentValue value) {

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_XMLCONTENT_VISIT_1, value.getPath()));
        }
        try {
            m_handler.resolveMapping(m_cms, m_content, value);
        } catch (CmsException e) {
            LOG.error(Messages.get().getBundle().key(Messages.LOG_XMLCONTENT_RESOLVE_MAPPING_1, value.getPath()), e);
        }
    }
}