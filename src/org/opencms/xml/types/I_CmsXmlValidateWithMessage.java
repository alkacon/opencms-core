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

package org.opencms.xml.types;

import org.opencms.i18n.CmsMessageContainer;
import org.opencms.xml.content.CmsDefaultXmlContentHandler;

/**
 * Interface to provide specific error messages on validation</p>
 * Should be used as an extension of {@link I_CmsXmlSchemaType}.
 * The {@link CmsDefaultXmlContentHandler} checks for that interface when it validates values.
 */
public interface I_CmsXmlValidateWithMessage {

    /**
     * Checks if a given value is valid according validation rules of this schema type in the XML schema.<p>
     *
     * In contrast to {@link I_CmsXmlSchemaType#validateValue(String)} a specific error message is returned
     * in the case of validation errors.<p>
     *
     * @param value the value to validate
     *
     * @return <code>null</code> if the value is valid, a localized error message otherwise.
     */
    CmsMessageContainer validateWithMessage(String value);
}
