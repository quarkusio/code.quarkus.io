const backendUrl = process.env.REACT_APP_BACKEND_URL || "";
export const CLIENT_NAME = 'Code Quarkus Frontend';
const headers = { headers: { 'Client-Name': CLIENT_NAME}};

export async function fetchExtensions() {
  try {
    const data = await fetch(`${backendUrl}/api/extensions`, headers);
    return await data.json();
  } catch(e) {
    throw new Error("Failed to load Quarkus extension list");
  }
}

export async function fetchConfig() {
  try {
    const data = await fetch(`${backendUrl}/api/config`, headers);
    return await data.json();
  } catch(e) {
    return {
      environment: 'dev'
    }
  }
}