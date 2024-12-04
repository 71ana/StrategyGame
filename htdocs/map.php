<?php
// map.php

// Preluăm harta jocului
$apiUrlMap = "http://localhost:8080/map";
$ch = curl_init($apiUrlMap);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
$responseMap = curl_exec($ch);
curl_close($ch);
$map = json_decode($responseMap, true);

// Mutarea jucătorului
if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $playerId = $_POST['playerId'];
    $newX = $_POST['x'];
    $newY = $_POST['y'];

    // Endpoint-ul pentru mutarea jucătorului
    $apiUrl = "http://localhost:8080/players/{$playerId}/move?x={$newX}&y={$newY}";

    $ch = curl_init($apiUrl);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_POST, true); // Cerere POST fără corp
    curl_setopt($ch, CURLOPT_HTTPHEADER, ['Content-Type: application/json']);

    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE); // Codul HTTP al răspunsului
    curl_close($ch);

    if ($httpCode !== 200) {
        // Afișăm mesajele de eroare în cazul unui răspuns incorect
        echo "<p>Eroare la mutarea jucătorului!</p>";
        echo "<p>Cod HTTP: {$httpCode}</p>";
        echo "<p>Răspuns server: {$response}</p>";
        exit();
    }

    // Redirecționare în caz de succes
    header("Location: players.php");
    exit();
}
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Harta Jocului</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background-image: url('fundal.jpg');
            background-size: cover;
            background-position: center center;
            background-attachment: fixed;
            color: white;
            text-align: center;
        }

        .div-1 {
            color: black;
        }

        h1 {
            margin-top: 20px;
        }

        table.map {
            margin: 20px auto;
            border-collapse: collapse;
            background-color: rgba(255, 255, 255, 0.6);
        }

        table.map td {
            width: 100px;
            height: 100px;
            border: 2px solid #000;
            text-align: center;
            vertical-align: middle;
            padding: 5px;
        }

        table.map td.white {
            background-color: #FFFFFF;
            color: black;
        }

        table.map td.black {
            background-color: #000000;
            color: white;
        }

        table.map td p {
            margin: 0;
            font-size: 12px;
            text-align: center;
        }

        form {
            margin-top: 20px;
            background-color: rgba(255, 255, 255, 0.8);
            padding: 20px;
            border-radius: 8px;
        }

        label, input, button {
            display: block;
            margin: 10px;
        }

        input, button {
            padding: 10px;
            font-size: 1rem;
        }

        button {
            background-color: #3498db;
            color: white;
            border: none;
            border-radius: 4px;
        }

        button:hover {
            background-color: #2980b9;
        }
    </style>
</head>
<body>
    <h1>Harta Jocului</h1>
    <table class="map">
        <?php
        $numColumns = 10; // Numărul de coloane pe orizontală
        $rowCount = ceil(count($map) / $numColumns); // Calculăm numărul de rânduri
        $cellIndex = 0;
        for ($row = 0; $row < $rowCount; $row++): ?>
            <tr>
                <?php
                for ($col = 0; $col < $numColumns; $col++):
                    if ($cellIndex < count($map)):
                        $cell = $map[$cellIndex];
                        $cellColorClass = getCellColorClass($cell['x'], $cell['y']);
                        ?>
                        <td class="<?= $cellColorClass ?>">
                            <p>Coordonate: (<?= $cell['x'] ?>, <?= $cell['y'] ?>)</p>
                            <p>Resursă: <?= $cell['resource'] ?? 'Niciuna' ?></p>
                        </td>
                        <?php
                        $cellIndex++;
                    else:
                        ?>
                        <td></td>
                    <?php endif; endfor; ?>
            </tr>
        <?php endfor; ?>
    </table>

    <h2>Mută Jucător</h2>
    <form method="POST" action="map.php">
        <center>
            <div class="div-1">ID</div>
            <input type="text" name="playerId" required>

            <div class="div-1">Noua coordonată X:</div>
            <input type="text" name="x" required>

            <div class="div-1">Noua coordonată Y:</div>
            <input type="text" name="y" required>

            <button type="submit">Mută</button>
        </center>
    </form>
</body>
</html>

<?php
// Funcția pentru a determina culoarea celulei
function getCellColorClass($x, $y) {
    // Alternăm culorile în funcție de paritatea coordonatelor
    if (($x + $y) % 2 == 0) {
        return 'white'; // Fundal alb, text negru
    } else {
        return 'black'; // Fundal negru, text alb
    }
}
?>
