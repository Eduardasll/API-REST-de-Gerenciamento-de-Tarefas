package br.com.portfolio.tarefas.dto;

import br.com.portfolio.tarefas.model.Prioridade;
import br.com.portfolio.tarefas.model.StatusTarefa;
import br.com.portfolio.tarefas.model.Tarefa;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Dados que a API DEVOLVE ao cliente.
 *
 * E um "record" (Java 17): uma classe imutavel e enxuta para transportar dados.
 * Converter a entidade em um Response evita expor detalhes internos do banco.
 */
public record TarefaResponse(
        Long id,
        String titulo,
        String descricao,
        StatusTarefa status,
        Prioridade prioridade,
        LocalDate prazo,
        LocalDateTime criadaEm
) {

    /**
     * Fabrica que converte uma entidade Tarefa neste objeto de resposta.
     */
    public static TarefaResponse de(Tarefa tarefa) {
        return new TarefaResponse(
                tarefa.getId(),
                tarefa.getTitulo(),
                tarefa.getDescricao(),
                tarefa.getStatus(),
                tarefa.getPrioridade(),
                tarefa.getPrazo(),
                tarefa.getCriadaEm()
        );
    }
}
