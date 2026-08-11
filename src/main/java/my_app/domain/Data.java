package my_app.domain;

import my_app.utils.Utils;

import java.util.List;

public class Data {
    public static final List<String> simNaoList = List.of("Sim", "Não");
    public static final List<String> ufList = List.of(
            "AC-Acre", "AL-Alagoas", "AP-Amapá", "AM-Amazonas", "BA-Bahia", "CE-Ceará", "DF-Distrito Federal", "ES-Espírito Santo",
            "GO-Goiás", "MA-Maranhão", "MT-Mato Grosso", "MS-Mato Grosso do Sul", "MG-Minas Gerais", "PA-Pará", "PB-Paraíba", "PR-Paraná",
            "PE-Pernambuco", "PI-Piauí", "RJ-Rio de Janeiro", "RN-Rio Grande do Norte", "RS-Rio Grande do Sul", "RO-Rondônia", "RR-Roraima",
            "SC-Santa Catarina", "SP-São Paulo", "SE-Sergipe", "TO-Tocantins"
    );

    public static final List<String> tiposPagamentoList = List.of("A VISTA", "CRÉDITO", "DÉBITO", "PIX", "A PRAZO");

    /**
     * Unidades de medida suportadas:
     * <ul>
     *   <li>{@code UN} — unidade</li>
     *   <li>{@code KG} — quilograma</li>
     *   <li>{@code g} — grama</li>
     *   <li>{@code ml} — mililitro</li>
     *   <li>{@code L} — litro</li>
     *   <li>{@code CX} — caixa</li>
     *   <li>{@code PCT} — pacote</li>
     *   <li>{@code DZ} — dúzia</li>
     *   <li>{@code PAR} — par</li>
     *   <li>{@code M} — metro</li>
     *   <li>{@code M2} — metro quadrado</li>
     *   <li>{@code M3} — metro cúbico</li>
     *   <li>{@code FD} — fardo</li>
     *   <li>{@code GF} — garrafa</li>
     *   <li>{@code RL} — rolo</li>
     *   <li>{@code SC} — saco/saca</li>
     *   <li>{@code KIT} — kit</li>
     * </ul>
     */
    public static final List<String> unidadesDeMedidaList = List.of(
            "UN", "KG", "g", "ml", "L", "CX", "PCT", "DZ", "PAR", "M", "M2", "M3", "FD", "GF", "RL", "SC", "KIT"
    );
    public static final List<String> tiposPessoaList = List.of("Física", "Jurídica");

    private static final String numberWhatsappSupport = "5532985066537";
    public static final String linkWhatsappSupport = "https://wa.me/"+numberWhatsappSupport;

    public static final String linkWebsiteOfficial = "https://plics-sw-webpage.vercel.app/";

    public static String getNumberWhatsappSupportFormatted() {
        return Utils.formatPhone(numberWhatsappSupport.replace("55",""));
    }

    static void main() {
        IO.println(getNumberWhatsappSupportFormatted());
    }
}
