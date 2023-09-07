import * as mysql from "mysql2";
import { getSegment, beginSubsegment, endSubsegment } from "./trace.js";

let dbConnection;

export const connection = (dbSecret) => {
    const segment = getSegment();
    const databaseConnectionSubsegment = beginSubsegment(segment, 'Database connection');

    dbConnection = mysql.createConnection({
        host: dbSecret.host,
        user: dbSecret.username,
        password: dbSecret.password,
        port: dbSecret.port,
        database: dbSecret.dbname
    });

    endSubsegment(databaseConnectionSubsegment);
};

export const query = (sqlQuery, params, processErrOrResults) => {
    const segment = getSegment();
    const executionSqlQuerySubsegment = beginSubsegment(segment, 'Execute : Get Talks SQL query');

    dbConnection.query(sqlQuery, params, processErrOrResults);

    endSubsegment(executionSqlQuerySubsegment);
};
