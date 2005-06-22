/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/monitor/CmsMemoryMonitorConfiguration.java,v $
 * Date   : $Date: 2005/06/22 14:19:40 $
 * Version: $Revision: 1.4 $
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

package org.opencms.monitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Memory Monitor configuration class.<p>
 * 
 * @author Armen Markarian 
 * 
 * @version $Revision: 1.4 $ 
 * 
 * @since 6.0.0 
 */
public class CmsMemoryMonitorConfiguration {

    /** The interval to use for sending emails. */
    private int m_emailInterval;

    /** Receivers for status emails. */
    private List m_emailReceiver;

    /** Sender for status emails. */
    private String m_emailSender;

    /** The interval to use for the logging. */
    private int m_logInterval;

    /** Memory limit that triggers a warning. */
    private int m_maxUsagePercent;

    /** The interval to use for warnings if status is disabled. */
    private int m_warningInterval;

    /**
     * Constructor with default values.<p>
     */
    public CmsMemoryMonitorConfiguration() {

        m_emailReceiver = new ArrayList();
    }

    /**
     * Sets the emailReceiver.<p>
     *
     * @param emailReceiver the emailReceiver to set
     */
    public void addEmailReceiver(String emailReceiver) {

        m_emailReceiver.add(emailReceiver);
    }

    /**
     * Returns the intervalEmail.<p>
     *
     * @return the intervalEmail
     */
    public int getEmailInterval() {

        return m_emailInterval;
    }

    /**
     * Returns a List of receiver.<p>
     *
     * @return a List of receiver
     */
    public List getEmailReceiver() {

        Collections.sort(m_emailReceiver);
        return m_emailReceiver;
    }

    /**
     * Returns the emailSender.<p>
     *
     * @return the emailSender
     */
    public String getEmailSender() {

        return m_emailSender;
    }

    /**
     * Returns the intervalLog.<p>
     *
     * @return the intervalLog
     */
    public int getLogInterval() {

        return m_logInterval;
    }

    /**
     * Returns the maxUsagePercent.<p>
     *
     * @return the maxUsagePercent
     */
    public int getMaxUsagePercent() {

        return m_maxUsagePercent;
    }

    /**
     * Returns the intervalWarning.<p>
     *
     * @return the intervalWarning
     */
    public int getWarningInterval() {

        return m_warningInterval;
    }

    /**
     * Initializes the configuration with the required parameters.<p>
     * 
     * @param maxUsagePercent the max usage percent value
     * @param logInterval the interval to log
     * @param emailInterval the interval to send email
     * @param warningInterval the interval to warn
     */
    public void initialize(String maxUsagePercent, String logInterval, String emailInterval, String warningInterval) {

        m_maxUsagePercent = Integer.parseInt(maxUsagePercent);
        m_logInterval = Integer.parseInt(logInterval);
        m_emailInterval = Integer.parseInt(emailInterval);
        m_warningInterval = Integer.parseInt(warningInterval);
    }

    /**
     * Sets the emailSender.<p>
     *
     * @param emailSender the emailSender to set
     */
    public void setEmailSender(String emailSender) {

        m_emailSender = emailSender;
    }
}
