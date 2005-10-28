<%@ page import="
    org.opencms.main.*,
	org.opencms.workplace.explorer.*,
	org.opencms.jsp.*"
%><%
	CmsExplorerInit wp = new CmsExplorerInit(new CmsJspActionElement(pageContext, request, response));
	
%>function vars_resources() { 

this.stati = new Array(
	"<%= wp.key("explorer.state0") %>",
	"<%= wp.key("explorer.state1") %>",
	"<%= wp.key("explorer.state2") %>",
	"<%= wp.key("explorer.state3") %>"
);
this.descr = new Array(
	"<%= wp.key("input.name") %>",    
	"<%= wp.key("input.title") %>",    
	"<%= wp.key("input.type") %>",    
	"<%= wp.key("input.size") %>",    
	"<%= wp.key("input.permissions") %>", 	
	"<%= wp.key("input.datelastmodified") %>",
	"<%= wp.key("input.userlastmodified") %>",  
	"<%= wp.key("input.datecreated") %>",
	"<%= wp.key("input.usercreated") %>",  
	"<%= wp.key("input.datereleased") %>",  
	"<%= wp.key("input.dateexpired") %>", 
	"<%= wp.key("input.state") %>",
	"<%= wp.key("input.lockedby") %>"
);	    

this.actProject;        
this.onlineProject;
this.actDirectory;
this.actDirId;

this.userName="<%= wp.getSettings().getUser().getName() %>";
this.servpath="<%= OpenCms.getSystemInfo().getOpenCmsContext() %>";
this.serverName="<%= request.getServerName() %>";

this.lockedBy="<%=    wp.key("title.locked") %>";
this.titleString="<%= wp.key("label.wptitle") %>";
this.langback="<%=    wp.key("button.back") %>";
this.langsearch="<%=  wp.key("button.search") %>";
this.langup="<%=      wp.key("button.parent") %>";
this.langnew="<%=     wp.key("button.new") %>";
this.langupload="<%=  wp.key("button.upload") %>";
this.langadress="<%=  wp.key("input.adress") %>";
this.langpage="<%=    wp.key("input.page") %>";
this.langloading="<%= wp.key("label.loading") %>";
this.altlockedby="<%= wp.key("explorer.lockedby") %>";
this.altlockedin="<%= wp.key("explorer.lockedin") %>";
this.altbelongto="<%= wp.key("explorer.altbelongto") %>";

this.viewcfg=<%= wp.getExplorerSettings() %>;      

} // vars_resources()

function initialize_resources() {

vi.iconPath="<%= wp.getResourceUri() %>";
vi.skinPath="<%= wp.getSkinUri() %>";
vi.stylePath="<%= wp.getStyleUri(wp.getJsp(),"workplace.css") %>";
<%= wp.buildContextMenues() %>

} // initialize_resources()

vr = new vars_resources();
vi = new vars_index();

initialize_resources();
initHist();