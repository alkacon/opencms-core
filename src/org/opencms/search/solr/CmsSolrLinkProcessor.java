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
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;

import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;

/**
 * This Solr post processor generates for each found document the corresponding link and
 * adds this link into the resulting document as field.<p>
 */
public class CmsSolrLinkProcessor implements I_CmsSolrPostSearchProcessor {

    public SolrDocument process(CmsObject cms, CmsResource resource, SolrInputDocument document) {

        // TODO: Should be removed as soon as the subtitueLink method returns the the correct detail page for a
        // given root path, if a explicit detail page is configured
        // @see lighthouse ticket: #559
        CmsObject linkCms = cms;
        String subSiteRoot = OpenCms.getADEManager().getSubSiteRoot(cms, resource.getRootPath());
        if (!cms.getRequestContext().getUri().startsWith(subSiteRoot)) {
            try {
                linkCms = OpenCms.initCmsObject(cms);
                CmsSite site = OpenCms.getSiteManager().getSiteForRootPath(subSiteRoot);
                if (site != null) {
                    if (site.getSiteRoot() != linkCms.getRequestContext().getSiteRoot()) {
                        linkCms.getRequestContext().setSiteRoot(site.getSiteRoot());
                    }
                }
                linkCms.getRequestContext().setUri(linkCms.getRequestContext().removeSiteRoot(subSiteRoot));
            } catch (CmsException e) {
                // noop
            }
        }
        document.addField("link", OpenCms.getLinkManager().substituteLink(linkCms, resource));
        return ClientUtils.toSolrDocument(document);
    }
}
