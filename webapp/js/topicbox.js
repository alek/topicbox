var WEBSOCKET_ENDPOINT = "ws://localhost:1981/ws/";
var TOPIC_RENAME_POST_URI  = "http://localhost:1981/renameTopic";

//
// submit data source for lda model estimation
//
function submitLDATask(task) {
	clearCanvas();
	try {
		var socket = new WebSocket(WEBSOCKET_ENDPOINT);
		socket.onopen = function() {
			socket.send("SUBMIT_LDA_TASK:" + task);
			message("submitted task : <h1>" + task + "</h1>")
		}
		socket.onmessage = function(msg) {
			if (msg.data.indexOf("TASK_NAME") == 0) {
				$("#selected_dataset").empty();
				$("#selected_dataset").append(msg.data.substring("TASK_NAME".length));
			} else {
				message(msg.data);
			}
		}
		socket.onclose = function() {
			message("[+] use <a href=\"#\" onClick=onclick=loadTopics()>Topic View</a> to start browsing");
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
		var socket = new WebSocket(WEBSOCKET_ENDPOINT);
		var selected_dataset = $("#selected_dataset").text();
		socket.onopen = function() {
			socket.send("LOAD_TOPICS:" + selected_dataset);
		}
		socket.onmessage = function(msg) {
			clearCanvas();
			if (msg.data == "MODEL_NOT_AVAILABLE") {
				renderNoDataAvailable();
				return;
			}
			var result = $.parseJSON(msg.data);
			for (var i=0; i<result.length; i++) {
				var renderContent = "<h3 class=\"data-source-description topic" + i + "\" id=\"topic_title_" + i + "\">topic." + i + "</h3><div id=\"topic" + i + "\" class=\"isotope\">";

				for (var j=0; j<result[i].length; j++) {
					var name = result[i][j][0];
					var weight = result[i][j][1];
					renderContent += "<div><a href=\"#\" class=\"item group" + getGroup(weight, j) + "\">" + name + "</a></div>";
				}
				
				renderContent += "</div>";
				renderContent += "<hr>";
				
				$("#container").append(renderContent);
				
				$('.topic' + i).editable(TOPIC_RENAME_POST_URI + "/" + selected_dataset, {
					indicator : 'updating...'
				});
				
				$("#topic" + i).isotope({
					masonry: {
						columnWidth: 70
					}
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
		var socket = new WebSocket(WEBSOCKET_ENDPOINT);
		socket.onopen = function() {
			socket.send("LOAD_DATA:" + $("#selected_dataset").text() + ";NUM_ENTRIES:500");
		}
		socket.onmessage = function(msg) {
			clearCanvas();
			if (msg.data == "MODEL_NOT_AVAILABLE") {
				renderNoDataAvailable();
				return;
			}
			var result = $.parseJSON(msg.data);

			for (var i=0; i<result.length; i++) {
				var renderContent = "<h3 class=\"data-source-description\">topic." + i + "</h3><div id=\"topic" + i + "\">";
				
				for (var j=0; j<result[i].length; j++) {
					renderContent += "<div><a href=\"#\" class=\"delem group" + j + "\">" + getDataTextEntry(result[i][j]) + "</a></div>";
				}
				
				renderContent += "</div>";
				renderContent += "<hr>";

				$("#container").append(renderContent);
				
				$("#topic" + i).isotope({
					masonry: {
						columnWidth: 70
					}
				});
				
				
			}
			
		}
	} catch (exception) {
		message("error loading topics ...")
	}
}

function renderNoDataAvailable() {
	message("[+] no data available. use <a href=\"#\" onClick=window.location.reload()>configure</a> tab to select data source");
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

// return (trimmed) data panel entry
function getDataTextEntry(content) {
	return (content < 64) ? content : content.substr(0, 64) + " ...";
}

// get topic group corresponding to given weight
function getGroup(weight, j) {
	var threshold = [1000, 300, 250, 200, 150, 100, 75];
	var category = 0;
	while (weight < threshold[category]) {
		category++;
		if (category == threshold.length) {
			break;
		}
	}
	return category;
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