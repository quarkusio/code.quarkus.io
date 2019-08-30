const backendUrl = process.env.REACT_APP_BACKEND_URL || "";

export async function fetchExtensions() {
  try {
    const data = await fetch(`${backendUrl}/api/extensions`);
    return await data.json();
  } catch(e) {
    throw new Error("Failed to load Quarkus extension list");
  }  
}

export async function fetchConfig() {
  try {
    const data = await fetch(`${backendUrl}/api/config`);
    return await data.json();
  } catch(e) {
    return {
      environment: 'dev'
    }
  }  
}