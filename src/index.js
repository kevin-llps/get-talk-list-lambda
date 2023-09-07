import { SecretsManagerClient, GetSecretValueCommand } from "@aws-sdk/client-secrets-manager";
import { connection, query } from "./db.js";
import { captureAWSv3Client, getSegment, beginSubsegment, endSubsegment } from "./trace.js";

const selectTalks = "SELECT t.date, t.title as 'titre', \
s.username as 'speaker', t.description \
FROM talk t JOIN speaker s \
ON t.speaker_id = s.id \
ORDER BY t.date,t.title";

const secretManagerClient = captureAWSv3Client(new SecretsManagerClient({ region: process.env.REGION }));

export const handler = (event, context, callback) => {
    const segment = getSegment();

    const handlerSubsegment = beginSubsegment(segment, 'Handler');

    console.log("event = ", event);
    console.log("context = ", context);

    context.callbackWaitsForEmptyEventLoop = false;

    const getSecretValueCommand = new GetSecretValueCommand({ SecretId: process.env.SECRET_NAME });

    const getTalksSubsegment = beginSubsegment(segment, 'Get Talks');

    secretManagerClient.send(getSecretValueCommand).then(secretResponse => {
        const dbSecret = JSON.parse(secretResponse.SecretString);

        connection(dbSecret);
        query(selectTalks, (err, results) => {
            endSubsegment(getTalksSubsegment);
            endSubsegment(handlerSubsegment);

            if (err) {
                return callback(err);
            }

            callback(null, results);
        });
    }, err => {
        console.log(err);
        endSubsegment(getTalksSubsegment);
        endSubsegment(handlerSubsegment);

        return callback(err);
    });
};