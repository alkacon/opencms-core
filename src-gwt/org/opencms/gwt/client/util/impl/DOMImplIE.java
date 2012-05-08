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

package org.opencms.gwt.client.util.impl;

import com.google.gwt.dom.client.Document;

/**
 * Helper class for DOM operations not made available by the GWT core classes.<p>
 * 
 * Implementation for MSIE 7 + 8 browsers, do not use for MSIE 9.<p>
 * 
 * @since 8.0.0
 */
public class DOMImplIE extends DOMImpl {

    /**
     * @see org.opencms.gwt.client.util.impl.DOMImpl#createIFrameElement(com.google.gwt.dom.client.Document, java.lang.String)
     */
    @Override
    public native com.google.gwt.dom.client.Element createIFrameElement(Document doc, String name) /*-{
        return doc.createElement("<iframe name='" + name + "'/>");
    }-*/;
}
