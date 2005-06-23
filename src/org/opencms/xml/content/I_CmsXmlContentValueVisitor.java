/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/content/I_CmsXmlContentValueVisitor.java,v $
 * Date   : $Date: 2005/06/23 08:12:45 $
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

package org.opencms.xml.content;

import org.opencms.xml.types.I_CmsXmlContentValue;

/**
 * Visitor interface that allows looping through all the values in a XML content document.<p> 
 * 
 * An implmentation of this interface can be passed to 
 * {@link org.opencms.xml.content.CmsXmlContent#visitAllValuesWith(I_CmsXmlContentValueVisitor)} in 
 * order to call the {@link #visit(I_CmsXmlContentValue)} method on all values of that document.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.5 $ 
 * 
 * @since 6.0.0 
 */
public interface I_CmsXmlContentValueVisitor {

    /**
     * Visits the given value.<p>
     * 
     * @param value the value to visit
     */
    void visit(I_CmsXmlContentValue value);
}