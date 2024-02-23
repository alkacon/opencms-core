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

package org.opencms.jsp;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.jsp.util.CmsJspLinkWrapper;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.staticexport.CmsLinkManager;
import org.opencms.util.CmsStringUtil;

import java.util.Locale;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.logging.Log;

/**
 * Implements the <code>&lt;cms:link&gt;[filename]&lt;/cms:link&gt;</code>
 * tag to add OpenCms managed links to a JSP page, required for link
 * management and the static
 * export to work properly.<p>
 *
 * @since 6.0.0
 */
public class CmsJspTagLink extends BodyTagSupport {

    /**
     * Parameters for the link tag.
     */
    public static class Parameters {

        /** The target. */
        public String m_target;

        /** The base uri. */
        public String m_baseUri;

        /** The detail page. */
        public String m_detailPage;

        /** The locale. */
        public Locale m_locale;

        /** The type. */
        public Type m_type;

        /**
         * Default constructor.
         */
        public Parameters() {}

        /**
         * Instantiates a new link tag params.
         *
         * @param target the target
         * @param baseUri the base uri
         * @param detailPage the detail page
         * @param locale the locale
         * @param type the type
         */
        public Parameters(String target, String baseUri, String detailPage, Locale locale, Type type) {

            m_target = target;
            m_baseUri = baseUri;
            m_detailPage = detailPage;
            m_locale = locale;
            m_type = type != null ? type : Type.DEFAULT;
        }

        /**
         * Gets the base uri.
         *
         * @return the base uri
         */
        public String getBaseUri() {

            return m_baseUri;
        }

        /**
         * Gets the detail page.
         *
         * @return the detail page
         */
        public String getDetailPage() {

            return m_detailPage;
        }

        /**
         * Gets the locale.
         *
         * @return the locale
         */
        public Locale getLocale() {

            return m_locale;
        }

        /**
         * Gets the target.
         *
         * @return the target
         */
        public String getTarget() {

            return m_target;
        }

        /**
         * Gets the type.
         *
         * @return the type
         */
        public Type getType() {

            return m_type;
        }

        /**
         * Sets the base uri.
         *
         * @param baseUri the new base uri
         */
        public void setBaseUri(String baseUri) {

            m_baseUri = baseUri;
        }

        /**
         * Sets the detail page.
         *
         * @param detailPage the new detail page
         */
        public void setDetailPage(String detailPage) {

            m_detailPage = detailPage;
        }

        /**
         * Sets the locale.
         *
         * @param locale the new locale
         */
        public void setLocale(Locale locale) {

            m_locale = locale;
        }

        /**
         * Sets the target.
         *
         * @param target the new target
         */
        public void setTarget(String target) {

            m_target = target;
        }

        /**
         * Sets the type.
         *
         * @param type the new type
         */
        public void setType(Type type) {

            m_type = type;
        }
    }

    /** Link type. */
    public enum Type {
        /** Default mode. */
        DEFAULT,
        /** Online link mode. */
        ONLINE,

        /** Server link mode. */
        SERVER,

        /** Permalink mode. */
        PERMA;
    }

    /** String describing request scope. */
    private static final String SCOPE_REQUEST = "request";

    /** String describing session scope. */
    private static final String SCOPE_SESSION = "session";

    /** String describing application scope. */
    private static final String SCOPE_APPLICATION = "application";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJspTagLink.class);

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = -2361021288258405388L;

    /** The variable to store the result. */
    private String m_var;

    /** The scope used for the var parameter. */
    private String m_scope;

    /** The optional base URI to create the link from. */
    private String m_baseUri;

    /** The target detail page path. */
    private String m_detailPage;

    /** The optional locale attribute. */
    private Locale m_locale;

    /** The link type. */
    private Type m_type;

    /**
     * Tries to read the active locale for the given (site) path, or for its parent path if the path can't be read.
     * <p>
     * If this fails, null is returned.
     *
     * @param cms the CMS context
     * @param baseUri the base URI for which to read the locale
     * @return the locale
     */
    public static Locale getBaseUriLocale(CmsObject cms, String baseUri) {

        try {
            try {
                return OpenCms.getLocaleManager().getDefaultLocale(
                    cms,
                    cms.readResource(baseUri, CmsResourceFilter.IGNORE_EXPIRATION));
            } catch (CmsVfsResourceNotFoundException e) {
                String parent = CmsResource.getParentFolder(baseUri);
                if (parent != null) {
                    return OpenCms.getLocaleManager().getDefaultLocale(
                        cms,
                        cms.readResource(parent, CmsResourceFilter.IGNORE_EXPIRATION));
                }
            }
        } catch (CmsException e) {
            LOG.info(e.getLocalizedMessage(), e);
        }
        return null;

    }

    /**
     * Returns a link to a file in the OpenCms VFS
     * that has been adjusted according to the web application path and the
     * OpenCms static export rules.<p>
     *
     * <p>If the <code>baseUri</code> parameter is provided, this will be treated as the source of the link,
     * if this is <code>null</code> then the current OpenCms user context URI will be used as source.</p>
     *
     * <p>If the <code>locale</code> parameter is provided, the locale in the request context will be switched
     * to the provided locale. This influences only the behavior of the
     * {@link org.opencms.staticexport.CmsLocalePrefixLinkSubstitutionHandler}.</p>
     *
     *
     * Relative links are converted to absolute links, using the current element URI as base.<p>
     * @param params TODO
     * @param req the current request
     *
     * @return the target link adjusted according to the web application path and the OpenCms static export rules
     *
     * @see #linkTagAction(String, ServletRequest)
     *
     * @since 8.0.3
     */
    public static String linkTagAction(Parameters params, ServletRequest req) {

        CmsFlexController controller = CmsFlexController.getController(req);
        // be sure the link is absolute
        String uri = CmsLinkManager.getAbsoluteUri(params.getTarget(), controller.getCurrentRequest().getElementUri());
        CmsObject cms = prepareCmsObject(controller.getCmsObject(), params);
        CmsLinkManager linkManager = OpenCms.getLinkManager();
        // generate the link
        switch (params.getType()) {
            case ONLINE:
                return linkManager.getOnlineLink(cms, uri, params.getDetailPage(), false);
            case PERMA:
                return linkManager.getPermalink(cms, uri);
            case SERVER:
                return linkManager.getServerLink(cms, uri);
            case DEFAULT:
            default:
                return linkManager.substituteLinkForUnknownTarget(cms, uri, params.getDetailPage(), false);

        }
    }

    /**
     * Returns a link to a file in the OpenCms VFS
     * that has been adjusted according to the web application path and the
     * OpenCms static export rules.<p>
     *
     * The current OpenCms user context URI will be used as source of the link.</p>
     *
     * Since OpenCms version 7.0.2, you can also use this method in case you are not sure
     * if the link is internal or external, as
     * {@link CmsLinkManager#substituteLinkForUnknownTarget(org.opencms.file.CmsObject, String)}
     * is used to calculate the link target.<p>
     *
     * Relative links are converted to absolute links, using the current element URI as base.<p>
     *
     * @param target the link that should be calculated, can be relative or absolute
     * @param req the current request
     *
     * @return the target link adjusted according to the web application path and the OpenCms static export rules
     *
     * @see org.opencms.staticexport.CmsLinkManager#substituteLinkForUnknownTarget(org.opencms.file.CmsObject, String)
     */
    public static String linkTagAction(String target, ServletRequest req) {

        return linkTagAction(target, req, null);
    }

    /**
     * Returns a link to a file in the OpenCms VFS
     * that has been adjusted according to the web application path and the
     * OpenCms static export rules.<p>
     *
     * If the <code>baseUri</code> parameter is provided, this will be treated as the source of the link,
     * if this is <code>null</code> then the current OpenCms user context URI will be used as source.</p>
     *
     * Relative links are converted to absolute links, using the current element URI as base.<p>
     *
     * @param target the link that should be calculated, can be relative or absolute
     * @param req the current request
     * @param baseUri the base URI for the link source
     *
     * @return the target link adjusted according to the web application path and the OpenCms static export rules
     *
     * @see #linkTagAction(String, ServletRequest)
     *
     * @since 8.0.3
     */
    public static String linkTagAction(String target, ServletRequest req, String baseUri) {

        return linkTagAction(target, req, baseUri, null);
    }

    /**
     * Returns a link to a file in the OpenCms VFS
     * that has been adjusted according to the web application path and the
     * OpenCms static export rules.<p>
     *
     * <p>If the <code>baseUri</code> parameter is provided, this will be treated as the source of the link,
     * if this is <code>null</code> then the current OpenCms user context URI will be used as source.</p>
     *
     * <p>If the <code>locale</code> parameter is provided, the locale in the request context will be switched
     * to the provided locale. This influences only the behavior of the
     * {@link org.opencms.staticexport.CmsLocalePrefixLinkSubstitutionHandler}.</p>
     *
     *
     * Relative links are converted to absolute links, using the current element URI as base.<p>
     *
     * @param target the link that should be calculated, can be relative or absolute
     * @param req the current request
     * @param baseUri the base URI for the link source
     * @param locale the locale for which the link should be created (see {@link org.opencms.staticexport.CmsLocalePrefixLinkSubstitutionHandler}
     *
     * @return the target link adjusted according to the web application path and the OpenCms static export rules
     *
     * @see #linkTagAction(String, ServletRequest)
     *
     * @since 8.0.3
     */
    public static String linkTagAction(String target, ServletRequest req, String baseUri, Locale locale) {

        return linkTagAction(target, req, baseUri, null, locale);
    }

    /**
     * Returns a link to a file in the OpenCms VFS
     * that has been adjusted according to the web application path and the
     * OpenCms static export rules.<p>
     *
     * <p>If the <code>baseUri</code> parameter is provided, this will be treated as the source of the link,
     * if this is <code>null</code> then the current OpenCms user context URI will be used as source.</p>
     *
     * <p>If the <code>locale</code> parameter is provided, the locale in the request context will be switched
     * to the provided locale. This influences only the behavior of the
     * {@link org.opencms.staticexport.CmsLocalePrefixLinkSubstitutionHandler}.</p>
     *
     *
     * Relative links are converted to absolute links, using the current element URI as base.<p>
     *
     * @param target the link that should be calculated, can be relative or absolute
     * @param req the current request
     * @param baseUri the base URI for the link source
     * @param detailPage the target detail page, in case of linking to a specific detail page
     * @param locale the locale for which the link should be created (see {@link org.opencms.staticexport.CmsLocalePrefixLinkSubstitutionHandler}
     *
     * @return the target link adjusted according to the web application path and the OpenCms static export rules
     *
     * @see #linkTagAction(String, ServletRequest)
     *
     * @since 8.0.3
     */
    public static String linkTagAction(
        String target,
        ServletRequest req,
        String baseUri,
        String detailPage,
        Locale locale) {

        return linkTagAction(new Parameters(target, baseUri, detailPage, locale, Type.DEFAULT), req);
    }

    /**
     * Initializes CmsObject with data from the tag parameters.
     *
     * @param cms1 the CmsObject to use as a base
     * @param params the parameters to use for the initialization
     * @return the initialized CmsObject
     */
    private static CmsObject prepareCmsObject(CmsObject cms1, Parameters params) {

        CmsObject cms = cms1;
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(params.getBaseUri()) || (null != params.getLocale())) {
            try {
                cms = OpenCms.initCmsObject(cms);
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(params.getBaseUri())) {
                    cms.getRequestContext().setUri(params.getBaseUri());
                    if (params.getLocale() == null) {
                        Locale baseUriLocale = getBaseUriLocale(cms, params.getBaseUri());
                        if (baseUriLocale != null) {
                            cms.getRequestContext().setLocale(baseUriLocale);
                        }
                    }
                }
                if (null != params.getLocale()) {
                    cms.getRequestContext().setLocale(params.getLocale());
                }
            } catch (CmsException e) {
                // should not happen, if it does we can't do anything useful and will just keep the original object
                LOG.debug(e.getLocalizedMessage(), e);
            }
        }
        return cms;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     *
     * @return EVAL_PAGE
     *
     * @throws JspException in case something goes wrong
     */
    @Override
    public int doEndTag() throws JspException {

        ServletRequest req = pageContext.getRequest();

        // This will always be true if the page is called through OpenCms
        if (CmsFlexController.isCmsRequest(req)) {
            try {

                // Get link-string from the body and reset body
                String link = getBodyContent().getString();
                getBodyContent().clear();
                Parameters params = new Parameters(link, getBaseUri(), getDetailPage(), m_locale, m_type);
                if (m_var != null) {
                    int scope = PageContext.PAGE_SCOPE; // default
                    if (SCOPE_REQUEST.equalsIgnoreCase(m_scope)) {
                        scope = PageContext.REQUEST_SCOPE;
                    } else if (SCOPE_SESSION.equalsIgnoreCase(m_scope)) {
                        scope = PageContext.SESSION_SCOPE;
                    } else if (SCOPE_APPLICATION.equalsIgnoreCase(m_scope)) {
                        scope = PageContext.APPLICATION_SCOPE;
                    }
                    CmsFlexController controller = CmsFlexController.getController(req);
                    CmsObject cms = prepareCmsObject(controller.getCmsObject(), params);
                    pageContext.setAttribute(m_var, new CmsJspLinkWrapper(cms, link, true), scope);
                } else {
                    // Calculate the link substitution
                    String newlink = linkTagAction(params, req);
                    // Write the result back to the page
                    getBodyContent().print(newlink);
                    getBodyContent().writeOut(pageContext.getOut());
                }
            } catch (Exception ex) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(Messages.get().getBundle().key(Messages.ERR_PROCESS_TAG_1, "link"), ex);
                }
                throw new JspException(ex);
            }
        }
        return EVAL_PAGE;
    }

    /**
     * Returns the base URI used to create the link target.<p>
     *
     * @return  the base URI used to create the link target
     */
    public String getBaseUri() {

        return m_baseUri;
    }

    /**
     * Returns the target detail page path.<p>
     *
     * @return the target detail page path
     */
    public String getDetailPage() {

        return m_detailPage;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    @Override
    public void release() {

        super.release();
    }

    /**
     * Sets the base URI used to create the link target.<p>
     *
     * @param baseUri the base URI used to create the link target
     */
    public void setBaseUri(String baseUri) {

        m_baseUri = baseUri;
    }

    /**
     * Sets the target detail page path.<p>
     *
     * @param detailPage the target detail page path
     */
    public void setDetailPage(String detailPage) {
        if ("".equals(detailPage)) {
            detailPage = null;
        }

        m_detailPage = detailPage;
    }

    /**
     * Sets the locale to use for the link.
     *
     * @param locale the locale to use for the link
     */
    public void setLocale(Locale locale) {

        m_locale = locale;
    }

    /**
     * Sets the locale for the link to create.
     *
     * @param localeName name of the locale, e.g. "en", "en_US", ...
     */
    public void setLocale(String localeName) {

        m_locale = CmsLocaleManager.getLocale(localeName);
    }

    /**
     * Sets the scope (only used in combination with the var parameter).
     * @param scope the scope for the variable
     */
    public void setScope(String scope) {

        m_scope = scope;
    }

    /**
     * Sets the type.
     *
     * @param type the link type
     */
    public void setType(String type) {

        if (type == null) {
            m_type = null;
        } else {
            m_type = Type.valueOf(type.toUpperCase());
        }

    }

    /**
     * Sets the type.
     *
     * @param type the type
     */
    public void setType(Type type) {

        m_type = type;
    }

    /**
     * Sets the variable name to store the result in.
     * @param var the variable name to store the result in
     */
    public void setVar(String var) {

        m_var = var;
    }

}