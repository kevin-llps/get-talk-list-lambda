const mysql = require('mysql2');
let connection;

const db = {

    connection(dbSecret) {
        connection = mysql.createConnection({
            host: dbSecret.hostname,
            user: dbSecret.username,
            password: dbSecret.password,
            port: dbSecret.port,
            database: dbSecret.dbname
        });
    },

    query(sqlQuery, params, processErrOrResults) {
        connection.query(sqlQuery, params, processErrOrResults);
    }

};

module.exports = db;
