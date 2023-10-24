import React from 'react';
import ReactDOM from 'react-dom/client';
import 'react-app-polyfill/ie11';
import {CodeQuarkus, fetchConfig, fetchPlatform} from '../lib';
import './theme.scss';
import {RedHatHeader} from './header/redhat-header';

const PUBLIC_URL = window.PUBLIC_URL;
const CLIENT_NAME = window.location.hostname;
const REQUEST_OPTIONS = {headers: {'Client-Name': CLIENT_NAME}};

const tagsDef = [
    {
        name: 'code',
        color: '#be9100',
        description: 'This extension provides starter code (may not be available in all languages).'
    },
    {
        name: 'stable',
        hide: true
    },
    {
        name: 'supported',
        href: 'https://access.redhat.com/support/offerings/production/soc/',
        color: '#6AB983'
    },
    {
        name: 'supported-in-jvm',
        description: 'Support in JVM, means that this extension is tested and verified for usage in a Java Virtual Machine, while usage in Native is considered Technology Preview',
        color: '#6AB983'
    },
    {
        name: 'dev-support',
        href: 'https://access.redhat.com/support/offerings/production/soc/',
        color: '#6AB983'
    },
    {
        name: 'tech-preview',
        description: 'Technology Preview features provide early access to upcoming product innovations, enabling you to test functionality and provide feedback during the development process. However, these features are not fully supported under Red Hat Subscription Level Agreements.',
        color: '#4A97E8'
    },
    {
        name: 'deprecated',
        description: 'This feature is likely to be replaced or removed in a future version of Red Hat build of Quarkus. See release notes on docs.redhat.com for more information',
        color: '#ff004a'
    },
    {
        name: 'redhat-support:supported',
        href: 'https://access.redhat.com/support/offerings/production/soc/',
        color: '#6AB983'
    },
    {
        name: 'redhat-support:supported-in-jvm',
        description: 'Support in JVM, means that this extension is tested and verified for usage in a Java Virtual Machine, while usage in Native is considered Technology Preview',
        color: '#6AB983'
    },
    {
        name: 'redhat-support:dev-support',
        href: 'https://access.redhat.com/support/offerings/production/soc/',
        color: '#6AB983'
    },
    {
        name: 'redhat-support:tech-preview',
        description: 'Technology Preview features provide early access to upcoming product innovations, enabling you to test functionality and provide feedback during the development process. However, these features are not fully supported under Red Hat Subscription Level Agreements.',
        color: '#4A97E8'
    },
    {
        name: 'redhat-support:deprecated',
        description: 'This feature is likely to be replaced or removed in a future version of Red Hat build of Quarkus. See release notes on docs.redhat.com for more information',
        color: '#ff004a'
    },
    {
        name: 'status:preview',
        color: '#4695eb',
        description: 'This is work in progress. API or configuration properties might change as the extension matures. Give us your feedback :)'
    },
    {
        name: 'status:experimental',
        color: '#ff004a',
        description: 'Early feedback is requested to mature the idea. There is no guarantee of stability nor long term presence in the platform until the solution matures.'
    },
    {
        name: 'status:deprecated',
        color: '#707070',
        description: 'This extension has been deprecated. It is likely to be replaced or removed in a future version of Quarkus'
    },
    {
        name: 'with:starter-code',
        color: '#be9100',
        description: 'This extension provides starter code (may not be available in all languages).'
    },
    {
        name: 'status:stable',
        hide: true
    }
];


const api = {
    backendUrl: PUBLIC_URL,
    clientName: CLIENT_NAME,
    requestOptions: REQUEST_OPTIONS,
    tagsDef
};

const root = ReactDOM.createRoot(document.getElementById('root'));

root.render(
    <React.StrictMode>
        <CodeQuarkus api={api} configApi={fetchConfig} platformApi={fetchPlatform} header={RedHatHeader}/>
    </React.StrictMode>
);
