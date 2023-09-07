import { handler } from "../index.js";
import * as db from "../db.js";
import * as trace from "../trace.js";
import { mockClient } from "aws-sdk-client-mock";
import { SecretsManagerClient, GetSecretValueCommand } from "@aws-sdk/client-secrets-manager";
import "aws-sdk-client-mock-jest";

const mockedSecretManagerClient = mockClient(SecretsManagerClient);

process.env.SECRET_NAME = "secretName";

const expectedSqlQuery = "SELECT t.date, t.title as 'titre', \
s.username as 'speaker', t.description \
FROM talk t JOIN speaker s \
ON t.speaker_id = s.id \
ORDER BY t.date,t.title";

const results = [{
    date: '2022-10-24',
    titre: 'AWS Lambda',
    speaker: 'kevin.llps',
    description: 'Présentation AWS Lambda'
}];

const secretJsonResponse = { SecretString: '{"host": "host", "username": "user", "password":"password", "port": "port", "dbname": "database", "engine": "mysql" }'};

const expectedSecretResponse = { SecretString: {
    host: "host",
    username: "user",
    password:"password",
    port: "port",
    dbname: "database",
    engine: "mysql"
}};

jest.spyOn(trace, 'captureAWSv3Client').mockImplementation(() => jest.fn());
jest.spyOn(trace, 'getSegment').mockImplementation(() => jest.fn());
jest.spyOn(trace, 'beginSubsegment').mockImplementation(() => jest.fn());
jest.spyOn(trace, 'endSubsegment').mockImplementation(() => jest.fn());

describe('Handler', () => {
    beforeEach(() => {
        mockedSecretManagerClient.reset();
    });

    it('should run handler successfully', async () => {
        const event = {};
        const context = { callbackWaitsForEmptyEventLoop : true };

        const connection = jest.spyOn(db, 'connection').mockImplementation(() => jest.fn());
        const query = jest.spyOn(db, 'query').mockImplementation((sqlQuery, processErrOrResults) => processErrOrResults(null, results));
        const callback = jest.fn((err, event) => event);

        mockedSecretManagerClient.on(GetSecretValueCommand, {
            SecretId: process.env.SECRET_NAME
        }).resolves(secretJsonResponse);

        await handler(event, context, callback);

        expect(context.callbackWaitsForEmptyEventLoop).toBeFalsy();
        expect(mockedSecretManagerClient).toHaveReceivedCommandTimes(GetSecretValueCommand, 1);
        expect(connection).toBeCalledWith(expectedSecretResponse.SecretString);
        expect(query).toBeCalledWith(expectedSqlQuery, expect.any(Function));
        expect(callback).toBeCalledWith(null, results);
    });

    it('should pass error to lambda callback when sql query return error', async () => {
        const event = {};
        const context = { callbackWaitsForEmptyEventLoop : true };
        const expectedError = new Error("Timeout");

        const connection = jest.spyOn(db, 'connection').mockImplementation(() => jest.fn());
        const query = jest.spyOn(db, 'query').mockImplementation((sqlQuery, processErrOrResults) => processErrOrResults(expectedError));
        const callback = jest.fn((err) => err);

        mockedSecretManagerClient.on(GetSecretValueCommand, {
            SecretId: process.env.SECRET_NAME
        }).resolves(secretJsonResponse);

        await handler(event, context, callback);

        expect(context.callbackWaitsForEmptyEventLoop).toBeFalsy();
        expect(mockedSecretManagerClient).toHaveReceivedCommandTimes(GetSecretValueCommand, 1);
        expect(connection).toBeCalledWith(expectedSecretResponse.SecretString);
        expect(query).toBeCalledWith(expectedSqlQuery, expect.any(Function));
        expect(callback).toBeCalledWith(expectedError);
    });
});