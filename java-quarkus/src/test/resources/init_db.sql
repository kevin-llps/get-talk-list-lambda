CREATE TABLE speaker
(
    id       int          NOT NULL PRIMARY KEY,
    username VARCHAR(200) NOT NULL
);

CREATE TABLE talk
(
    id          int           NOT NULL PRIMARY KEY,
    title       VARCHAR(100)  NOT NULL,
    description VARCHAR(1000) NOT NULL,
    date        DATETIME      NOT NULL,
    speaker_id  int           NOT NULL,
    FOREIGN KEY (speaker_id) REFERENCES speaker (id)
);

insert into speaker
values (1, 'kevin.llopis');

insert into talk
values (1, "AWS Cognito",
        "Après 2 ans à travailler sur la mise en place de cette solution au PMU, Kévin LLOPIS nous présentera son retour d'expérience en détaillant les points forts et les points faibles de Cognito.",
        '2022-10-13 19:00:00', 1);
