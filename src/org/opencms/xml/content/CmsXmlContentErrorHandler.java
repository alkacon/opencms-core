/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/content/CmsXmlContentErrorHandler.java,v $
 * Date   : $Date: 2004/12/01 17:36:03 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.xml.content;

import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Handler for issues found during XML content validation.<p> 
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.1 $
 * @since 5.5.4
 */
public class CmsXmlContentErrorHandler {

    /** The list of validation errors. */
    private Map m_errors;

    /** Indicates that the validated content has errors. */
    private boolean m_hasErrors;

    /** Indicates that the validated content has warnings. */
    private boolean m_hasWarnings;

    /** The list of validation warnings. */
    private Map m_warnings;

    /**
     * Create a new instance of the validation handler.<p>
     */
    public CmsXmlContentErrorHandler() {

        // initialize the internal error / warning list
        m_warnings = new HashMap();
        m_errors = new HashMap();
    }

    /**
     * Adds an error message to the internal list of errors,
     * also raised the "has errors" flag.<p>
     * 
     * @param value the value that contians the error
     * @param message the error message to add
     */
    public void addError(I_CmsXmlContentValue value, String message) {

        m_hasErrors = true;
        int todo = 0;
        // TODO: this will not work for nested schemas, must add a "getPath()" method to the value interface
        m_errors.put(value.getElementName(), message);
    }

    /**
     * Adds an warning message to the internal list of errors,
     * also raised the "has warning" flag.<p>
     * 
     * @param value the value that contians the warning
     * @param message the warning message to add
     */
    public void addWarning(I_CmsXmlContentValue value, String message) {

        m_hasWarnings = true;
        int todo = 0;
        // TODO: this will not work for nested schemas, must add a "getPath()" method to the value interface
        m_warnings.put(value.getElementName(), message);
    }

    /**
     * Returns the map of validation errors.<p>
     *
     * @return the map of validation errors
     */
    public Map getErrors() {

        return m_errors;
    }

    /**
     * Returns the map of validation warnings.<p>
     *
     * @return the map of validation warnings
     */
    public Map getWarnings() {

        return m_warnings;
    }

    /**
     * Returns true if the validated content had errors.<p>
     *
     * @return true if the validated content had errors
     */
    public boolean hasErrors() {

        return m_hasErrors;
    }

    /**
     * Returns true if the validated content has warnings.<p>
     *
     * @return true if the validated content had warnings
     */
    public boolean hasWarnings() {

        return m_hasWarnings;
    }
}