<?php
session_start();

$timeout = 600;
if (isset($_SESSION['last_activity']) && (time() - $_SESSION['last_activity'] > $timeout)) {
    logout();
}
$_SESSION['last_activity'] = time();

if (isset($_GET['logout'])) {
    logout();
}

function logout() {
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

function fetchData($url) {
    $ch = curl_init($url);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    $response = curl_exec($ch);
    curl_close($ch);
    return json_decode($response, true);
}

$map = fetchData("http://localhost:8080/map");
$players = fetchData("http://localhost:8080/players");

if (!$map || !is_array($map)) {
    die("Failed to fetch or parse the game map.");
}

if (!$players || !is_array($players)) {
    header("Location: start.php");
    exit();
}

$mapSize = 10;

function fetchPlayerDetails($playerId) {
    $apiUrl = "http://localhost:8080/players/{$playerId}";
    $ch = curl_init($apiUrl);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    $response = curl_exec($ch);
    curl_close($ch);
    return json_decode($response, true);
}

$playerDetails = fetchPlayerDetails($playerId);

if (!$playerDetails || !is_array($playerDetails)) {
    die("Failed to fetch player details.");
}

//if ($playerDetails['state'] === 'gameOver') {
//    echo "
//    <div id='winner-popup' class='popup'>
//        <div class='popup-content'>
//            <p>Game Over!</strong>.</p>
//            <button onclick='redirectToStart()'>OK</button>
//        </div>
//    </div>
//    <script>
//        function redirectToStart() {
//            window.location.href = 'start.php';
//        }
//    </script>
//    <style>
//        .popup {
//            position: fixed;
//            top: 0;
//            left: 0;
//            width: 100%;
//            height: 100%;
//            background-color: rgba(0, 0, 0, 0.5);
//            display: flex;
//            align-items: center;
//            justify-content: center;
//            z-index: 1000;
//        }
//        .popup-content {
//            background-color: white;
//            padding: 20px;
//            border-radius: 8px;
//            text-align: center;
//        }
//    </style>
//    ";
//    exit();
//}

$inventory = $playerDetails['inventory'] ?? [];

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

function getCellColorClass($x, $y, $playerX, $playerY, $players) {
    foreach ($players as $player) {
        if ($player['x'] == $x && $player['y'] == $y && $player['id'] != $_SESSION['playerId']) {
            return 'enemy';
        }
    }

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
        curl_exec($ch);
        curl_close($ch);

        $playerDetails = fetchPlayerDetails($playerId);
        $inventory = $playerDetails['inventory'] ?? [];

        $map = fetchData("http://localhost:8080/map");
    } elseif (isset($_POST['move'])) {
        $newX = intval($_POST['x']);
        $newY = intval($_POST['y']);

        $apiUrl = "http://localhost:8080/players/{$playerId}/move?x={$newX}&y={$newY}";
        $ch = curl_init($apiUrl);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_POST, true);
        curl_setopt($ch, CURLOPT_HTTPHEADER, ['Content-Type: application/json']);
        curl_exec($ch);
        curl_close($ch);

        $_SESSION['playerX'] = $newX;
        $_SESSION['playerY'] = $newY;

        header("Location: map.php");
        $map = fetchData("http://localhost:8080/map");
        exit();
    } elseif (isset($_POST['build'])) {
        $apiUrl = "http://localhost:8080/players/build-house/{$playerId}";
        $ch = curl_init($apiUrl);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_POST, true);
        curl_setopt($ch, CURLOPT_HTTPHEADER, ['Content-Type: application/json']);
        $response = curl_exec($ch);
        $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
        curl_close($ch);

        if ($httpCode == 200) { // Success case
            $playerDetails = fetchPlayerDetails($playerId);
            $inventory = $playerDetails['inventory'] ?? [];
//            echo "
//        <div id='success-popup' class='popup'>
//            <div class='popup-content'>
//                <span class='close-btn' onclick='closePopup()'>&times;</span>
//                <p>House built successfully!</p>
//            </div>
//        </div>
//        <script>
//            function closePopup() {
//                document.getElementById('success-popup').style.display = 'none';
//            }
//        </script>
//        <style>
//            .popup {
//                position: fixed;
//                top: 0;
//                left: 0;
//                width: 100%;
//                height: 100%;
//                background-color: rgba(0, 0, 0, 0.5);
//                display: flex;
//                align-items: center;
//                justify-content: center;
//                z-index: 1000;
//            }
//            .popup-content {
//                background-color: black;
//                color: white;
//                padding: 20px;
//                border-radius: 8px;
//                text-align: center;
//                box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
//                position: relative;
//            }
//            .close-btn {
//                position: absolute;
//                top: 10px;
//                right: 10px;
//                font-size: 20px;
//                font-weight: bold;
//                cursor: pointer;
//            }
//            .close-btn:hover {
//                color: red;
//            }
//        </style>
//        ";
        } else { // Failure case
            echo "
        <div id='error-popup' class='popup'>
            <div class='popup-content'>
                <span class='close-btn' onclick='closePopup()'>&times;</span>
                <p>Failed to build house. Not enough resources!</p>
            </div>
        </div>
        <script>
            function closePopup() {
                document.getElementById('error-popup').style.display = 'none';
            }
        </script>
        <style>
            .popup {
                position: fixed;
                top: 0;
                left: 0;
                width: 100%;
                height: 100%;
                background-color: rgba(0, 0, 0, 0.5);
                display: flex;
                align-items: center;
                justify-content: center;
                z-index: 1000;
            }
            .popup-content {
                background-color: black;
                color: white;
                padding: 20px;
                border-radius: 8px;
                text-align: center;
                box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
                position: relative;
            }
            .close-btn {
                position: absolute;
                top: 10px;
                right: 10px;
                font-size: 20px;
                font-weight: bold;
                cursor: pointer;
            }
            .close-btn:hover {
                color: red;
            }
        </style>
        ";
        }
    }

    elseif (isset($_POST['trade'])) {
        $targetPlayerId = intval($_POST['targetPlayerId']);
        $resourceToGive = $_POST['resourceToGive'];
        $quantityToGive = intval($_POST['quantityToGive']);
        $resourceToReceive = $_POST['resourceToReceive'];
        $quantityToReceive = intval($_POST['quantityToReceive']);

        $apiUrl = "http://localhost:8080/players/{$playerId}/trade?targetPlayerId={$targetPlayerId}&resourceToGive={$resourceToGive}&quantityToGive={$quantityToGive}&resourceToReceive={$resourceToReceive}&quantityToReceive={$quantityToReceive}";
        $ch = curl_init($apiUrl);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_POST, true);
        curl_setopt($ch, CURLOPT_HTTPHEADER, ['Content-Type: application/json']);
        $response = curl_exec($ch);
        $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
        curl_close($ch);

        if ($httpCode == 200) {
            $playerDetails = fetchPlayerDetails($playerId);
            $inventory = $playerDetails['inventory'] ?? [];
            echo "<p>Trade successful!</p>";
            $inventory = $playerDetails['inventory'] ?? [];
        } else {
            echo "<p>Failed to trade. HTTP Code: $httpCode</p>";
        }
    }
}
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <script src="https://cdn.jsdelivr.net/npm/sockjs-client/dist/sockjs.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/@stomp/stompjs/dist/stomp.min.js"></script>

    <title>Game Map</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background-image: url('fundal.jpg');
            background-size: cover;
            background-position: center center;
            background-attachment: fixed;
            color: white;
            margin: 0;
            padding: 0;
            align-items: center;
            justify-content: center;
        }

        h1 {
            text-align: center;
            margin: 20px 0;
        }

        .container {
            display: flex;
            justify-content: center;
            align-items: flex-start;
            gap: 30px;
            padding: 20px;
            background-color: rgba(0, 0, 0, 0.2);
            border-radius: 10px;
        }

        table.map {
            border-collapse: collapse; /* Ensures clean edges */
            border-spacing: 0;
            width: 600px; /* Set a fixed width */
            height: 600px; /* Ensures the table is a perfect square */
            background-color: rgba(255, 255, 255, 0.6);
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
            border-radius: 10px;
            overflow: hidden;
        }

        table.map td {
            width: 10%; /* Divides the table into 10 columns */
            height: 10%; /* Divides the table into 10 rows */
            border: 1px solid #000;
            text-align: center;
            vertical-align: middle;
            font-size: 0.8rem;
            font-weight: bold;
            color: black;
            background-color: #fff; /* Default color */
            transition: transform 0.2s, box-shadow 0.2s;
        }

        table.map td:hover {
            transform: scale(1.1);
            box-shadow: 0 0 8px yellow;
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
        }

        table.map td.enemy {
            background-color: red;
            color: white;
        }

        .buttons-group {
            display: flex;
            flex-direction: column;
            gap: 10px;
            width: 200px;
            background-color: rgba(255, 255, 255, 0.2); /* Optional */
            padding: 10px;
            border-radius: 10px; /* Optional */
        }

        .inventory {
            padding: 15px;
            background-color: rgba(0, 0, 0, 0.7);
            color: #fff;
            border-radius: 10px;
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.4);
            width: 180px;
            text-align: center;
        }

        .inventory h2 {
            font-size: 1.5rem;
            margin-bottom: 10px;
            text-shadow: 1px 1px 4px black;
        }

        .inventory ul {
            list-style: none;
            padding: 0;
            margin: 0;
        }

        .inventory li {
            display: flex;
            justify-content: space-between;
            padding: 5px;
            margin: 3px 0;
            background-color: rgba(255, 255, 255, 0.2);
            border-radius: 10px;
            font-size: 0.9rem;
        }

        .inventory li strong {
            color: #ffcc00;
            font-weight: bold;
        }

        button {
            padding: 8px 20px;
            font-size: 0.9rem;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            transition: background-color 0.3s, transform 0.2s;
            color: white;
        }

        button:hover {
            transform: scale(1.05);
        }

        button:active {
            transform: scale(0.95);
        }

        button.logout-button {
            background-color: red;
            box-shadow: 0 4px 8px rgba(255, 0, 0, 0.4);
        }

        button.logout-button:hover {
            background-color: darkred;
            box-shadow: 0 6px 12px rgba(255, 0, 0, 0.6);
        }

        button.collect-button {
            background-color: #3498db;
            box-shadow: 0 4px 8px rgba(0, 123, 255, 0.4);
        }

        button.collect-button:hover {
            background-color: #217dbb;
            box-shadow: 0 6px 12px rgba(0, 123, 255, 0.6);
        }

        input {
            padding: 10px;
            font-size: 0.85rem;
            border: 1px solid #ccc;
            border-radius: 5px;
            width: 90%;
            margin: 5px auto;
            text-align: center;
        }

        input:focus {
            outline: none;
            border-color: #3498db;
            box-shadow: 0 0 5px rgba(52, 152, 219, 0.5);
        }

        form {
            margin-top: 5px;
            padding: 10px;
            background-color: rgba(255, 255, 255, 0.3);
            border-radius: 10px;
            display: flex;
            flex-direction: column;
            gap: 5px;
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
        }


    </style>
</head>


<body>
<h1>Game Map</h1>
<div class="container">
    <table class="map">
        <?php for ($row = 0; $row < $mapSize; $row++): ?>
            <tr>
                <?php for ($col = 0; $col < $mapSize; $col++):
                    $cell = array_filter($map, fn($c) => $c['x'] == $col && $c['y'] == $row);
                    $cell = reset($cell);
                    $cellColorClass = getCellColorClass($col, $row, $playerX, $playerY, $players);
                    $cellContent = getCellContent($col, $row, $players); ?>
                    <td class="<?= $cellColorClass ?>">
                        <p>(<?= $row ?>, <?= $col ?>)</p>
                        <p><?= $cell['resource'] ?? '' ?></p>
                        <p><?= $cellContent ?></p>
                    </td>
                <?php endfor; ?>
            </tr>
        <?php endfor; ?>
    </table>

    <div class="inventory">
        <h2>Your Inventory</h2>
        <?php if (!empty($inventory)): ?>
            <ul>
                <?php foreach ($inventory as $item => $quantity): ?>
                    <li>
                        <strong><?= htmlspecialchars(ucfirst($item)) ?>:</strong>
                        <span><?= htmlspecialchars($quantity) ?></span>
                    </li>
                <?php endforeach; ?>
            </ul>
        <?php else: ?>
            <p>Your inventory is empty.</p>
        <?php endif; ?>
    </div>


    <div class="buttons-group">
        <form method="POST">
            <button type="submit" name="collect" class="collect-button">Collect Resource</button>
        </form>

        <form method="POST">
            <button type="submit" name="build" class="collect-button">Build House</button>
        </form>

        <form method="POST">
            <input type="number" name="y" placeholder="New X Coordinate" required>
            <input type="number" name="x" placeholder="New Y Coordinate" required>
            <button type="submit" name="move" class="collect-button">Move</button>
        </form>
    </div>

    <div class="buttons-group">
        <form method="POST">
            <input type="number" name="targetPlayerId" placeholder="Target Player ID" required>
            <input type="text" name="resourceToGive" placeholder="Resource to Give" required>
            <input type="number" name="quantityToGive" placeholder="Quantity to Give" required>
            <input type="text" name="resourceToReceive" placeholder="Resource to Receive" required>
            <input type="number" name="quantityToReceive" placeholder="Quantity to Receive" required>
            <button type="submit" name="trade" class="collect-button">Trade</button>
        </form>

        <form method="GET">
            <button type="submit" name="logout" class="logout-button">Logout</button>
        </form>

    </div>
</div>
<script>
    const socket = new SockJS('http://localhost:8080/ws');
    const stompClient = Stomp.over(socket);

    stompClient.connect({}, function (frame) {
        console.log('Connected to WebSocket');
        stompClient.subscribe('/topic/winner', function (message) {
            const winnerMessage = message.body;
            const popup = document.createElement('div');
            popup.className = 'popup';
            popup.innerHTML = `
            <div class="popup-content">
                <p>${winnerMessage}</p>
                <button onclick="closePopup()">OK</button>
            </div>
        `;
            document.body.appendChild(popup);
        });
    });

    function closePopup() {
        const popup = document.querySelector('.popup');
        if (popup) {
            popup.remove();
        }
    }
</script>

</body>
</html>