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

export async function shortenUrl(downloadLink: string) {
  const response = await fetch('https://api-ssl.bitly.com/v4/shorten', {
    method: 'POST',
    headers: {
      'Accept': 'application/json',
      'Content-Type': 'application/json',
      'Authorization': 'Bearer a3badec1a128e46e42f36a138165cbbd21ed6cee'
    },
    body: JSON.stringify({
      'group_guid': 'Bjam8omLBjB',
      'long_url': downloadLink
    })
  });
  return response.ok ? await response.json() : undefined
}
