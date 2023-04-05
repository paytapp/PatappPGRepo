import React from 'react';
import "./topbar.css";
import { FaUserCircle, FaBell, FaBraille } from 'react-icons/fa';
import helper from "../utility/helper.module.css";
import FormInput from '../formElements/Input/Input';
export default function Topbar() {
    return (

        <div className={helper.zk_wrapper+ " zk-topbar_wrapper d-flex align-items-center justify-content-between"}>
            <div className="zk-topbar_left d-flex align-items-center">
                <div className={helper.zk_iconBox +" zk-topbar_home "+ helper.zk_center }>
                    <FaBraille />
                </div>
                <span className={"logo"}>
                    <img src={`${window.basePath}/img/dark-logo.png`} alt="/" />
                </span>
                <div className="zk-topbar_search">
                    
                    <FormInput />
                </div>
            </div>
            <div className="zk-topbar_right d-flex align-items-center">
                <div className={helper.zk_iconBox +" "+ helper.zk_center}>
                    <FaBell />
                </div>
                <div className={helper.zk_iconBox +" "+ helper.zk_center}>
                    <FaUserCircle />
                </div>
                <span className="zk-login_txt"><b>Hello </b>, Bata Store</span>
            </div>
        </div>
        
    
    );
  }