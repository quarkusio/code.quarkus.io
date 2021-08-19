export const PUBLIC_URL = process.env.PUBLIC_URL && `${process.env.PUBLIC_URL}/`;
export const BACKEND_URL = process.env.REACT_APP_BACKEND_URL || PUBLIC_URL;
export const CLIENT_NAME = window.location.hostname;
export const REQUEST_OPTIONS = { headers: { 'Client-Name': CLIENT_NAME } };