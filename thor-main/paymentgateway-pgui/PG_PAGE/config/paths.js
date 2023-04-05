'use strict';

const path = require('path');
const fs = require('fs');
const getPublicUrlOrPath = require('react-dev-utils/getPublicUrlOrPath');

// Make sure any symlinks in the project folder are resolved:
// https://github.com/facebook/create-react-app/issues/637
const appDirectory = fs.realpathSync(process.cwd());
const resolveApp = relativePath => path.resolve(appDirectory, relativePath);

// We use `PUBLIC_URL` environment variable or "homepage" field to infer
// "public path" at which the app is served.
// webpack needs to know it to put the right <script> hrefs into HTML even in
// single-page apps that may serve index.html for nested URLs like /todos/42.
// We can't use a relative path in HTML because we don't want to load something
// like /todos/42/static/js/bundle.7289d.js. We have to know the root.
const publicUrlOrPath = getPublicUrlOrPath(
  process.env.NODE_ENV === 'development',
  require(resolveApp('package.json')).homepage,
  process.env.PUBLIC_URL
);

const buildPath = process.env.BUILD_PATH || 'build';

const moduleFileExtensions = [
  'web.mjs',
  'mjs',
  'web.js',
  'js',
  'web.ts',
  'ts',
  'web.tsx',
  'tsx',
  'json',
  'web.jsx',
  'jsx',
];

// Resolve file paths in the same order as webpack
const resolveModule = (resolveFn, filePath) => {
  const extension = moduleFileExtensions.find(extension =>
    fs.existsSync(resolveFn(`${filePath}.${extension}`))
  );

  if (extension) {
    return resolveFn(`${filePath}.${extension}`);
  }

  return resolveFn(`${filePath}.js`);
};

// config after eject: we're in ./config/
module.exports = {
  dotenv: resolveApp('.env'),
  appPath: resolveApp('.'),
  appBuild: resolveApp(buildPath),
  appPublic: resolveApp('public'),
  appPgHtml: resolveApp('public/index.html'),
  appResponseHtml: resolveApp('public/response.html'),
  appCheckoutResponseHtml: resolveApp('public/checkoutResponse.html'),
  appMobikwikResponseHtml: resolveApp('public/mobikwikResponse.html'),
  appUpiMerchantHostedHtml: resolveApp('public/upiMerchantHosted.html'),
  appErrorHtml: resolveApp('public/error.html'),
  appInvoiceHtml: resolveApp('public/invoicePay.html'),
  appEposPayHtml: resolveApp('public/eposPay.html'),
  appAutoPayMandateHtml: resolveApp('public/autoPayMandate.html'),
  appAutoPayResponseHtml: resolveApp('public/upiAutoPayResponse.html'),
  appStaticPgQrResponseHtml: resolveApp('public/staticPgQrResponse.html'),
  eNachRegistrationHtml: resolveApp('public/eNachRegistration.html'),
  eNachResponseHtml: resolveApp('public/iciciEnachResponse.html'),
  appIndexJs: resolveModule(resolveApp, 'src/index'),
  appResponseJs: resolveModule(resolveApp, 'src/responseIndex'),
  appCheckoutResponseJs: resolveModule(resolveApp, 'src/checkoutResponseIndex'),
  appMobikwikResponseJs: resolveModule(resolveApp, 'src/mobikwikResponseIndex'),
  appUpiMerchantHostedJs: resolveModule(resolveApp, 'src/upiHostedIndex'),
  appErrorJs: resolveModule(resolveApp, 'src/errorIndex'),
  appInvoiceJs: resolveModule(resolveApp, 'src/invoicePayIndex'),
  appEposPayJs: resolveModule(resolveApp, 'src/eposPayIndex'),
  appAutoPayMandateJs: resolveModule(resolveApp, 'src/autoPayMandateIndex'),
  appAutoPayResponseJs: resolveModule(resolveApp, 'src/autoPayResponseIndex'),
  appStaticPgQrResponseJs: resolveModule(resolveApp, 'src/staticPgQrResponseIndex'),
  eNachRegistrationJs: resolveModule(resolveApp, 'src/eNachRegistrationIndex'),
  eNachResponseJs: resolveModule(resolveApp, 'src/iciciEnachResponseIndex'),
  addAndPayPopupJs: resolveModule(resolveApp, 'src/addAndPayPopupIndex'),
  appPackageJson: resolveApp('package.json'),
  appSrc: resolveApp('src'),
  appTsConfig: resolveApp('tsconfig.json'),
  appJsConfig: resolveApp('jsconfig.json'),
  yarnLockFile: resolveApp('yarn.lock'),
  testsSetup: resolveModule(resolveApp, 'src/setupTests'),
  proxySetup: resolveApp('src/setupProxy.js'),
  appNodeModules: resolveApp('node_modules'),
  swSrc: resolveModule(resolveApp, 'src/service-worker'),
  publicUrlOrPath,
};



module.exports.moduleFileExtensions = moduleFileExtensions;
