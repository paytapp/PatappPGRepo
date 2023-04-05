import React from 'react';
import ReactDOM from 'react-dom';

import '../src/js/global';
import StaticPgQrResponse from './components/StaticPgQrResponse/StaticPgQrResponse';
import reportWebVitals from './reportWebVitals';


ReactDOM.render(
  <React.StrictMode>
    <StaticPgQrResponse />
  </React.StrictMode>,
  document.getElementById('root')
);

reportWebVitals();
