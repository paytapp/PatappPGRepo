import React from 'react';
import ReactDOM from 'react-dom';

import '../src/js/global';
import EnachRegistration from './components/EnachRegistration/EnachRegistration';
import reportWebVitals from './reportWebVitals';


ReactDOM.render(
  <React.StrictMode>
    <EnachRegistration />
  </React.StrictMode>,
  document.getElementById('root')
);

reportWebVitals();
