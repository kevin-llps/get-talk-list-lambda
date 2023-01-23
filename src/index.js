const secretManager = require('@aws-sdk/client-secrets-manager');
const db = require('./db');

const selectTalks = "SELECT t.date, t.title as 'titre', \
s.username as 'speaker', t.description \
FROM talk t JOIN speaker s \
ON t.speaker_id = s.id \
ORDER BY t.date,t.title";

const secretManagerClient = new secretManager.SecretsManagerClient({ region: process.env.REGION });

exports.handler = (event, context, callback) => {

    console.log("event = ", event);
    console.log("context = ", context);

    context.callbackWaitsForEmptyEventLoop = false;

    const getSecretValueCommand = new secretManager.GetSecretValueCommand({ SecretId: process.env.SECRET_NAME });

    secretManagerClient.send(getSecretValueCommand).then(secretResponse => {
        const dbSecret = secretResponse.SecretString;

        db.connection(dbSecret);
        db.query(selectTalks, (err, results) => {
            if (err) {
                return callback(err);
            }
            
            callback(null, results);
        });
    }, err => {
        console.log(err);
        return callback(err);
    });
};