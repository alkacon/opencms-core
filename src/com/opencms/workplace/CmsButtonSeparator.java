package com.opencms.workplace;

import org.w3c.dom.*;
import org.xml.sax.*;

import com.opencms.core.*;
import com.opencms.template.*;
import com.opencms.file.*;

import java.util.*;

public class CmsButtonSeparator extends A_CmsWpElement implements I_CmsWpElement {    
        
    public Object handleSpecialWorkplaceTag(A_CmsObject cms, Element n, Hashtable parameters, CmsXmlLanguageFile lang) throws CmsException {

        CmsXmlWpButtonsDefFile buttondef = new CmsXmlWpButtonsDefFile(cms, "/system/workplace/templates/ButtonTemplate");       
        String result = buttondef.getButtonSeparator();
        return result; 
    }                      
}
