<!-- ------------------------------------------------- JSP DECLARATIONS ------------------------------------------------ -->
<% /* Initialize the Bean */ %>
<jsp:useBean id="Bean" class="org.opencms.setup.CmsSetup" scope="session" />

        
<%  
    /* true if properties are initialized */
    boolean setupOk = (Bean.getProperties()!=null); 
    
    /* next page to be accessed */
    String nextPage = "step_8_browser_configuration_notes.jsp";

    
%>
<!-- ------------------------------------------------------------------------------------------------------------------- -->

<html>
<head> 
    <title>OpenCms Setup Wizard</title>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">    
    <link rel="Stylesheet" type="text/css" href="resources/style.css">
    <script language="Javascript">
            
        var enabled = false;
        var finished = false;
        var animation;
        var message = "Importing workplace ... please wait";
        var countchar = 0;      
        
        /* indicates if the document has been loaded */
        function enable()   {
            enabled = true;
            
            parent.data.location.href="step_7b_data_import.jsp";
            
            document.forms[0].info.value = message;
        }
        
        /* displays the given output */
        function start(out) {
            if(enabled) {   
                document.FORM.ctn.disabled = true;
                document.FORM.bck.disabled = true;
                temp ="";
                for(var i=out.length-1;i>=0;i--)    {
                    temp += unescape(out[i])+"\n";
                }
                var oldcontent = document.forms[0].output.value;
                document.forms[0].output.value = temp + oldcontent;
            }
        }
        
        /* Displays a message and enables the continue button */
        function finish()   {

            //window.clearInterval(animation);
            
            document.forms[0].info.value = "Finished. Please check the output below to ensure that the workplace has been imported without major errors.";
            document.FORM.ctn.disabled = false;
            document.FORM.bck.disabled = false;
            finished = true;
        }
        
        /* if finished, you can access next page */
        function nextpage() {
            if(finished)    {
                top.location.href="<%= nextPage %>";
            }
        }
        
        /* if finished, you can go back */
        function lastpage() {
            if(finished)    {
                history.back(-2);
            }
        }       
        
        /* status animation */
        function animate()  {
            if(countchar >= message.length) {
                countchar = 0;
                document.forms[0].info.value = "";
            }
            else    {
                document.forms[0].info.value += message.charAt(countchar);
                countchar++;
            }
        }
            
    </script>
</head>

<body <% if(setupOk){ out.print("onload='enable();'");} %>>
<table width="100%" height="100%" border="0" cellpadding="0" cellspacing="0">
<tr>    
<td align="center" valign="middle">
<table border="1" cellpadding="0" cellspacing="0">
<tr>
    <td><form action="<%= nextPage %>" method="POST" name="FORM">   
        <table class="background" width="700" height="500" border="0" cellpadding="5" cellspacing="0">
            <tr>
                <td class="title" height="25">OpenCms Setup Wizard</td>
            </tr>

            <tr>
                <td height="50" align="right"><img src="resources/opencms.gif" alt="OpenCms" border="0"></td>
            </tr>
            <% if(setupOk)  { %>
            <tr>
                <td height="375" align="center" valign="top">
                        <b>Status:</b><br><input type="text" style="width:650px" size="60" name="info"><br>
                        <textarea style="width:650px;height:325px;" cols="60" rows="16"  wrap="off" name="output"></textarea>
                </td>
            </tr>
            <tr>
                <td height="50" align="center">
                    <table border="0">
                        <tr>
                            <td width="200" align="right">
                                <input type="button" name="bck" class="button" style="width:150px;" width="150" width="150" value="&#060;&#060; Back" onclick="lastpage();">
                            </td>
                            <td width="200" align="left">
                                <input type="button" name="ctn" class="button" style="width:150px;" width="150" width="150" value="Continue &#062 &#62" onclick="nextpage();">
                            </td>
                            <td width="200" align="center">
                                <input type="button" class="button" style="width:150px;" width="150" width="150" value="Cancel" onclick="top.location.href='cancel.jsp'">
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
            <% } else   { %>
            <tr>
                <td align="center" valign="top" height="425">
                    <p><b>ERROR</b></p>
                    The setup wizard has not been started correctly!<br>
                    Please click <a href="" target="_top">here</a> to restart the Wizard
                </td>
            </tr>               
            <% } %> 
            </form>
            </table>
        </td>
    </tr>
</table>
</td>
</tr>
</table>
</body>
</html>