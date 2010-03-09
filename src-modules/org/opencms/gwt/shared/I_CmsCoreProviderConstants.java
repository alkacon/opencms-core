/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/shared/Attic/I_CmsCoreProviderConstants.java,v $
 * Date   : $Date: 2010/03/09 10:31:34 $
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

package org.opencms.gwt.shared;

/**
 * Constant interface for {@link org.opencms.gwt.CmsCoreProvider} and {@link org.opencms.gwt.client.util.CmsCoreProvider}.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 8.0.0
 * 
 * @see org.opencms.gwt.CmsCoreProvider
 * @see org.opencms.gwt.client.util.CmsCoreProvider
 */
public interface I_CmsCoreProviderConstants {

    /** Name of the used dictionary. */
    String DICT_NAME = "org.opencms.core";

    /** The OpenCms context, ie. <code>/opencms/opencms</code>. */
    String KEY_CONTEXT = "context";

    /** The current locale, ie. <code>de</code>. */
    String KEY_LOCALE = "locale";

    /** The current site, ie. <code>/sites/default</code>. */
    String KEY_SITE_ROOT = "site";

    /** The current workplace locale, ie. <code>en</code>. */
    String KEY_WP_LOCALE = "wp-locale";
}