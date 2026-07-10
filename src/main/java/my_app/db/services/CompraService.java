package my_app.db.services;

import my_app.db.DB;
import my_app.db.dto.CompraDto;
import my_app.db.models.CompraModel;
import my_app.db.repositories.ComprasRepository;
import net.sf.persism.Session;

import java.math.BigDecimal;
import java.sql.SQLException;

public class CompraService extends BaseService<CompraModel> {

    private final ComprasRepository comprasRepository;

    public CompraService() throws SQLException {
        this(DB.getPersismSession());
    }

    public CompraService(Session session) {
        super(new ComprasRepository(session));
        this.comprasRepository = (ComprasRepository) repository;
    }

    public CompraModel salvar(CompraDto dto) throws SQLException {
        var model = toModel(dto);
        model.setDataCriacaoMillis(System.currentTimeMillis());
        return repository.salvar(model);
    }

    @Override
    public void atualizar(CompraModel model) throws SQLException {
        super.atualizar(model);
    }

    public CompraModel toModel(CompraDto dto) {
        var model = new CompraModel();
        model.setProdutoCod(dto.produtoCod());
        model.setFornecedorId(dto.fornecedorId());
        model.setQuantidade(dto.quantidade());
        model.setPrecoDeCompra(dto.precoCompra());
        model.setDescontoEmReais(dto.descontoEmReais());
        model.setTipoPagamento(dto.tipoPagamento());
        model.setObservacao(dto.observacao());
        model.setDataCompra(dto.dataCompra());
        model.setNumeroNota(dto.numeroNota());
        model.setDataValidade(dto.dataValidade());
        model.setTotalLiquido(dto.totalLiquido());
        return model;
    }

    public BigDecimal somarComprasPorPeriodo(Long dataInicio, Long dataFim) throws SQLException {
        if (dataInicio >= dataFim)
            throw new IllegalArgumentException("Data de início deve ser anterior à data de fim");
        return comprasRepository.somarComprasPorPeriodo(dataInicio, dataFim);
    }

    public CompraModel toModel(CompraDto dto, long id, long dataCriacaoMillis) {
        var model = toModel(dto);
        model.setId(id);
        model.setDataCriacaoMillis(dataCriacaoMillis);
        return model;
    }
}
