// ===== SITM-MIO Mapa Interactivo con Leaflet =====

let mapa = null;
let stompClient = null;
let busMarkers = {};
let rutasCapas = {};
let estacionesCapas = [];
let capaBuses = null;
let capaEstaciones = null;
let capaRutas = null;
window.mapaIniciado = false;

function initMap() {
  if (window.mapaIniciado) return;
  window.mapaIniciado = true;

  mapa = L.map('map').setView([3.4516, -76.5320], 13);
  window.mapa = mapa;

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '© OpenStreetMap contributors',
    maxZoom: 18
  }).addTo(mapa);

  capaBuses = L.layerGroup().addTo(mapa);
  capaEstaciones = L.layerGroup().addTo(mapa);
  capaRutas = L.layerGroup().addTo(mapa);

  // Control de capas
  const overlays = {
    'Buses': capaBuses,
    'Estaciones/Paradas': capaEstaciones,
    'Rutas': capaRutas
  };
  L.control.layers(null, overlays, { collapsed: false, position: 'topright' }).addTo(mapa);

  cargarDatosMapa();
  conectarWebSocket();
}

// --- Cargar datos iniciales ---
async function cargarDatosMapa() {
  await Promise.all([
    cargarBuses(),
    cargarEstaciones(),
    cargarRutasMapa()
  ]);
}

async function cargarBuses(params = {}) {
  try {
    let url = '/api/buses';
    const qs = new URLSearchParams(params).toString();
    if (qs) url += '?' + qs;
    const res = await fetch(url);
    const buses = await res.json();
    renderizarBuses(buses);
  } catch (_) {}
}

function renderizarBuses(buses) {
  capaBuses.clearLayers();
  busMarkers = {};
  buses.forEach(bus => {
    if (bus.latitud && bus.longitud) {
      const marker = crearMarcadorBus(bus);
      capaBuses.addLayer(marker);
      busMarkers[bus.id] = { marker, data: bus };
    }
  });
  document.getElementById('contadorFiltros').textContent =
    `${buses.length} bus${buses.length !== 1 ? 'es' : ''} mostrado${buses.length !== 1 ? 's' : ''}`;
}

function crearMarcadorBus(bus) {
  const clase = obtenerClaseBus(bus);
  const icono = L.divIcon({
    className: '',
    html: `<div class="bus-marker-icon ${clase}" title="${bus.id}">🚌</div>`,
    iconSize: [28, 28],
    iconAnchor: [14, 14]
  });
  const marker = L.marker([bus.latitud, bus.longitud], { icon: icono });
  marker.on('click', () => mostrarDetalleBus(bus.id));
  return marker;
}

function obtenerClaseBus(bus) {
  if (bus.ultimoEvento === 'ACCIDENTE' || bus.prioridad === 'ALTA') return 'bus-accidente';
  if (bus.ultimoEvento === 'CONGESTION' || bus.prioridad === 'MEDIA') return 'bus-congestion';
  if (bus.ultimoEvento === 'GPS_NORMAL') return 'bus-gps';
  if (bus.prioridad === 'BAJA') return 'bus-baja';
  if (bus.rutaColor) return 'bus-baja';
  return 'bus-gps';
}

// --- Detalle de bus al hacer clic en el mapa (R4, R12) ---
async function mostrarDetalleBus(busId) {
  try {
    const res = await fetch(`/api/buses/${busId}`);
    if (!res.ok) return;
    const bus = await res.json();

    const header = document.getElementById('busModalHeader');
    const body = document.getElementById('busModalBody');

    // Color del header según prioridad/evento
    let headerClass = 'bg-primary';
    if (bus.prioridad === 'ALTA' || bus.ultimoEvento === 'ACCIDENTE') headerClass = 'bg-danger';
    else if (bus.prioridad === 'MEDIA' || bus.ultimoEvento === 'CONGESTION') headerClass = 'bg-warning text-dark';
    else if (bus.ultimoEvento === 'GPS_NORMAL') headerClass = 'bg-success';

    header.className = `modal-header ${headerClass}`;
    header.querySelector('.modal-title').innerHTML =
      `<i class="bi bi-bus-front-fill me-2"></i>${bus.id} - ${bus.numeroPlaca || ''}`;

    body.innerHTML = `
      <div class="bus-popup-header">${bus.rutaNombre || 'Sin ruta'}</div>
      <div class="row g-2">
        <div class="col-6">
          <div class="popup-field"><span class="popup-label">ID Bus:</span><span class="popup-value">${bus.id}</span></div>
          <div class="popup-field"><span class="popup-label">Ruta:</span><span class="popup-value">${bus.rutaId || '-'}</span></div>
          <div class="popup-field"><span class="popup-label">Latitud:</span><span class="popup-value">${bus.latitud?.toFixed(5) || '-'}</span></div>
          <div class="popup-field"><span class="popup-label">Longitud:</span><span class="popup-value">${bus.longitud?.toFixed(5) || '-'}</span></div>
        </div>
        <div class="col-6">
          <div class="popup-field"><span class="popup-label">Timestamp:</span><span class="popup-value" style="font-size:0.8rem">${bus.ultimoTimestamp || '-'}</span></div>
          <div class="popup-field"><span class="popup-label">Último evento:</span><span class="popup-value">${tipoEventoBadge(bus.ultimoEvento)}</span></div>
          <div class="popup-field"><span class="popup-label">Prioridad:</span><span class="popup-value">${prioridadBadge(bus.prioridad)}</span></div>
          <div class="popup-field"><span class="popup-label">Estado:</span><span class="popup-value">${estadoBadge(bus.estado)}</span></div>
        </div>
        <div class="col-12">
          <div class="popup-field">
            <span class="popup-label">Velocidad:</span>
            <span class="popup-value">${bus.velocidad ? bus.velocidad.toFixed(1) + ' km/h' : 'N/A'}</span>
          </div>
        </div>
      </div>
      <div class="text-center mt-3">
        <button class="btn btn-sm btn-outline-primary" onclick="centrarEnBus(${bus.latitud}, ${bus.longitud})">
          <i class="bi bi-crosshair2 me-1"></i>Centrar en mapa
        </button>
      </div>`;

    new bootstrap.Modal(document.getElementById('busModal')).show();
  } catch (_) {}
}

function centrarEnBus(lat, lon) {
  if (mapa && lat && lon) {
    mapa.flyTo([lat, lon], 16, { duration: 1 });
    bootstrap.Modal.getInstance(document.getElementById('busModal')).hide();
  }
}

// --- Estaciones con iconos diferenciados (R4) ---
async function cargarEstaciones() {
  try {
    const res = await fetch('/api/estaciones');
    const estaciones = await res.json();
    capaEstaciones.clearLayers();
    estaciones.forEach(e => {
      if (e.latitud && e.longitud) {
        const marker = crearMarcadorEstacion(e);
        capaEstaciones.addLayer(marker);
      }
    });
  } catch (_) {}
}

function crearMarcadorEstacion(estacion) {
  const esMayor = estacion.tipo === 'ESTACION_MAYOR';
  const icono = L.divIcon({
    className: '',
    html: `<div style="
      width:${esMayor ? 20 : 14}px;
      height:${esMayor ? 20 : 14}px;
      background:${esMayor ? '#2C3E50' : '#95A5A6'};
      border-radius:${esMayor ? '3px' : '50%'};
      border:2px solid white;
      box-shadow:0 2px 4px rgba(0,0,0,0.3);">
    </div>`,
    iconSize: [esMayor ? 20 : 14, esMayor ? 20 : 14],
    iconAnchor: [esMayor ? 10 : 7, esMayor ? 10 : 7]
  });
  const marker = L.marker([estacion.latitud, estacion.longitud], { icon: icono });
  marker.bindPopup(`
    <strong>${estacion.nombre}</strong><br>
    Tipo: <b>${estacion.tipo === 'ESTACION_MAYOR' ? '🏢 Estación Mayor' : '🚏 Parada'}</b><br>
    Ruta: ${estacion.ruta ? estacion.ruta.id : '-'}
  `);
  return marker;
}

// --- Rutas en el mapa con colores diferenciados (R4) ---
async function cargarRutasMapa() {
  try {
    const [rutasRes, estRes] = await Promise.all([
      fetch('/api/rutas'),
      fetch('/api/estaciones')
    ]);
    const rutas = await rutasRes.json();
    const estaciones = await estRes.json();
    capaRutas.clearLayers();

    rutas.forEach(ruta => {
      const pts = estaciones
        .filter(e => e.ruta && e.ruta.id === ruta.id && e.latitud)
        .map(e => [e.latitud, e.longitud]);

      if (pts.length >= 2) {
        const polyline = L.polyline(pts, {
          color: ruta.color || '#3498DB',
          weight: 4,
          opacity: 0.7,
          dashArray: '8, 4'
        });
        polyline.bindTooltip(ruta.id + ' - ' + ruta.nombre, { sticky: true });
        capaRutas.addLayer(polyline);
      }
    });
  } catch (_) {}
}

// --- WebSocket: actualización periódica de posición de buses (R4) - Mejora 2 ---
let wsConectado = false;

function conectarWebSocket() {
  try {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.debug = null;

    stompClient.connect({}, () => {
      wsConectado = true;
      actualizarIndicadorWS(true);

      stompClient.subscribe('/topic/buses', (msg) => {
        const actualizaciones = JSON.parse(msg.body);
        actualizarPosicionesBuses(actualizaciones);
        mostrarUltimaActualizacion();
      });
    }, () => {
      wsConectado = false;
      actualizarIndicadorWS(false);
      // Reintentar en 5 segundos si falla la conexión
      setTimeout(conectarWebSocket, 5000);
    });
  } catch (_) {
    actualizarIndicadorWS(false);
    setTimeout(conectarWebSocket, 5000);
  }
}

function actualizarIndicadorWS(conectado) {
  const ind = document.getElementById('wsIndicador');
  if (!ind) return;
  if (conectado) {
    ind.className = 'badge bg-success me-1';
    ind.innerHTML = '<i class="bi bi-circle-fill" style="font-size:.5rem"></i> En vivo';
    ind.title = 'Actualización en tiempo real activa (cada 5 s)';
  } else {
    ind.className = 'badge bg-danger me-1';
    ind.innerHTML = '<i class="bi bi-circle-fill" style="font-size:.5rem"></i> Sin conexión';
    ind.title = 'Reconectando...';
  }
}

function mostrarUltimaActualizacion() {
  const el = document.getElementById('ultimaActualizacion');
  if (el) {
    const ahora = new Date();
    el.textContent = `⟳ ${ahora.toLocaleTimeString('es-CO')}`;
    el.title = 'Posiciones actualizadas el ' + ahora.toLocaleString('es-CO');
  }
}

function actualizarPosicionesBuses(actualizaciones) {
  if (!Array.isArray(actualizaciones)) return;
  actualizaciones.forEach(upd => {
    const entry = busMarkers[upd.id];
    if (entry && upd.lat && upd.lon) {
      entry.marker.setLatLng([upd.lat, upd.lon]);
      entry.data = { ...entry.data, ...upd, latitud: upd.lat, longitud: upd.lon };
      // Actualizar clase del ícono según nuevo estado
      const clase = obtenerClaseBus({ ...entry.data, ultimoEvento: upd.ultimoEvento, prioridad: upd.prioridad });
      const nuevoIcono = L.divIcon({
        className: '',
        html: `<div class="bus-marker-icon ${clase}" title="${upd.id}">🚌</div>`,
        iconSize: [28, 28],
        iconAnchor: [14, 14]
      });
      entry.marker.setIcon(nuevoIcono);
      // Alerta si prioridad alta
      if (upd.prioridad === 'ALTA' || upd.ultimoEvento === 'ACCIDENTE') {
        mostrarAlertaCritica({ ...entry.data, id: upd.id });
      }
    }
  });
}

// --- Filtros del mapa (R4, R9, R11) ---
function aplicarFiltros() {
  const params = {};
  const rutaId = document.getElementById('filtroRuta').value;
  const prioridad = document.getElementById('filtroPrioridad').value;
  const estado = document.getElementById('filtroEstado').value;
  const tipoEvento = document.getElementById('filtroTipoEvento').value;
  if (rutaId) params.rutaId = rutaId;
  if (prioridad) params.prioridad = prioridad;
  if (estado) params.estado = estado;
  if (tipoEvento) params.tipoEvento = tipoEvento;
  cargarBuses(params);
}

function limpiarFiltros() {
  document.getElementById('filtroRuta').value = '';
  document.getElementById('filtroPrioridad').value = '';
  document.getElementById('filtroEstado').value = '';
  document.getElementById('filtroTipoEvento').value = '';
  cargarBuses();
}

// Iniciar mapa cuando se muestra el tab
document.addEventListener('DOMContentLoaded', () => {
  // El mapa se inicia cuando se activa el tab
});
