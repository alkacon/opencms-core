/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workflow/Attic/I_CmsWorkflowTransition.java,v $
 * Date   : $Date: 2006/08/19 13:40:50 $
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

package org.opencms.workflow;

/**
 * Identifies a workflow transition.<p>
 * 
 * A workflow should be imanged as a directed graph where the nodes are instances of
 * {@link I_CmsWorkflowState} and the edges are instances of {@link I_CmsWorkflowTransition}.
 * A {@link I_CmsWorkflowType}} describes a complete workflow graph.<p> 
 * 
 * @author Carsten Weinholz
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 7.0.0
 */
public interface I_CmsWorkflowTransition extends I_CmsWorkflowItem {

    /**
     * Returns the id of this workflow transition.<p>
     *  
     * @return the id of this workflow transition
     */
    String getId();
}