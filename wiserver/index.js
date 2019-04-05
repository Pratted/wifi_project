const admin = require("firebase-admin");
const express = require('express');
const serviceAccount = require('serviceAccountKey.json');
const app = express();
app.use(express.json());

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
    databaseURL: "https://wishare-fafce.firebaseio.com"
});

function log(msg){
    const timestamp = new Date().toLocaleString();
    console.log(timestamp + ": " + msg);
}

const db = admin.firestore();

let devices = {}; // global cache for all documents in devices collection (stupid i know, but the DB will be small for time being)
const PORT = process.argv[2];

// print out the incoming request before actually handling it in next()
function handleIncomingRequest(request, response, next){
    console.log('\n\n');
    log("".padStart(20, "~") + "New request!" + "".padStart(20, "~") +
        "\n Request Body: " + JSON.stringify(request.body, null, " "));
    next();
}

function synchronizeContacts(request, response){
    let phones = request.body.phones;

    log("Begin Synchronizing contacts...");
    phones = phones.filter(phone => devices.hasOwnProperty(phone));

    log("Found " + phones.length + " contacts.");
    log(JSON.stringify(phones));

    let reply = {"phones": phones}; // an array of phone numbers (strings)
    log("Sending response: " + JSON.stringify(reply));
    response.send(reply);
}

// to authenticate a user we make sure the token exists and they provided the corresponding phone for that token
// next is a callback function. After authenticating the user, we invoke the callback.
function authenticate(request, response, next){
    let phone = request.body.sender;
    let token = request.query.token;

    log("Phone: " + phone);
    log("Token: " + token);

    let doc = devices[phone];
    let authenticated = (doc !== undefined && doc.phone === phone && doc.token === token);

    log("Authenticated? " + authenticated);

    // invoke the callback if there is one.
    if(next !== undefined && authenticated){
        next();
    }

    if(!authenticated){
        response.send({authenticated: false});
    }

    return authenticated;
}

// update the global variable 'devices'
function refreshDevices(request, response, callback){
    db.collection('devices').get()
        .then((result) => {
            log("Refreshing device list...");
            log("Loaded " + result.size + " documents");

            result.forEach((doc) => devices[doc.id] = doc.data());

            log("Finished loading " + result.size + " documents");

            if(callback !== undefined){
                callback();
            }
        }).catch((err) => {
        log(err);
    });
}

function fowardDataMessage(request, response){
    log("Begin forwarding data message");

    let recipientPhones = request.body.to;
    log("Recipient Phone Numbers: " + JSON.stringify(recipientPhones));

    let recipientTokens = Object.values(devices) // return all the documents (phone numbers)
        .filter(doc => (recipientPhones.indexOf(doc.phone) !== -1)) // get all documents whose phone is in the recipientPhones
        .map(doc => doc.token); // get the tokens from the documents above

    log("Recipient Tokens: " + JSON.stringify(recipientTokens));

    let body = request.body;
    delete body["to"]; // delete the 'to' field so client's can't see which other clients received the message

    body.msg_type = body.msg_type.toString(); // convert int to string to fit FCM protocol

    let msg = {data: body}; // make an FCM data message (by including a 'data' key)

    log("The outgoing message is:\n" + JSON.stringify(msg, null, " "));

    admin.messaging().sendToDevice(recipientTokens, msg, {priority: 'normal', timeToLive: 3600})
        .then(function(response2){
            console.log("Response: ", response2);
            response.send({success: true});
        }).catch(function(error){
        log(error);
        response.send({success: false, error: error.toString()});
    });
}

function listen(){
    app.post('/', handleIncomingRequest, authenticate, refreshDevices, synchronizeContacts);
    app.post('/msg', handleIncomingRequest, authenticate, fowardDataMessage);

    app.listen(PORT);
    log("Starting server...");
    log("Listening on port " + PORT);
}

function startServer(){
    refreshDevices(null, null, listen);
}

startServer();
