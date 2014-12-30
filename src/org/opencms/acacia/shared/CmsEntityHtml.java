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

package org.opencms.acacia.shared;

import java.io.Serializable;

/**
 * The entity HTML representation including validation data.<p>
 */
public class CmsEntityHtml implements Serializable {

    /** The serial version id. */
    private static final long serialVersionUID = 8744574711101111191L;

    /** The HTML representation. */
    private String m_htmlContent;

    /** The validation result. */
    private CmsValidationResult m_validationResult;

    /**
     * Constructor.<p>
     * 
     * @param htmlContent the HTML representation
     * @param validationResult the validation result
     */
    public CmsEntityHtml(String htmlContent, CmsValidationResult validationResult) {

        m_htmlContent = htmlContent;
        m_validationResult = validationResult;
    }

    /**
     * Constructor needed for serialization.<p>
     */
    protected CmsEntityHtml() {

    }

    /**
     * Returns the HTML representation.<p>
     * 
     * @return the HTML representation
     */
    public String getHtmlContent() {

        return m_htmlContent;
    }

    /**
     * Returns the validation result.<p>
     * 
     * @return the validation result
     */
    public CmsValidationResult getValidationResult() {

        return m_validationResult;
    }

}
