/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/containerpage/Attic/I_CmsContainerBean.java,v $
 * Date   : $Date: 2009/10/13 11:59:41 $
 * Version: $Revision: 1.1.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2009 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.xml.containerpage;

import java.util.List;

/**
 * Describes the API to access a single container in a container page.<p>
 *
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 7.9.2 
 */
public interface I_CmsContainerBean {

    /**
     * Returns the elements.<p>
     *
     * @return the elements
     */
    List<I_CmsContainerElementBean> getElements();

    /**
     * Returns the maximal number of elements in the container.<p>
     *
     * @return the maximal number of elements in the container
     */
    int getMaxElements();

    /**
     * Returns the name.<p>
     *
     * @return the name
     */
    String getName();

    /**
     * Returns the type.<p>
     *
     * @return the type
     */
    String getType();

}