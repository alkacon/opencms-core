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

package org.opencms.i18n;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * A property based resource bundle with increased visibility of some key methods.<p>
 *
 * @since 6.2.0
 *
 * @see org.opencms.i18n.CmsResourceBundleLoader
 */
public class CmsPropertyResourceBundle extends PropertyResourceBundle implements I_CmsResourceBundle {

    /** The locale to use. */
    protected Locale m_locale;

    /**
     * Default constructor from parent class.<p>
     *
     * @param stream property file to read from
     *
     * @throws IOException in case the file could not be read from
     */
    public CmsPropertyResourceBundle(InputStream stream)
    throws IOException {

        super(stream);
    }

    /**
     * @see org.opencms.i18n.I_CmsResourceBundle#getClone()
     */
    public I_CmsResourceBundle getClone() {

        // doesn't need to be cloned
        return this;
    }

    /**
     * @see java.util.ResourceBundle#getLocale()
     */
    @Override
    public Locale getLocale() {

        return m_locale;
    }

    /**
     * @see org.opencms.i18n.I_CmsResourceBundle#setLocale(java.util.Locale)
     */
    public void setLocale(Locale l) {

        m_locale = l;
    }

    /**
     * @see org.opencms.i18n.I_CmsResourceBundle#setParent(java.util.ResourceBundle)
     */
    @Override
    public void setParent(ResourceBundle p) {

        super.setParent(p);
    }
}