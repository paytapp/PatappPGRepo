import React from 'react';
import ReactDOM from 'react-dom';

import '../src/js/global';
import reportWebVitals from './reportWebVitals';
import UpiMerchantHosted from './components/UpiMerchantHosted/UpiMerchantHosted';

ReactDOM.render(
  <React.StrictMode>
    <UpiMerchantHosted />
  </React.StrictMode>,
  document.getElementById('root')
);

reportWebVitals();
