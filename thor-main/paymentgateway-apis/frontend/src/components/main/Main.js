
import React, { Component } from 'react';

import { BrowserRouter as Router, Route, Switch, withRouter, useRouteMatch } from "react-router-dom";
import Access from '../access/Access';
import Home from '../pages/Home';

class Main extends Component{

    render(){
        return(
            <Router>
                <Switch>
                    <Route exact path={'/'} component={Access} />
                    <Route exact path={'/login'} component={Access} />
                    <Route exact path={'/home'} component={Home} />
                </Switch>
            </Router>
        )
    }
}

export default Main;