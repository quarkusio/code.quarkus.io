import React from 'react';
import { CodeQuarkus } from './code-quarkus';
import { Api, fetchConfig, fetchPlatform } from './api/code-quarkus-api';

const api: Api = {
  backendUrl: 'http://localhost:8080',
  clientName: 'test',
  requestOptions: { headers: { 'Client-Name': 'test' } }
};

export const CodeQuarkusDemo = () => {
  return <CodeQuarkus api={api} platformApi={fetchPlatform} configApi={fetchConfig} />;
};