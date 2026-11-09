package br.com.portfolio.tarefas.exception;

/**
 * Lancada quando uma tarefa com o ID informado nao existe.
 * O tratador global converte esta excecao em uma resposta HTTP 404.
 */
public class TarefaNaoEncontradaException extends RuntimeException {

    public TarefaNaoEncontradaException(Long id) {
        super("Tarefa nao encontrada");
    }
}
