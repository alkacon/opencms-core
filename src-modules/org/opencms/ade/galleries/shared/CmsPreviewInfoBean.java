/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/shared/Attic/CmsPreviewInfoBean.java,v $
 * Date   : $Date: 2010/05/27 09:42:23 $
 * Version: $Revision: 1.2 $
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

package org.opencms.ade.galleries.shared;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This bean contains the preview content for the selected item.<p>
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.2 $ 
 * 
 * @since 8.0.0
 */
public class CmsPreviewInfoBean {

    private Map<String, String> m_imageInfos;

    /** The html content of the preview. */
    private String m_previewHtml;

    private Map<String, String> m_propeties;

    /**
     * The contructor.<p>
     */
    public CmsPreviewInfoBean() {

        m_previewHtml = "";
        m_propeties = new LinkedHashMap<String, String>();
        m_imageInfos = new HashMap<String, String>();
        // implement
    }

    /**
     * Returns the imageInfos.<p>
     *
     * @return the imageInfos
     */
    public Map<String, String> getImageInfos() {

        return m_imageInfos;
    }

    /**
     * Returns the previewHtml.<p>
     *
     * @return the previewHtml
     */
    public String getPreviewHtml() {

        return m_previewHtml;
    }

    /**
     * Returns the propeties.<p>
     *
     * @return the propeties
     */
    public Map<String, String> getPropeties() {

        return m_propeties;
    }

    /**
     * Sets the imageInfos.<p>
     *
     * @param imageInfos the imageInfos to set
     */
    public void setImageInfos(Map<String, String> imageInfos) {

        m_imageInfos = imageInfos;
    }

    /**
     * Sets the previewHtml.<p>
     *
     * @param previewHtml the previewHtml to set
     */
    public void setPreviewHtml(String previewHtml) {

        m_previewHtml = previewHtml;
    }

    /**
     * Sets the propeties.<p>
     *
     * @param propeties the propeties to set
     */
    public void setPropeties(Map<String, String> propeties) {

        m_propeties = propeties;
    }
}