package my_app.db.dto;

public record CategoriaDto(String nome, Long dataCriacao) {
    public CategoriaDto {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome não pode ser nulo ou vazio");
        }
    }
}