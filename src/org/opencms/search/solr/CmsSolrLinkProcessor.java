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
import org.opencms.main.OpenCms;

import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;

/**
 * This Solr post processor generates for each found document the corresponding link and
 * adds this link into the resulting document as field.<p>
 */
public class CmsSolrLinkProcessor implements I_CmsSolrPostSearchProcessor {

    /**
     * @see org.opencms.search.solr.I_CmsSolrPostSearchProcessor#process(org.opencms.file.CmsObject, org.opencms.file.CmsResource, org.apache.solr.common.SolrInputDocument)
     */
    public SolrDocument process(CmsObject cms, CmsResource resource, SolrInputDocument document) {

        document.addField("link", OpenCms.getLinkManager().substituteLink(cms, resource));
        return ClientUtils.toSolrDocument(document);
    }

    /**
     * 
     * @see org.opencms.search.solr.I_CmsSolrPostSearchProcessor#init()
     */
    public void init() {

        // No actions necessary 
    }
}
