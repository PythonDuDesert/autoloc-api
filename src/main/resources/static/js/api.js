const Api={
  credentials:null,
  user:null,
  setCredentials(username,password){this.credentials={username,password};},
  clear(){this.credentials=null;this.user=null;sessionStorage.removeItem("autoloc_user");},
  authHeader(){if(!this.credentials)return {};return {Authorization:"Basic "+btoa(unescape(encodeURIComponent(this.credentials.username+":"+this.credentials.password)))}},
  async request(path,options={}){
    const headers=new Headers(options.headers||{}); headers.set("Accept","application/json");
    Object.entries(this.authHeader()).forEach(([k,v])=>headers.set(k,v));
    if(options.body && typeof options.body!=="string"){headers.set("Content-Type","application/json");options.body=JSON.stringify(options.body)}
    const res=await fetch(path,{...options,headers});
    let body=null; const text=await res.text();
    if(text){try{body=JSON.parse(text)}catch{body=text}}
    if(res.status===401)throw new ApiError(401,"Authentification requise.");
    if(res.status===403)throw new ApiError(403,"Accès interdit pour votre rôle.");
    if(!res.ok)throw new ApiError(res.status,body?.message||body||"Erreur serveur.");
    return body;
  },
  async login(username,password){
    this.setCredentials(username,password);
    await this.request("/api/hello");
    let role="USER";
    try{await this.request("/api/admin/report");role="ADMIN"}catch(e){if(e.status!==403)throw e}
    this.user={username,role};sessionStorage.setItem("autoloc_user",JSON.stringify(this.user));
  }
};
class ApiError extends Error{constructor(status,message){super(message);this.status=status}}
export {Api,ApiError};
