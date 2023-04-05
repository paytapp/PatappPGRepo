import React from 'react';
import { Component } from 'react';
import login from '../access/Login';
import { BrowserRouter as Router, Route, Redirect } from "react-router-dom";
import Topbar from '../topbar/Topbar';
import Sidebar from '../sidebar/Sidebar';
import Pages from './Pages';


class Home extends Component{
    
    constructor(props) {
        super(props);
        this.state = { isLoggedIn: false };
    }

    render(){
        return (
            <main>
                <Topbar />
                <Sidebar />
                <Pages />
            </main>
        )
    }
}

export default Home;

