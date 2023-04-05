import React from 'react';
import ReactDOM from 'react-dom';

import '../src/js/global';
import reportWebVitals from './reportWebVitals';
import AutoPayResponse from './components/AutoPayMandate/AutoPayResponse';

ReactDOM.render(
  <React.StrictMode>
    <AutoPayResponse />
  </React.StrictMode>,
  document.getElementById('root')
);

reportWebVitals();
