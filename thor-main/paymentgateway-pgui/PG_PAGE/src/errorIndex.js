import React from 'react';
import ReactDOM from 'react-dom';

import '../src/js/global';
import ErrorPage from './components/ErrorPage/ErrorPage';
import reportWebVitals from './reportWebVitals';
// import Response from './components/ResponsePage/Response';

ReactDOM.render(
  <React.StrictMode>
    <ErrorPage />
  </React.StrictMode>,
  document.getElementById('root')
);

reportWebVitals();
