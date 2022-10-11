const db = require('./db');

const selectTalks = "SELECT t.date, t.title as 'titre', \
s.username as 'speaker', t.description \
FROM talk t JOIN speaker s \
ON t.speaker_id = s.id \
ORDER BY t.date,t.title";

exports.handler = (event, context, callback) => {

    console.log("event = ", event);
    console.log("context = ", context);

    context.callbackWaitsForEmptyEventLoop = false;

    db.connection();
    db.query(selectTalks, (err, results) => {
        if (err) {
            return callback(err);
        }
        
        callback(null, results);
    });
};