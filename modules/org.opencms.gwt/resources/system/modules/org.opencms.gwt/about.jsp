<%@page taglibs="cms" import="java.util.*,org.opencms.main.*,org.opencms.i18n.*" %><!DOCTYPE html>
<html>
<head>
<cms:jquery js="jquery" />
<script type="text/javascript">
$(function() { 
	setTimeout(function() {
		window.timerId = setInterval(function() {
			scrollBy(0, 1);
			if($(window).scrollTop() + $(window).height() == $(document).height()) {
   		    	clearInterval(window.timerId);
			}
		} , 20);
	} , 1000);
	
	$("body").click(function() {
		clearInterval(window.timerId);
	}); 
	
	
});

</script>

</head>
<body>
<%
		StringBuffer html = new StringBuffer();
        html.append("<table style='margin-left:auto; margin-right: auto; margin-bottom: 10px'>");
        Set<String> keys = OpenCms.getSystemInfo().getBuildInfoKeys();
        for (String key : keys) {
            CmsSystemInfo.BuildInfoItem item = OpenCms.getSystemInfo().getBuildInfoItem(key);
            html.append("<tr>");

            html.append("<td>");
            html.append(CmsEncoder.escapeXml(item.getNiceName() + ":"));
            html.append("</td>");

            html.append("<td>");
            html.append(CmsEncoder.escapeXml(item.getValue()));
            html.append("</td>");

            html.append("</tr>");
        }
        html.append("</table>");
		out.println(html.toString());
%>



Lorem ipsum dolor sit amet, consectetur adipiscing elit. Suspendisse vitae vestibulum sem. Duis id volutpat sapien. Duis in metus ultricies, mattis augue vel, placerat purus. Curabitur at eros mollis, dignissim ligula et, aliquet enim. Sed vestibulum turpis quis purus ultricies, vel auctor orci mattis. Fusce id felis sapien. Curabitur tempus dui at orci cursus fermentum. Integer luctus nulla nec purus aliquet, sit amet posuere leo dapibus. Aliquam non nisl a velit aliquet tincidunt eu non erat. Pellentesque fermentum risus vel suscipit venenatis. Sed molestie, tellus eu lobortis euismod, elit metus consequat dolor, vel luctus eros turpis ut diam.

Ut ut vulputate dolor. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed semper vehicula nibh eget elementum. Integer sit amet ultrices nunc. Nullam sodales nisl nec ligula semper, elementum vestibulum justo rhoncus. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aenean eu ipsum interdum, tempor odio quis, elementum justo. Pellentesque sem nunc, fermentum sit amet mattis id, luctus sit amet magna.

Sed sit amet imperdiet justo, id varius lorem. Nulla vel tincidunt tortor, sed consectetur velit. Morbi et orci non elit faucibus blandit. Nullam condimentum euismod rhoncus. Nullam porttitor quam et nisl dapibus scelerisque. In mattis tempus lacus, quis viverra tortor rutrum in. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Maecenas facilisis egestas urna vitae porttitor. Duis sed est in massa sodales molestie eu ut ligula. Maecenas scelerisque, mauris quis interdum lobortis, dolor purus cursus quam, a commodo tellus arcu non justo. Phasellus in dolor adipiscing, tempor lacus in, tristique massa. Cras rutrum tincidunt orci, id ultrices tellus. Aenean molestie quis nisi id interdum.

Morbi tortor lectus, sollicitudin vel mollis id, pretium sed sapien. Donec interdum posuere risus, eu feugiat dolor ornare scelerisque. Nunc adipiscing auctor ullamcorper. Mauris lorem ligula, ullamcorper nec scelerisque vitae, porttitor hendrerit libero. Phasellus fermentum orci eu euismod lobortis. Fusce sit amet tristique libero, feugiat cursus libero. Donec quis porttitor justo, non ultricies lacus. Mauris quis tincidunt nunc, in tincidunt justo. Curabitur rutrum ornare vehicula. Nam tellus purus, tempor eget pellentesque a, feugiat quis leo. Nunc felis diam, iaculis eu tristique vitae, sagittis nec sem. Morbi elit urna, venenatis eget nunc in, posuere semper dolor. Vivamus placerat vel metus vitae feugiat.

Maecenas nisl odio, viverra a velit et, consectetur euismod metus. Cras vel tristique arcu, eget adipiscing justo. Morbi at tortor massa. Sed ut mi ac risus suscipit posuere. Vestibulum mattis auctor ligula sed interdum. Nam congue auctor ligula quis vestibulum. Mauris mollis ultrices dolor, a feugiat lorem ultrices vel. Phasellus quis vulputate est, nec luctus dolor. Aliquam nec est ut massa fringilla suscipit. 

Lorem ipsum dolor sit amet, consectetur adipiscing elit. Suspendisse vitae vestibulum sem. Duis id volutpat sapien. Duis in metus ultricies, mattis augue vel, placerat purus. Curabitur at eros mollis, dignissim ligula et, aliquet enim. Sed vestibulum turpis quis purus ultricies, vel auctor orci mattis. Fusce id felis sapien. Curabitur tempus dui at orci cursus fermentum. Integer luctus nulla nec purus aliquet, sit amet posuere leo dapibus. Aliquam non nisl a velit aliquet tincidunt eu non erat. Pellentesque fermentum risus vel suscipit venenatis. Sed molestie, tellus eu lobortis euismod, elit metus consequat dolor, vel luctus eros turpis ut diam.

Ut ut vulputate dolor. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed semper vehicula nibh eget elementum. Integer sit amet ultrices nunc. Nullam sodales nisl nec ligula semper, elementum vestibulum justo rhoncus. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aenean eu ipsum interdum, tempor odio quis, elementum justo. Pellentesque sem nunc, fermentum sit amet mattis id, luctus sit amet magna.

Sed sit amet imperdiet justo, id varius lorem. Nulla vel tincidunt tortor, sed consectetur velit. Morbi et orci non elit faucibus blandit. Nullam condimentum euismod rhoncus. Nullam porttitor quam et nisl dapibus scelerisque. In mattis tempus lacus, quis viverra tortor rutrum in. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Maecenas facilisis egestas urna vitae porttitor. Duis sed est in massa sodales molestie eu ut ligula. Maecenas scelerisque, mauris quis interdum lobortis, dolor purus cursus quam, a commodo tellus arcu non justo. Phasellus in dolor adipiscing, tempor lacus in, tristique massa. Cras rutrum tincidunt orci, id ultrices tellus. Aenean molestie quis nisi id interdum.

Morbi tortor lectus, sollicitudin vel mollis id, pretium sed sapien. Donec interdum posuere risus, eu feugiat dolor ornare scelerisque. Nunc adipiscing auctor ullamcorper. Mauris lorem ligula, ullamcorper nec scelerisque vitae, porttitor hendrerit libero. Phasellus fermentum orci eu euismod lobortis. Fusce sit amet tristique libero, feugiat cursus libero. Donec quis porttitor justo, non ultricies lacus. Mauris quis tincidunt nunc, in tincidunt justo. Curabitur rutrum ornare vehicula. Nam tellus purus, tempor eget pellentesque a, feugiat quis leo. Nunc felis diam, iaculis eu tristique vitae, sagittis nec sem. Morbi elit urna, venenatis eget nunc in, posuere semper dolor. Vivamus placerat vel metus vitae feugiat.

Maecenas nisl odio, viverra a velit et, consectetur euismod metus. Cras vel tristique arcu, eget adipiscing justo. Morbi at tortor massa. Sed ut mi ac risus suscipit posuere. Vestibulum mattis auctor ligula sed interdum. Nam congue auctor ligula quis vestibulum. Mauris mollis ultrices dolor, a feugiat lorem ultrices vel. Phasellus quis vulputate est, nec luctus dolor. Aliquam nec est ut massa fringilla suscipit. 


</body>
</html>