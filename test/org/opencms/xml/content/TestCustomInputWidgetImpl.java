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

package org.opencms.xml.content;

import org.opencms.widgets.CmsInputWidget;
import org.opencms.widgets.I_CmsWidget;

/**
 * Extension of the base select widget for testing the "custom widget class" function in the XML content
 * schema annotation.<p>
 */
public class TestCustomInputWidgetImpl extends CmsInputWidget {

    /***
     * Base constructor.<p>
     */
    public TestCustomInputWidgetImpl() {

        super();
    }

    /***
     * Base constructor with configuration String.<p>
     *
     * @param configuration the configuration String to use
     */
    public TestCustomInputWidgetImpl(String configuration) {

        super(configuration);
    }

    /**
     * @see org.opencms.widgets.CmsInputWidget#newInstance()
     */
    @Override
    public I_CmsWidget newInstance() {

        return new TestCustomInputWidgetImpl(getConfiguration());
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#setConfiguration(java.lang.String)
     */
    @Override
    public void setConfiguration(String configuration) {

        super.setConfiguration(configuration + "[some addition here]");
    }
}