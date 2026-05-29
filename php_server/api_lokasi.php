<?php
error_reporting(0);
header('Content-Type: application/json');

$file_lokasi = __DIR__ . '/kordinat.json';

// Create the file automatically with default values if it doesn't exist
if (!file_exists($file_lokasi)) {
    $default_data = array("lat" => -6.175392, "lng" => 106.827153, "status" => "start");
    @file_put_contents($file_lokasi, json_encode($default_data));
}

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $current = json_decode(file_get_contents($file_lokasi), true);
    if (!is_array($current)) $current = [];
    
    // Check if JSON or Form Data
    $contentType = isset($_SERVER["CONTENT_TYPE"]) ? trim($_SERVER["CONTENT_TYPE"]) : '';
    if (strpos($contentType, 'application/json') !== false) {
        $data = json_decode(file_get_contents('php://input'), true);
        if (isset($data['lat']) && isset($data['lng'])) {
            $current['lat'] = (float)$data['lat'];
            $current['lng'] = (float)$data['lng'];
        }
        if (isset($data['status'])) {
            $current['status'] = $data['status'];
        }
    } else {
        if (isset($_POST['lat']) && isset($_POST['lng'])) {
            $current['lat'] = (float)$_POST['lat'];
            $current['lng'] = (float)$_POST['lng'];
        }
        if (isset($_POST['status'])) {
            $current['status'] = $_POST['status'];
        }
    }
    
    if (!isset($current['status'])) {
        $current['status'] = 'start';
    }
    
    @file_put_contents($file_lokasi, json_encode($current));
    echo json_encode(['success' => true]);
} else {
    $current = json_decode(@file_get_contents($file_lokasi), true);
    if (!is_array($current)) $current = [];
    if (!isset($current['status'])) {
        $current['status'] = 'start';
    }
    
    // Simpan timestamp saat APK melakukan request (Heartbeat)
    $current['last_ping'] = time();
    @file_put_contents($file_lokasi, json_encode($current));
    
    echo json_encode($current);
}
?>
