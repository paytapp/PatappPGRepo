import React from 'react';
import ReactDOM from 'react-dom';

import '../src/js/global';
import '../src/js/variables';
import reportWebVitals from './reportWebVitals';
import Main from './components/Main/Main';
// import App from './components/App';

ReactDOM.render(
  <React.StrictMode>
    <Main />
    {/* <App /> */}
  </React.StrictMode>,
  document.getElementById('root')
);

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();
