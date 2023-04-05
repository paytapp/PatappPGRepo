import React from 'react';
import ReactDOM from 'react-dom';

import '../src/js/global';
import EnachResponse from './components/EnachRegistration/EnachResponse';
import reportWebVitals from './reportWebVitals';


ReactDOM.render(
  <React.StrictMode>
    <EnachResponse />
  </React.StrictMode>,
  document.getElementById('root')
);

reportWebVitals();
