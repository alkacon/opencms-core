/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/setup/Attic/CmsSetupTestResult.java,v $
 * Date   : $Date: 2005/02/17 12:44:35 $
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
 
package org.opencms.setup;

import java.io.Serializable;

/**
 * Contains info about the result of a setup test.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.4 $ $Date: 2005/02/17 12:44:35 $
 * @since 5.3
 */
public class CmsSetupTestResult extends Object implements Serializable, Cloneable {

    /** The clear text name of the test.<p> */
    private String m_name;
    
    /** A info string for the test result.<p> */
    private String m_info;
    
    /** A string describing the result of the test.<p> */
    private String m_result;
    
    /** A string offering some help in case a test failed.<p> */
    private String m_help;
    
    private boolean m_red;
    private boolean m_yellow;
    private boolean m_green;    

    /**
     * Creates a new setup test result.<p>
     */
    public CmsSetupTestResult() {
        super();
        
        setGreen();
        setName("");
        setInfo("");
        setResult("");
        setHelp("");
    }

    /**
     * Returns the description of the test, e.g. "Test xy failed due to...".<p>
     * 
     * @return the description of the test
     */
    public String getInfo() {
        return m_info;
    }

    /**
     * Returns the name of the test, e.g. "Operating system test".<p>
     * 
     * @return the name of the test
     */
    public String getName() {
        return m_name;
    }

    /**
     * Returns the result of the test, e.g. "Detected Apache Tomcat/4.1.24...".<p>
     * 
     * @return the result of the test
     */
    public String getResult() {
        return m_result;
    }

    /**
     * Sets the description of the test, e.g. "Test xy failed due to...".<p>
     * 
     * @param info the description of the test 
     */
    protected void setInfo(String info) {
        m_info = info;
    }

    /**
     * Sets the name of the test, e.g. "Operating system test".<p>
     * 
     * @param name the name of the test
     */
    protected void setName(String name) {
        m_name = name;
    }

    /**
     * Sets the result of the test, e.g. "Detected Apache Tomcat/4.1.24...".<p>
     * 
     * @param result the result of the test
     */
    protected void setResult(String result) {
        this.m_result = result;
    }

    /**
     * Returns true, if the conditions the test were fulfilled.<p>
     * 
     * @return true, if the conditions the test were fulfilled
     */
    public boolean isGreen() {
        return m_green;
    }

    /**
     * Returns true if the test found a violated condition.
     * It is assumed that it will be impossible to run OpenCms.<p>
     * 
     * @return true if the test found a violated a condition
     */
    public boolean isRed() {
        return m_red;
    }

    /**
     * Returns true if the test found a questionable condition.
     * It is possible that OpenCms will not run.<p>
     * 
     * @return true if the test found a questionable condition
     */
    public boolean isYellow() {
        return m_yellow;
    }

    /**
     * Sets if the conditions in the test were fulfilled.<p>
     */
    protected void setGreen() {
        m_green = true;
        m_red = false;
        m_yellow = false;
    }

    /**
     * Sets if the test found a violated condition.<p>
     */
    protected void setRed() {
        m_green = false;
        m_red = true;
        m_yellow = false;
    }

    /**
     * Sets if the test found a questionable condition.<p>
     */
    protected void setYellow() {
        m_green = false;
        m_red = false;
        m_yellow = true;
    }

    /**
     * Returns the help string what to do if a test failed.<p>
     * This string will be displayed in a help bubble.<p>
     * 
     * @return the help string what to do if a test failed
     */
    public String getHelp() {
        return m_help;
    }

    /**
     * Sets the help string what to do if a test failed.<p>
     * This string will be displayed in a help bubble.<p>
     * 
     * @param help the help string what to do if a test failed
     */
    protected void setHelp(String help) {
        m_help = help;
    }

}
