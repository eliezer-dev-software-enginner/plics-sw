package my_app.db.services;

import my_app.db.DB;
import my_app.db.dto.CompraDto;
import my_app.db.models.CompraModel;
import my_app.db.repositories.ComprasRepository;
import net.sf.persism.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.SQLException;

public class CompraService extends BaseService<CompraModel> {

    private static final Logger log = LoggerFactory.getLogger(CompraService.class);

    private final ComprasRepository comprasRepository;

    public CompraService() throws SQLException {
        this(DB.getPersismSession());
    }

    public CompraService(Session session) {
        super(new ComprasRepository(session));
        this.comprasRepository = (ComprasRepository) repository;
    }

    public CompraModel salvar(CompraDto dto) throws SQLException {
        return salvar(toModel(dto));
    }

    @Override
    public CompraModel salvar(CompraModel model) throws SQLException {
        model.setDataCriacaoMillis(System.currentTimeMillis());
        var salvo = repository.salvar(model);
        log.info("Compra salva: id={} produtoCod={} fornecedorId={}", salvo.getId(), salvo.getProdutoCod(), salvo.getFornecedorId());
        return salvo;
    }

    @Override
    public void atualizar(CompraModel model) throws SQLException {
        // repository.atualizar() direto (não super.atualizar()) pra não duplicar o log
        // genérico de BaseService com este, mais detalhado.
        repository.atualizar(model);
        log.info("Compra atualizada: id={} produtoCod={} fornecedorId={}", model.getId(), model.getProdutoCod(), model.getFornecedorId());
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
