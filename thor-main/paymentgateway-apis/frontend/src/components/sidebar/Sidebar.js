import React from 'react';
import './sidebar.css';
import { NavigationData } from './NavigationsData';
// import SubMenu from './SubMenu';
import Navigation from "./Navigation";

export default function Sidebar(){

    return (
        <div className="zk-sidebar">
            <div className="zk-navbar_div">
                <ul className="zk-navbar_ul">
                {
                    NavigationData.map((item, index) => {
                        return <Navigation  item={item} key={index} />;
                    })
                }
                </ul>
            </div>
        </div>
    )
}
