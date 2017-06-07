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

package org.opencms.workplace.editors.directedit;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsRequestContext;
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.i18n.I_CmsMessageBundle;

import java.util.Locale;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.PageContext;

/**
 * Convenience class to access the localized messages of this OpenCms package.
 * <p>
 *
 *
 * Additionally two utility methods for localization in the context of
 * {@link javax.servlet.jsp.tagext.TagSupport} implementations of the <code>org.opencms.jsp</code>
 * package are provided .
 * <p>
 *
 * The <code>javax.servlet.jsp.tagext.TagSupport</code> API constraint only allows to throw
 * certain <code>Exception</code> types which forbids to use {@link org.opencms.main.CmsException}
 * which will be localized with the current user's locale at the time the request is evaluated.
 * <p>
 *
 * At the same time <code>TagSupport</code> implementations may use their member
 * <code>pageContext</code> to get the <code>CmsObject</code> and therefore the user request's
 * locale.
 * <p>
 *
 * These methods provided here factor out the localization of exception messages and return Strings
 * for the <code>org.opencms.jsp</code> pacakge.
 * <p>
 *
 * @since 9.0.0
 */
public final class Messages extends A_CmsMessageBundle {

    /** Message constant for key in the resource bundle. */
    public static final String GUI_CLICK_TO_ADD_ELEMENT_TO_EMPTY_LIST_0 = "GUI_CLICK_TO_ADD_ELEMENT_TO_EMPTY_LIST_0";

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.workplace.editors.directedit.messages";

    /** Static instance member. */
    private static final I_CmsMessageBundle INSTANCE = new Messages();

    /**
     * Hides the public constructor for this utility class.
     * <p>
     */
    private Messages() {

        // hide the constructor
    }

    /**
     * Returns an instance of this localized message accessor.
     * <p>
     *
     * @return an instance of this localized message accessor
     */
    public static I_CmsMessageBundle get() {

        return INSTANCE;
    }

    /**
     * Returns the String for the given CmsMessageContainer localized to the current user's locale
     * if available or to the default locale else.
     * <p>
     *
     * This method is needed for localization of non- {@link org.opencms.main.CmsException}
     * instances that have to be thrown here due to API constraints (javax.servlet.jsp).
     * <p>
     *
     * @param container A CmsMessageContainer containing the message to localize.
     * @param cms the <code>CmsObject</code> belonging to the current user (e.g. obtained with
     *            <code>CmsFlexController.getCmsObject(ServletRequest)</code>).
     * @return the String for the given CmsMessageContainer localized to the current user's locale
     *         if available or to the default locale else.
     *         <p>
     */
    public static String getLocalizedMessage(CmsMessageContainer container, CmsObject cms) {

        Locale locale;
        if (cms != null) {
            CmsRequestContext context = cms.getRequestContext();
            locale = (context != null) ? context.getLocale() : Locale.getDefault();
        } else {
            locale = Locale.getDefault();
        }
        return container.key(locale);
    }

    /**
     * Returns the String for the given CmsMessageContainer localized to the current user's locale
     * if available or to the default locale else.
     * <p>
     *
     * This method is needed for localization of non- {@link org.opencms.main.CmsException}
     * instances that have to be thrown here due to API constraints (javax.servlet.jsp).
     * <p>
     *
     * @param container A CmsMessageContainer containing the message to localize.
     * @param context The page context that is known to any calling
     *            {@link javax.servlet.jsp.tagext.TagSupport} instance (member
     *            <code>pageContext</code>).
     * @return the String for the given CmsMessageContainer localized to the current user's locale
     *         if available or to the default locale else.
     *         <p>
     */
    public static String getLocalizedMessage(CmsMessageContainer container, PageContext context) {

        return Messages.getLocalizedMessage(container, context.getRequest());
    }

    /**
     * Returns the String for the given CmsMessageContainer localized to the current user's locale
     * if available or to the default locale else.
     * <p>
     *
     * This method allows a static method ({@link org.opencms.jsp.CmsJspTagInfo#infoTagAction(String, javax.servlet.http.HttpServletRequest)})
     * that has no <code>pageContext</code> in scope to lookup the locale at request time.
     * <p>
     *
     * @see #getLocalizedMessage(CmsMessageContainer, PageContext)
     * @param container A CmsMessageContainer containing the message to localize.
     * @param request The current request.
     * @return the String for the given CmsMessageContainer localized to the current user's locale
     *         if available or to the default locale else.
     *         <p>
     */
    public static String getLocalizedMessage(CmsMessageContainer container, ServletRequest request) {

        CmsObject cms = CmsFlexController.getCmsObject(request);
        return getLocalizedMessage(container, cms);

    }

    /**
     * Returns the bundle name for this OpenCms package.
     * <p>
     *
     * @return the bundle name for this OpenCms package
     */
    public String getBundleName() {

        return BUNDLE_NAME;
    }
}
