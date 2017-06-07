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

package org.opencms.i18n;

import org.opencms.gwt.I_CmsClientMessageBundle;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Tests all {@link org.opencms.i18n.I_CmsMessageBundle} instances for the OpenCms
 * core classes (folder src, org.* packages). <p>
 *
 * @since 6.0.0
 */
public final class TestCmsCoreMessageBundles extends TestCmsMessageBundles {

    /**
     * @see org.opencms.i18n.TestCmsMessageBundles#getNotLocalizedBundles(Locale)
     */
    @Override
    protected List<I_CmsMessageBundle> getNotLocalizedBundles(Locale locale) {

        return Collections.emptyList();
    }

    /**
     * @see org.opencms.i18n.TestCmsMessageBundles#getTestClientMessageBundles()
     */
    @Override
    protected List<I_CmsClientMessageBundle> getTestClientMessageBundles() throws Exception {

        return Collections.emptyList();
    }

    /**
     * @see org.opencms.i18n.TestCmsMessageBundles#getTestMessageBundles()
     */
    @Override
    protected I_CmsMessageBundle[] getTestMessageBundles() {

        return A_CmsMessageBundle.getOpenCmsMessageBundles();
    }
}
