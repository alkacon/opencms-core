
/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/util/Attic/MailUtils.java,v $
* Date   : $Date: 2001/02/22 15:35:16 $
* Version: $Revision: 1.1 $
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

package com.opencms.util;

//import com.opencms.file.*;
//import com.opencms.core.*;
//import java.util.*;
//import java.io.*;

/**
 * This is a general helper class for applications sending mails
 *
 * @author Stefan Marx <Stefan.Marx@framfab.de>
 */
public class MailUtils {

    /**
     * Check a given email address for conformness with
     * RFC822 rules, see http://www.rfc-editor.org/rfc.html
     * @author Stefan Marx <Stefan.Marx@framfab.de>
     * @param address EMail address to be checked
     * @return <code>true</code> if the address is syntactically correct, <code>false</code> otherwise.
    */
    public static boolean checkEmail(String address) {
        boolean result = true;
        try {
            javax.mail.internet.InternetAddress IPAdd = new javax.mail.internet.InternetAddress(address);
        } catch(javax.mail.internet.AddressException e) {
            result = false;
        }
        return result;
    }
}
