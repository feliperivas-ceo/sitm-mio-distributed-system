// ===== SITM-MIO App Principal =====

let currentUser = null;
let currentTab = 'mapa';
let zonas = [];
let rutas = [];

// --- Autenticación ---
document.getElementById('loginForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  const username = document.getElementById('loginUsername').value;
  const password = document.getElementById('loginPassword').value;
  try {
    const res = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password })
    });
    if (res.ok) {
      currentUser = await res.json();
      showDashboard();
    } else {
      const err = await res.json();
      document.getElementById('loginError').textContent = err.error || 'Error al iniciar sesión';
      document.getElementById('loginError').classList.remove('d-none');
    }
  } catch (ex) {
    document.getElementById('loginError').textContent = 'Error de conexión con el servidor';
    document.getElementById('loginError').classList.remove('d-none');
  }
});

async function checkSession() {
  try {
    const res = await fetch('/api/auth/me');
    if (res.ok) {
      currentUser = await res.json();
      showDashboard();
    }
  } catch (_) {}
}

function showDashboard() {
  document.getElementById('loginScreen').classList.add('d-none');
  document.getElementById('mainApp').classList.remove('d-none');
  document.getElementById('navUser').textContent = currentUser.nombre;

  const esAdmin = currentUser.rol === 'ADMIN';
  const badge = document.getElementById('navRolBadge');
  badge.textContent = esAdmin ? 'ADMIN' : 'CONTROLADOR';
  badge.className = 'badge ms-1 ' + (esAdmin ? 'bg-danger' : 'bg-success');

  // Navbar diferenciada por rol
  const navbar = document.querySelector('.navbar');
  if (navbar) {
    navbar.className = 'navbar navbar-dark px-3 py-2 ' + (esAdmin ? 'bg-danger' : 'bg-primary');
  }

  // Mostrar zona del controlador en navbar
  const navZona = document.getElementById('navZona');
  if (navZona) {
    if (!esAdmin && currentUser.zonaNombre) {
      navZona.textContent = '📍 ' + currentUser.zonaNombre;
      navZona.classList.remove('d-none');
    } else {
      navZona.classList.add('d-none');
    }
  }

  // Mostrar/ocultar elementos de admin
  document.querySelectorAll('.admin-only').forEach(el => {
    el.style.display = esAdmin ? '' : 'none';
  });

  // Ocultar tab de zona asignada para admin (tienen panel de admin zonas)
  const tabZonas = document.querySelector('[data-tab="zonas"]');
  if (tabZonas && esAdmin) {
    tabZonas.querySelector('.sidebar-label') && (tabZonas.querySelector('.sidebar-label').textContent = 'Admin Zonas');
  }

  mostrarBienvenida(currentUser.nombre, esAdmin);
  cargarRutas();
  cargarZonasFiltro();
  showTab('mapa');
}

function mostrarBienvenida(nombre, esAdmin) {
  const toast = document.createElement('div');
  toast.className = 'toast align-items-center text-white border-0 show position-fixed';
  toast.style.cssText = 'bottom:20px;right:20px;z-index:9999;min-width:260px;' +
    'background:' + (esAdmin ? '#C0392B' : '#27AE60');
  toast.innerHTML = `
    <div class="d-flex">
      <div class="toast-body">
        <i class="bi bi-${esAdmin ? 'shield-fill' : 'person-badge'} me-2"></i>
        Bienvenido, <strong>${nombre}</strong><br>
        <small>${esAdmin ? 'Acceso completo de administrador' : 'Vista de controlador activa'}</small>
      </div>
      <button type="button" class="btn-close btn-close-white me-2 m-auto" onclick="this.closest('.toast').remove()"></button>
    </div>`;
  document.body.appendChild(toast);
  setTimeout(() => toast.remove(), 4000);
}

async function logout() {
  await fetch('/api/auth/logout', { method: 'POST' });
  currentUser = null;
  document.getElementById('mainApp').classList.add('d-none');
  document.getElementById('loginScreen').classList.remove('d-none');
  document.getElementById('loginError').classList.add('d-none');
}

// --- Navegación entre tabs ---
function showTab(tab) {
  document.querySelectorAll('.tab-content').forEach(el => {
    el.classList.remove('active', 'd-flex');
    el.classList.add('d-none');
  });
  document.querySelectorAll('.sidebar-btn').forEach(el => el.classList.remove('active'));

  currentTab = tab;
  const tabEl = document.getElementById('tab' + capitalize(tab));
  if (tabEl) {
    tabEl.classList.remove('d-none');
    if (tab === 'mapa') tabEl.classList.add('active', 'd-flex');
    else tabEl.classList.add('active');
  }
  const btnEl = document.getElementById('btn' + capitalize(tab));
  if (btnEl) btnEl.classList.add('active');

  // Cargar datos según tab
  if (tab === 'mapa' && typeof initMap === 'function') {
    setTimeout(() => { if (window.mapaIniciado) window.mapa.invalidateSize(); else initMap(); }, 100);
  } else if (tab === 'analitica') {
    cargarAnalitica();
  } else if (tab === 'eventos') {
    cargarEventos();
    poblarFiltroEventos();
  } else if (tab === 'zonas') {
    cargarVistaZonas();
  } else if (tab === 'adminZonas') {
    cargarAdminZonas();
  } else if (tab === 'usuarios') {
    cargarUsuarios();
    cargarZonasParaSelect();
  }
}

function capitalize(str) {
  if (!str) return '';
  // Manejo especial para camelCase tabs
  const map = { adminZonas: 'AdminZonas' };
  return map[str] || str.charAt(0).toUpperCase() + str.slice(1);
}

// --- Rutas (para filtros y selectores) ---
async function cargarRutas() {
  try {
    const res = await fetch('/api/rutas');
    rutas = await res.json();
    const selMapa = document.getElementById('filtroRuta');
    const selEvento = document.getElementById('filtroEventoRuta');
    rutas.forEach(r => {
      [selMapa, selEvento].forEach(sel => {
        if (sel) {
          const opt = document.createElement('option');
          opt.value = r.id;
          opt.textContent = r.id + ' - ' + r.nombre;
          sel.appendChild(opt);
        }
      });
    });
  } catch (_) {}
}

// --- Zonas (para filtro del mapa - Mejora 4) ---
async function cargarZonasFiltro() {
  try {
    const res = await fetch('/api/zona/todas');
    if (!res.ok) return;
    const zonas = await res.json();
    const sel = document.getElementById('filtroZona');
    if (!sel) return;
    zonas.forEach(z => {
      const opt = document.createElement('option');
      opt.value = z.id;
      opt.textContent = z.nombre;
      sel.appendChild(opt);
    });
  } catch (_) {}
}

// --- Vista de Zonas (Controlador) - Mejora 5 ---
async function cargarVistaZonas() {
  const container = document.getElementById('zonaInfo');
  if (!currentUser) return;
  if (currentUser.rol === 'ADMIN') {
    container.innerHTML = cargarAdminZonasHTML();
    cargarAdminZonas();
    return;
  }
  try {
    const res = await fetch('/api/zona/mi-zona');
    const data = await res.json();
    if (!data.tieneZona) {
      container.innerHTML = '<div class="alert alert-info"><i class="bi bi-info-circle me-2"></i>No tienes zona asignada. Contacta al administrador.</div>';
      return;
    }
    const zona = data.zona;
    const buses = data.buses || [];
    const estaciones = data.estaciones || [];
    const stats = data.estadisticas || {};

    container.innerHTML = `
      <div class="card mb-3 border-primary shadow-sm">
        <div class="card-header bg-primary text-white fw-bold d-flex align-items-center gap-2">
          <i class="bi bi-geo-alt-fill"></i>
          <span>${zona.nombre}</span>
          <small class="ms-2 fw-normal opacity-75">${zona.descripcion || ''}</small>
        </div>
        <div class="card-body">
          <div class="row g-3">
            <div class="col-6 col-md-3">
              <div class="card text-center kpi-card primary h-100">
                <div class="card-body py-2">
                  <div class="fs-3 fw-bold text-primary">${stats.totalBuses || buses.length}</div>
                  <div class="text-muted small">Total buses</div>
                </div>
              </div>
            </div>
            <div class="col-6 col-md-3">
              <div class="card text-center kpi-card success h-100">
                <div class="card-body py-2">
                  <div class="fs-3 fw-bold text-success">${stats.busesActivos || 0}</div>
                  <div class="text-muted small">Buses activos</div>
                </div>
              </div>
            </div>
            <div class="col-6 col-md-3">
              <div class="card text-center kpi-card warning h-100">
                <div class="card-body py-2">
                  <div class="fs-3 fw-bold text-warning">${stats.totalEstaciones || estaciones.length}</div>
                  <div class="text-muted small">Estaciones</div>
                </div>
              </div>
            </div>
            <div class="col-6 col-md-3">
              <div class="card text-center kpi-card danger h-100">
                <div class="card-body py-2">
                  <div class="fs-3 fw-bold text-danger">${buses.filter(b => b.prioridad === 'ALTA').length}</div>
                  <div class="text-muted small">Alertas activas</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <h5 class="mt-3"><i class="bi bi-bus-front me-2"></i>Buses en mi zona</h5>
      <div class="table-responsive">
        <table class="table table-sm table-hover align-middle">
          <thead class="table-dark"><tr><th>ID</th><th>Placa</th><th>Ruta</th><th>Estado</th><th>Prioridad</th><th>Vel.</th></tr></thead>
          <tbody>${buses.map(b => `
            <tr class="${b.prioridad === 'ALTA' ? 'table-danger' : b.prioridad === 'MEDIA' ? 'table-warning' : ''}">
              <td><code>${b.id}</code></td>
              <td>${b.placa || b.numeroPlaca || '-'}</td>
              <td><span class="badge bg-secondary">${b.rutaId || '-'}</span></td>
              <td>${estadoBadge(b.estado)}</td>
              <td>${prioridadBadge(b.prioridad)}</td>
              <td>${b.velocidad ? b.velocidad.toFixed(1) + ' km/h' : '-'}</td>
            </tr>`).join('')}
          </tbody>
        </table>
      </div>
      <h5 class="mt-3"><i class="bi bi-signpost-2 me-2"></i>Estaciones / Paradas</h5>
      <div class="table-responsive">
        <table class="table table-sm table-hover">
          <thead class="table-dark"><tr><th>Nombre</th><th>Tipo</th><th>Ruta</th></tr></thead>
          <tbody>${estaciones.map(e => `
            <tr>
              <td>${e.nombre}</td>
              <td><span class="badge ${e.tipo === 'ESTACION_MAYOR' ? 'bg-dark' : 'bg-secondary'}">${e.tipo || '-'}</span></td>
              <td>${e.rutaId || '-'}</td>
            </tr>`).join('')}
          </tbody>
        </table>
      </div>`;
  } catch (ex) {
    container.innerHTML = '<div class="alert alert-danger"><i class="bi bi-exclamation-triangle me-2"></i>Error cargando información de zona</div>';
  }
}

// --- Admin Zonas ---
async function cargarAdminZonas() {
  try {
    const [zonasRes, rutasRes, usersRes] = await Promise.all([
      fetch('/api/zonas'),
      fetch('/api/rutas'),
      fetch('/api/users')
    ]);
    zonas = await zonasRes.json();
    rutas = await rutasRes.json();
    const users = await usersRes.json();
    const container = document.getElementById('zonasList');
    if (!container) return;
    const controladoresSinZona = users.filter(u => u.rol === 'CONTROLADOR' && !u.zona);
    container.innerHTML = zonas.map(z => {
      const controladores = users.filter(u => u.zona && u.zona.id === z.id);
      const optsCtrl = controladoresSinZona.map(c =>
        `<option value="${c.id}">${c.nombre} (${c.username})</option>`).join('');
      return `
        <div class="col-md-4">
          <div class="card zona-card shadow-sm h-100">
            <div class="card-header bg-primary text-white d-flex justify-content-between align-items-center">
              <strong>${z.nombre}</strong>
              <div>
                <button class="btn btn-sm btn-light me-1" onclick="editarZona(${z.id}, '${z.nombre}', '${(z.descripcion||'').replace(/'/g,'\\&#39;')}')">
                  <i class="bi bi-pencil"></i>
                </button>
                <button class="btn btn-sm btn-danger" onclick="eliminarZona(${z.id})">
                  <i class="bi bi-trash"></i>
                </button>
              </div>
            </div>
            <div class="card-body d-flex flex-column">
              <p class="text-muted small">${z.descripcion || 'Sin descripción'}</p>
              <div class="fw-bold mb-1"><i class="bi bi-people me-1"></i>Controladores (${controladores.length})</div>
              <div class="mb-2">
                ${controladores.length === 0 ? '<p class="text-muted small mb-1">Sin controladores asignados</p>' :
                  controladores.map(c => `
                    <span class="badge bg-info text-dark me-1 mb-1">
                      ${c.nombre}
                      <button class="btn-close btn-close-sm ms-1" style="font-size:.5rem" title="Desasignar"
                        onclick="desasignarControlador(${c.id})"></button>
                    </span>`).join('')}
              </div>
              ${optsCtrl ? `
              <div class="input-group input-group-sm mb-2">
                <select id="ctrlAsignar_${z.id}" class="form-select form-select-sm">
                  <option value="">Asignar controlador...</option>
                  ${optsCtrl}
                </select>
                <button class="btn btn-sm btn-outline-primary" onclick="asignarControlador(${z.id}, 'ctrlAsignar_${z.id}')">
                  <i class="bi bi-person-plus"></i>
                </button>
              </div>` : '<p class="text-muted small">Todos los controladores tienen zona</p>'}
              <div class="fw-bold mt-1 mb-1"><i class="bi bi-signpost-2 me-1"></i>Rutas en sistema</div>
              <div>${rutas.map(r => `<span class="badge me-1 mb-1" style="background:${r.color || '#666'}">${r.id}</span>`).join('')}</div>
            </div>
          </div>
        </div>`;
    }).join('');
  } catch (_) {}
}

function cargarAdminZonasHTML() { return ''; }

function abrirModalZona() {
  document.getElementById('zonaId').value = '';
  document.getElementById('zonaNombre').value = '';
  document.getElementById('zonaDesc').value = '';
  document.getElementById('zonaModalTitle').textContent = 'Nueva Zona';
  new bootstrap.Modal(document.getElementById('zonaModal')).show();
}

function editarZona(id, nombre, desc) {
  document.getElementById('zonaId').value = id;
  document.getElementById('zonaNombre').value = nombre;
  document.getElementById('zonaDesc').value = desc;
  document.getElementById('zonaModalTitle').textContent = 'Editar Zona';
  new bootstrap.Modal(document.getElementById('zonaModal')).show();
}

async function guardarZona() {
  const id = document.getElementById('zonaId').value;
  const body = {
    nombre: document.getElementById('zonaNombre').value,
    descripcion: document.getElementById('zonaDesc').value
  };
  const method = id ? 'PUT' : 'POST';
  const url = id ? `/api/zonas/${id}` : '/api/zonas';
  await fetch(url, { method, headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) });
  bootstrap.Modal.getInstance(document.getElementById('zonaModal')).hide();
  cargarAdminZonas();
}

async function eliminarZona(id) {
  if (!confirm('¿Eliminar esta zona?')) return;
  await fetch(`/api/zonas/${id}`, { method: 'DELETE' });
  cargarAdminZonas();
}

async function asignarControlador(zonaId, selectId) {
  const sel = document.getElementById(selectId);
  const userId = sel ? sel.value : '';
  if (!userId) return;
  try {
    const res = await fetch(`/api/zonas/${zonaId}/controladores/${userId}`, { method: 'POST' });
    const data = await res.json();
    alert(data.mensaje || 'Controlador asignado');
    cargarAdminZonas();
  } catch (_) { alert('Error asignando controlador'); }
}

async function desasignarControlador(userId) {
  if (!confirm('¿Desasignar este controlador de su zona?')) return;
  try {
    const res = await fetch(`/api/zonas/controladores/${userId}`, { method: 'DELETE' });
    const data = await res.json();
    alert(data.mensaje || 'Controlador desasignado');
    cargarAdminZonas();
  } catch (_) { alert('Error desasignando controlador'); }
}

// --- Gestión de Usuarios - Mejora 8 ---
async function cargarUsuarios() {
  try {
    const res = await fetch('/api/users');
    const users = await res.json();
    const tbody = document.getElementById('usuariosTable');
    tbody.innerHTML = users.map(u => {
      const esAdmin = u.rol === 'ADMIN';
      const activo = Boolean(u.activo);
      return `
      <tr class="${!activo ? 'table-secondary text-muted' : ''}">
        <td>${u.id}</td>
        <td><code>${u.username}</code></td>
        <td>${u.nombre || '-'}</td>
        <td class="small">${u.email || '-'}</td>
        <td>
          <span class="badge ${esAdmin ? 'bg-danger' : 'bg-primary'}">${u.rol}</span>
          <button class="btn btn-sm btn-link p-0 ms-1" title="Cambiar rol"
            onclick="cambiarRol(${u.id}, '${esAdmin ? 'CONTROLADOR' : 'ADMIN'}')">
            <i class="bi bi-arrow-left-right small"></i>
          </button>
        </td>
        <td>${u.zona ? '<span class="badge bg-secondary">' + u.zona.nombre + '</span>' : '<span class="text-muted small">-</span>'}</td>
        <td>
          <button class="btn btn-sm ${activo ? 'btn-success' : 'btn-outline-secondary'}"
            onclick="toggleActivo(${u.id}, ${activo})" title="${activo ? 'Desactivar' : 'Activar'}">
            <i class="bi bi-${activo ? 'check-circle-fill' : 'x-circle-fill'}"></i>
            ${activo ? 'Activo' : 'Inactivo'}
          </button>
        </td>
        <td>
          <button class="btn btn-sm btn-outline-primary me-1" title="Editar"
            onclick="editarUsuario(${JSON.stringify(u).replace(/"/g,'&quot;')})">
            <i class="bi bi-pencil"></i>
          </button>
          <button class="btn btn-sm btn-outline-danger" title="Eliminar"
            onclick="eliminarUsuario(${u.id}, '${u.username}')">
            <i class="bi bi-trash"></i>
          </button>
        </td>
      </tr>`;
    }).join('');
  } catch (_) {}
}

async function toggleActivo(userId, estaActivo) {
  try {
    const res = await fetch(`/api/users/${userId}/activo`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ activo: !estaActivo })
    });
    if (res.ok) cargarUsuarios();
  } catch (_) {}
}

async function cambiarRol(userId, nuevoRol) {
  if (!confirm(`¿Cambiar rol a ${nuevoRol}?`)) return;
  try {
    const res = await fetch(`/api/users/${userId}/rol`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ rol: nuevoRol })
    });
    if (res.ok) cargarUsuarios();
  } catch (_) {}
}

async function cargarZonasParaSelect() {
  try {
    const res = await fetch('/api/zonas');
    zonas = await res.json();
    const sel = document.getElementById('uZona');
    sel.innerHTML = '<option value="">Sin zona</option>';
    zonas.forEach(z => {
      const o = document.createElement('option');
      o.value = z.id; o.textContent = z.nombre;
      sel.appendChild(o);
    });
  } catch (_) {}
}

function abrirModalUsuario() {
  document.getElementById('usuarioId').value = '';
  document.getElementById('uUsername').value = '';
  document.getElementById('uPassword').value = '';
  document.getElementById('uNombre').value = '';
  document.getElementById('uEmail').value = '';
  document.getElementById('uRol').value = 'CONTROLADOR';
  document.getElementById('uZona').value = '';
  document.getElementById('usuarioModalTitle').textContent = 'Nuevo Usuario';
  cargarZonasParaSelect();
  new bootstrap.Modal(document.getElementById('usuarioModal')).show();
}

function editarUsuario(u) {
  document.getElementById('usuarioId').value = u.id;
  document.getElementById('uUsername').value = u.username;
  document.getElementById('uPassword').value = '';
  document.getElementById('uNombre').value = u.nombre || '';
  document.getElementById('uEmail').value = u.email || '';
  document.getElementById('uRol').value = u.rol;
  document.getElementById('uZona').value = u.zona ? u.zona.id : '';
  document.getElementById('usuarioModalTitle').textContent = 'Editar Usuario';
  cargarZonasParaSelect();
  new bootstrap.Modal(document.getElementById('usuarioModal')).show();
}

async function guardarUsuario() {
  const id = document.getElementById('usuarioId').value;
  const body = {
    username: document.getElementById('uUsername').value,
    password: document.getElementById('uPassword').value,
    nombre:   document.getElementById('uNombre').value,
    email:    document.getElementById('uEmail').value,
    rol:      document.getElementById('uRol').value,
    zonaId:   document.getElementById('uZona').value || null
  };
  const method = id ? 'PUT' : 'POST';
  const url = id ? `/api/users/${id}` : '/api/users';
  const res = await fetch(url, { method, headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) });
  if (res.ok) {
    bootstrap.Modal.getInstance(document.getElementById('usuarioModal')).hide();
    cargarUsuarios();
  } else {
    const err = await res.json();
    alert(err.error || 'Error al guardar usuario');
  }
}

async function eliminarUsuario(id, username) {
  if (!confirm(`¿Eliminar usuario "${username}"?`)) return;
  await fetch(`/api/users/${id}`, { method: 'DELETE' });
  cargarUsuarios();
}

// --- Historial de Eventos - Mejora 11 ---
async function cargarEventos() {
  const rutaId = document.getElementById('filtroEventoRuta')?.value || '';
  const prioridad = document.getElementById('filtroEventoPrioridad')?.value || '';
  const tipoEvento = document.getElementById('filtroEventoTipo')?.value || '';
  const busId = (document.getElementById('filtroEventoBus')?.value || '').trim();
  try {
    // Filtros por query params (server-side) para mejor rendimiento
    const params = new URLSearchParams();
    if (rutaId) params.append('rutaId', rutaId);
    if (prioridad) params.append('prioridad', prioridad);
    if (tipoEvento) params.append('tipoEvento', tipoEvento);
    if (busId) params.append('busId', busId);
    const res = await fetch('/api/eventos?' + params.toString());
    let eventos = await res.json();

    const tbody = document.getElementById('eventosTable');
    const empty = document.getElementById('eventosEmpty');
    if (eventos.length === 0) {
      tbody.innerHTML = '';
      if (empty) empty.classList.remove('d-none');
      return;
    }
    if (empty) empty.classList.add('d-none');
    tbody.innerHTML = eventos.map(e => `
      <tr class="event-${e.prioridad ? e.prioridad.toLowerCase() : 'baja'}">
        <td class="small">${e.timestamp || '-'}</td>
        <td><code>${e.busId || '-'}</code>${e.busPlaca ? '<br><small class="text-muted">' + e.busPlaca + '</small>' : ''}</td>
        <td><span class="badge bg-secondary">${e.rutaId || '-'}</span></td>
        <td>${tipoEventoBadge(e.tipoEvento)}</td>
        <td>${prioridadBadge(e.prioridad)}</td>
        <td class="small">${e.latitud ? (+e.latitud).toFixed(4) : '-'}</td>
        <td class="small">${e.longitud ? (+e.longitud).toFixed(4) : '-'}</td>
        <td>${e.estadoProcesado
          ? '<span class="badge bg-success"><i class="bi bi-check-circle-fill me-1"></i>Procesado</span>'
          : '<span class="badge bg-warning text-dark"><i class="bi bi-clock me-1"></i>Pendiente</span>'}</td>
      </tr>`).join('');
  } catch (_) {}
}

function limpiarFiltrosEventos() {
  ['filtroEventoRuta', 'filtroEventoPrioridad', 'filtroEventoTipo'].forEach(id => {
    const el = document.getElementById(id);
    if (el) el.value = '';
  });
  const busEl = document.getElementById('filtroEventoBus');
  if (busEl) busEl.value = '';
  cargarEventos();
}

async function poblarFiltroEventos() {
  const sel = document.getElementById('filtroEventoRuta');
  if (!sel || sel.options.length > 1) return;
  try {
    const res = await fetch('/api/rutas');
    const rs = await res.json();
    rs.forEach(r => {
      const o = document.createElement('option');
      o.value = r.id; o.textContent = r.id;
      sel.appendChild(o);
    });
  } catch (_) {}
}

// --- Helpers de UI ---
function estadoBadge(estado) {
  const map = { ACTIVO: 'bg-success', EN_RUTA: 'bg-primary', EN_PARADA: 'bg-warning text-dark', INACTIVO: 'bg-secondary' };
  return `<span class="badge ${map[estado] || 'bg-secondary'}">${estado || '-'}</span>`;
}

function prioridadBadge(prioridad) {
  const map = { ALTA: 'bg-danger', MEDIA: 'bg-warning text-dark', BAJA: 'bg-success' };
  return `<span class="badge ${map[prioridad] || 'bg-secondary'}">${prioridad || '-'}</span>`;
}

function tipoEventoBadge(tipo) {
  const map = {
    ACCIDENTE: 'alert-accidente',
    CONGESTION: 'alert-congestion',
    GPS_NORMAL: 'alert-gps',
    MOTOR: 'alert-media',
    PUERTAS: 'alert-media',
    OTRO: 'bg-secondary'
  };
  return `<span class="alert-badge ${map[tipo] || 'bg-secondary'}">${tipo || '-'}</span>`;
}

// --- Alertas críticas visuales - Mejora 12 ---
// Código de color: 🚨 Rojo=ACCIDENTE | 🟠 Naranja=CONGESTION | 🟡 Amarillo=MEDIA | 🟢 Verde=GPS_NORMAL

let alertasMostradas = new Set();

function mostrarAlertaCritica(bus) {
  // Evitar duplicados dentro de 30 segundos
  const key = bus.id + '_' + (bus.ultimoEvento || bus.prioridad);
  if (alertasMostradas.has(key)) return;
  alertasMostradas.add(key);
  setTimeout(() => alertasMostradas.delete(key), 30000);

  let container = document.getElementById('alertasContainer');
  if (!container) {
    container = document.createElement('div');
    container.id = 'alertasContainer';
    document.body.appendChild(container);
  }

  const { clase, icono, titulo, color } = clasificarAlerta(bus);

  const toast = document.createElement('div');
  toast.className = `alert alerta-toast ${clase} d-flex align-items-start gap-2 py-2 px-3`;
  toast.style.borderLeftColor = color;
  toast.innerHTML = `
    <span style="font-size:1.3rem">${icono}</span>
    <div class="flex-fill">
      <strong style="color:${color}">${titulo}</strong> — Bus <code>${bus.id}</code><br>
      <small class="text-muted">
        Ruta: ${bus.rutaId || '?'} | Estado: ${bus.estado || '-'} | Evento: ${bus.ultimoEvento || '-'}
      </small>
    </div>
    <button type="button" class="btn-close" onclick="this.parentElement.remove()"></button>`;
  container.prepend(toast);

  // Autolimpieza: 8s para accidentes, 12s para otros
  const duracion = titulo === 'ACCIDENTE' ? 8000 : 12000;
  setTimeout(() => { if (toast.parentElement) toast.remove(); }, duracion);
}

function clasificarAlerta(bus) {
  const evento = bus.ultimoEvento;
  const prioridad = bus.prioridad;
  if (evento === 'ACCIDENTE') {
    return { clase: 'alerta-accidente', icono: '🚨', titulo: 'ACCIDENTE', color: '#C0392B' };
  }
  if (prioridad === 'ALTA') {
    return { clase: 'alerta-alto', icono: '🔴', titulo: 'PRIORIDAD ALTA', color: '#E74C3C' };
  }
  if (evento === 'CONGESTION' || prioridad === 'MEDIA') {
    return { clase: 'alerta-congestion', icono: '🟠', titulo: 'CONGESTIÓN', color: '#E67E22' };
  }
  return { clase: 'alerta-media', icono: '🟡', titulo: evento || 'EVENTO', color: '#F39C12' };
}

/** Cargar y mostrar el panel de alertas activas */
async function mostrarPanelAlertas() {
  const panel = document.getElementById('alertasPanel');
  const body = document.getElementById('alertasPanelBody');
  if (!panel || !body) return;
  try {
    const res = await fetch('/api/alertas/criticas');
    const alertas = await res.json();
    panel.classList.remove('d-none');
    if (alertas.length === 0) {
      body.innerHTML = '<div class="p-2 text-muted text-center small">Sin alertas críticas activas</div>';
      return;
    }
    body.innerHTML = alertas.map(a => `
      <div class="alerta-item">
        <span>${a.iconoAlerta}</span>
        <div>
          <strong style="color:${a.colorAlerta}">${a.nivelAlerta}</strong>
          <span class="ms-1">Bus ${a.busId}</span><br>
          <small class="text-muted">Ruta: ${a.rutaId || '-'} | ${a.ultimoEvento || '-'}</small>
        </div>
      </div>`).join('');
  } catch (_) {}
}

/** Actualizar el contador de alertas en la navbar */
async function actualizarContadorAlertas() {
  try {
    const res = await fetch('/api/alertas/resumen');
    const data = await res.json();
    const total = (data.critico || 0) + (data.alto || 0);
    const badge = document.getElementById('contadorAlertas');
    const num = document.getElementById('numAlertas');
    if (badge && num) {
      num.textContent = total;
      if (total > 0) {
        badge.classList.remove('d-none');
        badge.className = total > 0 ? 'badge bg-danger me-1' : 'badge bg-warning text-dark me-1';
      } else {
        badge.classList.add('d-none');
      }
    }
  } catch (_) {}
}

// Inicio
checkSession();

// Actualizar contador de alertas cada 10 segundos cuando está autenticado
setInterval(() => {
  if (currentUser) actualizarContadorAlertas();
}, 10000);
