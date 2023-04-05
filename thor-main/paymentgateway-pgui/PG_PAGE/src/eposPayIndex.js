import React from 'react';
import ReactDOM from 'react-dom';

import '../src/js/global';
import EposPayPage from './components/EposPayPage/EposPayPage';
import reportWebVitals from './reportWebVitals';


ReactDOM.render(
  <React.StrictMode>
    <EposPayPage />
  </React.StrictMode>,
  document.getElementById('root')
);

reportWebVitals();
