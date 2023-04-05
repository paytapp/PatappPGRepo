<%@taglib prefix="s" uri="/struts-tags"%>
<link rel="icon" href="../image/favicon-32x32.png">
<link href="../css/bootstrap.minr.css" rel="stylesheet">
<link href="../fonts/css/font-awesome.min.css" rel="stylesheet">
<link href="../css/default.css" rel="stylesheet">
<link href="../css/custom.css" rel="stylesheet">
<link rel="stylesheet" href="../css/navigation.css">
<link href="../css/welcomePage.css" rel="stylesheet">
<script type="text/javascript" src="../js/jquery.js"></script>
<script type="text/javascript" src="../js/jquery.easing.js"></script>
<script type="text/javascript" src="../js/jquery.dimensions.js"></script>
<script type="text/javascript" src="../js/jquery.accordion.js"></script>
<link rel="stylesheet" href="../css/loader.css">
<script>
	$.noConflict();
	// Code that uses other library's $ can follow here.
</script>
<script type="text/javascript">
	jQuery().ready(function() {
		// simple accordion
		jQuery('#list1a').accordion();
		jQuery('#list1b').accordion({
			autoheight : false
		});

		// second simple accordion with special markup
		jQuery('#navigation').accordion({
			active : false,
			header : '.head',
			navigation : true,
			event : 'click',
			fillSpace : false,
			animated : 'easeslide'
		});
	});
</script>

<div id="fade"></div>
<div id="modal" class="lodinggif">
	<img id="loader" src="../image/loader.gif" />
</div>
<div class="row" style="border-bottom: 1px solid #d4d4d4;">
	<div class="col-md-12 col-xs-12">
		<nav class="navbar navbar-default top-navbar" role="navigation">
			<div class="navbar-header">
				<button type="button" class="navbar-toggle" data-toggle="collapse"
					data-target=".sidebar-collapse">
					<span class="sr-only">Toggle navigation</span> <span
						class="icon-bar"></span> <span class="icon-bar"></span> <span
						class="icon-bar"></span>
				</button>
				<a class="navbar-brand" href="home"><img
					src="../image/lpay.png" width="180"></a>
			</div>
			<ul class="nav navbar-top-links navbar-right">

				<li class="dropdown"><a class="dropdown-toggle"
					data-toggle="dropdown" href="#" aria-expanded="false"> <i
						class="fa fa-user fa-fw"></i><b>Welcome <s:property
								value="%{#session.USER.businessName}" /></b> <i
						class="fa fa-caret-down"></i>
				</a>
					<ul class="dropdown-menu dropdown-user">
						<li><s:a action="logout">
								<i class="fa fa-sign-out fa-fw"></i> Logout</s:a></li>
					</ul></li>
			</ul>
		</nav>
	</div>
</div>
<div class="col-md-2 left_col col-xs-12" id="wrapperr">
	<div>
		<nav class="navbar-default navbar-side" role="navigation">
			<div id="main">
				<div class="sidebar-collapse">
					<ul id="navigation" class="nav side-menu">
					<li><s:a action='home' class="head1">
				    <i class="fa fa-home"></i> Super Admin Dashboard</s:a></li>
				    <li><a style="cursor: pointer" class="head"><i
								class="fa fa-list-alt"></i> Admin Setup <span
								class="fa fa-chevron-down"></span></a>
							    <ul>
							    <li><s:a action='viewAdmin'>Admin Account</s:a></li>
								<li><s:a action='adminSignup'>Create Admin</s:a></li>
							 </ul></li>
					</ul>
					</div>
			</div>
		</nav>
	</div>
</div>
<script src="../js/bootstrap.min.js"></script>
<script>
	;
	(function($, window, document, undefined) {

		var pluginName = "metisMenu", defaults = {
			toggle : true
		};

		function Plugin(element, options) {
			this.element = element;
			this.settings = $.extend({}, defaults, options);
			this._defaults = defaults;
			this._name = pluginName;
			this.init();
		}

		Plugin.prototype = {
			init : function() {

				var $this = $(this.element), $toggle = this.settings.toggle;

				$this.find('li.active').has('ul').children('ul').addClass(
						'collapse in');
				$this.find('li').not('.active').has('ul').children('ul')
						.addClass('collapse');

				$this.find('li').has('ul').children('a').on(
						'click',
						function(e) {
							e.preventDefault();

							$(this).parent('li').toggleClass('active')
									.children('ul').collapse('toggle');

							if ($toggle) {
								$(this).parent('li').siblings().removeClass(
										'active').children('ul.in').collapse(
										'hide');
							}
						});
			}
		};

		$.fn[pluginName] = function(options) {
			return this.each(function() {
				if (!$.data(this, "plugin_" + pluginName)) {
					$.data(this, "plugin_" + pluginName, new Plugin(this,
							options));
				}
			});
		};

	})(jQuery, window, document);
</script>
<script>
	(function($) {
		"use strict";
		var mainApp = {

			initFunction : function() {
				/*MENU 
				------------------------------------*/
				$('#main-menu').metisMenu();

				$(window).bind("load resize", function() {
					if ($(this).width() < 768) {
						$('div.sidebar-collapse').addClass('collapse')
					} else {
						$('div.sidebar-collapse').removeClass('collapse')
					}
				});

			},

			initialization : function() {
				mainApp.initFunction();

			}

		}
		// Initializing ///

		$(document).ready(function() {
			mainApp.initFunction();
		});

	}(jQuery));
</script>
