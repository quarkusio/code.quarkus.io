const backendUrl = process.env.REACT_APP_BACKEND_URL || "";
const headers = { headers: { 'Client-Name': 'Code Quarkus Frontend'}};

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