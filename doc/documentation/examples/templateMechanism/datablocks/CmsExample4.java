import com.opencms.template.*;
import com.opencms.core.*; 
import com.opencms.file.*;

public class CmsExample4 extends CmsXmlTemplate {
    public Object getHello(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject)
                         throws CmsException {
        return "Hello World from java!";
    }
}
