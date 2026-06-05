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
    await cargarAnaliticaHistorica();
  } 
  
  catch (e) {
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
    {
      label: 'Total de buses',
      valor: data.totalBuses,
      icono: 'bi-bus-front-fill',
      clase: 'primary'
    },
    {
      label: 'Buses activos',
      valor: data.busesActivos,
      icono: 'bi-activity',
      clase: 'success'
    },
    {
      label: 'Eventos recibidos',
      valor: data.totalEventos,
      icono: 'bi-bell-fill',
      clase: 'warning'
    },
    {
      label: 'Eventos críticos',
      valor: data.eventosCriticos,
      icono: 'bi-exclamation-triangle-fill',
      clase: 'danger'
    },
    {
      label: 'Rutas registradas',
      valor: data.totalRutas,
      icono: 'bi-signpost-2-fill',
      clase: 'primary'
    }
  ];

  document.getElementById('kpiCards').innerHTML = kpis.map(kpi => `
    <div class="col-xl col-lg-4 col-md-6 col-12">
      <div class="card shadow-sm kpi-card ${kpi.clase} h-100">
        <div class="card-body">
          <div class="kpi-icon-wrapper ${kpi.clase}">
            <i class="bi ${kpi.icono}"></i>
          </div>

          <div class="kpi-content">
            <p class="kpi-value">
              ${Number(kpi.valor ?? 0).toLocaleString('es-CO')}
            </p>

            <div class="kpi-label">
              ${kpi.label}
            </div>
          </div>
        </div>
      </div>
    </div>
  `).join('');
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

  if (chartPrioridad) {
    chartPrioridad.destroy();
  }

  chartPrioridad = new Chart(ctx, {
    type: 'doughnut',

    data: {
      labels: ['Crítica', 'Alta', 'Media', 'Baja'],

      datasets: [{
        data: [
          datos.CRITICA || 0,
          datos.ALTA || 0,
          datos.MEDIA || 0,
          datos.BAJA || 0
        ],

        backgroundColor: [
          '#922B21',
          '#E74C3C',
          '#F39C12',
          '#27AE60'
        ],

        borderWidth: 2
      }]
    },

    options: {
      responsive: true,

      plugins: {
        legend: {
          position: 'bottom'
        }
      }
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
// ===== Analítica histórica del experimento =====

let chartVelocidadHistorica = null;

async function cargarAnaliticaHistorica() {
  const version =
    document.getElementById('historicoVersion')?.value || 'concurrente';

  const routeId =
    document.getElementById('historicoRuta')?.value || '';

  const month =
    document.getElementById('historicoMes')?.value || '';

  const params = new URLSearchParams({
    version,
    routeId,
    month
  });

  try {
    const [responseHistorico, responseBenchmark] = await Promise.all([
      fetch(`/api/analytics/historico/velocidades?${params.toString()}`),
      fetch('/api/analytics/historico/benchmark')
    ]);

    if (!responseHistorico.ok) {
      throw new Error('No fue posible cargar el histórico');
    }

    const data = await responseHistorico.json();
    const benchmark = responseBenchmark.ok
      ? await responseBenchmark.json()
      : {};

    renderizarKPIsHistoricos(data, benchmark);
    renderizarTablaHistorica(data.resultados || []);
    renderizarGraficoHistorico(data.resultados || []);

  } catch (error) {
    console.error(error);

    document.getElementById('historicoTable').innerHTML = `
      <tr>
        <td colspan="4"
            class="text-center text-danger py-4">
          No fue posible cargar los resultados históricos.
        </td>
      </tr>
    `;
  }
}

function renderizarKPIsHistoricos(data, benchmark) {
  const version = data.version || '-';

  document.getElementById('historicoKpiVersion').textContent =
    version;

  document.getElementById('historicoKpiResultados').textContent =
    Number(data.cantidadResultados || 0)
      .toLocaleString('es-CO');

  document.getElementById('historicoKpiRegistros').textContent =
    Number(data.totalRegistros || 0)
      .toLocaleString('es-CO');

  let tiempo = '-';

  if (version === 'monolitico') {
    tiempo =
      `${benchmark.monolitico?.tiempoMs ?? '-'} ms`;
  }

  if (version === 'concurrente') {
    tiempo =
      `${benchmark.concurrente?.tiempoMs ?? '-'} ms · ` +
      `${benchmark.concurrente?.speedup ?? '-'}x`;
  }

  if (version === 'distribuido') {
    tiempo =
      `Worker máximo: ` +
      `${benchmark.distribuido?.worker2TiempoMs ?? '-'} ms`;
  }

  document.getElementById('historicoKpiTiempo').textContent =
    tiempo;
}

function renderizarTablaHistorica(resultados) {
  const tbody = document.getElementById('historicoTable');

  if (!tbody) return;

  if (!resultados.length) {
    tbody.innerHTML = `
      <tr>
        <td colspan="4"
            class="text-center text-muted py-4">
          No existen resultados para los filtros seleccionados.
        </td>
      </tr>
    `;
    return;
  }

  tbody.innerHTML = resultados.map(row => `
    <tr>
      <td>
        <span class="badge bg-primary">
          ${row.routeId}
        </span>
      </td>

      <td>${row.month}</td>

      <td>
        ${Number(row.averageSpeedKmh).toFixed(2)} km/h
      </td>

      <td>
        ${Number(row.count).toLocaleString('es-CO')}
      </td>
    </tr>
  `).join('');
}

function renderizarGraficoHistorico(resultados) {
  const ctx =
    document.getElementById('chartVelocidadHistorica');

  if (!ctx) return;

  if (chartVelocidadHistorica) {
    chartVelocidadHistorica.destroy();
  }

  const muestra = resultados.slice(0, 30);

  chartVelocidadHistorica = new Chart(ctx, {
    type: 'bar',

    data: {
      labels: muestra.map(row =>
        `${row.routeId} · ${row.month}`
      ),

      datasets: [{
        label: 'Velocidad promedio (km/h)',

        data: muestra.map(row =>
          row.averageSpeedKmh
        ),

        backgroundColor: '#3498DB',
        borderRadius: 5
      }]
    },

    options: {
      responsive: true,

      plugins: {
        legend: {
          display: false
        }
      },

      scales: {
        y: {
          beginAtZero: true
        }
      }
    }
  });
}

function limpiarFiltrosHistoricos() {
  document.getElementById('historicoVersion').value =
    'concurrente';

  document.getElementById('historicoRuta').value =
    '';

  document.getElementById('historicoMes').value =
    '';

  cargarAnaliticaHistorica();
}