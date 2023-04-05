import React from 'react';
import ReactDOM from 'react-dom';

import '../src/js/global';
import reportWebVitals from './reportWebVitals';
import InvoicePayPage from './components/InvoicePayPage/InvoicePayPage';

ReactDOM.render(
  <React.StrictMode>
    <InvoicePayPage />
  </React.StrictMode>,
  document.getElementById('root')
);

reportWebVitals();
