import React, { Component } from 'react'; 
import { BrowserRouter as Router, Route, Switch, Redirect } from "react-router-dom";
import Login from './Login';
import SignUp from './singUp';
import "./access.css";



class Access extends Component {
    constructor(){
        super()
        this.state = {
            activeComponent : "login",
            isLogin: false
        }

    }

    tabHandler = (e, action) => {
        this.setState({activeComponent: action});
        if(action == "dashboard"){
            this.setState({isLogin: true, });
            this.setState({activeComponent: action});
        }
    }

    render() {
        let componentArr = [
            {
                "component" : Login,
                "label" : "login"
            },
            {
                "component" : SignUp,
                "label" : "register"
            },
            
        ];
        
        const componentsToRender = componentArr.map(key => {
            if(key.label === this.state.activeComponent) {                                    
                let SpecificComponent = key.component;
                return <SpecificComponent tabHandler={this.tabHandler} key={key.label} />
            }
        });

        
        if(this.state.isLogin == true){
            return (
               <Redirect to='/home' />
            )
        }else{
            return (
                <>
                    <div className="lp-access_wrapper">
                        <div className="lp-logo">
                            <img src={`${window.basePath}/img/white-logo.png`} alt="/" />
                        </div>
                        <div className="container">
                            <div className="row">
                                <div className="lp-access_box">
                                    <span className="lp-shape"></span>
                                    { componentsToRender }
                                </div>
                            </div>
                        </div>
                        <img src={`${window.basePath}/img/bg-5.jpg`} className="lp-access_bg" /> 
                    </div>
                </>
            )
        }
    
    }

}

export default Access;