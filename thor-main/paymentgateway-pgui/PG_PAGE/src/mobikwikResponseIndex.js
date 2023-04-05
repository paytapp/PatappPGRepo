import React from 'react';
import ReactDOM from 'react-dom';

import '../src/js/global';
import reportWebVitals from './reportWebVitals';
import MobikwikResponse from './components/ResponsePage/MobikwikResponse';

ReactDOM.render(
  <React.StrictMode>
    <MobikwikResponse />
  </React.StrictMode>,
  document.getElementById('root')
);

reportWebVitals();
