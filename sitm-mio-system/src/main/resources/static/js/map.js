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

// --- Mejora 3: íconos y colores diferenciados por tipo (R4) ---

function crearMarcadorBus(bus) {
  const color = colorBus(bus);
  const icono = L.divIcon({
    className: '',
    html: `<div class="bus-marker-icon" style="background:${color};border:2px solid white;box-shadow:0 2px 6px rgba(0,0,0,.4);" title="${bus.id} - ${bus.rutaId || ''}">
             <svg width="14" height="14" viewBox="0 0 24 24" fill="white">
               <path d="M4 16c0 .88.39 1.67 1 2.22V20a1 1 0 0 0 2 0v-1h10v1a1 1 0 0 0 2 0v-1.78c.61-.55 1-1.34 1-2.22V6c0-3.5-3.58-4-8-4s-8 .5-8 4zm3.5 1c-.83 0-1.5-.67-1.5-1.5S6.67 14 7.5 14s1.5.67 1.5 1.5S8.33 17 7.5 17zm9 0c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5zM5 11V7h14v4H5z"/>
             </svg>
           </div>`,
    iconSize: [26, 26],
    iconAnchor: [13, 13]
  });
  const marker = L.marker([bus.latitud, bus.longitud], { icon: icono });
  marker.on('click', () => mostrarDetalleBus(bus.id));
  return marker;
}

function colorBus(bus) {
  if (bus.ultimoEvento === 'ACCIDENTE' || bus.prioridad === 'ALTA') return '#C0392B';
  if (bus.ultimoEvento === 'CONGESTION' || bus.prioridad === 'MEDIA') return '#E67E22';
  if (bus.rutaColor) return bus.rutaColor;
  return '#2980B9';
}

function obtenerClaseBus(bus) {
  if (bus.ultimoEvento === 'ACCIDENTE' || bus.prioridad === 'ALTA') return 'bus-accidente';
  if (bus.ultimoEvento === 'CONGESTION' || bus.prioridad === 'MEDIA') return 'bus-congestion';
  if (bus.ultimoEvento === 'GPS_NORMAL') return 'bus-gps';
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

// Marcador diferenciado: estaciones grandes con ícono cuadrado, paradas con círculo (Mejora 3)
function crearMarcadorEstacion(estacion) {
  const esMayor = estacion.tipo === 'ESTACION_MAYOR';
  const svgEstacion = `<svg width="16" height="16" viewBox="0 0 24 24" fill="white">
    <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
  </svg>`;
  const svgParada = `<svg width="12" height="12" viewBox="0 0 24 24" fill="white">
    <circle cx="12" cy="12" r="8"/>
  </svg>`;

  const icono = L.divIcon({
    className: '',
    html: `<div style="
      width:${esMayor ? 22 : 14}px;
      height:${esMayor ? 22 : 14}px;
      background:${esMayor ? '#1A252F' : '#7F8C8D'};
      border-radius:${esMayor ? '4px' : '50%'};
      border:2px solid white;
      box-shadow:0 2px 5px rgba(0,0,0,.4);
      display:flex;align-items:center;justify-content:center;">
      ${esMayor ? svgEstacion : svgParada}
    </div>`,
    iconSize: [esMayor ? 22 : 14, esMayor ? 22 : 14],
    iconAnchor: [esMayor ? 11 : 7, esMayor ? 11 : 7]
  });
  const marker = L.marker([estacion.latitud, estacion.longitud], { icon: icono });
  marker.bindPopup(`
    <strong>${estacion.nombre}</strong><br>
    <span style="font-size:.9em">${estacion.tipo === 'ESTACION_MAYOR' ? '🏢 <b>Estación Mayor</b>' : '🚏 <b>Parada</b>'}</span><br>
    Ruta: <b>${estacion.ruta ? estacion.ruta.id : '-'}</b>
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
  const lista = Array.isArray(actualizaciones) ? actualizaciones : [actualizaciones];
  lista.forEach(upd => {
    const lat = upd.latitud ?? upd.lat;
    const lon = upd.longitud ?? upd.lon;
    const entry = busMarkers[upd.id];
    if (entry && lat && lon) {
      entry.marker.setLatLng([lat, lon]);
      entry.data = { ...entry.data, ...upd, latitud: lat, longitud: lon };
      // Actualizar ícono con nuevo color según estado
      const busActualizado = { ...entry.data, ultimoEvento: upd.ultimoEvento, prioridad: upd.prioridad };
      const color = colorBus(busActualizado);
      const nuevoIcono = L.divIcon({
        className: '',
        html: `<div class="bus-marker-icon" style="background:${color};border:2px solid white;box-shadow:0 2px 6px rgba(0,0,0,.4);" title="${upd.id}">
                 <svg width="14" height="14" viewBox="0 0 24 24" fill="white"><path d="M4 16c0 .88.39 1.67 1 2.22V20a1 1 0 0 0 2 0v-1h10v1a1 1 0 0 0 2 0v-1.78c.61-.55 1-1.34 1-2.22V6c0-3.5-3.58-4-8-4s-8 .5-8 4zm3.5 1c-.83 0-1.5-.67-1.5-1.5S6.67 14 7.5 14s1.5.67 1.5 1.5S8.33 17 7.5 17zm9 0c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5zM5 11V7h14v4H5z"/></svg>
               </div>`,
        iconSize: [26, 26],
        iconAnchor: [13, 13]
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
