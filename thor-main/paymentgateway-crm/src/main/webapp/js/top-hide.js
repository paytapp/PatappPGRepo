// JavaScript Document
function tabs(x)
  {
    var lis=document.getElementById("sidebarTabs").childNodes; //gets all the LI from the UL
    for(i=0;i<lis.length;i++)
    {
      lis[i].className=""; //removes the classname from all the LI
    }
    x.className="selected"; //the clicked tab gets the classname selected
    var res=document.getElementById("tabContent");  //the resource for the main tabContent
    var tab=x.id;
    switch(tab) //this switch case replaces the tabContent
    {
      case "tab1":
        res.innerHTML=document.getElementById("tab1Content").innerHTML;
        break;
      case "tab2":
        res.innerHTML=document.getElementById("tab2Content").innerHTML;
        break;
      case "tab3":
        res.innerHTML=document.getElementById("tab3Content").innerHTML;
        break;
		case "tab4":
        res.innerHTML=document.getElementById("tab4Content").innerHTML;
        break;
		case "tab5":
        res.innerHTML=document.getElementById("tab5Content").innerHTML;
        break;
		case "tab6":
        res.innerHTML=document.getElementById("tab6Content").innerHTML;
        break;
		case "tab7":
        res.innerHTML=document.getElementById("tab7Content").innerHTML;
        break;
      default:
        res.innerHTML=document.getElementById("tab1Content").innerHTML;
        break;
    }
  }
  
  
  
  
  
  function tabsR(x)
  {
    var lis=document.getElementById("sidebarTabs1").childNodes; //gets all the LI from the UL
    for(i=0;i<lis.length;i++)
    {
      lis[i].className=""; //removes the classname from all the LI
    }
    x.className="selected"; //the clicked tab gets the classname selected
    var res=document.getElementById("tabContentR");  //the resource for the main tabContentR
    var tab=x.id;
    switch(tab) //this switch case replaces the tabContentR
    {
      case "tab1R":
        res.innerHTML=document.getElementById("tab1RContent").innerHTML;
        break;
      case "tab2R":
        res.innerHTML=document.getElementById("tab2RContent").innerHTML;
        break;
      case "tab3R":
        res.innerHTML=document.getElementById("tab3RContent").innerHTML;
        break;
		case "tab4R":
        res.innerHTML=document.getElementById("tab4RContent").innerHTML;
        break;
		case "tab5R":
        res.innerHTML=document.getElementById("tab5RContent").innerHTML;
        break;
      default:
        res.innerHTML=document.getElementById("tab1RContent").innerHTML;
        break;
    }
  }
  
  
  
  
  
  function tabsA(x)
  {
    var lis=document.getElementById("sidebarTabs2").childNodes; //gets all the LI from the UL
    for(i=0;i<lis.length;i++)
    {
      lis[i].className=""; //removes the classname from all the LI
    }
    x.className="selected"; //the clicked tab gets the classname selected
    var res=document.getElementById("tabContentA");  //the resource for the main tabContentA
    var tab=x.id;
    switch(tab) //this switch case replaces the tabContentA
    {
      case "tab1A":
        res.innerHTML=document.getElementById("tab1AContent").innerHTML;
        break;
      case "tab2A":
        res.innerHTML=document.getElementById("tab2AContent").innerHTML;
        break;
      case "tab3A":
        res.innerHTML=document.getElementById("tab3AContent").innerHTML;
        break;
      default:
        res.innerHTML=document.getElementById("tab1AContent").innerHTML;
        break;
    }
  }
  
  
  
  
  
  
  
  function tabsB(x)
  {
    var lis=document.getElementById("sidebarTabs1B").childNodes; //gets all the LI from the UL
    for(i=0;i<lis.length;i++)
    {
      lis[i].className=""; //removes the classname from all the LI
    }
    x.className="selected"; //the clicked tab gets the classname selected
    var res=document.getElementById("tabContentB");  //the resource for the main tabContentB
    var tab=x.id;
    switch(tab) //this switch case replaces the tabContentB
    {
      case "tab1B":
        res.innerHTML=document.getElementById("tab1BContent").innerHTML;
        break;
      case "tab2B":
        res.innerHTML=document.getElementById("tab2BContent").innerHTML;
        break;
      case "tab3B":
        res.innerHTML=document.getElementById("tab3BContent").innerHTML;
        break;
		case "tab4B":
        res.innerHTML=document.getElementById("tab4BContent").innerHTML;
        break;
		case "tab5B":
        res.innerHTML=document.getElementById("tab5BContent").innerHTML;
        break;
		case "tab6B":
        res.innerHTML=document.getElementById("tab6BContent").innerHTML;
        break;
		case "tab7B":
        res.innerHTML=document.getElementById("tab7BContent").innerHTML;
        break;
		case "tab8B":
        res.innerHTML=document.getElementById("tab8BContent").innerHTML;
        break;
		case "tab9B":
        res.innerHTML=document.getElementById("tab9BContent").innerHTML;
        break;
		case "tab10B":
        res.innerHTML=document.getElementById("tab10BContent").innerHTML;
        break;
      default:
        res.innerHTML=document.getElementById("tab1BContent").innerHTML;
        break;
    }
  }