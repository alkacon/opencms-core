/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
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

package org.opencms.search.solr;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;

/**
 * The post document processor can be used in order to
 * manipulate search results after the OpenCms permission
 * check has been done.<p>
 * 
 * NOTE: Currently it is only possible to use this interface 
 * if you run an embedded Solr server instance.<p>
 * 
 * @since 8.5.0
 */
public interface I_CmsSolrPostSearchProcessor {

    /**
     * (Re-)Initializes the post processor.<p> 
     */
    void init();

    /**
     * Performs the post processing.<p>
     * 
     * @param searchCms the CMS object
     * @param resource the resource for the found document
     * @param document the document itself
     * 
     * @return the manipulated Solr document
     */
    SolrDocument process(CmsObject searchCms, CmsResource resource, SolrInputDocument document);
}
