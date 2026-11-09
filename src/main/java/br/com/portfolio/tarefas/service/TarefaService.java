package br.com.portfolio.tarefas.service;

import br.com.portfolio.tarefas.dto.TarefaRequest;
import br.com.portfolio.tarefas.exception.TarefaNaoEncontradaException;
import br.com.portfolio.tarefas.model.StatusTarefa;
import br.com.portfolio.tarefas.model.Tarefa;
import br.com.portfolio.tarefas.repository.TarefaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Camada de regras de negocio.
 *
 * O Controller fala com o Service, e o Service fala com o Repository.
 * Assim, as regras ficam concentradas aqui, e nao espalhadas pelas rotas.
 */
@Service
public class TarefaService {

    private final TarefaRepository repository;

    // Injecao de dependencia via construtor: o Spring entrega o repositorio pronto.
    public TarefaService(TarefaRepository repository) {
        this.repository = repository;
    }

    public Tarefa criar(TarefaRequest request) {
        Tarefa tarefa = new Tarefa();
        tarefa.setTitulo(request.getTitulo().trim());
        tarefa.setDescricao(request.getDescricao());
        tarefa.setPrioridade(request.getPrioridade());
        tarefa.setPrazo(request.getPrazo());
        // status e criadaEm sao definidos automaticamente na entidade (@PrePersist).
        return repository.save(tarefa);
    }

    public List<Tarefa> listar(StatusTarefa status) {
        if (status != null) {
            return repository.findByStatus(status);
        }
        return repository.findAll();
    }

    public Tarefa buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new TarefaNaoEncontradaException(id));
    }

    public Tarefa atualizar(Long id, TarefaRequest request) {
        Tarefa tarefa = buscarPorId(id);
        tarefa.setTitulo(request.getTitulo().trim());
        tarefa.setDescricao(request.getDescricao());
        tarefa.setPrioridade(request.getPrioridade());
        tarefa.setPrazo(request.getPrazo());
        return repository.save(tarefa);
    }

    public Tarefa alterarStatus(Long id, StatusTarefa novoStatus) {
        Tarefa tarefa = buscarPorId(id);
        tarefa.setStatus(novoStatus);
        return repository.save(tarefa);
    }

    public void excluir(Long id) {
        Tarefa tarefa = buscarPorId(id);
        repository.delete(tarefa);
    }
}
