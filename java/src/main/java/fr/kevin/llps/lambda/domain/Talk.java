package fr.kevin.llps.lambda.domain;

import java.time.LocalDateTime;

public record Talk(String title,
                   LocalDateTime date,
                   String speaker,
                   String description) {
}
