/*
* File   : $Source: /alkacon/cvs/opencms/modules/searchform/src/com/opencms/modules/search/form/Attic/I_CmsSearchEngine.java,v $
* Date   : $Date: 2002/02/19 10:05:49 $
* Version: $Revision: 1.2 $
*
* Copyright (C) 2000  The OpenCms Group
*
* This File is part of OpenCms -
* the Open Source Content Mananagement System
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.com
*
* You should have received a copy of the GNU General Public License
* long with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/

package com.opencms.modules.search.form;

/**
 * Common interface for Searchengine classes.
 * All Searchengine classes have to implement this interface (e.g. CmsHtdig for Ht://Dig).
 *
 * @author    Markus Fabritius
 * @version $Revision: 1.2 $ $Date: 2002/02/19 10:05:49 $
 */
public interface I_CmsSearchEngine {
	/**
	 * Get method for the excerpt.
     *
	 * @return   The excerpt value
	 */
	public String getExcerpt();

	/**
	 * Get method for first number of the current page.
	 *
	 * @return   The firstDisplay value
	 */
	public int getFirstDisplay();

	/**
	 * Get method for last number of the current page.
	 *
	 * @return   The lastDisplay value
	 */
	public int getLastDisplay();

	/**
	 * Get method for the total hit of search.
     *
	 * @return   The match value
	 */
	public int getMatch();

	/**
	 * Get method for last modified Date of the file.
	 *
	 * @return   The modified value
	 */
	public String getModified();

	/**
	 * Get method for the total number of page.
	 *
	 * @return   The pages value
	 */
	public int getPages();

	/**
	 * Get method for the weight of hit in percent.
	 *
	 * @return   The percentMatch value
	 */
	public int getPercentMatch();

	/**
	 * Get method for the current search word.
	 *
	 * @return   The searchWord value
	 */
	public String getSearchWord();

	/**
	 * Get method for the size of the file.
	 *
	 * @return   The size value
	 */
	public int getSize();

	/**
	 * Get method for the title.
	 *
	 * @return   The title value
	 */
	public String getTitle();

	/**
	 * Get method for the Url.
	 *
	 * @return   The url value
	 */
	public String getUrl();
}
