<?php
session_start(); // Start the session

ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    // Get player name and coordinates from the form
    $playerName = $_POST['playerName'];
    $x = $_POST['x'];
    $y = $_POST['y'];

    // Step 1: Check if a player with the same name exists
    $checkUrl = "http://localhost:8080/players/by-name?playerName={$playerName}";
    $ch = curl_init($checkUrl);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);

    if ($httpCode === 200) {
        // Player exists, decode the response
        $player = json_decode($response, true);
        if (isset($player['id'])) {
            // Log in as existing player
            $_SESSION['playerId'] = $player['id'];
            $_SESSION['playerX'] = $player['x'];
            $_SESSION['playerY'] = $player['y'];

            // Redirect to map.php
            header("Location: map.php");
            exit();
        }
    }

    // Step 2: If player does not exist, create a new one
    $createUrl = "http://localhost:8080/players/create?playerName={$playerName}&x={$x}&y={$y}";
    $ch = curl_init($createUrl);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_POST, true); // POST request
    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);

    if ($httpCode === 200) {
        $player = json_decode($response, true);
        if (isset($player['id'])) {
            $_SESSION['playerId'] = $player['id'];
            $_SESSION['playerX'] = $x;
            $_SESSION['playerY'] = $y;

            // Redirect to map.php
            header("Location: map.php");
            exit();
        } else {
            echo "Failed to save player ID in session.";
        }
    } else {
        echo "API call failed with HTTP code: $httpCode";
        echo "<br>Response: " . htmlspecialchars($response);
    }
}
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Start Joc</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            text-align: center;
            background-color: #f4f4f4;
        }

        form {
            margin-top: 50px;
            padding: 20px;
            background-color: #fff;
            display: inline-block;
            border-radius: 8px;
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
        }

        input, button {
            margin: 10px 0;
            padding: 10px;
            font-size: 1rem;
            width: 100%;
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
<h1>House Builder</h1>
<form method="POST" action="start.php">
    <input type="text" name="playerName" placeholder="Numele jucatorului" required>
    <input type="number" name="x" placeholder="Coordonata Y" required>
    <input type="number" name="y" placeholder="Coordonata X" required>
    <button type="submit">Play</button>
</form>
</body>
</html>