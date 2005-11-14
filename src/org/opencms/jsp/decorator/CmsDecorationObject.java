/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/decorator/CmsDecorationObject.java,v $
 * Date   : $Date: 2005/11/14 15:04:05 $
 * Version: $Revision: 1.1.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 */

package org.opencms.jsp.decorator;

import org.opencms.util.CmsMacroResolver;

import java.util.Locale;

/**
 * The CmsDecorationObject defines a single text decoration.<p>
 * 
 * It uses the information of a <code>{@link CmsDecorationDefintion}</code> to create the
 * pre- and postfix for a text decoration.
 
 * @author Michael Emmerich  
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 6.1.3 
 */
public class CmsDecorationObject {

    /** Macro for the decoration. */
    public static final String MACRO_DECORATION = "decoration";

    /** Macro for the locale. */
    public static final String MACRO_LOCALE = "locale";

    /** The decoration. */
    private String m_decoration;

    /** The CmsDecorationDefintion to be used by this decoration object. */
    private CmsDecorationDefintion m_decorationDefinition;

    /** The key for this decoration. */
    private String m_decorationKey;

    /** Flag if this decoration object was used once. */
    private boolean m_firstOccurance;

    /** The locale of this decoration. */
    private Locale m_locale;

    /**
     * Constructor, creates a new, empty decoration object.<p>
     */
    public CmsDecorationObject() {

        m_decorationDefinition = new CmsDecorationDefintion();
        m_firstOccurance = true;
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
        m_firstOccurance = true;
        m_locale = locale;
    }

    /**
     * Gets the decorated content for this decoration object.<p>
     * 
     * @return decorated content
     */
    public String getContentDecoration() {

        StringBuffer content = new StringBuffer();
        // TODO: we have to handle with word phrases, too

        // add the pretext
        if (isFirstOccurance() && m_decorationDefinition.isMarkFirst()) {
            content.append(m_decorationDefinition.getPreTextFirst());
        } else {
            content.append(m_decorationDefinition.getPreText());
        }
        // now add the original word
        content.append(m_decorationKey);

        // add the posttext
        if (isFirstOccurance() && m_decorationDefinition.isMarkFirst()) {
            content.append(m_decorationDefinition.getPostTextFirst());
            m_firstOccurance = false;
        } else {
            content.append(m_decorationDefinition.getPostText());
        }

        // replace the occurance of the ${decoration} makro in the decorated text
        return replaceMacros(content.toString());
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
     * Resets the first occurance flag.<p> 
     * 
     * This should be done if the same instance of a CmsDecorationObject is reused for processing
     * a second time.     *
     */
    public void resetFirstOccuranceFlag() {

        m_firstOccurance = true;
    }

    /**
     * Sets the decoration.<p>
     *
     * @param decoration the decoration to set
     */
    public void setDecoration(String decoration) {

        m_decoration = decoration;
    }

    /**
     * Sets the decorationDefinition.<p>
     *
     * @param decorationDefinition the decorationDefinition to set
     */
    public void setDecorationDefinition(CmsDecorationDefintion decorationDefinition) {

        m_decorationDefinition = decorationDefinition;
    }

    /**
     * Sets the decorationKey.<p>
     *
     * @param decorationKey the decorationKey to set
     */
    public void setDecorationKey(String decorationKey) {

        m_decorationKey = decorationKey;
    }

    /**
     * @see java.lang.Object#toString()
     */
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
     * Tests if this is the first occurance of the decoration key.<p>
     * @return true if the decoration key was not used before
     */
    private boolean isFirstOccurance() {

        return m_firstOccurance;
    }

    /**
     * Replaces the macros in the given message.<p>
     * 
     * @param msg the message in which the macros are replaced
     * 
     * @return the message with the macros replaced
     */
    private String replaceMacros(String msg) {

        CmsMacroResolver resolver = CmsMacroResolver.newInstance();
        resolver.addMacro(MACRO_DECORATION, m_decoration);
        if (m_locale != null) {
            resolver.addMacro(MACRO_LOCALE, m_locale.toString());
        }
        return resolver.resolveMacros(msg);
    }

}
