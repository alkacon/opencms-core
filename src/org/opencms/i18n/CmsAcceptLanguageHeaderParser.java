/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/i18n/CmsAcceptLanguageHeaderParser.java,v $
 * Date   : $Date: 2005/10/02 09:03:16 $
 * Version: $Revision: 1.12.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * This file is based on:
 * org.apache.fulcrum.localization.LocaleTokenizer
 * from the Apache Fulcrum/Turbine project.
 * 
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Turbine" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Turbine", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.opencms.i18n;

import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

import javax.servlet.http.HttpServletRequest;

/**
 * Parses the HTTP <code>Accept-Language</code> header as per section 14.4 of RFC 2068 
 * (HTTP 1.1 header field definitions) and creates a sorted list of Locales from it.
 * 
 * @author Daniel Rall 
 * @author Alexander Kandzior
 *   
 * @version $Revision: 1.12.2.1 $ 
 * 
 * @since 6.0.0 
 */
public class CmsAcceptLanguageHeaderParser implements Iterator {

    /**
     * Struct representing an element of the HTTP <code>Accept-Language</code> header.
     */
    private class AcceptLanguage implements Comparable {

        /** The language and country. */
        Locale m_locale;

        /**  The m_quality of our m_locale (as values approach <code>1.0</code>, they indicate increased user preference). */
        Float m_quality = DEFAULT_QUALITY;

        /**
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        public final int compareTo(Object acceptLang) {

            return m_quality.compareTo(((AcceptLanguage)acceptLang).m_quality);
        }
    }

    /** A constant for the HTTP <code>Accept-Language</code> header. */
    public static final String ACCEPT_LANGUAGE = "Accept-Language";

    /** The default m_quality value for an <code>AcceptLanguage</code> object. */
    protected static final Float DEFAULT_QUALITY = new Float(1.0f);

    /** Separates elements of the <code>Accept-Language</code> HTTP header. */
    private static final char LOCALE_SEPARATOR = ',';

    /** Separates m_locale from m_quality within elements. */
    private static final char QUALITY_SEPARATOR = ';';

    /** The parsed <code>Accept-Language</code> headers. */
    private List m_acceptLanguage = new ArrayList(3);

    /** The parsed locales. */
    private List m_locales;

    /**
     * Parses the <code>Accept-Language</code> header from the provided request.<p>
     * 
     * @param req the request to parse
     * @param defaultLocale the default locale to use
     */
    public CmsAcceptLanguageHeaderParser(HttpServletRequest req, Locale defaultLocale) {

        this(req.getHeader(ACCEPT_LANGUAGE), defaultLocale);
    }

    /**
     * Parses the <code>Accept-Language</code> header.<p>
     * 
     * @param header the <code>Accept-Language</code> header (i.e. <code>en, es;q=0.8, zh-TW;q=0.1</code>)
     * @param defaultLocale the default locale to use
     */
    public CmsAcceptLanguageHeaderParser(String header, Locale defaultLocale) {

        // check if there was a locale foud in the HTTP header.
        // if not, use the default locale.
        if (header == null) {
            m_locales = new ArrayList();
            m_locales.add(defaultLocale);
        } else {
            List tokens = CmsStringUtil.splitAsList(header, LOCALE_SEPARATOR, true);
            Iterator it = tokens.iterator();
            while (it.hasNext()) {
                AcceptLanguage acceptLang = new AcceptLanguage();
                String element = (String)it.next();
                int index;

                // Record and cut off any quality value that comes after a semi-colon.
                if ((index = element.indexOf(QUALITY_SEPARATOR)) != -1) {
                    String q = element.substring(index);
                    element = element.substring(0, index);
                    if ((index = q.indexOf('=')) != -1) {
                        try {
                            acceptLang.m_quality = Float.valueOf(q.substring(index + 1));
                        } catch (NumberFormatException useDefault) {
                            // noop
                        }
                    }
                }

                element = element.trim();

                // Create a Locale from the language. A dash may separate the language from the country.
                if ((index = element.indexOf('-')) == -1) {
                    // No dash means no country.
                    acceptLang.m_locale = new Locale(element, "");
                } else {
                    acceptLang.m_locale = new Locale(element.substring(0, index), element.substring(index + 1));
                }

                m_acceptLanguage.add(acceptLang);
            }

            // sort by quality in descending order
            Collections.sort(m_acceptLanguage, Collections.reverseOrder());

            // store all calculated Locales in a List
            m_locales = new ArrayList(m_acceptLanguage.size());
            Iterator i = m_acceptLanguage.iterator();
            while (i.hasNext()) {
                AcceptLanguage lang = (AcceptLanguage)i.next();
                m_locales.add(lang.m_locale);
            }
        }

    }

    /**
     * Creates a value string for the HTTP Accept-Language header based on the default localed.<p>
     * 
     * @return value string for the HTTP Accept-Language
     */
    public static String createLanguageHeader() {

        String header;

        // get the default accept-language header value
        List defaultLocales = OpenCms.getLocaleManager().getDefaultLocales();
        Iterator i = defaultLocales.iterator();
        header = "";
        while (i.hasNext()) {
            Locale loc = (Locale)i.next();
            header += loc.getLanguage() + ", ";
        }
        header = header.substring(0, header.length() - 2);
        return header;
    }

    /**
     * Returns the sorted list of accepted Locales.<p>
     * 
     * @return the sorted list of accepted Locales
     */
    public List getAcceptedLocales() {

        return m_locales;
    }

    /**
     * @return Whether there are more locales.
     */
    public boolean hasNext() {

        return !m_acceptLanguage.isEmpty();
    }

    /**
     * Creates a <code>Locale</code> from the next element of the <code>Accept-Language</code> header.
     * 
     * @return The next highest-rated <code>Locale</code>.
     */
    public Object next() {

        if (m_acceptLanguage.isEmpty()) {
            throw new NoSuchElementException();
        }
        return ((AcceptLanguage)m_acceptLanguage.remove(0)).m_locale;
    }

    /**
     * Not implemented.
     * 
     * @throws CmsIllegalArgumentException always to signal that remove is not implemented 
     *         (<b>interface contract defines {@link UnsupportedOperationException}</b>) 
     */
    public final void remove() throws CmsIllegalArgumentException {

        throw new CmsRuntimeException(org.opencms.db.Messages.get().container(
            org.opencms.db.Messages.ERR_UNSUPPORTED_OPERATION_2,
            getClass().getName(),
            "remove()"));
    }
}