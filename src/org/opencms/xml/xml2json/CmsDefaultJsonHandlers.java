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

package org.opencms.xml.xml2json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Provides default JSON handlers.
 *
 * <p>Some JSON handlers are always provided, they do not need to be loaded via ServiceLoader.
 */
public class CmsDefaultJsonHandlers {

    /** The folder handler instance. */
    private static CmsFolderJsonHandler m_folderHandler = new CmsFolderJsonHandler();

    /** The XML handler instance. */
    private static I_CmsJsonHandler m_xmlContentHandler = new CmsOnlineCachingHandlerWrapper(
        new CmsXmlContentJsonHandler(),
        "concurrencyLevel=4,maximumSize=10000");

    /** The JSP handler instance. */
    private static CmsJspJsonHandler m_jspHandler = new CmsJspJsonHandler();

    /** The container page handler instance. */
    private static CmsContainerPageJsonHandler m_containerPageHandler = new CmsContainerPageJsonHandler();

    /** The list config handler instance. */
    private static CmsListConfigJsonHandler m_listConfigJsonHandler = new CmsListConfigJsonHandler();

    /**
     * Gets the default JSON handlers.
     *
     * @return the list of default JSON handlers
     */
    public static List<I_CmsJsonHandler> getHandlers() {

        return new ArrayList<>(
            Arrays.asList(
                m_folderHandler,
                m_xmlContentHandler,
                m_jspHandler,
                m_containerPageHandler,
                m_listConfigJsonHandler));
    }

}
