<?php
session_start();

if (isset($_GET['logout'])) {
    $playerId = $_SESSION['playerId'] ?? null;

    if ($playerId) {
        $apiUrl = "http://localhost:8080/players/{$playerId}/logout";
        $ch = curl_init($apiUrl);
        curl_setopt($ch, CURLOPT_CUSTOMREQUEST, "DELETE");
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        $response = curl_exec($ch);
        $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
        curl_close($ch);

        if ($httpCode !== 204) {
            die("Failed to delete player session. HTTP Code: {$httpCode}");
        }
    }

    session_destroy();
    header("Location: start.php");
    exit();
}

$playerId = $_SESSION['playerId'] ?? null;
$playerX = $_SESSION['playerX'] ?? null;
$playerY = $_SESSION['playerY'] ?? null;

if (!$playerId || $playerX === null || $playerY === null) {
    header("Location: start.php");
    exit();
}

function fetchMapData() {
    $apiUrlMap = "http://localhost:8080/map";
    $ch = curl_init($apiUrlMap);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    $responseMap = curl_exec($ch);
    curl_close($ch);
    return json_decode($responseMap, true);
}

function fetchPlayersData() {
    $apiUrlPlayers = "http://localhost:8080/players";
    $ch = curl_init($apiUrlPlayers);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    $responsePlayers = curl_exec($ch);
    curl_close($ch);
    return json_decode($responsePlayers, true);
}

$map = fetchMapData();
$players = fetchPlayersData();

if (!$map || !is_array($map)) {
    die("Failed to fetch or parse the game map.");
}

if (!$players || !is_array($players)) {
    die("Failed to fetch or parse players data.");
}

$mapSize = 10;

function getCellContent($x, $y, $players) {
    $content = '';
    foreach ($players as $player) {
        if ($player['x'] == $x && $player['y'] == $y) {
            $playerName = $player['playerName'] ?? "Unknown Player";
            if ($player['id'] == $_SESSION['playerId']) {
                $content .= "<strong>You ({$playerName})</strong>";
            } else {
                $content .= "{$playerName}";
            }
        }
    }
    return $content;
}

function getCellColorClass($x, $y, $playerX, $playerY) {
    if ($x == $playerX && $y == $playerY) {
        return 'highlight';
    }
    return (($x + $y) % 2 == 0) ? 'white' : 'black';
}

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    if (isset($_POST['collect'])) {
        $apiUrl = "http://localhost:8080/players/{$playerId}/collect";
        $ch = curl_init($apiUrl);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_POST, true);
        curl_setopt($ch, CURLOPT_HTTPHEADER, ['Content-Type: application/json']);
        $response = curl_exec($ch);
        $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
        curl_close($ch);

        if ($httpCode === 200) {
            $map = fetchMapData();
            echo "<p>Resource collected successfully!</p>";
        } else {
            echo "<p>Failed to collect resource. HTTP Code: $httpCode</p>";
        }
    } elseif (isset($_POST['move'])) {
        $newX = intval($_POST['x']);
        $newY = intval($_POST['y']);

        $apiUrl = "http://localhost:8080/players/{$playerId}/move?x={$newX}&y={$newY}";
        $ch = curl_init($apiUrl);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_POST, true);
        curl_setopt($ch, CURLOPT_HTTPHEADER, ['Content-Type: application/json']);
        $response = curl_exec($ch);
        $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
        curl_close($ch);

        if ($httpCode !== 200) {
            echo "<p>Error moving player!</p>";
            echo "<p>HTTP Code: {$httpCode}</p>";
            echo "<p>Server Response: {$response}</p>";
            exit();
        }

        $_SESSION['playerX'] = $newX;
        $_SESSION['playerY'] = $newY;

        header("Location: map.php");
        exit();
    }
}
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Game Map</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background-image: url('fundal.jpg');
            background-size: cover;
            background-position: center center;
            background-attachment: fixed;
            color: white;
            text-align: center;
            margin: 0;
            padding: 0;
        }

        h1 {
            margin-top: 20px;
            font-size: 2.5rem;
            text-shadow: 2px 2px 5px black;
        }

        table.map {
            margin: 20px auto;
            border-collapse: separate;
            border-spacing: 5px;
            background-color: rgba(255, 255, 255, 0.6);
            aspect-ratio: 1/1;
            width: 80%;
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
            border-radius: 10px;
            overflow: hidden;
        }

        table.map td {
            width: 8%;
            height: 8%;
            border: 2px solid #000;
            text-align: center;
            vertical-align: middle;
            font-size: 0.9rem;
            font-weight: bold;
            color: black;
            transition: transform 0.2s, box-shadow 0.2s;
        }

        table.map td:hover {
            transform: scale(1.1);
            box-shadow: 0 0 10px yellow;
        }

        table.map td.white {
            background-color: #FFFFFF;
        }

        table.map td.black {
            background-color: #000000;
            color: white;
        }

        table.map td.highlight {
            background-color: yellow;
            color: black;
            font-weight: bold;
            border: 3px solid red;
        }

        button {
            padding: 15px 30px;
            font-size: 1rem;
            border: none;
            border-radius: 10px;
            margin: 10px;
            cursor: pointer;
            transition: background-color 0.3s, transform 0.2s;
        }

        button:hover {
            transform: scale(1.05);
        }

        button:active {
            transform: scale(0.95);
        }

        button.logout-button {
            background-color: red;
            color: white;
            box-shadow: 0 4px 8px rgba(255, 0, 0, 0.4);
        }

        button.logout-button:hover {
            background-color: darkred;
            box-shadow: 0 6px 12px rgba(255, 0, 0, 0.6);
        }

        button.collect-button {
            background-color: #3498db;
            color: white;
            box-shadow: 0 4px 8px rgba(0, 123, 255, 0.4);
        }

        button.collect-button:hover {
            background-color: #217dbb;
            box-shadow: 0 6px 12px rgba(0, 123, 255, 0.6);
        }

        form {
            margin-top: 20px;
            background-color: rgba(255, 255, 255, 0.9);
            padding: 20px;
            border-radius: 10px;
            display: inline-block;
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
        }

        input {
            padding: 10px;
            font-size: 1rem;
            border: 1px solid #ccc;
            border-radius: 5px;
            width: 80%;
            margin: 10px auto;
            display: block;
            text-align: center;
        }
    </style>
</head>
<body>
<h1>Game Map</h1>
<table class="map">
    <?php for ($row = 0; $row < $mapSize; $row++): ?>
        <tr>
            <?php for ($col = 0; $col < $mapSize; $col++):
                $cell = array_filter($map, fn($c) => $c['x'] == $col && $c['y'] == $row);
                $cell = reset($cell);
                $cellColorClass = getCellColorClass($col, $row, $playerX, $playerY);
                $cellContent = getCellContent($col, $row, $players); ?>
                <td class="<?= $cellColorClass ?>">
                    <p>(<?= $col ?>, <?= $row ?>)</p>
                    <p><?= $cell['resource'] ?? 'None' ?></p>
                    <p><?= $cellContent ?></p>
                </td>
            <?php endfor; ?>
        </tr>
    <?php endfor; ?>
</table>

<form method="POST" action="map.php">
    <button type="submit" name="collect" class="collect-button">Collect Resource</button>
</form>

<form method="POST" action="map.php">
    <input type="number" name="x" placeholder="New X Coordinate" required>
    <input type="number" name="y" placeholder="New Y Coordinate" required>
    <button type="submit" name="move" class="collect-button">Move</button>
</form>

<form method="GET" action="map.php">
    <button type="submit" name="logout" class="logout-button">Logout</button>
</form>

</body>
</html>
