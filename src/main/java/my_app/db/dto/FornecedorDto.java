package my_app.db.dto;

public record FornecedorDto(String nome, String cnpj, String telefone, String email, String endereco, Long dataCriacao) {
    public FornecedorDto {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome não pode ser nulo ou vazio");
        }
    }
}