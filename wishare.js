const admin = require("firebase-admin");
const functions = require('firebase-functions');

const express = require('express');
const serviceAccount = require("serviceAccountKey.json");
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
let tokens = {};
let listening = false; // initialized to false so the script can initialize devices before starting...
const PORT = process.argv[2];

const options = {
  priority: "normal",
  timeToLive: 60 * 60
};

// this is a database trigger. If any document is modified, this trigger is invoked so 'devices' can be updated.
exports.modifyUser = functions.firestore
    .document('devices/{phone}')
    .onWrite((change, context) => {
      log("A DOCUMENT IN FIRESTORE HAS CHANGED!");
      // Get an object with the current document value.
      // If the document does not exist, it has been deleted.
      const newDocument = change.after.exists ? change.after.data() : null;

      log("New Document: " + newDocument !== null ? JSON.stringify(newDocument) : "null");

      // Get an object with the previous document value (for update or delete)
      const oldDocument = change.before.data();
      log("Old Document: " + oldDocument !== null ? JSON.stringify(oldDocument) : "null");

      // since a document was just modified, we need to update the document in the global variable 'devices'
      if(newDocument != null){
        log("Replacing the document in devices for " + newDocument.id);
        devices[newDocument.id] = newDocument;
      }
      else{
        delete devices[oldDocument.id];
      }
    });

function handleIncomingRequest(request, response, next){
  log("New request! Request Body: " + JSON.stringify(request.body));

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
  log("Authenticating " + phone);
  let doc = devices[phone];
  let authenticated = (doc !== undefined && doc.phone === phone && doc.token === token);

  log("Authenticated? " + authenticated);

  // invoke the callback if there is one.
  if(next !== undefined && authenticated){
    next();
  }

  return authenticated;
}

// update the global variable 'devices'
function refreshDevices(){
  db.collection('devices').get()
      .then((result) => {
        log("Refreshing device list...");
        log("Loaded " + result.size + " documents");

        result.forEach((doc) => {
          devices[doc.id] = doc.data();
        });

        log("Finished loading " + result.size + " documents");

        // on first run we need to initialize the device list before starting the server...
        if(!listening){
          listening = true;
          startApp();
        }
  }).catch((err) => {
    log(err);
  });
}

function fowardDataMessage(req, res){
  var db = admin.firestore();
  var token = req.query.token;
  var to = req.body.to;

  console.log("using " + token);
  console.log("req.body " + JSON.stringify(req.body, null, " "));
  console.log("req.body.to " + JSON.stringify(req.body.to));  
  
  db.collection('devices').where('token', '==', token).get()
  .then((snapshot) => {
    console.log(snapshot.size);

    // authenticated...
    if(snapshot.size === 1){
      console.log("sending message...");
      // res.send({authenticated:true});

      db.collection('devices').get()
        .then((snapshot) => {
          let tokens = [];
        console.log("Phones: " + JSON.stringify(req.body.to));
        snapshot.forEach((doc) => {
          console.log("phone " + doc.data().phone);

          if(to.includes(doc.data().phone)){
            tokens.push(doc.data().token);
          }

        });
          console.log("token: " + JSON.stringify(tokens));
          // res.send({phones: records});
           req.body.msg_type = req.body.msg_type.toString(); 
           delete req.body["to"];
           console.log("req.body: " + JSON.stringify(req.body, null, " "));
	
          let msg = {data: req.body};
          console.log("Fowarding msg -> " + JSON.stringify(msg, null, " "));

          admin.messaging().sendToDevice(tokens[0], {data: req.body}, options)
            .then(function(response) {
              res.send({success : true});
              console.log("Successfully sent message:", response);
            })
            .catch(function(error) {
              res.send({success : false});
              console.log("Error sending message:", error);
              return;
            });
          
          })
        .catch((err) => {
          res.send({success : false});
        console.log('Error getting documents', err);
      }); 
    }
    else{
      res.send({authenticated:false});
      // res.send("token " + token + " not found...");
    }
    console.log(snapshot.size);
  })
  .catch((err) => {
    console.log('Error getting documents', err);
  });
}

refreshDevices();

function startApp(){
  app.post('/', handleIncomingRequest, authenticate, synchronizeContacts);
  app.post('/msg', handleIncomingRequest, authenticate, fowardDataMessage);

  app.listen(PORT);
  log("Starting server...");
  log("Listening on port " + PORT);
}
