/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.xml.sitemap;

/**
 * Sitemap Manager.<p>
 * 
 * Provides all relevant functions for using the sitemap.<p>
 * 
 * @since 8.0.0
 */
public class CmsSitemapManager {

    /** Property name constants. */
    public enum Property {

        /** <code>navigation</code> property name. */
        navigation("navigation"),
        /** <code>sitemap</code> property name. */
        sitemap("sitemap"),
        /** <code>template</code> property name. */
        template("template"),
        /** <code>template-inhertited</code> property name. */
         templateInherited("template-inherited"),
        internalRedirect("internalRedirect"), externalRedirect(
        "externalRedirect"), isRedirect("isRedirect");

        /** The name of the property. */
        private final String m_name;

        /**
         * Default constructor.<p>
         * 
         * @param name the name of the property
         */
        private Property(String name) {

            m_name = name;
        }

        /**
         * Returns the name of the property.<p>
         * 
         * @return the name of the property
         */
        public String getName() {

            return m_name;
        }

        /**
         * @see java.lang.Enum#toString()
         */
        @Override
        public String toString() {

            return m_name;
        }
    }
}
