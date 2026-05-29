package my_app.db.models_old;

import my_app.db.dto.ClienteDto;
import my_app.domain.ModelBase;
import my_app.domain.SqlField;

public class ClienteModel extends ModelBase<ClienteDto> {
    @SqlField(name = "nome", type = "string")
    public String nome;
    @SqlField(name = "cpf_cnpj", type = "string")
    public String cpfCnpj;
    @SqlField(name = "celular", type = "string")
    public String celular;
    @SqlField(name = "email", type = "string")
    public String email;

    @Override
    public ClienteModel fromIdAndDtoAndMillis(Long id, ClienteDto clienteDto, long millis) {
        this.id = id;
        this.dataCriacao = millis;
        this.nome = clienteDto.nome();
        this.cpfCnpj = clienteDto.cnpj();
        this.email = clienteDto.email();
        this.celular = clienteDto.telefone();

        return this;
    }
}