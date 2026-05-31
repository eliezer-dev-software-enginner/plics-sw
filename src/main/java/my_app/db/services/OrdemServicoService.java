package my_app.db.services;

import my_app.db.DB;
import my_app.db.models.OrdemServicoModel;
import my_app.db.repositories.OrdemServicoRepository;
import net.sf.persism.Session;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class OrdemServicoService extends BaseService<OrdemServicoModel> {

    private final OrdemServicoRepository ordemServicoRepository;

    public OrdemServicoService() throws SQLException {
        this(DB.getPersismSession());
    }

    public OrdemServicoService(Session session) {
        super(new OrdemServicoRepository(session));
        this.ordemServicoRepository = (OrdemServicoRepository) repository;
    }

    @Override
    public OrdemServicoModel salvar(OrdemServicoModel model) throws SQLException {
        validar(model);
        model.setDataCriacao(LocalDateTime.now());
        if (model.getNumeroOs() == null) {
            model.setNumeroOs(ordemServicoRepository.gerarProximoNumeroOS());
        }
        return repository.salvar(model);
    }

    @Override
    public void atualizar(OrdemServicoModel model) throws SQLException {
        validar(model);
        repository.atualizar(model);
    }

    public void excluir(long id) throws SQLException {
        var os = repository.buscarById(id);
        if (os == null) throw new IllegalArgumentException("Ordem de serviço não encontrada");
        repository.excluirById(id);
    }

    public List<OrdemServicoModel> buscarPorCliente(Integer clienteId) throws SQLException {
        return ordemServicoRepository.buscarPorCliente(clienteId);
    }

    public List<OrdemServicoModel> buscarPorTecnico(Integer tecnicoId) throws SQLException {
        return ordemServicoRepository.buscarPorTecnico(tecnicoId);
    }

    public List<OrdemServicoModel> buscarPorPeriodo(Long dataInicio, Long dataFim) throws SQLException {
        return ordemServicoRepository.buscarPorPeriodo(dataInicio, dataFim);
    }

    public List<OrdemServicoModel> buscarPorStatus(String status) throws SQLException {
        return ordemServicoRepository.buscarPorStatus(status);
    }

    private void validar(OrdemServicoModel model) {
        if (model.getClienteId() == null)
            throw new IllegalArgumentException("Cliente é obrigatório");
        if (model.getTecnicoId() == null)
            throw new IllegalArgumentException("Técnico é obrigatório");
        if (model.getEquipamento() == null || model.getEquipamento().trim().isEmpty())
            throw new IllegalArgumentException("Equipamento é obrigatório");
        if (model.getTotalLiquido() == null || model.getTotalLiquido().compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Total líquido inválido");
        if (model.getStatus() == null) model.setStatus("Orçamento");
        if (model.getMaoDeObraValor() == null) model.setMaoDeObraValor(BigDecimal.ZERO);
        if (model.getPecasValor() == null) model.setPecasValor(BigDecimal.ZERO);
    }
}
