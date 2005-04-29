/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/mail/CmsMailSettings.java,v $
 * Date   : $Date: 2005/04/29 15:00:35 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.mail;

import org.opencms.main.CmsLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;

/**
 * Contains the settings for the OpenCms mail service.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @since 5.3
 */
public class CmsMailSettings {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsMailSettings.class);  

    /** The default protocol for sending mail ("smtp"). */
    public static final String C_MAIL_DEFAULT_PROTOCOL = "smtp";

    /** The default mail from address. */
    public static final String C_MAIL_DEFAULT_SENDER = "opencms@unconfigured.com";    
    
    /** The default mail "from" sender address. */
    private String m_mailFromDefault;
    
    /** The list of internal mail hosts. */
    private List m_mailHosts;
    
    /** The default order if no order is given for a host. */
    private int m_orderDefault;
    
    /**
     * Empty constructor, required for configuration.<p> 
     */
    public CmsMailSettings() {
        m_mailFromDefault = C_MAIL_DEFAULT_SENDER;
        m_mailHosts = new ArrayList();
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_EMPTY_CONSTRUCTOR_CALLED_1));
        }          
    }
    
    /**
     * Adds a new mail host to the internal list of mail hosts.<p>
     * 
     * @param hostname the name of the mail host
     * @param order the order in which the host is tried
     * @param protocol the protocol to use (default "smtp")
     * @param username the user name to use for authentication 
     * @param password the password to use for authentication
     */    
    public void addMailHost(String hostname, String order, String protocol, String username, String password) {
        m_orderDefault += 10;
        Integer theOrder;
        try {
            theOrder = Integer.valueOf(order);
            if (theOrder.intValue() > m_orderDefault) {
                m_orderDefault = theOrder.intValue();
            }
        } catch (Throwable t) {
            theOrder = new Integer(m_orderDefault);
        }
        CmsMailHost host = new CmsMailHost(hostname, theOrder, protocol, username, password);
        m_mailHosts.add(host);
        if (CmsLog.LOG.isInfoEnabled()) {
            CmsLog.LOG.info(Messages.get().key(Messages.LOG_ADD_HOST_1, host));
        }           
        Collections.sort(m_mailHosts);
    }
    
    /**
     * Returns the mail from default sender.<p>
     * 
     * @return the mail from default sender
     */
    public String getMailFromDefault() {
        return m_mailFromDefault;
    }
    
    /**
     * Returns an unmodifiable sorted list of all configured mail hosts.<p>
     *  
     * @return an unmodifiable sorted list of all configured mail hosts
     */
    public List getMailHosts() {
        return Collections.unmodifiableList(m_mailHosts);
    }
    
    /**
     * Returns the default mail host.<p>
     * 
     * @return the default mail host
     */
    public CmsMailHost getDefaultMailHost() {

        return (CmsMailHost)m_mailHosts.get(0);
    }
   
    /**
     * Sets the mail from default sender.<p>
     * 
     * @param sender the mail from default sender to set
     */
    public void setMailFromDefault(String sender) {
        m_mailFromDefault = sender;
        if (CmsLog.LOG.isInfoEnabled()) {
            CmsLog.LOG.info(Messages.get().key(Messages.LOG_DEFAULT_SENDER_1, m_mailFromDefault));
        }        
    }
}
