<%@ page import="org.opencms.workplace.commons.*,
                 org.opencms.file.CmsResource,
                 org.opencms.main.*,
                 org.opencms.site.CmsSiteManager"
        buffer="none"
%><%
        
        // initialize the workplace class
        CmsSecure wp = new CmsSecure(pageContext, request, response);
        boolean displayForm = true;

// perform the users submitted action

switch(wp.getAction()) {


case CmsSecure.ACTION_CHSECEXP:

//////////////////// ACTION: main change secure and export action

        wp.actionChangeSecureExport();
break;


case CmsSecure.ACTION_CANCEL:

//////////////////// ACTION: cancel button pressed

        wp.actionCloseDialog();

break;


case CmsSecure.ACTION_DEFAULT:

//////////////////// ACTION: show security and export settings dialog (default)
wp.setParamAction("secure");

%>

<%= wp.htmlStart("help.explorer.contextmenu.secure") %>

<style type="text/css">
  #tablelabel {
    padding-right:20px;
    horizontal-align:middle;
  }

  table {
    border:0;
  }
</style>

<script type="text/javascript">

  // validate that the export/secure and internal property are  not checked at the same time
  function checkNoIntern() {
    if((document.secure.elements['secure'][0].checked || document.secure.elements['export'][0].checked) 
                                                      && document.secure.elements['intern'].checked) {
      alert('<%= wp.key("error.message.secure.notintern") %>');
      document.secure.elements['intern'].checked = false;
    }
  }


  function checkNoSecureNorExport() {
    if(document.secure.elements['intern'].checked && 
      (document.secure.elements['secure'][0].checked || document.secure.elements['export'][0].checked)) {
      alert('<%= wp.key("error.message.secure.noexportsecure") %>');
      document.secure.elements['export'][0].checked = false;
      document.secure.elements['export'][1].checked = true;
      document.secure.elements['secure'][0].checked = false;
      document.secure.elements['secure'][1].checked = true;
    }
  }
</script>


  <%= wp.bodyStart("dialog") %>
  <%= wp.dialogStart() %>

  <form name="secure" class="nomargin" action="<%= wp.getDialogUri() %>" method="post" onsubmit="return submitAction('<%= wp.DIALOG_OK %>', null, 'secure');" id="secure">
  
    <%= wp.paramsAsHidden() %>

    <%= wp.dialogContentStart(wp.getParamTitle()) %>
    
    <input type="hidden" name="<%= wp.PARAM_FRAMENAME %>" value=""> 
    
    <%@ include file="includes/resourceinfo.txt" %>

    <%= wp.dialogBlockStart(wp.key("label.address")) %>
    <%= wp.getResourceUrl() %>
    <%= wp.dialogBlockEnd() %>
    <%= wp.dialogSpacer() %>

    <%= wp.dialogBlockStart(wp.key("label.securityinternalsettings")) %>
    <table>

      <tr>
      
      <% 
      // display secure dialog, if there exists a secure server for the current site
      if(CmsSiteManager.getCurrentSite(wp.getCms()).hasSecureServer()) { 
      %>

        <td width="100pt"><%= wp.key("input.secure") %></td>
        <td>
          <%= wp.buildRadio(I_CmsConstants.C_PROPERTY_SECURE) %>
        </td>

      <% } else { %>
        <td colspan="2">

          <%= wp.key("message.nosecureserver") %>

        </td>
      <% } %>
      </tr>

      <tr>
        <td width="100pt"><%= wp.key("input.intern") %></td>
        <td style="padding-left:10px">
          <input type="checkbox" id="intern" name="intern" value="true" style="text-align:left" onclick="checkNoSecureNorExport()" <%= Boolean.valueOf(wp.readInternProp()).booleanValue() ? "checked=\"checked\"" : "" %>>
          </td>
         
      </tr>

    </table>

    <%= wp.dialogBlockEnd() %>

    <% 
       // display export-part of the dialog only if export is enabled
       if(OpenCms.getStaticExportManager().isStaticExportEnabled() && wp.showExportSettings()) { 
    %>

    <%= wp.dialogBlockStart(wp.key("label.exportsettings")) %>

    <table border="0">

      <tr>
        <td width="100pt"><%= wp.key("input.export") %></td>
        <td>
          <%= wp.buildRadio(I_CmsConstants.C_PROPERTY_EXPORT) %>
        </td>
      </tr>
    </table>

    <table border="0">
      <tr>
        <td width="100pt">
          <%= wp.key("input.exportname") %>:
        </td>

        <td class="maxwidth">
          <input type="text" id="exportname" name="exportname" class="maxwidth" value="<%= wp.readProperty(I_CmsConstants.C_PROPERTY_EXPORTNAME) %>">
        </td>
      </tr>

    </table>

    <%= wp.dialogBlockEnd() %>

    <% } %>

    <%= wp.dialogContentEnd() %> 
    <%= wp.dialogButtonsOkCancel() %></p>

    </form>

    <%= wp.dialogEnd() %>
    <%= wp.bodyEnd() %>
    <%= wp.htmlEnd() %>


<% } %>