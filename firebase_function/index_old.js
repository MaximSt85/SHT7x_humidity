// The Cloud Functions for Firebase SDK to create Cloud Functions and setup triggers.
const functions = require('firebase-functions');

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });


// The Firebase Admin SDK to access the Firebase Realtime Database. 
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);


// Take the text parameter passed to this HTTP endpoint and insert it into the
// Realtime Database under the path /messages/:pushId/original
exports.addMessage = functions.https.onRequest((req, res) => {
  // Grab the text parameter.
  const original = req.query.text;
  // Push the new message into the Realtime Database using the Firebase Admin SDK.
  admin.database().ref('/messages').push({original: original}).then(snapshot => {
    // Redirect with 303 SEE OTHER to the URL of the pushed object in the Firebase console.
    res.redirect(303, snapshot.ref);
  });
});



// Listens for new messages added to /messages/:pushId/original and creates an
// uppercase version of the message to /messages/:pushId/uppercase
exports.makeUppercase = functions.database.ref('/messages/{pushId}/original')
    .onWrite(event => {
      // Grab the current value of what was written to the Realtime Database.
      const original = event.data.val();
      console.log('Uppercasing', event.params.pushId, original);
      const uppercase = original.toUpperCase();
      // You must return a Promise when performing asynchronous tasks inside a Functions such as
      // writing to the Firebase Realtime Database.
      // Setting an "uppercase" sibling in the Realtime Database returns a Promise.
      return event.data.ref.parent.child('uppercase').set(uppercase);
    });


exports.sendMessageNotification = functions.database.ref('/chats/{chatRef}/{id}')
	.onWrite(event => {
		const receiverId = event.data.child('receiverId').val();
		const followedUid = event.data.child('senderId').val();
		//const receiverId = event.params.receiverId;
  		//const senderId = event.params.senderId;
		console.log('We have a new receiver id:', receiverId);
		const getReceiverTokensPromise = admin.database().ref(`/users/${receiverId}/notificationTokens`).once('value');
		return Promise.all([getReceiverTokensPromise, ]).then(results => {
			const tokensSnapshot = results[0];
			console.log('Token is:', tokensSnapshot.val());
			const payload = {
      			notification: {
        		title: 'You have a new message!',
        		body: 'Message'
      			}
    		};
			const tokens = Object.keys(tokensSnapshot.val());
			return admin.messaging().sendToDevice(tokens, payload).then(response => {
				const tokensToRemove = [];
      			response.results.forEach((result, index) => {
        			const error = result.error;
        			if (error) {
          				console.error('Failure sending notification to', tokens[index], error);
          				// Cleanup the tokens who are not registered anymore.
          				if (error.code === 'messaging/invalid-registration-token' ||
              				error.code === 'messaging/registration-token-not-registered') {
            				tokensToRemove.push(tokensSnapshot.ref.child(tokens[index]).remove());
          				}
        			}
      			});
      			return Promise.all(tokensToRemove);
			});
		});
	});

exports.sendNotification1 = functions.database.ref('/chats/{chatRef}/{id}').onWrite(event => {
	const receiverId = event.data.child('receiverId').val();
	const senderId = event.data.child('senderId').val();
	const senderName = event.data.child('messageUser').val();
	const messageText = event.data.child('messageText').val();
	//console.log('We have a new receiver id:', receiverId);
	return admin.database().ref(`/users/${receiverId}`).once('value').then(data => {
		const getReceiverToken = data.val().notificationTokens;
		//console.log('Token for receiver is :', getReceiverToken);
		return admin.database().ref(`/users/${senderId}`).once('value').then(data => {
			const age = data.val().userAge;
			const sex = data.val().userSex;
			var newMessage = data.val().newMessage;
			newMessage +=1;
			admin.database().ref(`/users/${senderId}/newMessage`).set(newMessage);
			return admin.database().ref(`/muteNotifications/${receiverId}/${senderId}`).once('value').then(data => {
				var payload;
				const muteUser = data.val();
				return admin.database().ref(`/mute/${receiverId}`).once('value').then(data => {
					//console.log('muteUser is :', muteUser);
					const muteAll = data.val();
					//console.log('muteAll is :', muteAll);
					//console.log('id is :', receiverId);
					var isMute = false;
					if (muteAll === true ) {
						isMute = true;
	  				}
					else {
						isMute = false;
						//if (muteUser === true ) {isMute = true;}
						//else {isMute = false;}
					}
					//console.log('isMute is: ', isMute);
					if (isMute === true) {
						//console.log('ispayload is without sound');
						payload = {
							notification: {
								title: `New message from ${senderName}`,
								body: `${messageText}`
							},
							data: {
								"USER_ID1": `${receiverId}`,
								"USER_ID2": `${senderId}`,
								"USER_NAME2": `${senderName}`,
								"USER_AGE2": `${age}`,
								"USER_SEX2": `${sex}`
							}
						};
					}
					else {
						payload = {
							notification: {
								title: `New message from ${senderName}`,
								body: `${messageText}`,
								sound: 'default'
							},
							data: {
								"USER_ID1": `${receiverId}`,
								"USER_ID2": `${senderId}`,
								"USER_NAME2": `${senderName}`,
								"USER_AGE2": `${age}`,
								"USER_SEX2": `${sex}`
							}
						};
					}
					return admin.messaging().sendToDevice(getReceiverToken, payload);
				});
			});
		});
	});
});


/**/



