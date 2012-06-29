/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.gwt.shared.alias;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A bean representing the result of trying to import a single alias.<p>
 */
public class CmsAliasImportResult implements IsSerializable {

    /** The line containing the data for the alias. */
    private String m_line;

    /** The message from importing the alias. */
    private String m_message;

    /** The import status. */
    private CmsAliasImportStatus m_status;

    /**
     * Creates a new instance.<p>
     * 
     * @param status the import status 
     * @param message the import message
     */
    public CmsAliasImportResult(CmsAliasImportStatus status, String message) {

        m_message = message;
        m_status = status;
    }

    /**
     * Creates a new instance.<p>
     * 
     * @param line the line containing the alias data 
     * @param status the import status 
     * @param message the import message 
     */
    public CmsAliasImportResult(String line, CmsAliasImportStatus status, String message) {

        m_line = line;
        m_status = status;
        m_message = message;
    }

    /**
     * Default constructor used for serialization.<p>
     */
    protected CmsAliasImportResult() {

        // do nothing 
    }

    /** 
     * Gets the line containing the alias data.<p>
     * 
     * @return the line containing the alias data 
     */
    public String getLine() {

        return m_line;
    }

    /**
     * Gets the import message.<p>
     *  
     * @return the import message 
     */
    public String getMessage() {

        return m_message;
    }

    /**
     * Gets the status.<p>
     * 
     * @return the status 
     */
    public CmsAliasImportStatus getStatus() {

        return m_status;
    }

    /**
     * Sets the line containing the alias data.<p>
     * 
     * @param line the line containing the alias data 
     */
    public void setLine(String line) {

        m_line = line;
    }

}
