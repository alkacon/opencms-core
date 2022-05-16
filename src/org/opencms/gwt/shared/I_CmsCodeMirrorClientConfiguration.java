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

package org.opencms.gwt.shared;

/**
 * AutoBean interface that represents the configuration for the client-side CodeMirror editor widget.
 */
public interface I_CmsCodeMirrorClientConfiguration {

    /**
     * Gets the height, in pixels.
     *
     *  @return the height (or null if no height was set)
     */
    public Integer getHeight();

    /**
     * Gets the CodeMirror I18N phrases, as a JSON string.
     *
     * @return a JSON string containing the CodeMirror I18N phrases
     */
    public String getPhrasesJSON();

    /**
     * Gets the start mode.
     *
     * @return the start mode
     */
    public String getStartMode();

    /**
     * Sets the height in pixels.
     *
     * @param height the height in pixesls
     */
    public void setHeight(Integer height);

    /**
     * Sets the CodeMirror I18N phrases as a JSON string
     *
     * @param json a JSON string with the CodeMirror I18N phrases
     */
    public void setPhrasesJSON(String json);

    /**
     * Sets the start mode.
     *
     * @param startMode the start mode
     */
    public void setStartMode(String startMode);

}
