import React from 'react';
import ReactDOM from 'react-dom';

import '../src/js/global';
import reportWebVitals from './reportWebVitals';
import CheckoutResponse from './components/ResponsePage/CheckoutResponse';

ReactDOM.render(
  <React.StrictMode>
    <CheckoutResponse />
  </React.StrictMode>,
  document.getElementById('root')
);

reportWebVitals();
