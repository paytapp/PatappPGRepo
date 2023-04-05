import React from 'react';
import ReactDOM from 'react-dom';

import '../src/js/global';
import reportWebVitals from './reportWebVitals';
import Response from './components/ResponsePage/Response';

ReactDOM.render(
  <React.StrictMode>
    <Response />
  </React.StrictMode>,
  document.getElementById('root')
);

reportWebVitals();
