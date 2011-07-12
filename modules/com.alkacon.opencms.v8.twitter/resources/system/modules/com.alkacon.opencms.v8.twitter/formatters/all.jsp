<%@page buffer="none" session="false" taglibs="c,cms,fmt" %>
<fmt:setLocale value="${cms.locale}" />
<fmt:bundle basename="com/alkacon/opencms/v8/twitter/messages">
<cms:formatter var="content" val="value">

<div class="twitter-box">
	<c:choose>
		<c:when test="${cms.edited}" >
			<%-- Handle the case the twitter box was recenty moved --%>						
			<div id="twtr-widget-1" class="twtr-widget twtr-widget-profile twtr-scroll">
				<div style="width: ${cms.container.width}px;" class="twtr-doc">
		            		<div class="twtr-hd"><a class="twtr-profile-img-anchor" href="http://twitter.com/intent/user?screen_name=${value.Channel}" target="_blank"><img src="http://a0.twimg.com/profile_images/59352834/opencms_punkt_160.png_913563173_normal.png" class="twtr-profile-img" alt="profile"></a>
		                      		<h3><c:out value="${value.Channel}" /></h3>
		                      		<h4><a href="http://twitter.com/intent/user?screen_name=${value.Channel}" target="_blank"><c:out value="${value.Channel}" /></a></h4> 
					</div>
		            		<div class="twtr-bd">
		              			<div style="height: ${cms.element.settings["box-height"]}px;" class="twtr-timeline">
		                			<div class="twtr-tweets">
		                  				<div class="twtr-reference-tweet">
		                  					<fmt:message key="v8.twitter.reload" />
		                  				</div>
		                  				<!-- tweets show here -->
		                			</div>
		              			</div>
		            		</div>
		                      	<div class="twtr-ft">
		                        	<div>
		                        		<a href="http://twitter.com" target="_blank"><img src="http://widgets.twimg.com/i/widget-logo.png" alt=""></a>
		                          		<span><a href="http://twitter.com/${value.Channel}" style="color: rgb(255, 255, 255);" class="twtr-join-conv" target="_blank">Join the conversation</a></span>
		                        	</div>
		                      	</div>
		    		</div>          
			</div>
		</c:when>
		<c:otherwise>
			<%-- Handle the case the page was recently reloaded, execute the scripts --%>											
			<script type="text/javascript">
				new TWTR.Widget({
				  version: 2,
				  type: 'profile',
				  rpp: 10,
				  interval: 6000,
				  width: '${cms.container.width}',
				  height: '${cms.element.settings["box-height"]}',
				  theme: {
				    shell: {
				      background: '<c:out value="${value.BackgroundColor}" />',
				      color: '#ffffff'
				    },
				    tweets: {
				      background: '#ffffff',
				      color: '#000000',
				      links: '${value.LinksColor}'
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
				}).render().setUser('${value.Channel}').start();
			</script>
		</c:otherwise>		
	</c:choose>
</div>

</cms:formatter>
</fmt:bundle>