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

package org.opencms.jsp;

import org.opencms.flex.CmsFlexController;
import org.opencms.flex.CmsFlexRequest;
import org.opencms.main.CmsLog;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.tagext.TryCatchFinally;

import org.apache.commons.logging.Log;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * This tag is used to dynamically add request parameters which are available during the execution of its body.<p>
 * Request parameters are added with the &lt;cms:param&gt; tag.
 */
public class CmsJspTagAddParams extends TagSupport implements I_CmsJspTagParamParent, TryCatchFinally {

    /**
     * Keeps track of the runtime state of a single execution of this tag.<p>
     */
    public static class ParamState {

        /** The running request. */
        private CmsFlexRequest m_request;

        /** The original set of parameters. */
        private Map<String, String[]> m_savedParams;

        /** The saved set of dynamic parameters. */
        private Set<String> m_savedDynamicParams;

        /**
         * Creates a new instance.<p>
         *
         * @param request the current request
         */
        public ParamState(CmsFlexRequest request) {
            m_request = request;
        }

        /**
         * Adds a request parameter.<p>
         *
         * @param key the parameter name
         * @param value the parameter value
         */
        public void addParameter(String key, String value) {

            m_request.addParameterMap(Collections.singletonMap(key, new String[] {value}));
            m_request.getDynamicParameters().add(key);
        }

        /**
         * Initializes the tag state and sets a new parameter map on the request for which it was instantiated.<p>
         */
        public void init() {

            m_savedParams = m_request.getParameterMap();
            m_savedDynamicParams = m_request.getDynamicParameters();
            Map<String, String[]> params = Maps.newHashMap();
            if (m_savedParams != null) {
                params.putAll(m_savedParams);
            }
            m_request.setParameterMap(params);
            Set<String> dynamicParams = Sets.newHashSet(m_savedDynamicParams);
            m_request.setDynamicParameters(dynamicParams);
        }

        /**
         * Restores the original set of parameters.<p>
         */
        public void undoChanges() {

            m_request.setParameterMap(m_savedParams);
            m_request.setDynamicParameters(m_savedDynamicParams);
        }

    }

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJspTagAddParams.class);

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The current state. */
    private ParamState m_state;

    /**
     * @see org.opencms.jsp.I_CmsJspTagParamParent#addParameter(java.lang.String, java.lang.String)
     */
    public void addParameter(String name, String value) {

        if (m_state != null) {
            m_state.addParameter(name, value);
        }
    }

    /**
     * @see javax.servlet.jsp.tagext.TryCatchFinally#doCatch(java.lang.Throwable)
     */
    public void doCatch(Throwable t) throws Throwable {

        throw t;
    }

    /**
     * @see javax.servlet.jsp.tagext.TryCatchFinally#doFinally()
     */
    public void doFinally() {

        clearState();
    }

    /**
     * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
     */
    @Override
    public int doStartTag() {

        HttpServletRequest request = (HttpServletRequest)(pageContext.getRequest());
        if (CmsFlexController.isCmsRequest(request)) {
            CmsFlexController controller = CmsFlexController.getController(request);
            m_state = new ParamState(controller.getCurrentRequest());
            m_state.init();
        } else {
            LOG.error("Using <cms:addparams> tag outside of a Flex request");
        }
        return EVAL_BODY_INCLUDE;
    }

    /**
     * Clears the tag state if it is set, and restores the previous set of parameters.<p>
     */
    private void clearState() {

        if (m_state != null) {
            ParamState state = m_state;
            m_state = null;
            state.undoChanges();
        }
    }

}
