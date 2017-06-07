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

package org.opencms.jsp.decorator;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.xml.content.CmsXmlContent;

import java.util.Locale;

/**
 * Interface for a CmsDecoratorConfiguration.<p>
 *
 * This interface describes a CmsDecoratorConfiguration which provides methods to
 * get a filled CmsDecorationBundle. A decoration bundle contains a datastructure
 * of text decorations which can be used in the current request context.
 *
 */
public interface I_CmsDecoratorConfiguration {

    /**
     * Builds a CmsDecorationDefintion from a given configuration file.<p>
     *
     * @param configuration the configuration file
     * @param i the number of the decoration definition to create
     * @return CmsDecorationDefintion created form configuration file
     */
    CmsDecorationDefintion getDecorationDefinition(CmsXmlContent configuration, int i);

    /**
     * Gets the decoration bundle.<p>
     *@return the decoration bundle to be used
     */
    CmsDecorationBundle getDecorations();

    /**
     * Tests if a decoration key was used before in this configuration.<p>
     * @param key the key to look for
     * @return true if this key was already used
     */
    boolean hasUsed(String key);

    /**
     * Initialises the configuration.<p>
     *
     *@param cms the CmsObject
     * @param configFile the configuration file
     * @param locale to locale to build this configuration for
     * @throws CmsException if something goes wrong
     */
    void init(CmsObject cms, String configFile, Locale locale) throws CmsException;

    /**
     * Tests if a tag is contained in the exclude list of the decorator.<p>
     *
     * @param tag the tag to test
     * @return true if the tag is in the exclode list, false othwerwise.
     */
    boolean isExcluded(String tag);

    /**
     * Mark a decoration key as already used.<p>
     * @param key the key to mark
     */
    void markAsUsed(String key);

    /**
     * Resets the used decoration keys.<p>
     */
    void resetMarkedDecorations();
}
