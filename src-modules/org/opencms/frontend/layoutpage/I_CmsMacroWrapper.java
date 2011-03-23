/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/frontend/layoutpage/I_CmsMacroWrapper.java,v $
 * Date   : $Date: 2011/03/23 14:50:00 $
 * Version: $Revision: 1.7 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.frontend.layoutpage;

import org.opencms.file.CmsObject;

/**
 * Wrapper for template engines containing macros that are used to generate HTML output.<p>
 * 
 * Use this class with caution! It might be moved to the OpenCms core packages in the future.<p>
 * 
 * @author Andreas Zahner
 * 
 * @since 6.2.0
 */
public interface I_CmsMacroWrapper {

    /**
     * Returns the file suffix for macro files.<p>
     * 
     * @return the file suffix for macro files
     */
    String getFileSuffix();

    /**
     * Returns the rendered macro.<p>
     * 
     * @param macroName the name of the macro to render
     * @return the rendered macro
     */
    String getResult(String macroName);

    /**
     * Returns the rendered macro using the given context object and the macro parameters.<p>
     * 
     * @param macroName the name of the macro to render
     * @param args the runtime arguments for the macro
     * @return the rendered macro
     */
    String getResult(String macroName, String[] args);

    /**
     * Initializes the template engine with the given macro file.<p>
     * 
     * @param cms the OpenCms user context to use
     * @param macroFile the OpenCms VFS path of the macro template file to use
     * 
     * @throws Exception if the initialization of the macro template engine fails
     */
    void init(CmsObject cms, String macroFile) throws Exception;

    /**
     * Adds a name/value pair to the context usable in the macros.<p>
     * 
     * @param key the name to key the provided value with
     * @param value the corresponding value
     * @return Object that was replaced in the Context if applicable or null if not
     */
    Object putContextVariable(String key, Object value);

    /**
     * Removes the value associated with the specified key from the context.<p>
     * 
     * @param key the name of the value to remove
     * @return the value that the key was mapped to, or null if unmapped
     */
    Object removeContextVariable(String key);
}