package com.opencms.workplace;

/*
 * File   : $File$
 * Date   : $Date: 2000/11/01 14:29:04 $
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


import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;

import java.util.*;
import java.io.*;

/**
 * Thread for create a new project.
 * Creation date: (13.10.00 14:39:20)
 * @author: Hanjo Riege
 */
public class CmsAdminDatabaseExportThread extends Thread implements I_CmsConstants{

	private CmsObject m_cms;
	private String m_fileName;
	private String[] m_exportPaths;
	private boolean m_excludeSystem;
	
/**
 * Insert the method's description here.
 * Creation date: (13.09.00 09:52:24)
 */
public CmsAdminDatabaseExportThread(CmsObject cms, String fileName, String[] exportPaths, boolean excludeSystem) {
	
	m_cms = cms;
	m_exportPaths = exportPaths;
	m_fileName = fileName;
	m_excludeSystem = excludeSystem;
		
	}
	public void run() {

System.err.println("mgm----databaseExport thread started----------------"+m_fileName+" - "+m_excludeSystem+" - "+m_exportPaths.toString());
		I_CmsSession session = m_cms.getRequestContext().getSession(true);
		try{
			// do the export   
			m_cms.exportResources(m_fileName, m_exportPaths, m_excludeSystem);

		}catch (CmsException e) {			
			session.putValue(C_SESSION_THREAD_ERROR, Utils.getStackTrace(e));
			if (A_OpenCms.isLogging()) {
				A_OpenCms.log(A_OpenCms.C_OPENCMS_CRITICAL,e.getMessage());
			}
		}
	}
}
