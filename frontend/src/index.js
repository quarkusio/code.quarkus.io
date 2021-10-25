import React from 'react';
import ReactDOM from 'react-dom';
import 'react-app-polyfill/ie11';
import reportWebVitals from './reportWebVitals';
import { CodeQuarkus, fetchConfig, fetchPlatform } from '@quarkusio/code-quarkus.components';
import './theme.scss';

const PUBLIC_URL = process.env.PUBLIC_URL && `${process.env.PUBLIC_URL}/`;
const BACKEND_URL = process.env.REACT_APP_BACKEND_URL || PUBLIC_URL;
const CLIENT_NAME = window.location.hostname;
const REQUEST_OPTIONS = { headers: { 'Client-Name': CLIENT_NAME } };

const api = {
    backendUrl: BACKEND_URL,
    clientName: CLIENT_NAME,
    requestOptions: REQUEST_OPTIONS
};

ReactDOM.render(
  <React.StrictMode>
    <CodeQuarkus api={api} configApi={fetchConfig} platformApi={fetchPlatform} />
  </React.StrictMode>,
  document.getElementById('root')
);

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();
