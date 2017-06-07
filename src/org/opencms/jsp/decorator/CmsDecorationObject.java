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

package org.opencms.jsp.decorator;

import org.opencms.util.CmsMacroResolver;

import java.util.Locale;

/**
 * The CmsDecorationObject defines a single text decoration.<p>
 *
 * It uses the information of a <code>{@link CmsDecorationDefintion}</code> to create the
 * pre- and postfix for a text decoration.

 *
 * @since 6.1.3
 */
public class CmsDecorationObject {

    /** Macro for the decoration. */
    public static final String MACRO_DECORATION = "decoration";

    /** Macro for the decoration key. */
    public static final String MACRO_DECORATIONKEY = "decorationkey";

    /** Macro for the language. */
    public static final String MACRO_LANGUAGE = "language";

    /** Macro for the locale. */
    public static final String MACRO_LOCALE = "locale";

    /** The decoration. */
    private String m_decoration;

    /** The CmsDecorationDefintion to be used by this decoration object. */
    private CmsDecorationDefintion m_decorationDefinition;

    /** The key for this decoration. */
    private String m_decorationKey;

    /** The locale of this decoration. */
    private Locale m_locale;

    /**
     * Constructor, creates a new, empty decoration object.<p>
     */
    public CmsDecorationObject() {

        m_decorationDefinition = new CmsDecorationDefintion();
    }

    /**
     * Constructor, creates a new decoration object with given values.<p>
     *
     * @param decorationKey the decoration key
     * @param decoration the decoration for this decoration key
     * @param decDef the decoration defintion to be used
     * @param locale the locale of this decoration object
     */
    public CmsDecorationObject(String decorationKey, String decoration, CmsDecorationDefintion decDef, Locale locale) {

        m_decorationKey = decorationKey;
        m_decoration = decoration;
        m_decorationDefinition = decDef;
        m_locale = locale;
    }

    /**
     * Gets the decorated content for this decoration object.<p>
     *
     * @param config the configuration used
     * @param text the text to be decorated
     * @param contentLocale the locale of the content to be decorated
     * @return decorated content
     */
    public String getContentDecoration(I_CmsDecoratorConfiguration config, String text, String contentLocale) {

        StringBuffer content = new StringBuffer();
        // TODO: we have to handle with word phrases, too

        // add the pretext
        if (!config.hasUsed(m_decorationKey) && m_decorationDefinition.isMarkFirst()) {
            content.append(m_decorationDefinition.getPreTextFirst());
        } else {
            content.append(m_decorationDefinition.getPreText());
        }
        // now add the original word
        content.append(text);

        // add the posttext
        if (!config.hasUsed(m_decorationKey) && m_decorationDefinition.isMarkFirst()) {
            content.append(m_decorationDefinition.getPostTextFirst());
            config.markAsUsed(m_decorationKey);
        } else {
            content.append(m_decorationDefinition.getPostText());
        }

        // replace the occurance of the ${decoration} makro in the decorated text
        return replaceMacros(content.toString(), contentLocale);
    }

    /**
     * Returns the decoration.<p>
     *
     * @return the decoration
     */
    public String getDecoration() {

        return m_decoration;
    }

    /**
     * Returns the decorationDefinition.<p>
     *
     * @return the decorationDefinition
     */
    public CmsDecorationDefintion getDecorationDefinition() {

        return m_decorationDefinition;
    }

    /**
     * Returns the decorationKey.<p>
     *
     * @return the decorationKey
     */
    public String getDecorationKey() {

        return m_decorationKey;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        StringBuffer buf = new StringBuffer();
        buf.append(this.getClass().getName());
        buf.append(" [name = '");
        buf.append(m_decorationKey);
        buf.append("', decoration = '");
        buf.append(m_decoration);
        buf.append("', locale = '");
        buf.append(m_locale);
        buf.append("' decorationDefinition ='");
        buf.append(m_decorationDefinition);
        buf.append("']");
        return buf.toString();
    }

    /**
     * Replaces the macros in the given message.<p>
     *
     * @param msg the message in which the macros are replaced
     * @param contentLocale the locale of the content that is currently decorated
     *
     * @return the message with the macros replaced
     */
    private String replaceMacros(String msg, String contentLocale) {

        CmsMacroResolver resolver = CmsMacroResolver.newInstance();
        resolver.addMacro(MACRO_DECORATION, m_decoration);
        resolver.addMacro(MACRO_DECORATIONKEY, m_decorationKey);
        if (m_locale != null) {
            resolver.addMacro(MACRO_LOCALE, m_locale.toString());
            if (!contentLocale.equals(m_locale.toString())) {
                resolver.addMacro(MACRO_LANGUAGE, "lang=\"" + m_locale.toString() + "\"");
            }
        }

        return resolver.resolveMacros(msg);
    }

}
