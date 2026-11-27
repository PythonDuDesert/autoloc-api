const Api = {
  credentials: null,
  user: null,

  setCredentials(username, password) {
    this.credentials = { username, password };
  },

  clear() {
    this.credentials = null;
    this.user = null;
    sessionStorage.removeItem("autoloc_user");
  },

  authHeader() {
    if (!this.credentials) return {};

    return {
      Authorization:
        "Basic " +
        btoa(
          unescape(
            encodeURIComponent(
              this.credentials.username + ":" + this.credentials.password
            )
          )
        ),
    };
  },

  async request(path, options = {}) {
    const headers = new Headers(options.headers || {});
    headers.set("Accept", "application/json");

    Object.entries(this.authHeader()).forEach(([key, value]) => {
      headers.set(key, value);
    });

    if (options.body && typeof options.body !== "string") {
      headers.set("Content-Type", "application/json");
      options.body = JSON.stringify(options.body);
    }

    const response = await fetch(path, { ...options, headers });

    let body = null;
    const text = await response.text();

    if (text) {
      try {
        body = JSON.parse(text);
      } catch {
        body = text;
      }
    }

    if (response.status === 401) {
      throw new ApiError(401, "Authentification requise.");
    }

    if (response.status === 403) {
      throw new ApiError(403, "Accès interdit pour votre rôle.");
    }

    if (!response.ok) {
      throw new ApiError(
        response.status,
        body?.message || body || "Erreur serveur."
      );
    }

    return body;
  },

  async login(username, password) {
    this.setCredentials(username, password);

    await this.request("/api/hello");

    let role = "USER";

    try {
      await this.request("/api/admin/report");
      role = "ADMIN";
    } catch (error) {
      if (error.status !== 403) throw error;
    }

    this.user = { username, role };
    sessionStorage.setItem("autoloc_user", JSON.stringify(this.user));
  },

  async register(username, password) {
    return this.request("/api/public/register", {
      method: "POST",
      body: { username, password },
    });
  },
};

class ApiError extends Error {
  constructor(status, message) {
    super(message);
    this.status = status;
  }
}

export { Api, ApiError };
