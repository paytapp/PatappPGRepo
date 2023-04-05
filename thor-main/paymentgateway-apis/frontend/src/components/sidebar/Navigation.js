
import React, { useState } from 'react';
import { Link } from "react-router-dom";

const Navigation = ({ item, index }) => {

    const activeMenu = (e) => {

        var _getAllInput = document.querySelectorAll(".zk-navbar_ul .main_li");
        _getAllInput.forEach(function(index, array, elment){
            index.classList.remove("activeNav");
        })

        e.target.closest("li").classList.add("activeNav");

    }
   

    return(
        <>
        <li key={index} className="main_li" onClick={activeMenu}>
            <Link 
                to={item.path} 
                key={index} 
                title={item.title}>
                { item.menuIcon && <span className="zk-menuIcon">{item.menuIcon}</span> }
                { item.title }
                { item.icon && <span className="zk-menuArrow">{item.icon}</span>}
            </Link>
        { 
            <ul className="zk-dropdown">
                {item.subNav.map((item, index) => {
                    return (
                        <li key={index}>
                            <Link key={index} to={item.title}>
                                {item.title}
                            </Link>
                        </li>
                    )
                })}
            </ul>
        }
         
        </li>
        </>
    )

}

export default Navigation;