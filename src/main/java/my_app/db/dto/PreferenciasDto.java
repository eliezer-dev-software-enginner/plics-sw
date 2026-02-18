package my_app.db.dto;

public record PreferenciasDto(
        String tema,
        String login,
        String senha,
        int credentiaisHabilitadas,
        int primeiroAcesso
){}