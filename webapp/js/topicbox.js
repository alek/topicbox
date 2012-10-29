//
// submit data source for lda model estimation
//
function submitLDATask(task) {
	clearCanvas();
	try {
		var socket = new WebSocket("ws://localhost:1981/ws/");
		socket.onopen = function() {
			socket.send("submitLDATask:" + task);
			message("submitted task : <h1>" + task + "</h1>")
		}
		socket.onmessage = function(msg) {
			message("received message : " + msg.data);
		}
	} catch (exception) {
		message("error submitting task to topicbox server");
	}
}

//
// load topics for a given model
//
function loadTopics() {
	clearCanvas();
	message("<h5>loading topics ...</h5>");
	try {
		var socket = new WebSocket("ws://localhost:1981/ws/");
		socket.onopen = function() {
			socket.send("loadTopics");
		}
		socket.onmessage = function(msg) {
			clearCanvas();
			if (msg.data == "MODEL_NOT_AVAILABLE") {
				message("no models available");
				return;
			}
			var result = $.parseJSON(msg.data);
			for (var i=0; i<result.length; i++) {

				var renderContent = "<h3 class=\"data-source-description\">topic." + i + "</h3><div id=\"topic" + i + "\">";

				for (var j=0; j<result[i].length; j++) {
					renderContent += "<div class=\"element group" + j + "\" data-symbol=\"Li\" data-category=\"alkali\">"
									+ "<h2 class=\"name\">" + result[i][j] + "</h2>"
									+ "<p class=\"weight\">6.941</p>"
									+ "</div>";
				}
				
				renderContent += "</div>";
				
				$("#container").append(renderContent);
				
				$("#topic." + i).isotope({
				        itemSelector: '.element'
				});
			}
			
		}
	} catch (exception) {
		message("error loading topics...");
	}
}

//
// load data samples for topics in given model
//
function loadData() {
	clearCanvas();
	message("loading data ...");
	try {
		var socket = new WebSocket("ws://localhost:1981/ws/");
		socket.onopen = function() {
			socket.send("loadData");
		}
		socket.onmessage = function(msg) {
			clearCanvas();
			if (msg.data == "MODEL_NOT_AVAILABLE") {
				message("no models available");
				return;
			}
			var result = $.parseJSON(msg.data);

			for (var i=0; i<result.length; i++) {

				var renderContent = "<h3 class=\"data-source-description\">topic." + i + "</h3><div id=\"topic" + i + "\">";
				
				for (var j=0; j<result[i].length; j++) {
					
					renderContent += "<div class=\"element group" + i 
								+ "\" data-symbol=\"Li\" data-category=\"alkali\" style=\"width:" + getBoxWidth(result[i][j]) +"px;height:" + getBoxHeight(result[i][j]) + "px\">"
								+ "<p class=\"name\">" + result[i][j] + "</p>"
								+ "</div>";
								
				}
				
				renderContent += "</div>";

				$("#container").append(renderContent);

				$("#topic." + i).isotope({
				        itemSelector: '.element'
				});
				
			}
			
		}
	} catch (exception) {
		message("error loading topics ...")
	}
}

// get entry box width
function getBoxWidth(content) {
	var width = 5*content.length;
	return (width < 600) ? width : 600;
}

// get entry box height
function getBoxHeight(content) {
	return (content.length < 100) ? 80 : 100;
}

// write message 
function message(msg) {
	$("#topics").append("<h4>").append(msg).append("</h4>");
}

// remove all elements
function clearCanvas() {
	$("#topics").empty();
	$("#container").empty();
}