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
  const badge = document.getElementById('navRolBadge');
  badge.textContent = currentUser.rol;
  badge.className = 'badge ms-1 ' + (currentUser.rol === 'ADMIN' ? 'bg-danger' : 'bg-info');

  // Mostrar/ocultar elementos de admin
  document.querySelectorAll('.admin-only').forEach(el => {
    el.style.display = currentUser.rol === 'ADMIN' ? '' : 'none';
  });

  cargarRutas();
  showTab('mapa');
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

// --- Vista de Zonas (Controlador) ---
async function cargarVistaZonas() {
  const container = document.getElementById('zonaInfo');
  if (!currentUser) return;
  if (currentUser.rol === 'ADMIN') {
    container.innerHTML = cargarAdminZonasHTML();
    cargarAdminZonas();
    return;
  }
  if (!currentUser.zonaId) {
    container.innerHTML = '<div class="alert alert-info">No tienes zona asignada. Contacta al administrador.</div>';
    return;
  }
  try {
    const [busRes, estRes] = await Promise.all([
      fetch('/api/buses/filter?rutaId='),
      fetch('/api/estaciones')
    ]);
    const buses = await busRes.json();
    const estaciones = await estRes.json();

    container.innerHTML = `
      <div class="card mb-3 border-primary">
        <div class="card-header bg-primary text-white fw-bold">
          <i class="bi bi-geo-alt-fill me-2"></i>${currentUser.zonaNombre || 'Mi Zona'}
        </div>
        <div class="card-body">
          <div class="row g-3">
            <div class="col-md-4">
              <div class="card text-center kpi-card primary">
                <div class="card-body">
                  <div class="fs-2 fw-bold text-primary">${buses.length}</div>
                  <div class="text-muted small">Buses en zona</div>
                </div>
              </div>
            </div>
            <div class="col-md-4">
              <div class="card text-center kpi-card success">
                <div class="card-body">
                  <div class="fs-2 fw-bold text-success">${buses.filter(b => b.estado === 'ACTIVO' || b.estado === 'EN_RUTA').length}</div>
                  <div class="text-muted small">Buses activos</div>
                </div>
              </div>
            </div>
            <div class="col-md-4">
              <div class="card text-center kpi-card warning">
                <div class="card-body">
                  <div class="fs-2 fw-bold text-warning">${estaciones.length}</div>
                  <div class="text-muted small">Estaciones</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <h5 class="mt-3">Buses en zona</h5>
      <div class="table-responsive">
        <table class="table table-sm table-hover">
          <thead class="table-dark"><tr><th>ID</th><th>Placa</th><th>Ruta</th><th>Estado</th><th>Prioridad</th></tr></thead>
          <tbody>${buses.slice(0,10).map(b => `
            <tr>
              <td><code>${b.id}</code></td>
              <td>${b.numeroPlaca}</td>
              <td>${b.rutaId || '-'}</td>
              <td>${estadoBadge(b.estado)}</td>
              <td>${prioridadBadge(b.prioridad)}</td>
            </tr>`).join('')}
          </tbody>
        </table>
      </div>
      <h5 class="mt-3">Estaciones / Paradas</h5>
      <div class="table-responsive">
        <table class="table table-sm table-hover">
          <thead class="table-dark"><tr><th>Nombre</th><th>Tipo</th><th>Ruta</th></tr></thead>
          <tbody>${estaciones.map(e => `
            <tr>
              <td>${e.nombre}</td>
              <td><span class="badge ${e.tipo === 'ESTACION_MAYOR' ? 'bg-dark' : 'bg-secondary'}">${e.tipo}</span></td>
              <td>${e.ruta ? e.ruta.id : '-'}</td>
            </tr>`).join('')}
          </tbody>
        </table>
      </div>`;
  } catch (ex) {
    container.innerHTML = '<div class="alert alert-danger">Error cargando información de zona</div>';
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
    container.innerHTML = zonas.map(z => {
      const controladores = users.filter(u => u.zona && u.zona.id === z.id);
      return `
        <div class="col-md-4">
          <div class="card zona-card shadow-sm h-100">
            <div class="card-header bg-primary text-white d-flex justify-content-between">
              <strong>${z.nombre}</strong>
              <div>
                <button class="btn btn-sm btn-light me-1" onclick="editarZona(${z.id}, '${z.nombre}', '${z.descripcion || ''}')">
                  <i class="bi bi-pencil"></i>
                </button>
                <button class="btn btn-sm btn-danger" onclick="eliminarZona(${z.id})">
                  <i class="bi bi-trash"></i>
                </button>
              </div>
            </div>
            <div class="card-body">
              <p class="text-muted small">${z.descripcion || 'Sin descripción'}</p>
              <div class="fw-bold mb-2"><i class="bi bi-people me-1"></i>Controladores (${controladores.length})</div>
              ${controladores.length === 0 ? '<p class="text-muted small">Sin controladores asignados</p>' :
                controladores.map(c => `<span class="badge bg-info text-dark me-1 mb-1">${c.nombre}</span>`).join('')}
              <div class="fw-bold mt-2 mb-1"><i class="bi bi-signpost-2 me-1"></i>Rutas asociadas</div>
              ${rutas.map(r => `<span class="badge me-1 mb-1" style="background:${r.color}">${r.id}</span>`).join('')}
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

// --- Gestión de Usuarios ---
async function cargarUsuarios() {
  try {
    const res = await fetch('/api/users');
    const users = await res.json();
    const tbody = document.getElementById('usuariosTable');
    tbody.innerHTML = users.map(u => `
      <tr>
        <td>${u.id}</td>
        <td><code>${u.username}</code></td>
        <td>${u.nombre}</td>
        <td>${u.email || '-'}</td>
        <td>${u.rol === 'ADMIN' ? '<span class="badge bg-danger">ADMIN</span>' : '<span class="badge bg-info text-dark">CONTROLADOR</span>'}</td>
        <td>${u.zona ? u.zona.nombre : '<span class="text-muted">-</span>'}</td>
        <td>${u.activo ? '<i class="bi bi-check-circle-fill text-success"></i>' : '<i class="bi bi-x-circle-fill text-danger"></i>'}</td>
        <td>
          <button class="btn btn-sm btn-outline-primary me-1" onclick="editarUsuario(${JSON.stringify(u).replace(/"/g,'&quot;')})">
            <i class="bi bi-pencil"></i>
          </button>
          <button class="btn btn-sm btn-outline-danger" onclick="eliminarUsuario(${u.id}, '${u.username}')">
            <i class="bi bi-trash"></i>
          </button>
        </td>
      </tr>`).join('');
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

// --- Historial de Eventos ---
async function cargarEventos() {
  const rutaId = document.getElementById('filtroEventoRuta')?.value || '';
  const prioridad = document.getElementById('filtroEventoPrioridad')?.value || '';
  try {
    const res = await fetch('/api/eventos');
    let eventos = await res.json();
    if (rutaId) eventos = eventos.filter(e => e.ruta && e.ruta.id === rutaId);
    if (prioridad) eventos = eventos.filter(e => e.prioridad === prioridad);

    const tbody = document.getElementById('eventosTable');
    const empty = document.getElementById('eventosEmpty');
    if (eventos.length === 0) {
      tbody.innerHTML = '';
      empty.classList.remove('d-none');
      return;
    }
    empty.classList.add('d-none');
    tbody.innerHTML = eventos.map(e => `
      <tr class="event-${e.prioridad ? e.prioridad.toLowerCase() : 'baja'}">
        <td>${e.timestamp ? new Date(e.timestamp).toLocaleString('es-CO') : '-'}</td>
        <td><code>${e.bus ? e.bus.id : '-'}</code></td>
        <td>${e.ruta ? e.ruta.id : '-'}</td>
        <td>${tipoEventoBadge(e.tipoEvento)}</td>
        <td>${prioridadBadge(e.prioridad)}</td>
        <td>${e.latitud ? e.latitud.toFixed(4) : '-'}</td>
        <td>${e.longitud ? e.longitud.toFixed(4) : '-'}</td>
        <td>${e.estadoProcesado ? '<i class="bi bi-check-circle-fill text-success"></i>' : '<i class="bi bi-clock text-warning"></i>'}</td>
      </tr>`).join('');
  } catch (_) {}
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

// --- Alertas críticas visuales ---
function mostrarAlertaCritica(bus) {
  let container = document.getElementById('alertasContainer');
  if (!container) {
    container = document.createElement('div');
    container.id = 'alertasContainer';
    document.body.appendChild(container);
  }
  const tipo = bus.ultimoEvento || bus.prioridad;
  let clase = '';
  let icono = '';
  let titulo = '';
  if (tipo === 'ACCIDENTE' || bus.prioridad === 'ALTA') {
    clase = 'alerta-accidente'; icono = '🚨'; titulo = 'ACCIDENTE';
  } else if (tipo === 'CONGESTION' || bus.prioridad === 'MEDIA') {
    clase = 'alerta-congestion'; icono = '⚠️'; titulo = 'CONGESTIÓN';
  } else {
    clase = 'alerta-media'; icono = '📍'; titulo = tipo;
  }

  const toast = document.createElement('div');
  toast.className = `alert alerta-toast ${clase} d-flex align-items-start gap-2 py-2 px-3`;
  toast.innerHTML = `
    <span style="font-size:1.2rem">${icono}</span>
    <div class="flex-fill">
      <strong>${titulo}</strong> - Bus ${bus.id}<br>
      <small class="text-muted">Ruta ${bus.rutaId || '?'} | ${bus.estado || ''}</small>
    </div>
    <button type="button" class="btn-close" onclick="this.parentElement.remove()"></button>`;
  container.prepend(toast);
  setTimeout(() => { if (toast.parentElement) toast.remove(); }, 8000);
}

// Inicio
checkSession();
