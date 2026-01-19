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

package org.opencms.xml.content;

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.file.CmsObject;

import java.util.Locale;

/**
 * Interface for 'content augmentations' which can be used to augment the currently edited content.
 *
 * <p>This can effectively replace the complete content, but in
 */
public interface I_CmsXmlContentAugmentation {

    /**
     * Context provided to the content augmentation class.
     *
     * <p>Used to access the original content, the currently edited locale, the current CmsObject, etc., and to send the new content back to the editor at the end.
     */
    public interface Context {

        public String getParameter(String param);

        /**
         * Helper method for calling the JSP at the given path.
         *
         * <p>The context will be made available as the request attribute 'context' in the called JSP.
         *
         * <p>This method can not be used reentrantly (i.e. from a JSP which has already been called via callJsp).
         *
         * @param path the path of the JSP to call
         * @throws Exception if something goes wrong
         */
        void callJsp(String path) throws Exception;

        /**
         * The ADE configuration that's currently being used.
         *
         * @return the ADE configuration
         */
        CmsADEConfigData getADEConfig();

        /**
         * Gets the current CMS context.
         *
         * @return the current CMS context
         */
        CmsObject getCmsObject();

        /**
         * Gets a copy of the currently edited XML content.
         *
         * <p>Modifying this object by itself does not do anything for the content editor, you have to send either the modified object
         * or another modified copy back to the content editor via setResult().
         *
         * @return a copy of the currently edited XML content
         */
        CmsXmlContent getContent();

        /**
         * Gets the currently edited locale of the content.
         *
         * @return the currently edited locale of the content
         */
        Locale getLocale();

        boolean isAborted();

        void progress(String progressMessage);

        /**
         * Sets the HTML to display to the user after the augmentation.
         * @param message
         */
        void setHtmlMessage(String message);

        /**
         * Sets the locale to switch to.
         *
         * <p>If not called, the current locale will be used if possible.
         *
         * @param locale the locale to switch to
         */
        void setNextLocale(Locale locale);

        /**
         * Sends the augmented content back to the content editor.
         * @param result the augmented content
         */
        void setResult(CmsXmlContent result);
    }

    /**
     * The name of the request attribute used to pass the augmentation context to JSPs.
     */
    public static final String ATTR_CONTEXT = "context";

    /**
     * Augments the content provided in the given context.
     *
     *  <p>Implementations can query the given context for information like the original XML content and the current locale, do some processing,
     *  and, in the end, call the setResult method to provide the finished, augmented content.
     *
     * @param context the augmentation context - provides access to the content and further information
     * @throws Exception if something goes wrong
     */
    void augmentContent(Context context) throws Exception;

}
