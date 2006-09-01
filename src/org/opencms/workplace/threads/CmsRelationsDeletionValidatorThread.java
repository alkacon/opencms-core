/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/threads/Attic/CmsRelationsDeletionValidatorThread.java,v $
 * Date   : $Date: 2006/09/01 10:29:39 $
 * Version: $Revision: 1.1.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.workplace.threads;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.report.A_CmsReportThread;
import org.opencms.report.I_CmsReport;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;

/**
 * A report thread to display all referencing relations to a set of resources.<p>
 * 
 * These relations would be broken if the set of resources would be deleted.<p>
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 6.5.1 
 */
public class CmsRelationsDeletionValidatorThread extends A_CmsReportThread {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsRelationsDeletionValidatorThread.class);

    /** A list of names of resources to be deleted.<p> */
    private List m_resourceNames;

    /** A flag to indicate if the deletion operation will include the siblings. */
    private boolean m_includeSiblings;

    /** A flag to indicate if the deletion of the given resources will break relations. */
    private boolean m_willBreakRelations;
    
    
    /**
     * Returns flag to indicate if the deletion of the given resources will break relations.<p>
     *
     * @return flag to indicate if the deletion of the given resources will break relations,
     *    if <code>null</code> the thread is not ready yet.
     */
    public boolean getWillBreakRelations() {
    
        return m_willBreakRelations;
    }


    /**
     * Creates a thread that validates the referencing relations to the resources in the list.<p>
     * 
     * @param cms the current OpenCms context object
     * @param resourceNames the list of names of resources which will be deleted
     * @param includeSiblings if the deletion operation will include the siblings
     */
    public CmsRelationsDeletionValidatorThread(CmsObject cms, List resourceNames, boolean includeSiblings) {

        super(cms, Messages.get().getBundle().key(
            Messages.GUI_RELATION_DELETION_VALIDATOR_THREAD_NAME_1,
            new Object[] {cms.getRequestContext().currentProject().getName()}));

        m_resourceNames = resourceNames;
        m_includeSiblings = includeSiblings;

        initHtmlReport(cms.getRequestContext().getLocale());
    }


    /**
     * @see org.opencms.report.A_CmsReportThread#getReportUpdate()
     */
    public String getReportUpdate() {

        return getReport().getReportUpdate();
    }
    
    /**
     * @see java.lang.Thread#start()
     */
    public synchronized void start() {
    
        super.start();
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {

        CmsObject cms = getCms();
        List resourceList = m_resourceNames;

        // write header
        getReport().println(Messages.get().container(Messages.RPT_RELATIONS_DELETION_BEGIN_0), I_CmsReport.FORMAT_HEADLINE);
        getReport().println();
        
        // expand the folders to single resources
        getReport().println(Messages.get().container(Messages.RPT_RELATIONS_DELETION_EXPANDFOLDER_BEGIN_0), I_CmsReport.FORMAT_HEADLINE);
        Iterator itResources = new ArrayList(resourceList).iterator();
        while (itResources.hasNext()) {
            String resName = (String)itResources.next();
            getReport().print(Messages.get().container(Messages.RPT_RELATIONS_DELETION_EXPANDFOLDER_1, resName));
            try {
                CmsResource resource = cms.readResource(resName);
                if (resource.isFolder()) {
                    Iterator itChilds = cms.readResources(resName, CmsResourceFilter.IGNORE_EXPIRATION, true).iterator();
                    while (itChilds.hasNext()) {
                        CmsResource child = (CmsResource)itChilds.next();
                        String childPath = cms.getSitePath(child);
                        if (!resourceList.contains(childPath)) {
                            resourceList.add(childPath);
                        }
                    }
                }
                getReport().println(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0), I_CmsReport.FORMAT_OK);
            } catch (CmsException e) {
                getReport().println(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_FAILED_0), I_CmsReport.FORMAT_ERROR);
                // should never happen
                if (LOG.isErrorEnabled()) {
                    LOG.error(e);
                }
            }
        }
        getReport().println(Messages.get().container(Messages.RPT_RELATIONS_DELETION_EXPANDFOLDER_END_0), I_CmsReport.FORMAT_HEADLINE);
        getReport().println();

        if (m_includeSiblings) {
            // expand to siblings
            getReport().println(Messages.get().container(Messages.RPT_RELATIONS_DELETION_EXPANDSIBLINGS_BEGIN_0), I_CmsReport.FORMAT_HEADLINE);
            itResources = new ArrayList(resourceList).iterator();
            while (itResources.hasNext()) {
                String resName = (String)itResources.next();
                getReport().print(Messages.get().container(Messages.RPT_RELATIONS_DELETION_EXPANDSIBLING_1, resName));
                try {
                    CmsResource resource = cms.readResource(resName);
                    if (!resource.isFolder()) {
                        Iterator itSiblings = cms.readSiblings(resName, CmsResourceFilter.IGNORE_EXPIRATION).iterator();
                        while (itSiblings.hasNext()) {
                            CmsResource sibling = (CmsResource)itSiblings.next();
                            String siblingPath = cms.getSitePath(sibling);
                            if (!resourceList.contains(siblingPath)) {
                                resourceList.add(siblingPath);
                            }
                        }
                    }
                    getReport().println(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0), I_CmsReport.FORMAT_OK);
                } catch (CmsException e) {
                    getReport().println(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_FAILED_0), I_CmsReport.FORMAT_ERROR);
                    // should never happen
                    if (LOG.isErrorEnabled()) {
                        LOG.error(e);
                    }
                }
            }
            getReport().println(Messages.get().container(Messages.RPT_RELATIONS_DELETION_EXPANDSIBLINGS_END_0), I_CmsReport.FORMAT_HEADLINE);
            getReport().println();
        }

        // check every resource
        m_willBreakRelations = false;
        getReport().println(Messages.get().container(Messages.RPT_RELATIONS_DELETION_VALIDATION_BEGIN_0), I_CmsReport.FORMAT_HEADLINE);
        itResources = resourceList.iterator();
        while (itResources.hasNext()) {
            String resName = (String)itResources.next();
            getReport().println(Messages.get().container(Messages.RPT_RELATIONS_DELETION_VALIDATION_1, resName));
            try {
                Iterator it = cms.getRelationsForResource(resName, CmsRelationFilter.SOURCES).iterator();
                while (it.hasNext()) {
                    CmsRelation relation = (CmsRelation)it.next();
                    String resourceName = cms.getRequestContext().removeSiteRoot(relation.getSourcePath());
                    // add only if the source is not to be deleted too
                    getReport().print(Messages.get().container(Messages.RPT_RELATIONS_DELETION_REFERENCING_1, resourceName));
                    if (!resourceList.contains(resourceName)) {
                        m_willBreakRelations = true;
                        getReport().println(Messages.get().container(Messages.RPT_RELATIONS_DELETION_BREAK_0), I_CmsReport.FORMAT_ERROR);
                    } else {
                        getReport().println(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0), I_CmsReport.FORMAT_OK);
                    }
                }
            } catch (CmsException e) {
                getReport().println(e);
                // should never happen
                if (LOG.isErrorEnabled()) {
                    LOG.error(e);
                }
            }
        }
        getReport().println(Messages.get().container(Messages.RPT_RELATIONS_DELETION_VALIDATION_END_0), I_CmsReport.FORMAT_HEADLINE);
        getReport().println();

        // write footer
        getReport().println(Messages.get().container(Messages.RPT_RELATIONS_DELETION_END_0), I_CmsReport.FORMAT_HEADLINE);
    }
}
