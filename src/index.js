import { SecretsManagerClient, GetSecretValueCommand } from "@aws-sdk/client-secrets-manager";
import { connection, query } from "./db.js";

const selectTalks = "SELECT t.date, t.title as 'titre', \
s.username as 'speaker', t.description \
FROM talk t JOIN speaker s \
ON t.speaker_id = s.id \
ORDER BY t.date,t.title";

const secretManagerClient = new SecretsManagerClient({ region: process.env.REGION });

export const handler = (event, context, callback) => {

    console.log("event = ", event);
    console.log("context = ", context);

    context.callbackWaitsForEmptyEventLoop = false;

    const getSecretValueCommand = new GetSecretValueCommand({ SecretId: process.env.SECRET_NAME });

    secretManagerClient.send(getSecretValueCommand).then(secretResponse => {
        const dbSecret = secretResponse.SecretString;

        connection(dbSecret);
        query(selectTalks, (err, results) => {
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