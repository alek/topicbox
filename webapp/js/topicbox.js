

// topicbox service configuration | TODO : change the way this is retrieved
var WEBSOCKET_ENDPOINT = "ws://localhost:1981/ws/";
var TOPIC_RENAME_POST_URI  = "http://localhost:1981/renameTopic";

// get topicbox websocket 
function getWebsocket() {
	return new WebSocket(WEBSOCKET_ENDPOINT);
}

// send json data to given websocket
function sendRequest(socket, data) {
	socket.send(JSON.stringify(data));
}

//
// submit data source for lda model estimation
//
function submitLDATask(task, numTopics) {
	var taskData = "";
	if (task == "custom") {
		taskData = $("#custom_task_uri")[0].value;
	}
	
	// num topics not passed - try to pick it up from select
	if (!numTopics) {
	    numTopics = $("#number_of_topics")[0].value;
	}

	clearCanvas();
	try {
		var socket = getWebsocket();
		socket.onopen = function() {
			sendRequest(socket, {
				request : "SUBMIT_LDA_TASK",
				taskName : task,
				taskData : taskData,
				numTopics : numTopics
			});
			message("submitted task : <h1>" + task + "</h1><br><h3 class=\"status_update_box\"><div id=\"task_gen_updates\"></div></h3>")
		}
		socket.onmessage = function(msg) {
			if (msg.data.indexOf("TASK_NAME") == 0) {
				setSelectedDataset(msg.data.substring("TASK_NAME".length));
			} else {
				$("#task_gen_updates").empty();
				$("#task_gen_updates").append(msg.data);
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
function loadTopics(dataset, numTopics, dataSource) {
	clearCanvas();
	message("<h5>loading topics ...</h5>");

	if (dataset) {
	    setSelectedDataset(dataset + "/" + numTopics);
	}

	if (dataSource) {
	    setDataSource(dataSource);
	}

	try {
		var socket = getWebsocket();
		socket.onopen = function() {
			sendRequest(socket, {
				request : "LOAD_TOPICS",
				dataset : dataset ? dataset : getSelectedDataset(),
				numTopics : numTopics ? numTopics : getNumberOfTopics(),
				dataSource : dataSource ? dataSource : getDataSource()
			});
		}
		socket.onmessage = function(msg) {
			clearCanvas();
			if (msg.data == "MODEL_NOT_AVAILABLE") {
				renderNoDataAvailable();
				return;
			}
			var result = $.parseJSON(msg.data);
								
			$("#container").append(getTopicMatrixViewNav());
			
			for (var i=0; i<result.length; i++) {
				
				var renderContent = "<h3 class=\"data-source-description topic" 
										+ i + "\" id=\"topic_title_" + i + "\">topic." 
										+ i + "</h3><div id=\"topic" + i + "\" class=\"isotope\">";

				for (var j=0; j<result[i].length; j++) {
					
					var name = result[i][j][0];
					var weight = result[i][j][1];
					
					renderContent += "<div><a href=\"#\"  rel=\"tooltip\" title=\"topic weight : " + weight + "\" class=\"item group" 
											+ getGroup(weight, j) 
											+ "\" onclick=loadKeywordDescription(\"" + name + "\")>" 
											+ name + "</a></div>";
				}
				
				renderContent += "</div>";
				renderContent += "<hr>";
				
				$("#container").append(renderContent);
				
				$('.topic' + i).editable(TOPIC_RENAME_POST_URI + "/" + getSelectedDataset() + "/" + getNumberOfTopics(), {
					indicator : 'updating...'
				});
				
				$("#topic" + i).isotope({
					masonry: {
						columnWidth: 70
					}
				});

				$('#topic' + i).tooltip('show');

			}
			
		}
	} catch (exception) {
		message("error loading topics...");
	}
}

//
// load keyword/topic allocation matrix & render d3 structure
//
function loadTopicMatrix() {
	clearCanvas();
	$("#container").append(getTopicMatrixViewNav());
	try {
		var socket = getWebsocket();
		socket.onopen = function() {
			sendRequest(socket, {
				request : "GET_KEYWORD_COOCCURRENCE",
				dataset : getSelectedDataset(),
				numTopics : getNumberOfTopics(),
				dataSource : getDataSource(),
				maxKeywordsPerTopic : 4
			});
		}
		socket.onmessage = function(msg) {
			var result = $.parseJSON(msg.data);
			renderTopicMatrix("#container", result);
		}
	} catch (exception) {
		message("error loading keyword/topic matrix");
	}
	
}

//
// load data samples for topics in given model
//
function loadData(dataset, numTopics, dataSource) {

	clearCanvas();
	
	if (dataset) {
	    setSelectedDataset(dataset + "/" + numTopics);
	}

	if (dataSource) {
	    setDataSource(dataSource);
	}

	message("loading data ...");
	try {
		var socket = getWebsocket();
		socket.onopen = function() {
			sendRequest(socket, {
				request : "LOAD_DATA",
				dataset : dataset ? dataset : getSelectedDataset(),
				numTopics : numTopics ? numTopics : getNumberOfTopics(),
				dataSource : dataSource ? dataSource : getDataSource(),
				numEntries : 500
			});
		}
		socket.onmessage = function(msg) {
			clearCanvas();
			if (msg.data == "MODEL_NOT_AVAILABLE") {
				renderNoDataAvailable();
				return;
			} else if (msg.data == "MODEL_INFERENCER_NOT_READY") {
			    renderInferenceNotReady();
			    return;
			}
			
			var result = $.parseJSON(msg.data);

			for (var i=0; i<result.length; i++) {
				var renderContent = "<h3 class=\"data-source-description\">topic." 
									+ i + "</h3><div id=\"topic" + i + "\">";
				
				for (var j=0; j<result[i].length; j++) {
					renderContent += "<div><a href=\"#\" class=\"delem group"
					 				+ j + "\">" + getDataTextEntry(result[i][j]) + "</a></div>";
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

//
// list currently available models
//
function listModels() {
    clearCanvas();
    message("<h3 class=\"status_update_box\">estimated models</h3>");
    try {
	var socket = getWebsocket();
	socket.onopen = function() {
	    sendRequest(socket, {
		    request : "LIST_MODELS"
		});
	}
	socket.onmessage = function(msg) {
	    var result = $.parseJSON(msg.data);
	    for (var id in result) {
		message("<h2 class=\"data-source\"><a href=\"#\" " 
			+ "onclick=loadTopics(\"" + result[id]["taskName"] +"\"," + result[id]["numTopics"]  + ",\"" + result[id]["dataSource"] + "\")>" 
			+ result[id]["taskName"] + "</a></h2>" 
			+ "num topics : <b>" + result[id]["numTopics"] + "</b> | "  
			+ "data source : <b>" + result[id]["dataSource"] + "</b> | "
			+ "estimation started : " + result[id]["estimationStarted"] + " | " 
			+ "estimation complete : " + result[id]["modelComplete"] 
			);
	    }
	}
    } catch (exception) {
	message("error retrieving model list");
    }
}

// 
// load model description on given keyword
//
function loadKeywordDescription(keywordName) {
	clearCanvas();
	message("<h2>" + keywordName + "</h2>");
	try {
		var socket = getWebsocket();
		socket.onopen = function() {
			sendRequest(socket, {
				request : "DESCRIBE_KEYWORD",
				keyword : keywordName,
				dataset : getSelectedDataset(),
				numTopics : getNumberOfTopics(),
				dataSource : getDataSource()
			});
		}
		socket.onmessage = function(msg) {
			var result = $.parseJSON(msg.data);
			topicEntries = result["topicEntries"];
			for (entry in topicEntries) {
				var entryData = topicEntries[entry];
				message("topic : " + entryData[0] + " | weight : " + entryData[1]["weight"]);
			}
		}
	} catch (exception) {
		message("error retrieving description for keyword : " + keywordName);
	}
}

function renderNoDataAvailable() {
	message("[+] no data available. use <a href=\"#\" onClick=window.location.reload()>configure</a> tab to select data source");
}

function renderInferenceNotReady() {
    message("[+] model inferencer not ready, wait for model estimation to complete");
}

function getTopicMatrixViewNav() {
	return "<div align=\"right\"><a href=\"#\" class=\"view_select\" onClick=onclick=loadTopics()>keywords</a>" + 
    		" | <a href=\"#\" class=\"view_select\" onClick=onclick=loadTopicMatrix()>matrix view</a></div>";
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

// set active dataset
function setSelectedDataset(dataset) {
	$("#selected_dataset").empty();
	$("#selected_dataset").append(dataset);
}

function setDataSource(dataSource) {
    $("#data_source").empty();
    $("#data_source").append(dataSource);
}

// get active dataset
function getSelectedDataset() {
	    return $("#selected_dataset").text().split("/")[0];
}

function getNumberOfTopics() {
    var numTopics = $("#selected_dataset").text().split("/")[1];
    return numTopics ? numTopics : 0;
}

function getDataSource() {
    var dataSource = $("#data_source").text();
    return dataSource ? dataSource : "";
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