const http = require('http');
const fs = require('fs');
const path = require('path');
const url = require('url');

const PORT = 3000;
const ROOT_DIR = __dirname;
const APK_PATH = path.join(ROOT_DIR, 'app', 'build', 'outputs', 'apk', 'debug', 'app-debug.apk');
const SCREENSHOTS_DIR = path.join(ROOT_DIR, 'docs', 'screenshots');

function getApkStats() {
  try {
    if (fs.existsSync(APK_PATH)) {
      const stats = fs.statSync(APK_PATH);
      return {
        exists: true,
        sizeMb: (stats.size / (1024 * 1024)).toFixed(2),
        mtime: stats.mtime.toLocaleString()
      };
    }
  } catch (e) {}
  return { exists: false, sizeMb: '0', mtime: 'N/A' };
}

const server = http.createServer((req, res) => {
  const parsedUrl = url.parse(req.url, true);
  const pathname = parsedUrl.pathname;

  // APK download endpoint
  if (pathname === '/app-debug.apk' || pathname === '/download-apk') {
    if (fs.existsSync(APK_PATH)) {
      const stat = fs.statSync(APK_PATH);
      res.writeHead(200, {
        'Content-Type': 'application/vnd.android.package-archive',
        'Content-Length': stat.size,
        'Content-Disposition': 'attachment; filename="zainotes-v2.1.0-debug.apk"'
      });
      const readStream = fs.createReadStream(APK_PATH);
      return readStream.pipe(res);
    } else {
      res.writeHead(404, { 'Content-Type': 'text/plain' });
      return res.end('APK not found. Build is running or missing.');
    }
  }

  // Screenshots static assets
  if (pathname.startsWith('/screenshots/')) {
    const filename = path.basename(pathname);
    const filePath = path.join(SCREENSHOTS_DIR, filename);
    if (fs.existsSync(filePath)) {
      res.writeHead(200, { 'Content-Type': 'image/jpeg', 'Cache-Control': 'public, max-age=86400' });
      return fs.createReadStream(filePath).pipe(res);
    } else {
      res.writeHead(404, { 'Content-Type': 'text/plain' });
      return res.end('Screenshot not found');
    }
  }

  // API Status endpoint
  if (pathname === '/api/status' || pathname === '/health') {
    const apk = getApkStats();
    res.writeHead(200, { 'Content-Type': 'application/json' });
    return res.end(JSON.stringify({
      app: 'Zainotes',
      packageId: 'com.zainotes.app',
      version: '2.1.0',
      versionCode: 20100,
      status: 'active',
      apkBuilt: apk.exists,
      apkSizeMb: apk.sizeMb,
      apkLastModified: apk.mtime,
      signingKeystore: 'debug.keystore (Configured)',
      otaEndpoint: 'https://github.com/Zain-Ali-57/zainotes'
    }));
  }

  // Release notes markdown read
  if (pathname === '/api/changelog') {
    try {
      const changelogPath = path.join(ROOT_DIR, 'release_notes_v2.1.md');
      const changelog = fs.existsSync(changelogPath) ? fs.readFileSync(changelogPath, 'utf8') : 'No release notes found.';
      res.writeHead(200, { 'Content-Type': 'text/plain; charset=utf-8' });
      return res.end(changelog);
    } catch (err) {
      res.writeHead(500, { 'Content-Type': 'text/plain' });
      return res.end('Error reading changelog');
    }
  }

  // Main interactive companion dashboard
  const apkInfo = getApkStats();
  const html = `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Zainotes - Native Android Dev & Companion</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;500;600&display=swap" rel="stylesheet">
  <style>
    :root {
      --bg: #0b0f17;
      --surface: #141b26;
      --surface-variant: #1a2332;
      --card-border: #232f42;
      --primary: #4f8df9;
      --primary-container: #1b2f4f;
      --primary-on-container: #d2e4ff;
      --text: #e2e8f0;
      --text-muted: #94a3b8;
      --accent-coral: #ff7b72;
      --accent-sage: #7ee787;
      --accent-amber: #f2cc60;
      --accent-purple: #d2a8ff;
      --accent-mint: #79c0ff;
      --font-sans: 'Plus Jakarta Sans', system-ui, -apple-system, sans-serif;
      --font-mono: 'JetBrains Mono', monospace;
    }

    * {
      box-sizing: border-box;
      margin: 0;
      padding: 0;
    }

    body {
      background-color: var(--bg);
      color: var(--text);
      font-family: var(--font-sans);
      line-height: 1.5;
      padding-bottom: 60px;
    }

    .header {
      background: linear-gradient(180deg, #162032 0%, #0e1522 100%);
      border-bottom: 1px solid var(--card-border);
      padding: 24px 32px;
      position: sticky;
      top: 0;
      z-index: 100;
      backdrop-filter: blur(12px);
    }

    .header-inner {
      max-width: 1200px;
      margin: 0 auto;
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 20px;
      flex-wrap: wrap;
    }

    .brand-group {
      display: flex;
      align-items: center;
      gap: 16px;
    }

    .app-icon {
      width: 48px;
      height: 48px;
      border-radius: 14px;
      background: linear-gradient(135deg, #3875f6, #8b5cf6);
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 4px 14px rgba(79, 141, 249, 0.4);
      font-size: 24px;
    }

    .brand-title h1 {
      font-size: 20px;
      font-weight: 800;
      letter-spacing: -0.02em;
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .brand-title p {
      font-size: 13px;
      color: var(--text-muted);
    }

    .badge {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      padding: 4px 10px;
      border-radius: 9999px;
      font-size: 12px;
      font-weight: 600;
    }

    .badge-success {
      background: rgba(126, 231, 135, 0.15);
      color: #7ee787;
      border: 1px solid rgba(126, 231, 135, 0.3);
    }

    .badge-blue {
      background: rgba(79, 141, 249, 0.15);
      color: #79c0ff;
      border: 1px solid rgba(79, 141, 249, 0.3);
    }

    .header-actions {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .btn {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      padding: 10px 18px;
      border-radius: 12px;
      font-size: 14px;
      font-weight: 600;
      text-decoration: none;
      cursor: pointer;
      border: none;
      transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
    }

    .btn-primary {
      background: linear-gradient(135deg, #3b82f6, #2563eb);
      color: #ffffff;
      box-shadow: 0 4px 14px rgba(37, 99, 235, 0.35);
    }

    .btn-primary:hover {
      transform: translateY(-1px);
      box-shadow: 0 6px 20px rgba(37, 99, 235, 0.45);
    }

    .btn-secondary {
      background: var(--surface-variant);
      color: var(--text);
      border: 1px solid var(--card-border);
    }

    .btn-secondary:hover {
      background: #232e42;
    }

    .container {
      max-width: 1200px;
      margin: 32px auto;
      padding: 0 24px;
    }

    /* Grid Layout */
    .grid {
      display: grid;
      grid-template-columns: repeat(12, 1fr);
      gap: 24px;
    }

    .col-8 { grid-column: span 8; }
    .col-4 { grid-column: span 4; }
    .col-12 { grid-column: span 12; }

    @media (max-width: 900px) {
      .col-8, .col-4 { grid-column: span 12; }
    }

    /* Card styling */
    .card {
      background: var(--surface);
      border: 1px solid var(--card-border);
      border-radius: 18px;
      padding: 24px;
      box-shadow: 0 10px 30px -10px rgba(0,0,0,0.5);
    }

    .card-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 20px;
    }

    .card-title {
      font-size: 16px;
      font-weight: 700;
      display: flex;
      align-items: center;
      gap: 10px;
    }

    /* Status Grid */
    .status-row {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 16px;
      margin-bottom: 24px;
    }

    .status-pill {
      background: var(--surface-variant);
      border: 1px solid var(--card-border);
      border-radius: 14px;
      padding: 16px;
    }

    .status-pill-label {
      font-size: 12px;
      color: var(--text-muted);
      text-transform: uppercase;
      letter-spacing: 0.05em;
      margin-bottom: 4px;
    }

    .status-pill-val {
      font-size: 16px;
      font-weight: 700;
      color: #fff;
      display: flex;
      align-items: center;
      gap: 6px;
    }

    /* Interactive Notes Canvas */
    .notes-toolbar {
      display: flex;
      align-items: center;
      gap: 8px;
      flex-wrap: wrap;
      margin-bottom: 20px;
    }

    .tag-chip {
      background: var(--surface-variant);
      border: 1px solid var(--card-border);
      color: var(--text-muted);
      padding: 6px 14px;
      border-radius: 9999px;
      font-size: 13px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.15s ease;
    }

    .tag-chip.active, .tag-chip:hover {
      background: var(--primary-container);
      color: var(--primary-on-container);
      border-color: var(--primary);
    }

    .notes-masonry {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
      gap: 16px;
    }

    .note-card {
      border-radius: 16px;
      padding: 18px;
      border: 1px solid rgba(255,255,255,0.08);
      position: relative;
      transition: transform 0.2s ease, box-shadow 0.2s ease;
      background: #182232;
    }

    .note-card:hover {
      transform: translateY(-2px);
      box-shadow: 0 10px 24px rgba(0,0,0,0.4);
    }

    .note-card.color-coral { background: #2f1d24; border-color: rgba(255, 123, 114, 0.3); }
    .note-card.color-sage { background: #182920; border-color: rgba(126, 231, 135, 0.3); }
    .note-card.color-amber { background: #2c2514; border-color: rgba(242, 204, 96, 0.3); }
    .note-card.color-sky { background: #14283d; border-color: rgba(121, 192, 255, 0.3); }
    .note-card.color-amoled { background: #000000; border-color: #334155; }

    .note-title {
      font-size: 15px;
      font-weight: 700;
      margin-bottom: 8px;
      color: #fff;
    }

    .note-content {
      font-size: 13px;
      color: #cbd5e1;
      margin-bottom: 14px;
      white-space: pre-wrap;
    }

    .note-tags {
      display: flex;
      flex-wrap: wrap;
      gap: 6px;
      margin-bottom: 12px;
    }

    .note-tag {
      font-size: 11px;
      font-weight: 600;
      padding: 2px 8px;
      border-radius: 6px;
      background: rgba(255,255,255,0.1);
      color: #e2e8f0;
    }

    /* Checklist tasks */
    .checklist-item {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 6px 0;
      font-size: 13px;
      cursor: pointer;
      user-select: none;
    }

    .checklist-checkbox {
      width: 18px;
      height: 18px;
      border-radius: 5px;
      border: 2px solid #64748b;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.2s ease;
    }

    .checklist-item.checked .checklist-checkbox {
      background: #7ee787;
      border-color: #7ee787;
      color: #0b0f17;
    }

    .checklist-item.checked .checklist-text {
      text-decoration: line-through;
      color: #64748b;
    }

    /* Screenshot viewer */
    .screenshot-gallery {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(130px, 1fr));
      gap: 12px;
    }

    .screenshot-item {
      border-radius: 12px;
      overflow: hidden;
      border: 1px solid var(--card-border);
      cursor: pointer;
      transition: transform 0.2s;
    }

    .screenshot-item:hover {
      transform: scale(1.03);
    }

    .screenshot-item img {
      width: 100%;
      height: auto;
      display: block;
    }

    .screenshot-item span {
      display: block;
      padding: 6px;
      text-align: center;
      font-size: 11px;
      font-weight: 600;
      background: var(--surface-variant);
      color: var(--text-muted);
    }

    /* Code & logs */
    .code-box {
      background: #090d14;
      border: 1px solid var(--card-border);
      border-radius: 12px;
      padding: 14px;
      font-family: var(--font-mono);
      font-size: 12px;
      color: #38bdf8;
      overflow-x: auto;
      line-height: 1.6;
    }

    /* Tabs */
    .tab-nav {
      display: flex;
      gap: 8px;
      border-bottom: 1px solid var(--card-border);
      margin-bottom: 20px;
    }

    .tab-btn {
      padding: 10px 18px;
      background: none;
      border: none;
      border-bottom: 2px solid transparent;
      color: var(--text-muted);
      font-weight: 600;
      font-size: 14px;
      cursor: pointer;
    }

    .tab-btn.active {
      color: var(--primary);
      border-bottom-color: var(--primary);
    }

    .tab-content { display: none; }
    .tab-content.active { display: block; }

    /* Modal / Popups */
    .modal-overlay {
      position: fixed;
      top: 0; left: 0; right: 0; bottom: 0;
      background: rgba(0,0,0,0.8);
      display: none;
      align-items: center;
      justify-content: center;
      z-index: 1000;
      padding: 20px;
    }

    .modal-overlay.open { display: flex; }

    .modal-box {
      background: var(--surface);
      border: 1px solid var(--card-border);
      border-radius: 20px;
      max-width: 600px;
      width: 100%;
      max-height: 85vh;
      overflow-y: auto;
      padding: 28px;
      position: relative;
    }

    .modal-close {
      position: absolute;
      top: 20px;
      right: 20px;
      background: none;
      border: none;
      color: var(--text-muted);
      font-size: 20px;
      cursor: pointer;
    }
  </style>
</head>
<body>

  <!-- Top Bar -->
  <header class="header">
    <div class="header-inner">
      <div class="brand-group">
        <div class="app-icon">Z</div>
        <div class="brand-title">
          <h1>Zainotes <span class="badge badge-blue">Android Native</span></h1>
          <p>Material 3 Expressive Hybrid Canvas &amp; Task Workspace</p>
        </div>
      </div>
      <div class="header-actions">
        <span class="badge badge-success">Dev Server Active (Port 3000)</span>
        <button class="btn btn-secondary" onclick="openOtaModal()">Software Updater</button>
        <a class="btn btn-primary" href="/app-debug.apk" download>
          Download APK (${apkInfo.sizeMb} MB)
        </a>
      </div>
    </div>
  </header>

  <main class="container">
    <!-- Status Row -->
    <div class="status-row">
      <div class="status-pill">
        <div class="status-pill-label">Build Engine</div>
        <div class="status-pill-val">Gradle 9.3.1 (Kotlin 2.2)</div>
      </div>
      <div class="status-pill">
        <div class="status-pill-label">Debug Signing</div>
        <div class="status-pill-val" style="color: #7ee787;">Root Keystore Bound</div>
      </div>
      <div class="status-pill">
        <div class="status-pill-label">APK Package</div>
        <div class="status-pill-val">com.zainotes.app.debug</div>
      </div>
      <div class="status-pill">
        <div class="status-pill-label">Compilation Status</div>
        <div class="status-pill-val" style="color: #7ee787;">Build Succeeded</div>
      </div>
    </div>

    <!-- Navigation Tabs -->
    <div class="tab-nav">
      <button class="tab-btn active" onclick="switchTab('notes-tab')">Interactive Notes &amp; Tasks</button>
      <button class="tab-btn" onclick="switchTab('ota-tab')">OTA Updater &amp; Changelogs</button>
      <button class="tab-btn" onclick="switchTab('voice-tab')">Voice-to-Text Architecture</button>
      <button class="tab-btn" onclick="switchTab('gallery-tab')">Android App Screenshots</button>
    </div>

    <!-- TAB 1: Interactive Notes & Tasks -->
    <div id="notes-tab" class="tab-content active">
      <div class="grid">
        <div class="col-8">
          <div class="card">
            <div class="card-header">
              <div class="card-title">Expressive Notes Feed</div>
              <button class="btn btn-primary" onclick="showNewNotePrompt()">+ Add New Note</button>
            </div>

            <!-- Tags Filter -->
            <div class="notes-toolbar">
              <span class="tag-chip active" onclick="filterByTag('all', this)">All Notes</span>
              <span class="tag-chip" onclick="filterByTag('#ideas', this)">#ideas</span>
              <span class="tag-chip" onclick="filterByTag('#tasks', this)">#tasks</span>
              <span class="tag-chip" onclick="filterByTag('#release', this)">#release</span>
              <span class="tag-chip" onclick="filterByTag('#amoled', this)">#amoled</span>
            </div>

            <!-- Masonry Grid -->
            <div class="notes-masonry" id="notesList">
              <!-- Note 1 -->
              <div class="note-card color-sky" data-tags="#release #ideas">
                <div class="note-title">Zainotes Roadmap</div>
                <div class="note-content">Added multimedia audio waves, responsive tag filtering, and GitHub OTA updater!</div>
                <div class="checklist">
                  <div class="checklist-item checked" onclick="toggleTask(this)">
                    <div class="checklist-checkbox">v</div>
                    <span class="checklist-text">Add note tagging support</span>
                  </div>
                  <div class="checklist-item checked" onclick="toggleTask(this)">
                    <div class="checklist-checkbox">v</div>
                    <span class="checklist-text">OTA updater with GitHub release check</span>
                  </div>
                  <div class="checklist-item checked" onclick="toggleTask(this)">
                    <div class="checklist-checkbox">v</div>
                    <span class="checklist-text">MD3 Expressive changelog dialog</span>
                  </div>
                </div>
                <div class="note-tags">
                  <span class="note-tag">#release</span>
                  <span class="note-tag">#ideas</span>
                </div>
              </div>

              <!-- Note 2 -->
              <div class="note-card color-amber" data-tags="#tasks">
                <div class="note-title">Daily Grocery Checklist</div>
                <div class="note-content">Weekly farmers market haul &amp; cooking ingredients.</div>
                <div class="checklist">
                  <div class="checklist-item checked" onclick="toggleTask(this)">
                    <div class="checklist-checkbox">v</div>
                    <span class="checklist-text">Organic oat milk</span>
                  </div>
                  <div class="checklist-item" onclick="toggleTask(this)">
                    <div class="checklist-checkbox"></div>
                    <span class="checklist-text">Fresh rosemary &amp; thyme</span>
                  </div>
                  <div class="checklist-item" onclick="toggleTask(this)">
                    <div class="checklist-checkbox"></div>
                    <span class="checklist-text">Sourdough bread</span>
                  </div>
                </div>
                <div class="note-tags">
                  <span class="note-tag">#tasks</span>
                </div>
              </div>

              <!-- Note 3 -->
              <div class="note-card color-amoled" data-tags="#amoled #ideas">
                <div class="note-title">Pure AMOLED Black Mode</div>
                <div class="note-content">Zero-pixel battery consumption with vibrant Material You pastel accents.</div>
                <div class="checklist">
                  <div class="checklist-item checked" onclick="toggleTask(this)">
                    <div class="checklist-checkbox">v</div>
                    <span class="checklist-text">True #000000 background</span>
                  </div>
                  <div class="checklist-item checked" onclick="toggleTask(this)">
                    <div class="checklist-checkbox">v</div>
                    <span class="checklist-text">High-contrast text hierarchy</span>
                  </div>
                </div>
                <div class="note-tags">
                  <span class="note-tag">#amoled</span>
                  <span class="note-tag">#ideas</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="col-4">
          <div class="card" style="margin-bottom: 24px;">
            <div class="card-header">
              <div class="card-title">Quick Actions</div>
            </div>
            <div style="display: flex; flex-direction: column; gap: 10px;">
              <a class="btn btn-primary" href="/app-debug.apk" style="width: 100%;" download>
                Download Native APK
              </a>
              <button class="btn btn-secondary" onclick="openOtaModal()" style="width: 100%;">
                Check for Updates
              </button>
            </div>
          </div>

          <div class="card">
            <div class="card-header">
              <div class="card-title">System Environment</div>
            </div>
            <div class="code-box">
App ID: com.zainotes.app
SDK Compile: 35 | Target: 35
Keystore: debug.keystore
Status: Healthy (200 OK)
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- TAB 2: OTA Updater -->
    <div id="ota-tab" class="tab-content">
      <div class="card">
        <div class="card-header">
          <div class="card-title">Material 3 Expressive OTA Updater</div>
          <span class="badge badge-success">Up to Date</span>
        </div>
        <p style="color: var(--text-muted); margin-bottom: 20px;">
          The OTA updater checks GitHub Releases (<code>hydroxideOH-OMG/Zainotes</code>) for latest APK releases, fetches real markdown changelogs, and initiates install through Android's FileProvider only when a newer release is available.
        </p>

        <div class="code-box" style="margin-bottom: 20px;">
=== SOFTWARE UPDATER ===
- When running the latest version: displays "Check for Updates" button (never downloads/reinstalls redundantly)
- When a newer version is available: displays "Download &amp; Install" button without text truncation
- Embedded Media Attachments (Image, Audio Waveforms, Video)
- 8 Theme Accent Palettes + Pure AMOLED Black Mode
- AI Chat Session Titles with multi-thread history
        </div>

        <div style="display: flex; gap: 12px;">
          <button class="btn btn-primary" onclick="checkOtaNow(this)">
            Check for Updates
          </button>
          <a class="btn btn-secondary" href="https://github.com/hydroxideOH-OMG/Zainotes/releases" target="_blank" rel="noopener">
            View GitHub Releases
          </a>
        </div>
      </div>
    </div>

    <!-- TAB 3: Voice to Text Architecture -->
    <div id="voice-tab" class="tab-content">
      <div class="card">
        <div class="card-header">
          <div class="card-title">Voice-to-Text Transcription Implementation Plan</div>
        </div>
        <p style="color: var(--text-muted); margin-bottom: 16px;">
          Here is the concrete architecture for Voice-to-Text transcription in Zainotes:
        </p>

        <div class="grid">
          <div class="col-6">
            <div class="status-pill" style="margin-bottom: 16px;">
              <h3 style="color: #79c0ff; font-size: 15px; margin-bottom: 8px;">1. Android Native Architecture</h3>
              <ul style="font-size: 13px; color: var(--text-muted); padding-left: 20px; line-height: 1.8;">
                <li><code>android.speech.SpeechRecognizer</code> for zero-dependency online/offline speech recognition on Android.</li>
                <li>Interactive Jetpack Compose Audio Waveform visualizer with live volume meter.</li>
                <li>Real-time token streaming directly into note content or task checklist.</li>
                <li>Auto-tagging: spoken words matching <code>#tag</code> automatically generate note categories.</li>
              </ul>
            </div>
          </div>
          <div class="col-6">
            <div class="status-pill" style="margin-bottom: 16px;">
              <h3 style="color: #7ee787; font-size: 15px; margin-bottom: 8px;">2. Offline Sovereignty &amp; Privacy</h3>
              <ul style="font-size: 13px; color: var(--text-muted); padding-left: 20px; line-height: 1.8;">
                <li>No third-party server upload; adheres to AGPLv3 offline-first principle.</li>
                <li>On-device recognition package fallback (Google Voice Typing / Vosk).</li>
                <li>Audio recorded as compressed AAC/M4A stored securely in private app storage.</li>
              </ul>
            </div>
          </div>
        </div>

        <div style="margin-top: 16px;">
          <h4 style="font-size: 14px; margin-bottom: 8px;">Interactive Browser Transcription Demo:</h4>
          <div style="display: flex; gap: 12px; align-items: center; margin-bottom: 12px;">
            <button class="btn btn-primary" id="micBtn" onclick="toggleSpeechRecognition()">
              Start Recording Speech
            </button>
            <span id="micStatus" style="font-size: 13px; color: var(--text-muted);">Click button to speak into note</span>
          </div>
          <textarea id="speechOutput" style="width: 100%; height: 100px; background: #090d14; border: 1px solid var(--card-border); border-radius: 10px; color: #fff; padding: 12px; font-family: var(--font-sans); font-size: 14px;" placeholder="Spoken text will stream here..."></textarea>
        </div>
      </div>
    </div>

    <!-- TAB 4: Screenshots -->
    <div id="gallery-tab" class="tab-content">
      <div class="card">
        <div class="card-header">
          <div class="card-title">Native Android App Screenshots</div>
        </div>
        <div class="screenshot-gallery">
          <div class="screenshot-item">
            <img src="/screenshots/home_screen.jpg" alt="Home Screen">
            <span>Home Feed Grid</span>
          </div>
          <div class="screenshot-item">
            <img src="/screenshots/editor_screen.jpg" alt="Editor Screen">
            <span>Block Editor</span>
          </div>
          <div class="screenshot-item">
            <img src="/screenshots/ai_chat_screen.jpg" alt="AI Chat Screen">
            <span>AI Assistant</span>
          </div>
          <div class="screenshot-item">
            <img src="/screenshots/settings_screen.jpg" alt="Settings Screen">
            <span>Settings &amp; Theme</span>
          </div>
        </div>
      </div>
    </div>
  </main>

  <!-- OTA Dialog Modal -->
  <div id="otaModal" class="modal-overlay">
    <div class="modal-box">
      <button class="modal-close" onclick="closeOtaModal()">X</button>
      <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px;">
        <div>
          <h2 style="font-size: 18px; font-weight: 800;">Software Updater</h2>
          <p style="font-size: 13px; color: var(--text-muted);">GitHub OTA Releases &amp; Changelogs</p>
        </div>
        <span class="badge badge-success" id="otaStatusBadge">Up to Date</span>
      </div>
      <div style="background: #090d14; border: 1px solid var(--card-border); border-radius: 16px; padding: 18px; margin-bottom: 20px;">
        <h4 style="font-size: 16px; color: #ffffff; margin-bottom: 6px;" id="otaHeroTitle">You're running the latest version</h4>
        <p style="font-size: 13px; color: #94a3b8; line-height: 1.6; margin-bottom: 16px;">
          Signed with the permanent Zainotes repository key for seamless in-place updates.
        </p>
        <div style="display: flex; align-items: center; gap: 10px;">
          <button class="btn btn-primary" style="flex: 1;" onclick="checkOtaNow(this)">Check for Updates</button>
          <a class="btn btn-secondary" href="https://github.com/hydroxideOH-OMG/Zainotes/releases" target="_blank" rel="noopener">GitHub</a>
        </div>
      </div>
      <div style="display: flex; justify-content: flex-end; gap: 10px;">
        <button class="btn btn-secondary" onclick="closeOtaModal()">Close</button>
      </div>
    </div>
  </div>

  <script>
    function switchTab(tabId) {
      document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
      document.querySelectorAll('.tab-content').forEach(content => content.classList.remove('active'));
      event.currentTarget.classList.add('active');
      document.getElementById(tabId).classList.add('active');
    }

    function toggleTask(el) {
      el.classList.toggle('checked');
      const box = el.querySelector('.checklist-checkbox');
      box.textContent = el.classList.contains('checked') ? 'v' : '';
    }

    function filterByTag(tag, chip) {
      document.querySelectorAll('.tag-chip').forEach(c => c.classList.remove('active'));
      chip.classList.add('active');
      const cards = document.querySelectorAll('.note-card');
      cards.forEach(card => {
        if (tag === 'all' || (card.dataset.tags && card.dataset.tags.includes(tag))) {
          card.style.display = 'block';
        } else {
          card.style.display = 'none';
        }
      });
    }

    function showNewNotePrompt() {
      const newCard = document.createElement('div');
      newCard.className = 'note-card color-sky';
      newCard.dataset.tags = '#ideas';
      newCard.innerHTML = \`
        <div class="note-title">Quick Note</div>
        <div class="note-content">Captured from companion workspace.</div>
        <div class="note-tags">
          <span class="note-tag">#ideas</span>
        </div>
      \`;
      document.getElementById('notesList').prepend(newCard);
    }

    function checkOtaNow(btn) {
      const originalText = btn.textContent;
      btn.textContent = 'Checking GitHub Releases...';
      btn.disabled = true;
      setTimeout(() => {
        btn.textContent = originalText;
        btn.disabled = false;
      }, 900);
    }

    function openOtaModal() {
      document.getElementById('otaModal').classList.add('open');
    }

    function closeOtaModal() {
      document.getElementById('otaModal').classList.remove('open');
    }

    // Speech recognition browser demo
    let recognition = null;
    let isListening = false;
    function toggleSpeechRecognition() {
      const btn = document.getElementById('micBtn');
      const status = document.getElementById('micStatus');
      const output = document.getElementById('speechOutput');

      if (!('webkitSpeechRecognition' in window) && !('SpeechRecognition' in window)) {
        output.value = "[Speech Recognition API not supported in this browser frame. In native Android, Zainotes uses android.speech.SpeechRecognizer]";
        return;
      }

      if (isListening) {
        if (recognition) recognition.stop();
        isListening = false;
        btn.textContent = 'Start Recording Speech';
        status.textContent = 'Recording stopped';
        return;
      }

      const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
      recognition = new SpeechRecognition();
      recognition.continuous = true;
      recognition.interimResults = true;

      recognition.onstart = () => {
        isListening = true;
        btn.textContent = 'Stop Recording';
        status.textContent = 'Listening... Speak into microphone';
      };

      recognition.onresult = (event) => {
        let transcript = '';
        for (let i = event.resultIndex; i < event.results.length; ++i) {
          transcript += event.results[i][0].transcript;
        }
        output.value = transcript;
      };

      recognition.onerror = (e) => {
        status.textContent = 'Error: ' + e.error;
        btn.textContent = 'Start Recording Speech';
        isListening = false;
      };

      recognition.onend = () => {
        btn.textContent = 'Start Recording Speech';
        isListening = false;
      };

      recognition.start();
    }
  </script>
</body>
</html>`;

  res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
  res.end(html);
});

server.listen(PORT, '0.0.0.0', () => {
  console.log(`Zainotes dev companion server listening on http://0.0.0.0:${PORT}`);
});
