/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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

package org.opencms.ui.login;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.mail.CmsHtmlMail;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.scheduler.I_CmsScheduledJob;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;

import org.antlr.stringtemplate.StringTemplate;

/**
 * Scheduled job for locking user  accounts which have not been logged into for longer than the configured time.<p>
 */
public class CmsLockInactiveAccountsJob implements I_CmsScheduledJob {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsLockInactiveAccountsJob.class);

    /**
     * @see org.opencms.scheduler.I_CmsScheduledJob#launch(org.opencms.file.CmsObject, java.util.Map)
     */
    public String launch(CmsObject cms, Map<String, String> parameters) throws Exception {

        List<CmsUser> users = OpenCms.getOrgUnitManager().getUsersWithoutAdditionalInfo(cms, "", true);
        List<String> lockedUsers = new ArrayList<String>();
        Locale locale = CmsLocaleManager.getDefaultLocale();

        boolean testOnly = Boolean.parseBoolean(parameters.get("test"));
        for (CmsUser user : users) {
            try {
                if (OpenCms.getLoginManager().canLockBecauseOfInactivity(cms, user)) {
                    if (OpenCms.getLoginManager().checkInactive(user)) {
                        user = cms.readUser(user.getId());
                        if (user.getAdditionalInfo().get(CmsLoginController.KEY_ACCOUNT_LOCKED) == null) {
                            LOG.info("User is inactive: " + user.getName());
                            if (!testOnly) {
                                user.getAdditionalInfo().put(CmsLoginController.KEY_ACCOUNT_LOCKED, "true");
                                cms.writeUser(user);
                            }
                            lockedUsers.add(user.getDisplayName(cms, locale));
                        }
                    }
                }
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        String mailto = parameters.get("mailto");
        if ((mailto != null) && !lockedUsers.isEmpty()) {
            List<String> mailAddresses = CmsStringUtil.splitAsList(mailto, ",");
            OpenCms.getLocaleManager();

            String header = CmsInactiveUserMessages.getReportHeader(locale);
            String subject = CmsInactiveUserMessages.getReportSubject(locale);
            CmsHtmlMail mail = new CmsHtmlMail();

            mail.setSubject(subject);
            for (String address : mailAddresses) {
                mail.addTo(address.trim());
            }
            String templateText = new String(
                CmsFileUtil.readFully(getClass().getResourceAsStream("locked-users-report.html")),
                "UTF-8");
            StringTemplate template = new StringTemplate(templateText);
            template.setAttribute("header", header);
            template.setAttribute("users", lockedUsers);
            template.toString();
            mail.setHtmlMsg(template.toString());
            try {
                mail.send();
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return "";

    }
}
