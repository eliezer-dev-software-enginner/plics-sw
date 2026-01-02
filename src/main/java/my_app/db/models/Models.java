package my_app.db.models;

import java.math.BigDecimal;

public class Models {
    public static class Produto {
        public Long id;
        public String codigoBarras;
        public String descricao;
        public BigDecimal precoCompra;
        public BigDecimal precoVenda;
        public BigDecimal margem;
        public BigDecimal lucro;
        public String unidade;
        public String categoria;
        public String fornecedor;
        public Integer estoque;
        public String observacoes;
        public String imagem;
    }
}
