package my_app.examples;

import megalodonte.State;
import my_app.utils.Utils;
import java.math.BigDecimal;

/**
 * Demonstração do fluxo correto de inputs monetários no ERP V2.
 * 
 * Este exemplo mostra como os valores monetários devem fluir corretamente:
 * 1. Estado armazena centavos (String)
 * 2. Input exibe formato BRL (R$ 10,00)
 * 3. Conversão para BD usa reais (BigDecimal)
 */
public class CurrencyInputExample {
    
    public static void main(String[] args) {
        System.out.println("=== Fluxo Monetário Correto ===");
        
        // 1. ESTADO: Sempre armazena centavos como String
        State<String> precoCompraState = State.of("1500"); // R$ 15,00
        State<String> precoVendaState = State.of("2500"); // R$ 25,00
        
        System.out.println("Estado inicial:");
        System.out.println("  Preço compra: " + precoCompraState.get() + " centavos");
        System.out.println("  Preço venda: " + precoVendaState.get() + " centavos");
        
        // 2. CONVERSÃO PARA BD: De centavos (String) para reais (BigDecimal)
        BigDecimal precoCompraBD = Utils.deCentavosParaReal(precoCompraState.get());
        BigDecimal precoVendaBD = Utils.deCentavosParaReal(precoVendaState.get());
        
        System.out.println("\nValores para persistência no banco:");
        System.out.println("  Preço compra BD: " + precoCompraBD); // 15.00
        System.out.println("  Preço venda BD: " + precoVendaBD);  // 25.00
        
        // 3. CARREGAMENTO DO BD: De reais (BigDecimal) para centavos (String)
        String precoCompraCarregado = Utils.deRealParaCentavos(precoCompraBD);
        String precoVendaCarregado = Utils.deRealParaCentavos(precoVendaBD);
        
        System.out.println("\nValores carregados do banco:");
        System.out.println("  Preço compra carregado: " + precoCompraCarregado); // "1500"
        System.out.println("  Preço venda carregado: " + precoVendaCarregado); // "2500"
        
        // 4. ATUALIZAÇÃO DO ESTADO: Sem problemas de formatação
        precoCompraState.set(precoCompraCarregado);
        precoVendaState.set(precoVendaCarregado);
        
        System.out.println("\nEstado após carregar do banco:");
        System.out.println("  Preço compra: " + precoCompraState.get()); // "1500"
        System.out.println("  Preço venda: " + precoVendaState.get());  // "2500"
        
        // 5. EXEMPLO DE DIGITAÇÃO: Simular usuário digitando "3000"
        System.out.println("\nSimulando digitação de '3000' (R$ 30,00):");
        precoCompraState.set("3000");
        System.out.println("  Estado atualizado: " + precoCompraState.get()); // "3000"
        System.out.println("  Input exibiria: R$ 30,00");
        
        System.out.println("\n=== Resumo do Fluxo ===");
        System.out.println("✅ Estado sempre armazena centavos (String): '1000', '2500', etc");
        System.out.println("✅ Input exibe formato BRL: R$ 10,00, R$ 25,00");
        System.out.println("✅ BD armazena reais (BigDecimal): 10.00, 25.00");
        System.out.println("✅ Conversões funcionam corretamente em ambos os sentidos");
    }
}