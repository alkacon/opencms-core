/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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

package org.opencms.jsp.util;

import org.opencms.file.CmsObject;
import org.opencms.jsp.CmsJspTagScaleImage;
import org.opencms.loader.CmsImageScaler;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.staticexport.CmsLinkManager;
import org.opencms.util.CmsStringUtil;

import java.util.Date;

/**
 * Common value wrapper class for XML content values and element setting values.<p>
 */
abstract class A_CmsJspValueWrapper {

    /**
     * Returns the substituted link to the given target.<p>
     *
     * @param cms the cms context
     * @param target the link target
     *
     * @return the substituted link
     */
    protected static String substituteLink(CmsObject cms, String target) {

        if (cms != null) {
            return OpenCms.getLinkManager().substituteLink(
                cms,
                CmsLinkManager.getAbsoluteUri(String.valueOf(target), cms.getRequestContext().getUri()));
        } else {
            return "";
        }
    }

    /**
     * Returns the current cms context.<p>
     *
     * @return the cms context
     */
    public abstract CmsObject getCmsObject();

    /**
     * Returns if the value has been configured.<p>
     *
     * @return <code>true</code> if the value has been configured
     */
    public abstract boolean getExists();

    /**
     * Returns <code>true</code> in case the value is empty, that is either <code>null</code> or an empty String.<p>
     *
     * @return <code>true</code> in case the value is empty
     */
    public abstract boolean getIsEmpty();

    /**
     * Returns <code>true</code> in case the value is empty or whitespace only,
     * that is either <code>null</code> or String that contains only whitespace chars.<p>
     *
     * @return <code>true</code> in case the value is empty or whitespace only
     */
    public abstract boolean getIsEmptyOrWhitespaceOnly();

    /**
     * Returns <code>true</code> in case the value exists and is not empty.<p>
     *
     * @return <code>true</code> in case the value exists and is not empty
     */
    public abstract boolean getIsSet();

    /**
     * Strips all HTML markup from the current string value.<p>
     *
     * @return the given input with all HTML stripped.
     */
    public String getStripHtml() {

        return CmsJspElFunctions.stripHtml(this);
    }

    /**
     * Parses the value to boolean.<p>
     *
     * @return the boolean value
     */
    public boolean getToBoolean() {

        return Boolean.parseBoolean(getToString());
    }

    /**
     * Converts a time stamp to a date.<p>
     *
     * @return the date
     */
    public Date getToDate() {

        return CmsJspElFunctions.convertDate(getToString());
    }

    /**
     * Parses the value to a Double.<p>
     *
     * @return the Double value
     */
    public Double getToFloat() {

        return new Double(Double.parseDouble(getToString()));
    }

    /**
     * Returns the scaled image bean to the current string value.<p>
     *
     * @return the scaled image bean
     */
    public CmsJspScaledImageBean getToImage() {

        try {
            return CmsJspTagScaleImage.imageTagAction(getCmsObject(), getToString(), new CmsImageScaler(), null);
        } catch (CmsException e) {
            // TODO: logging
            return null;
        }
    }

    /**
     * Parses the value to a Long.<p>
     *
     * @return the Long value
     */
    public Long getToInteger() {

        return new Long(Long.parseLong(getToString()));
    }

    /**
     * Returns the substituted link to the current string value.<p>
     *
     * @return the substituted link
     */
    public String getToLink() {

        String target = toString();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(target)) {
            return substituteLink(getCmsObject(), getToString());
        }
        return "";
    }

    /**
     * Returns the string value.<p>
     *
     * @return the string value
     */
    public String getToString() {

        return toString();
    }
}
