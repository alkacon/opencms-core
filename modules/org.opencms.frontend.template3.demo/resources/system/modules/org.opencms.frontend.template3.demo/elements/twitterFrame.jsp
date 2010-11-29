<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %><html>
	<head>
		<script src="http://widgets.twimg.com/j/2/widget.js"></script>
		<style> body{ margin: 0; padding: 0; overflow: hidden;}</style>
	</head>
	<body>
		<script>
		new TWTR.Widget({
		  version: 2,
		  type: 'profile',
		  rpp: 10,
		  interval: 6000,
		  title: '<c:out value="${param.title}" />',
		  subject: '<c:out value="${param.subtitle}" />',
		  width: 228,
		  height: 250,
		  theme: {
		    shell: {
		      background: '#<c:out value="${param.background}" />',
		      color: '#ffffff'
		    },
		    tweets: {
		      background: '#ffffff',
		      color: '#444444',
		      links: '#43c43f'
		    }
		  },
		  features: {
		    scrollbar: true,
		    loop: false,
		    live: true,
		    hashtags: true,
		    timestamp: true,
		    avatars: false,
		    behavior: 'all'
		  }
		}).render().setUser('<c:out value="${param.channel}" />').start();
		</script>
	</body>
</html>