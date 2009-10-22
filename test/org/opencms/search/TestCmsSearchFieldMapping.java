/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/search/TestCmsSearchFieldMapping.java,v $
 * Date   : $Date: 2009/10/22 15:50:05 $
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

package org.opencms.search;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.search.extractors.I_CmsExtractionResult;
import org.opencms.search.fields.CmsSearchFieldMapping;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;

import java.util.List;

/**
 * This test class provides an example how to define the customer mapping of the search content
 * to the self-defined fields. <p>
 * 
 * This class can be used as the class attribute of the mapping node in the field configuration:
 * e.g 
 * <field name="myFilename" store="true" index="untokenized" boost="0.1">
 * <mapping type="dynamic"  class="org.opencms.search.fields.MyCmsSearchFieldMapping">filename</mapping>
 * </field>
 * 
 * @author Polina Smagina 
 * @version $Revision: 1.1 $
 * 
 * @since
 */
public class TestCmsSearchFieldMapping extends CmsSearchFieldMapping {

    /**
     * Returns the String value extracted form the provided data according to the rules of this mapping type.<p> 
     * 
     * @param cms the OpenCms context used for building the search index
     * @param res the resource that is indexed
     * @param extractionResult the plain text extraction result from the resource
     * @param properties the list of all properties directly attached to the resource (not searched)
     * @param propertiesSearched the list of all searched properties of the resource  
     * 
     * @return the String value extracted form the provided data according to the rules of this mapping type
     */
    @Override
    public String getStringValue(
        CmsObject cms,
        CmsResource res,
        I_CmsExtractionResult extractionResult,
        List<CmsProperty> properties,
        List<CmsProperty> propertiesSearched) {

        String content = null;

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(getParam())) {

            if (getParam().equals("extension")) {
                // if myExtension
                // extract the file extension
                content = CmsFileUtil.getExtension(res.getName());
            } else if (getParam().equals("filename")) {
                // if myFilename
                // extract the filename
                content = res.getName();
            }
        }
        if (content == null) {
            // in case the content is not available, use the default value for this mapping
            content = getDefaultValue();
        }
        return content;
    }
}
