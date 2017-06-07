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
import org.opencms.xml.types.I_CmsXmlContentValue;

import org.apache.commons.logging.Log;

/**
 * Visitor implementation that provides validation for all visited values.<p>
 *
 * This class is used when {@link org.opencms.xml.content.CmsXmlContent#validate(CmsObject)}
 * is called validate a XML content object.<p>
 *
 * @since 6.0.0
 */
class CmsXmlContentValidationVisitor implements I_CmsXmlContentValueVisitor {

    /** Static reference to the log. */
    private static final Log LOG = CmsLog.getLog(CmsXmlContentValidationVisitor.class);

    /** The initialized OpenCms user context (required for VFS access). */
    CmsObject m_cms;

    /** The error handler instance that stores the errors and warnings found. */
    CmsXmlContentErrorHandler m_errorHandler;

    /**
     * Creates a new validation node visitor.<p>
     *
     * @param cms the initialized OpenCms user context (required for VFS access)
     */
    public CmsXmlContentValidationVisitor(CmsObject cms) {

        // start with a new instance of the error handler
        m_errorHandler = new CmsXmlContentErrorHandler();
        // store reference to the provided CmsObject
        m_cms = cms;
    }

    /**
     * Returns the error handler instance that stores the errors and warnings found.<p>
     *
     * @return the error handler instance that stores the errors and warnings found
     */
    public CmsXmlContentErrorHandler getErrorHandler() {

        return m_errorHandler;
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentValueVisitor#visit(org.opencms.xml.types.I_CmsXmlContentValue)
     */
    public void visit(I_CmsXmlContentValue value) {

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_XMLCONTENT_VISIT_1, value.getPath()));
        }

        m_errorHandler = value.getContentDefinition().getContentHandler().resolveValidation(
            m_cms,
            value,
            m_errorHandler);
    }
}