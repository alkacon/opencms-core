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

package org.opencms.ade.contenteditor.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Contains status information about a content augmentation that has just been run.
 */
public class CmsContentAugmentationDetails implements IsSerializable {

    /** The message to display, as HTML (null for no message) . */
    private String m_htmlMessage;

    /**
     * The list of locales from the augmented content.
     */
    private List<String> m_locales;

    /** The locale to switch to (null to not switch locale). */
    private String m_nextLocale;

    /**
     * Creates a new instance.
     */
    public CmsContentAugmentationDetails() {

    }

    /**
     * Gets the message to display to the user, in HTML format.
     *
     * @return the HTML message
     */
    public String getHtmlMessage() {

        return m_htmlMessage;
    }

    /**
     * Gets the locales that are part of the augmented content (as strings).
     *
     * @return the list of locales
     */
    public List<String> getLocales() {

        return m_locales;
    }

    /**
     * Gets tje next locale to switch to (might be null if we should remain in the current locale).
     *
     * @return the locale to switch to
     */
    public String getNextLocale() {

        return m_nextLocale;
    }

    /**
     * Sets the message to display, in HTML format
     *
     * @param htmlMessage the message to display
     */
    public void setHtmlMessage(String htmlMessage) {

        m_htmlMessage = htmlMessage;
    }

    /**
     * Sets the locales.
     *
     * @param locales the locales
     */
    public void setLocales(List<String> locales) {

        m_locales = locales;
    }

    /**
     * Sets the next locale to switch to (can be null if current locale should be maintained).
     *
     * @param nextLocale the next locale to switch to
     */
    public void setNextLocale(String nextLocale) {

        m_nextLocale = nextLocale;
    }

}
