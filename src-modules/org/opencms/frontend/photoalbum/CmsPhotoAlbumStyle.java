/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/frontend/photoalbum/CmsPhotoAlbumStyle.java,v $
 * Date   : $Date: 2011/03/23 14:52:28 $
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

package org.opencms.frontend.photoalbum;

import org.opencms.util.CmsStringUtil;

/**
 * Stores the CSS style sheet class names of the HTML element to use for the photo album output pages.<p>
 * 
 * @author Andreas Zahner 
 * 
 * @version $Revision: 1.7 $ 
 * 
 * @since 6.1.3 
 */
public class CmsPhotoAlbumStyle {

    /** The CSS class name for the detail image description text cell. */
    private String m_classDetailImageDescription;

    /** The CSS class name for the detail image title text cell. */
    private String m_classDetailImageTitle;

    /** The CSS class name for the links on the album pages. */
    private String m_classLink;
    
    /** The CSS class name for the navigation row of the album pages. */
    private String m_classNavigation;

    /** The CSS class name for the page title of the album pages. */
    private String m_classPageTitle;

    /** The CSS class name for the thumbnail overview image title cells. */
    private String m_classThumbImageTitle;

    /** The CSS class name for the thumbnail overview table. */
    private String m_classThumbTable;

    /** The CSS class name for the thumbnail overview text cells to show above and below the tumbs. */
    private String m_classThumbText;

    /**
     * Constructor to get a new instance of the style object.<p>
     */
    public CmsPhotoAlbumStyle() {

        // nothing to do here
    }

    /**
     * Returns the CSS class name for the detail image description text cell.<p>
     * 
     * @return the CSS class name for the detail image description text cell
     */
    public String getClassDetailImageDescription() {

        return checkStyleClass(m_classDetailImageDescription);
    }

    /**
     * Returns the CSS class name for the detail image title text cell.<p>
     * 
     * @return the CSS class name for the detail image title text cell
     */
    public String getClassDetailImageTitle() {

        return checkStyleClass(m_classDetailImageTitle);
    }

    /**
     * Returns the CSS class name for the links on the album pages.<p>
     * 
     * @return the CSS class name for the links on the album pages
     */
    public String getClassLink() {

        return checkStyleClass(m_classLink);
    }

    /**
     * Returns the CSS class name for the navigation row of the album pages.<p>
     * 
     * @return the CSS class name for the navigation row of the album pages
     */
    public String getClassNavigation() {

        return checkStyleClass(m_classNavigation);
    }

    /**
     * Returns the CSS class name for the page title of the album pages.<p>
     * 
     * @return the CSS class name for the page title of the album pages
     */
    public String getClassPageTitle() {

        return checkStyleClass(m_classPageTitle);
    }

    /**
     * Returns the CSS class name for the thumbnail overview image title cells.<p>
     * 
     * @return the CSS class name for the thumbnail overview image title cells
     */
    public String getClassThumbImageTitle() {

        return checkStyleClass(m_classThumbImageTitle);
    }

    /**
     * Returns the CSS class name for the thumbnail overview table.<p>
     * 
     * @return the CSS class name for the thumbnail overview table
     */
    public String getClassThumbTable() {

        return checkStyleClass(m_classThumbTable);
    }

    /**
     * Returns the CSS class name for the thumbnail overview text cells to show above and below the tumbs.<p>
     * 
     * @return the CSS class name for the thumbnail overview text cells to show above and below the tumbs
     */
    public String getClassThumbText() {

        return checkStyleClass(m_classThumbText);
    }

    /**
     * Sets the CSS class name for the detail image description text cell.<p>
     * 
     * @param classDetailImageDescription the CSS class name for the detail image description text cell
     */
    public void setClassDetailImageDescription(String classDetailImageDescription) {

        m_classDetailImageDescription = classDetailImageDescription;
    }

    /**
     * Sets the CSS class name for the detail image title text cell.<p>
     * 
     * @param classDetailImageTitle the CSS class name for the detail image title text cell
     */
    public void setClassDetailImageTitle(String classDetailImageTitle) {

        m_classDetailImageTitle = classDetailImageTitle;
    }

    /**
     * Sets the CSS class name for the links on the album pages.<p>
     * 
     * @param classLink the CSS class name for the links on the album pages
     */
    public void setClassLink(String classLink) {

        m_classLink = classLink;
    }

    /**
     * Sets the CSS class name for the navigation row of the album pages.<p>
     * 
     * @param classNavigation the CSS class name for the navigation row of the album pages
     */
    public void setClassNavigation(String classNavigation) {

        m_classNavigation = classNavigation;
    }

    /**
     * Sets the CSS class name for the page title of the album pages.<p>
     * 
     * @param classPageTitle the CSS class name for the page title of the album pages
     */
    public void setClassPageTitle(String classPageTitle) {

        m_classPageTitle = classPageTitle;
    }

    /**
     * Sets the CSS class name for the thumbnail overview image title cells.<p>
     * 
     * @param classThumbImageTitle the CSS class name for the thumbnail overview image title cells
     */
    public void setClassThumbImageTitle(String classThumbImageTitle) {

        m_classThumbImageTitle = classThumbImageTitle;
    }

    /**
     * Sets the CSS class name for the thumbnail overview table.<p>
     * 
     * @param classThumbTable the CSS class name for the thumbnail overview table
     */
    public void setClassThumbTable(String classThumbTable) {

        m_classThumbTable = classThumbTable;
    }

    /**
     * Sets the CSS class name for the thumbnail overview text cells to show above and below the tumbs.<p>
     * 
     * @param classThumbText the CSS class name for the thumbnail overview text cells to show above and below the tumbs
     */
    public void setClassThumbText(String classThumbText) {

        m_classThumbText = classThumbText;
    }

    /**
     * Checks the value of the specified style class and returns an empty String if the parameter is null.<p>
     * 
     * @param styleClass the value to check
     * @return the style class or an empty String if the parameter is null
     */
    private String checkStyleClass(String styleClass) {

        if (CmsStringUtil.isEmpty(styleClass)) {
            return "";
        }
        StringBuffer result = new StringBuffer(64);
        result.append(" class=\"");
        result.append(styleClass);
        result.append("\"");
        return result.toString();
    }

}
