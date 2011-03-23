/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/newsletter/CmsNewsletterDistributor.java,v $
 * Date   : $Date: 2011/03/23 14:52:39 $
 * Version: $Revision: 1.7 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.newsletter;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.mail.Email;

/**
 * 
 * A content distributor that sends the extracted xmlcontent data to the recepient via email.
 * <p>
 * 
 * Class to send a newsletter to a number of CmsNewsletterRecipients
 * <p>
 * 
 * @author Jan Baudisch
 * @author Achim Westermann
 * 
 * @version $Revision: 1.7 $
 * 
 * @since 6.0.2
 * 
 */
public class CmsNewsletterDistributor {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsNewsletterDistributor.class);

    /**
     * Sends a {@link I_CmsNewsletter} to a list of {@link I_CmsNewsletterRecipient} objects.<p>
     * 
     * @param recipients a list of CmsNewsletterRecipient objects
     * @param newsletter the newsletter to be distributed
     * @param cms the CmsObject
     */
    public void distribute(CmsObject cms, List recipients, I_CmsNewsletter newsletter) {

        Iterator recipientsIterator = recipients.iterator();
        while (recipientsIterator.hasNext()) {
            I_CmsNewsletterRecipient recipient = (I_CmsNewsletterRecipient)recipientsIterator.next();
            try {
                Email mail = newsletter.getEmail(cms, recipient);
                mail.addTo(recipient.getEmail(), recipient.getFullName());
                mail.send();
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

}
