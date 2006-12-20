/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/publish/CmsPublishJobBase.java,v $
 * Date   : $Date: 2006/12/20 14:01:20 $
 * Version: $Revision: 1.1.2.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2006 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.publish;

import org.opencms.util.CmsUUID;

import java.util.Locale;

/**
 * Defines a read-only publish job.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1.2.2 $
 * 
 * @since 6.5.5
 */
public class CmsPublishJobBase {

    /** The postfix for the publish report file name. */
    public static final String REPORT_FILENAME_POSTFIX = ".html";
    
    /** The prefix for the publish report file name. */
    public static final String REPORT_FILENAME_PREFIX = "publishReport_";
    
    /** The separator for the publish report file name. */
    public static final String REPORT_FILENAME_SEPARATOR = "_";
    
    /** The delegate publish job. */
    protected CmsPublishJobInfoBean m_publishJob;

    /**
     * Default constructor.<p>
     * 
     * @param publishJob the delegate publish job
     */
    protected CmsPublishJobBase(CmsPublishJobInfoBean publishJob) {

        m_publishJob = publishJob;
    }

    /**
     * Returns the locale for this publish job.<p>
     * 
     * @return the locale for this publish job
     */
    public Locale getLocale() {

        return m_publishJob.getLocale();
    }

    /**
     * Returns the project name or {@link Messages#GUI_DIRECT_PUBLISH_PROJECT_NAME_0}
     * if it is a direct publish job.<p>
     * 
     * @param locale the locale
     * 
     * @return the project name
     */
    public String getProjectName(Locale locale) {

        return m_publishJob.getProjectName(locale);
    }

    /**
     * Returns the publish history id.<p>
     * 
     * @return the publish history id
     */
    public CmsUUID getPublishHistoryId() {
        
        return m_publishJob.getPublishHistoryId();
    }

    /**
     * Returns the number of resources in the publish list.<p>
     * 
     * @return the number of resources in the publish list
     */
    public int getSize() {

        return m_publishJob.getSize();
    }

    /**
     * Returns the name of the user who initialized this publish job.<p>
     * 
     * @return the name of the user who initialized this publish job
     */
    public String getUserName() {

        return m_publishJob.getUser().getName();
    }
    
    /**
     * Returns the direct publish state.<p>
     * 
     * @return the direct publish state
     */
    public boolean isDirectPublish() {

        return m_publishJob.isDirectPublish();
    }
}
