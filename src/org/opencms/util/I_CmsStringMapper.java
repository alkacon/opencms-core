/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/util/Attic/I_CmsStringMapper.java,v $
 * Date   : $Date: 2005/01/20 08:49:36 $
 * Version: $Revision: 1.5 $
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

package org.opencms.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Used to resolve macro names of the form <code>${name}</code>.<p>
 * 
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.5 $
 * @since 6.0 alpha 3
 * @see org.opencms.util.CmsStringUtil#substituteMacros(String, I_CmsStringMapper)
 * @see org.opencms.util.CmsStringUtil#substituteMacros(String, I_CmsStringMapper, boolean)
 */
public interface I_CmsStringMapper {

    /** Identifier for "magic" parameter names. */
    String C_KEY_OPENCMS = "opencms:";

    /** The prefix indicating that the key represents a page context object. */
    String C_KEY_PAGE_CONTEXT = "pageContext.";

    /** The prefix indicating that the key represents a Cms property to be read on the current request URI. */
    String C_KEY_PROPERTY = "property.";

    /** The prefix indicating that the key represents a Cms property to be read on the current element. */
    String C_KEY_PROPERTY_ELEMENT = "elementProperty.";

    /** The prefix indicating that the key represents a Http request parameter. */
    String C_KEY_REQUEST_PARAM = "param.";

    /** Identified for "magic" parameter commands. */
    String[] C_VALUE_NAMES_ARRAY_OPENCMS = {"uri", "filename", "folder"};

    /** The "magic" commands wrapped in a List. */
    List C_VALUE_NAMES_OPENCMS = Collections.unmodifiableList(Arrays.asList(C_VALUE_NAMES_ARRAY_OPENCMS));

    /** Key used to specify the current time as macro value. */
    String KEY_CURRENT_TIME = "currenttime";

    /** Key used to specify the city of the current user as macro value. */
    String KEY_CURRENT_USER_CITY = "currentuser:city";

    /** Key used to specify the email address of the current user as macro value. */
    String KEY_CURRENT_USER_EMAIL = "currentuser:email";

    /** Key used to specify the first name of the current user as macro value. */
    String KEY_CURRENT_USER_FIRSTNAME = "currentuser:firstname";

    /** Key used to specify the full name of the current user as macro value. */
    String KEY_CURRENT_USER_FULLNAME = "currentuser:fullname";

    /** Key used to specify the last name of the current user as macro value. */
    String KEY_CURRENT_USER_LASTNAME = "currentuser:lastname";

    /** Key used to specify the username of the current user as macro value. */
    String KEY_CURRENT_USER_NAME = "currentuser:name";

    /** Key used to specify the street of the current user as macro value. */
    String KEY_CURRENT_USER_STREET = "currentuser:street";

    /** Key used to specify the zip code of the current user as macro value. */
    String KEY_CURRENT_USER_ZIP = "currentuser:zip";

    /** Key prefix used to specify the value of a localized key as macro value. */
    String KEY_LOCALIZED_PREFIX = "key:";

    /** Key used to specify the request encoding as macro value. */
    String KEY_REQUEST_ENCODING = "request:encoding";

    /** Key used to specify the folder of the request uri as macro value. */
    String KEY_REQUEST_FOLDER = "request:folder";

    /** Key user to specify the request locale as macro value. */
    String KEY_REQUEST_LOCALE = "request:locale";

    /** Key used to specify the request uri as macro value. */
    String KEY_REQUEST_URI = "request:uri";

    /** Key used to specify the validation path as macro value. */
    String KEY_VALIDATION_PATH = "validation:path";

    /** Key used to specify the validation regex as macro value. */
    String KEY_VALIDATION_REGEX = "validation:regex";

    /** Key used to specifiy the validation value as macro value. */
    String KEY_VALIDATION_VALUE = "validation:value";

    /**
     * Maps a key (macro name) to a string value.<p>
     * 
     * Implementations must return null if the specified key cannot be mapped to a string.<p>
     * 
     * @param key the key to map
     * 
     * @return the mapped value or <code>null</code>
     */
    String getValue(String key);
    
}