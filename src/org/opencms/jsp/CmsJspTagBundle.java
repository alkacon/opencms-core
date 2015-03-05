/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.i18n.CmsResourceBundleLoader;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.servlet.ServletResponse;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;

import org.apache.taglibs.standard.tag.common.fmt.BundleSupport;
import org.apache.taglibs.standard.tag.common.fmt.SetLocaleSupport;
import org.apache.taglibs.standard.tag.el.fmt.BundleTag;

/**
 * Provides tag access to OpenCms resource bundles.<p>
 * 
 * This replaces the <code>&lt;fmt:bundle basename=""&gt;</code> tag which is not capable of using OpenCms resource bundles.<p>
 * 
 * You can use <code>&lt;fmt:message key=""&gt;</code> tags inside the <code>&lt;cms:bundle basename=""&gt;</code> tag as usual.
 * 
 * @since 8.5.2
 */
public class CmsJspTagBundle extends BundleTag {

    /** Serial version UID required for safe serialisation. */
    private static final long serialVersionUID = 7592250223728101278L;

    /** The basename attribute value. */
    private String m_basename;

    /** The localization to use. */
    private LocalizationContext m_locCtxt;

    /**
     * Empty constructor.<p>
     */
    public CmsJspTagBundle() {

        super();
        m_basename = null;
        m_locCtxt = null;
    }

    /**
     * Returns the initialized localization context.<p>
     * @param pc the current page context
     * @param basename the bas name of the bundle
     * 
     * @return the initialized localization context
     */
    public static LocalizationContext getLocalizationContext(PageContext pc, String basename) {

        LocalizationContext locCtxt = null;
        ResourceBundle bundle = null;

        if ((basename == null) || basename.equals("")) {
            return new LocalizationContext();
        }

        // Try preferred locales
        Locale pref = getLocale(pc, javax.servlet.jsp.jstl.core.Config.FMT_LOCALE);
        if (pref != null) {
            // Preferred locale is application-based
            bundle = findMatch(basename, pref);
            if (bundle != null) {
                locCtxt = new LocalizationContext(bundle, pref);
            }
        }

        if (locCtxt == null) {
            // No match found with preferred locales, try using fallback locale
            locCtxt = BundleSupport.getLocalizationContext(pc, basename);
        } else {
            // set response locale
            if (locCtxt.getLocale() != null) {
                setResponseLocale(pc, locCtxt.getLocale());
            }
        }

        return locCtxt;
    }

    /**
     * Returns the locale specified by the named scoped attribute or context
     * configuration parameter.
     *
     * <p> The named scoped attribute is searched in the page, request,
     * session (if valid), and application scope(s) (in this order). If no such
     * attribute exists in any of the scopes, the locale is taken from the
     * named context configuration parameter.
     *
     * @param pageContext the page in which to search for the named scoped
     * attribute or context configuration parameter
     * @param name the name of the scoped attribute or context configuration
     * parameter
     *
     * @return the locale specified by the named scoped attribute or context
     * configuration parameter, or <tt>null</tt> if no scoped attribute or
     * configuration parameter with the given name exists
     */
    static Locale getLocale(PageContext pageContext, String name) {

        Locale loc = null;

        Object obj = javax.servlet.jsp.jstl.core.Config.find(pageContext, name);
        if (obj != null) {
            if (obj instanceof Locale) {
                loc = (Locale)obj;
            } else {
                loc = SetLocaleSupport.parseLocale((String)obj);
            }
        }

        return loc;
    }

    /**
     * Stores the given locale in the response object of the given page
     * context, and stores the locale's associated charset in the
     * javax.servlet.jsp.jstl.fmt.request.charset session attribute, which
     * may be used by the <requestEncoding> action in a page invoked by a
     * form included in the response to set the request charset to the same as
     * the response charset (this makes it possible for the container to
     * decode the form parameter values properly, since browsers typically
     * encode form field values using the response's charset).
     *
     * @param pc the page context whose response object is assigned
     * the given locale
     * @param locale the response locale
     */
    static void setResponseLocale(PageContext pc, Locale locale) {

        // set response locale
        ServletResponse response = pc.getResponse();
        response.setLocale(locale);

        // get response character encoding and store it in session attribute
        if (pc.getSession() != null) {
            try {
                pc.setAttribute(
                    "javax.servlet.jsp.jstl.fmt.request.charset",
                    response.getCharacterEncoding(),
                    PageContext.SESSION_SCOPE);
            } catch (IllegalStateException ex) {
                // invalidated session ignored
            }
        }
    }

    /**
     * Gets the resource bundle with the given base name and preferred locale.
     *
     * @param basename the resource bundle base name
     * @param pref the preferred locale
     */
    private static ResourceBundle findMatch(String basename, Locale pref) {

        ResourceBundle match = null;
        try {
            ResourceBundle bundle = CmsResourceBundleLoader.getBundle(basename, pref);
            match = bundle;
        } catch (MissingResourceException mre) {
            // ignored
        }
        return match;
    }

    /**
     * Internal action method.<p>
     * 
     * @return EVAL_BODY_BUFFERED
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    @Override
    public int doStartTag() {

        m_locCtxt = getLocalizationContext(pageContext, getBasename());
        return EVAL_BODY_BUFFERED;
    }

    /**
     * Returns the basename attribute value.<p>
     * 
     * @return the basename attribute value
     */
    public String getBasename() {

        return m_basename;
    }

    /**
     * Returns the localization context to use.<p>
     * 
     * @see org.apache.taglibs.standard.tag.common.fmt.BundleSupport#getLocalizationContext()
     */
    @Override
    public LocalizationContext getLocalizationContext() {

        // TODO: Auto-generated method stub
        return m_locCtxt;
    }

    /**
     * Sets the basename attribute value.<p>
     * 
     * @param bn the basename attribute value
     * 
     * @see org.apache.taglibs.standard.tag.el.fmt.BundleTag#setBasename(java.lang.String)
     */
    @Override
    public void setBasename(String bn) {

        m_basename = bn;
    }

}
