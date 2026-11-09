package br.com.portfolio.tarefas.repository;

import br.com.portfolio.tarefas.model.StatusTarefa;
import br.com.portfolio.tarefas.model.Tarefa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Acesso aos dados da tabela "tarefas".
 *
 * Ao estender JpaRepository, ganhamos automaticamente metodos como
 * save, findById, findAll e deleteById, sem precisar escrever SQL.
 */
@Repository
public interface TarefaRepository extends JpaRepository<Tarefa, Long> {

    /**
     * Metodo derivado: o Spring Data gera a consulta a partir do nome.
     * "findByStatus" vira "SELECT * FROM tarefas WHERE status = ?".
     */
    List<Tarefa> findByStatus(StatusTarefa status);
}
