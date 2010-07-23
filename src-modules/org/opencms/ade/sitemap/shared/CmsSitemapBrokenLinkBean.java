/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/shared/Attic/CmsSitemapBrokenLinkBean.java,v $
 * Date   : $Date: 2010/07/23 11:38:26 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.sitemap.shared;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A bean which represents either the source or the target of a broken link.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsSitemapBrokenLinkBean implements IsSerializable {

    /** The child beans (usually represent link targets). */
    private List<CmsSitemapBrokenLinkBean> m_children = new ArrayList<CmsSitemapBrokenLinkBean>();

    /** The title. */
    private String m_subtitle;

    /** The subtitle. */
    private String m_title;

    /**
     * Constructor.<p>
     * @param title the title 
     * @param subtitle the subtitle 
     */
    public CmsSitemapBrokenLinkBean(String title, String subtitle) {

        m_title = title;
        m_subtitle = subtitle;
    }

    /**
     * Hidden default constructor.<p>
     */
    protected CmsSitemapBrokenLinkBean() {

        // do nothing
    }

    /**
     * Adds a child bean to this bean.<p>
     * 
     * The child usually represents a link target.<p>
     * 
     * @param bean the bean to add as a sub-bean 
     */
    public void addChild(CmsSitemapBrokenLinkBean bean) {

        getChildren().add(bean);
    }

    /**
     * Returns the child beans of this bean.<p>
     * 
     * @return the list of child beans 
     */
    public List<CmsSitemapBrokenLinkBean> getChildren() {

        return m_children;
    }

    /**
     * Returns the sub-title of the bean.<p>
     * 
     * @return the sub-title 
     */
    public String getSubTitle() {

        return m_subtitle;

    }

    /**
     * Returns the title of the bean.<p>
     * 
     * @return the title of the bean
     */
    public String getTitle() {

        return m_title;
    }

}
