/*
* File   : $Source $
* Date   : $Date: 2003/07/14 12:50:19 $
* Version: $Revision: 1.4 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.opencms.defaults;

/**
 * Content Definitions that uses the timing,
 * that means the cd can define a timeinterval in which the content is valid.
 * The publicationDate is the begin of the timeinterval and the purgeDate is the end.
 * If this interface is used the method isTimedContent() should return true.
 */
public interface I_CmsTimedContentDefinition {

    /**
     * Returns the date after that the content is valid and can be shown.
     *
     * @return The date as a long value, or 0 if no publication date.
     */
    long getPublicationDate();

    /**
     * Returns the date till that the content is valid and can be shown.
     *
     * @return The date as a long value, or 0 if no purge date.
     */
    long getPurgeDate();

    /**
     * Returns the date when the next other content becomes valid.
     *
     * @return The date as a long value, or 0 if not used.
     */
    long getAdditionalChangeDate();
}