var horizontalScrollingNav = function() {
    var SETTINGS = {
        navBarTravelling: false,
        navBarTravelDirection: "",
        navBarTravelDistance: 150
    }

    var _id = document.getElementById.bind(document);

    // Out advancer buttons
    var btnScrollLeft = _id("btn-scroll-left");
    var btnScrollRight = _id("btn-scroll-right");

    var horizontalNav = _id("horizontal-nav");
    var horizontalNavContent = _id("horizontal-nav-content");

    horizontalNav.setAttribute("data-overflowing", determineOverflow(horizontalNavContent, horizontalNav));

    // Set the indicator
    // moveIndicator(horizontalNav.querySelector("[aria-selected=\"true\"]"), colours[0]);

    // Handle the scroll of the horizontal container
    var last_known_scroll_position = 0;
    var ticking = false;

    function doSomething(scroll_pos) {
        horizontalNav.setAttribute("data-overflowing", determineOverflow(horizontalNavContent, horizontalNav));
    }

    horizontalNav.addEventListener("scroll", function() {
        last_known_scroll_position = window.scrollY;
        if (!ticking) {
            window.requestAnimationFrame(function() {
                doSomething(last_known_scroll_position);
                ticking = false;
            });
        }
        ticking = true;
    });

    var scrollLeft = function() {
        // If in the middle of a move return
        if (SETTINGS.navBarTravelling === true) {
            return;
        }
        // If we have content overflowing both sides or on the left
        if (determineOverflow(horizontalNavContent, horizontalNav) === "left" || determineOverflow(horizontalNavContent, horizontalNav) === "both") {
            // Find how far this panel has been scrolled
            var availableScrollLeft = horizontalNav.scrollLeft;
            // If the space available is less than two lots of our desired distance, just move the whole amount
            // otherwise, move by the amount in the settings
            if (availableScrollLeft < SETTINGS.navBarTravelDistance * 2) {
                horizontalNavContent.style.transform = "translateX(" + availableScrollLeft + "px)";
            } else {
                horizontalNavContent.style.transform = "translateX(" + SETTINGS.navBarTravelDistance + "px)";
            }
            // We do want a transition (this is set in CSS) when moving so remove the class that would prevent that
            horizontalNavContent.classList.remove("horizontal-nav-content-no-transition");
            // Update our settings
            SETTINGS.navBarTravelDirection = "left";
            SETTINGS.navBarTravelling = true;
        }
        // Now update the attribute in the DOM
        horizontalNav.setAttribute("data-overflowing", determineOverflow(horizontalNavContent, horizontalNav));
    }

    var scrollRight = function() {
        // If in the middle of a move return
        if (SETTINGS.navBarTravelling === true) {
            return;
        }
        // If we have content overflowing both sides or on the right
        if (determineOverflow(horizontalNavContent, horizontalNav) === "right" || determineOverflow(horizontalNavContent, horizontalNav) === "both") {
            // Get the right edge of the container and content
            var navBarRightEdge = horizontalNavContent.getBoundingClientRect().right;
            var navBarScrollerRightEdge = horizontalNav.getBoundingClientRect().right;
            // Now we know how much space we have available to scroll
            var availableScrollRight = Math.floor(navBarRightEdge - navBarScrollerRightEdge);
            // If the space available is less than two lots of our desired distance, just move the whole amount
            // otherwise, move by the amount in the settings
            if (availableScrollRight < SETTINGS.navBarTravelDistance * 2) {
                horizontalNavContent.style.transform = "translateX(-" + availableScrollRight + "px)";
            } else {
                horizontalNavContent.style.transform = "translateX(-" + SETTINGS.navBarTravelDistance + "px)";
            }
            // We do want a transition (this is set in CSS) when moving so remove the class that would prevent that
            horizontalNavContent.classList.remove("horizontal-nav-content-no-transition");
            // Update our settings
            SETTINGS.navBarTravelDirection = "right";
            SETTINGS.navBarTravelling = true;
        }
        // Now update the attribute in the DOM
        horizontalNav.setAttribute("data-overflowing", determineOverflow(horizontalNavContent, horizontalNav));
    }


    btnScrollLeft.addEventListener("click", function() {
        scrollLeft();
    });

    btnScrollRight.addEventListener("click", function() {
        scrollRight();
    });

    horizontalNav.addEventListener("wheel", function(event) {
        event.preventDefault();

        if (event.deltaY < 0) {
            scrollLeft();
        } else if (event.deltaY > 0) {
            scrollRight();
        }
    })

    horizontalNavContent.addEventListener("transitionend", function() {
        // get the value of the transform, apply that to the current scroll position (so get the scroll pos first) and then remove the transform
        var styleOfTransform = window.getComputedStyle(horizontalNavContent, null);
        var tr = styleOfTransform.getPropertyValue("-webkit-transform") || styleOfTransform.getPropertyValue("transform");
        // If there is no transition we want to default to 0 and not null
        var amount = Math.abs(parseInt(tr.split(",")[4]) || 0);
        horizontalNavContent.style.transform = "none";
        horizontalNavContent.classList.add("horizontal-nav-content-no-transition");
        // Now lets set the scroll position
        if (SETTINGS.navBarTravelDirection === "left") {
            horizontalNav.scrollLeft = horizontalNav.scrollLeft - amount;
        } else {
            horizontalNav.scrollLeft = horizontalNav.scrollLeft + amount;
        }
        SETTINGS.navBarTravelling = false;
    }, false);

    // Handle setting the currently active link
    horizontalNavContent.addEventListener("click", function(e) {
        var links = [].slice.call(document.querySelectorAll(".nav-link"));
        links.forEach(function(item) {
            item.setAttribute("aria-selected", "false");
        })
        e.target.setAttribute("aria-selected", "true");
        
        // Pass the clicked item and it's colour to the move indicator function
        // moveIndicator(e.target, colours[links.indexOf(e.target)]);
    });

    function determineOverflow(content, container) {
        var containerMetrics = container.getBoundingClientRect();
        var containerMetricsRight = Math.floor(containerMetrics.right);
        var containerMetricsLeft = Math.floor(containerMetrics.left);
        var contentMetrics = content.getBoundingClientRect();
        var contentMetricsRight = Math.floor(contentMetrics.right);
        var contentMetricsLeft = Math.floor(contentMetrics.left);
        if (containerMetricsLeft > contentMetricsLeft && containerMetricsRight < contentMetricsRight) {
            return "both";
        } else if (contentMetricsLeft < containerMetricsLeft) {
            return "left";
        } else if (contentMetricsRight > containerMetricsRight) {
            return "right";
        } else {
            return "none";
        }
    }

    /**
     * @fileoverview dragscroll - scroll area by dragging
     * @version 0.0.8
     * 
     * @license MIT, see https://github.com/asvd/dragscroll
     * @copyright 2015 asvd <heliosframework@gmail.com> 
     */


    (function (root, factory) {
        if (typeof define === 'function' && define.amd) {
            define(['exports'], factory);
        } else if (typeof exports !== 'undefined') {
            factory(exports);
        } else {
            factory((root.dragscroll = {}));
        }
    }(this, function (exports) {
        var _window = window;
        var _document = document;
        var mousemove = 'mousemove';
        var mouseup = 'mouseup';
        var mousedown = 'mousedown';
        var EventListener = 'EventListener';
        var addEventListener = 'add'+EventListener;
        var removeEventListener = 'remove'+EventListener;
        var newScrollX, newScrollY;

        var dragged = [];
        var reset = function(i, el) {
            for (i = 0; i < dragged.length;) {
                el = dragged[i++];
                el = el.container || el;
                el[removeEventListener](mousedown, el.md, 0);
                _window[removeEventListener](mouseup, el.mu, 0);
                _window[removeEventListener](mousemove, el.mm, 0);
            }

            // cloning into array since HTMLCollection is updated dynamically
            dragged = [].slice.call(_document.getElementsByClassName('dragscroll'));
            for (i = 0; i < dragged.length;) {
                (function(el, lastClientX, lastClientY, pushed, scroller, cont){
                    (cont = el.container || el)[addEventListener](
                        mousedown,
                        cont.md = function(e) {
                            if (!el.hasAttribute('nochilddrag') ||
                                _document.elementFromPoint(
                                    e.pageX, e.pageY
                                ) == cont
                            ) {
                                pushed = 1;
                                lastClientX = e.clientX;
                                lastClientY = e.clientY;

                                e.preventDefault();
                            }
                        }, 0
                    );

                    _window[addEventListener](
                        mouseup, cont.mu = function() {pushed = 0;}, 0
                    );

                    _window[addEventListener](
                        mousemove,
                        cont.mm = function(e) {
                            if (pushed) {
                                (scroller = el.scroller||el).scrollLeft -=
                                    newScrollX = (- lastClientX + (lastClientX=e.clientX));
                                scroller.scrollTop -=
                                    newScrollY = (- lastClientY + (lastClientY=e.clientY));
                                if (el == _document.body) {
                                    (scroller = _document.documentElement).scrollLeft -= newScrollX;
                                    scroller.scrollTop -= newScrollY;
                                }
                            }
                        }, 0
                    );
                })(dragged[i++]);
            }
        }

        
        if (_document.readyState == 'complete') {
            reset();
        } else {
            _window[addEventListener]('load', reset, 0);
        }

        exports.reset = reset;
    }));
}

window.addEventListener("load", function() {    
    horizontalScrollingNav();
});

window.addEventListener("resize", function() {
    horizontalScrollingNav();
});