public class CmsExample7 extends CmsXmlTemplate {


public byte[] getContent(CmsObject cms, String templateFile,
String elementName, Hashtable parameters, String templateSelector)
throws CmsException {

    CmsXmlTemplateFile template = getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);
    template.setData("greetings", "Hello from Java Class!");
    return startProcessing(cms, template, elementName, parameters, templateSelector);
    } 
}
