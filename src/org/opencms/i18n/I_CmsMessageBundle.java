/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/i18n/I_CmsMessageBundle.java,v $
 * Date   : $Date: 2005/09/16 08:49:34 $
 * Version: $Revision: 1.5.2.1 $
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
 */

package org.opencms.i18n;

import java.util.Locale;

/**
 * Utility interface for generating localized messages for internal OpenCms operations.<p>
 * 
 * Every OpenCms core packages should provide one class implementing this interface.
 * This class should be called <code>Messages</code> and be a subclass of 
 * <code>{@link org.opencms.i18n.A_CmsMessageBundle}</code>. Moreover, the implementation 
 * should contain a <code>public static I_CmsMessageBundle get()</code> method, that returns 
 * an instance of the class. Also the implementation should contain public static members
 * for all available localized keys.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.5.2.1 $ 
 * 
 * @since 6.0.0 
 */
public interface I_CmsMessageBundle {

    /**
     * Creates a message container for this package with the given arguments.<p>
     * 
     * Convenience method for a message with no arguments.<p>
     * 
     * @param key the message key to use
     * @return a message container for this package with the given arguments
     */
    CmsMessageContainer container(String key);

    /**
     * Creates a message container for this package with the given arguments.<p>
     * 
     * Convenience method for a message with one argument.<p>
     * 
     * @param key the message key to use
     * @param arg0 the message argument
     * @return a message container for this package with the given arguments
     */
    CmsMessageContainer container(String key, Object arg0);

    /**
     * Creates a message container for this package with the given arguments.<p>
     * 
     * Convenience method for a message with two arguments.<p>
     * 
     * @param key the message key to use
     * @param arg0 the first message argument
     * @param arg1 the second message argument
     * @return a message container for this package with the given arguments
     */
    CmsMessageContainer container(String key, Object arg0, Object arg1);

    /**
     * Creates a message container for this package with the given arguments.<p>
     * 
     * Convenience method for a message with three arguments.<p>
     * 
     * @param key the message key to use
     * @param arg0 the first message argument
     * @param arg1 the second message argument
     * @param arg2 the third message argument
     * 
     * @return a message container for this package with the given arguments
     */
    CmsMessageContainer container(String key, Object arg0, Object arg1, Object arg2);

    /**
     * Creates a message container for this package with the given arguments.<p>
     * 
     * @param key the message key to use
     * @param args the message arguments to use
     * @return a message container for this package with the given arguments
     */
    CmsMessageContainer container(String key, Object[] args);

    /** 
     * Returns the localized message bundle wrapped in this instance initialized with the OpenCms default locale.<p> 
     * 
     * @return the localized message bundle wrapped in this instance initialized with the OpenCms default locale
     */
    CmsMessages getBundle();

    /** 
     * Returns the localized message bundle wrapped in this instance initialized with the provided locale.<p> 
     * 
     * @param locale the locale to use
     * @return the localized message bundle wrapped in this instance initialized with the provided locale
     */
    CmsMessages getBundle(Locale locale);

    /**
     * Returns the bundle name for this OpenCms package.<p>
     * 
     * @return the bundle name for this OpenCms package
     */
    String getBundleName();

    /**
     * Returns the selected localized message from this bundle for the OpenCms default locale.<p>
     * 
     * Convenience method for messages without argument.<p>
     * 
     * @param locale the locale to use
     * @param key the message key
     * 
     * @return the selected localized message from this bundle  for the given locale.<p>
     */
    String key(Locale locale, String key);

    /**
     * Returns the selected localized message from this bundle for the given locale.<p>
     * 
     * @param locale the locale to use
     * @param key the message key
     * @param args the message arguments
     * 
     * @return the selected localized message from this bundle for the given locale
     */
    String key(Locale locale, String key, Object[] args);

    /**
     * Returns the selected localized message from this bundle for the OpenCms default locale.<p>
     * 
     * Convenience method for messages without arguments.<p>
     * 
     * @param key the message key
     * 
     * @return the selected localized message from this bundle for the OpenCms default locale
     */
    String key(String key);

    /**
     * Returns the selected localized message from this bundle for the OpenCms default locale.<p>
     * 
     * Convenience method for messages with one argument.<p>
     * 
     * @param key the message key
     * @param arg0 the message argument
     * 
     * @return the selected localized message from this bundle for the OpenCms default locale
     */
    String key(String key, Object arg0);

    /**
     * Returns the selected localized message from this bundle for the OpenCms default locale.<p>
     * 
     * Convenience method for messages with two arguments.<p>
     * 
     * @param key the message key
     * @param arg0 the first message argument
     * @param arg1 the second message argument
     * 
     * @return the selected localized message from this bundle for the OpenCms default locale
     */
    String key(String key, Object arg0, Object arg1);

    /**
     * Returns the selected localized message from this bundle for the OpenCms default locale.<p>
     * 
     * Convenience method for messages with three arguments.<p>
     * 
     * @param key the message key
     * @param arg0 the first message argument
     * @param arg1 the second message argument
     * @param arg2 the third message argument
     * 
     * @return the selected localized message from this bundle for the OpenCms default locale
     */
    String key(String key, Object arg0, Object arg1, Object arg2);

    /**
     * Returns the selected localized message from this bundle for the OpenCms default locale.<p>
     * 
     * @param key the message key
     * @param args the message arguments
     * 
     * @return the selected localized message from this bundle for the OpenCms default locale
     */
    String key(String key, Object[] args);
}