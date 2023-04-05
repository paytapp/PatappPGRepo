import React from 'react';
import ReactDOM from 'react-dom';

import '../src/js/global';
import reportWebVitals from './reportWebVitals';
import AutoPayMandate from './components/AutoPayMandate/AutoPayMandate';

ReactDOM.render(
  <React.StrictMode>
    <AutoPayMandate />
  </React.StrictMode>,
  document.getElementById('root')
);

reportWebVitals();
