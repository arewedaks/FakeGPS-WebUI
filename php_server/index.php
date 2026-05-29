<!DOCTYPE html>
<html>
<head>
    <title>Remote Fake GPS Dashboard</title>
    <style>
        body { font-family: Arial; padding: 20px; }
        input { padding: 10px; margin: 5px 0; width: 200px; }
        button { padding: 10px 20px; background: #28a745; color: white; border: none; cursor: pointer; }
    </style>
</head>
<body>
    <h2>Kontrol Lokasi Fake GPS</h2>
    <form action="api_lokasi.php" method="POST" target="hidden_iframe">
        <label>Latitude:</label><br>
        <input type="text" name="lat" value="-6.175392" required><br>
        <label>Longitude:</label><br>
        <input type="text" name="lng" value="106.827153" required><br>
        <button type="submit">Update Lokasi</button>
    </form>
    <iframe name="hidden_iframe" style="display:none;"></iframe>
    <p>Setelah klik Update, aplikasi Android akan otomatis bergeser ke lokasi ini dalam hitungan detik.</p>
</body>
</html>
