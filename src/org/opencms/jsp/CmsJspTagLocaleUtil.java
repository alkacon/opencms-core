/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/Attic/CmsJspTagLocaleUtil.java,v $
 * Date   : $Date: 2005/05/03 12:17:52 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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
 
package org.opencms.jsp;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsRequestContext;
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsException;

import java.util.Locale;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;

/**
 * Utility for localization in the context of {@link javax.servlet.jsp.tagext.TagSupport} 
 * implementations of the <code>org.opencms.jsp</code> package. Instances never may (and can) 
 * be created as this class is a stateless helper with static-only methods. <p>
 * 
 * The <code>javax.servlet.jsp.tagext.TagSupport</code> API constraint only allows 
 * to throw certain <code>Exception</code> types which forbids to use {@link org.opencms.main.CmsException} 
 * which will be localized with the current user's locale at the time the request is evaluated. <p>
 * 
 * At the same time <code>TagSupport</code> implementations may use their member <code>pageContext</code> 
 * to get the <code>CmsObject</code> and therefore the user request's locale. <p>
 * 
 * This class factors out the localization of exception messages and returned Strings 
 * for the <code>org.opencms.jsp</code> pacakge. 
 * 
 * @author westermann a.westermann@alkacon.com
 * 
 */
public final class CmsJspTagLocaleUtil {

    /**
     * No instance ever may be created. This is no singleton but 
     * a stateless helper with static-only methods. <p>
     *
     */
    private CmsJspTagLocaleUtil(){
        // disallow instantiation.
    }
    
    /**
     * Returns the String for the given CmsMessageContainer localized to the 
     * current user's locale if available or to the default locale else. <p>
     * 
     * This method is needed for localization of non- {@link CmsException} instances 
     * that have to be thrown here due to API constraints (javax.servlet.jsp) and returned error strings. <p>
     * 
     * @param container A CmsMessageContainer containing the message to localize. 
     * @param context The page context that is known to any calling {@link javax.servlet.jsp.tagext.TagSupport} instance (member <code>pageContext</code>). 
     * @return the String for the given CmsMessageContainer localized to the 
     *         current user's locale if available or to the default locale else. <p>
     */
    public static String getLocalizedMessage(CmsMessageContainer container, PageContext context) {

        return getLocalizedMessage(container, context.getRequest());
    }
    
    /**
     * Returns the String for the given CmsMessageContainer localized to the 
     * current user's locale if available or to the default locale else. <p>
     * 
     * This method allows a static method ({@link CmsJspTagInfo#infoTagAction(String, HttpServletRequest)}) 
     * that has no <code>pageContext</code> in scope to lookup the locale at request time. <p>
     *  
     * @see #getLocalizedMessage(CmsMessageContainer, PageContext)
     * @param container A CmsMessageContainer containing the message to localize. 
     * @param request The current request. 
     * @return the String for the given CmsMessageContainer localized to the 
     *         current user's locale if available or to the default locale else. <p>
     */
    public static String getLocalizedMessage(CmsMessageContainer container, ServletRequest request) {

        String msg;
        CmsRequestContext requestContext;
        Locale locale;
        CmsObject cms = CmsFlexController.getCmsObject(request);
        if ((cms == null)
            || ((requestContext = cms.getRequestContext()) == null)
            || ((locale = requestContext.getLocale()) == null)) {
            msg = container.key();
        } else {
            msg = container.key(locale);
        }
        return msg;
    }
}
