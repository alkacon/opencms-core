/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/util/Attic/MailUtils.java,v $
* Date   : $Date: 2003/07/21 11:05:04 $
* Version: $Revision: 1.5 $
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


package com.opencms.util;

/**
 * This is a general helper class for applications sending mails
 *
 * @author Stefan Marx (Stefan.Marx@framfab.de)
 */
public final class MailUtils {
    
    /**
     * Hides the public constructor.<p>
     */
    private MailUtils() {
    }

    /**
     * Check a given email address for conformness with
     * RFC822 rules, see http://www.rfc-editor.org/rfc.html.<p>
     * 
     * @param address EMail address to be checked
     * @return <code>true</code> if the address is syntactically correct, <code>false</code> otherwise
    */
    public static boolean checkEmail(String address) {
        boolean result = true;
        try {
            new javax.mail.internet.InternetAddress(address);
        } catch (javax.mail.internet.AddressException e) {
            result = false;
        }
        return result;
    }
}
