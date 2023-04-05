import React from 'react';
import { FaSearch,FaChalkboard,FaAngleDown,FaRegChartBar,FaTools,FaSuitcase,FaChartPie,FaUsers } from 'react-icons/fa';

export const NavigationData =  [
    {
        id: 1,
        title: "Dashboard",
        path : "/home",
        active: true,
        menuIcon: <FaChartPie />,
        subNav : []
    },
    {
        id: 2,
        title : "Analytics",
        path : "#",
        active: false,
        menuIcon: <FaRegChartBar />,
        icon: <FaAngleDown />,
        subNav : [
            { title : "Performance Report" },
            { title : "Revenue Report" },
        ]
    },
    {
        id: 3,
        title : "Merchant Setup",
        menuIcon : <FaTools />,
        path : "#",
        active: false,
        icon: <FaAngleDown />,
        subNav : [
            { title : "User Registration" },
            { title : "Merchant Account" },
            { title : "Merchant Underwriting" },
            { title : "User Audit Trail" },
            { title : "Merchant List for MPA" },
            { title : "User Status" },
            { title : "Create Custom Page" }
        ]
    },
    {
        id: 4,
        title : "Merchant Config",
        menuIcon: <FaSuitcase />,
        path : "#",
        active: false,
        icon: <FaAngleDown />,
        subNav : [
            { title: "Merchant Mapping" },
            { title: "Payment Options" },
            { title: "Payout Mapping" },
            { title: "SUF Details" },
            { title: "Discount Details" },
            { title: "Charging Details" },
            { title: "Bulk Update Charges" },
            { title: "Smart Router" },
            { title: "Mer Default Charges" },
            { title: "Pending Request" }
        ]
    },
    { 
        id: 5,
        title: "Reseller",
        menuIcon: <FaUsers />,
        path : "#",
        active: false,
        icon: <FaAngleDown />,
        subNav : [
            { title: "Reseller Account" },
            { title: "Reseller Charges Update" },
            { title: "Reseller Merchant List" }
        ]
    },
    { 
        id: 6,
        title: "View Configuration",
        menuIcon: <FaChalkboard />,
        path : "#",
        active: false,
        icon: <FaAngleDown />,
        subNav : [
            { title: "View Smart Router" },
            { title: "Charging Details" },
            { title: "Reseller Charges" },
            { title: "Production Details" },
            { title: "View Virtual Details" }
        ]
    },
    { 
        id: 7,
        title: "Quick Search",
        menuIcon: <FaSearch />,
        path : "#",
        active: false,
        icon: <FaAngleDown />,
        subNav : [
            { title: "Search Transaction" },
            { title: "Download Transation" },
            { title: "Download Txn Trails" }
        ]
    },
]