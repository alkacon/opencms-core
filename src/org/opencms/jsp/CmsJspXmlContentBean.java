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

package org.opencms.jsp;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Provides access to XML content tag functions from scriptlet code.<p>
 *
 * Otherwise provides all functions from the parent class <code>{@link org.opencms.jsp.CmsJspActionElement}</code>.<p>
 *
 * @since 6.2.0
 */
public class CmsJspXmlContentBean extends CmsJspActionElement {

    /**
     * Empty constructor, required for every JavaBean.
     *
     * @see CmsJspActionElement#CmsJspActionElement()
     */
    public CmsJspXmlContentBean() {

        super();
    }

    /**
     * Constructor, with parameters.
     *
     * @param context the JSP page context object
     * @param req the JSP request
     * @param res the JSP response
     *
     * @see CmsJspActionElement#CmsJspActionElement(PageContext, HttpServletRequest, HttpServletResponse)
     */
    public CmsJspXmlContentBean(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super(context, req, res);
    }

    /**
     * Loads a set of <code>{@link org.opencms.xml.I_CmsXmlDocument}</code>, same as
     * using the <code>&lt;cms:contentload collector="***" param="***" editable="***" /&gt;</code> tag.<p>
     *
     * The locale for accessing the content is read form the current OpenCms users request context.<p>
     *
     * @param collectorName the collector name to use
     * @param collectorParam the parameters for the collector
     * @param editable indicates if "direct edit" support is required (will insert additional HTML)
     *
     * @return an XML content container loaded with the selected content
     *
     * @throws JspException in case something goes wrong
     */
    public I_CmsXmlContentContainer contentload(String collectorName, String collectorParam, boolean editable)
    throws JspException {

        return contentload(collectorName, collectorParam, getRequestContext().getLocale(), editable);
    }

    /**
     * Loads a set of <code>{@link org.opencms.xml.I_CmsXmlDocument}</code>, same as
     * using the <code>&lt;cms:contentload collector="***" param="***" locale="***" editable="***" /&gt;</code> tag.<p>
     *
     * @param collectorName the collector name to use
     * @param collectorParam the parameters for the collector
     * @param locale the locale to use to access the content
     * @param editable indicates if "direct edit" support is required (will insert additional HTML)
     *
     * @return an XML content container loaded with the selected content
     *
     * @throws JspException in case something goes wrong
     */
    public I_CmsXmlContentContainer contentload(
        String collectorName,
        String collectorParam,
        Locale locale,
        boolean editable) throws JspException {

        return new CmsJspTagContentLoad(null, getJspContext(), collectorName, collectorParam, locale, editable);
    }

    /**
     * Loads a set of <code>{@link org.opencms.xml.I_CmsXmlDocument}</code>, same as
     * using the <code>&lt;cms:contentload collector="***" param="***" locale="***" editable="***" /&gt;</code> tag.<p>
     *
     * @param collectorName the collector name to use
     * @param collectorParam the collector param to use
     * @param pageIndex the display page index (may contain macros)
     * @param pageSize the display page size (may contain macros)
     * @param locale the locale to use to access the content
     * @param editable indicates if "direct edit" support is required (will insert additional HTML)
     *
     * @return an XML content container loaded with the selected content
     *
     * @throws JspException in case something goes wrong
     */
    public I_CmsXmlContentContainer contentload(
        String collectorName,
        String collectorParam,
        String pageIndex,
        String pageSize,
        Locale locale,
        boolean editable) throws JspException {

        return new CmsJspTagContentLoad(
            null,
            getJspContext(),
            collectorName,
            collectorParam,
            pageIndex,
            pageSize,
            locale,
            editable);
    }

    /**
     * Enables looping over a list of element values in the given parent container, same as
     * using the <code>&lt;cms:contentloop element="***" /&gt;</code> tag.<p>
     *
     * @param container the XML content container to read the content from
     * @param element the element to loop over
     *
     * @return an XML content container to be used to loop over the selected element values in the parent container
     */
    public I_CmsXmlContentContainer contentloop(I_CmsXmlContentContainer container, String element) {

        return new CmsJspTagContentLoop(container, element);
    }

    /**
     * Returns the currently looped content element String value from the given XML content container, same as
     * using the <code>&lt;cms:contentshow /&gt;</code> tag.<p>
     *
     * This is to be used with a container initialized by <code>{@link #contentloop(I_CmsXmlContentContainer, String)}</code>,
     * in this case the element name is already set by the content loop container.<p>
     *
     * The locale for accessing the content is read form the current OpenCms users request context.<p>
     *
     * @param container the XML content container to read the content from
     *
     * @return the selected content element String value from the given XML content container
     */
    public String contentshow(I_CmsXmlContentContainer container) {

        return contentshow(container, null, null);
    }

    /**
     * Returns the selected content element String value from the given XML content container, same as
     * using the <code>&lt;cms:contentshow element="***" /&gt;</code> tag.<p>
     *
     * The locale for accessing the content is read form the current OpenCms users request context.<p>
     *
     * @param container the XML content container to read the content from
     * @param element the element to show
     *
     * @return the selected content element String value from the given XML content container
     */
    public String contentshow(I_CmsXmlContentContainer container, String element) {

        return contentshow(container, element, null);
    }

    /**
     * Returns the selected content element String value from the given XML content container, same as
     * using the <code>&lt;cms:contentshow element="***" locale="***" /&gt;</code> tag.<p>
     *
     * @param container the XML content container to read the content from
     * @param element the element to show
     * @param locale the locale to read the element from
     *
     * @return the selected content element String value from the given XML content container
     */
    public String contentshow(I_CmsXmlContentContainer container, String element, Locale locale) {

        return CmsJspTagContentShow.contentShowTagAction(container, getJspContext(), element, locale, false);
    }
}