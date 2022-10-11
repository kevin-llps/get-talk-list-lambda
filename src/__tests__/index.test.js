const index = require('../index');
const db = require('../db');

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

describe('Handler', () => {
    it('should run handler successfully', () => {
        const event = {};
        const context = { callbackWaitsForEmptyEventLoop : true };
        
        const connection = jest.spyOn(db, 'connection').mockImplementation(() => jest.fn());
        const query = jest.spyOn(db, 'query').mockImplementation((sqlQuery, processErrOrResults) => processErrOrResults(null, results));
        const callback = jest.fn((err, event) => event);

        index.handler(event, context, callback);

        expect(context.callbackWaitsForEmptyEventLoop).toBeFalsy();
        expect(connection).toHaveBeenCalled();
        expect(query).toBeCalledWith(expectedSqlQuery, expect.any(Function));
        expect(callback).toBeCalledWith(null, results);
    });

    it('should pass error to lambda callback when sql query return error', () => {
        const event = {};
        const context = { callbackWaitsForEmptyEventLoop : true };
        const expectedError = new Error("Timeout");
        
        const connection = jest.spyOn(db, 'connection').mockImplementation(() => jest.fn());
        const query = jest.spyOn(db, 'query').mockImplementation((sqlQuery, processErrOrResults) => processErrOrResults(expectedError));
        const callback = jest.fn((err) => err);

        index.handler(event, context, callback);

        expect(context.callbackWaitsForEmptyEventLoop).toBeFalsy();
        expect(connection).toHaveBeenCalled();
        expect(query).toBeCalledWith(expectedSqlQuery, expect.any(Function));
        expect(callback).toBeCalledWith(expectedError);
    });
});