const toggleImg = () => {
  let table  = document.getElementById("tableDiv")
  let imgDiv = document.getElementById("imgDiv")
  table.style.display = "none";
  imgDiv.style.display= "block";
}

const toggleResultsImg = () => {
  let table  = document.getElementById("tableDiv")
  let imgDiv = document.getElementById("imgDiv")
  table.style.display = "block";
  imgDiv.style.display= "none";
}

const toggleResults = () => {
  let table  = document.getElementById("tableDiv")
  let docDiv = document.getElementById("docDiv")
  table.style.display = "block";
  docDiv.style.display= "none";
}

const addRowEvent = () => {
  let rows = document.getElementsByTagName("tr");
  for (var i = 0; i < rows.length; i++){
        rows[i].onclick = function() {
           let id = this.cells[0].innerHTML;

           fetch('/document?docID='+id, {method: 'GET'})
             .then(response => {
                 return response.json();
             })
             .then(result => {
                 let table  = document.getElementById("tableDiv")
                 let docDiv = document.getElementById("docDiv")
                 let paragraph = document.getElementById("docContent")
                 table.style.display = "none";
                 docDiv.style.display= "block";
                 paragraph.innerHTML = result["content"];
             });
        };
    }
}

const showResults = (documents) => {

  let table = document.getElementById("searchResults")

  documents.forEach((doc,index) => {
    let row = table.insertRow(index+1);
    let noDoc = row.insertCell(0);
    let title = row.insertCell(1);
    let rank  = row.insertCell(2);
    noDoc.innerHTML = doc["id"];
    title.innerHTML = doc["title"];
    rank.innerHTML  = doc["rank"];

    title.className = "tCell";
    row.className = "bg-info";

  });

  addRowEvent();
}

const search = () => {
  let q = document.getElementById("searchBox").value;
  fetch('/searchResult?query='+q, {method: 'GET'})
    .then(res => {
            return res.json()
        }).then(result => {
            showResults(result);
        });
}

const loadDatalist = (queries) => {
  let datalistQueries = document.getElementById('queries')
  queries.forEach(query => {
        let option = document.createElement('option')
        option.text  = query["num"]
        option.value = query["q"]
        datalistQueries.appendChild(option)
    });
}

const loadQueries = () => {
  fetch('/queries', {method: 'GET'})
    .then(response => {
        return response.json();
    })
    .then(result => {
        loadDatalist(result);
    });
}

$(document).ready()
{
    loadQueries();
}
