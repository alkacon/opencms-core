/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/frontend/templateone/CmsTemplateLink.java,v $
 * Date   : $Date: 2005/06/22 10:38:21 $
 * Version: $Revision: 1.4 $
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
package org.opencms.frontend.templateone;


/**
 * Represents a single link to be used to display on the page.<p>
 * 
 * These objects are used for the head link row creation of template one.<p>
 * 
 * @author Andreas Zahner 
 * @version $Revision: 1.4 $
 */
public class CmsTemplateLink implements Comparable {
    
    /** The anchor of the link.<p> */
    private String m_anchor;
    
    /** The order of the link, can be used to sort link lists.<p> */
    private int m_order;
    
    /** The target of the link.<p> */
    private String m_target;
    
    /** The text of the link.<p> */
    private String m_text;
    
    /**
     * Creates a new CmsTemplateLink.<p>
     */
    public CmsTemplateLink() {
        m_target = "";
        m_order = 0;
    }
    
    /**
     * 
     * Creates a new CmsTemplateLink with initialized parameters.<p>
     * 
     * @param anchor the link anchor
     * @param text the text to display for the link
     * @param target the linkt target, can be left empty
     * @param order the order number for sorting link lists
     */
    public CmsTemplateLink(String anchor, String text, String target, int order) {
        m_anchor = anchor;
        if (target != null) {
            m_target = target;    
        } else {
            m_target = "";
        }
        m_text = text;
        m_order = order;
    }
    
    /**
     * Compares this instance to another given object instance of this class to sort a list of links.<p>
     * 
     * Use java.util.Collections.sort(List) to sort a list of link objects by their order number ascending.<p>
     * 
     * @param theObject the other given object instance to compare with
     * @return the comparison result for the objects
     */
    public int compareTo(Object theObject) {
        // sort links by their order number in ascending order
        return new Integer(getOrder()).compareTo(new Integer(((CmsTemplateLink)theObject).getOrder()));    
    }
    
    /**
     * Returns the link anchor.<p>
     * 
     * @return the link anchor
     */
    public String getAnchor() {
        return m_anchor;
    }
    
    /**
     * Returns the order number of the link.<p>
     * 
     * @return the order number of the link
     */
    public int getOrder() {
        return m_order;    
    }
    
    /**
     * Returns the target of the link.<p>
     * 
     * @return the target of the link
     */
    public String getTarget() {
        return m_target;
    }
    
    /**
     * Returns the displayed link text.<p>
     * 
     * @return the displayed link text
     */
    public String getText() {
        return m_text;
    }
    
    /**
     * Sets the link anchor.<p>
     * 
     * @param anchor the link anchor
     */
    public void setAnchor(String anchor) {
        m_anchor = anchor;
    }
    
    /**
     * Sets the order number of the link.<p>
     * 
     * @param order the order number of the link
     */
    public void setOrder(int order) {
        m_order = order;
    }
    
    /**
     * Sets the target of the link.<p>
     * 
     * @param target the target of the link
     */
    public void setTarget (String target) {
        if (target != null) {
            m_target = target;    
        } else {
            m_target = "";
        }
    }
    
    /**
     * Sets the displayed link text.<p>
     * 
     * @param text the displayed link text
     */
    public void setText(String text) {
        m_text = text;    
    }

}
