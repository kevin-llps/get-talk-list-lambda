const mysql = require('mysql2');
let connection;

const db = {

    connection() {
        connection = mysql.createConnection({
            host: process.env.RDS_HOSTNAME,
            user: process.env.RDS_USERNAME,
            password: process.env.RDS_PASSWORD,
            port: process.env.RDS_PORT,
            database: process.env.RDS_DATABASE
        });
    },

    query(sqlQuery, params, processErrOrResults) {
        connection.query(sqlQuery, params, processErrOrResults);
    }

};

module.exports = db;
