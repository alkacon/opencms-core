package com.opencms.template;

/**
 * Interface for OpenCms dump template classes.
 * <P>
 * All methods extending the functionality of the common
 * template class interface to the special behaviour
 * of dump template classes may be defined here.
 * <P>
 * Primarily, this interface is important for the launcher, 
 * NOT for the template engine.
 * The CmsDumpLauncher can launch all templates that
 * implement the I_CmsDumpTemplate interface. 
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.1 $ $Date: 2000/01/13 17:50:48 $
 */
public interface I_CmsDumpTemplate extends I_CmsTemplate {
}
