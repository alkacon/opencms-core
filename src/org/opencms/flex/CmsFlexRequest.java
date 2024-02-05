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

package org.opencms.flex;

import org.opencms.file.CmsObject;
import org.opencms.file.history.CmsHistoryResourceHandler;
import org.opencms.jsp.util.CmsJspStandardContextBean;
import org.opencms.loader.CmsJspLoader;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.staticexport.CmsLinkManager;
import org.opencms.util.CmsCollectionsGenericWrapper;
import org.opencms.util.CmsParameterEscaper;
import org.opencms.util.CmsRequestUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.logging.Log;

import com.google.common.collect.Sets;

/**
 * Wrapper class for a HttpServletRequest.<p>
 *
 * This class wraps the standard HttpServletRequest so that it's output can be delivered to
 * the CmsFlexCache.<p>
 *
 * @since 6.0.0
 */
public class CmsFlexRequest extends HttpServletRequestWrapper {

    /** Request parameter for FlexCache commands. */
    public static final String PARAMETER_FLEX = "_flex";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsFlexRequest.class);

    /** JSP Loader instance. */
    private static CmsJspLoader m_jspLoader;

    /** The max allowed recursive include number.*/
    private static final int MAX_INCLUDE_RECURSION = 7;

    /** Map of attributes from the original request. */
    private Map<String, Object> m_attributes;

    /** Flag to decide if this request can be cached or not. */
    private boolean m_canCache;

    /** The CmsFlexController for this request. */
    private CmsFlexController m_controller;

    /** Flag to force a JSP recompile. */
    private boolean m_doRecompile;

    /** The requested resources element URI in the OpenCms VFS. */
    private String m_elementUri;

    /** The site root of the requested resource. */
    private String m_elementUriSiteRoot;

    /** The parameter escaper. */
    private CmsParameterEscaper m_escaper;

    /** List of all include calls (to prevent an endless inclusion loop). */
    private List<String> m_includeCalls;

    /** Flag to check if this request is in the online project or not. */
    private boolean m_isOnline;

    /** The CmsFlexRequestKey for this request. */
    private CmsFlexRequestKey m_key;

    /** Map of parameters from the original request. */
    private Map<String, String[]> m_parameters;

    /** Stores the request URI after it was once calculated. */
    private String m_requestUri;

    /** Stores the request URL after it was once calculated. */
    private StringBuffer m_requestUrl;

    /** A set of keys of parameters which should be stored in a cached include even if they were not passed as additional parameters to the include call. */
    private Set<String> m_dynamicParameters = Sets.newHashSet();

    /**
     * Creates a new CmsFlexRequest wrapper which is most likely the "Top"
     * request wrapper, i.e. the wrapper that is constructed around the
     * first "real" (not wrapped) request.<p>
     *
     * @param req the request to wrap
     * @param controller the controller to use
     */
    public CmsFlexRequest(HttpServletRequest req, CmsFlexController controller) {

        super(req);
        m_controller = controller;
        CmsObject cms = m_controller.getCmsObject();
        m_elementUri = cms.getSitePath(m_controller.getCmsResource());
        m_elementUriSiteRoot = cms.getRequestContext().getSiteRoot();
        m_includeCalls = new Vector<String>();
        m_parameters = CmsCollectionsGenericWrapper.map(req.getParameterMap());
        m_attributes = CmsRequestUtil.getAtrributeMap(req);
        m_isOnline = cms.getRequestContext().getCurrentProject().isOnlineProject();
        String[] params = req.getParameterValues(PARAMETER_FLEX);
        boolean nocachepara = CmsHistoryResourceHandler.isHistoryRequest(req);
        boolean dorecompile = false;
        if (params != null) {
            if (OpenCms.getRoleManager().hasRole(cms, CmsRole.WORKPLACE_MANAGER)) {
                List<String> paramList = Arrays.asList(params);
                boolean firstCall = controller.isEmptyRequestList();
                nocachepara |= paramList.contains("nocache");
                dorecompile = paramList.contains("recompile");
                boolean p_on = paramList.contains("online");
                boolean p_off = paramList.contains("offline");
                if (paramList.contains("purge") && firstCall) {
                    OpenCms.fireCmsEvent(
                        new CmsEvent(
                            I_CmsEventListener.EVENT_FLEX_PURGE_JSP_REPOSITORY,
                            new HashMap<String, Object>(0)));
                    OpenCms.fireCmsEvent(
                        new CmsEvent(
                            I_CmsEventListener.EVENT_FLEX_CACHE_CLEAR,
                            Collections.<String, Object> singletonMap(
                                "action",
                                Integer.valueOf(CmsFlexCache.CLEAR_ENTRIES))));
                    dorecompile = false;
                } else if ((paramList.contains("clearcache") || dorecompile) && firstCall) {
                    if (!(p_on || p_off)) {
                        OpenCms.fireCmsEvent(
                            new CmsEvent(
                                I_CmsEventListener.EVENT_FLEX_CACHE_CLEAR,
                                Collections.<String, Object> singletonMap(
                                    "action",
                                    Integer.valueOf(CmsFlexCache.CLEAR_ALL))));
                    } else {
                        if (p_on) {
                            OpenCms.fireCmsEvent(
                                new CmsEvent(
                                    I_CmsEventListener.EVENT_FLEX_CACHE_CLEAR,
                                    Collections.<String, Object> singletonMap(
                                        "action",
                                        Integer.valueOf(CmsFlexCache.CLEAR_ONLINE_ALL))));
                        }
                        if (p_off) {
                            OpenCms.fireCmsEvent(
                                new CmsEvent(
                                    I_CmsEventListener.EVENT_FLEX_CACHE_CLEAR,
                                    Collections.<String, Object> singletonMap(
                                        "action",
                                        Integer.valueOf(CmsFlexCache.CLEAR_OFFLINE_ALL))));
                        }
                    }
                } else if (paramList.contains("clearvariations") && firstCall) {
                    if (!(p_on || p_off)) {
                        OpenCms.fireCmsEvent(
                            new CmsEvent(
                                I_CmsEventListener.EVENT_FLEX_CACHE_CLEAR,
                                Collections.<String, Object> singletonMap(
                                    "action",
                                    Integer.valueOf(CmsFlexCache.CLEAR_ENTRIES))));
                    } else {
                        if (p_on) {
                            OpenCms.fireCmsEvent(
                                new CmsEvent(
                                    I_CmsEventListener.EVENT_FLEX_CACHE_CLEAR,
                                    Collections.<String, Object> singletonMap(
                                        "action",
                                        Integer.valueOf(CmsFlexCache.CLEAR_ONLINE_ENTRIES))));
                        }
                        if (p_off) {
                            OpenCms.fireCmsEvent(
                                new CmsEvent(
                                    I_CmsEventListener.EVENT_FLEX_CACHE_CLEAR,
                                    Collections.<String, Object> singletonMap(
                                        "action",
                                        Integer.valueOf(CmsFlexCache.CLEAR_OFFLINE_ENTRIES))));
                        }
                    }
                }
            }
        }
        m_canCache = ((((m_isOnline && m_controller.getCmsCache().isEnabled())
            || (!m_isOnline && m_controller.getCmsCache().cacheOffline())) && !nocachepara) || dorecompile);
        m_doRecompile = dorecompile;
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_FLEXREQUEST_CREATED_NEW_REQUEST_1, m_elementUri));
        }
    }

    /**
     * Constructs a new wrapper layer around an (already wrapped) CmsFlexRequest.<p>
     *
     * @param req the request to be wrapped
     * @param controller the controller to use
     * @param resource the target resource that has been requested
     */
    CmsFlexRequest(HttpServletRequest req, CmsFlexController controller, String resource) {

        super(req);
        m_controller = controller;
        m_elementUri = CmsLinkManager.getAbsoluteUri(resource, m_controller.getCurrentRequest().getElementUri());
        m_elementUriSiteRoot = m_controller.getCurrentRequest().m_elementUriSiteRoot;
        m_isOnline = m_controller.getCurrentRequest().isOnline();
        m_canCache = m_controller.getCurrentRequest().isCacheable();
        m_doRecompile = m_controller.getCurrentRequest().isDoRecompile();
        m_includeCalls = m_controller.getCurrentRequest().getCmsIncludeCalls();
        m_parameters = CmsCollectionsGenericWrapper.map(req.getParameterMap());
        m_attributes = CmsRequestUtil.getAtrributeMap(req);
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_FLEXREQUEST_REUSING_FLEX_REQUEST_1, m_elementUri));
        }
    }

    /**
     * Adds the specified Map to the attributes of the request,
     * added attributes will not overwrite existing attributes in the
     * request.<p>
     *
     * @param map the map to add
     *
     * @return the merged map of attributes
     */
    public Map<String, Object> addAttributeMap(Map<String, Object> map) {

        if (map == null) {
            return m_attributes;
        }
        if ((m_attributes == null) || (m_attributes.size() == 0)) {
            m_attributes = new HashMap<String, Object>(map);
        } else {
            Map<String, Object> attributes = new HashMap<String, Object>();
            attributes.putAll(m_attributes);
            Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Object> entry = it.next();
                String key = entry.getKey();
                // prevent flexcache controller to be overwritten
                if (CmsFlexController.ATTRIBUTE_NAME.equals(key)) {
                    continue;
                } else if (CmsJspStandardContextBean.ATTRIBUTE_NAME.equals(key)) {
                    CmsJspStandardContextBean bean = (CmsJspStandardContextBean)entry.getValue();
                    bean.updateCmsObject(m_controller.getCmsObject());
                    bean.updateRequestData(this);
                }
                attributes.put(key, entry.getValue());
            }
            m_attributes = new HashMap<String, Object>(attributes);
        }

        return m_attributes;
    }

    /**
     * Adds the specified Map to the parameters of the request,
     * added parameters will not overwrite existing parameters in the
     * request.<p>
     *
     * Remember that the value for a parameter name in
     * a HttpRequest is a String array. If a parameter name already
     * exists in the HttpRequest, the values will be added to the existing
     * value array. Multiple occurrences of the same value for one
     * parameter are also possible.<p>
     *
     * @param map the map to add
     *
     * @return the merged map of parameters
     */
    public Map<String, String[]> addParameterMap(Map<String, String[]> map) {

        if (map == null) {
            return m_parameters;
        }
        if ((m_parameters == null) || (m_parameters.size() == 0)) {
            m_parameters = Collections.unmodifiableMap(map);
        } else {
            Map<String, String[]> parameters = new HashMap<String, String[]>();
            parameters.putAll(m_parameters);

            Iterator<Map.Entry<String, String[]>> it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, String[]> entry = it.next();
                String key = entry.getKey();
                // Check if the parameter name (key) exists
                if (parameters.containsKey(key)) {

                    String[] oldValues = parameters.get(key);
                    String[] newValues = entry.getValue();

                    String[] mergeValues = new String[oldValues.length + newValues.length];
                    System.arraycopy(newValues, 0, mergeValues, 0, newValues.length);
                    System.arraycopy(oldValues, 0, mergeValues, newValues.length, oldValues.length);

                    parameters.put(key, mergeValues);
                } else {
                    // No: Add new value array
                    parameters.put(key, entry.getValue());
                }
            }
            m_parameters = Collections.unmodifiableMap(parameters);
        }

        return m_parameters;
    }

    /**
     * Enables escaping for all parameters which are not in the list of exceptions.<p>
     */
    public void enableParameterEscaping() {

        if (m_escaper == null) {
            LOG.info("Enabling parameter escaping for the current flex request");
            m_escaper = new CmsParameterEscaper();
        }
    }

    /**
     * Return the value of the specified request attribute, if any; otherwise,
     * return <code>null</code>.<p>
     *
     * @param name the name of the desired request attribute
     *
     * @return the value of the specified request attribute
     *
     * @see javax.servlet.ServletRequest#getAttribute(java.lang.String)
     */
    @Override
    public Object getAttribute(String name) {

        Object object = m_attributes.get(name);
        if (object == null) {
            object = super.getAttribute(name);
        }
        return object;
    }

    /**
     * Returns a <code>Map</code> of the attributes of this request.<p>
     *
     * @return a <code>Map</code> containing attribute names as keys
     *  and attribute values as map values
     */
    public Map<String, Object> getAttributeMap() {

        return m_attributes;
    }

    /**
     * Return the names of all defined request attributes for this request.<p>
     *
     * @return the names of all defined request attributes for this request
     *
     * @see javax.servlet.ServletRequest#getAttributeNames
     */
    @Override
    public Enumeration<String> getAttributeNames() {

        Vector<String> v = new Vector<String>();
        v.addAll(m_attributes.keySet());
        return v.elements();
    }

    /**
     * Gets the set of dynamic parameters.<p>
     *
     * Normally, when caching a JSP which includes another JSP, only the parameters given directly to the include call will be cached in the new flex cache entry's include list.
     * But when the include call happens implicitly (e.g. for elements rendered in a cms:container tag), we can't pass it any parameters. In this case, we have to modify the parameter
     * map of the current flex request in the JSP, and the keys for the modified parameters need to be stored in this set for the Flex cache to work correctly.
     *
     * @return the set of keys for the dynamic parameters
     */
    public Set<String> getDynamicParameters() {

        return m_dynamicParameters;
    }

    /**
     * Returns the full element URI site root path to the resource currently processed.<p>
     *
     * @return the name of the resource currently processed
     */
    public String getElementRootPath() {

        return m_controller.getCmsObject().getRequestContext().addSiteRoot(m_elementUriSiteRoot, m_elementUri);
    }

    /**
     * Returns the element URI of the resource currently processed,
     * relative to the current site root.<p>
     *
     * This might be the name of an included resource,
     * not necessarily the name the resource requested by the user.<p>
     *
     * @return the name of the resource currently processed
     */
    public String getElementUri() {

        return m_elementUri;
    }

    /**
     * Return the value of the specified request parameter, if any; otherwise,
     * return <code>null</code>.<p>
     *
     * If there is more than one value defined,
     * return only the first one.<p>
     *
     * @param name the name of the desired request parameter
     *
     * @return the value of the specified request parameter
     *
     * @see javax.servlet.ServletRequest#getParameter(java.lang.String)
     */
    @Override
    public String getParameter(String name) {

        String[] values = m_parameters.get(name);
        if (values != null) {
            if (m_escaper != null) {
                return m_escaper.escape(name, values[0]);
            } else {
                return (values[0]);
            }
        } else {
            return (null);
        }
    }

    /**
     * Gets the parameter escaper.<p>
     *
     * @return the parameter escaper
     */
    public CmsParameterEscaper getParameterEscaper() {

        return m_escaper;
    }

    /**
     * Returns a <code>Map</code> of the parameters of this request.<p>
     *
     * Request parameters are extra information sent with the request.
     * For HTTP servlets, parameters are contained in the query string
     * or posted form data.<p>
     *
     * @return a <code>Map</code> containing parameter names as keys
     *  and parameter values as map values
     *
     * @see javax.servlet.ServletRequest#getParameterMap()
     */
    @Override
    public Map<String, String[]> getParameterMap() {

        // NOTE: The parameters in this map are not escaped, so when escaping is enabled,
        // its values may be different from those obtained via getParameter/getParameterValues
        return m_parameters;
    }

    /**
     * Return the names of all defined request parameters for this request.<p>
     *
     * @return the names of all defined request parameters for this request
     *
     * @see javax.servlet.ServletRequest#getParameterNames()
     */
    @Override
    public Enumeration<String> getParameterNames() {

        Vector<String> v = new Vector<String>();
        v.addAll(m_parameters.keySet());
        return (v.elements());
    }

    /**
     * Returns the defined values for the specified request parameter, if any;
     * otherwise, return <code>null</code>.<p>
     *
     * @param name Name of the desired request parameter
     *
     * @return the defined values for the specified request parameter, if any;
     *          <code>null</code> otherwise
     *
     * @see javax.servlet.ServletRequest#getParameterValues(java.lang.String)
     */
    @Override
    public String[] getParameterValues(String name) {

        if (m_escaper != null) {
            return m_escaper.escape(name, m_parameters.get(name));
        } else {
            return m_parameters.get(name);
        }
    }

    /**
     * Allows requests to be dispatched to internal VFS resources or
     * external JSP pages, overloads the standard servlet API <code>getRequestDispatcher()</code> method.<p>
     *
     * @param target the target for the request dispatcher
     *
     * @return a special RequestDispatcher that allows access to VFS resources
     */
    @Override
    public javax.servlet.RequestDispatcher getRequestDispatcher(String target) {

        String absolutUri = CmsLinkManager.getAbsoluteUri(target, m_controller.getCurrentRequest().getElementUri());
        return new CmsFlexRequestDispatcher(
            m_controller.getTopRequest().getRequestDispatcher(absolutUri),
            absolutUri,
            null);
    }

    /**
     * Replacement for the standard servlet API getRequestDispatcher() method.<p>
     *
     * This variation is used if an external file (probably JSP) is dispatched to.
     * This external file must have a "mirror" version, i.e. a file in the OpenCms VFS
     * that represents the external file.<p>
     *
     * @param vfs_target the OpenCms file that is a "mirror" version of the external file
     * @param ext_target the external file (outside the OpenCms VFS)
     *
     * @return the constructed CmsFlexRequestDispatcher
     */
    public CmsFlexRequestDispatcher getRequestDispatcherToExternal(String vfs_target, String ext_target) {

        return new CmsFlexRequestDispatcher(
            m_controller.getTopRequest().getRequestDispatcher(ext_target),
            CmsLinkManager.getAbsoluteUri(vfs_target, m_controller.getCmsObject().getRequestContext().getUri()),
            ext_target);
    }

    /**
     * Wraps the request URI, overloading the standard API.<p>
     *
     * This ensures that any wrapped request will use the "faked"
     * target parameters. Remember that for the real request,
     * a mixture of PathInfo and other request information is used to
     * identify the target.<p>
     *
     * @return a faked URI that will point to the wrapped target in the VFS
     *
     * @see javax.servlet.http.HttpServletRequest#getRequestURI()
     */
    @Override
    public String getRequestURI() {

        if (m_requestUri != null) {
            return m_requestUri;
        }
        StringBuffer buf = new StringBuffer(128);
        buf.append(OpenCms.getSystemInfo().getOpenCmsContext());
        buf.append(getElementUri());
        m_requestUri = buf.toString();
        return m_requestUri;
    }

    /**
     * Wraps the request URL, overloading the standard API,
     * the wrapped URL will always point to the currently included VFS resource.<p>
     *
     * @return a faked URL that will point to the included target in the VFS
     *
     * @see javax.servlet.http.HttpServletRequest#getRequestURL()
     */
    @Override
    public StringBuffer getRequestURL() {

        if (m_requestUrl != null) {
            return m_requestUrl;
        }
        StringBuffer buf = new StringBuffer(128);
        buf.append(getScheme());
        buf.append("://");
        buf.append(getServerName());
        buf.append(":");
        buf.append(getServerPort());
        buf.append(getRequestURI());
        m_requestUrl = buf;
        return m_requestUrl;
    }

    /**
     * This is a work around for servlet containers creating a new application dispatcher
     * instead of using our request dispatcher, so missing RFS JSP pages are not requested to
     * OpenCms and the dispatcher is unable to load the included/forwarded JSP file.<p>
     *
     * @see javax.servlet.http.HttpServletRequestWrapper#getServletPath()
     */
    @Override
    public String getServletPath() {

        // unwrap the request to prevent multiple unneeded attempts to generate missing JSP files
        // m_controller.getTopRequest() does not return the right request here when forwarding
        // this method is generally called exactly once per request on different servlet containers
        // only resin calls it twice
        ServletRequest req = getRequest();
        while (req instanceof CmsFlexRequest) {
            req = ((CmsFlexRequest)req).getRequest();
        }
        String servletPath = null;
        if (req instanceof HttpServletRequest) {
            servletPath = ((HttpServletRequest)req).getServletPath();
        } else {
            servletPath = super.getServletPath();
        }
        // generate missing JSP file
        CmsJspLoader jspLoader = getJspLoader();
        if (jspLoader != null) {
            jspLoader.updateJspFromRequest(servletPath, this);
        }
        return servletPath;
    }

    /**
     * Checks if JSPs should always be recompiled.<p>
     *
     * This is useful in case directive based includes are used
     * with &lt;%@ include file="..." %&gt; on a JSP.
     * Note that this also forces the request not to be cached.<p>
     *
     * @return true if JSPs should be recompiled, false otherwise
     */
    public boolean isDoRecompile() {

        return m_doRecompile;
    }

    /**
     * Indicates that this request belongs to an online project.<p>
     *
     * This is required to distinguish between online and offline
     * resources in the cache. Since the resources have the same name,
     * a suffix [online] or [offline] is added to distinguish the strings
     * when building cache keys.
     * Any resource from a request that isOnline() will be saved with
     * the [online] suffix and vice versa.<p>
     *
     * Resources in the OpenCms workplace are not distinguished between
     * online and offline but have their own suffix [workplace].
     * The assumption is that if you do change the workplace, this is
     * only on true development machines so you can do the cache clearing
     * manually if required.<p>
     *
     * The suffixes are used so that we have a simple String name
     * for the resources in the cache. This makes it easy to
     * use a standard HashMap for storage of the resources.<p>
     *
     * @return true if an online resource was requested, false otherwise
     */
    public boolean isOnline() {

        return m_isOnline;
    }

    /**
     * @see javax.servlet.ServletRequestWrapper#removeAttribute(java.lang.String)
     */
    @Override
    public void removeAttribute(String name) {

        m_attributes.remove(name);
        m_controller.getTopRequest().removeAttribute(name);
    }

    /**
     * @see javax.servlet.ServletRequestWrapper#setAttribute(java.lang.String, java.lang.Object)
     */
    @Override
    public void setAttribute(String name, Object value) {

        m_attributes.put(name, value);
        m_controller.getTopRequest().setAttribute(name, value);
    }

    /**
     * Sets the specified Map as attribute map of the request.<p>
     *
     * The map should be immutable.
     * This will completely replace the attribute map.
     * Use this in combination with {@link #getAttributeMap()} and
     * {@link #addAttributeMap(Map)} in case you want to set the old status
     * of the attribute map after you have modified it for
     * a specific operation.<p>
     *
     * @param map the map to set
     */
    public void setAttributeMap(Map<String, Object> map) {

        m_attributes = new HashMap<String, Object>(map);
    }

    /**
     * Sets the set of dynamic parameters.<p>
     *
     * @param dynamicParams the set of dynamic parameters
     */
    public void setDynamicParameters(Set<String> dynamicParams) {

        if (dynamicParams == null) {
            dynamicParams = Sets.newHashSet();
        }
        m_dynamicParameters = dynamicParams;
    }

    /**
     * Sets the specified Map as parameter map of the request.<p>
     *
     * The map should be immutable.
     * This will completely replace the parameter map.
     * Use this in combination with {@link #getParameterMap()} and
     * {@link #addParameterMap(Map)} in case you want to set the old status
     * of the parameter map after you have modified it for
     * a specific operation.<p>
     *
     * @param map the map to set
     */
    public void setParameterMap(Map<String, String[]> map) {

        m_parameters = map;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        // return the uri of the element requested for this request, useful in debugging
        return m_elementUri;
    }

    /**
     * Returns the List of include calls which will be passed to the next wrapping layer.<p>
     *
     * The set of include calls is maintained to detect
     * an endless inclusion loop.<p>
     *
     * @return the List of include calls
     */
    protected List<String> getCmsIncludeCalls() {

        return m_includeCalls;
    }

    /**
     * Returns the jsp loader instance.<p>
     *
     * @return the jsp loader instance
     */
    protected CmsJspLoader getJspLoader() {

        if (m_jspLoader == null) {
            try {
                m_jspLoader = (CmsJspLoader)OpenCms.getResourceManager().getLoader(CmsJspLoader.RESOURCE_LOADER_ID);
            } catch (ArrayIndexOutOfBoundsException e) {
                // ignore, loader not configured
            }
        }
        return m_jspLoader;
    }

    /**
     * Adds another include call to this wrapper.<p>
     *
     * The set of include calls is maintained to detect
     * an endless inclusion loop.<p>
     *
     * @param target the target name (absolute OpenCms URI) to add
     */
    void addInlucdeCall(String target) {

        m_includeCalls.add(target);
    }

    /**
     * Checks if a given target has been included earlier and exceeds the max allowed recursions.<p>
     *
     * The set of include calls is maintained to detect
     * an endless inclusion loop.<p>
     *
     * @param target the target name (absolute OpenCms URI) to check for
     * @return true if the target is already included, false otherwise
     */
    boolean exceedsCallLimit(String target) {

        if (m_includeCalls.contains(target)) {
            int count = 0;
            for (String call : m_includeCalls) {
                if (call.equals(target)) {
                    count++;
                    if (count > MAX_INCLUDE_RECURSION) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Returns the CmsFlexCacheKey for this request,
     * the key will be calculated if necessary.<p>
     *
     * @return the CmsFlexCacheKey for this request
     */
    CmsFlexRequestKey getCmsCacheKey() {

        // The key for this request is only calculated if actually requested
        if (m_key == null) {
            m_key = new CmsFlexRequestKey(this, m_elementUri, m_isOnline);
        }
        return m_key;
    }

    /**
     * This is needed to decide if this request can be cached or not.<p>
     *
     * Using the request to decide if caching is used or not
     * makes it possible to set caching to false e.g. on a per-user
     * or per-project basis.<p>
     *
     * @return <code>true</code> if the request is cacheable, false otherwise
     */
    boolean isCacheable() {

        return m_canCache;
    }

    /**
     * Removes an include call from this wrapper.<p>
     *
     * The set of include calls is maintained to detect
     * an endless inclusion loop.<p>
     *
     * @param target the target name (absolute OpenCms URI) to remove
     */
    void removeIncludeCall(String target) {

        m_includeCalls.remove(target);
    }
}
