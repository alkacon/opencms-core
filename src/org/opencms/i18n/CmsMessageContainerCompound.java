/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/i18n/Attic/CmsMessageContainerCompound.java,v $
 * Date   : $Date: 2005/05/30 15:20:41 $
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

package org.opencms.i18n;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * A <code>CmsMessageContainer</code> implementation that may be used to 
 * collect several instances that may be localized by the same mechanism 
 * (exceptions, loggin) as the superclass. <p>
 * 
 * Additional helper methods support the generation 
 * of localizable formatted output that consists of format literals and localized 
 * messages. <p>
 * 
 * The methods {@link #getKey()}, {@link #getBundle()} and {@link #getArgs()} 
 * and need not to be called as the contained messages may spread over several packages / bundles 
 * with separate arguments. <p>
 * 
 * @author Achim Westermann (a.westermann@alkacon.com)
 * 
 */
public class CmsMessageContainerCompound extends CmsMessageContainer {

    private static final CmsMessageContainer CONTAINER_COLON = new CmsMessageContainer(null, null) {

        /**
         * @return always the String containing newline character (':')
         */
        public String key() {

            return ":";
        }

        /**
         * @param locale ignored
         * @return always the String containing colon character (':')
         */
        public String key(Locale locale) {

            return ":";
        }

    };

    private static final CmsMessageContainer CONTAINER_DOT = new CmsMessageContainer(null, null) {

        /**
         * @return always the String containing dot character
         */
        public String key() {

            return ".";
        }

        /**
         * @param locale ignored
         * @return always the String containing dot character
         */
        public String key(Locale locale) {

            return ".";
        }

    };

    private static final CmsMessageContainer CONTAINER_NEWLINE = new CmsMessageContainer(null, null) {

        /**
         * @return always the String containing newline character
         */
        public String key() {

            return "\n";
        }

        /**
         * @param locale ignored
         * @return always the String containing newline character
         */
        public String key(Locale locale) {

            return "\n";
        }

    };

    private static final CmsMessageContainer CONTAINER_COMMA = new CmsMessageContainer(null, null) {

        /**
         * @return always the String containing newline character
         */
        public String key() {

            return ",";
        }

        /**
         * @param locale ignored
         * @return always the String containing newline character
         */
        public String key(Locale locale) {

            return ",";
        }

    };

    private static final CmsMessageContainer CONTAINER_SPACE = new CmsMessageContainer(null, null) {

        /**
         * @return always the String containing space character
         */
        public String key() {

            return " ";
        }

        /**
         * @param locale ignored
         * @return always the String containing space character
         */
        public String key(Locale locale) {

            return " ";
        }

    };

    /*
     * The <code>List</code> to store messages.
     */
    private List m_messages = new LinkedList();

    /**
     * Creates an empty instance that has to be filled with 
     * <code>CmsMessageContainer</code> instances via the different 
     * <code>add...</code> methods. <p>
     * 
     */
    public CmsMessageContainerCompound() {

        // these members must not be read: inheritance garbage 
        // accepted to be able to replace superclass.
        super(null, null);

    }

    /**
     * Adds a colon character (':') to the current position. It will 
     * be printed when this instance is localized for output after the 
     * message that has been added before. <p>
     *
     * @return the very same instance to allow chaining of invocations comparable to {@link StringBuffer}
     */
    public CmsMessageContainerCompound addColon() {

        m_messages.add(CONTAINER_COLON);
        return this;
    }

    /**
     * Adds a comma character to the current position. It will 
     * be printed when this instance is localized for output after the 
     * message that has been added before. <p>
     *
     * @return the very same instance to allow chaining of invocations comparable to {@link StringBuffer}
     */
    public CmsMessageContainerCompound addComma() {

        m_messages.add(CONTAINER_COMMA);
        return this;
    }

    /**
     * Adds a dot character to the current position. It will 
     * be printed when this instance is localized for output after the 
     * message that has been added before. <p>
     *
     * @return the very same instance to allow chaining of invocations comparable to {@link StringBuffer}
     */
    public CmsMessageContainerCompound addDot() {

        m_messages.add(CONTAINER_DOT);
        return this;
    }

    /**
     * Add the given message to this bunch of <code>CmsMessageContainer</code> instances 
     * only if not contained before.<p> 
     * 
     * @param container The message to add
     * @return the very same instance to allow chaining of invocations comparable to {@link StringBuffer}
     * 
     */
    public CmsMessageContainerCompound add(CmsMessageContainer container) {

        m_messages.add(container);
        return this;
    }

    /**
     * Adds a newline marker to the current position. It will 
     * be printed when this instance is localized for output after the 
     * message that has been added before. <p>
     * 
     * @return the very same instance to allow chaining of invocations comparable to {@link StringBuffer}
     */
    public CmsMessageContainerCompound addNewline() {

        m_messages.add(CONTAINER_NEWLINE);
        return this;
    }

    /**
     * Adds a space character to the current position. It will 
     * be printed when this instance is localized for output after the 
     * message that has been added before. <p>
     * 
     * @return the very same instance to allow chaining of invocations comparable to {@link StringBuffer}
     *
     */
    public CmsMessageContainerCompound addSpace() {

        m_messages.add(CONTAINER_SPACE);
        return this;
    }

    /**
     * Must not be called as the internal member <code>key</code> of this 
     * intance is null always: This is a bunch of <code>CmsMessageContainer</code> 
     * instances that have different keys / args and are related to different bundles (packages).<p>
     * 
     * @return null always
     * 
     * @see org.opencms.i18n.CmsMessageContainer#getArgs()
     */
    public Object[] getArgs() {

        return null;
    }

    /**
     * Must not be called as the internal member <code>key</code> of this 
     * intance is null always: This is a bunch of <code>CmsMessageContainer</code> 
     * instances that have different keys and are related to different bundles (packages).<p>
     * 
     * @return null always
     * 
     * @see org.opencms.i18n.CmsMessageContainer#getBundle()
     */
    public I_CmsMessageBundle getBundle() {

        return null;
    }

    /**
     * Must not be called as the internal member <code>key</code> of this 
     * intance is null always: This is a bunch of <code>CmsMessageContainer</code> 
     * instances that have different keys and are related to different bundles (packages).<p>
     * 
     * @return null always
     * @see org.opencms.i18n.CmsMessageContainer#getKey()
     */
    public String getKey() {

        return null;
    }

    /**
     * @return The concatenation of the <code>key()</code> result of the 
     *         contained <code>CmsMessageContainer</code> instances 
     * @see org.opencms.i18n.CmsMessageContainer#key()
     */
    public String key() {

        Iterator it = this.m_messages.iterator();
        CmsMessageContainer container;
        StringBuffer ret = new StringBuffer();
        while (it.hasNext()) {
            container = (CmsMessageContainer)it.next();
            ret.append(container.key());
        }
        return ret.toString();
    }

    /**
     * @param locale the locale used to localize the output
     * @see org.opencms.i18n.CmsMessageContainer#key(java.util.Locale)
     * @return The concatenation of the <code>key(Locale)</code> result of the 
     *         contained <code>CmsMessageContainer</code> instances 
     * 
     */
    public String key(Locale locale) {

        Iterator it = this.m_messages.iterator();
        CmsMessageContainer container;
        StringBuffer ret = new StringBuffer();
        while (it.hasNext()) {
            container = (CmsMessageContainer)it.next();
            ret.append(container.key(locale));
        }
        return ret.toString();
    }

    /**
     * Returns the the classname with the <code>toString()</code> result of the contained 
     * instances nested in square brackets. <p>
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {

        Iterator it = this.m_messages.iterator();
        StringBuffer ret = new StringBuffer(getClass().getName());
        ret.append('[');
        while (it.hasNext()) {
            ret.append("\n  ");
            ret.append(it.next().toString());
        }
        ret.append("\n]");
        return ret.toString();
    }
}
