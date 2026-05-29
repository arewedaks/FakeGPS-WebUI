<?php
error_reporting(0);

$file_lokasi = __DIR__ . '/kordinat.json';

// Inisialisasi file kordinat.json jika belum ada
if (!file_exists($file_lokasi)) {
    $default_data = array("lat" => -6.175392, "lng" => 106.827153, "status" => "start");
    @file_put_contents($file_lokasi, json_encode($default_data));
}

$saved = false;
// Tangani form submit secara langsung di index.php
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $current = json_decode(@file_get_contents($file_lokasi), true);
    if (!is_array($current)) $current = [];

    if (isset($_POST['lat']) && isset($_POST['lng'])) {
        $current['lat'] = (float)$_POST['lat'];
        $current['lng'] = (float)$_POST['lng'];
    }
    
    if (isset($_POST['status'])) {
        $current['status'] = $_POST['status'];
    } else if (!isset($current['status'])) {
        $current['status'] = 'start';
    }

    // Tulis ke JSON
    @file_put_contents($file_lokasi, json_encode($current));
    
    $saved = true;
}

// Baca data JSON saat ini untuk ditampilkan di WebUI
$current = json_decode(@file_get_contents($file_lokasi), true);
if (!is_array($current)) {
    $current = array("lat" => -6.175392, "lng" => 106.827153, "status" => "start");
}

$lat = isset($current['lat']) ? $current['lat'] : -6.175392;
$lng = isset($current['lng']) ? $current['lng'] : 106.827153;
$status = isset($current['status']) ? $current['status'] : 'start';
$last_ping = isset($current['last_ping']) ? $current['last_ping'] : 0;
$is_alive = (time() - $last_ping) <= 10;
?>
<!DOCTYPE html>
<html lang="id">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>FakeGPS Premium Remote</title>
    <!-- Modern Font -->
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;600;800&display=swap" rel="stylesheet">
    <!-- Leaflet CSS for Map -->
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
    <!-- Leaflet JS -->
    <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
    <style>
        :root {
            --bg-dark: #09090b;
            --panel-bg: rgba(24, 24, 27, 0.7);
            --panel-border: rgba(255, 255, 255, 0.1);
            --text-main: #fafafa;
            --text-muted: #a1a1aa;
            --accent: #6366f1;
            --accent-hover: #4f46e5;
            --success: #10b981;
            --danger: #ef4444;
        }

        body {
            font-family: 'Outfit', sans-serif;
            background: radial-gradient(circle at top right, #1e1b4b, var(--bg-dark));
            color: var(--text-main);
            margin: 0;
            padding: 0;
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
            overflow-x: hidden;
        }

        .container {
            display: flex;
            flex-direction: column;
            gap: 20px;
            width: 100%;
            max-width: 900px;
            padding: 20px;
            z-index: 10;
        }

        @media (min-width: 768px) {
            .container {
                flex-direction: row;
            }
        }

        .glass-panel {
            background: var(--panel-bg);
            backdrop-filter: blur(12px);
            -webkit-backdrop-filter: blur(12px);
            border: 1px solid var(--panel-border);
            border-radius: 24px;
            padding: 30px;
            box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
        }

        .map-section {
            flex: 2;
            display: flex;
            flex-direction: column;
            overflow: hidden;
            padding: 20px;
        }

        #map {
            width: 100%;
            height: 400px;
            border-radius: 16px;
            border: 1px solid var(--panel-border);
            margin-top: 10px;
            z-index: 1; 
        }

        .control-section {
            flex: 1;
            display: flex;
            flex-direction: column;
        }

        h2 {
            margin-top: 0;
            font-weight: 800;
            font-size: 28px;
            background: linear-gradient(to right, #818cf8, #c084fc);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            margin-bottom: 20px;
        }

        .status-indicator {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            padding: 8px 16px;
            border-radius: 20px;
            font-size: 14px;
            font-weight: 600;
            margin-bottom: 25px;
            background: rgba(255, 255, 255, 0.05);
            border: 1px solid var(--panel-border);
            transition: all 0.3s ease;
        }

        .status-dot {
            width: 10px;
            height: 10px;
            border-radius: 50%;
            background-color: var(--text-muted);
            box-shadow: 0 0 0 rgba(0,0,0,0);
        }

        .status-indicator.active .status-dot {
            background-color: var(--success);
            box-shadow: 0 0 10px var(--success);
            animation: pulse 2s infinite;
        }

        @keyframes pulse {
            0% { box-shadow: 0 0 0 0 rgba(16, 185, 129, 0.7); }
            70% { box-shadow: 0 0 0 10px rgba(16, 185, 129, 0); }
            100% { box-shadow: 0 0 0 0 rgba(16, 185, 129, 0); }
        }

        .input-group {
            margin-bottom: 20px;
        }

        label {
            display: block;
            margin-bottom: 8px;
            font-size: 14px;
            color: var(--text-muted);
            font-weight: 600;
            text-transform: uppercase;
        }

        input {
            width: 100%;
            padding: 14px 16px;
            background-color: rgba(0,0,0,0.2);
            border: 1px solid var(--panel-border);
            border-radius: 12px;
            color: white;
            font-size: 16px;
            font-family: monospace;
            box-sizing: border-box;
            transition: all 0.3s ease;
        }

        input:focus {
            outline: none;
            border-color: var(--accent);
            box-shadow: 0 0 15px rgba(99, 102, 241, 0.3);
            background-color: rgba(0,0,0,0.4);
        }

        .btn {
            width: 100%;
            padding: 16px;
            border: none;
            border-radius: 12px;
            font-size: 16px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s ease;
            position: relative;
            overflow: hidden;
            font-family: 'Outfit', sans-serif;
            margin-bottom: 12px;
            color: white;
        }

        .btn-update {
            background: linear-gradient(135deg, var(--accent), var(--accent-hover));
            box-shadow: 0 10px 20px -10px var(--accent);
        }

        .action-row {
            display: flex;
            gap: 12px;
            margin-top: 15px;
        }

        .btn-start {
            background: linear-gradient(135deg, #059669, #10b981);
            box-shadow: 0 10px 20px -10px var(--success);
        }

        .btn-stop {
            background: linear-gradient(135deg, #b91c1c, #ef4444);
            box-shadow: 0 10px 20px -10px var(--danger);
        }

        .toast {
            position: fixed;
            bottom: 30px;
            left: 50%;
            transform: translateX(-50%) translateY(20px);
            background: rgba(255, 255, 255, 0.9);
            color: var(--bg-dark);
            padding: 12px 24px;
            border-radius: 30px;
            font-size: 15px;
            font-weight: 600;
            opacity: 0;
            transition: all 0.4s cubic-bezier(0.68, -0.55, 0.265, 1.55);
            pointer-events: none;
            box-shadow: 0 10px 30px rgba(0,0,0,0.5);
            z-index: 9999;
        }

        .toast.show {
            opacity: 1;
            transform: translateX(-50%) translateY(0);
        }
        
        .tip {
            font-size: 13px;
            color: var(--text-muted);
            margin-top: 15px;
            text-align: center;
        }
    </style>
</head>
<body>

    <form method="POST" action="" id="mainForm" class="container">
        <!-- Input status tersembunyi -->
        <input type="hidden" name="status" id="status" value="<?php echo htmlspecialchars($status); ?>">
        
        <div class="glass-panel map-section">
            <h2 style="font-size: 22px; margin-bottom: 5px;">Peta Lokasi</h2>
            <p style="color: var(--text-muted); font-size: 14px; margin-top: 0;">Geser pin atau klik pada peta untuk memilih titik koordinat secara langsung.</p>
            <div id="map"></div>
        </div>

        <div class="glass-panel control-section">
            <h2>FakeGPS UI</h2>
            
            <div id="statusBadge" class="status-indicator <?php echo $is_alive ? 'active' : ''; ?>">
                <div class="status-dot"></div>
                <span id="statusText"><?php echo $is_alive ? 'APK JALAN (TERHUBUNG)' : 'APK MATI (OFFLINE)'; ?></span>
            </div>

            <div class="input-group">
                <label>Latitude</label>
                <input type="text" name="lat" id="lat" value="<?php echo htmlspecialchars($lat); ?>" required>
            </div>
            
            <div class="input-group">
                <label>Longitude</label>
                <input type="text" name="lng" id="lng" value="<?php echo htmlspecialchars($lng); ?>" required>
            </div>

            <button type="submit" class="btn btn-update">📍 Perbarui Koordinat</button>

            <div class="action-row">
                <button type="button" class="btn btn-start" onclick="submitStatus('start')">▶ Mulai</button>
                <button type="button" class="btn btn-stop" onclick="submitStatus('stop')">⏹ Henti</button>
            </div>
            
            <p class="tip">Di-handle langsung oleh <b>index.php</b></p>
        </div>

    </form>

    <div id="toast" class="toast">Berhasil diupdate!</div>

    <script>
        // Ambil data langsung dari PHP backend di halaman ini
        let currentLat = <?php echo (float)$lat; ?>;
        let currentLng = <?php echo (float)$lng; ?>;
        let map, marker;

        // Initialize Map
        map = L.map('map').setView([currentLat, currentLng], 15);
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '&copy; OpenStreetMap contributors'
        }).addTo(map);

        marker = L.marker([currentLat, currentLng], { draggable: true }).addTo(map);

        // Update inputs when marker is dragged
        marker.on('dragend', function (e) {
            const pos = marker.getLatLng();
            document.getElementById('lat').value = pos.lat.toFixed(6);
            document.getElementById('lng').value = pos.lng.toFixed(6);
        });

        // Move marker and update inputs when map is clicked
        map.on('click', function(e) {
            marker.setLatLng(e.latlng);
            document.getElementById('lat').value = e.latlng.lat.toFixed(6);
            document.getElementById('lng').value = e.latlng.lng.toFixed(6);
        });
        
        // Listener untuk tombol start/stop
        function submitStatus(newStatus) {
            document.getElementById('status').value = newStatus;
            document.getElementById('mainForm').submit();
        }

        // Polling status APK setiap 3 detik tanpa refresh halaman
        setInterval(() => {
            fetch('kordinat.json?t=' + Date.now())
                .then(res => res.json())
                .then(data => {
                    const lastPing = data.last_ping || 0;
                    const now = Math.floor(Date.now() / 1000);
                    const isAlive = (now - lastPing) <= 10;
                    
                    const badge = document.getElementById('statusBadge');
                    const text = document.getElementById('statusText');
                    
                    if (isAlive) {
                        badge.classList.add('active');
                        text.textContent = 'APK JALAN (TERHUBUNG)';
                    } else {
                        badge.classList.remove('active');
                        text.textContent = 'APK MATI (OFFLINE)';
                    }
                }).catch(e => console.log('Ping check error', e));
        }, 3000);

        // Tampilkan toast jika berhasil simpan POST
        const isSaved = <?php echo $saved ? 'true' : 'false'; ?>;
        if (isSaved) {
            const toast = document.getElementById('toast');
            toast.textContent = '🚀 Koordinat tersimpan di kordinat.json!';
            toast.classList.add('show');
            setTimeout(() => { toast.classList.remove('show'); }, 2500);
            
            // Hapus re-submit state dari history browser
            if (window.history.replaceState) {
                window.history.replaceState(null, null, window.location.href);
            }
        }
    </script>
</body>
</html>
