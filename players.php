<?php
$apiUrl = "http://localhost:8080/players";

$ch = curl_init($apiUrl);

curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_HTTPGET, true);

$response = curl_exec($ch);

curl_close($ch);

$players = json_decode($response, true);
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Jucători</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background-image: url('fundal.jpg');
            background-size: cover;
            background-position: center center;
            background-attachment: fixed;
            font-family: 'Arial', sans-serif;
            background-color: #f4f4f9;
            margin: 0;
            padding: 0;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
        }

        h1 {
            background-color:white;
            text-align: center;
            font-size: 2rem;
            color: black;
            margin-bottom: 20px;
        }

        table {
            width: 80%;
            border-collapse: collapse;
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
            background-color: #fff;
            border-radius: 8px;
            overflow: hidden;
        }

        table th, table td {
            padding: 12px 15px;
            text-align: center;
            border: 1px solid #ddd;
        }

        table th {
            background-color: #3498db;
            color: white;
            font-size: 1.1rem;
        }

        table tr:nth-child(even) {
            background-color: #f2f2f2;
        }

        table tr:hover {
            background-color: #e0e0e0;
            cursor: pointer;
        }

        table td {
            color: #555;
        }

        table td:nth-child(1), table td:nth-child(2) {
            font-weight: bold;
        }

        .button {
            background-color: #3498db;
            color: white;
            padding: 10px 20px;
            border: none;
            border-radius: 4px;
            text-align: center;
            text-decoration: none;
            display: inline-block;
            cursor: pointer;
        }

        .button:hover {
            background-color: #2980b9;
        }
    </style>
</head>
<body>
    <div>
        <h1>Players</h1>
        <table>
            <tr>
                <th>ID</th>
                <th>Nume</th>
                <th>Poziție</th>
                <th>Inventar</th> <!-- Coloana pentru inventar -->
            </tr>
            <?php foreach ($players as $player): ?>
                <tr>
                    <td><?= $player['id'] ?></td>
                    <td><?= $player['playerName'] ?></td>
                    <td>(<?= $player['x'] ?>, <?= $player['y'] ?>)</td>
	<td>
    <?php
        if (!empty($player['inventory'])):
            echo "<ul>"; 
            foreach ($player['inventory'] as $item => $quantity):
                $resourceName = '';
                if ($item === 'wood') {
                    $resourceName = 'Wood';
                } elseif ($item === 'stone') {
                    $resourceName = 'Stone';
                } elseif ($item === 'brick') {
                    $resourceName = 'Brick';
                } else {
                    $resourceName = 'Unknown Resource'; // Optional pentru resurse neidentificate
                }

                echo "<li>" . htmlspecialchars($resourceName) . ": " . htmlspecialchars($quantity) . "</li>";
            endforeach;
            echo "</ul>";
        else:
            echo "Fără inventar";
        endif;
    ?>
</td>

                </tr>
            <?php endforeach; ?>
        </table>
    </div>
</body>
</html>
