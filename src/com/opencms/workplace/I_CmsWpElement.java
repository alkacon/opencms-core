package com.opencms.workplace;

import org.w3c.dom.*;
import org.xml.sax.*;

import com.opencms.file.*;
import com.opencms.core.*;

import java.util.*;

public interface I_CmsWpElement {
    public Object handleSpecialWorkplaceTag(A_CmsObject cms, Element n, Hashtable parameters) throws CmsException;    
}
