/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/util/I_CmsMacroResolver.java,v $
 * Date   : $Date: 2005/06/22 14:58:54 $
 * Version: $Revision: 1.5 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

/**
 * Describes a macro mapper, which is used to efficiently resolve macros
 * in the form of <code>${key}</code> in an input String.<p>
 * 
 * The macro replacement is pre-implemented in 
 * <code>{@link org.opencms.util.CmsMacroResolver#resolveMacros(String, I_CmsMacroResolver)}</code>.<p>
 * 
 * @author Alexander Kandzior 
 *
 * @version $Revision: 1.5 $ 
 * 
 * @since 6.0.0 
 */
public interface I_CmsMacroResolver {

    /** Delimiter char <code>'$'</code> for a macro. */
    char MACRO_DELIMITER = '$';

    /** End char <code>'}'</code> for a macro. */
    char MACRO_END = '}';

    /** Start char <code>'{'</code> for a macro.  */
    char MACRO_START = '{';

    /**
     * Resolves a single macro to the macro value, returns <code>null</code> if the macro could not be resolved.<p> 
     * 
     * @param macro the macro to resolve
     * @return the resolved macro or <code>null</code> if the macro could not be resolved
     */
    String getMacroValue(String macro);

    /**
     * Returns <code>true</code> if macros that could not be resolved are kept "as is" in the 
     * input String, <code>false</code> if they are replaced by an empty String.<p>
     * 
     * @return <code>true</code> if macros that could not be resolved are kept "as is" in the 
     *      input String, <code>false</code> if they are replaced by an empty String
     */
    boolean isKeepEmptyMacros();

    /**
     * Resolves all macros in the input, replacing them with the macro values.<p> 
     * 
     * The flag {@link #isKeepEmptyMacros()} controls how to deal with  
     * macros found in the input that can not be resolved. 
     * 
     * @param input the input to resolve the macros in
     * 
     * @return the input with all macros resolved
     */
    String resolveMacros(String input);
}