/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.jsp.search.config;

/** Interface the common search configuration must implement. */
public interface I_CmsSearchConfigurationCommon {

    /** Returns the extra params given to Solr.
     * @return The extra params given to Solr - in format "p1=v1&p2=v2".
     */
    String getExtraSolrParams();

    /** Returns the parameter name of the request parameter used to send the last query string.
     * @return The request parameter name used to send the last query string.
     */
    String getLastQueryParam();

    /** Returns the parameter name of the request parameter used to send the current query string.
     * @return The request parameter name used to send the current query string.
     */
    String getQueryParam();

    /** Returns the Solr core that should be used. Can also be <code>null</code>.
     * @return The Solr core to use, or <code>null</code> if none is configured.
     */
    String getSolrCore();

    /** Returns the Solr index that should be used. Can also be <code>null</code>.
     * @return The Solr index to use, or <code>null</code> if none is configured.
     */
    String getSolrIndex();

}
