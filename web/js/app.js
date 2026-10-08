const form = document.getElementById('analysisForm');
const analyzeBtn = document.getElementById('analyzeBtn');
const demoBtn = document.getElementById('demoBtn');
const resetBtn = document.getElementById('resetBtn');

const levelBadge = document.getElementById('levelBadge');
const emptyResult = document.getElementById('emptyResult');
const resultContent = document.getElementById('resultContent');
const score = document.getElementById('score');
const scoreBar = document.getElementById('scoreBar');
const threatName = document.getElementById('threatName');
const recommendation = document.getElementById('recommendation');
const reasonsList = document.getElementById('reasonsList');

const counters = {
  total: document.getElementById('total'),
  criticas: document.getElementById('criticas'),
  altas: document.getElementById('altas'),
  medias: document.getElementById('medias'),
  bajas: document.getElementById('bajas')
};

form.addEventListener('submit', async (event) => {
  event.preventDefault();
  analyzeBtn.disabled = true;
  analyzeBtn.textContent = 'Analizando...';

  const body = new URLSearchParams(new FormData(form));
  body.set('conexionSospechosa', document.getElementById('conexionSospechosa').checked ? 'true' : 'false');
  body.set('alertaActiva', document.getElementById('alertaActiva').checked ? 'true' : 'false');
  body.set('comportamientoExtrano', document.getElementById('comportamientoExtrano').checked ? 'true' : 'false');

  try {
    const response = await fetch('/api/detect', { method: 'POST', body });
    const data = await response.json();
    if (!response.ok) throw new Error(data.error || 'No se pudo analizar el evento.');
    showResult(data);
    await refreshDashboard();
  } catch (error) {
    alert(error.message);
  } finally {
    analyzeBtn.disabled = false;
    analyzeBtn.textContent = 'Analizar con Prolog';
  }
});

demoBtn.addEventListener('click', () => {
  document.getElementById('usuario').value = 'admin';
  document.getElementById('ip').value = '185.44.20.10';
  document.getElementById('tipoEvento').value = 'login';
  document.getElementById('intentosFallidos').value = 10;
  document.getElementById('descripcion').value = 'Diez intentos fallidos desde una IP no habitual con comportamiento anómalo.';
  document.getElementById('conexionSospechosa').checked = true;
  document.getElementById('alertaActiva').checked = true;
  document.getElementById('comportamientoExtrano').checked = true;
});

resetBtn.addEventListener('click', async () => {
  if (!confirm('¿Deseas eliminar el historial de análisis?')) return;
  await fetch('/api/reset', { method: 'POST' });
  await refreshDashboard();
  levelBadge.textContent = 'SIN ANÁLISIS';
  levelBadge.className = 'level-badge neutral';
  resultContent.classList.add('hidden');
  emptyResult.classList.remove('hidden');
});

function showResult(registro) {
  const result = registro.analisis;
  const levelClass = result.nivel.toLowerCase();
  levelBadge.textContent = result.nivel;
  levelBadge.className = `level-badge ${levelClass}`;
  emptyResult.classList.add('hidden');
  resultContent.classList.remove('hidden');
  score.textContent = result.puntuacion;
  scoreBar.style.width = `${Math.min(100, (result.puntuacion / 11) * 100)}%`;
  threatName.textContent = result.amenaza;
  recommendation.textContent = result.recomendacion;
  reasonsList.innerHTML = '';
  result.razones.forEach(reason => {
    const li = document.createElement('li');
    li.textContent = reason;
    reasonsList.appendChild(li);
  });
}

async function refreshDashboard() {
  const [reportResponse, historyResponse] = await Promise.all([
    fetch('/api/report'),
    fetch('/api/history')
  ]);
  const report = await reportResponse.json();
  const history = await historyResponse.json();

  counters.total.textContent = report.total;
  counters.criticas.textContent = report.criticas;
  counters.altas.textContent = report.altas;
  counters.medias.textContent = report.medias;
  counters.bajas.textContent = report.bajas;

  const body = document.getElementById('historyBody');
  if (!history.length) {
    body.innerHTML = '<tr><td colspan="6" class="table-empty">Aún no hay análisis.</td></tr>';
    return;
  }

  body.innerHTML = history.map(item => {
    const nivel = item.analisis.nivel.toLowerCase();
    return `<tr>
      <td>${escapeHtml(item.fecha)}</td>
      <td>${escapeHtml(item.usuario)}</td>
      <td>${escapeHtml(item.ip)}</td>
      <td>${escapeHtml(item.tipoEvento)}</td>
      <td>${escapeHtml(item.analisis.amenaza)}</td>
      <td><span class="inline-level level-badge ${nivel}">${escapeHtml(item.analisis.nivel)}</span></td>
    </tr>`;
  }).join('');
}

function escapeHtml(value) {
  return String(value).replace(/[&<>'"]/g, character => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#039;', '"': '&quot;'
  }[character]));
}

refreshDashboard();
