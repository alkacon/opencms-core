/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/CmsXmlValidationErrorHandler.java,v $
 * Date   : $Date: 2005/02/17 12:45:12 $
 * Version: $Revision: 1.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.xml;

import org.opencms.util.CmsStringUtil;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.util.XMLErrorHandler;
import org.xml.sax.SAXParseException;

/**
 * Error hander for writing errors found during XML validation to the OpenCms log.<p>
 * 
 * Exceptions caused by warnings are suppressed (but written to the log if level is set to WARN).<p>
 * 
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @version $Revision: 1.4 $
 */
public class CmsXmlValidationErrorHandler extends XMLErrorHandler {

    /** Stores the warnings that occur during a SAX parse. */
    private Element m_warnings;

    /**
     * Constructor from superclass.<p> 
     */
    public CmsXmlValidationErrorHandler() {

        super();
        m_warnings = DocumentHelper.createElement("warnings");
    }

    /**
     * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
     */
    public void error(SAXParseException e) {

        String message = e.getMessage();
        if (CmsStringUtil.isNotEmpty(message)) {

            if (message.startsWith("sch-props-correct.2:")) {
                // HACK: multiple schema includes cause errors in validation with Xerces 2
                // the schema nevertheless is usable 
                // redirect this error to be a warning
                warning(e);
                return;
            }
        }

        super.error(e);
    }

    /**
     * Returns the warnings.<p>
     *
     * @return the warnings
     */
    public Element getWarnings() {

        return m_warnings;
    }

    /**
     * @see org.dom4j.util.XMLErrorHandler#warning(org.xml.sax.SAXParseException)
     */
    public void warning(SAXParseException e) {

        Element element = m_warnings.addElement(WARNING_QNAME);
        addException(element, e);
    }
}