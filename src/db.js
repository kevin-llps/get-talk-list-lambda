import * as mysql from "mysql2";

let dbConnection;

export const connection = (dbSecret) => {
    dbConnection = mysql.createConnection(dbSecret);
};

export const query = (sqlQuery, params, processErrOrResults) => {
    dbConnection.query(sqlQuery, params, processErrOrResults);
};
