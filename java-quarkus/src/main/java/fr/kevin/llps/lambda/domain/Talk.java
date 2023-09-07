package fr.kevin.llps.lambda.domain;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.time.LocalDateTime;

@RegisterForReflection
public record Talk(String title,
                   LocalDateTime date,
                   String speaker,
                   String description) {
}
