const functions = require('firebase-functions');

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });


const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

exports.sendNotification = functions.database.ref('/sht75/data/trigger').onUpdate(event => {
    return admin.database().ref('/sht75/data/lastHumidity').once('value').then(data => {
        const humidity1 = data.child('humidity1').val();
        const humidity2 = data.child('humidity2').val();
        const humidity3 = data.child('humidity3').val();
        const humidity4 = data.child('humidity4').val();
        const humidity5 = data.child('humidity5').val();
        //console.log('humidity1: ', humidity1);
        return admin.database().ref('/users').once('value').then(function(snapshot) {
            snapshot.forEach(function(child) {
                //console.log('child key is : ', child.key);
                //console.log('child value is : ', child.val().token);
                const mute = child.val().mute;
                const threshold = child.val().threshold;
                const token = child.val().token;
                const underAbove = child.val().underAbove;
                if (token) {
                    //console.log('token is : ', token);
                    var payload;
                    const messageText = " h1:" + humidity1 + "\r\n" +
                     " h2:" + humidity2 + "\r\n" +
                     " h3:" + humidity3 + "\r\n" +
                     " h4:" + humidity4 + "\r\n" +
                     " h5:" + humidity5 + "\r\n";
                    if (underAbove === "Under") {
                        if (humidity1 < threshold || humidity2 < threshold || humidity3 < threshold
                         || humidity4 < threshold || humidity5 < threshold) {
                            if (mute === true) {
                                payload = {
                                    notification: {
                                        title: `HUMIDIY OUT OF RANGE`,
                                        body: `${messageText}`
                                    }
                                };
                            }
                            else {
                                payload = {
                                    notification: {
                                        title: `HUMIDIY OUT OF RANGE`,
                                        body: `${messageText}`,
                                        sound: 'default'
                                    }
                                };
                            }
                            admin.messaging().sendToDevice(token, payload);
                        }
                    }
                    if (underAbove === "Above") {
                        if (humidity1 > threshold || humidity2 > threshold || humidity3 > threshold
                         || humidity4 > threshold || humidity5 > threshold) {
                            if (mute === true) {
                                payload = {
                                    notification: {
                                        title: `HUMIDIY OUT OF RANGE`,
                                        body: `${messageText}`
                                    }
                                };
                            }
                            else {
                                payload = {
                                    notification: {
                                        title: `HUMIDIY OUT OF RANGE`,
                                        body: `${messageText}`,
                                        sound: 'default'
                                    }
                                };
                            }
                            admin.messaging().sendToDevice(token, payload);
                        }
                    }
                }
            });
        });
    });
});
