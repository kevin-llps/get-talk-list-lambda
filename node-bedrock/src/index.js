import { BedrockRuntimeClient, InvokeModelCommand } from "@aws-sdk/client-bedrock-runtime";
import { SecretsManagerClient, GetSecretValueCommand } from "@aws-sdk/client-secrets-manager";
import { connection, query } from "./db.js";
import { extract } from "./queryStringExtractor.js";
import { captureAWSv3Client, getSegment, beginSubsegment, endSubsegment } from "./trace.js";

const bedrockClient = new BedrockRuntimeClient({ region: process.env.REGION, requestTimeout: 5000 });
const secretManagerClient = captureAWSv3Client(new SecretsManagerClient({ region: process.env.REGION }));

export const handler = (event, context, callback) => {
    const segment = getSegment();

    const handlerSubsegment = beginSubsegment(segment, 'Handler');

    console.log("event = ", event);
    console.log("context = ", context);

    context.callbackWaitsForEmptyEventLoop = false;

    // event.prompt example => Get all talk records by selecting columns title as 'titre', description, date, username as 'speaker'

    const promptData = `\n\nHuman: There is a database with tables:
    - Table named talk containing talk records.
    - Table named speaker containing talk records.

    The table talk has following columns:\n
    - id\n
    - title\n
    - description\n
    - date (YYYY-MM-DD HH:mm:ss)\n
    - speaker_id (foreign key references column id from speaker table)\n

    The table speaker has following columns:\n
    - id\n
    - username\n

    \n\n

    Can you generate SQL query for the below:\n
    - ${event.prompt}\n\nAssistant:`;

    const body = JSON.stringify({
        "prompt": promptData,
        "max_tokens_to_sample":4096,
        "temperature":0.5,
        "top_k":250,
        "top_p":0.5,
        "stop_sequences": ["\n\nHuman:"]
      });

    const input = {
        body: body,
        contentType: "application/json",
        accept: "application/json",
        modelId: "anthropic.claude-v2",
    };

    const invokeModelCommand = new InvokeModelCommand(input);

    console.log("request to bedrock");

    bedrockClient.send(invokeModelCommand).then(response => {
        console.log("Response OK");
        console.log(response);

        const asciiDecoder = new TextDecoder('ascii');
        const body = JSON.parse(asciiDecoder.decode(response.body));

        console.log(body);

        const sqlQuery = extract(body.completion);

        console.log(sqlQuery);

        const getSecretValueCommand = new GetSecretValueCommand({ SecretId: process.env.SECRET_NAME });

        const getTalksSubsegment = beginSubsegment(segment, 'Get Talks');

        console.log("Call Secret Manager");

        secretManagerClient.send(getSecretValueCommand).then(secretResponse => {
            const dbSecret = JSON.parse(secretResponse.SecretString);

            console.log("Secret Manager Response OK");

            connection(dbSecret);
            query(sqlQuery, (err, results) => {
                endSubsegment(getTalksSubsegment);
                endSubsegment(handlerSubsegment);

                if (err) {
                    return callback(err);
                }

                callback(null, results);
            });
        }, err => {
            console.log("Secret Manager Response KO");

            console.log(err);
            endSubsegment(getTalksSubsegment);
            endSubsegment(handlerSubsegment);

            return callback(err);
        });

    }, err => {
        console.log("Response KO");
        console.log(err);
    });
};