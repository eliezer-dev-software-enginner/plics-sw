package my_app.domain.states;

import megalodonte.ComputedState;
import megalodonte.base.state.State;
import my_app.utils.Utils;

import java.math.BigDecimal;

public class TotaisState {
    public final ComputedState<String> totalBruto;
    public final ComputedState<String> totalLiquido;
    public final ComputedState<String> descontoComputed;

    public TotaisState(State<String> preco, State<String> qtd, State<String> desconto) {
        this.totalBruto = ComputedState.of(() -> {
            BigDecimal qtdValue = qtd.get().trim().isEmpty()
                    ? BigDecimal.ZERO : new BigDecimal(qtd.get());
            BigDecimal precoValue = new BigDecimal(preco.get()).movePointLeft(2);
            return Utils.toBRLCurrency(qtdValue.multiply(precoValue));
        }, qtd, preco, desconto);

        this.totalLiquido = ComputedState.of(() -> {
            BigDecimal qtdValue = qtd.get().trim().isEmpty()
                    ? BigDecimal.ZERO : new BigDecimal(qtd.get());
            BigDecimal precoValue = new BigDecimal(preco.get()).movePointLeft(2);
            BigDecimal descontoValue = new BigDecimal(desconto.get()).movePointLeft(2);
            return qtdValue.multiply(precoValue).subtract(descontoValue).toString();
        }, qtd, preco, desconto);

        this.descontoComputed = ComputedState.of(
                () -> Utils.toBRLCurrency(Utils.deCentavosParaReal(desconto.get())),
                desconto);
    }
}