import { Api } from "./api.js";
const resources = {
  vehicles: { label: "Véhicules", path: "/api/vehicules", admin: false },
  myReservations: { label: "Mes réservations", path: "/api/mes-reservations", admin: false },
  reservations: { label: "Réservations", path: "/api/reservations", admin: true },
  clients: { label: "Clients", path: "/api/clients", admin: true },
  agencies: { label: "Agences", path: "/api/agences", admin: true },
  contracts: { label: "Contrats", path: "/api/contrats", admin: true },
  payments: { label: "Paiements", path: "/api/paiements", admin: true },
  maintenance: { label: "Maintenance", path: "/api/maintenances", admin: true },
};

const App = {
  root: document.getElementById("app"),
  page: "home",

  async init() {
    const saved = sessionStorage.getItem("autoloc_user");

    if (saved && Api.token) {
      Api.user = JSON.parse(saved);
      this.renderShell();
      return;
    }

    Api.clear();
    this.login();
  },

  login(message = "") {
    this.root.innerHTML = `
      <main class="login-page">
        <section class="login">
          <div class="brand">AutoLoc</div>

          <div class="auth-tabs">
            <button type="button" class="auth-tab active" id="loginTab">Connexion</button>
            <button type="button" class="auth-tab" id="registerTab">Inscription</button>
          </div>

          <div id="authContent"></div>
        </section>
      </main>
    `;

    const loginTab = document.getElementById("loginTab");
    const registerTab = document.getElementById("registerTab");

    const renderLogin = () => {
      loginTab.classList.add("active");
      registerTab.classList.remove("active");

      document.getElementById("authContent").innerHTML = `
        <h1>Connexion</h1>
        <p class="muted">Accédez à votre espace de location.</p>
        ${message ? `<div class="alert error">${message}</div>` : ""}

        <form id="loginForm">
          <div class="field">
            <label for="username">Nom d'utilisateur</label>
            <input id="username" autocomplete="username" required>
          </div>

          <div class="field">
            <label for="password">Mot de passe</label>
            <input id="password" type="password"
                   autocomplete="current-password" required>
          </div>

          <button class="btn primary full" id="loginBtn">
            Se connecter
          </button>
        </form>

        <p class="auth-help">
          Pas encore de compte ?
          <button type="button" class="link-button" id="goRegister">
            Créer un compte
          </button>
        </p>
      `;

      document.getElementById("loginForm").onsubmit = async (event) => {
        event.preventDefault();

        const button = document.getElementById("loginBtn");
        button.disabled = true;

        try {
          await Api.login(
            document.getElementById("username").value,
            document.getElementById("password").value
          );
          this.renderShell();
        } catch (error) {
          Api.clear();
          button.disabled = false;
          this.login(
            error.status === 401
              ? "Identifiants incorrects."
              : error.message
          );
        }
      };

      document.getElementById("goRegister").onclick = renderRegister;
    };

    const renderRegister = () => {
      loginTab.classList.remove("active");
      registerTab.classList.add("active");

      document.getElementById("authContent").innerHTML = `
        <h1>Créer un compte</h1>
        <p class="muted">
          Votre compte sera enregistré dans la base MySQL AutoLoc
          avec le rôle USER.
        </p>

        <div id="registerMessage"></div>

        <form id="registerForm">
          <div class="field">
            <label for="registerUsername">Nom d'utilisateur</label>
            <input id="registerUsername"
                   minlength="3" maxlength="50"
                   autocomplete="username" required>
          </div>

          <div class="field">
            <label for="registerPassword">Mot de passe</label>
            <input id="registerPassword"
                   type="password"
                   minlength="8"
                   autocomplete="new-password" required>
          </div>

          <div class="field">
            <label for="registerPasswordConfirm">Confirmation</label>
            <input id="registerPasswordConfirm"
                   type="password"
                   minlength="8"
                   autocomplete="new-password" required>
          </div>

          <button class="btn primary full" id="registerBtn">
            Créer mon compte
          </button>
        </form>

        <p class="auth-help">
          Déjà inscrit ?
          <button type="button" class="link-button" id="goLogin">
            Se connecter
          </button>
        </p>
      `;

      document.getElementById("registerForm").onsubmit = async (event) => {
        event.preventDefault();

        const username = document.getElementById("registerUsername").value.trim();
        const password = document.getElementById("registerPassword").value;
        const confirmation =
          document.getElementById("registerPasswordConfirm").value;
        const button = document.getElementById("registerBtn");
        const messageBox = document.getElementById("registerMessage");

        if (password !== confirmation) {
          messageBox.innerHTML =
            '<div class="alert error">Les mots de passe ne correspondent pas.</div>';
          return;
        }

        button.disabled = true;

        try {
          await Api.register(username, password);

          messageBox.innerHTML =
            '<div class="alert success">Compte créé avec succès. Vous pouvez maintenant vous connecter.</div>';

          document.getElementById("registerForm").reset();

          setTimeout(() => {
            renderLogin();
          }, 900);
        } catch (error) {
          button.disabled = false;

          messageBox.innerHTML =
            `<div class="alert error">${error.message}</div>`;
        }
      };

      document.getElementById("goLogin").onclick = renderLogin;
    };

    renderLogin();
    registerTab.onclick = renderRegister;
    loginTab.onclick = renderLogin;
  },

  nav() {
    const visible = Object.entries(resources)
      .filter(([, resource]) =>
        !resource.admin || Api.user?.role === "ADMIN"
      );

    return `
      <nav class="nav">
        <button class="ghost brand" data-page="home">AutoLoc</button>

        <div class="nav-links">
          ${visible.map(([key, resource]) =>
            `<button class="nav-link" data-page="${key}">
              ${resource.label}
            </button>`
          ).join("")}
        </div>

        <div class="nav-user">
          <div class="avatar">
            ${Api.user?.username?.[0]?.toUpperCase() || "U"}
          </div>
          <span class="user-name">
            ${Api.user?.username} · ${Api.user?.role}
          </span>
          <button class="btn secondary" id="logout">Quitter</button>
        </div>
      </nav>
    `;
  },

  renderShell() {
    this.root.innerHTML =
      this.nav() +
      `<main class="container" id="page"></main>
       <footer class="footer">AutoLoc · Spring Boot · MySQL</footer>`;

    document.querySelectorAll("[data-page]").forEach((button) => {
      button.onclick = () => this.go(button.dataset.page);
    });

    document.getElementById("logout").onclick = () => {
      Api.clear();
      this.login();
    };

    this.go(this.page);
  },

  async go(page) {
    this.page = page;

    document.querySelectorAll("[data-page]").forEach((button) => {
      button.classList.toggle("active", button.dataset.page === page);
    });

    const element = document.getElementById("page");
    if (!element) return;

    if (page === "home") return this.home(element);
    if (page === "vehicles") return this.vehicles(element);
    if (page === "myReservations") return this.myReservations(element);

    return this.listResource(element, page);
  },

  async home(element) {
    element.innerHTML = `
      <section class="hero">
        <h1>Louez votre véhicule simplement.</h1>
        <p>
          AutoLoc centralise vos véhicules, réservations et opérations
          de location dans une interface claire et responsive.
        </p>
      </section>

      <div class="section-head">
        <div>
          <h2>Votre espace</h2>
          <p class="muted">
            Connecté en tant que ${Api.user.username}
            · rôle ${Api.user.role}
          </p>
        </div>
      </div>

      <div class="grid">
        <div class="card stat">
          <div>
            <div class="muted small">Véhicules</div>
            <strong>Catalogue</strong>
          </div>
          <div class="icon">🚗</div>
        </div>

        <div class="card stat">
          <div>
            <div class="muted small">Réservations</div>
            <strong>Locations</strong>
          </div>
          <div class="icon">📅</div>
        </div>

        <div class="card stat">
          <div>
            <div class="muted small">Accès</div>
            <strong>${Api.user.role}</strong>
          </div>
          <div class="icon">✓</div>
        </div>
      </div>

      <div class="section-head">
        <h2>État du service</h2>
      </div>

      <div class="card" id="health">Vérification de l'API…</div>
    `;

    try {
      const message = await Api.request("/api/hello");
      document.getElementById("health").innerHTML =
        `<div class="alert success">${message}</div>`;
    } catch (error) {
      document.getElementById("health").innerHTML =
        `<div class="alert error">HTTP ${error.status}: ${error.message}</div>`;
    }
  },

  async vehicles(element) {
    element.innerHTML = `
      <div class="section-head">
        <div>
          <h2>Véhicules</h2>
          <p class="muted">
            Consultez les véhicules.
          </p>
        </div>
      </div>

      <div class="toolbar">
        <input class="input search" id="q"
               placeholder="Rechercher une marque, un modèle, une immatriculation…">
        <select class="select" id="status">
          <option value="">Tous les statuts</option>
          <option>DISPONIBLE</option>
          <option>LOUE</option>
          <option>MAINTENANCE</option>
        </select>
      </div>

      <div id="vehicleList" class="grid">
        <div class="loading">Chargement…</div>
      </div>
    `;

    try {
      const data = await Api.request(resources.vehicles.path);
      this.vehicleCards(data || []);
      document.getElementById("q").oninput =
        () => this.vehicleCards(data || []);
      document.getElementById("status").onchange =
        () => this.vehicleCards(data || []);
    } catch (error) {
      this.endpointUnavailable(
        document.getElementById("vehicleList"),
        error,
        "/api/vehicules"
      );
    }
  },

  vehicleCards(data) {
    const query =
      (document.getElementById("q")?.value || "").toLowerCase();
    const status =
      document.getElementById("status")?.value || "";

    const list = (Array.isArray(data) ? data : [])
      .filter((vehicle) =>
        JSON.stringify(vehicle).toLowerCase().includes(query)
      )
      .filter((vehicle) => !status || vehicle.statut === status);

    const element = document.getElementById("vehicleList");

    if (!list.length) {
      element.innerHTML =
        `<div class="card empty">Aucun véhicule correspondant.</div>`;
      return;
    }

    element.innerHTML = list.map((vehicle) => `
      <article class="card vehicle-card">
        <div class="vehicle-top">🚗${this.vehicleImg(vehicle)}</div>
        <h3>${vehicle.marque || "Véhicule"} ${vehicle.modele || ""}</h3>

        <div class="vehicle-meta">
          <span class="badge">${vehicle.categorie || "—"}</span>
          <span class="badge ${
            vehicle.statut === "DISPONIBLE"
              ? "ok"
              : vehicle.statut === "MAINTENANCE"
                ? "danger"
                : "warn"
          }">
            ${vehicle.statut || "—"}
          </span>
        </div>

        <div class="vehicle-bottom">
          <div class="price">
            ${vehicle.tarifJournalier ?? "—"} €
            <span>/ jour</span>
          </div>

          <button class="btn primary"
                  onclick='App.vehicleDetail(${JSON.stringify(vehicle)
                    .replaceAll("'", "&#39;")})'>
            Détails
          </button>
        </div>
      </article>
    `).join("");
  },

  vehicleDetail(vehicle) {
    const element = document.getElementById("page");

    element.innerHTML = `
      <div class="section-head">
        <div>
          <h2>${vehicle.marque || ""} ${vehicle.modele || "Véhicule"}</h2>
          <p class="muted">Fiche véhicule</p>
        </div>

        <button class="btn secondary" onclick="App.go('vehicles')">
          ← Retour
        </button>
      </div>

      <div class="detail">
        <div class="card detail-main">
           <div class="car">🚗${this.vehicleImg(vehicle)}</div>

          <div class="detail-list">
            ${this.detailItem("Immatriculation", vehicle.immatriculation)}
            ${this.detailItem("Catégorie", vehicle.categorie)}
            ${this.detailItem("Statut", vehicle.statut)}
            ${this.detailItem(
              "Tarif journalier",
              vehicle.tarifJournalier
                ? `${vehicle.tarifJournalier} €`
                : "—"
            )}
            ${this.detailItem(
              "Agence",
              vehicle.agence?.nom ||
              vehicle.agence?.ville ||
              vehicle.agence
            )}
            ${this.detailItem(
              "Équipements",
              Array.isArray(vehicle.equipements)
                ? vehicle.equipements
                    .map((item) => item.libelle || item)
                    .join(", ")
                : vehicle.equipements
            )}
          </div>
        </div>

        <div class="card">
          <h3>Réserver ce véhicule</h3>
          ${vehicle.statut === "MAINTENANCE"
            ? `<div class="alert error">Ce véhicule est en maintenance.</div>`
            : `
          <form id="reserveForm">
            <div class="field">
              <label for="dateDebut">Du</label>
              <input id="dateDebut" type="date" min="${this.today()}" required>
            </div>
            <div class="field">
              <label for="dateFin">Au</label>
              <input id="dateFin" type="date" min="${this.today()}" required>
            </div>
            <p class="muted" id="reservePrice">Choisissez vos dates.</p>
            <div id="reserveMessage"></div>
            <button class="btn primary full" id="reserveBtn">Réserver</button>
          </form>`}
        </div>
      </div>
    `;

    this.bindReserveForm(vehicle);
  },

  vehicleImg(vehicle) {
    const slug = `${vehicle.marque || ""}-${vehicle.modele || ""}`
        .toLowerCase()
        .normalize("NFD").replace(/[\u0300-\u036f]/g, "")
        .replace(/[^a-z0-9]+/g, "-")
        .replace(/^-|-$/g, "");
    return `<img src="/images/vehicules/${slug}.jpg"
                 alt="${vehicle.marque || ""} ${vehicle.modele || ""}"
                 loading="lazy" onerror="this.remove()">`;
  },
  today() {
    return new Date().toLocaleDateString("sv-SE"); // AAAA-MM-JJ, date locale
  },

  frDate(iso) {
    return iso ? iso.split("-").reverse().join("/") : "—";
  },

  statutLabel(statut) {
    return {
      En_ATTENTE: "En attente", EN_ATTENTE: "En attente",
      CONFIRMEE: "Confirmée", ANNULEE: "Annulée", TERMINEE: "Terminée",
    }[statut] || statut;
  },

  canCancel(r) {
    return ["En_ATTENTE", "EN_ATTENTE", "CONFIRMEE"].includes(r.statut) && this.today() < r.dateDebut;
  },

  bindReserveForm(vehicle) {
    const form = document.getElementById("reserveForm");
    if (!form) return;

    const debut = document.getElementById("dateDebut");
    const fin = document.getElementById("dateFin");
    const price = document.getElementById("reservePrice");
    const message = document.getElementById("reserveMessage");

    const update = () => {
      if (debut.value) fin.min = debut.value;
      if (!debut.value || !fin.value || fin.value < debut.value) {
        price.textContent = "Choisissez vos dates.";
        return;
      }
      const jours = (new Date(fin.value) - new Date(debut.value)) / 86400000 + 1;
      const total = (jours * Number(vehicle.tarifJournalier || 0)).toFixed(2);
      price.textContent = `${jours} jour(s) · total estimé ${total} €`;
    };
    debut.onchange = update;
    fin.onchange = update;

    form.onsubmit = async (event) => {
      event.preventDefault();
      const button = document.getElementById("reserveBtn");
      button.disabled = true;
      message.innerHTML = "";
      try {
        await Api.request("/api/mes-reservations", {
          method: "POST",
          body: { idVehicule: vehicle.idVehicule, dateDebut: debut.value, dateFin: fin.value },
        });
        this.go("myReservations");
      } catch (error) {
        button.disabled = false;
        message.innerHTML = `<div class="alert error">${error.message}</div>`;
      }
    };
  },

  async myReservations(element) {
    element.innerHTML = `
      <div class="section-head">
        <div>
          <h2>Mes réservations</h2>
          <p class="muted">Une seule réservation en cours à la fois. Annulation possible jusqu'à la veille du début.</p>
        </div>
        <button class="btn primary" onclick="App.go('vehicles')">Réserver un véhicule</button>
      </div>
      <div id="myList"><div class="loading">Chargement…</div></div>
    `;

    const list = document.getElementById("myList");
    try {
      const data = (await Api.request("/api/mes-reservations")) || [];
      if (!data.length) {
        list.innerHTML = `<div class="card empty">Aucune réservation pour le moment.</div>`;
        return;
      }

      list.innerHTML = `
        <div class="table-wrap">
          <table>
            <thead><tr><th>Véhicule</th><th>Du</th><th>Au</th><th>Statut</th><th></th></tr></thead>
            <tbody>
              ${data.map((r) => `
                <tr>
                  <td>${r.vehicule?.marque || ""} ${r.vehicule?.modele || ""}
                      <span class="muted small">${r.vehicule?.immatriculation || ""}</span></td>
                  <td>${this.frDate(r.dateDebut)}</td>
                  <td>${this.frDate(r.dateFin)}</td>
                  <td><span class="badge ${
                    r.statut === "ANNULEE" ? "danger" : r.statut === "CONFIRMEE" ? "ok" : r.statut === "TERMINEE" ? "" : "warn"
                  }">${this.statutLabel(r.statut)}</span></td>
                  <td>${this.canCancel(r)
                    ? `<button class="btn secondary" data-cancel="${r.idReservation}">Annuler</button>`
                    : ""}</td>
                </tr>`).join("")}
            </tbody>
          </table>
        </div>`;

      list.querySelectorAll("[data-cancel]").forEach((button) => {
        button.onclick = async () => {
          if (!confirm("Annuler cette réservation ?")) return;
          button.disabled = true;
          try {
            await Api.request(`/api/mes-reservations/${button.dataset.cancel}/annuler`, { method: "PATCH" });
            this.myReservations(element);
          } catch (error) {
            button.disabled = false;
            alert(error.message);
          }
        };
      });
    } catch (error) {
      this.endpointUnavailable(list, error, "/api/mes-reservations");
    }
  },

  detailItem(key, value) {
    return `
      <div>
        <span>${key}</span>
        <strong>${value ?? "—"}</strong>
      </div>
    `;
  },

  async listResource(element, key) {
    const resource = resources[key];

    element.innerHTML = `
      <div class="section-head">
        <div>
          <h2>${resource.label}</h2>
          <p class="muted">Données de l'API AutoLoc.</p>
        </div>
      </div>

      <div id="resourceContent">
        <div class="loading">Chargement…</div>
      </div>
    `;

    try {
      const data = await Api.request(resource.path);
      this.table(
        document.getElementById("resourceContent"),
        data,
        resource.label
      );
    } catch (error) {
      this.endpointUnavailable(
        document.getElementById("resourceContent"),
        error,
        resource.path
      );
    }
  },

  table(element, data, label) {
    const rows = Array.isArray(data) ? data : data ? [data] : [];

    if (!rows.length) {
      element.innerHTML =
        `<div class="card empty">
          Aucune donnée pour ${label.toLowerCase()}.
        </div>`;
      return;
    }

    const keys = [
      ...new Set(rows.flatMap((row) => Object.keys(row || {}))),
    ].slice(0, 10);

    element.innerHTML = `
      <div class="table-wrap">
        <table>
          <thead>
            <tr>${keys.map((key) => `<th>${key}</th>`).join("")}</tr>
          </thead>

          <tbody>
            ${rows.map((row) => `
              <tr>
                ${keys.map((key) =>
                  `<td>${this.cell(row[key])}</td>`
                ).join("")}
              </tr>
            `).join("")}
          </tbody>
        </table>
      </div>
    `;
  },

  cell(value) {
    if (value === null || value === undefined) return "—";

    if (typeof value === "object") {
      return Array.isArray(value)
        ? value.map((item) => this.cell(item)).join(", ")
        : Object.entries(value)
            .map(([key, item]) => `${key}: ${this.cell(item)}`)
            .join(" · ");
    }

    return String(value);
  },

  endpointUnavailable(element, error, path) {
    // 401 : identifiants absents ou expirés -> retour à la connexion (et non « endpoint manquant »)
    if (error.status === 401) {
      Api.clear();
      this.login("Votre session a expiré. Reconnectez-vous.");
      return;
    }

    if (error.status === 403) {
      element.innerHTML = `
        <div class="card">
          <div class="alert error">
            <strong>Accès interdit</strong>
            <p>Votre rôle ne permet pas de consulter cette ressource.</p>
          </div>
        </div>
      `;
      return;
    }

    if (error.status !== 404) {
      element.innerHTML = `
        <div class="card">
          <div class="alert error">
            <strong>Chargement impossible</strong>
            <p class="small">HTTP ${error.status || "?"} — ${error.message}</p>
          </div>
        </div>
      `;
      return;
    }

    element.innerHTML = `
      <div class="card">
        <div class="alert ${notFound ? "warn" : "error"}">
          <strong>${notFound ? "Fonctionnalité en attente du backend" : "Erreur lors du chargement"}</strong>
          <p>${notFound
            ? `L'endpoint <code>${path}</code> n'est pas encore exposé par l'API.`
            : `Impossible de charger <code>${path}</code>.`}</p>
          <p class="small">HTTP ${error.status || "?"} — ${error.message}</p>
        </div>
      </div>
    `;
  },
};

window.App = App;
App.init();
