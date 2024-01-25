import { extract } from "../queryStringExtractor.js";

describe('QueryStringExtractor', () => {
    it('given string completion should return SQL query', () => {
        const completion = ' Here are the SQL queries for the requested operations on the talk table:\n' +
        '1. Count talk records by speaker:\n' +
        '\n' +
        '```sql\n' +
        'SELECT speaker_id, COUNT(*) AS talk_count\n' +
        'FROM talk\n' +
        'GROUP BY speaker_id;\n' +
        '```\n' +
        '\n' +
        'The first query groups the rows by speaker_id and counts the number of talks per speaker using the COUNT() function.';

        const expectedSqlQuery = "SELECT speaker_id, COUNT(*) AS talk_count FROM talk GROUP BY speaker_id;";

        const result = extract(completion);

        expect(result).toEqual(expectedSqlQuery);
    });
});