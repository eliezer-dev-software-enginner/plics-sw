package my_app.screens;

import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.components.*;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Container;
import megalodonte.props.ContainerProps;
import megalodonte.router.v4.ScreenContext;
import my_app.domain.components.Components;

import java.util.List;

public class InfoUpdateScreen implements ScreenComponent {
    private final ScreenContext ctx;
    private final InfoUpdateScreenViewModel vm;

    private record NotaAtualizacao(String title, String[] notes){}

    private final List<NotaAtualizacao> notasAtualizacaoList = List.of(
            new NotaAtualizacao("v1.0.3", new String[]{
                    "Feat: Escolhe se cliente é físico ou juridico, isso habilita campo com base no tipo selecionado",
                    "Fix: Não permite dois clientes com o mesmo cpf ou cnpj",
                    "Fix: Correção em Ordem de serviço. Edição só é habilitado se clicar em uma célula.",
                    "Fix: Ao limpar formulário mantém o \"cliente padrão\" e tipo pagamento \"crédito\"",
                    "Fix: Alterações no Tecnico agora afetam a tela de Ordem de serviço dinamicamente",
                    "Fix: Data de visita em Ordem de Serviço não tem mais o horário 00:00",
                    "Fix: correção e validação completa na tela de Categorias",
                    "Fix: correção na exibição dos totais na tela de Compra de mercadoria"
            })
    );

    public InfoUpdateScreen(ScreenContext ctx) {
        this.ctx = ctx;
        this.vm = new InfoUpdateScreenViewModel();
    }

    public Component render() {
        var container = new Container();

        notasAtualizacaoList.forEach(it -> {
            var columnInsideCard = new Column();
            columnInsideCard.c_child(Components.FormTitle(it.title));
            columnInsideCard.c_child(new LineHorizontal());
            columnInsideCard.c_child(new SpacerHorizontal(10));

            for (String note : it.notes) {
                columnInsideCard.c_child(new Text(note));
            }
            container.c_child(columnInsideCard);
        });

        return new Container(new ContainerProps().paddingAll(10)).children(
                new Card(container)
        );
    }
}
