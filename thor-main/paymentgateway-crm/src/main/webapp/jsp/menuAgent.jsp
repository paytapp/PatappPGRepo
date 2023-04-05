<%@taglib prefix="s" uri="/struts-tags"%>
<link rel="icon" href="../image/favicon-32x32.png">
<link href="../fonts/css/font-awesome.min.css" rel="stylesheet">
<link href="../css/welcomePage.css" rel="stylesheet">
<script src="../js/jquery.min.js"></script>
<link rel="stylesheet" href="../css/styles.css">

<script>
	$.noConflict();
</script>
<script>

$(document).ready(function(e){
	function resizeHeight(){
		var _height = window.innerHeight;
		document.querySelector(".right_col").style.minHeight = _height+"px";
	}
	resizeHeight();

	window.addEventListener("resize", function(e){
		console.log("hi")
		resizeHeight()
	})
})

	$(document).ready(
			function(e) {
				

				$(".head").click(function(e) {
					// $(".head").next().slideUp();
					$(this).next().slideToggle();
				});

				var _getActive = $(document).attr('title');
				// console.log(_getActive);
				var _setWord = "";
				for (var i = 0; i < _getActive.length; i++) {
					if (_getActive[i].indexOf(" ") != -1) {
						_setWord += _getActive[i].replace(" ", "-");
					} else {
						_setWord += _getActive[i];
					}
				}

				$("[data-page=" + _setWord + "]").closest("ul").prev(".head")
						.addClass("selected");

				$("[data-page=" + _setWord + "]").closest("li").addClass(
						"selected");
				$("[data-page=" + _setWord + "]").addClass("current");

				$(".collapse-nav").on("click", function(e) {
					$(".collapse-nav").toggleClass("collapse-icon");
					$(".side-wrapper").toggleClass("active-sidebar");
					$(".side-wrapper").toggleClass("active-collapse");
					$(".right_col").toggleClass("right_col_extend");
				});

				$(".side-wrapper").on("hover", function(e) {
					var _isSidebarActive = $(this).hasClass("active-sidebar");
					if (_isSidebarActive == true) {
						$(".side-wrapper").toggleClass("active-collapse");
						$(".right_col").toggleClass("right_col_extend");
					}
				})

				var _navScroll = document.querySelector(".side-wrapper_navigation");
				function navScroll() {
					var _active = $(".selected");
					if(_active > 0){
						var _selectPosition = $(".selected").offset().top;
						var _windowSize = $(window).height();
						if (_selectPosition > _windowSize) {
							_navScroll.scrollTo(0, _windowSize);
						}
					}
				}

				navScroll();

			})
</script>

<style>
span#arrow {
	margin-left: 0 !important;
	float: right;
	margin-top: 11px;
}



</style>


<aside class="side-wrapper">
	<header class="side-wrapper_logo">
		<a class="logo_link" href="home">
			<img src="../image/white-logo.png" alt="Pg" class="white-logo-png">
			<img src="../image/white-logo-abr.png" alt="Pg" class="white-logo-abr">
		</a>
	</header>
	<!-- /.side-wrapper_logo -->
	<nav class="side-wrapper_navigation">
		<ul id="navigation" class="nav side-menu">
			<%-- <li><s:a action='searchTransactionAgent'><span class="nav_icon">S</span>Search Transaction</s:a></li> --%>

				<li><a style="cursor: pointer" class="head">
					<span class="nav_icon"><i class="fa fa-user"></i></span><span class="menu-text">My Account</span>
					<span class="fa fa-angle-down" id="arrow"></span></a>
					<ul>
						<li><s:a action="agentProfile"><span class="nav_icon">MP</span><span class="menu-text">My Profile</span></s:a></li>
						<li><s:a action="loginHistoryRedirect"><span class="nav_icon">LH</span><span class="menu-text">Login History</span></s:a>
						</li>
						<li><s:a action='passwordChange'><span class="nav_icon">CP</span><span class="menu-text">Change PIN</span></s:a></li>
	
					</ul>
			</li>
			<li>
				<a style="cursor: pointer" class="head">
				<span class="nav_icon"><i class="fa fa-file"></i></span><span class="menu-text">Agent Access</span><span class="fa fa-angle-down" id="arrow"></span></a>
					<ul>
					<li><s:a action='agentSearch'><span class="nav_icon">AS</span><span class="menu-text">Agent Search</span></s:a></li>
					</ul>
			</li> 
			<!-- ticketing -->
		</ul>
	</nav>
	<!-- /.side_wrapper-navigation -->
</aside>



 

<script src="../js/bootstrap.min.js"></script>
<script>
	;(function ($, window, document, undefined) {

	    var pluginName = "metisMenu",
	        defaults = {
	            toggle: true
	        };
	        
	    function Plugin(element, options) {
	        this.element = element;
	        this.settings = $.extend({}, defaults, options);
	        this._defaults = defaults;
	        this._name = pluginName;
	        this.init();
	    }

	    Plugin.prototype = {
	        init: function () {

	            var $this = $(this.element),
	                $toggle = this.settings.toggle;

	            $this.find('li.active').has('ul').children('ul').addClass('collapse in');
	            $this.find('li').not('.active').has('ul').children('ul').addClass('collapse');

	            $this.find('li').has('ul').children('a').on('click', function (e) {
	                e.preventDefault();

	                $(this).parent('li').toggleClass('active').children('ul').collapse('toggle');

	                if ($toggle) {
	                    $(this).parent('li').siblings().removeClass('active').children('ul.in').collapse('hide');
	                }
	            });
	        }
	    };

	    $.fn[ pluginName ] = function (options) {
	        return this.each(function () {
	            if (!$.data(this, "plugin_" + pluginName)) {
	                $.data(this, "plugin_" + pluginName, new Plugin(this, options));
	            }
	        });
	    };

	})(jQuery, window, document);
</script>
<script>

(function ($) {
"use strict";
var mainApp = {

    initFunction: function () {
        /*MENU 
        ------------------------------------*/
        $('#main-menu').metisMenu();
		
        $(window).bind("load resize", function () {
            if ($(this).width() < 768) {
                $('div.sidebar-collapse').addClass('collapse')
            } else {
                $('div.sidebar-collapse').removeClass('collapse')
            }
        });

 
    },

    initialization: function () {
        mainApp.initFunction();

    }

}
// Initializing ///

$(document).ready(function () {
    mainApp.initFunction();
});

}(jQuery));
</script>
