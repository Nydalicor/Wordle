const height = 6;
const width = 5;
var row = 0;
var col = 0;

var row_to_valid = 0;
var current_col = 0;
var validated_rows = [];

window.onload = function () {
    initialize();
}

//grid of the game
function initialize() {
    for (let r = 0; r < height; r++) {
        for (let c = 0; c < width; c++) {
            let tile = document.createElement("span");
            tile.id = r.toString() + "-" + c.toString();
            tile.classList.add("cell");
            tile.innerText = "";
            document.getElementById("board").appendChild(tile);
        }
    }
}

document.addEventListener('DOMContentLoaded', function () {
    const buttons = document.querySelectorAll('.btn');
    const deleteBtn = document.querySelector('.delete');
    const enterBtn = document.querySelector('.enter');

    buttons.forEach(btn => {
        btn.addEventListener('click', () => {
            const letter = btn.innerText;
            //alert("You clicked: " + letter);
            parseInput(btn);
        });
    });

    deleteBtn.addEventListener('click', () => {
        //il me reste de traiter le cas ou on essaie de supprimer une lettre dans une ligne qui a ete validée
        parseInput(null, true, false);
    });

    enterBtn.addEventListener('click', () => {
        parseInput(null, false, true)

    });
});
/*
    Request format to : 
    - Go to / or /index.html to see the game and get redirected to /play.html => http://localhost:8021/ or http://localhost:8021/index.html
    - Go to /play.html to see the game => http://localhost:8021/play.html
    - Try a guess => http://localhost:8021/play.html?guess=myGuess
    - POST METHOD ISN'T WORKING ACTUALLY
*/
async function askServer() {
    const word = getWord();
    // Send the word to the server for verification through GET method
    // You can use AJAX or fetch to send the request
    // Example using fetch:
    // alert(word);
    const sessionCookie = document.cookie.split(';').filter(item => item.includes('SESSIONID'))[0].split('=')[1];
    const requestOptions = {
        method: 'GET',
        headers: {
            'Cookie': 'SESSIONID=' + sessionCookie
        }
    };
    await fetch('/play.html?guess=' + word, requestOptions)
        .then(response => { return response.text() })
        .then(data => {
            // the data is :{"result":"YBBBG","attempts":[AZERT]}

            let result = data.substring(11, 16);
            let attempts = data.substring(data.indexOf('[') + 1, data.indexOf(']'));
            console.log(result);
            console.log(attempts);
            // Handle the response from the server
            // The response is a JSON object containing the result of the verification
            // The result is a string of 5 letters (G, Y, B)
            // G: correct letter
            // Y: existant letter but not in the correct position
            // B: non existant letter
            // Example: GYBGG
            ColoringCells(result, row_to_valid);
            // alert(data.result);
            UpdateText(result);
        })
        .catch(error => {
            console.error('Error occurred while verifying the word:', error);
        });
}
function UpdateText(result) {
    let textElement = document.getElementById('text');
    let newText;
    // result = result.substring(0, 5);
    if (result.substring(0, 5) == "GGGGG") {
        newText = "Congrats !!";
    } else if (result == "BBBBB") {
        newText = "Oups , try again";
    } else {
        newText = "Oups , try again";
    }
    if (row == height || col == width) {
        newText = "Game Over !"
    }
    if (result.substring(0, 6) == "invalid") {
        newText = result
    }
    textElement.innerHTML = newText;
}
function ColoringCells(result, vrow) {
    for (let i = 0; i < result.length; i++) {
        let letter = result[i];
        if (letter == 'G') {
            let color_Cell = document.getElementById(vrow.toString() + '-' + i.toString());
            if (color_Cell) {
                color_Cell.classList.add('correct-letter');
            }
        } else if (letter == 'Y') {
            let color_Cell = document.getElementById(vrow.toString() + '-' + i.toString());
            if (color_Cell) {
                color_Cell.classList.add('existant-letter');
            }
        } else if (letter == 'B') {
            let color_Cell = document.getElementById(vrow.toString() + '-' + i.toString());
            if (color_Cell) {
                color_Cell.classList.add('non-existant-letter');
            }
        }
    }
}

function getWord() {
    let word = '';
    for (let c = 0; c < width; c++) {
        const currCell = document.getElementById(row_to_valid.toString() + '-' + c.toString());
        word += currCell.innerText;
    }
    return word;
}

function parseInput(btn, isDelete = false, isEnter = false) {
    if (isDelete) {
        if (!validated_rows.includes(row_to_valid)) {
            if (0 < current_col && current_col <= width) {
                current_col -= 1;
                col = current_col;
                row = row_to_valid;
            }
            let currCell = document.getElementById(row_to_valid.toString() + '-' + current_col.toString());
            currCell.innerText = "";
        }


    } else if (isEnter) {
        validated_rows.push(row_to_valid);
        askServer();
    } else {
        if (col < width) {
            let currCell = document.getElementById(row.toString() + '-' + col.toString());
            if (currCell.innerText == "") {
                currCell.innerText = btn.innerText;
                col += 1;

                current_col = col;
                row_to_valid = row;

                if (col === width) {
                    col = 0;
                    row += 1;

                    if (row === height) {
                        row = 0;
                    }
                }
            }
        }

    }

}
