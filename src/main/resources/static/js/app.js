import {Api,ApiError} from "./api.js";

const resources={
 vehicles:{label:"Véhicules",path:"/api/vehicules",admin:false},
 reservations:{label:"Réservations",path:"/api/reservations",admin:false},
 clients:{label:"Clients",path:"/api/clients",admin:true},
 agencies:{label:"Agences",path:"/api/agences",admin:true},
 contracts:{label:"Contrats",path:"/api/contrats",admin:true},
 payments:{label:"Paiements",path:"/api/paiements",admin:true},
 maintenance:{label:"Maintenance",path:"/api/maintenances",admin:true}
};

const App={
  root:document.getElementById("app"),
  page:"home",
  async init(){
    const saved=sessionStorage.getItem("autoloc_user");
    if(saved){Api.user=JSON.parse(saved);this.renderShell();return}
    this.login();
  },
  login(message=""){
    this.root.innerHTML=`<main class="login-page"><form class="login" id="loginForm">
      <div class="brand">AutoLoc</div><h1>Connexion</h1>
      <p class="muted">Accédez à votre espace de location.</p>
      ${message?`<div class="alert error">${message}</div>`:""}
      <div class="field"><label>Nom d'utilisateur</label><input id="username" autocomplete="username" required></div>
      <div class="field"><label>Mot de passe</label><input id="password" type="password" autocomplete="current-password" required></div>
      <button class="btn primary full" id="loginBtn">Se connecter</button>
    </form></main>`;
    document.getElementById("loginForm").onsubmit=async e=>{
      e.preventDefault();const b=document.getElementById("loginBtn");b.disabled=true;
      try{await Api.login(username.value,password.value);this.renderShell()}catch(err){Api.clear();b.disabled=false;this.login(err.status===401?"Identifiants incorrects.":err.message)}
    };
  },
  nav(){
    const visible=Object.entries(resources).filter(([,r])=>!r.admin||Api.user?.role==="ADMIN");
    return `<nav class="nav"><button class="ghost brand" data-page="home">AutoLoc</button>
      <div class="nav-links">${visible.map(([key,r])=>`<button class="nav-link" data-page="${key}">${r.label}</button>`).join("")}</div>
      <div class="nav-user"><div class="avatar">${Api.user?.username?.[0]?.toUpperCase()||"U"}</div><span class="user-name">${Api.user?.username} · ${Api.user?.role}</span><button class="btn secondary" id="logout">Quitter</button></div>
    </nav>`;
  },
  renderShell(){
    this.root.innerHTML=this.nav()+`<main class="container" id="page"></main><footer class="footer">AutoLoc · Spring Boot · MySQL</footer>`;
    document.querySelectorAll("[data-page]").forEach(b=>b.onclick=()=>this.go(b.dataset.page));
    logout.onclick=()=>{Api.clear();this.login()};
    this.go(this.page);
  },
  async go(page){
    this.page=page;document.querySelectorAll("[data-page]").forEach(b=>b.classList.toggle("active",b.dataset.page===page));
    const el=document.getElementById("page"); if(!el)return;
    if(page==="home")return this.home(el);
    if(page==="vehicles")return this.vehicles(el);
    return this.listResource(el,page);
  },
  async home(el){
    el.innerHTML=`<section class="hero"><h1>Louez votre véhicule simplement.</h1><p>AutoLoc centralise vos véhicules, réservations et opérations de location dans une interface claire et responsive.</p><div class="hero-actions"><button class="btn primary" onclick="App.go('vehicles')">Rechercher un véhicule</button><button class="btn secondary" onclick="App.go('reservations')">Voir les réservations</button></div></section>
    <div class="section-head"><div><h2>Votre espace</h2><p class="muted">Connecté en tant que ${Api.user.username} · rôle ${Api.user.role}</p></div></div>
    <div class="grid"><div class="card stat"><div><div class="muted small">Véhicules</div><strong>Catalogue</strong></div><div class="icon">🚗</div></div>
    <div class="card stat"><div><div class="muted small">Réservations</div><strong>Locations</strong></div><div class="icon">📅</div></div>
    <div class="card stat"><div><div class="muted small">Accès</div><strong>${Api.user.role}</strong></div><div class="icon">✓</div></div></div>
    <div class="section-head"><h2>État du service</h2></div><div class="card" id="health">Vérification de l'API…</div>`;
    try{const msg=await Api.request("/api/hello");health.innerHTML=`<div class="alert success">${msg}</div>`}
    catch(e){health.innerHTML=`<div class="alert error">HTTP ${e.status}: ${e.message}</div>`}
  },
  async vehicles(el){
    el.innerHTML=`<div class="section-head"><div><h2>Véhicules</h2><p class="muted">Consultez les véhicules réellement retournés par l'API.</p></div></div><div class="toolbar"><input class="input search" id="q" placeholder="Rechercher une marque, un modèle, une immatriculation…"><select class="select" id="status"><option value="">Tous les statuts</option><option>DISPONIBLE</option><option>LOUE</option><option>MAINTENANCE</option></select></div><div id="vehicleList" class="grid"><div class="loading">Chargement…</div></div>`;
    try{const data=await Api.request(resources.vehicles.path);this.vehicleCards(data||[]);q.oninput=()=>this.vehicleCards(data||[]);status.onchange=()=>this.vehicleCards(data||[])}
    catch(e){this.endpointUnavailable(vehicleList,e,"/api/vehicules")}
  },
  vehicleCards(data){
    const q=(document.getElementById("q")?.value||"").toLowerCase(),s=document.getElementById("status")?.value||"";
    const list=(Array.isArray(data)?data:[]).filter(v=>JSON.stringify(v).toLowerCase().includes(q)&&(!s||v.statut===s));
    const el=document.getElementById("vehicleList"); if(!list.length){el.innerHTML=`<div class="card empty">Aucun véhicule correspondant.</div>`;return}
    el.innerHTML=list.map(v=>`<article class="card vehicle-card"><div class="vehicle-top">🚗</div><h3>${v.marque||"Véhicule"} ${v.modele||""}</h3><div class="vehicle-meta"><span class="badge">${v.categorie||"—"}</span><span class="badge ${v.statut==="DISPONIBLE"?"ok":v.statut==="MAINTENANCE"?"danger":"warn"}">${v.statut||"—"}</span></div><div class="vehicle-bottom"><div class="price">${v.tarifJournalier??"—"} € <span>/ jour</span></div><button class="btn primary" onclick='App.vehicleDetail(${JSON.stringify(v).replaceAll("'","&#39;")})'>Détails</button></div></article>`).join("");
  },
  vehicleDetail(v){
    const el=document.getElementById("page");el.innerHTML=`<div class="section-head"><div><h2>${v.marque||""} ${v.modele||"Véhicule"}</h2><p class="muted">Fiche véhicule</p></div><button class="btn secondary" onclick="App.go('vehicles')">← Retour</button></div>
      <div class="detail"><div class="card detail-main"><div class="car">🚗</div><div class="detail-list">
      ${this.detailItem("Immatriculation",v.immatriculation)}${this.detailItem("Catégorie",v.categorie)}${this.detailItem("Statut",v.statut)}${this.detailItem("Tarif journalier",v.tarifJournalier?`${v.tarifJournalier} €`:"—")}${this.detailItem("Agence",v.agence?.nom||v.agence?.ville||v.agence)}${this.detailItem("Équipements",Array.isArray(v.equipements)?v.equipements.map(x=>x.libelle||x).join(", "):v.equipements)}
      </div></div><div class="card"><h3>Réserver ce véhicule</h3><p class="muted">La réservation sera disponible dès qu'un endpoint REST correspondant sera exposé par le backend.</p><button class="btn primary" disabled>Réserver</button></div></div>`;
  },
  detailItem(k,v){return `<div><span>${k}</span><strong>${v??"—"}</strong></div>`},
  async listResource(el,key){
    const r=resources[key];el.innerHTML=`<div class="section-head"><div><h2>${r.label}</h2><p class="muted">Données de l'API AutoLoc.</p></div></div><div id="resourceContent"><div class="loading">Chargement…</div></div>`;
    try{const data=await Api.request(r.path);this.table(document.getElementById("resourceContent"),data,r.label)}
    catch(e){this.endpointUnavailable(document.getElementById("resourceContent"),e,r.path)}
  },
  table(el,data,label){
    const rows=Array.isArray(data)?data:(data?[data]:[]);if(!rows.length){el.innerHTML=`<div class="card empty">Aucune donnée pour ${label.toLowerCase()}.</div>`;return}
    const keys=[...new Set(rows.flatMap(x=>Object.keys(x||{})))].slice(0,10);
    el.innerHTML=`<div class="table-wrap"><table><thead><tr>${keys.map(k=>`<th>${k}</th>`).join("")}</tr></thead><tbody>${rows.map(row=>`<tr>${keys.map(k=>`<td>${this.cell(row[k])}</td>`).join("")}</tr>`).join("")}</tbody></table></div>`;
  },
  cell(v){if(v===null||v===undefined)return"—";if(typeof v==="object")return Array.isArray(v)?v.map(x=>this.cell(x)).join(", "):Object.entries(v).map(([k,x])=>`${k}: ${this.cell(x)}`).join(" · ");return String(v)},
  endpointUnavailable(el,e,path){el.innerHTML=`<div class="card"><div class="alert warn"><strong>Fonctionnalité en attente du backend</strong><p>Le frontend n'invente aucune route. L'endpoint <code>${path}</code> n'est pas encore exposé par l'API actuelle.</p><p class="small">HTTP ${e.status||"?"} — ${e.message}</p></div></div>`}
};
window.App=App;App.init();
