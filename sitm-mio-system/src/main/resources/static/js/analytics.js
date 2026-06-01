// ===== SITM-MIO Analítica =====

let chartVelocidad = null;
let chartPrioridad = null;
let chartMeses = null;
let chartBuses = null;

async function cargarAnalitica() {
  try {
    const [resumen, velocidad, prioridades, meses, buses, rutas] = await Promise.all([
      fetch('/api/analytics/resumen').then(r => r.json()),
      fetch('/api/analytics/velocidad-por-ruta').then(r => r.json()),
      fetch('/api/analytics/eventos-criticos').then(r => r.json()),
      fetch('/api/analytics/eventos-por-mes').then(r => r.json()),
      fetch('/api/analytics/buses-activos').then(r => r.json()),
      fetch('/api/rutas').then(r => r.json()).catch(() => [])
    ]);

    renderizarKPIs(resumen);
    renderizarChartVelocidad(velocidad.velocidadPorRuta || {});
    renderizarChartPrioridad(prioridades);
    renderizarChartMeses(meses.eventosPorMes || {});
    renderizarChartBuses(buses);
    renderizarTablaRutas(rutas, velocidad.velocidadPorRuta || {}, resumen);
  } catch (e) {
    document.getElementById('kpiCards').innerHTML =
      '<div class="col-12"><div class="alert alert-warning">Error cargando datos de analítica</div></div>';
  }
}

function renderizarTablaRutas(rutas, velocidades, resumen) {
  const tbody = document.getElementById('tablaRutasBody');
  if (!tbody || !rutas.length) return;
  const busesTotal = resumen.totalBuses || 0;
  tbody.innerHTML = rutas.map(r => {
    const vel = velocidades[r.id];
    const velStr = vel ? vel.toFixed(1) + ' km/h' : 'N/A';
    const busesEst = Math.ceil(busesTotal / (rutas.length || 1));
    const estado = vel > 40 ? '<span class="badge bg-success">Normal</span>'
      : vel > 20 ? '<span class="badge bg-warning text-dark">Moderado</span>'
      : vel ? '<span class="badge bg-danger">Lento</span>'
      : '<span class="badge bg-secondary">Sin datos</span>';
    return `<tr>
      <td><span class="badge me-1" style="background:${r.color || '#666'}">${r.id}</span>${r.nombre}</td>
      <td>${velStr}</td>
      <td>~${busesEst}</td>
      <td>${estado}</td>
    </tr>`;
  }).join('');
}

function renderizarKPIs(data) {
  const kpis = [
    { label: 'Total Buses', valor: data.totalBuses, icono: 'bi-bus-front-fill', clase: 'primary' },
    { label: 'Buses Activos', valor: data.busesActivos, icono: 'bi-activity', clase: 'success' },
    { label: 'Total Eventos', valor: data.totalEventos, icono: 'bi-bell-fill', clase: 'warning' },
    { label: 'Eventos Críticos', valor: data.eventosCriticos, icono: 'bi-exclamation-triangle-fill', clase: 'danger' },
    { label: 'Total Rutas', valor: data.totalRutas, icono: 'bi-signpost-2-fill', clase: 'primary' }
  ];
  document.getElementById('kpiCards').innerHTML = kpis.map(k => `
    <div class="col-md-2 col-sm-4 col-6">
      <div class="card shadow-sm kpi-card ${k.clase} h-100">
        <div class="card-body text-center py-3">
          <i class="bi ${k.icono} fs-2 text-${k.clase}"></i>
          <div class="fs-2 fw-bold mt-1">${k.valor ?? 0}</div>
          <div class="text-muted small">${k.label}</div>
        </div>
      </div>
    </div>`).join('');
}

function renderizarChartVelocidad(datos) {
  const ctx = document.getElementById('chartVelocidad');
  if (!ctx) return;
  if (chartVelocidad) chartVelocidad.destroy();
  const labels = Object.keys(datos);
  const values = Object.values(datos);
  chartVelocidad = new Chart(ctx, {
    type: 'bar',
    data: {
      labels,
      datasets: [{
        label: 'Vel. Promedio (km/h)',
        data: values,
        backgroundColor: ['#E74C3C','#E67E22','#2ECC71','#3498DB','#9B59B6'],
        borderRadius: 6
      }]
    },
    options: {
      responsive: true,
      plugins: { legend: { display: false } },
      scales: { y: { beginAtZero: true, max: 80 } }
    }
  });
}

function renderizarChartPrioridad(datos) {
  const ctx = document.getElementById('chartPrioridad');
  if (!ctx) return;
  if (chartPrioridad) chartPrioridad.destroy();
  chartPrioridad = new Chart(ctx, {
    type: 'doughnut',
    data: {
      labels: ['Alta', 'Media', 'Baja'],
      datasets: [{
        data: [datos.ALTA || 0, datos.MEDIA || 0, datos.BAJA || 0],
        backgroundColor: ['#E74C3C', '#F39C12', '#27AE60'],
        borderWidth: 2
      }]
    },
    options: {
      responsive: true,
      plugins: { legend: { position: 'bottom' } }
    }
  });
}

function renderizarChartMeses(datos) {
  const ctx = document.getElementById('chartMeses');
  if (!ctx) return;
  if (chartMeses) chartMeses.destroy();
  const labels = Object.keys(datos);
  const values = Object.values(datos);
  chartMeses = new Chart(ctx, {
    type: 'line',
    data: {
      labels: labels.length > 0 ? labels : ['Sin datos'],
      datasets: [{
        label: 'Eventos',
        data: values.length > 0 ? values : [0],
        borderColor: '#3498DB',
        backgroundColor: 'rgba(52,152,219,0.15)',
        tension: 0.4,
        fill: true,
        pointRadius: 5,
        pointBackgroundColor: '#3498DB'
      }]
    },
    options: {
      responsive: true,
      plugins: { legend: { display: false } },
      scales: { y: { beginAtZero: true } }
    }
  });
}

function renderizarChartBuses(datos) {
  const ctx = document.getElementById('chartBuses');
  if (!ctx) return;
  if (chartBuses) chartBuses.destroy();
  chartBuses = new Chart(ctx, {
    type: 'pie',
    data: {
      labels: ['Activo', 'En Ruta', 'En Parada', 'Inactivo'],
      datasets: [{
        data: [
          datos.ACTIVO || 0,
          datos.EN_RUTA || 0,
          datos.EN_PARADA || 0,
          datos.INACTIVO || 0
        ],
        backgroundColor: ['#27AE60', '#3498DB', '#F39C12', '#95A5A6'],
        borderWidth: 2
      }]
    },
    options: {
      responsive: true,
      plugins: { legend: { position: 'bottom' } }
    }
  });
}
