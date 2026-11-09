package br.com.portfolio.tarefas.dto;

import br.com.portfolio.tarefas.model.StatusTarefa;
import jakarta.validation.constraints.NotNull;

/**
 * Dados enviados no endpoint PATCH /tarefas/{id}/status.
 */
public class AtualizarStatusRequest {

    @NotNull(message = "O status e obrigatorio (PENDENTE, EM_ANDAMENTO ou CONCLUIDA)")
    private StatusTarefa status;

    public StatusTarefa getStatus() {
        return status;
    }

    public void setStatus(StatusTarefa status) {
        this.status = status;
    }
}
