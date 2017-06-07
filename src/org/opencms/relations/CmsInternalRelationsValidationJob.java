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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.relations;

import org.opencms.file.CmsObject;
import org.opencms.mail.CmsSimpleMail;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsStringBufferReport;
import org.opencms.report.I_CmsReport;
import org.opencms.scheduler.I_CmsScheduledJob;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.mail.internet.InternetAddress;

/**
 * A schedulable OpenCms job to validate internal relations.<p>
 *
 * This job will validate all link parseable resources of the context project.<p>
 *
 * Parameters used by this job (all optional):
 * <code>email</code> parameter, should be a comma separated list of email addresses,
 *            if empty the email address of the context user will be user instead.<p>
 * <code>from</code> parameter, should be the "from" field of the email to send,
 *            a valid email address, if empty the default will be used.<p>
 * <code>subject</code> parameter, should be the subject of the sent email,
 *            if empty a default subject text will be used,
 *            a good practice is to use the same job name as subject.<p>
 *
 * @since 6.5.0
 *
 * @see org.opencms.relations.I_CmsLinkParseable
 */
public class CmsInternalRelationsValidationJob implements I_CmsScheduledJob {

    /** The email parameter name. */
    public static final String PARAM_EMAIL = "email";

    /** The subject parameter name. */
    public static final String PARAM_SUBJECT = "subject";

    /** The from parameter name. */
    public static final String PARAM_FROM = "from";

    /**
     * @see org.opencms.scheduler.I_CmsScheduledJob#launch(CmsObject, Map)
     */
    public String launch(CmsObject cms, Map<String, String> parameters) throws Exception {

        I_CmsReport report = null;
        String msg = null;
        try {
            report = new CmsStringBufferReport(cms.getRequestContext().getLocale());
            report.println(
                Messages.get().container(
                    Messages.GUI_RELATIONS_VALIDATION_PROJECT_1,
                    cms.getRequestContext().getCurrentProject().getName()),
                I_CmsReport.FORMAT_HEADLINE);
            // TODO: replace by CmsObject#getRelationsForResource
            OpenCms.getPublishManager().validateRelations(cms, null, report);
        } catch (Exception e) {
            if (report != null) {
                report.println(e);
            } else {
                msg = CmsException.getStackTraceAsString(e);
            }
        }

        // parse the parameters
        String from = parameters.get(PARAM_FROM);
        String addresses = parameters.get(PARAM_EMAIL);
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(addresses)) {
            addresses = cms.getRequestContext().getCurrentUser().getEmail();
        }
        List<InternetAddress> to = new ArrayList<InternetAddress>();
        Iterator<String> it = CmsStringUtil.splitAsList(addresses, ',').iterator();
        while (it.hasNext()) {
            to.add(new InternetAddress(it.next()));
        }
        String subject = parameters.get(PARAM_SUBJECT);
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(subject)) {
            subject = Messages.get().getBundle(cms.getRequestContext().getLocale()).key(
                Messages.GUI_RELATIONS_VALIDATION_DEFAULT_SUBJECT_0);
        }
        if (report != null) {
            msg = report.toString();
        }

        // compose the email
        CmsSimpleMail mail = new CmsSimpleMail();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(from)) {
            mail.setFrom(from);
        }
        mail.setTo(to);
        mail.setSubject(subject);
        mail.setMsg(msg);
        mail.send();

        return null;
    }
}