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

package org.opencms.search.galleries;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.i18n.CmsMultiMessages;
import org.opencms.jsp.util.CmsJspContentAccessBean;
import org.opencms.jsp.util.CmsObjectFunctionTransformer;
import org.opencms.jsp.util.CmsStringTemplateRenderer;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.util.CmsCollectionsGenericWrapper;
import org.opencms.util.CmsMacroResolver;
import org.opencms.xml.A_CmsXmlDocument;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Macro resolver used to resolve macros for the gallery name mapping.<p>
 *
 * This supports the following special macros:
 * <ul>
 * <li>%(no_prefix:some more text): This will expand to "some more text" if, after expanding all other macros in the input string,
 *     there is at least one character before the occurence of this macro, and to an empty string otherwise.
 * <li>%(value:/Some/XPath): This will expand to the value under the given XPath in the XML content and locale with
 *     which the macro resolver was initialized. If no value is found under the XPath, the macro will expand to an empty string.
 * <li>%(page_nav): This will expand to the NavText property of the container page in which this element is referenced.
 *                  If this element is referenced from multiple container pages with the same locale, this macro is expanded
 *                  to an empty string.
 *<li>%(page_title): Same as %(page_nav), but uses the Title property instead of NavText.
 *</ul>
 */
public class CmsGalleryNameMacroResolver extends CmsMacroResolver {

    /** Macro prefix. */
    public static final String NO_PREFIX = "no_prefix";

    /** Pattern used to match the no_prefix macro. */
    public static final Pattern NO_PREFIX_PATTERN = Pattern.compile("%\\(" + NO_PREFIX + ":(.*?)\\)");

    /** Macro name. */
    public static final String PAGE_NAV = "page_nav";

    /** Macro name. */
    public static final String PAGE_ROOTPATH = "page_rootpath";

    /** Macro name. */
    public static final String PAGE_SITEPATH = "page_sitepath";

    /** Macro name. */
    public static final String PAGE_TITLE = "page_title";

    /** Prefix for the isDetailPage? macro. */
    public static final String PREFIX_ISDETAILPAGE = "isDetailPage?";

    /** Prefix for the stringtemplate macro. */
    public static final String PREFIX_STRINGTEMPLATE = "stringtemplate:";

    /** Macro prefix. */
    public static final String PREFIX_VALUE = "value:";

    /** The logger instance for the class. */
    private static final Log LOG = CmsLog.getLog(CmsGalleryNameMacroResolver.class);

    /** The XML content to use for the gallery name mapping. */
    private A_CmsXmlDocument m_content;

    /** The locale in the XML content. */
    private Locale m_contentLocale;

    /** The default string template source. */
    private final Function<String, String> m_defaultStringTemplateSource = s -> {
        return m_content.getHandler().getParameter(s);
    };

    /** Collection of pages the content is placed on. */
    private Collection<CmsResource> m_pages;

    /** The current string template source. */
    private Function<String, String> m_stringTemplateSource = m_defaultStringTemplateSource;

    /** Cached detail page status. */
    private Boolean m_isOnDetailPage;

    /**
     * Creates a new instance.<p>
     *
     * @param cms the CMS context to use for VFS operations
     * @param content the content to use for macro value lookup
     * @param locale the locale to use for macro value lookup
     */
    public CmsGalleryNameMacroResolver(CmsObject cms, A_CmsXmlDocument content, Locale locale) {

        setCmsObject(cms);
        if (null == locale) {
            locale = OpenCms.getLocaleManager().getBestAvailableLocaleForXmlContent(cms, content.getFile(), content);
        }
        CmsMultiMessages message = new CmsMultiMessages(locale);
        message.addMessages(OpenCms.getWorkplaceManager().getMessages(locale));
        message.addMessages(content.getContentDefinition().getContentHandler().getMessages(locale));
        setMessages(message);
        m_content = content;
        m_contentLocale = locale;
    }

    /**
     * @see org.opencms.util.CmsMacroResolver#getMacroValue(java.lang.String)
     */
    @Override
    public String getMacroValue(String macro) {

        if (macro.startsWith(PREFIX_VALUE)) {
            String path = macro.substring(PREFIX_VALUE.length());
            I_CmsXmlContentValue contentValue = m_content.getValue(path, m_contentLocale);
            String value = null;
            if (contentValue != null) {
                value = contentValue.getStringValue(m_cms);
            }
            if (value == null) {
                value = "";
            }
            return value;
        } else if (macro.equals(PAGE_TITLE)) {
            return getContainerPageProperty(CmsPropertyDefinition.PROPERTY_TITLE);
        } else if (macro.equals(PAGE_NAV)) {
            return getContainerPageProperty(CmsPropertyDefinition.PROPERTY_NAVTEXT);
        } else if (macro.equals(PAGE_ROOTPATH)) {
            return getContainerPagePath(false);
        } else if (macro.equals(PAGE_SITEPATH)) {
            return getContainerPagePath(true);
        } else if (macro.startsWith(PREFIX_STRINGTEMPLATE)) {
            return resolveStringTemplate(macro.substring(PREFIX_STRINGTEMPLATE.length()));
        } else if (macro.startsWith(PREFIX_ISDETAILPAGE)) {
            return resolveIsDetailPage(macro.substring(PREFIX_ISDETAILPAGE.length()));
        } else if (macro.startsWith(NO_PREFIX)) {
            return "%(" + macro + ")";
            // this is just to prevent the %(no_prefix:...) macro from being expanded to an empty string. We could call setKeepEmptyMacros(true) instead,
            // but that would also affect other macros.
        } else {
            return super.getMacroValue(macro);
        }
    }

    /**
     * @see org.opencms.util.CmsMacroResolver#resolveMacros(java.lang.String)
     */
    @Override
    public String resolveMacros(String input) {

        if (input == null) {
            return null;
        }
        // We are overriding this method to implement the no_prefix macro. This is because
        // we only know what the no_prefix macro should expand to after resolving all other
        // macros (there could be an arbitrary number of macros before it which might potentially
        // all expand to the empty string).
        String result = super.resolveMacros(input);
        Matcher matcher = NO_PREFIX_PATTERN.matcher(result);
        if (matcher.find()) {
            StringBuffer resultBuffer = new StringBuffer();
            matcher.appendReplacement(
                resultBuffer,
                matcher.start() == 0 ? "" : result.substring(matcher.start(1), matcher.end(1)));
            matcher.appendTail(resultBuffer);
            result = resultBuffer.toString();
        }
        return result;
    }

    public void setStringTemplateSource(Function<String, String> stringtemplateSource) {

        if (stringtemplateSource == null) {
            stringtemplateSource = m_defaultStringTemplateSource;
        }
        m_stringTemplateSource = stringtemplateSource;
    }

    /**
     * Returns the site path of the page the resource is on, iff it is on exactly one page per locale.
     * @return the site path of the page the resource is on, iff it is on exactly one page per locale.
     */
    protected String getContainerPagePath(boolean isSitePath) {

        Collection<CmsResource> pages = getContainerPages();
        if (pages.isEmpty()) {
            return null;
        }
        Map<Locale, String> pagePathByLocale = Maps.newHashMap();
        for (CmsResource page : pages) {
            Locale pageLocale = OpenCms.getLocaleManager().getDefaultLocale(m_cms, page);
            String pagePathCandidate = page.getRootPath();
            if (isSitePath) {
                pagePathCandidate = OpenCms.getSiteManager().getSiteForRootPath(pagePathCandidate).getSitePath(
                    pagePathCandidate);
            }
            if (pagePathCandidate != null) {
                if (pagePathByLocale.get(pageLocale) == null) {
                    pagePathByLocale.put(pageLocale, pagePathCandidate);
                } else {
                    return ""; // more than one container page per locale is referencing this content.
                }
            }
        }
        Locale matchingLocale = OpenCms.getLocaleManager().getBestMatchingLocale(
            m_contentLocale,
            OpenCms.getLocaleManager().getDefaultLocales(),
            Lists.newArrayList(pagePathByLocale.keySet()));
        String result = pagePathByLocale.get(matchingLocale);
        if (result == null) {
            result = "";
        }
        return result;
    }

    /**
     * Gets the given property of the container page referencing this content.<p>
     *
     * If more than one container page with the same locale reference this content, the empty string will be returned.
     *
     * @param propName the property name to look up
     *
     * @return the value of the named property on the container page, or an empty string
     */
    protected String getContainerPageProperty(String propName) {

        Collection<CmsResource> pages = getContainerPages();
        if (pages.isEmpty()) {
            return "";
        }
        try {
            Map<Locale, String> pagePropsByLocale = Maps.newHashMap();
            for (CmsResource page : pages) {
                List<CmsProperty> pagePropertiesList = m_cms.readPropertyObjects(page, true);
                Map<String, CmsProperty> pageProperties = CmsProperty.toObjectMap(pagePropertiesList);
                Locale pageLocale = OpenCms.getLocaleManager().getDefaultLocale(m_cms, page);
                CmsProperty pagePropCandidate = pageProperties.get(propName);
                if (pagePropCandidate != null) {
                    if (pagePropsByLocale.get(pageLocale) == null) {
                        pagePropsByLocale.put(pageLocale, pagePropCandidate.getValue());
                    } else {
                        return ""; // more than one container page per locale is referencing this content.
                    }
                }
            }
            Locale matchingLocale = OpenCms.getLocaleManager().getBestMatchingLocale(
                m_contentLocale,
                OpenCms.getLocaleManager().getDefaultLocales(),
                Lists.newArrayList(pagePropsByLocale.keySet()));
            String result = pagePropsByLocale.get(matchingLocale);
            if (result == null) {
                result = "";
            }
            return result;
        } catch (CmsException e) {
            LOG.warn(e.getLocalizedMessage(), e);
            return "";
        }
    }

    /**
     * Returns all container pages the content is placed on.
     * @return all container pages the content is placed on.
     */
    Collection<CmsResource> getContainerPages() {

        if (null != m_pages) {
            return m_pages;
        }
        m_pages = new HashSet<CmsResource>();
        try {
            Collection<CmsRelation> relations = m_cms.readRelations(
                CmsRelationFilter.relationsToStructureId(m_content.getFile().getStructureId()));
            for (CmsRelation relation : relations) {
                CmsResource source = relation.getSource(m_cms, CmsResourceFilter.IGNORE_EXPIRATION);
                if (CmsResourceTypeXmlContainerPage.isContainerPage(source)) {
                    m_pages.add(source);
                }
            }
        } catch (CmsException e) {
            LOG.warn(e.getLocalizedMessage(), e);
            m_pages = Collections.emptySet();
        }
        return m_pages;
    }

    /**
     * Checks if we are on a detail page (using the path from the CmsObject).
     *
     * @return true if we are on a detail page
     */
    private boolean isOnDetailPage() {

        if (m_isOnDetailPage != null) {
            return m_isOnDetailPage.booleanValue();
        }
        try {
            String uri = m_cms.getRequestContext().getUri();
            CmsResource page = m_cms.readResource(uri);
            boolean result = OpenCms.getADEManager().isDetailPage(m_cms, page);
            m_isOnDetailPage = Boolean.valueOf(result);
            return result;
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            return false;
        }
    }

    /**
     * Evaluates the content of the isDetailPage? macro.
     *
     * <p>
     * The macro has the form %(isDetailPage?foo:bar), and in the case the current page (according to the URI of the CmsObject)
     * is a detail page, the macro should evaluate to 'foo', and otherwise to 'bar'.
     *
     * @param content the macro content
     * @return the macro value
     */
    private String resolveIsDetailPage(String content) {

        int colonPos = content.indexOf(":");
        if (colonPos != -1) {
            String beforeColon = content.substring(0, colonPos);
            String afterColon = content.substring(colonPos + 1);
            return isOnDetailPage() ? beforeColon : afterColon;
        } else {
            LOG.error("Invalid body for isDetailPage? macro: " + content);
            return content;
        }
    }

    /**
     * Evaluates the contents of a %(stringtemplate:...) macro by evaluating them as StringTemplate code.<p>
     *
     * @param stMacro the contents of the macro after the stringtemplate: prefix
     * @return the StringTemplate evaluation result
     */
    private String resolveStringTemplate(String stMacro) {

        String template = m_stringTemplateSource.apply(stMacro.trim());
        if (template == null) {
            return "";
        }
        CmsJspContentAccessBean jspContentAccess = new CmsJspContentAccessBean(m_cms, m_contentLocale, m_content);
        Map<String, Object> params = Maps.newHashMap();
        params.put(
            CmsStringTemplateRenderer.KEY_FUNCTIONS,
            CmsCollectionsGenericWrapper.createLazyMap(new CmsObjectFunctionTransformer(m_cms)));

        // We don't necessarily need the page title / navigation, so instead of passing the computed values to the template, we pass objects whose
        // toString methods compute the values
        params.put(PAGE_TITLE, new Object() {

            @Override
            public String toString() {

                return getContainerPageProperty(CmsPropertyDefinition.PROPERTY_TITLE);
            }
        });

        params.put("isDetailPage", Boolean.valueOf(isOnDetailPage()));

        params.put(PAGE_NAV, new Object() {

            @Override
            public String toString() {

                return getContainerPageProperty(CmsPropertyDefinition.PROPERTY_NAVTEXT);

            }
        });
        String result = CmsStringTemplateRenderer.renderTemplate(m_cms, template, jspContentAccess, params);
        return result;
    }
}
