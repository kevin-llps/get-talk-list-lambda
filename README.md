# Get Talk List Lambda

L'objectif de cette lambda est de renvoyer la liste des `talk` (présents en base).

## Format de la requête

La lambda sera déclenchée par un événement (appel d'endpoint)
mais aucun attribut ne sera transmis à la lambda via cet événement.

## Format de la réponse

```
[
    {
        "date": "date",
        "titre": "string",
        "speaker": "string",
        "description": "string"
    },
    {
        "date": "date",
        "titre": "string",
        "speaker": "string",
        "description": "string"
    },
    {
        ...
    }
]
```