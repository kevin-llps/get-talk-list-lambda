import * as mysql from "mysql2";

let dbConnection;

export const connection = (dbSecret) => {
    dbConnection = mysql.createConnection({
        host: dbSecret.host,
        user: dbSecret.username,
        password: dbSecret.password,
        port: dbSecret.port,
        database: dbSecret.dbname
    });
};

export const query = (sqlQuery, params, processErrOrResults) => {
    dbConnection.query(sqlQuery, params, processErrOrResults);
};
