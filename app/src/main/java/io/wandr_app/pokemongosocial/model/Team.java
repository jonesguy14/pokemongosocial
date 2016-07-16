package io.wandr_app.pokemongosocial.model;

/**
 * Created by kylel on 7/16/2016.
 */
public enum Team {
    MYSTIC("Mystic"), VALOR("Valor"), INSTINCT("Instinct"), UNKNOWN("Unknown");
    private final String name;

    Team(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static Team fromString(String teamName) {
        switch (teamName) {
            case "Instinct":
                return INSTINCT;
            case "Mystic":
                return MYSTIC;
            case "Valor":
                return VALOR;
        }
        return UNKNOWN;
    }
}
