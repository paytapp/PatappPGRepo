import React from 'react';
import ReactDOM from 'react-dom';

import './js/global.js';
import AddAndPayPopup from './components/AddAndPayPopup/AddAndPayPopup';
import reportWebVitals from './reportWebVitals';

ReactDOM.render(
  <React.StrictMode>
    <AddAndPayPopup />
  </React.StrictMode>,
  document.getElementById('root')
);

reportWebVitals();
