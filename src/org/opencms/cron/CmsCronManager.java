/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/cron/Attic/CmsCronManager.java,v $
 * Date   : $Date: 2003/10/29 13:00:42 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.cron;

/**
 * Manages the Cms cron table and it's cron jobs.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.1 $ $Date: 2003/10/29 13:00:42 $
 * @since 5.1.12
 */
public class CmsCronManager {

    /**
     * Default constructor.<p>
     */
    public CmsCronManager() {
    }

    /*
    public void writeCronTab(String cronTab) throws CmsException {
        List cronEntries = (List) new ArrayList();
        StringTokenizer tokens = new StringTokenizer(cronTab, "\r\n");

        while (tokens.hasMoreTokens()) {
            cronEntries.add(new CmsCronEntry(tokens.nextToken().trim()));
        }

        writeCronTab(cronEntries);
    }

    public void writeCronTab(List cronEntries) {
        CmsCronEntry cronEntry = null;
        List cronEntryNodes = (List) new ArrayList();

        try {
            Document document = DocumentHelper.createDocument();
            Element crontab = document.addElement("crontab");

            Iterator i = cronEntries.iterator();
            while (i.hasNext()) {
                cronEntry = (CmsCronEntry) i.next();
                cronEntryNodes.add(cronEntry.toXml());
            }
            
            OpenCms.getRegistry().writeNodes("//system/crontab", cronEntryNodes);
        } catch (DOMException domException) {
            if (org.opencms.main.OpenCms.getLog(this).isErrorEnabled()) {
                org.opencms.main.OpenCms.getLog(this);
            }
        } catch (Exception e) {
            if (org.opencms.main.OpenCms.getLog(this).isErrorEnabled()) {
                org.opencms.main.OpenCms.getLog(this).error("Error clearing crontab", e);
            }
        }
    }
    */

}
