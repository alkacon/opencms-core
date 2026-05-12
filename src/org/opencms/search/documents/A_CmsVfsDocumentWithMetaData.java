/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.search.documents;

/**
 * Base class for all document factories that allow to configure if meta data should be extracted or ignored.
 */
public abstract class A_CmsVfsDocumentWithMetaData extends A_CmsVfsDocument {

    /** Parameter key for configuring if meta data should be extracted or not. */
    public static final String PARAM_EXTRACT_METADATA = "extract.metadata";

    /** Flag, indicating if metadata should be extracted. */
    private boolean m_extractMetaData;

    /**
     * Creates a new instance of this document factory.<p>
     *
     * @param name name of the documenttype
     */
    public A_CmsVfsDocumentWithMetaData(String name) {

        this(name, true);
    }

    /**
     * Creates a new instance of this document factory.<p>
     *
     * @param name name of the documenttype
     * @param extractMetaDataByDefault flag, indicating if meta data should be extracted by default.
     */
    public A_CmsVfsDocumentWithMetaData(String name, boolean extractMetaDataByDefault) {

        super(name);
        m_extractMetaData = extractMetaDataByDefault;
    }

    /**
     * @see org.opencms.search.documents.I_CmsDocumentFactory#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    @Override
    public void addConfigurationParameter(String key, String value) {

        switch (key) {
            case PARAM_EXTRACT_METADATA:
                m_extractMetaData = !"false".equalsIgnoreCase(value);
                break;
            default:
                // do nothing;
        }
    }

    /**
     * @return flag, indicating if meta data should be extracted.
     */
    public boolean isExtractMetaData() {

        return m_extractMetaData;
    }
}
