import React from 'react';
import ReactDOM from 'react-dom/client';
import 'react-app-polyfill/ie11';
import { CodeQuarkus, fetchConfig, fetchPlatform } from '../lib';
import './theme.scss';

const CLIENT_NAME = window.location.hostname;
const REQUEST_OPTIONS = { headers: { 'Client-Name': CLIENT_NAME } };

const api = {
    backendUrl: window.API_URL,
    clientName: CLIENT_NAME,
    requestOptions: REQUEST_OPTIONS
};

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
    <React.StrictMode>
        <CodeQuarkus api={api} configApi={fetchConfig} platformApi={fetchPlatform} />
    </React.StrictMode>
);


