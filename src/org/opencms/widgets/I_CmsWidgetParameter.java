/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/widgets/I_CmsWidgetParameter.java,v $
 * Date   : $Date: 2005/05/13 15:16:31 $
 * Version: $Revision: 1.1 $
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

package org.opencms.widgets;

import org.opencms.file.CmsObject;

/**
 * Parameter value wrapper used by the OpenCms workplace widgets.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.1 $
 * @since 5.5.0
 */
public interface I_CmsWidgetParameter {

    /**
     * Returns the default value of this parameter.<p>
     * 
     * If no default value has been provided, <code>null</code> is returned.<p>
     * 
     * @param cms an initialized instance of an OpenCms user context
     * 
     * @return the default value of this parameter
     */
    String getDefault(CmsObject cms);

    /**
     * Returns the form id of this parameter.<p>  
     * 
     * @return the form id of this parameter
     */
    String getId();

    /**
     * Returns the index of this widget parameter, 
     * starting with 0.<p>
     * 
     * This is usefull in case there are more then one parameters 
     * with the same name, for example when creating a list of parameters of the same type.<p> 
     * 
     * @return the index of this widget parameter
     */
    int getIndex();

    /**
     * Returns the localized key identificator of this parameter.<p>  
     * 
     * @return the localized key identificator of this parameter
     */
    String getKey();

    /**
     * Returns the maximum occurences of this parameter.<p>
     *
     * @return the maximum occurences of this parameter
     */
    int getMaxOccurs();

    /**
     * Returns the minimum occurences of this parameter.<p>
     *
     * @return the minimum occurences of this parameter
     */
    int getMinOccurs();

    /**
     * Returns the name of this parameter.<p>
     *
     * @return the name of this parameter
     */
    String getName();

    /**
     * Returns the value of this parameter.<p>
     * 
     * @param cms an initialized instance of an OpenCms user context
     * 
     * @return the value of this parameter
     */
    String getStringValue(CmsObject cms);

    /**
     * Returns <code>true</code> if this widgets value contains an error.<p>
     *
     * @return <code>true</code> if this widgets value contains an error
     */
    boolean hasError();

    /**
     * Sets the value of this parameter.<p>  
     * 
     * This method does provide processing of the content based on the
     * users current OpenCms context. This can be used e.g. for link 
     * extraction and replacement in the content.<p>
     * 
     * @param cms an initialized instance of an OpenCms user context
     * @param value the value to set
     */
    void setStringValue(CmsObject cms, String value);
}