package com.opencms.file;

/**
 * This interface describes THE resource broker. It merges all resource broker into
 * one interface.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.3 $ $Date: 1999/12/10 11:10:23 $
 */
public interface I_CmsResourceBroker 
	extends I_CmsRbMetadefinition, I_CmsRbFile, I_CmsRbUser, I_CmsRbTask
{
}
