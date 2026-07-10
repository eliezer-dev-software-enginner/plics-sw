package my_app.domain.states;

import megalodonte.base.state.State;
import my_app.db.models.ClienteModel;
import my_app.db.models.FornecedorModel;
import my_app.domain.Data;

public class EnderecoState {
    public State<String> ufSelected = State.of(Data.ufList.getFirst());
    public State<String> cidade = State.of("");
    public State<String> bairro = State.of("");
    public State<String> rua = State.of("");
    public State<String> numero = State.of("");
    public State<String> cep = State.of("");

    public void populateFromFornecedorModel(FornecedorModel model){
        cep.set(model.getCep());
        ufSelected.set(model.getUfSelected());
        cidade.set(model.getCidade());
        bairro.set(model.getBairro());
        rua.set(model.getRua());
        numero.set(model.getNumero());
    }
    public void populateFromClienteModel(ClienteModel model){
        cep.set(model.getCep());
        ufSelected.set(model.getUf());
        cidade.set(model.getCidade());
        bairro.set(model.getBairro());
        rua.set(model.getRua());
        numero.set(model.getNumero());
    }

    public void clear() {
        cep.set("");
        ufSelected.set(Data.ufList.getFirst());
        cidade.set("");
        bairro.set("");
        rua.set("");
        numero.set("");
    }
}
