package my_app.screens.termoUsoScreen;

import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.components.*;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Container;
import megalodonte.props.ContainerProps;
import megalodonte.router.v4.ScreenContext;
import my_app.domain.Data;
import my_app.domain.components.Components;

public class TermoUsoScreen implements ScreenComponent {

    public TermoUsoScreen(ScreenContext ctx) {}

    public Component render() {
        return new Scroll(new Container(new ContainerProps().paddingAll(10)).children(
            Components.FormTitle("Política de Privacidade – Plics SW"),
                new SpacerVertical(2),
                new Text("Última atualização: 28 de julho de 2026"),
                new SpacerVertical(2),
                new Text("A sua privacidade é importante para nós. Esta Política de Privacidade explica como o Plics SW coleta, utiliza, armazena e protege as informações dos usuários."),
                new SpacerVertical(2),
                new Text("Ao utilizar o aplicativo, você concorda com os termos desta Política."),
                new SpacerVertical(10),
                Components.FormSubtitle("1. Sobre o Plics SW"),
                new SpacerVertical(2),
                new Text("O Plics SW é um sistema de gestão empresarial (ERP) desenvolvido para auxiliar pequenos e médios negócios no gerenciamento de clientes, fornecedores, produtos, estoque, vendas, financeiro e demais rotinas administrativas."),
                new SpacerVertical(10),
                Components.FormSubtitle("2. Informações coletadas"),
                new TextFlow(new Text("O Plics SW foi desenvolvido para funcionar principalmente de forma local (offline). Os dados cadastrados pelo usuário permanecem armazenados no computador onde o aplicativo está instalado.\n" +
                        "\n" +
                        "Dependendo dos recursos utilizados, o aplicativo poderá coletar as seguintes informações:\n" +
                        "\n" +
                        "Nome do usuário;\n" +
                        "Endereço de e-mail;\n" +
                        "Informações da licença do software;\n" +
                        "Identificador do dispositivo para validação da licença;\n" +
                        "Informações técnicas do aplicativo (versão instalada, sistema operacional e registros de erros);\n" +
                        "Dados necessários para atualização do aplicativo.\n" +
                        "\n" +
                        "O Plics SW não coleta automaticamente informações financeiras, bancárias ou dados pessoais além daqueles necessários para o funcionamento do software.")),
                new SpacerVertical(10),
                Components.FormSubtitle("3. Dados cadastrados pelo usuário"),
                new SpacerVertical(2),
                new TextFlow(new Text("As informações inseridas pelo usuário, como:\n" +
                        "\n" +
                        "Clientes;\n" +
                        "Fornecedores;\n" +
                        "Produtos;\n" +
                        "Estoque;\n" +
                        "Vendas;\n" +
                        "Ordens de serviço;\n" +
                        "Informações financeiras;\n" +
                        "Demais registros criados dentro do sistema;\n" +
                        "\n" +
                        "são armazenadas localmente no computador do usuário e pertencem exclusivamente ao próprio usuário.\n" +
                        "\n" +
                        "Esses dados não são enviados aos nossos servidores, exceto quando alguma funcionalidade específica exigir essa comunicação e houver autorização do usuário.")),
                new SpacerVertical(10),
                Components.FormSubtitle("4. Como utilizamos as informações"),
                new SpacerVertical(2),
                new TextFlow(new Text("As informações coletadas podem ser utilizadas para:\n" +
                        "\n" +
                        "validar a licença do software;\n" +
                        "disponibilizar atualizações;\n" +
                        "melhorar a estabilidade do aplicativo;\n" +
                        "corrigir erros;\n" +
                        "oferecer suporte técnico;\n" +
                        "prevenir uso indevido ou fraude.")),
                new SpacerVertical(10),
                Components.FormSubtitle("5. Compartilhamento de informações"),
                new TextFlow(new Text("As informações coletadas podem ser utilizadas para:\n" +
                        "\n" +
                        "validar a licença do software;\n" +
                        "disponibilizar atualizações;\n" +
                        "melhorar a estabilidade do aplicativo;\n" +
                        "corrigir erros;\n" +
                        "oferecer suporte técnico;\n" +
                        "prevenir uso indevido ou fraude.")),
                new SpacerVertical(10),
                Components.FormSubtitle("6. Segurança"),
                new TextFlow(new Text("Adotamos medidas técnicas e organizacionais para proteger as informações contra acesso não autorizado, alteração, divulgação ou destruição.\n" +
                        "\n" +
                        "Apesar dos nossos esforços, nenhum sistema é totalmente seguro. Recomendamos que o usuário mantenha cópias de segurança (backups) de seus dados regularmente.")),
                new SpacerVertical(10),
                Components.FormSubtitle("7. Atualizações do aplicativo"),
                new TextFlow(new Text("O Plics SW poderá verificar a existência de novas versões pela internet para oferecer atualizações automáticas ou informar a disponibilidade de uma nova versão.\n" +
                        "\n" +
                        "Durante esse processo poderão ser transmitidas apenas as informações necessárias para identificar a versão instalada e realizar a atualização.")),
                new SpacerVertical(10),
                Components.FormSubtitle("8. Serviços de terceiros"),
                new TextFlow(new Text("O aplicativo poderá utilizar serviços de terceiros para funcionalidades específicas, como:\n" +
                        "\n" +
                        "verificação de licença;\n" +
                        "distribuição de atualizações;\n" +
                        "armazenamento de registros de erros;\n" +
                        "suporte ao usuário.\n" +
                        "\n" +
                        "Cada serviço possui sua própria política de privacidade.")),
                new SpacerVertical(10),
                Components.FormSubtitle("9. Direitos do usuário"),
                new TextFlow(new Text("O usuário poderá, a qualquer momento:\n" +
                        "\n" +
                        "solicitar informações sobre os dados eventualmente armazenados;\n" +
                        "solicitar correções;\n" +
                        "solicitar exclusão de informações que estejam sob responsabilidade do desenvolvedor, quando aplicável;\n" +
                        "entrar em contato para esclarecimentos sobre esta Política.")),
                new SpacerVertical(10),
                Components.FormSubtitle("10. Alterações nesta Política"),
                new TextFlow(new Text("Esta Política poderá ser atualizada periodicamente.\n" +
                        "\n" +
                        "Sempre que houver alterações relevantes, uma nova versão será disponibilizada juntamente com a data de atualização.")),
                new SpacerVertical(10),
                Components.FormSubtitle("11. Contato"),
                new TextFlow(new Text("Caso tenha dúvidas sobre esta Política de Privacidade, entre em contato:\n" +
                        "\n" +
                        "Desenvolvedor: Eliezer Dev\n" +
                        "\n" +
                        "E-mail: eliezerassuncaocustodio@hotmail.com\n" +
                        "\n" +
                        "Site: " + Data.linkWebsiteOfficial)),
                new SpacerVertical(10),
                Components.FormSubtitle("12. Legislação aplicável"),
                new TextFlow(new Text("Esta Política é regida pelas leis da República Federativa do Brasil, especialmente pela Lei Geral de Proteção de Dados (Lei nº 13.709/2018 – LGPD), sem prejuízo das normas aplicáveis da Microsoft Store e demais legislações eventualmente incidentes.\n" +
                        "\n" +
                        "Ao utilizar o Plics SW, o usuário declara ter lido e concordado com esta Política de Privacidade."))
        ));
    }
}
