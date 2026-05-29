<?php
$file_lokasi = 'kordinat.json';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $lat = $_POST['lat'];
    $lng = $_POST['lng'];
    $data = array("lat" => (float)$lat, "lng" => (float)$lng);
    file_put_contents($file_lokasi, json_encode($data));
    echo "OK";
} else {
    header('Content-Type: application/json');
    if (file_exists($file_lokasi)) {
        echo file_get_contents($file_lokasi);
    } else {
        echo json_encode(array("lat" => -6.175392, "lng" => 106.827153));
    }
}
?>
