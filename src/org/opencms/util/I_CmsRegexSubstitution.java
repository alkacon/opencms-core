/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.util;

import java.util.regex.Matcher;

/**
 * Interface for generating a substitution for a pattern in a string.<p>
 *
 * @since 8.0.0
 */
public interface I_CmsRegexSubstitution {

    /**
     * Should return the substitution for the current match of the matcher.<p>
     *
     * @param string the base string in which the match occurred
     *
     * @param matcher the matcher which is currently being used
     *
     * @return the substitution which should be used for the current match of the matcher
     */
    String substituteMatch(String string, Matcher matcher);
}
