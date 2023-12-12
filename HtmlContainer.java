
/**
 * The HtmlContainer class represents an HTML container for the Wordle game.
 * It provides the default HTML template and contains JavaScript functions for interacting with the game.
 * It also provides a method to update the guess section of the HTML page.
 */
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public class HtmlContainer {
    private static final String DEFAULT_HTML = "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "\n<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
            "    <img src=\"\">\n" +
            "    <title>Wordle</title>\n" +
            "    <style>\n" +
            "        body {\n" +
            "            font-family: Arial, Helvetica, sans-serif;\n" +
            "            text-align: center;\n" +
            "        }\n" +
            "        .container{\n" +
            "            display: none;\n" +
            "        }\n" +
            "        #board {\n" +
            "            width: 349px;\n" +
            "            height: 395px;\n" +
            "            margin: 0 auto;\n" +
            "            margin-top: 10px;\n" +
            "            margin-bottom: 18px;\n" +
            "            display: flex;\n" +
            "            flex-wrap: wrap;\n" +
            "        }\n" +
            "\n        #text {\n" +
            "            font-family: cursive;\n" +
            "            font-size: 21px;\n" +
            "            font-weight: 200;\n" +
            "            text-align: center;\n" +
            "        }\n" +
            "\n        .cell {\n" +
            "            border: 2px solid rgb(188, 188, 188);\n" +
            "            width: 60px;\n" +
            "            height: 60px;\n" +
            "            margin: 2.5px;\n" +
            "            color: rgb(37, 39, 39);\n" +
            "            font-size: 30px;\n" +
            "            font-weight: bold;\n" +
            "            display: flex;\n" +
            "            justify-content: center;\n" +
            "            align-items: center;\n" +
            "        }\n" +
            "\n        .correct-letter {\n" +
            "            background-color: rgb(66, 219, 66);\n" +
            "            color: rgb(250, 255, 255);\n" +
            "        }\n" +
            "\n        .existant-letter {\n" +
            "            background-color: rgb(250, 237, 62);\n" +
            "            color: rgb(250, 255, 255);\n" +
            "        }\n" +
            "\n        .non-existant-letter {\n" +
            "            background-color: rgb(51, 51, 49);\n" +
            "            color: rgb(250, 255, 255);\n" +
            "        }\n" +
            "\n        .keyboard {\n" +
            "            display: flex;\n" +
            "            flex-direction: column;\n" +
            "            align-items: center;\n" +
            "        }\n" +
            "\n        button {\n" +
            "            background-color: rgb(157, 157, 243);\n" +
            "            border: none;\n" +
            "            display: inline-block;\n" +
            "            padding: 9px 10px;\n" +
            "            margin: 0.2rem;\n" +
            "            font-size: 1rem;\n" +
            "            color: rgb(255, 255, 255);\n" +
            "            width: 50px;\n" +
            "            height: 50px;\n" +
            "            border-radius: 0.5rem;\n" +
            "            transition: background-color 0.3s;\n" +
            "        }\n" +
            "\n        button:hover {\n" +
            "            background-color: rgba(76, 76, 155, 0.9);\n" +
            "            cursor: pointer;\n" +
            "        }\n" +
            "\n        .delete,\n" +
            "        .enter {\n" +
            "            font-size: 1rem;\n" +
            "            text-transform: capitalize;\n" +
            "        }\n" +
            "\n        .delete {\n" +
            "            width: 80px;\n" +
            "        }\n" +
            "\n        .enter {\n" +
            "            width: 320px;\n" +
            "        }\n" +
            ".input-container {\n" +
            "    display: inline-table;\n" +
            "    width: 200px;\n" +
            "    margin: 0 auto;\n" +
            "    margin-top: 5rem;" +
            "    }\n" +
            "#guess {" +
            "    width: 211px;" +
            "    height: 44px;" +
            "    padding: 5px;" +
            "    border: 1px solid #ccc;" +
            "    margin-bottom: 10px;" +
            "    margin-top: 15px;" +
            "    border-radius: 5px;" +
            "}" +
            "input[type=\"submit\"] {" +
            "    background-color: rgb(157, 157, 243);" +
            "    border: 1px solid #fff;" +
            "    padding: 10px 20px;" +
            "    border-radius: 0.5rem;" +
            "    color: #fff;" +
            "    cursor: pointer;" +
            "    height: 50px;" +
            "    width: 224px;" +
            "}\n" +
            "#answer span{" +
            "font-size: 40px;" +
            "}\n" +
            ".js-enabled {" +
            "    display: block;" +
            "}\n" +
            "    </style>\n" +
            "    <script>\n" +
            "    const height = 6;\n" +
            "    const width = 5;\n" +
            "    var row = 0;\n" +
            "    var col = 0;\n" +
            "\n" +
            "    var row_to_valid = 0;\n" +
            "    var current_col=0;\n" +
            "    var validated_rows = [];\n" +
            "    var filled = false;\n" +
            "    \n" +
            "    document.addEventListener('DOMContentLoaded', function () {\n" +
            "        var containerElement = document.querySelector('.container');\n" +
            "        if (containerElement) {\n" +
            "        containerElement.classList.add('js-enabled');\n" +
            "       }\n" +

            "        const buttons = document.querySelectorAll('.btn');\n" +
            "        const deleteBtn = document.querySelector('.delete');\n" +
            "        const enterBtn = document.querySelector('.enter');\n" +
            "\n" +
            "        buttons.forEach(btn => {\n" +
            "            btn.addEventListener('click', () => {\n" +
            "                const letter = btn.innerText;\n" +
            "                //alert(\"You clicked: \" + letter);\n" +
            "                processInput(btn);\n" +
            "            });\n" +
            "        });\n" +
            "\n" +
            "        deleteBtn.addEventListener('click', () => {\n" +
            "            //il me reste de traiter le cas ou on essaie de supprimer une lettre dans une ligne qui a ete validée\n"
            +
            "            processInput(null, true, false);\n" +
            "        });\n" +
            "\n" +
            "        enterBtn.addEventListener('click', () => {\n" +
            "            processInput(null, false, true)\n" +
            "\n" +
            "        });\n" +
            "    });\n" +
            "    /*\n" +
            "        Request format to :\n" +
            "        - Go to / or /index.html to see the game and get redirected to /play.html => http://localhost:8021/ or http://localhost:8021/index.html\n"
            +
            "        - Go to /play.html to see the game => http://localhost:8021/play.html\n" +
            "        - Try a guess => http://localhost:8021/play.html?guess=myGuess\n" +
            "        - POST METHOD ISN'T WORKING ACTUALLY\n" +
            "    */\n" +
            "    async function askServer() {\n" +
            "        const word = getWord();\n" +
            "        // Send the word to the server for verification through GET method\n" +
            "        // You can use AJAX or fetch to send the request\n" +
            "        // Example using fetch:\n" +
            "        // alert(word);\n" +
            "        let requestOptions = {};\n" +
            "        if(document.cookie != \"\"){\n" +
            "           const sessionCookie = document.cookie.split(';').filter(item => item.includes('SESSIONID'))[0].split('=')[1];\n"
            +
            "            requestOptions = {\n" +
            "            method: 'GET',\n" +
            "            headers: {\n" +
            "            'Cookie': 'SESSIONID=' + sessionCookie\n" +
            "             }\n" +
            "         };\n}else{\n" +
            "             requestOptions = {method: 'GET'};\n}\n" +
            "         await fetch('/play.html?guess=' + word, requestOptions)\n" +
            "             .then(response => { return response.text() })\n" +
            "             .then(data => {\n" +
            "                let result = data.substring(11, 16);\n" +
            "                let attempts = data.substring(data.indexOf('[') + 1, data.indexOf(']'));\n" +
            "                 // Handle the response from the server\n" +
            "                // The response is a JSON object containing the result of the verification\n" +
            "                // The result is a string of 5 letters (G, Y, B)\n" +
            "                // G: correct letter Y: existant letter but not in the correct position  B: non existant letter  Example: GYBGG\n"
            +
            "                ColoringCells(result, row_to_valid);\n" +
            "                UpdateText(result);\n" +
            "             })\n" +
            "            .catch(error => {\n" +
            "                console.error('Error occurred while verifying the word:', error);\n" +
            "            });\n" +
            "    }\n" +
            "    function UpdateText(result){\n" +
            "        let textElement = document.getElementById('text');\n" +
            "        let newText;\n" +
            "        // result = result.substring(0, 5);\n" +
            "        if(result.substring(0, 5)==\"GGGGG\"){\n" +
            "            newText =\"Congrats , you found it !\";\n" +
            "        }else if(result==\"BBBBB\"){\n" +
            "            newText =\"Oups !\";\n" +
            "        }else{\n" +
            "            newText =\"Oups !\";\n" +
            "        }\n" +
            "        if(row == height){\n" +
            "            newText = newText+\" Gameover!\";\n" +
            "        }\n" +
            "        if(result.substring(0, 6)==\"invalid\"){\n" +
            "            newText=result\n" +
            "        }\n" +
            "        textElement.innerHTML = newText;\n" +
            "    }\n" +
            "    function ColoringCells(result, vrow) {\n" +
            "        for (let i = 0; i < result.length; i++) {\n" +
            "            let letter = result[i];\n" +
            "            if (letter == 'G') {\n" +
            "                let color_Cell = document.getElementById(vrow.toString() + '-' + i.toString());\n" +
            "                if (color_Cell) {\n" +
            "                    color_Cell.classList.add('correct-letter');\n" +
            "                }\n" +
            "            } else if (letter == 'Y') {\n" +
            "                let color_Cell = document.getElementById(vrow.toString() + '-' + i.toString());\n" +
            "                if (color_Cell) {\n" +
            "                    color_Cell.classList.add('existant-letter');\n" +
            "                }\n" +
            "            } else if (letter == 'B') {\n" +
            "                let color_Cell = document.getElementById(vrow.toString() + '-' + i.toString());\n" +
            "                if (color_Cell) {\n" +
            "                    color_Cell.classList.add('non-existant-letter');\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "\n" +
            "    function getWord() {\n" +
            "        let word ='';\n" +
            "        for (let c = 0; c < width; c++) {\n" +
            "            const currCell = document.getElementById(row_to_valid.toString() + '-' + c.toString());\n" +
            "            word += currCell.innerText;\n" +
            "        }\n" +
            "        return word;\n" +
            "    }\n" +
            "\n" +
            "    function processInput(btn, isDelete = false, isEnter = false) {\n" +
            "        if (isDelete) {\n" +
            "            if (!validated_rows.includes(row_to_valid)) {\n" +
            "                if (0 < current_col && current_col <= width) {\n" +
            "                        current_col -= 1;\n" +
            "                        col=current_col;\n" +
            "                        row=row_to_valid;\n" +
            "                }\n" +
            "                let currCell = document.getElementById(row_to_valid.toString() + '-' + current_col.toString());\n"
            +
            "                currCell.innerText = \"\";\n" +
            "            }    \n" +
            "        } else if (isEnter) {\n" +
            "            askServer();\n" +
            "            if(filled){validated_rows.push(row_to_valid);}\n" +
            "        } else {\n" +
            "            if(row==0||validated_rows.includes(row-1)){\n" +
            "                if (col < width) {\n" +
            "                    filled =false;\n" +
            "                    let currCell = document.getElementById(row.toString() + '-' + col.toString());\n" +
            "                    if (currCell.innerText == \"\") {\n" +
            "                        currCell.innerText = btn.innerText;\n" +
            "                        col += 1;\n" +
            "                        current_col = col;\n" +
            "                        row_to_valid = row;\n" +
            "\n" +
            "                        if (col === width) {\n" +
            "                            filled = true;\n" +
            "                            col = 0;\n" +
            "                            row += 1;\n" +
            "                        }\n" +
            "                    }\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "    </script>\n" +
            "</head>\n" +
            "\n<body>\n" +
            "    <br>\n" +
            "    <div id=\"text\">Welcome to Wordle!</div>\n" +
            "    <div class=\"container\">\n" +
            "        <div id=\"board\">\n" +
            "            <!-- Grid cells -->\n" +
            "            <span class=\"cell\" id=\"0-0\"></span>\n" +
            "            <span class=\"cell\" id=\"0-1\"></span>\n" +
            "            <span class=\"cell\" id=\"0-2\"></span>\n" +
            "            <span class=\"cell\" id=\"0-3\"></span>\n" +
            "            <span class=\"cell\" id=\"0-4\"></span>\n" +
            "\n" +
            "            <span class=\"cell\" id=\"1-0\"></span>\n" +
            "            <span class=\"cell\" id=\"1-1\"></span>\n" +
            "            <span class=\"cell\" id=\"1-2\"></span>\n" +
            "            <span class=\"cell\" id=\"1-3\"></span>\n" +
            "            <span class=\"cell\" id=\"1-4\"></span>\n" +
            "\n" +
            "            <span class=\"cell\" id=\"2-0\"></span>\n" +
            "            <span class=\"cell\" id=\"2-1\"></span>\n" +
            "            <span class=\"cell\" id=\"2-2\"></span>\n" +
            "            <span class=\"cell\" id=\"2-3\"></span>\n" +
            "            <span class=\"cell\" id=\"2-4\"></span>\n" +
            "\n" +
            "            <span class=\"cell\" id=\"3-0\"></span>\n" +
            "            <span class=\"cell\" id=\"3-1\"></span>\n" +
            "            <span class=\"cell\" id=\"3-2\"></span>\n" +
            "            <span class=\"cell\" id=\"3-3\"></span>\n" +
            "            <span class=\"cell\" id=\"3-4\"></span>\n" +
            "\n" +
            "            <span class=\"cell\" id=\"4-0\"></span>\n" +
            "            <span class=\"cell\" id=\"4-1\"></span>\n" +
            "            <span class=\"cell\" id=\"4-2\"></span>\n" +
            "            <span class=\"cell\" id=\"4-3\"></span>\n" +
            "            <span class=\"cell\" id=\"4-4\"></span>\n" +
            "\n" +
            "            <span class=\"cell\" id=\"5-0\"></span>\n" +
            "            <span class=\"cell\" id=\"5-1\"></span>\n" +
            "            <span class=\"cell\" id=\"5-2\"></span>\n" +
            "            <span class=\"cell\" id=\"5-3\"></span>\n" +
            "            <span class=\"cell\" id=\"5-4\"></span>\n" +
            "        </div>\n" +
            "        <div class=\"keyboard\">\n" +
            "            <div class=\"row\">\n" +
            "                <button class=\"btn\">A</button>\n" +
            "                <button class=\"btn\">Z</button>\n" +
            "                <button class=\"btn\">E</button>\n" +
            "                <button class=\"btn\">R</button>\n" +
            "                <button class=\"btn\">T</button>\n" +
            "                <button class=\"btn\">Y</button>\n" +
            "                <button class=\"btn\">U</button>\n" +
            "                <button class=\"btn\">I</button>\n" +
            "                <button class=\"btn\">O</button>\n" +
            "                <button class=\"btn\">P</button>\n" +
            "            </div>\n" +
            "            <div class=\"row\">\n" +
            "                <button class=\"btn\">Q</button>\n" +
            "                <button class=\"btn\">S</button>\n" +
            "                <button class=\"btn\">D</button>\n" +
            "                <button class=\"btn\">F</button>\n" +
            "                <button class=\"btn\">G</button>\n" +
            "                <button class=\"btn\">H</button>\n" +
            "                <button class=\"btn\">J</button>\n" +
            "                <button class=\"btn\">K</button>\n" +
            "                <button class=\"btn\">L</button>\n" +
            "                <button class=\"btn\">M</button>\n" +
            "            </div>\n" +
            "            <div class=\"row\">\n" +
            "                <button class=\"btn\">W</button>\n" +
            "                <button class=\"btn\">X</button>\n" +
            "                <button class=\"btn\">C</button>\n" +
            "                <button class=\"btn\">V</button>\n" +
            "                <button class=\"btn\">B</button>\n" +
            "                <button class=\"btn\">N</button>\n" +
            "                <button class=\"delete\">Delete</button>\n" +
            "            </div>\n" +
            "            <div class=\"row\">\n" +
            "                <button class=\"enter\" type=\"submit\" value=\"\">ENTER</button>\n" +
            "            </div>\n" +
            "        </div>\n" +
            "    </div>\n" +
            "    <noscript>\n" +
            "        <div class=\"row\">\n" +
            "            <form action=\"http://localhost:8021/play.html\" method=\"POST\">\n" +
            "               <div class=\"input-container\">\n" +
            "                <input type=\"text\" name=\"guess\" id=\"guess\">\n" +
            "                <input type=\"submit\" value=\"ENTER\">\n" +
            "               </div>\n" +
            "            </form>\n" +
            "        </div>\n" +
            "    </noscript>\n" +
            "    <br>\n" +
            "</body>\n" +
            "\n</html>";

    private String html;

    public HtmlContainer(String imagePath) {
        // Initialize with default HTML content
        html = DEFAULT_HTML;

        // Replace the placeholder with the base64 image
        String base64Image = convertImageToBase64(imagePath);
        html = html.replace("<img src=\"\">", "<img src=\"data:image/jpg;base64," + base64Image + "\">");
    }

    public String getHtml() {
        // Get the current HTML content
        return html;
    }

    public void updateGuessSection(String response, String guess) {
        StringBuilder coloredGuess = new StringBuilder();
        System.out.println("Response tamère: " + response);
        System.out.println("Guess : " + guess);
        for (int i = 0; i < response.length(); i++) {
            char letter = response.charAt(i);
            switch (letter) {
                case 'G':
                    coloredGuess.append("<span class=\"correct-letter\">").append(guess.charAt(i)).append("</span>");
                    break;
                case 'Y':
                    coloredGuess.append("<span class=\"existant-letter\">").append(guess.charAt(i)).append("</span>");
                    break;
                case 'B':
                    coloredGuess.append("<span class=\"non-existant-letter\">").append(guess.charAt(i))
                            .append("</span>");
                    break;
                default:
                    coloredGuess.append(guess.charAt(i));
            }
        }
        html = html.replace(
                "<input type=\"text\" name=\"guess\" id=\"guess\">", "<div id=\"answer\">\n" +
                        coloredGuess.toString() + "</div>" + "\n<input type=\"text\" name=\"guess\" id=\"guess\">\n");
        System.out.println("HTML: " + html);
    }

    private String convertImageToBase64(String imagePath) {
        try {
            byte[] imageBytes = Files.readAllBytes(Paths.get(imagePath));
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return ""; // Gestion d'erreur, vous pouvez traiter différemment
        }
    }

}