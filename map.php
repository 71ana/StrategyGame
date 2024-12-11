<?php
session_start(); // Start session

// Retrieve player ID and position from the session
$playerId = $_SESSION['playerId'] ?? null;
$playerX = $_SESSION['playerX'] ?? null;
$playerY = $_SESSION['playerY'] ?? null;

if (!$playerId || $playerX === null || $playerY === null) {
    header("Location: start.php");
    exit();
}

// Fetch the game map from the API
$apiUrlMap = "http://localhost:8080/map";
$ch = curl_init($apiUrlMap);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
$responseMap = curl_exec($ch);
curl_close($ch);
$map = json_decode($responseMap, true);

if (!$map) {
    die("Failed to fetch the game map.");
}

// Function to determine cell color class
function getCellColorClass($x, $y, $playerX, $playerY) {
    if ($x == $playerX && $y == $playerY) {
        return 'highlight'; // Highlight the player's position
    }
    return (($x + $y) % 2 == 0) ? 'white' : 'black'; // Chessboard pattern
}

// Handle player movement (if form is submitted)
if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $newX = $_POST['x'];
    $newY = $_POST['y'];

    $apiUrl = "http://localhost:8080/players/{$playerId}/move?x={$newX}&y={$newY}";

    $ch = curl_init($apiUrl);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_HTTPHEADER, ['Content-Type: application/json']);

    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);

    if ($httpCode !== 200) {
        echo "<p>Eroare la mutarea jucătorului!</p>";
        echo "<p>Cod HTTP: {$httpCode}</p>";
        echo "<p>Răspuns server: {$response}</p>";
        exit();
    }

    // Update player position in session
    $_SESSION['playerX'] = $newX;
    $_SESSION['playerY'] = $newY;

    header("Location: map.php");
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

        table.map td.highlight {
            background-color: yellow;
            color: black;
            font-weight: bold;
        }

        form {
            margin-top: 20px;
            background-color: rgba(255, 255, 255, 0.8);
            padding: 20px;
            border-radius: 8px;
        }

        input, button {
            display: block;
            margin: 10px auto;
            padding: 10px;
            font-size: 1rem;
            width: 80%;
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
    $numColumns = 10; // Number of columns
    $rowCount = 10; // Number of rows
    $cellIndex = 0;

    for ($row = 0; $rowCount; $row++): ?>
        <tr>
            <?php for ($col = 0; $col < $numColumns; $col++): ?>
                <?php if ($cellIndex < count($map)):
                    $cell = $map[$cellIndex];
                    $cellColorClass = getCellColorClass($cell['x'], $cell['y'], $playerX, $playerY); ?>
                    <td class="<?= $cellColorClass ?>">
                        <p>Coordonate: (<?= $cell['x'] ?>, <?= $cell['y'] ?>)</p>
                        <p>Resursă: <?= $cell['resource'] ?? 'Niciuna' ?></p>
                    </td>
                    <?php $cellIndex++; ?>
                <?php else: ?>
                    <td></td>
                <?php endif; ?>
            <?php endfor; ?>
        </tr>
    <?php endfor; ?>
</table>

<h2>Muta Jucator</h2>
<form method="POST" action="map.php">
    <input type="number" name="x" placeholder="Noua coordonată X" required>
    <input type="number" name="y" placeholder="Noua coordonată Y" required>
    <button type="submit">Muta</button>
</form>
</body>
</html>
