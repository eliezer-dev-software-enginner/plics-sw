package my_app.db.dto;

import java.math.BigDecimal;

public class ProdutoDto {
    public String codigoBarras;
    public String descricao;
    public BigDecimal precoCompra;
    public BigDecimal precoVenda;
    public String unidade;
    public String marca;

    public Long categoriaId;
    public Long fornecedorId;

    public BigDecimal estoque;
    public String observacoes;
    public String imagem;
    public String validade;
    public String comissao;
    public String garantia;
}
