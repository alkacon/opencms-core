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

package org.opencms.pdftools;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is responsbile for creating and parsing links to generated PDFs.<p>
 */
public class CmsPdfLink {

    /**
     * Exception which is thrown when parsing a link as a PDF link fails.<p<
     */
    public static class CmsPdfLinkParseException extends Exception {

        /** Serial version id. */
        private static final long serialVersionUID = 1L;

    }

    /** Group of characters without slashes. */
    public static final String NOSLASH_GROUP = "([^/]+)";

    /** The prefix string for PDF links. */
    public static final String PDF_LINK_PREFIX = "pdflink";

    /** Regular expression for parsing PDF links. */
    public static final String PDF_LINK_REGEX = PDF_LINK_PREFIX
        + "/"
        + NOSLASH_GROUP
        + "/"
        + "("
        + CmsUUID.UUID_REGEX
        + ")"
        + "/"
        + NOSLASH_GROUP
        + "\\.pdf/?";

    /** Compiled regular expression for parsing PDF links. */
    public static final Pattern PDF_LINK_REGEX_COMPILED = Pattern.compile(PDF_LINK_REGEX);

    /** The content resource of the PDF link. */
    private CmsResource m_content;

    /** The formatter resource of the PDF link. */
    private CmsResource m_formatter;

    /** The PDF link. */
    private String m_link;

    /** The locale of the PDF link. */
    private Locale m_locale;

    /**
     * Creates a new PDF link object based on the formatter and content resources and the locale of the current CMS context.<p>
     *
     * @param cms the current CMS context
     * @param formatter the formatter resource
     * @param content the content resource
     * @throws CmsException if something goes wrong
     */
    public CmsPdfLink(CmsObject cms, CmsResource formatter, CmsResource content)
    throws CmsException {

        Locale locale = cms.getRequestContext().getLocale();
        m_content = content;
        m_locale = locale;
        String detailName = cms.getDetailName(
            content,
            cms.getRequestContext().getLocale(),
            OpenCms.getLocaleManager().getDefaultLocales());
        String s = "/" + PDF_LINK_PREFIX + "/" + locale + "/" + formatter.getStructureId() + "/" + detailName + ".pdf";
        m_link = OpenCms.getLinkManager().substituteLink(cms, s);
    }

    /**
     * Creates a PDF link object by parsing it from a link string.<p>
     *
     * @param cms the current CMS context
     * @param link the link as a string
     *
     * @throws CmsPdfLinkParseException if the given link is not a PDF link
     * @throws CmsException if something else goes wrong
     */
    public CmsPdfLink(CmsObject cms, String link)
    throws CmsPdfLinkParseException, CmsException {

        Matcher matcher = PDF_LINK_REGEX_COMPILED.matcher(link);
        m_link = link;
        if (matcher.find()) {
            String localeStr = matcher.group(1);
            String formatterId = matcher.group(2);
            String detailName = matcher.group(3);
            CmsUUID id = cms.readIdForUrlName(detailName);
            m_content = cms.readResource(id, CmsResourceFilter.ignoreExpirationOffline(cms));
            m_locale = CmsLocaleManager.getLocale(localeStr);
            m_formatter = cms.readResource(new CmsUUID(formatterId));
        } else {
            throw new CmsPdfLinkParseException();
        }
    }

    /**
     * Returns the content.<p>
     *
     * @return the content
     */
    public CmsResource getContent() {

        return m_content;
    }

    /**
     * Gets the formatter resource.<p>
     *
     * @return the formatter resource
     */
    public CmsResource getFormatter() {

        return m_formatter;
    }

    /**
     * Returns the link.<p>
     *
     * @return the link
     */
    public String getLink() {

        return m_link;
    }

    /**
     * Returns the locale.<p>
     *
     * @return the locale
     */
    public Locale getLocale() {

        return m_locale;
    }
}
