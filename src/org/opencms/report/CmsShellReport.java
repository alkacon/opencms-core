/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/report/CmsShellReport.java,v $
 * Date   : $Date: 2008/02/27 12:05:41 $
 * Version: $Revision: 1.27 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2008 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.report;

import java.util.Locale;

/**
 * Report class used for the shell.<p>
 * 
 * It stores nothing. It just prints everything to <code>{@link System#out}</code><p>.
 * 
 * @author Alexander Kandzior
 * @author Jan Baudisch
 * 
 * @version $Revision: 1.27 $ 
 * 
 * @since 6.0.0 
 */
public class CmsShellReport extends CmsPrintStreamReport {

    /**
     * Constructs a new report using the provided locale for the output language.<p>
     *  
     * @param locale the locale to use for the output language
     */
    public CmsShellReport(Locale locale) {

        super(System.out, locale, false);
    }
}