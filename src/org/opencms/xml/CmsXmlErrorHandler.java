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

package org.opencms.xml;

import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;

import org.apache.commons.logging.Log;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Error hander for writing errors found during XML validation to the OpenCms log.<p>
 *
 * Exceptions caused by warnings are suppressed (but written to the log if level is set to WARN).<p>
 *
 * @since 6.0.0
 */
public class CmsXmlErrorHandler implements ErrorHandler {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsXmlErrorHandler.class);

    /** The name of the resource that is parsed, for logging (optional). */
    private String m_resourceName;

    /**
     * Creates an OpenCms XML error handler.<p>
     */
    public CmsXmlErrorHandler() {

        this("");
    }

    /**
     * Creates an OpenCms XML error handler with a resource name for error logging.<p>
     *
     * @param resourceName the name (path) of the XML resource that is handled, for logging
     */
    public CmsXmlErrorHandler(String resourceName) {

        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(resourceName)) {
            m_resourceName = " " + resourceName;
        } else {
            m_resourceName = "";
        }
    }

    /**
     * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
     */
    public void error(SAXParseException exception) throws SAXException {

        LOG.error(Messages.get().getBundle().key(Messages.LOG_PARSING_XML_RESOURCE_ERROR_1, m_resourceName), exception);
        throw exception;
    }

    /**
     * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
     */
    public void fatalError(SAXParseException exception) throws SAXException {

        LOG.error(
            Messages.get().getBundle().key(Messages.LOG_PARSING_XML_RESOURCE_FATAL_ERROR_1, m_resourceName),
            exception);
        throw exception;
    }

    /**
     * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
     */
    public void warning(SAXParseException exception) {

        if (LOG.isWarnEnabled()) {
            LOG.error(
                Messages.get().getBundle().key(Messages.LOG_PARSING_XML_RESOURCE_WARNING_1, m_resourceName),
                exception);
        }
    }
}
