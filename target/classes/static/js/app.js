'use strict';

// ─── Cytoscape instance ────────────────────────────────────────────────────────

const cy = cytoscape({
  container: document.getElementById('cy'),
  style: [
    {
      selector: 'node', // aquí se definen las características visuales del vértice
      style: {
        'label': 'data(label)',
        'text-valign': 'center',
        'text-halign': 'center',
        'font-size': '12px',
        'text-wrap': 'wrap',
        'text-max-width': '40px',
        'width': 40, 'height': 40,
        'background-color': '#bdc3d8',
        'border-width': 2,
        'border-color': '#8890a8',
        'color': '#1a1a2e',
        'font-weight': 'bold'
      }
    },
    {
      selector: 'node.gray',
      style: { 'background-color': '#f0a500', 'border-color': '#c88800' }
    },
    {
      selector: 'node.red',
      style: { 'background-color': '#e94560', 'border-color': '#c03040', 'color': '#fff' }
    },
    {
      selector: 'node.orange',
      style: { 'background-color': '#ff6b35', 'border-color': '#cc5522' }
    },
    {
      selector: 'node.black',
      style: { 'background-color': '#4a4a6a', 'border-color': '#2a2a4a', 'color': '#aaa' }
    },
    {
      selector: 'node.green',
      style: { 'background-color': '#2ecc71', 'border-color': '#27ae60', 'color': '#fff' }
    },
    {
      selector: 'node:selected',
      style: { 'border-width': 3, 'border-color': '#fff' }
    },
    {
      selector: 'edge', // aquí se definen las características visuales de las aristas
      style: {
        'label': 'data(label)',
        'font-size': '11px',
        'color': '#a0a0b0',
        'text-background-color': '#1a1a2e',
        'text-background-opacity': 1,
        'text-background-padding': '2px',
        'width': 3,
        'line-color': '#3a3a5a',
        'target-arrow-color': '#3a3a5a',
        'target-arrow-shape': 'data(arrowShape)',
        'curve-style': 'bezier'
      }
    },
    {
      selector: 'edge.tree',
      style: { 'line-color': '#2ecc71', 'target-arrow-color': '#2ecc71', 'width': 3 }
    },
    {
      selector: 'edge.highlighted',
      style: { 'line-color': '#e94560', 'target-arrow-color': '#e94560', 'width': 3 }
    }
  ],
  elements: [],
  layout: { name: 'preset' },
  wheelSensitivity: 0.1 // zoom ratio (mouse scroll)
});

// ─── State ─────────────────────────────────────────────────────────────────────

let nextId = 0;
let edgeSource = null;  // vertex selected for edge drawing
let steps = [];
let currentStep = 0;
let playTimer = null;

// ─── UI refs ───────────────────────────────────────────────────────────────────

const modeSelect    = document.getElementById('editMode');
const graphTypeEl   = document.getElementById('graphType');
const algoSelect    = document.getElementById('algorithmSelect');
const srcInput      = document.getElementById('sourceVertex');
const sourceRow     = document.getElementById('sourceRow');
const btnRun        = document.getElementById('btnRun');
const btnClear      = document.getElementById('btnClear');
const btnLayout     = document.getElementById('btnLayout');
const btnExample    = document.getElementById('btnExample');
const stepCounter   = document.getElementById('stepCounter');
const stepDesc      = document.getElementById('stepDesc');
const btnSave       = document.getElementById('btnSave');
const btnLoad       = document.getElementById('btnLoad');
const fileInput     = document.getElementById('fileInput');
const btnFirst      = document.getElementById('btnFirst');
const btnPrev       = document.getElementById('btnPrev');
const btnPlay       = document.getElementById('btnPlay');
const btnNext       = document.getElementById('btnNext');
const btnLast       = document.getElementById('btnLast');
const speedSlider   = document.getElementById('speedSlider');
const matrixOverlay = document.getElementById('matrixOverlay');
const matrixTable   = document.getElementById('matrixTable');
const matrixTitle   = document.getElementById('matrixTitle');
const dsPanel       = document.getElementById('dsPanel');
const dsPanelHint   = document.getElementById('dsPanelHint');
const dataStructSvg = document.getElementById('dataStructSvg');
const btnDsSmaller  = document.getElementById('btnDsSmaller');
const btnDsLarger   = document.getElementById('btnDsLarger');
const btnDsClose    = document.getElementById('btnDsClose');

// ----- Constant values -------------------
const INF = 1073741823;

// ─── Data structure panel size control ───────────────────────────────────────

let dsPanelHeight = 230;
const DS_MIN_HEIGHT = 80;
const DS_MAX_HEIGHT = 800;
let isDraggingPanelBorder = false;
let dragStartY = 0;
let dragStartHeight = 0;

// Drag handler for panel border
dsPanel.addEventListener('mousedown', (e) => {
  // Check if click is on the top border (within 8px)
  if (e.clientY - dsPanel.getBoundingClientRect().top < 8) {
    isDraggingPanelBorder = true;
    dragStartY = e.clientY;
    dragStartHeight = dsPanelHeight;
    dsPanel.style.cursor = 'row-resize';
    e.preventDefault();
  }
});

document.addEventListener('mousemove', (e) => {
  if (isDraggingPanelBorder) {
    const delta = dragStartY - e.clientY; // negative = drag up (expand), positive = drag down (shrink)
    const newHeight = Math.max(DS_MIN_HEIGHT, Math.min(DS_MAX_HEIGHT, dragStartHeight + delta));
    dsPanelHeight = newHeight;
    dsPanel.style.height = newHeight + 'px';
  }
});

document.addEventListener('mouseup', () => {
  if (isDraggingPanelBorder) {
    isDraggingPanelBorder = false;
    dsPanel.style.cursor = 'default';
  }
});

// Keep button functionality as fallback
btnDsSmaller?.addEventListener('click', () => {
  dsPanelHeight = Math.max(DS_MIN_HEIGHT, dsPanelHeight - 30);
  dsPanel.style.height = dsPanelHeight + 'px';
});

btnDsLarger?.addEventListener('click', () => {
  dsPanelHeight = Math.min(DS_MAX_HEIGHT, dsPanelHeight + 30);
  dsPanel.style.height = dsPanelHeight + 'px';
});

btnDsClose?.addEventListener('click', () => {
  dsPanel.classList.add('hidden');
});

// ─── Algorithms that don't need source ─────────────────────────────────────────

const noSource = new Set(['floyd_warshall','kruskal','kosaraju','topological_sort',
                          'hierholzer','cut_vertices','blocks','edge_classification',
                          'transitive_closure']);

algoSelect.addEventListener('change', () => {
  sourceRow.style.display = noSource.has(algoSelect.value) ? 'none' : 'flex';
});
algoSelect.dispatchEvent(new Event('change'));

// ─── Graph editing ─────────────────────────────────────────────────────────────

function getMode() { return modeSelect.value; }

cy.on('tap', function(evt) {
  const mode = getMode();
  if (evt.target === cy) {
    if (mode === 'addVertex') addVertex(evt.position);
    else edgeSource = null;
  }
});

cy.on('tap', 'node', function(evt) {
  const mode = getMode();
  const node = evt.target;

  if (mode === 'addEdge') {
    if (!edgeSource) {
      edgeSource = node;
      node.addClass('selected');
    } else {
      if (edgeSource.id() !== node.id()) {
        const w = promptWeight();
        addEdge(edgeSource.id(), node.id(), w);
      }
      edgeSource.removeClass('selected');
      edgeSource = null;
    }
  } else if (mode === 'delete') {
    node.remove();
  }
});

cy.on('tap', 'edge', function(evt) {
  if (getMode() === 'delete') evt.target.remove();
});

cy.on('cxttap', 'node', function(evt) {
  // right-click / long-press → delete
  evt.target.remove();
});

function addVertex(pos) {
  const id = nextId++;
  cy.add({ group: 'nodes', data: { id: String(id), label: String(id) }, position: pos });
}

function addEdge(src, tgt, weight) {
  const directed = graphTypeEl.value === 'directed';
  cy.add({
    group: 'edges',
    data: {
      id: `e${src}-${tgt}`,
      source: src, target: tgt,
      label: weight !== 1 ? String(weight) : '',
      weight,
      arrowShape: directed ? 'triangle' : 'none'
    }
  });
  if (!directed) {
    // also update reverse if it exists (for display); edges are logically undirected
  }
}

function promptWeight() {
  const w = prompt('Edge weight:', '1');
  const n = parseInt(w, 10);
  return isNaN(n) ? 1 : n;
}

btnClear.addEventListener('click', () => {
  cy.elements().remove();
  nextId = 0;
  resetPlayback();
  matrixOverlay.classList.add('hidden');
});

btnLayout.addEventListener('click', () => {
  cy.layout({ name: 'cose', animate: true, padding: 40 }).run();
});

btnExample.addEventListener('click', loadExample);

// ─── Example graph ─────────────────────────────────────────────────────────────
// Hardcoded example
function loadExample() {
  cy.elements().remove();
  nextId = 0;
  const directed = graphTypeEl.value === 'directed';
  const positions = [
    {x:200,y:150},{x:350,y:80},{x:500,y:150},{x:500,y:300},{x:350,y:370},{x:200,y:300}
  ];
  positions.forEach((pos, i) => {
    cy.add({ group:'nodes', data:{id:String(i),label:String(i)}, position:pos });
    nextId++;
  });
  const edges = [[0,1,4],[0,5,2],[1,2,5],[1,3,6],[2,3,3],[3,4,2],[4,5,1],[5,3,8],[2,5,7]];
  edges.forEach(([s,t,w]) => {
    cy.add({ group:'edges', data:{
      id:`e${s}-${t}`, source:String(s), target:String(t),
      label:String(w), weight:w,
      arrowShape: directed ? 'triangle' : 'none'
    }});
  });
}

// ------ Save / Load ------------------------------------------------------------

// convierte la gráfica en pantalla a un documento xml
function graphToXml(){
  const doc = document.implementation.createDocument(null, 'graph', null);
  const root = doc.documentElement;
  root.setAttribute('directed', String(graphTypeEl.value === 'directed')); // true or false

  const verticesEl = doc.createElement('vertices');
  cy.nodes().forEach(node => {
    const v = doc.createElement('vertex');
    const pos = node.position();
    v.setAttribute('id', node.id());
    v.setAttribute('x', String(Math.round(pos.x)));
    v.setAttribute('y', String(Math.round(pos.y)));
    verticesEl.appendChild(v);
  });

  const edgesEl = doc.createElement('edges');
  cy.edges().forEach(edge => {
    const e = doc.createElement('edge');
    e.setAttribute('source', edge.data('source'));
    e.setAttribute('target', edge.data('target'));
    e.setAttribute('weight', String(edge.data('weight') || 1));
    edgesEl.appendChild(e);
  });

  root.appendChild(verticesEl);
  root.appendChild(edgesEl);

  const xml = new XMLSerializer().serializeToString(doc)
    .replace(/(<\/?(?:vertices|edges)>|<(?:vertices|edges)\/>)/g, '\n $1')
    .replace(/(<(?:vertex|edge) )/g, '\n    $1')
    .replace(/<\/graph>$/, '\n</graph>');
  return '<?xml version="1.0" encoding="UTF-8"?>\n' + xml + '\n';
}

function parseIntAttr(el, name){
  const raw = el.getAttribute(name);
  const n = Number(raw);
  if(raw === null || raw.trim() === '' || !Number.isInteger(n)){
    throw new Error(`<${el.tagName}> has missing or invalid "${name}" attribute`);
  }
  return n;
}

function parseGraphXml(text) {
  const doc = new DOMParser().parseFromString(text, 'application/xml');
  if (doc.getElementsByTagName('parsererror').length) throw new Error('Malformed XML file');

  const root = doc.documentElement;
  if (root.tagName !== 'graph') throw new Error('Root element must be <graph>');

  const directedAttr = root.getAttribute('directed');
  const directed = directedAttr === null ? null : directedAttr === 'true';

  const ids = new Set();
  const vertices = [...root.querySelectorAll('vertices > vertex')].map(el => {
    const v = { id: parseIntAttr(el, 'id'), x: parseIntAttr(el, 'x'), y: parseIntAttr(el, 'y') };
    if (ids.has(v.id)) throw new Error(`Duplicate vertex id ${v.id}`);
    ids.add(v.id);
    return v;
  });

  const edgeKeys = new Set();
  const edges = [...root.querySelectorAll('edges > edge')].map(el => {
    const source = parseIntAttr(el, 'source');
    const target = parseIntAttr(el, 'target');
    const raw = el.getAttribute('weight');
    const weight = raw === null || raw.trim() === '' ? 1 : parseIntAttr(el, 'weight');
    if (!ids.has(source) || !ids.has(target)) {
      throw new Error(`Edge ${source}-${target} references an unknown vertex`);
    }
    const key = `${source}-${target}`;
    if (edgeKeys.has(key)) throw new Error(`Duplicate edge ${key}`);
    edgeKeys.add(key);
    return { source, target, weight };
  });

  return { directed, vertices, edges };
}

function renderGraph({ directed, vertices, edges }) {
  stopPlay();
  cy.elements().remove();
  resetPlayback();
  matrixOverlay.classList.add('hidden');
  if (directed !== null) graphTypeEl.value = directed ? 'directed' : 'undirected';

  vertices.forEach(({ id, x, y }) => {
    cy.add({ group: 'nodes', data: { id: String(id), label: String(id) }, position: { x, y } });
  });
  edges.forEach(({ source, target, weight }) => addEdge(String(source), String(target), weight));

  nextId = vertices.reduce((m, v) => Math.max(m, v.id + 1), 0);
  cy.fit(undefined, 40);
}

btnSave.addEventListener('click', async () => {
  const xml = graphToXml();

  if (window.showSaveFilePicker) {
    try {
      const handle = await window.showSaveFilePicker({
        suggestedName: 'graph.xml',
        types: [{ description: 'Graph XML', accept: { 'application/xml': ['.xml'] } }]
      });
      const writable = await handle.createWritable();
      await writable.write(xml);
      await writable.close();
    } catch (e) {
      if (e.name !== 'AbortError') alert('Error: ' + e.message);
    }
    return;
  }

  let name = prompt('File name:', 'graph.xml');
  if (name === null) return;
  name = name.trim() || 'graph.xml';
  if (!name.toLowerCase().endsWith('.xml')) name += '.xml';

  const url = URL.createObjectURL(new Blob([xml], { type: 'application/xml' }));
  const a = document.createElement('a');
  a.href = url;
  a.download = name;
  a.click();
  URL.revokeObjectURL(url);
});

btnLoad.addEventListener('click', () => fileInput.click());

fileInput.addEventListener('change', async () => {
  const file = fileInput.files[0];
  if (!file) return;
  try {
    renderGraph(parseGraphXml(await file.text()));
  } catch (e) {
    alert('Error: ' + e.message);
  } finally {
    fileInput.value = '';
  }
});

// ─── Run algorithm ─────────────────────────────────────────────────────────────

btnRun.addEventListener('click', async () => {
  stopPlay();
  resetPlayback();
  matrixOverlay.classList.add('hidden');

  const algo = algoSelect.value;
  const directed = graphTypeEl.value === 'directed';
  const vertices = cy.nodes().map(n => ({ id: parseInt(n.id(), 10) }));
  const edges = cy.edges().map(e => ({
    source: parseInt(e.data('source'), 10),
    target: parseInt(e.data('target'), 10),
    weight: e.data('weight') || 1
  }));

  if (vertices.length === 0) {
    alert('Add some vertices first!');
    return;
  }

  const body = {
    vertices, edges, directed,
    sourceVertex: parseInt(srcInput.value, 10) || 0
  };

  btnRun.disabled = true;
  btnRun.textContent = '⏳ Running…';

  try {
    const res = await fetch(`/api/algorithm/${algo}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body)
    });

    if (!res.ok) {
      const err = await res.json().catch(() => ({ message: res.statusText }));
      throw new Error(err.message || res.statusText);
    }

    const data = await res.json();
    steps = data.steps || [];
    currentStep = 0;

    if (steps.length > 0) {
      renderStep(steps[0]);
      stepDesc.textContent = data.message || '';
    }

    updatePlayControls();
  } catch (e) {
    alert('Error: ' + e.message);
  } finally {
    btnRun.disabled = false;
    btnRun.textContent = '▶ Run';
  }
});

// ─── Step rendering ────────────────────────────────────────────────────────────

const COLOR_MAP = {
  white:  'white',
  gray:   'gray',
  red:    'red',
  orange: 'orange',
  black:  'black',
  green:  'green'
};

function renderStep(step) {
  // Reset all classes
  cy.nodes().removeClass('white gray red orange black green');
  cy.edges().removeClass('tree highlighted');

  // Apply vertex colors
  if (step.vertexColors) {
    Object.entries(step.vertexColors).forEach(([id, color]) => {
      const node = cy.$id(id);
      if (node.length) {
        const cls = COLOR_MAP[color] || 'white';
        node.addClass(cls);
      }
    });
  }

  // Apply vertex labels (distance, SCC id, etc.)
  if (step.vertexLabels) {
    Object.entries(step.vertexLabels).forEach(([id, lbl]) => {
      const node = cy.$id(id);
      if (node.length) {
        const base = node.id();
        node.data('label', lbl ? `${base}\n${lbl}` : base);
      }
    });
  } else {
    cy.nodes().forEach(n => n.data('label', n.id()));
  }

  if(step.step == 0){
    // Remove edge labels (weights) for some algorithms.
    if (['bfs', 'dfs'].includes(algoSelect.value)) {
      cy.edges().forEach(e => e.data('label', ''));
    }else{ // Restore weights for the rest of them
      cy.edges().forEach(e => {
        const w = e.data('weight');
        e.data('label', w && w !== 1 ? String(w) : '');
      });
    }
  }

  // Tree edges — green
  if (step.treeEdges) {
    step.treeEdges.forEach(([u, v]) => {
      findEdge(u, v).addClass('tree');
    });
  }

  // Highlighted edges — red
  if (step.highlightedEdges) {
    step.highlightedEdges.forEach(([u, v]) => {
      findEdge(u, v).addClass('highlighted');
    });
  }

  // Matrix overlay
  const extra = step.extra || {};
  if (extra.matrix && extra.vertices) {
    renderMatrix(extra, algoSelect.value === 'transitive_closure');
  } else {
    matrixOverlay.classList.add('hidden');
  }

  // Data structure panel
  if (extra.bq) {
    renderBinomialQueue(extra.bq, step.vertexColors);
  } else if (extra.queue) {
    renderQueue(extra.queue, step.vertexColors);
  } else if (extra.stack) {
    renderStack(extra.stack, step.vertexColors);
  } else if (extra.binomialQueue) {
    renderBinQueue(extra.binomialQueue, step.vertexColors);
  } else {
    dsPanel.classList.add('hidden');
  }

  // Update playbar
  stepCounter.textContent = `Step ${step.step + 1} / ${steps.length}`;
  stepDesc.textContent = step.description || '';
}

function findEdge(u, v) {
  const e = cy.edges(`[source="${u}"][target="${v}"]`);
  if (e.length) return e;
  return cy.edges(`[source="${v}"][target="${u}"]`);
}

// ─── Matrix rendering ──────────────────────────────────────────────────────────

function renderMatrix(extra, isBool) {
  const { matrix, vertices, activeI, activeJ } = extra;
  matrixTitle.textContent = isBool ? 'Cerradura Transitiva' : 'Matriz de distancias';
  matrixOverlay.classList.remove('hidden');

  let html = '<table><tr><th></th>';
  vertices.forEach(v => { html += `<th>${v}</th>`; });
  html += '</tr>';

  matrix.forEach((row, i) => {
    html += `<tr><th>${vertices[i]}</th>`;
    row.forEach((cell, j) => {
      let cls = '';
      if (i === activeI && j === activeJ) cls = 'active-ij';
      const display = isBool ? (cell ? '1' : '0') : (cell >= INF ? '∞' : String(cell));
      html += `<td class="${cls}">${display}</td>`;
    });
    html += '</tr>';
  });
  html += '</table>';
  matrixTable.innerHTML = html;
}

// ─── Playback controls ─────────────────────────────────────────────────────────

function resetPlayback() {
  steps = [];
  currentStep = 0;
  stepCounter.textContent = '—';
  stepDesc.textContent = 'Run an algorithm to begin';
  cy.nodes().forEach(n => { n.removeClass('white gray red orange black green'); n.data('label', n.id()); });
  cy.edges().removeClass('tree highlighted');
  dsPanel.classList.add('hidden');
  updatePlayControls();
}

function updatePlayControls() {
  const has = steps.length > 0;
  btnFirst.disabled = !has || currentStep === 0;
  btnPrev.disabled  = !has || currentStep === 0;
  btnNext.disabled  = !has || currentStep >= steps.length - 1;
  btnLast.disabled  = !has || currentStep >= steps.length - 1;
  btnPlay.disabled  = !has;
}

btnFirst.addEventListener('click', () => { stopPlay(); goStep(0); });
btnPrev.addEventListener ('click', () => { stopPlay(); goStep(currentStep - 1); });
btnNext.addEventListener ('click', () => { stopPlay(); goStep(currentStep + 1); });
btnLast.addEventListener ('click', () => { stopPlay(); goStep(steps.length - 1); });

btnPlay.addEventListener('click', () => {
  if (playTimer) stopPlay();
  else startPlay();
});

function goStep(n) {
  if (!steps.length) return;
  currentStep = Math.max(0, Math.min(n, steps.length - 1));
  renderStep(steps[currentStep]);
  updatePlayControls();
}

function startPlay() {
  if (currentStep >= steps.length - 1) currentStep = 0;
  btnPlay.textContent = '⏸';
  advance();
}

function advance() {
  if (currentStep >= steps.length - 1) { stopPlay(); return; }
  currentStep++;
  renderStep(steps[currentStep]);
  updatePlayControls();
  const delay = 2100 - parseInt(speedSlider.value, 10);
  playTimer = setTimeout(advance, Math.max(50, delay));
}

function stopPlay() {
  clearTimeout(playTimer);
  playTimer = null;
  btnPlay.textContent = '▶';
}

// ─── Keyboard shortcuts ────────────────────────────────────────────────────────

document.addEventListener('keydown', e => {
  if (e.target.tagName === 'INPUT' || e.target.tagName === 'SELECT') return;
  if (e.key === 'ArrowRight') { stopPlay(); goStep(currentStep + 1); }
  if (e.key === 'ArrowLeft')  { stopPlay(); goStep(currentStep - 1); }
  if (e.key === ' ')          { e.preventDefault(); btnPlay.click(); }
  if (e.key === 'v' || e.key === 'V') modeSelect.value = 'addVertex';
  if (e.key === 'e' || e.key === 'E') modeSelect.value = 'addEdge';
  if (e.key === 's' || e.key === 'S') modeSelect.value = 'select';
  if (e.key === 'd' || e.key === 'D') modeSelect.value = 'delete';
});

// Load a default example on startup
loadExample();

// ---------------- Data structure visualization -------------------------------------

const DataStructure_Fill = {
  white: '#bdc3d8', gray: '#f0a500', red: '#e94560',
  orange: '#ff6b35', black: '#4a4a6a', green: '#2ecc71'
};

function nodeTextColor(color) {
  return (color === 'red' || color === 'black') ? '#dde' : '#111';
}

const nodeStroke = "#30305a"; // default stroke for nodes in the data structures

// ─── Queue ───────────────────────────────────────────────────────

function renderQueue(queue, vertexColors) {
  if (!queue || queue.length === 0) { dsPanel.classList.add('hidden'); return; }
  dsPanel.classList.remove('hidden');

  dsPanelHint.textContent = 'Queue';

  const boxSize = 40;
  const boxPad = 8;
  const totalW = queue.length * (boxSize + boxPad) + boxPad;
  const boxH = 40;
  const svgH = 60;

  let htmlLabel = '';

  // Draw boxes for each element
  queue.forEach((vertex, idx) => {
    const x = boxPad + idx * (boxSize + boxPad);
    const y = 10;
    
    const color = (vertexColors && vertexColors[String(vertex)]) || 'white';
    const fill = DataStructure_Fill[color] || DataStructure_Fill.white;
    const tc = nodeTextColor(color);

    // Draw rectangle
    htmlLabel += `<rect x="${x}" y="${y}" width="${boxSize}" height="${boxH}" ` +
            `fill="${fill}" stroke="${nodeStroke}" stroke-width="1.5" rx="2"/>`;
    
    // Draw vertex id
    htmlLabel += `<text x="${(x + boxSize / 2).toFixed(1)}" y="${(y + boxH / 2 + 5).toFixed(1)}" ` +
            `text-anchor="middle" dominant-baseline="middle" ` +
            `font-size="14" font-weight="bold" fill="${tc}">v${vertex}</text>`;
  });

  dataStructSvg.setAttribute('width', totalW);
  dataStructSvg.setAttribute('height', svgH);
  dataStructSvg.setAttribute('viewBox', `0 0 ${totalW} ${svgH}`);
  dataStructSvg.innerHTML = htmlLabel;
}

// ─── Stack ───────────────────────────────────────────────────────
function renderStack(stack, vertexColors){
    if (!stack || stack.length === 0) { dsPanel.classList.add('hidden'); return; }

  dsPanel.classList.remove('hidden');

  dsPanelHint.textContent = 'Stack';

  const boxSize = 40;
  const boxPad = 8;
  const totalH = stack.length * (boxSize + boxPad) + boxPad;
  const boxH = 40;
  const svgW = 60;

  let htmlLabel = '';

  // Draw boxes for each element
  stack.forEach((vertex, idx) => {
    const x = 10;
    const y = boxPad + (stack.length - 1 - idx) * (boxSize + boxPad);
    
    const color = (vertexColors && vertexColors[String(vertex)]) || 'white';
    const fill = DataStructure_Fill[color] || DataStructure_Fill.white;
    const tc = nodeTextColor(color);

    // Draw rectangle
    htmlLabel += `<rect x="${x}" y="${y}" width="${boxSize}" height="${boxH}" ` +
            `fill="${fill}" stroke="${nodeStroke}" stroke-width="1.5" rx="2"/>`;
    
    // Draw vertex id
    htmlLabel += `<text x="${(x + boxSize / 2).toFixed(1)}" y="${(y + boxH / 2 + 5).toFixed(1)}" ` +
            `text-anchor="middle" dominant-baseline="middle" ` +
            `font-size="14" font-weight="bold" fill="${tc}">v${vertex}</text>`;
  });

  dataStructSvg.setAttribute('width', svgW);
  dataStructSvg.setAttribute('height', totalH);
  dataStructSvg.setAttribute('viewBox', `0 0 ${svgW} ${totalH}`);
  dataStructSvg.innerHTML = htmlLabel;
}

// ---- Binomial queue -------------------
function renderBinQueue(bqState, vertexColors){

  const BQ_LH   = 15;   // px between levels
  const BQ_R    = 21;   // node circle radius
  const BQ_PAD  = 15;   // px gap between separate nodes

  if (!bqState || bqState.length === 0) { dsPanel.classList.add('hidden'); return; }

  dsPanel.classList.remove('hidden');

  dsPanelHint.textContent = 'Binomial Queue';

  let leftPad = 20;
  let treeWidth = 0;
  const topPad = 20;
  let htmlLabel = '';
  let maxTreeHeight = 0;


  // Se dibujan primero las líneas
  for(const tree of bqState){
    tree.forEach((node, idx) => {
      if(idx === 0){
        if(node[0] > maxTreeHeight){
          maxTreeHeight = node[0];
        }
        treeWidth = node[1];
      } else {
        if(node[4] != -1){ // Solo se agrega la línea si tiene padre
          const x1 = node[3]*(BQ_PAD + 2*BQ_R) + leftPad + BQ_R;
          const y1 = node[2]*(BQ_LH + 2*BQ_R) + topPad + BQ_R;
          const x2 = node[5]*(BQ_PAD + 2*BQ_R) + leftPad + BQ_R;
          const y2 = node[4]*(BQ_LH + 2*BQ_R) + topPad + BQ_R;
          htmlLabel += `<line x1="${x1}" y1="${y1}" ` +
            `x2="${x2}" y2="${y2}" stroke="${DataStructure_Fill.white}" stroke-width="1.5"/>`;
        }
      }
    });
    leftPad += treeWidth*(BQ_PAD + 2*BQ_R);
  }

  leftPad = 20;
  // Luego los círculos y etiquetas
  for(const tree of bqState){
    tree.forEach((node, idx) => {
      if(idx === 0){
        treeWidth = node[1];
      } else {
        const x1 = node[3]*(BQ_PAD + 2*BQ_R) + leftPad + BQ_R;
        const y1 = node[2]*(BQ_LH + 2*BQ_R) + topPad + BQ_R;
        const color = (vertexColors && vertexColors[String(node[0])]) || 'white';
        const fill = DataStructure_Fill[color] || DataStructure_Fill.white;
        const tc = nodeTextColor(color);
        htmlLabel += `<circle cx="${x1}" cy="${y1}" r="${BQ_R}" ` +
            `fill="${fill}" stroke="#252540" stroke-width="${2}"/>`;
        
        let nodeLabel = String(node[0]) + ", ";
        if(node[1] >= 1073741823){
          nodeLabel += "∞";
        } else {
          nodeLabel += String(node[1]);
        }
        htmlLabel += `<text x="${x1}" y="${y1}" ` +
            `text-anchor="middle" dominant-baseline="middle" ` +
            `font-size="12" font-weight="bold" fill="${tc}">${nodeLabel}</text>`;
      }
    });
    leftPad += treeWidth*(BQ_PAD + 2*BQ_R);
  }

  const dsHeight = maxTreeHeight * (BQ_LH + 2 * BQ_R) + topPad + BQ_R;
  dataStructSvg.setAttribute('width', leftPad);
  dataStructSvg.setAttribute('height', dsHeight);
  dataStructSvg.setAttribute('viewBox', `0 0 ${leftPad} ${dsHeight}`);
  dataStructSvg.innerHTML = htmlLabel;
}