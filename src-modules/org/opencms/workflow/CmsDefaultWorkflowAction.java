/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workflow/Attic/CmsDefaultWorkflowAction.java,v $
 * Date   : $Date: 2006/08/19 13:40:38 $
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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Implements the default workflow action class.<p>
 * 
 * @author Carsten Weinholz
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 7.0.0
 */
public class CmsDefaultWorkflowAction implements I_CmsWorkflowAction {

    /** Default workflow action. */
    public static final I_CmsWorkflowAction FINISH = new CmsDefaultWorkflowAction("Finish");

    /** Default workflow action. */
    public static final I_CmsWorkflowAction FORWARD = new CmsDefaultWorkflowAction("Forward");

    /** Name of the uri parameter of the forward action. */
    public static final String FORWARD_URI = "forward_uri";

    /** Default workflow action. */
    public static final I_CmsWorkflowAction NOOP = new CmsDefaultWorkflowAction("Noop");

    /** Default workflow action. */
    public static final I_CmsWorkflowAction PUBLISH = new CmsDefaultWorkflowAction("Publish");

    /** Default workflow action. */
    public static final I_CmsWorkflowAction SIGNAL = new CmsDefaultWorkflowAction("Signal");

    /** Name of the transition parameter of the signal action. */
    public static final String SIGNAL_TRANSITION = "signal_transition";

    /** The name of the workflow state. */
    private String m_name;

    /** The map of additional parameters. */
    private Map m_parameter;

    /**
     * Constructor to initialize the name of the state.<p>
     * 
     * @param name the name of the type
     */
    public CmsDefaultWorkflowAction(String name) {

        m_name = name;
        m_parameter = new HashMap();
    }

    /**
     * Returns a cloned copy of the default action.<p>
     * 
     * @return the cloned copy of the action
     */
    public Object clone() {

        CmsDefaultWorkflowAction clone = new CmsDefaultWorkflowAction(m_name);
        clone.m_parameter.putAll(m_parameter);
        return clone;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object action) {

        if (action instanceof I_CmsWorkflowAction) {
            return getId().equals(((I_CmsWorkflowAction)action).getId());
        }
        return false;
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowAction#getId()
     */
    public String getId() {

        return m_name;
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowAction#getName(java.util.Locale)
     */
    public String getName(Locale locale) {

        return m_name;
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowAction#getParameter(java.lang.String)
     */
    public Object getParameter(String name) {

        return m_parameter.get(name);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {

        return getId().hashCode();
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowAction#setParameter(java.lang.String, java.lang.Object)
     */
    public void setParameter(String name, Object value) {

        m_parameter.put(name, value);
    }
}
